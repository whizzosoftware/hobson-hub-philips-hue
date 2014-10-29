/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

/**
 * An exception that occurs when authentication to the Hue bridge fails.
 *
 * @author Dan Noguerol
 */
public class HueAuthenticationException extends HueException {
    /**
     * Constructor.
     *
     * @param msg the error message
     */
    public HueAuthenticationException(String msg) {
        super(msg);
    }
}
