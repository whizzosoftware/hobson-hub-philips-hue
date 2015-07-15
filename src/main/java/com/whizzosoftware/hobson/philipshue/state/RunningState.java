/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.api.dto.*;
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
    public State onRefresh(StateContext context) {
        logger.trace("onRefresh()");

        // set the plugin status to running
        if (!hasRunningStatus) {
            context.setPluginStatus(PluginStatus.running());
            hasRunningStatus = true;
        }

        // get an updated list of lights
        context.sendGetAllLightsRequest(new GetAllLightsRequest());

        return this;
    }

    @Override
    public State onBridgeHostUpdate(StateContext context) {
        logger.debug("Bridge host updated: " + context.getBridgeHost());
        return new InitializingState();
    }

    @Override
    public State onBridgeResponse(StateContext context, BridgeResponse response) {
        if (response instanceof GetAllLightsResponse) {
            GetAllLightsResponse galr = (GetAllLightsResponse)response;
            for (Light light : galr.getLights()) {
                // create any lights we don't already know about
                if (!context.hasHueLight(light.getId())) {
                    context.createHueLight(light);
                } else {
                    // if there's a state key in the light data (some firmware versions), use it
                    if (light.getState() != null) {
                        context.onLightState(light.getId(), light.getState());
                    // otherwise, we have to request it specifically
                    } else {
                        context.sendGetLightAttributeAndStateRequest(new GetLightAttributeAndStateRequest(light.getId()));
                    }
                }
            }
        } else if (response instanceof GetLightAttributeAndStateResponse) {
            GetLightAttributeAndStateResponse glasr = (GetLightAttributeAndStateResponse)response;
            context.onLightState(glasr.getId(), glasr.getState());
        }
        return this;
    }

    @Override
    public State onBridgeRequestFailure(StateContext context, Object requestContext, Throwable t) {
        // if a failure occurs getting state of all lights, invalidate all currently discovered lights
        if (requestContext instanceof GetAllLightsRequest) {
            logger.warn("Error while requesting light information from Hue bridge; will retry", t);
            context.onAllLightStateFailure(t);
        // if a failure occurs getting state of one light, invalidate just that light
        } else if (requestContext instanceof GetLightAttributeAndStateRequest) {
            GetLightAttributeAndStateRequest glasr = (GetLightAttributeAndStateRequest)requestContext;
            context.onLightStateFailure(glasr.getId(), t);
        } else {
            logger.warn("Error while requesting light information from Hue bridge; will retry", t);
        }
        return this;
    }

    @Override
    public State onSetVariable(StateContext context, String deviceId, String name, Object value) {
        context.onSetVariable(deviceId, name, value);
        return this;
    }
}
