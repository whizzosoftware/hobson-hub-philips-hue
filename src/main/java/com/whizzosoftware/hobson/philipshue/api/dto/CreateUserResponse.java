/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api.dto;

/**
 * A response from the Hue bridge for a CreateUser request.
 *
 * @author Dan Noguerol
 */
public class CreateUserResponse extends BridgeResponse {
    private String username;

    public CreateUserResponse(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
