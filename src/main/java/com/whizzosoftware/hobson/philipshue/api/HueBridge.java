/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

import com.whizzosoftware.hobson.philipshue.api.dto.*;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;

/**
 * A class for communicating with the Hue bridge via its REST interface.
 *
 * @author Dan Noguerol
 */
public class HueBridge {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String userName;
    private String host;
    private String bridgeBaseUrl;
    private HueBridgeParser parser;

    /**
     * Constructor
     *
     * @param host the bridge name
     * @param deviceType the device type (required by the Hue API)
     * @param userName the username to use when accessing the bridge
     *
     * @throws HueException on failure
     */
    public HueBridge(String host, String deviceType, String userName) throws HueException {
        this.host = host;
        this.userName = userName;
        this.bridgeBaseUrl = "http://" + host;

        this.parser = new HueBridgeParser();

        // verify that username meets length requirement
        if (userName == null) {
            throw new HueException("No user name specified");
        } else if (userName.length() < 10) {
            throw new HueException("User name is too short");
        }
    }

    public String getHost() {
        return host;
    }

    /**
     * Create a user on the Hue bridge.
     *
     * @param http the HttpChannel instance to use for the request
     * @param request the request object
     *
     * @throws HueException on failure
     */
    public void sendCreateUserRequest(HttpContext http, CreateUserRequest request) throws HueException {
        JSONObject json = new JSONObject();
        json.put("devicetype", request.getDeviceType());
        json.put("username", request.getUserName());

        String uri = bridgeBaseUrl + "/api";
        if (logger.isTraceEnabled()) {
            logger.trace("createUser sending request to {}: {}", uri, json.toString());
        }

        try {
            http.sendHttpPostRequest(new URI(uri), null, json.toString().getBytes(), null);
        } catch (Exception e) {
            throw new HueException("Error sending CreateUser request", e);
        }
    }

    /**
     * Return all lights the Hue bridge knows about.
     *
     * @param http the HttpChannel instance to use for the request
     * @param request the request object
     *
     * @throws HueException on failure
     */
    public void sendGetAllLightsRequest(HttpContext http, GetAllLightsRequest request) throws HueException {
        try {
            String uri = bridgeBaseUrl + "/api/" + userName + "/lights";
            logger.trace("getAllLights sending request to {}", uri);
            http.sendHttpGetRequest(new URI(uri), null, request);
        } catch (Exception e) {
            throw new HueException("Error sending GetAllLights request", e);
        }
    }

    /**
     * Return information about a specific light.
     *
     * @param http the HttpChannel instance to use for the request
     * @param request the request object
     *
     * @throws HueException on failure
     */
    public void sendGetLightAttributeAndStateRequest(HttpContext http, GetLightAttributeAndStateRequest request) throws HueException {
        try {
            String uri = bridgeBaseUrl + "/api/" + userName + "/lights/" + request.getId();
            logger.trace("getLightAttributeAndState sending request to {}", uri);
            http.sendHttpGetRequest(new URI(uri), null, request);
        } catch (Exception e) {
            throw new HueException("Error getting light attribute information", e);
        }
    }

    /**
     * Sets the state of a specific Hue light.
     *
     * @param http the HttpChannel instance to use for the request
     * @param request the request object
     *
     * @throws HueException on failure
     */
    public void sendSetLightStateRequest(HttpContext http, SetLightStateRequest request) throws HueException {
        String json = parser.createLightStateJSON(request.getState()).toString();
        String uri = bridgeBaseUrl + "/api/" + userName + "/lights/" + request.getId() + "/state";
        logger.trace("setLightState sending request to {}: {}", uri, json);
        try {
            http.sendHttpPutRequest(new URI(uri), null, json.getBytes(), json);
        } catch (Exception e) {
            throw new HueException("Error sending SetLightState request", e);
        }
    }

    /**
     * Parses a bridge response based on the request type.
     *
     * @param request the request corresponding to the response
     * @param statusCode the HTTP status code returned by the bridge
     * @param response the response body
     *
     * @return a BridgeResponse instance
     *
     * @throws HueException on failure
     */
    public BridgeResponse parseResponse(Object request, int statusCode, String response) throws HueException {
        if (request instanceof CreateUserRequest) {
            return parser.parseCreateUserResponse(statusCode, response);
        } else if (request instanceof GetAllLightsRequest) {
            return parser.parseGetAllLightsResponse(statusCode, response);
        } else if (request instanceof GetLightAttributeAndStateRequest) {
            GetLightAttributeAndStateRequest glasr = (GetLightAttributeAndStateRequest)request;
            return parser.parseGetLightAttributeAndStateResponse(glasr.getId(), statusCode, response);
        } else if (request instanceof SetLightStateRequest) {
            return parser.parseSetLightStateResponse(statusCode, response);
        } else {
            return null;
        }
    }
}
