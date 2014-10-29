/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

/**
 * A general exception when dealing with the Hue bridge.
 *
 * @author Dan Noguerol
 */
public class HueException extends Exception {
    public HueException(String msg) {
        super(msg);
    }

    public HueException(String msg, Throwable t) {
        super(msg, t);
    }
}
