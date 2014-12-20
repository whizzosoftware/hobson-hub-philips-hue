/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api.dto;

/**
 * A request to get attributes/state for a specific light from the Hue bridge.
 *
 * @author Dan Noguerol
 */
public class GetLightAttributeAndStateRequest {
    private String id;

    public GetLightAttributeAndStateRequest(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
