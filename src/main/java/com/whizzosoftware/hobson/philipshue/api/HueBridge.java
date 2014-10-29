/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * A class for communicating with the Hue bridge via its REST interface.
 *
 * @author Dan Noguerol
 */
public class HueBridge {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private String userName;
    private String bridgeBaseUrl;
    private HttpClient client;
    private HueBridgeParser parser;

    private static final int DEFAULT_TIMEOUT = 5000;

    /**
     * Constructor
     *
     * @param bridgeName the bridge name
     * @param deviceType the deviceType (required by the Hue API)
     * @param userName the username to use when accessing the bridge
     *
     * @throws HueException on failure
     */
    public HueBridge(String bridgeName, String deviceType, String userName) throws HueException {
        this(bridgeName, deviceType, userName, DEFAULT_TIMEOUT);
    }

    /**
     * Constructor
     *
     * @param bridgeName the bridge name
     * @param deviceType the device type (required by the Hue API)
     * @param userName the username to use when accessing the bridge
     * @param timeout the timeout interval for requests
     *
     * @throws HueException on failure
     */
    public HueBridge(String bridgeName, String deviceType, String userName, int timeout) throws HueException {
        this.userName = userName;
        this.bridgeBaseUrl = "http://" + bridgeName;

        HttpClientParams params = new HttpClientParams();
        params.setSoTimeout(timeout);
        this.client = new HttpClient(params);

        this.parser = new HueBridgeParser();

        // verify that username meets length requirement
        if (userName == null) {
            throw new HueException("No user name specified");
        } else if (userName.length() < 10) {
            throw new HueException("User name is too short");
        }
    }

    /**
     * Create a user on the Hue bridge.
     *
     * @param deviceType the device type (required by the Hue API)
     * @param username the username to use when accessing the bridge
     *
     * @throws HueException on failure
     */
    public void createUser(String deviceType, String username) throws HueException {
        JSONObject request = new JSONObject();
        request.put("devicetype", deviceType);
        request.put("username", username);

        String uri = bridgeBaseUrl + "/api";
        if (logger.isTraceEnabled()) {
            logger.trace("createUser sending request to {}: {}", uri, request.toString());
        }

        try {
            PostMethod post = new PostMethod(uri);
            post.setRequestEntity(new StringRequestEntity(request.toString(), "application/json", "UTF8"));
            int statusCode = client.executeMethod(post);
            parser.parseCreateUserResponse(statusCode, post.getResponseBodyAsStream());
        } catch (IOException e) {
            throw new HueException("Error creating user", e);
        }
    }

    /**
     * Return all lights the Hue bridge knows about.
     *
     * @return a List of Light instances
     *
     * @throws HueException on failure
     */
    public List<Light> getAllLights() throws HueException {
        try {
            String uri = bridgeBaseUrl + "/api/" + userName + "/lights";
            logger.trace("getAllLights sending request to {}", uri);
            GetMethod get = new GetMethod(uri);
            int statusCode = client.executeMethod(get);
            return parser.parseGetLightsResponse(statusCode, get.getResponseBodyAsStream());
        } catch (IOException e) {
            throw new HueException("Error getting light information", e);
        }
    }

    /**
     * Return information about a specific light.
     *
     * @param id the light ID
     *
     * @return a LightState object
     *
     * @throws HueException on failure
     */
    public LightState getLightAttributeAndState(String id) throws HueException {
        try {
            String uri = bridgeBaseUrl + "/api/" + userName + "/lights/" + id;
            logger.trace("getLightAttributeAndState sending request to {}", uri);
            GetMethod get = new GetMethod(uri);
            int statusCode = client.executeMethod(get);
            return parser.parseLightAttributeAndStateResponse(statusCode, get.getResponseBodyAsStream());
        } catch (IOException e) {
            throw new HueException("Error getting light attribute information", e);
        }
    }

    /**
     * Sets the state of a specific Hue light.
     *
     * @param id the light ID
     * @param ls the state to set
     *
     * @throws HueException on failure
     */
    public void setLightState(String id, LightState ls) throws HueException {
        String request = ls.toJSONString();
        String uri = bridgeBaseUrl + "/api/" + userName + "/lights/" + id + "/state";
        logger.trace("setLightState sending request to {}: {}", uri, request);
        try {
            PutMethod post = new PutMethod(uri);
            post.setRequestEntity(new StringRequestEntity(request, "application/json", "UTF8"));
            int statusCode = client.executeMethod(post);
            parser.parseSetLightStateResponse(statusCode, post.getResponseBodyAsStream());
        } catch (IOException e) {
            throw new HueException("Error creating user", e);
        }
    }
}
