/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.api.dto.BridgeResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The initial state representing plugin initialization.
 *
 * @author Dan Noguerol
 */
public class InitializingState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public State onRefresh(StateContext context) {
        State nextState;

        // grab the bridge host from the config
        String bridgeHost = context.getBridgeHost();

        if (bridgeHost != null) {
            // create new Hue channel
            logger.debug("Using Hue bridge at {}", bridgeHost);
            nextState = new AuthorizingState();
        } else {
            logger.debug("The Hue plugin is not configured.");
            context.setPluginStatus(PluginStatus.notConfigured("The plugin is not configured"));
            nextState = new FailedState();
        }

        return nextState;
    }

    @Override
    public State onBridgeHostUpdate(StateContext context) {
        return this;
    }

    @Override
    public State onBridgeResponse(StateContext context, BridgeResponse response) {
        return this;
    }

    @Override
    public State onBridgeRequestFailure(StateContext context, Object requestContext, Throwable t) {
        return this;
    }

    @Override
    public State onSetVariable(StateContext context, String deviceId, String name, Object value) {
        logger.debug("Received set variable request before initialization was complete; ignoring");
        return this;
    }
}
