/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.api.dto.CreateUserResponse;
import com.whizzosoftware.hobson.philipshue.api.dto.ErrorResponse;
import com.whizzosoftware.hobson.philipshue.api.dto.GetLightAttributeAndStateResponse;

import org.junit.Test;
import static org.junit.Assert.*;

public class CreateUserStateTest {
    @Test
    public void testInit() {
        MockStateContext ctx = new MockStateContext("host");
        CreateUserState state = new CreateUserState();
        assertEquals(0, ctx.getSendCreateUserRequestCount());

        state.onRefresh(ctx);
        assertEquals(1, ctx.getSendCreateUserRequestCount());
        assertTrue(state.onBridgeResponse(ctx, new CreateUserResponse("")) instanceof RunningState);
    }

    @Test
    public void testInitWithUnexpectedResponse() {
        MockStateContext ctx = new MockStateContext("host");
        CreateUserState state = new CreateUserState();
        assertEquals(0, ctx.getSendCreateUserRequestCount());

        state.onRefresh(ctx);
        assertEquals(1, ctx.getSendCreateUserRequestCount());
        assertTrue(state.onBridgeResponse(ctx, new GetLightAttributeAndStateResponse("", null)) instanceof FailedState);
        assertNotNull(ctx.getPluginStatus());
        assertEquals(PluginStatus.Status.FAILED, ctx.getPluginStatus().getStatus());
    }

    @Test
    public void testInitWithLinkButtonErrorResponse() {
        MockStateContext ctx = new MockStateContext("host");
        CreateUserState state = new CreateUserState();
        assertEquals(0, ctx.getSendCreateUserRequestCount());

        state.onRefresh(ctx);
        assertEquals(1, ctx.getSendCreateUserRequestCount());
        assertTrue(state.onBridgeResponse(ctx, new ErrorResponse(ErrorResponse.LINK_BUTTON_NOT_PRESSED, null, null)) instanceof CreateUserState);
        assertNotNull(ctx.getPluginStatus());
    }

    @Test
    public void testInitWithOtherErrorResponse() {
        MockStateContext ctx = new MockStateContext("host");
        CreateUserState state = new CreateUserState();
        assertEquals(0, ctx.getSendCreateUserRequestCount());

        state.onRefresh(ctx);
        assertEquals(1, ctx.getSendCreateUserRequestCount());
        assertTrue(state.onBridgeResponse(ctx, new ErrorResponse(ErrorResponse.UNAUTHORIZED_USER, null, null)) instanceof FailedState);
        assertNotNull(ctx.getPluginStatus());
        assertEquals(PluginStatus.Status.FAILED, ctx.getPluginStatus().getStatus());
    }

    @Test
    public void testInitWithTimeouts() {
        MockStateContext ctx = new MockStateContext("host");
        CreateUserState state = new CreateUserState();
        assertEquals(0, ctx.getSendCreateUserRequestCount());

        long now = System.currentTimeMillis();

        // check for initial request
        state.onRefresh(ctx, now);
        assertEquals(1, ctx.getSendCreateUserRequestCount());

        // check that next request isn't made too soon
        assertTrue(state.onRefresh(ctx, now + 1000) instanceof CreateUserState);
        assertEquals(1, ctx.getSendCreateUserRequestCount());

        // check that second request is made when timeout is spot on
        assertTrue(state.onRefresh(ctx, now + state.getTimeout()) instanceof CreateUserState);
        assertEquals(2, ctx.getSendCreateUserRequestCount());

        // check that next request isn't made too soon
        assertTrue(state.onRefresh(ctx, now + state.getTimeout() - 1) instanceof CreateUserState);
        assertEquals(2, ctx.getSendCreateUserRequestCount());

        // check that the third request is made when timeout has passed
        assertTrue(state.onRefresh(ctx, now + state.getTimeout() + state.getTimeout() + 100) instanceof CreateUserState);
        assertEquals(3, ctx.getSendCreateUserRequestCount());

        // check that no further request is made
        assertTrue(state.onRefresh(ctx, now + state.getTimeout() + state.getTimeout() + state.getTimeout() - 1) instanceof CreateUserState);
        assertEquals(3, ctx.getSendCreateUserRequestCount());

        // check that failure occurs (note that we were 100 seconds ahead last time so we need to be 100 seconds ahead this time as well)
        assertTrue(state.onRefresh(ctx, now + state.getTimeout() + state.getTimeout() + state.getTimeout() + 100) instanceof FailedState);
        assertEquals(3, ctx.getSendCreateUserRequestCount());
        assertNotNull(ctx.getPluginStatus());
        assertEquals(PluginStatus.Status.FAILED, ctx.getPluginStatus().getStatus());
    }

    @Test
    public void testOnBridgeHostUpdate() {
        MockStateContext ctx = new MockStateContext("host");
        CreateUserState state = new CreateUserState();
        assertTrue(state.onBridgeHostUpdate(ctx) instanceof InitializingState);
    }

    @Test
    public void testOnBridgeRequestFailure() {
        MockStateContext ctx = new MockStateContext("host");
        CreateUserState state = new CreateUserState();
        assertNull(ctx.getPluginStatus());
        assertTrue(state.onBridgeRequestFailure(ctx, null, new Exception()) instanceof FailedState);
        assertEquals(PluginStatus.Status.FAILED, ctx.getPluginStatus().getStatus());
    }

    @Test
    public void testOnSetVariable() {
        MockStateContext ctx = new MockStateContext("host");
        CreateUserState state = new CreateUserState();
        assertTrue(state.onSetVariable(ctx, "", "", "") instanceof CreateUserState);
    }
}
