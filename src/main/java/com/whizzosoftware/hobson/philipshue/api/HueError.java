/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * An error coming back from the Hue bridge.
 *
 * @author Dan Noguerol
 */
public class HueError {
    private Integer type;
    private String address;
    private String description;

    public HueError(JSONObject json) throws JSONException {
        this.type = json.getInt("type");
        this.address = json.getString("address");
        this.description = json.getString("description");
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
