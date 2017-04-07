/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.api.dto.*;

/**
 * Interface that represents all the actions that a state can perform.
 *
 * @author Dan Noguerol
 */
public interface StateContext {
    /**
     * Returns the Hue device string to be used for API calls.
     *
     * @return String
     */
    String getHueDeviceString();

    /**
     * Returns the Hue user string to be used for API calls.
     *
     * @return String
     */
    String getHueUserString();

    /**
     * Returns the currently configured bridge host.
     *
     * @return String
     */
    String getBridgeHost();

    /**
     * Send a request to create a user to the Hue bridge.
     *
     * @param request the request object
     */
    void sendCreateUserRequest(CreateUserRequest request);

    /**
     * Send a request for a list of all known lights to the Hue bridge.
     *
     * @param request the request object
     */
    void sendGetAllLightsRequest(GetAllLightsRequest request);

    /**
     * Send a request for the state of a specific light to the Hue bridge.
     *
     * @param request the request object
     */
    void sendGetLightAttributeAndStateRequest(GetLightAttributeAndStateRequest request);

    /**
     * Set the state of a specific light to the Hue bridge.
     *
     * @param request the request object
     */
    void sendSetLightStateRequest(SetLightStateRequest request);

    /**
     * Sets the plugin status.
     *
     * @param status the current status
     */
    void setPluginStatus(PluginStatus status);

    /**
     * Callback when state about a light is received from the Hue bridge.
     *
     * @param deviceId the device ID of the light
     * @param state the current state of the light
     */
    void onLightState(String deviceId, LightState state);

    /**
     * Callback when a failure to obtain the state of a light from the Hue bridge occurs.
     *
     * @param deviceId the device ID of the the light
     * @param t the cause of the failure
     */
    void onLightStateFailure(String deviceId, Throwable t);

    /**
     * Callback when a failure to obtain the state of all lights from the Hue bridge occurs.
     *
     * @param t the cause of the failure
     */
    void onAllLightStateFailure(Throwable t);

    /**
     * Callback when a request is received to set a device variable.
     *
     * @param deviceId the device ID of the light to update
     * @param name the variable name
     * @param value the variable value
     */
    void onSetVariable(String deviceId, String name, Object value);

    /**
     * Creates a new HueLight based on a Light instance returned from an API call.
     *
     * @param light Light
     */
    void createHueLight(Light light);

    /**
     * Indicates whether a HueLight with the specified device ID exists.
     *
     * @param deviceId the device ID
     *
     * @return a boolean
     */
    boolean hasHueLight(String deviceId);
}
