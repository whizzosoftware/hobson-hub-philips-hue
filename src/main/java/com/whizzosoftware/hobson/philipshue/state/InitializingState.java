/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.bootstrap.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.api.HueBridge;
import com.whizzosoftware.hobson.philipshue.api.HueException;
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
    public State onLoop(StateContext context) {
        State nextState = this;

        // close any previous Hue channel
        context.closeHueChannel();

        // grab the bridge host from the config
        String bridgeHost = context.getBridgeHost();

        if (bridgeHost != null) {
            // create new Hue channel
            try {
                logger.debug("Using Hue bridge at {}", bridgeHost);
                context.setHueChannel(new HueBridge(bridgeHost, context.getHueDeviceString(), context.getHueUserString()));
                nextState = new AuthorizingState();
            } catch (HueException e) {
                logger.error("Error initializing connection to Hue bridge", e);
            }
        } else {
            logger.warn("Plugin is not configured");
            context.setPluginStatus(new PluginStatus(PluginStatus.Status.NOT_CONFIGURED, "The plugin is not configured"));
            return new FailedState();
        }

        return nextState;
    }

    @Override
    public State onBridgeHostUpdate(StateContext context) {
        return this;
    }

    @Override
    public State onSetVariable(StateContext context, String deviceId, String name, Object value) {
        logger.debug("Received set variable request before initialization was complete; ignoring");
        return this;
    }
}
