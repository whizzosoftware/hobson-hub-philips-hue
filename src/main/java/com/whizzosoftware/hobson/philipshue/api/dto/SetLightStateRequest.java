/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * A request to set the state of a specific light on the Hue bridge.
 *
 * @author Dan Noguerol
 */
public class SetLightStateRequest {
    private String id;
    private LightState state;

    public SetLightStateRequest(String id, LightState state) {
        this.id = id;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public LightState getState() {
        return state;
    }

    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("state", state).toString();
    }
}
