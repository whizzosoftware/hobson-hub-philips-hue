/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api.dto;

/**
 * A request to create a Hue bridge user.
 *
 * @author Dan Noguerol
 */
public class CreateUserRequest extends BridgeRequest {
    private String deviceType;
    private String userName;

    public CreateUserRequest(String deviceType, String userName) {
        this.deviceType = deviceType;
        this.userName = userName;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getUserName() {
        return userName;
    }
}
