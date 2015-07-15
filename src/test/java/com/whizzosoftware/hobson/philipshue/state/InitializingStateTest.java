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
import com.whizzosoftware.hobson.philipshue.api.dto.GetAllLightsResponse;
import org.junit.Test;
import static org.junit.Assert.*;

public class InitializingStateTest {
    @Test
    public void testOnRefreshWithHost() {
        InitializingState state = new InitializingState();
        HuePlugin plugin = new HuePlugin("plugin");
        assertTrue(state.onRefresh(new MockStateContext(plugin, "host")) instanceof AuthorizingState);
    }

    @Test
    public void testOnRefreshWithoutHost() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, null);
        InitializingState state = new InitializingState();
        assertNull(ctx.getPluginStatus());
        assertTrue(state.onRefresh(ctx) instanceof FailedState);
        assertNotNull(ctx.getPluginStatus());
        assertEquals(PluginStatus.Code.NOT_CONFIGURED, ctx.getPluginStatus().getCode());
    }

    @Test
    public void testOnBridgeResponse() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        InitializingState state = new InitializingState();
        assertTrue(state.onBridgeResponse(ctx, new GetAllLightsResponse(null)) instanceof InitializingState);
    }

    @Test
    public void testOnBridgeHostUpdate() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        InitializingState state = new InitializingState();
        assertTrue(state.onBridgeHostUpdate(ctx) instanceof InitializingState);
    }

    @Test
    public void testOnBridgeRequestFailure() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        InitializingState state = new InitializingState();
        assertTrue(state.onBridgeRequestFailure(ctx, null, new Exception()) instanceof InitializingState);
    }

    @Test
    public void testOnSetVariable() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        InitializingState state = new InitializingState();
        assertTrue(state.onSetVariable(ctx, "", "", "") instanceof InitializingState);
    }
}
