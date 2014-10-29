/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public LightState(JSONObject obj) throws JSONException {
        this.on = obj.getBoolean("on");
        this.brightness = obj.getInt("bri");
        if (obj.has("xy")) {
            JSONArray a = obj.getJSONArray("xy");
            this.x = new Float(a.getDouble(0));
            this.y = new Float(a.getDouble(1));
        }
        this.effect = obj.getString("effect");
        if (obj.has("modelid")) {
            this.model = obj.getString("modelid");
        }
        this.reachable = obj.getBoolean("reachable");
    }

    public Boolean getOn() {
        return on;
    }

    public void setOn(Boolean on) {
        this.on = on;
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public Float getX() {
        return x;
    }

    public void setX(Float x) {
        this.x = x;
    }

    public Float getY() {
        return y;
    }

    public void setY(Float y) {
        this.y = y;
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

    public String toJSONString() throws JSONException {
        JSONObject json = new JSONObject();
        if (this.on != null) {
            json.put("on", this.on);
        }
        if (this.brightness != null) {
            json.put("bri", this.brightness);
        }
        if ((this.on == null || this.on) && this.x != null && this.y != null) {
            JSONArray a = new JSONArray();
            a.put(this.x);
            a.put(this.y);
            json.put("xy", a);
        }
        if ((this.on == null || this.on) && this.effect != null) {
            json.put("effect", this.effect);
        }
        return json.toString();
    }

    protected void setRGBColor(int cred, int cgreen, int cblue, String model) {
        ColorConversion.PointF p = ColorConversion.calculateXY(cred, cgreen, cblue, "");
        this.x = p.x;
        this.y = p.y;
    }
}
