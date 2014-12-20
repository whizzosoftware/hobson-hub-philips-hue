/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

import com.whizzosoftware.hobson.philipshue.api.dto.*;
import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

public class HueBridgeParserTest {
    @Test
    public void testParseGetAllLights() throws Exception {
        HueBridgeParser parser = new HueBridgeParser();
        GetAllLightsResponse response = (GetAllLightsResponse)parser.parseGetAllLightsResponse(200, "{\"1\":{\"name\": \"Hue Downlight\"},\"2\":{\"name\": \"Hue Downlight 1\"},\"3\":{\"name\": \"Hue Downlight 2\"}}");
        assertEquals(3, response.getLights().size());
    }

    @Test
    public void testParseGetAllLightsWithError() throws Exception {
        HueBridgeParser parser = new HueBridgeParser();
        BridgeResponse response = parser.parseGetAllLightsResponse(200, "[{\"error\":{\"type\":1,\"address\":\"/lights\",\"description\":\"unauthorized user\"}}]");
        assertTrue(response instanceof ErrorResponse);
    }

    @Test
    public void testCreateUser() throws HueException {
        HueBridgeParser parser = new HueBridgeParser();
        parser.parseCreateUserResponse(200, "[{\"success\":{\"username\":\"fasfjewnfwenkfekf\"}}]");
    }

    @Test
    public void testCreateUserWithError() throws HueException {
        HueBridgeParser parser = new HueBridgeParser();
        BridgeResponse response = parser.parseCreateUserResponse(200, "[{\"error\":{\"type\":101,\"address\":\"\",\"description\":\"link button not pressed\"}}]");
        assertTrue(response instanceof ErrorResponse);
    }

    @Test
    public void testParseGetLightAttributeAndState() throws HueException {
        HueBridgeParser parser = new HueBridgeParser();
        GetLightAttributeAndStateResponse response = (GetLightAttributeAndStateResponse)parser.parseGetLightAttributeAndStateResponse("", 200, "{\"state\": {\"on\":false,\"bri\":0,\"hue\":0,\"sat\":0,\"xy\":[0.700,0.200],\"ct\":0,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true}, \"type\": \"Extended color light\", \"name\": \"Hue Downlight\", \"modelid\": \"LCT002\", \"swversion\": \"66010673\", \"pointsymbol\": { \"1\":\"none\", \"2\":\"none\", \"3\":\"none\", \"4\":\"none\", \"5\":\"none\", \"6\":\"none\", \"7\":\"none\", \"8\":\"none\" }}");
        LightState state = response.getState();
        assertEquals("none", state.getEffect());
        assertEquals((Integer)0, state.getBrightness());
        assertNotNull(state.getX());
        assertEquals(0.700, state.getX(), 1);
        assertNotNull(state.getY());
        assertEquals(0.200, state.getY(), 1);
        assertEquals(true, state.isReachable());
        assertEquals(false, state.getOn());
    }

    @Test
    public void testParseSetLightStateResponse() throws HueException {
        HueBridgeParser parser = new HueBridgeParser();
        parser.parseSetLightStateResponse(200, "[{\"success\":{\"/lights/1/state/on\":true}},{\"success\":{\"/lights/1/state/hue\":10}},{\"success\":{\"/lights/1/state/sat\":20}},{\"success\":{\"/lights/1/state/bri\":30}},{\"success\":{\"/lights/1/state/effect\":\"none\"}}]");
    }
}
