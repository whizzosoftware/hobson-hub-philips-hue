/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api.dto;

import com.whizzosoftware.hobson.philipshue.api.ColorConversion;

/**
 * Represents the state of a Hue light.
 *
 * @author Dan Noguerol
 */
public class LightState {
    private Boolean on;
    private Float x;
    private Float y;
    private Integer brightness;
    private String effect;
    private String model;
    private Boolean reachable;

    /**
     * Constructor
     *
     * @param on is light on?
     * @param brightness the brightness value (0-255)
     * @param red red color value (0-255)
     * @param green green color value (0-255)
     * @param blue blue color value (0-255)
     * @param effect the effect
     * @param reachable is light reachable?
     */
    public LightState(Boolean on, Integer brightness, Integer red, Integer green, Integer blue, String effect, String model, Boolean reachable) {
        setOn(on);
        setBrightness(brightness);
        if (red != null && green != null && blue != null) {
            setRGBColor(red, green, blue, model);
        }
        setEffect(effect);
        setModel(model);
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

    public boolean hasBrightness() {
        return (brightness != null);
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public boolean hasXY() {
        return (x != null && y != null);
    }

    public Float getX() {
        return x;
    }

    public Float getY() {
        return y;
    }

    public void setXY(Float x, Float y) {
        this.x = x;
        this.y = y;
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
        return "LightState[on=" + getOn() + ",bri=" + getBrightness() + ",x=" + getX() + ",y=" + getY() + "]";
    }

    protected void setRGBColor(int cred, int cgreen, int cblue, String model) {
        ColorConversion.PointF p = ColorConversion.calculateXY(cred, cgreen, cblue, "");
        this.x = p.x;
        this.y = p.y;
    }
}
