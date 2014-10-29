/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

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
    public State onLoop(StateContext context);

    /**
     * Callback when the bridge host configuration changes.
     *
     * @param context the state context
     *
     * @return the next transition state
     */
    public State onBridgeHostUpdate(StateContext context);

    /**
     * Callback when a variable update request occurs.
     *
     * @param context the state context
     * @param name the variable name
     * @param value the variable value
     *
     * @return the next transition state
     */
    public State onSetVariable(StateContext context, String deviceId, String name, Object value);
}
