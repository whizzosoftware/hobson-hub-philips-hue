/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api.dto;

/**
 * A response from the Hue bridge for a GetLightAttributeAndState request.
 *
 * @author Dan Noguerol
 */
public class GetLightAttributeAndStateResponse extends BridgeResponse {
    private String id;
    private LightState state;

    public GetLightAttributeAndStateResponse(String id, LightState state) {
        this.id = id;
        this.state = state;
    }

    public String getId() {
        return id;
    }

    public LightState getState() {
        return state;
    }
}
