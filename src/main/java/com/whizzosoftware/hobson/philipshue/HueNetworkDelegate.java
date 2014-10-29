/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.philipshue.api.HueException;
import com.whizzosoftware.hobson.philipshue.api.LightState;

/**
 * A delegate interface that Hue device classes use to talk to the Hue network. This makes unit testing much
 * easier.
 *
 * @author Dan Noguerol
 */
public interface HueNetworkDelegate {
    /**
     * Set the state of a light.
     *
     * @param id the device ID
     * @param lightState the state to set
     *
     * @throws com.whizzosoftware.hobson.philipshue.api.HueException on failure
     */
    public void setLightState(String id, LightState lightState) throws HueException;

    /**
     * Returns the current state of a light.
     *
     * @param id the device ID
     *
     * @return the state of the light or null if the device ID doesn't exist
     * @throws HueException on failure
     */
    public LightState getLightState(String id) throws HueException;
}
