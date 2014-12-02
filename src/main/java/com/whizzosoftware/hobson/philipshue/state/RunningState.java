/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.HueLight;
import com.whizzosoftware.hobson.philipshue.api.HueException;
import com.whizzosoftware.hobson.philipshue.api.Light;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A state representing the "normal" plugin running state.
 *
 * @author Dan Noguerol
 */
public class RunningState implements State {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean hasRunningStatus = false;

    @Override
    public State onLoop(StateContext context) {
        // set the plugin status to running
        if (!hasRunningStatus) {
            context.setPluginStatus(new PluginStatus(PluginStatus.Status.RUNNING));
            hasRunningStatus = true;
        }

        // get an updated list of lights
        try {
            for (Light light : context.getHueChannel().getAllLights()) {
                // create any lights we don't already know about
                if (!context.hasHueLight(light.getId())) {
                    context.createHueLight(light);
                }
            }
        } catch (HueException e) {
            logger.error("Error retrieving light data from bridge", e);
        }

        // refresh any lights we do know about
        context.refreshAllLights();

        return this;
    }

    @Override
    public State onBridgeHostUpdate(StateContext context) {
        logger.debug("Bridge host updated: " + context.getBridgeHost());
        return new InitializingState();
    }

    @Override
    public State onSetVariable(StateContext context, String deviceId, String name, Object value) {
        HueLight light = context.getHueLight(deviceId);
        if (light != null) {
            light.onSetVariable(name, value);
        }
        return this;
    }
}
