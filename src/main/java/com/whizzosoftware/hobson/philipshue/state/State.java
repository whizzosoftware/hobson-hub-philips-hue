/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.philipshue.api.dto.BridgeResponse;

/**
 * An interface implemented by all classes that implement states of the finite state machine. The interface defines
 * event callbacks that can trigger state transitions.
 *
 * @author Dan Noguerol
 */
public interface State {
    /**
     * Callback when event loop runs.
     *
     * @param context the state context
     *
     * @return the next transition state
     */
    public State onRefresh(StateContext context);

    /**
     * Callback when the bridge host configuration changes.
     *
     * @param context the state context
     *
     * @return the next transition state
     */
    public State onBridgeHostUpdate(StateContext context);

    /**
     * Callback when a response from the bridge is received.
     *
     * @param context the state context
     * @param response the response
     *
     * @return the next transition state
     */
    public State onBridgeResponse(StateContext context, BridgeResponse response);

    /**
     * Callback when a request to the bridge fails.
     *
     * @param context the state context
     * @param t the cause of the failure
     *
     * @return the next transition state
     */
    public State onBridgeRequestFailure(StateContext context, Throwable t);

    /**
     * Callback when a variable update request occurs.
     *
     * @param context the state context
     * @param deviceId the device ID
     * @param name the variable name
     * @param value the variable value
     *
     * @return the next transition state
     */
    public State onSetVariable(StateContext context, String deviceId, String name, Object value);
}
