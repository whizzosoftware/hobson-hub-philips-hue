/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.bootstrap.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.HueLight;
import com.whizzosoftware.hobson.philipshue.api.HueBridge;
import com.whizzosoftware.hobson.philipshue.api.Light;

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
    public String getHueDeviceString();

    /**
     * Returns the Hue user string to be used for API calls.
     *
     * @return String
     */
    public String getHueUserString();

    /**
     * Sets the plugin status.
     *
     * @param status the current status
     */
    public void setPluginStatus(PluginStatus status);

    /**
     * Returns the currently configured bridge host.
     *
     * @return String
     */
    public String getBridgeHost();

    /**
     * Returns the currently active HueChannel for making API calls.
     *
     * @return HueChannel
     */
    public HueBridge getHueChannel();

    /**
     * Sets the currently active HueChannel for making API calls.
     *
     * @param hueBridge the active HueChannel instance
     */
    public void setHueChannel(HueBridge hueBridge);

    /**
     * Closes the currently active HueChannel.
     */
    public void closeHueChannel();

    /**
     * Creates a new HueLight based on a Light instance returned from an API call.
     *
     * @param light Light
     */
    public void createHueLight(Light light);

    /**
     * Indicates whether a HueLight with the specified device ID exists.
     *
     * @param deviceId the device ID
     *
     * @return a boolean
     */
    public boolean hasHueLight(String deviceId);

    /**
     * Returns the HueLight for the specified device ID.
     *
     * @param deviceId the device ID
     *
     * @return a HueLight instance (or null if not found)
     */
    public HueLight getHueLight(String deviceId);

    /**
     * Refreshes all active HueLight instances.
     */
    public void refreshAllLights();
}
