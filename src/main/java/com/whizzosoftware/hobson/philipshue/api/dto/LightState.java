/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api.dto;

import com.whizzosoftware.hobson.api.color.Color;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Represents the state of a Hue light.
 *
 * @author Dan Noguerol
 */
public class LightState {
    public static final String MODE_HS = "hs";
    public static final String MODE_CT = "ct";

    private Boolean on;
    private Integer hue;
    private Integer saturation;
    private Integer brightness;
    private Integer colorTemperature;
    private String colorMode;
    private String effect;
    private String model;
    private Boolean reachable;

    /**
     * Constructor
     *
     * @param on is light on?
     * @param hue the hue value (0-65535)
     * @param saturation the saturation value (1-254)
     * @param brightness the brightness value (1-254)
     * @param effect the effect
     * @param model the model of light
     * @param reachable is light reachable?
     */
    public LightState(Boolean on, Integer hue, Integer saturation, Integer brightness, Integer ct, String colorMode, String effect, String model, Boolean reachable) {
        setOn(on);
        setHue(hue);
        setSaturation(saturation);
        setBrightness(brightness);
        setColorTemperature(ct);
        setColorMode(colorMode);
        setEffect(effect);
        setModel(model);
        setReachable(reachable);
    }

    public LightState(Boolean on, Color color, String effect, Boolean reachable) {
        setOn(on);
        if (color != null && color.isColor()) {
            setHue((int)(color.getHue() * 182.0416666667));
            setSaturation((int)(color.getSaturation() * 2.54));
            setBrightness((int)(color.getBrightness() * 2.54));
            setColorMode(MODE_HS);
        } else if (color != null && color.isColorTemperature()) {
            setColorTemperature(colorTemperature = 1000000 / color.getColorTemperature());
            setBrightness((int)(color.getBrightness() * 2.54));
            setColorMode(MODE_CT);
        }
        setEffect(effect);
        setReachable(reachable);
    }

    public LightState() {}

    public boolean hasOn() {
        return (on != null);
    }

    public Boolean getOn() {
        return on;
    }

    public void setOn(Boolean on) {
        this.on = on;
    }

    public boolean hasColor() {
        return (MODE_HS.equals(colorMode) && hue != null && saturation != null && brightness != null);
    }

    public boolean hasColorTemperature() {
        return (MODE_CT.equals(colorMode) && colorTemperature != null && brightness != null);
    }

    public boolean hasHue() {
        return (hue != null);
    }

    public Integer getHue() {
        return hue;
    }

    public void setHue(Integer hue) {
        this.hue = hue;
    }

    public boolean hasSaturation() {
        return (saturation != null);
    }

    public Integer getSaturation() {
        return saturation;
    }

    public void setSaturation(Integer saturation) {
        this.saturation = saturation;
    }

    public boolean hasBrightness() {
        return (brightness != null);
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Integer getColorTemperature() {
        return colorTemperature;
    }

    public void setColorTemperature(Integer colorTemperature) {
        this.colorTemperature = colorTemperature;
    }

    public String getColorMode() {
        return colorMode;
    }

    public void setColorMode(String colorMode) {
        this.colorMode = colorMode;
    }

    public boolean hasEffect() {
        return (effect != null);
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean isReachable() {
        return (reachable != null && reachable);
    }

    public void setReachable(Boolean reachable) {
        this.reachable = reachable;
    }

    public String toString() {
        return new ToStringBuilder(this).
            append("on", getOn()).
            append("hue", getHue()).
            append("sat", getSaturation()).
            append("bri", getBrightness()).
            append("ct", getColorTemperature()).
            append("colormode", getColorMode()).
            toString();
    }
}
