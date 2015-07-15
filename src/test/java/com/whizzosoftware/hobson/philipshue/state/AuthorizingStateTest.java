/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.HuePlugin;
import com.whizzosoftware.hobson.philipshue.api.dto.Light;
import com.whizzosoftware.hobson.philipshue.api.dto.ErrorResponse;
import com.whizzosoftware.hobson.philipshue.api.dto.GetAllLightsResponse;
import com.whizzosoftware.hobson.philipshue.api.dto.GetLightAttributeAndStateResponse;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class AuthorizingStateTest {
    @Test
    public void testInit() {
        AuthorizingState state = new AuthorizingState();
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        assertEquals(0, ctx.getGetAllLightRequestsCount());

        // first attempt
        state.onRefresh(ctx);
        assertEquals(1, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // respond with lights
        List<Light> lights = new ArrayList<>();
        lights.add(new Light("1", "Light", "model", null));
        assertTrue(state.onBridgeResponse(ctx, new GetAllLightsResponse(lights)) instanceof RunningState);
    }

    @Test
    public void testInitWithAuthError() {
        AuthorizingState state = new AuthorizingState();
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        assertEquals(0, ctx.getGetAllLightRequestsCount());

        // first attempt
        state.onRefresh(ctx);
        assertEquals(1, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // response with unauthorized user
        assertTrue(state.onBridgeResponse(ctx, new ErrorResponse(ErrorResponse.UNAUTHORIZED_USER, null, null)) instanceof CreateUserState);
    }

    @Test
    public void testInitWithOtherError() {
        AuthorizingState state = new AuthorizingState();
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        assertEquals(0, ctx.getGetAllLightRequestsCount());

        // first attempt
        state.onRefresh(ctx);
        assertEquals(1, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // response with unauthorized user
        assertTrue(state.onBridgeResponse(ctx, new ErrorResponse(ErrorResponse.LINK_BUTTON_NOT_PRESSED, null, null)) instanceof FailedState);
        assertEquals(PluginStatus.Code.FAILED, ctx.getPluginStatus().getCode());
    }

    @Test
    public void testInitWithUnexpectedResponse() {
        AuthorizingState state = new AuthorizingState();
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        assertEquals(0, ctx.getGetAllLightRequestsCount());

        // first attempt
        state.onRefresh(ctx);
        assertEquals(1, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // response with unauthorized user
        assertTrue(state.onBridgeResponse(ctx, new GetLightAttributeAndStateResponse("", null)) instanceof FailedState);
        assertEquals(PluginStatus.Code.FAILED, ctx.getPluginStatus().getCode());
    }

    @Test
    public void testInitWithTimeouts() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        AuthorizingState state = new AuthorizingState();
        assertEquals(0, ctx.getGetAllLightRequestsCount());

        long now = System.currentTimeMillis();

        // check for initial request
        state.onRefresh(ctx, now);
        assertEquals(1, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // check that next request isn't made too soon
        assertTrue(state.onRefresh(ctx, now + 1000) instanceof AuthorizingState);
        assertEquals(1, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // check that second request is made when timeout is spot on
        assertTrue(state.onRefresh(ctx, now + state.getTimeout()) instanceof AuthorizingState);
        assertEquals(2, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // check that next request isn't made too soon
        assertTrue(state.onRefresh(ctx, now + state.getTimeout() - 1) instanceof AuthorizingState);
        assertEquals(2, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // check that the third request is made when timeout has passed
        assertTrue(state.onRefresh(ctx, now + state.getTimeout() + state.getTimeout() + 100) instanceof AuthorizingState);
        assertEquals(3, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // check that no further request is made
        assertTrue(state.onRefresh(ctx, now + state.getTimeout() + state.getTimeout() + state.getTimeout() - 1) instanceof AuthorizingState);
        assertEquals(3, ctx.getGetAllLightRequestsCount());
        assertNull(ctx.getPluginStatus());

        // check that failure occurs (note that we were 100 seconds ahead last time so we need to be 100 seconds ahead this time as well)
        assertTrue(state.onRefresh(ctx, now + state.getTimeout() + state.getTimeout() + state.getTimeout() + 100) instanceof FailedState);
        assertEquals(3, ctx.getGetAllLightRequestsCount());
        assertNotNull(ctx.getPluginStatus());
        assertEquals(PluginStatus.Code.FAILED, ctx.getPluginStatus().getCode());
    }

    @Test
    public void testOnBridgeHostUpdate() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        AuthorizingState state = new AuthorizingState();
        assertTrue(state.onBridgeHostUpdate(ctx) instanceof InitializingState);
    }

    @Test
    public void testOnBridgeRequestFailure() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        AuthorizingState state = new AuthorizingState();
        assertNull(ctx.getPluginStatus());
        assertTrue(state.onBridgeRequestFailure(ctx, null, new Exception()) instanceof FailedState);
        assertEquals(PluginStatus.Code.FAILED, ctx.getPluginStatus().getCode());
    }

    @Test
    public void testOnSetVariable() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        AuthorizingState state = new AuthorizingState();
        assertTrue(state.onSetVariable(ctx, "", "", "") instanceof AuthorizingState);
    }
}
