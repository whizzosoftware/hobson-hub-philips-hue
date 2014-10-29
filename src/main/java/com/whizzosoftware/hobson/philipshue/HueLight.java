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
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.philipshue.api.ColorConversion;
import com.whizzosoftware.hobson.philipshue.api.HueException;
import com.whizzosoftware.hobson.philipshue.api.LightState;
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

    private final HueNetworkDelegate delegate;
    private final String model;

    /**
     * Constructor.
     *
     * @param plugin the HobsonPlugin that created this device
     * @param id the ID of the device on the Hue network
     * @param defaultName the device's default name
     * @param delegate a delegate interface the device uses to communicate with the Hue network
     */
    public HueLight(HuePlugin plugin, String id, String model, String defaultName, HueNetworkDelegate delegate) {
        super(plugin, id);
        this.model = model;
        setDefaultName(defaultName);
        this.delegate = delegate;
    }

    @Override
    public void onStartup() {
        publishVariable(VariableConstants.COLOR, null, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.LEVEL, null, HobsonVariable.Mask.READ_WRITE);
        publishVariable(VariableConstants.ON, null, HobsonVariable.Mask.READ_WRITE);
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
    public void onSetVariable(String variableName, Object value) {
        try {
            logger.debug("Setting variable for device {} ({}={})", getId(), variableName, value);

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
                logger.debug("Setting variable for device {} ({}={})", getId(), variableName, value);
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
                logger.debug("New state for device {} is {}", getId(), state);
                delegate.setLightState(getId(), state);
            }

        } catch (HueException e) {
            logger.error("Error sending command to Hue bridge", e);
        }
    }

    public void refresh() {
        logger.debug("Refreshing device {}", getId());
        try {
            LightState state = delegate.getLightState(getId());
            if (state != null) {
                List<VariableUpdate> updates = null;

                HobsonVariable var = getVariable(VariableConstants.ON);
                if (var != null) {
                    Boolean on = state.getOn();
                    if (!state.isReachable()) {
                        on = null;
                    }
                    if ((var.getValue() == null && on != null) || (var.getValue() != null && !var.getValue().equals(on))) {
                        logger.debug("Detected change in on status for {}: {} (old value was {})", getId(), state.getOn(), var.getValue());
                        updates = new ArrayList<VariableUpdate>();
                        updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.ON, on, System.currentTimeMillis()));
                    }
                }

                var = getVariable(VariableConstants.COLOR);
                if (var != null) {
                    String color = convertLightStateToColor(state);
                    if (!state.isReachable()) {
                        color = null;
                    }
                    if ((var.getValue() == null && color != null) || (color != null && !var.getValue().equals(color))) {
                        logger.debug("Detected change in color status for {}: {} (old value was {})", getId(), color, var.getValue());
                        if (updates == null) {
                            updates = new ArrayList<VariableUpdate>();
                        }
                        updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.COLOR, color, System.currentTimeMillis()));
                    }
                }

                var = getVariable(VariableConstants.LEVEL);
                if (var != null) {
                    Integer level = convertBrightnessToLevel(state.getBrightness());
                    if (!state.isReachable()) {
                        level = null;
                    }
                    if ((var.getValue() == null && level != null) || (level != null && !var.getValue().equals(level))) {
                        logger.debug("Detected change in level status for {}: {} (old value was {})", getId(), level, var.getValue());
                        if (updates == null) {
                            updates = new ArrayList<VariableUpdate>();
                        }
                        updates.add(new VariableUpdate(getPluginId(), getId(), VariableConstants.LEVEL, level, System.currentTimeMillis()));
                    }
                }

                if (updates != null) {
                    fireVariableUpdateNotifications(updates);
                }
            }
        } catch (HueException e) {
            logger.error("Error refreshing device " + getId(), e);
        }

        logger.trace("Refresh of device {} complete", getId());
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
