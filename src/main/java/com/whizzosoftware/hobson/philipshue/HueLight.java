/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.color.Color;
import com.whizzosoftware.hobson.api.device.DeviceType;
import com.whizzosoftware.hobson.api.device.proxy.AbstractHobsonDeviceProxy;
import com.whizzosoftware.hobson.api.event.device.DeviceUnavailableEvent;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.api.variable.DeviceVariableState;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableMask;
import com.whizzosoftware.hobson.philipshue.api.dto.Light;
import com.whizzosoftware.hobson.philipshue.api.dto.LightState;
import com.whizzosoftware.hobson.philipshue.api.dto.GetLightAttributeAndStateRequest;
import com.whizzosoftware.hobson.philipshue.api.dto.SetLightStateRequest;
import com.whizzosoftware.hobson.philipshue.state.StateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that represents a Hobson device for a Hue light.
 *
 * @author Dan Noguerol
 */
public class HueLight extends AbstractHobsonDeviceProxy {
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
        super(plugin, id, defaultName, DeviceType.LIGHTBULB);
        this.model = model;
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
    public void onStartup(String name, Map<String,Object> config) {
        long now = System.currentTimeMillis();
        publishVariables(
            createDeviceVariable(VariableConstants.COLOR, VariableMask.READ_WRITE, initialColor, initialColor != null ? now : null),
            createDeviceVariable(VariableConstants.ON, VariableMask.READ_WRITE, initialOnValue, initialOnValue != null ? now : null)
        );
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String getManufacturerName() {
        return "Philips";
    }

    @Override
    public String getManufacturerVersion() {
        return null;
    }

    @Override
    public String getModelName() {
        return model;
    }

    @Override
    public String getPreferredVariableName() {
        return VariableConstants.ON;
    }

    @Override
    public void onDeviceConfigurationUpdate(Map<String, Object> map) {
    }

    @Override
    protected TypedProperty[] getConfigurationPropertyTypes() {
        return null;
    }

    @Override
    public void onSetVariables(Map<String,Object> values) {
        logger.debug("Setting variables for device {}: {})", getContext(), values);

        Boolean on = null;
        Color color = null;

        for (String name : values.keySet()) {
            Object value = values.get(name);
            if (VariableConstants.ON.equals(name)) {
                if (value instanceof String) {
                    on = Boolean.parseBoolean((String) value);
                } else if (value instanceof Boolean) {
                    on = (Boolean) value;
                }
            } else if (VariableConstants.COLOR.equals(name)) {
                color = new Color((String) value);
            }
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
            Map<String,Object> updates = null;

            long now = System.currentTimeMillis();

            // set the check-in time
            if (state.isReachable()) {
                setLastCheckin(now);

                DeviceVariableState var = getVariableState(VariableConstants.ON);
                if (var != null) {
                    Boolean on = state.getOn();
                    if (!state.isReachable()) {
                        on = null;
                    }
                    if ((var.getValue() == null && on != null) || (var.getValue() != null && !var.getValue().equals(on))) {
                        logger.debug("Detected change in on status for {}: {} (old value was {})", getContext(), state.getOn(), var.getValue());
                        updates = new HashMap<>();
                        updates.put(VariableConstants.ON, on);
                    }
                }

                var = getVariableState(VariableConstants.COLOR);
                if (var != null) {
                    String color = convertLightStateToColor(state);
                    if (!state.isReachable()) {
                        color = null;
                    }
                    if ((var.getValue() == null && color != null) || (color != null && !var.getValue().equals(color))) {
                        logger.debug("Detected change in color status for {}: {} (old value was {})", getContext(), color, var.getValue());
                        if (updates == null) {
                            updates = new HashMap<>();
                        }
                        updates.put(VariableConstants.COLOR, color);
                    }
                }

                if (updates != null) {
                    setVariableValues(updates);
                }
            } else {
                postEvent(new DeviceUnavailableEvent(now, getContext()));
            }
        }
    }

    void onLightStateFailure(Throwable t) {
        logger.debug("Received failure for light " + getContext(), t);

        // set all variables to null to indicate that the current state of this light is now unknown
        long now = System.currentTimeMillis();
        Map<String,Object> updates = new HashMap<>();
        updates.put(VariableConstants.ON, now);
        updates.put(VariableConstants.COLOR, now);
        setVariableValues(updates);
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
