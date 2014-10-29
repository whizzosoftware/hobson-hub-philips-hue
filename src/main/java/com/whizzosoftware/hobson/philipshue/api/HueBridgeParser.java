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
import org.json.JSONTokener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * A class for parsing Hue bridge responses.
 *
 * @author Dan Noguerol
 */
public class HueBridgeParser {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public List<Light> parseGetLightsResponse(int statusCode, InputStream response) throws HueException {
        validateStatusCode(statusCode);

        Object o = new JSONTokener(response).nextValue();
        if (o instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) o;
            List<Light> lights = new ArrayList<Light>();
            if (logger.isTraceEnabled()) {
                logger.trace("getAllLights response: {}", jsonObj.toString());
            }
            for (Object key : jsonObj.keySet()) {
                String deviceId = (String) key;
                JSONObject jo = jsonObj.getJSONObject(deviceId);
                String model = null;
                if (jo.has("modelid")) {
                    model = jo.getString("modelid");
                }
                lights.add(new Light(deviceId, jo.getString("name"), model));
            }
            return lights;
        } else if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.has("error")) {
                    throw createHueException(json.getJSONObject("error"));
                }
            }
        }
        throw new HueException("Error getting light information; received unexpected response " + response);
    }

    public void parseCreateUserResponse(int statusCode, InputStream response) throws HueException {
        validateStatusCode(statusCode);

        Object o = new JSONTokener(response).nextValue();
        if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.has("error")) {
                    throw createHueException(json.getJSONObject("error"));
                } else if (json.has("success")) {
                    return;
                }
            }
        }
        throw new HueException("Error creating user; received unexpected response " + response);
    }

    public LightState parseLightAttributeAndStateResponse(int statusCode, InputStream response) throws HueException {
        validateStatusCode(statusCode);

        Object o = new JSONTokener(response).nextValue();
        if (o instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject)o;
            if (jsonObj.has("state")) {
                return new LightState(jsonObj.getJSONObject("state"));
            }
        } else if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.has("error")) {
                    throw createHueException(json.getJSONObject("error"));
                }
            }
        }
        throw new HueException("Error getting light information; received unexpected response " + response);
    }

    public void parseSetLightStateResponse(int statusCode, InputStream response) throws HueException {
        validateStatusCode(statusCode);

        Object o = new JSONTokener(response).nextValue();
        if (o instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) o;
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject json = jsonArray.getJSONObject(i);
                if (json.has("error")) {
                    throw createHueException(json.getJSONObject("error"));
                }
            }
            return;
        }
        throw new HueException("Error setting light state; received unexpected response " + response);
    }

    protected void validateStatusCode(int statusCode) throws HueException {
        if (statusCode != 200) {
            throw new HueException("Error setting light state; received status code " + statusCode);
        }
    }

    protected HueException createHueException(JSONObject json) throws JSONException {
        HueError error = new HueError(json);
        if (error.getType() == 1) {
            return new HueAuthenticationException(error.getDescription());
        } else if (error.getType() == 101) {
            return new HueLinkButtonNotPressedException(error.getDescription());
        } else {
            return new HueException(error.getDescription());
        }
    }
}
