package com.whizzosoftware.hobson.philipshue.api;

import com.whizzosoftware.hobson.philipshue.api.dto.*;

import org.junit.Test;
import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

public class HueBridgeTest {
    @Test
    public void testParseResponseWithCreateUserRequestSuccess() throws HueException {
        HueBridge bridge = new HueBridge("host", "device", "thedudeabides");
        CreateUserRequest request = new CreateUserRequest("", "thedudeabides");
        ByteArrayInputStream is = new ByteArrayInputStream("[{\"success\":{\"username\": \"thedudeabides\"}}]".getBytes());
        String s = "[{\"success\":{\"username\": \"thedudeabides\"}}]";
        BridgeResponse response = bridge.parseResponse(request, 200, s);
        assertTrue(response instanceof CreateUserResponse);
        assertEquals("thedudeabides", ((CreateUserResponse)response).getUsername());
    }

    @Test
    public void testParseResponseWithCreateUserRequestError() throws HueException {
        HueBridge bridge = new HueBridge("host", "device", "thedudeabides");
        CreateUserRequest request = new CreateUserRequest("", "thedudeabides");
        ByteArrayInputStream is = new ByteArrayInputStream("[{\"error\":{\"type\":1,\"address\":\"address\",\"description\":\"description\"}}]".getBytes());
        String s = "[{\"error\":{\"type\":1,\"address\":\"address\",\"description\":\"description\"}}]";
        BridgeResponse response = bridge.parseResponse(request, 200, s);
        assertTrue(response instanceof ErrorResponse);
        assertEquals(1, (int)((ErrorResponse)response).getType());
    }

    @Test
    public void testGetAllLightsRequestSuccess() throws HueException {
        HueBridge bridge = new HueBridge("host", "device", "thedudeabides");
        GetAllLightsRequest request = new GetAllLightsRequest();
        ByteArrayInputStream is = new ByteArrayInputStream("{\"1\":{\"state\":{\"on\":true,\"bri\":144,\"hue\":13088,\"sat\":212,\"xy\":[0.5128,0.4147],\"ct\":467,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"xy\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 1\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}},\"2\":{\"state\":{\"on\":false,\"bri\":0,\"hue\":0,\"sat\":0,\"xy\":[0,0],\"ct\":0,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 2\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}}}".getBytes());
        String s = "{\"1\":{\"state\":{\"on\":true,\"bri\":144,\"hue\":13088,\"sat\":212,\"xy\":[0.5128,0.4147],\"ct\":467,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"xy\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 1\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}},\"2\":{\"state\":{\"on\":false,\"bri\":0,\"hue\":0,\"sat\":0,\"xy\":[0,0],\"ct\":0,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 2\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}}}";
        BridgeResponse response = bridge.parseResponse(request, 200, s);
        assertTrue(response instanceof GetAllLightsResponse);
        GetAllLightsResponse galr = (GetAllLightsResponse)response;
        assertEquals(2, galr.getLights().size());
    }

    @Test
    public void testGetAllLightsRequestError() throws HueException {
        HueBridge bridge = new HueBridge("host", "device", "thedudeabides");
        GetAllLightsRequest request = new GetAllLightsRequest();
        ByteArrayInputStream is = new ByteArrayInputStream("[{\"error\":{\"type\":1,\"address\":\"address\",\"description\":\"description\"}}]".getBytes());
        String s = "[{\"error\":{\"type\":1,\"address\":\"address\",\"description\":\"description\"}}]";
        BridgeResponse response = bridge.parseResponse(request, 200, s);
        assertTrue(response instanceof ErrorResponse);
        assertEquals(1, (int)((ErrorResponse)response).getType());
    }

    @Test
    public void testGetLightAttributeAndStateRequestSuccess() throws HueException {
        HueBridge bridge = new HueBridge("host", "device", "thedudeabides");
        GetLightAttributeAndStateRequest request = new GetLightAttributeAndStateRequest("device1");
        ByteArrayInputStream is = new ByteArrayInputStream("{\"state\":{\"hue\":50000,\"on\":true,\"effect\":\"none\",\"alert\":\"none\",\"bri\":200,\"sat\":200,\"ct\":500,\"xy\":[0.5,0.5],\"reachable\":true,\"colormode\":\"hs\"},\"type\":\"Living Colors\",\"name\":\"LC 1\",\"modelid\":\"LC0015\",\"swversion\":\"1.0.3\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}}".getBytes());
        String s = "{\"state\":{\"hue\":50000,\"on\":true,\"effect\":\"none\",\"alert\":\"none\",\"bri\":200,\"sat\":200,\"ct\":500,\"xy\":[0.5,0.5],\"reachable\":true,\"colormode\":\"hs\"},\"type\":\"Living Colors\",\"name\":\"LC 1\",\"modelid\":\"LC0015\",\"swversion\":\"1.0.3\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}}";
        BridgeResponse response = bridge.parseResponse(request, 200, s);
        assertTrue(response instanceof GetLightAttributeAndStateResponse);
        GetLightAttributeAndStateResponse glasr = (GetLightAttributeAndStateResponse)response;
        assertEquals(true, glasr.getState().getOn());
    }

    @Test
    public void testGetLightAttributeAndStateRequestError() throws HueException {
        HueBridge bridge = new HueBridge("host", "device", "thedudeabides");
        GetLightAttributeAndStateRequest request = new GetLightAttributeAndStateRequest("device1");
        String s = "[{\"error\":{\"type\":1,\"address\":\"address\",\"description\":\"description\"}}]";
        BridgeResponse response = bridge.parseResponse(request, 200, s);
        assertTrue(response instanceof ErrorResponse);
        assertEquals(1, (int)((ErrorResponse)response).getType());
    }

    @Test
    public void testSetLightStateRequestSuccess() throws HueException {
        HueBridge bridge = new HueBridge("host", "device", "thedudeabides");
        SetLightStateRequest request = new SetLightStateRequest("light1", new LightState(null, null, null, null, null, null, null, null, null));
        ByteArrayInputStream is = new ByteArrayInputStream("[{\"success\":{\"/lights/1/state/bri\":200}},{\"success\":{\"/lights/1/state/on\":true}},{\"success\":{\"/lights/1/state/hue\":50000}}]".getBytes());
        String s = "[{\"success\":{\"/lights/1/state/bri\":200}},{\"success\":{\"/lights/1/state/on\":true}},{\"success\":{\"/lights/1/state/hue\":50000}}]";
        BridgeResponse response = bridge.parseResponse(request, 200, s);
        assertTrue(response instanceof SetLightStateResponse);
    }

    @Test
    public void testSetLightStateRequestError() throws HueException {
        HueBridge bridge = new HueBridge("host", "device", "thedudeabides");
        SetLightStateRequest request = new SetLightStateRequest("light1", new LightState(null, null, null, null, null, null, null, null, null));
        ByteArrayInputStream is = new ByteArrayInputStream("[{\"error\":{\"type\":1,\"address\":\"address\",\"description\":\"description\"}}]".getBytes());
        String s = "[{\"error\":{\"type\":1,\"address\":\"address\",\"description\":\"description\"}}]";
        BridgeResponse response = bridge.parseResponse(request, 200, s);
        assertTrue(response instanceof ErrorResponse);
        assertEquals(1, (int)((ErrorResponse)response).getType());
    }
}
