/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.color.Color;
import com.whizzosoftware.hobson.api.device.AbstractHobsonDevice;
import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableContext;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
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
            initialColor = convertLightStateToColor(light.getState());
        }
    }

    @Override
    public void onStartup(PropertyContainer config) {
        long now = System.currentTimeMillis();
        publishVariable(VariableConstants.COLOR, initialColor, HobsonVariable.Mask.READ_WRITE, initialColor != null ? now : null);
        publishVariable(VariableConstants.ON, initialOnValue, HobsonVariable.Mask.READ_WRITE, initialOnValue != null ? now : null);
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
        Color color = null;

        if (VariableConstants.ON.equals(variableName)) {
            if (value instanceof String) {
                on = Boolean.parseBoolean((String) value);
            } else if (value instanceof Boolean) {
                on = (Boolean)value;
            }
        } else if (VariableConstants.COLOR.equals(variableName)) {
            logger.debug("Setting variable for device {} ({}={})", getContext(), variableName, value);
            color = new Color((String)value);
        }

        if (on != null || color != null) {
            LightState state = new LightState(on, color, null, null);
            logger.debug("New state for device {} is {}", getContext(), state);
            context.sendSetLightStateRequest(new SetLightStateRequest(getContext().getDeviceId(), state));
        }
    }

    void refresh() {
        logger.debug("Refreshing device {}", getContext());
        context.sendGetLightAttributeAndStateRequest(new GetLightAttributeAndStateRequest(getContext().getDeviceId()));
    }

    void onLightState(LightState state) {
        if (state != null) {
            List<VariableUpdate> updates = null;

            long now = System.currentTimeMillis();

            // set the check-in time
            setDeviceAvailability(state.isReachable(), state.isReachable() ? now : null);

            HobsonVariable var = getVariable(VariableConstants.ON);
            if (var != null) {
                Boolean on = state.getOn();
                if (!state.isReachable()) {
                    on = null;
                }
                if ((var.getValue() == null && on != null) || (var.getValue() != null && !var.getValue().equals(on))) {
                    logger.debug("Detected change in on status for {}: {} (old value was {})", getContext(), state.getOn(), var.getValue());
                    updates = new ArrayList<>();
                    updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.ON), on, now));
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
                    updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.COLOR), color, now));
                }
            }

            if (updates != null) {
                fireVariableUpdateNotifications(updates);
            }
        }
    }

    void onLightStateFailure(Throwable t) {
        logger.debug("Received failure for light " + getContext(), t);

        // set all variables to null to indicate that the current state of this light is now unknown
        long now = System.currentTimeMillis();
        List<VariableUpdate> updates = new ArrayList<>();
        updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.ON), null, now));
        updates.add(new VariableUpdate(VariableContext.create(getContext(), VariableConstants.COLOR), null, now));
        fireVariableUpdateNotifications(updates);
    }

    Integer convertBrightnessToLevel(Integer brightness) {
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
      
    Integer convertLevelToBrightness(Integer level) {
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

    String convertLightStateToColor(LightState state) {
        if (state != null && state.hasColor()) {
            return new Color((int)(Math.round(state.getHue() / 182.0416666667)), (int)(state.getSaturation() / 2.54), (int)(state.getBrightness() / 2.54)).toString();
        } else if (state != null && state.hasColorTemperature()) {
            return new Color(1000000 / state.getColorTemperature(), (int)(state.getBrightness() / 2.54)).toString();
        } else {
            return null;
        }
    }
}
