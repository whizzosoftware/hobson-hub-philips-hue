/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api.dto;

/**
 * An error response from the Hue bridge.
 *
 * @author Dan Noguerol
 */
public class ErrorResponse extends BridgeResponse {
    public static final int UNAUTHORIZED_USER = 1;
    public static final int LINK_BUTTON_NOT_PRESSED = 101;

    private Integer type;
    private String address;
    private String description;

    public ErrorResponse(Integer type, String address, String description) {
        this.type = type;
        this.address = address;
        this.description = description;
    }

    public Integer getType() {
        return type;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }
}
