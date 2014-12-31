/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.api.dto.Light;
import com.whizzosoftware.hobson.philipshue.api.dto.GetAllLightsResponse;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class RunningStateTest {
    @Test
    public void testOnRefresh() {
        MockStateContext ctx = new MockStateContext("host");
        RunningState state = new RunningState();
        assertNull(ctx.getPluginStatus());
        assertEquals(0, ctx.getGetAllLightRequestsCount());

        state.onRefresh(ctx);
        assertNotNull(ctx.getPluginStatus());
        assertEquals(PluginStatus.Status.RUNNING, ctx.getPluginStatus().getStatus());
        assertEquals(1, ctx.getGetAllLightRequestsCount());
    }

    @Test
    public void testOnBridgeResponseWithLights() {
        MockStateContext ctx = new MockStateContext("host");
        RunningState state = new RunningState();
        assertEquals(0, ctx.getGetAllLightRequestsCount());

        state.onRefresh(ctx);
        assertEquals(1, ctx.getGetAllLightRequestsCount());

        List<Light> lights = new ArrayList<>();
        lights.add(new Light("1", "Light1", "Model", null));
        lights.add(new Light("2", "Light2", "Model", null));
        lights.add(new Light("3", "Light3", "Model", null));

        state.onBridgeResponse(ctx, new GetAllLightsResponse(lights));
        assertEquals(3, ctx.getHueLights().size());

        // make sure no duplicate lights are created
        state.onBridgeResponse(ctx, new GetAllLightsResponse(lights));
        assertEquals(3, ctx.getHueLights().size());
    }

    @Test
    public void testOnBridgeHostUpdate() {
        MockStateContext ctx = new MockStateContext("host");
        RunningState state = new RunningState();
        assertTrue(state.onBridgeHostUpdate(ctx) instanceof InitializingState);
    }

    @Test
    public void testOnBridgeReuqestFailure() {
        MockStateContext ctx = new MockStateContext("host");
        RunningState state = new RunningState();
        assertTrue(state.onBridgeRequestFailure(ctx, new Exception()) instanceof RunningState);
    }

    @Test
    public void testOnSetVariable() {
        MockStateContext ctx = new MockStateContext("host");
        ctx.createHueLight(new Light("1", "light1", "model1", null));
        assertEquals(0, ctx.getSetLightStateRequests().size());

        RunningState state = new RunningState();
        assertTrue(state.onSetVariable(ctx, "1", "on", true) instanceof RunningState);
    }
}