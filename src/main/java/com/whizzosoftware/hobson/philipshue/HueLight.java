/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.device.AbstractHobsonDevice;
import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.philipshue.api.ColorConversion;
import com.whizzosoftware.hobson.philipshue.api.dto.Light;
import com.whizzosoftware.hobson.philipshue.api.dto.LightState;
import com.whizzosoftware.hobson.philipshue.api.dto.GetLightAttributeAndStateRequest;
import com.whizzosoftware.hobson.philipshue.api.dto.SetLightStateRequest;
import com.whizzosoftware.hobson.philipshue.state.StateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that represents a Hobson device for a Hue light.
 *
 * @author Dan Noguerol
 */
public class HueLight extends AbstractHobsonDevice {
    private Logger logger = LoggerFactory.getLogger(getClass());

    private final String model;
    private StateContext context;
    private Boolean initialOnValue;
    private Integer initialLevel;
    private String initialColor;

    /**
     * Constructor.
     *
     * @param plugin the HobsonPlugin that created this device
     * @param id the ID of the device on the Hue network
     * @param model the device model
     * @param defaultName the device's default name
     * @param context the StateContext instance to use
     */
    public HueLight(HuePlugin plugin, String id, String model, String defaultName, StateContext context) {
        super(plugin, id);
        this.model = model;
        setDefaultName(defaultName);
        this.context = context;
    }

    public HueLight(HuePlugin plugin, String id, String model, String defaultName, StateContext context, Light light) {
        this(plugin, id, model, defaultName, context);

        if (light != null && light.getState() != null) {
            initialOnValue = light.getState().getOn();
            initialLevel = convertBrightnessToLevel(light.getState().getBrightness());
            initialColor = convertLightStateToColor(light.getState());
        }
    }

    @Override
    public void onStartup(PropertyContainer config) {
        publishVariable(VariableConstants.COLOR, initialColor, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.LEVEL, initialLevel, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.ON, initialOnValue, HobsonVariable.Mask.READ_WRITE);
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public DeviceType getType() {
        return DeviceType.LIGHTBULB;
    }

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.ON;
    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return null;
    }

    @Override
    public void onSetVariable(String variableName, Object value) {
        logger.debug("Setting variable for device {} ({}={})", getContext(), variableName, value);

        Boolean on = null;
        Integer brightness = null;
        Integer red = null;
        Integer green = null;
        Integer blue = null;

        if (VariableConstants.ON.equals(variableName)) {
            if (value instanceof String) {
                on = Boolean.parseBoolean((String) value);
            } else if (value instanceof Boolean) {
                on = (Boolean)value;
            }
        } else if (VariableConstants.COLOR.equals(variableName)) {
            logger.debug("Setting variable for device {} ({}={})", getContext(), variableName, value);
            ColorConversion.Color color = ColorConversion.createColorFromRGBString((String)value);
            if (color != null) {
                red = color.r;
                green = color.g;
                blue = color.b;
            }
        } else if (VariableConstants.LEVEL.equals(variableName)) {
            brightness = convertLevelToBrightness((Integer)value);
        }

        if (on != null || brightness != null || red != null) {
            LightState state = new LightState(on, brightness, red, green, blue, null, model, null);
            logger.debug("New state for device {} is {}", getContext(), state);
            context.sendSetLightStateRequest(new SetLightStateRequest(getContext().getDeviceId(), state));
        }
    }

    public void refresh() {
        logger.debug("Refreshing device {}", getContext());
        context.sendGetLightAttributeAndStateRequest(new GetLightAttributeAndStateRequest(getContext().getDeviceId()));
    }

    public void onLightState(LightState state) {
        if (state != null) {
            List<VariableUpdate> updates = null;

            long now = System.currentTimeMillis();

            // set the check-in time
            checkInDevice(state.isReachable() ? now : null);

            HobsonVariable var = getVariable(VariableConstants.ON);
            if (var != null) {
                Boolean on = state.getOn();
                if (!state.isReachable()) {
                    on = null;
                }
                if ((var.getValue() == null && on != null) || (var.getValue() != null && !var.getValue().equals(on))) {
                    logger.debug("Detected change in on status for {}: {} (old value was {})", getContext(), state.getOn(), var.getValue());
                    updates = new ArrayList<>();
                    updates.add(new VariableUpdate(getContext(), VariableConstants.ON, on, now));
                }
            }

            var = getVariable(VariableConstants.COLOR);
            if (var != null) {
                String color = convertLightStateToColor(state);
                if (!state.isReachable()) {
                    color = null;
                }
                if ((var.getValue() == null && color != null) || (color != null && !var.getValue().equals(color))) {
                    logger.debug("Detected change in color status for {}: {} (old value was {})", getContext(), color, var.getValue());
                    if (updates == null) {
                        updates = new ArrayList<>();
                    }
                    updates.add(new VariableUpdate(getContext(), VariableConstants.COLOR, color, now));
                }
            }

            var = getVariable(VariableConstants.LEVEL);
            if (var != null) {
                Integer level = convertBrightnessToLevel(state.getBrightness());
                if (!state.isReachable()) {
                    level = null;
                }
                if ((var.getValue() == null && level != null) || (level != null && !var.getValue().equals(level))) {
                    logger.debug("Detected change in level status for {}: {} (old value was {})", getContext(), level, var.getValue());
                    if (updates == null) {
                        updates = new ArrayList<>();
                    }
                    updates.add(new VariableUpdate(getContext(), VariableConstants.LEVEL, level, now));
                }
            }

            if (updates != null) {
                fireVariableUpdateNotifications(updates);
            }
        }
    }

    public void onLightStateFailure(Throwable t) {
        logger.debug("Received failure for light " + getContext(), t);

        // set all variables to null to indicate that the current state of this light is now unknown
        long now = System.currentTimeMillis();
        List<VariableUpdate> updates = new ArrayList<>();
        updates.add(new VariableUpdate(getContext(), VariableConstants.ON, null, now));
        updates.add(new VariableUpdate(getContext(), VariableConstants.COLOR, null, now));
        updates.add(new VariableUpdate(getContext(), VariableConstants.LEVEL, null, now));
        fireVariableUpdateNotifications(updates);
    }

    protected Integer convertBrightnessToLevel(Integer brightness) {
        if (brightness != null) {
            if (brightness == 0) {
                return 0;
            } else if (brightness == 255) {
                return 100;
            } else {
                int l = (int)(brightness / 255.0 * 100.0) + 1;
                if (brightness > 0 && l == 0) {
                    l++;
                }
                return l;
            }
        } else {
            return null;
        }
    }
      
    protected Integer convertLevelToBrightness(Integer level) {
        if (level != null) {
            if (level == 0) {
                return 0;
            } else if (level == 100) {
                return 255;
            } else {
                return (int)(255.0 * (level / 100.0));
            }
        } else {
            return null;
        }
    } 

    protected String convertLightStateToColor(LightState state) {
        if (state != null && state.getX() != null && state.getY() != null) {
            ColorConversion.Color c = ColorConversion.colorFromXY(new ColorConversion.PointF(state.getX(), state.getY()), model);
            return "rgb(" + c.r + "," + c.g + "," + c.b + ")";
        } else {
            return null;
        }
    }
}
