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
import com.whizzosoftware.hobson.philipshue.api.dto.CreateUserRequest;
import com.whizzosoftware.hobson.philipshue.api.dto.CreateUserResponse;
import com.whizzosoftware.hobson.philipshue.api.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A state representing the process of creating a Hobson specific user on the Hue bridge.
 *
 * @author Dan Noguerol
 */
public class CreateUserState extends AbstractTimeoutState {
    private final static Logger logger = LoggerFactory.getLogger(CreateUserState.class);

    public State onRefresh(StateContext ctx, long now) {
        State state = super.onRefresh(ctx, now);

        if (state instanceof FailedState) {
            logger.warn("Timeout waiting for Hue bridge to respond to CreateUser request");
            ctx.setPluginStatus(PluginStatus.failed("Timeout waiting for response from Hue bridge. See log for details."));
        }

        return state;
    }

    @Override
    public State onBridgeHostUpdate(StateContext context) {
        return new InitializingState();
    }

    @Override
    public State onBridgeResponse(StateContext context, BridgeResponse response) {
        if (response instanceof CreateUserResponse) {
            return new RunningState();
        } else if (response instanceof ErrorResponse) {
            ErrorResponse er = (ErrorResponse)response;
            if (er.getType() == ErrorResponse.LINK_BUTTON_NOT_PRESSED) {
                logger.debug("Plugin not authorized to talk to Hue bridge; waiting for user to press bridge button");
                context.setPluginStatus(PluginStatus.failed("Hobson is not authorized to talk to the Hue bridge. Please press the button on your bridge within the next 30 seconds."));
                return this;
            }
        }

        logger.warn("Received unexpected response to CreateUser request: {}", response);
        context.setPluginStatus(PluginStatus.failed("Unable to create Hue bridge user. See log for details."));
        return new FailedState();
    }

    @Override
    public State onBridgeRequestFailure(StateContext ctx, Object requestContext, Throwable t) {
        logger.error("Error response received from Hue bridge while creating user", t);
        ctx.setPluginStatus(PluginStatus.failed("Received error response from Hue bridge. See log for details."));
        return new FailedState();
    }

    @Override
    public State onSetVariable(StateContext context, String deviceId, String name, Object value) {
        return this;
    }

    @Override
    protected void performRequest(StateContext context) {
        context.sendCreateUserRequest(new CreateUserRequest(context.getHueDeviceString(), context.getHueUserString()));
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
