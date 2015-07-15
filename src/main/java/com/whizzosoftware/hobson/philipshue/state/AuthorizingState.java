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
import com.whizzosoftware.hobson.philipshue.api.dto.ErrorResponse;
import com.whizzosoftware.hobson.philipshue.api.dto.GetAllLightsRequest;
import com.whizzosoftware.hobson.philipshue.api.dto.GetAllLightsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A state representing the authorization process. This includes detecting if API calls can be made, creating a
 * Hobson user if one doesn't exist and checking if the Hue Bridge button needs to be pressed.
 *
 * @author Dan Noguerol
 */
public class AuthorizingState extends AbstractTimeoutState {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public State onRefresh(StateContext ctx, long now) {
        State s = super.onRefresh(ctx, now);

        if (s instanceof FailedState) {
            logger.warn("Timeout waiting for Hue bridge to respond to GetAllLights request");
            ctx.setPluginStatus(PluginStatus.failed("Timeout waiting for response from Hue bridge. See log for details."));
        }

        return s;
    }

    @Override
    public State onBridgeHostUpdate(StateContext context) {
        return new InitializingState();
    }

    @Override
    public State onBridgeResponse(StateContext context, BridgeResponse response) {
        if (response instanceof GetAllLightsResponse) {
            return new RunningState();
        } else if (response instanceof ErrorResponse) {
            ErrorResponse er = (ErrorResponse)response;
            if (er.getType() == ErrorResponse.UNAUTHORIZED_USER) {
                logger.debug("Received unauthorized user response; attempting to create user");
                return new CreateUserState();
            } else {
                logger.error("Received unexpected error from Hue Bridge: {}", er);
                context.setPluginStatus(PluginStatus.failed("Received unexpected error from Hue Bridge. See log for details."));
                return new FailedState();
            }
        }

        logger.error("Received unexpected response from Hue Bridge: {}", response);
        context.setPluginStatus(PluginStatus.failed("Received unexpected response from Hue Bridge. See log for details."));
        return new FailedState();
    }

    @Override
    public State onBridgeRequestFailure(StateContext ctx, Object requestContext, Throwable t) {
        logger.error("Error response received from Hue bridge while requesting list of lights", t);
        ctx.setPluginStatus(PluginStatus.failed("Received error response from Hue bridge. See log for details."));
        return new FailedState();
    }

    @Override
    public State onSetVariable(StateContext context, String deviceId, String name, Object value) {
        logger.debug("Received set variable request while waiting for authorization; ignoring");
        return this;
    }

    @Override
    protected void performRequest(StateContext ctx) {
        ctx.sendGetAllLightsRequest(new GetAllLightsRequest());
    }

    @Override
    protected long getTimeout() {
        return 5000;
    }

    @Override
    protected int getMaximumRetryCount() {
        return 2;
    }
}
