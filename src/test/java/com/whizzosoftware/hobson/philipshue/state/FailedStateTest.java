/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.philipshue.HuePlugin;
import com.whizzosoftware.hobson.philipshue.api.dto.GetAllLightsResponse;
import org.junit.Test;
import org.junit.internal.runners.statements.Fail;

import static org.junit.Assert.*;

public class FailedStateTest {
    @Test
    public void testOnRefresh() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        FailedState state = new FailedState();
        assertTrue(state.onRefresh(ctx) instanceof FailedState);
        assertNull(ctx.getPluginStatus());
    }

    @Test
    public void testOnBridgeHostUpdated() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        FailedState state = new FailedState();
        assertTrue(state.onBridgeHostUpdate(ctx) instanceof InitializingState);
    }

    @Test
    public void testOnBridgeResponse() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        FailedState state = new FailedState();
        assertTrue(state.onBridgeResponse(ctx, new GetAllLightsResponse(null)) instanceof FailedState);
    }

    @Test
    public void testOnBridgeRequestFailure() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        FailedState state = new FailedState();
        assertTrue(state.onBridgeRequestFailure(ctx, null, new Exception()) instanceof FailedState);
    }

    @Test
    public void testOnSetVariable() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext ctx = new MockStateContext(plugin, "host");
        FailedState state = new FailedState();
        assertTrue(state.onSetVariable(ctx, "", "", "") instanceof FailedState);
    }
}
