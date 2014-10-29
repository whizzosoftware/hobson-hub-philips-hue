/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

/**
 * Represents a Hue light as the Hue bridge sees it.
 *
 * @author Dan Noguerol
 */
public class Light {
    private String id;
    private String name;
    private String model;

    public Light(String id, String name, String model) {
        this.id = id;
        this.name = name;
        this.model = model;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getModel() {
        return model;
    }

    public String toString() {
        return this.id + ":" + getName();
    }
}
