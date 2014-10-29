/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A state representing a plugin failure.
 *
 * @author Dan Noguerol
 */
public class FailedState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public State onLoop(StateContext context) {
        // do nothing
        return this;
    }

    @Override
    public State onBridgeHostUpdate(StateContext context) {
        logger.debug("Bridge host updated: " + context.getBridgeHost());
        return new InitializingState();
    }

    @Override
    public State onSetVariable(StateContext context, String deviceId, String name, Object value) {
        // do nothing
        logger.debug("Received set variable request while waiting for authorization; ignoring");
        return this;
    }
}
