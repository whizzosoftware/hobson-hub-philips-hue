/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

import com.whizzosoftware.hobson.philipshue.api.dto.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * A class for parsing Hue bridge responses.
 *
 * @author Dan Noguerol
 */
class HueBridgeParser {
    private static final Logger logger = LoggerFactory.getLogger(HueBridgeParser.class);

    BridgeResponse parseGetAllLightsResponse(int statusCode, String response) throws HueException {
        validateStatusCode(statusCode);

        Object o = new JSONTokener(response).nextValue();
        if (o instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) o;
            List<Light> lights = new ArrayList<>();
            if (logger.isTraceEnabled()) {
                logger.trace("getAllLights response: {}", jsonObj.toString());
            }
            for (Object key : jsonObj.keySet()) {
                LightState state = null;
                String deviceId = (String) key;
                JSONObject jo = jsonObj.getJSONObject(deviceId);
                String model = null;
                if (jo.has("modelid")) {
                    model = jo.getString("modelid");
                }
                if (jo.has("state")) {
                    state = createLightState(jo.getJSONObject("state"));
                }
                lights.add(new Light(deviceId, jo.getString("name"), model, state));
            }
            return new GetAllLightsResponse(lights);
        } else if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.has("error")) {
                    return createErrorResponse(json.getJSONObject("error"));
                }
            }
        }
        throw new HueException("Error getting light information; received unexpected response " + response);
    }

    BridgeResponse parseCreateUserResponse(int statusCode, String response) throws HueException {
        validateStatusCode(statusCode);

        Object o = new JSONTokener(response).nextValue();
        if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.has("error")) {
                    return createErrorResponse(json.getJSONObject("error"));
                } else if (json.has("success")) {
                    JSONObject js = json.getJSONObject("success");
                    return new CreateUserResponse(js.getString("username"));
                }
            }
        }
        throw new HueException("Error creating user; received unexpected response " + response);
    }

    BridgeResponse parseGetLightAttributeAndStateResponse(String deviceId, int statusCode, String response) throws HueException {
        validateStatusCode(statusCode);

        Object o = new JSONTokener(response).nextValue();
        if (o instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject)o;
            if (logger.isTraceEnabled()) {
                logger.trace("getLightAttributeAndState({}) response: {}", deviceId, jsonObj.toString());
            }
            if (jsonObj.has("state")) {
                return new GetLightAttributeAndStateResponse(deviceId, createLightState(jsonObj.getJSONObject("state")));
            }
        } else if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.has("error")) {
                    return createErrorResponse(json.getJSONObject("error"));
                }
            }
        }
        throw new HueException("Error getting light information; received unexpected response " + response);
    }

    BridgeResponse parseSetLightStateResponse(int statusCode, String response) throws HueException {
        validateStatusCode(statusCode);

        Object o = new JSONTokener(response).nextValue();
        if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.has("error")) {
                    return createErrorResponse(json.getJSONObject("error"));
                }
            }
            return new SetLightStateResponse();
        }
        throw new HueException("Error setting light state; received unexpected response " + response);
    }

    void validateStatusCode(int statusCode) throws HueException {
        if (statusCode != 200) {
            throw new HueException("Error obtaining light state; received status code " + statusCode);
        }
    }

    ErrorResponse createErrorResponse(JSONObject json) throws JSONException {
        HueError error = new HueError(json);
        return new ErrorResponse(error.getType(), error.getAddress(), error.getDescription());
    }

    LightState createLightState(JSONObject obj) throws JSONException {
        LightState state = new LightState();
        state.setOn(obj.getBoolean("on"));
        state.setHue(obj.getInt("hue"));
        state.setSaturation(obj.getInt("sat"));
        state.setBrightness(obj.getInt("bri"));
        state.setColorTemperature(obj.getInt("ct"));
        state.setColorMode(obj.getString("colormode"));
        state.setEffect(obj.getString("effect"));
        if (obj.has("modelid")) {
            state.setModel(obj.getString("modelid"));
        }
        state.setReachable(obj.getBoolean("reachable"));
        return state;
    }

    JSONObject createLightStateJSON(LightState state) {
        JSONObject json = new JSONObject();
        if (state.hasOn()) {
            json.put("on", state.getOn());
        }
        if (state.hasColor()) {
            json.put("hue", state.getHue());
            json.put("sat", state.getSaturation());
            json.put("bri", state.getBrightness());
        } else if (state.hasColorTemperature()) {
            json.put("ct", state.getColorTemperature());
            json.put("bri", state.getBrightness());
        }
        if ((!state.hasOn() || state.getOn()) && state.hasEffect()) {
            json.put("effect", state.getEffect());
        }
        return json;
    }
}
