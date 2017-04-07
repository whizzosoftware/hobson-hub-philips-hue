/*
 *******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.device.proxy.HobsonDeviceProxy;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.variable.DeviceVariableContext;
import com.whizzosoftware.hobson.api.variable.DeviceVariableState;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.philipshue.api.dto.GetAllLightsRequest;
import com.whizzosoftware.hobson.philipshue.state.AuthorizingState;
import com.whizzosoftware.hobson.philipshue.state.RunningState;
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class HuePluginTest {
    @Test
    public void testHubResponseFailure() throws Exception {
        MockDeviceManager dm = new MockDeviceManager();

        HuePlugin plugin = new HuePlugin("pluginId", "version", "description");
        plugin.setDeviceManager(dm);

        PropertyContainer config = new PropertyContainer();
        config.setPropertyValue(HuePlugin.PROP_BRIDGE_HOST, "localhost:8081");
        plugin.onPluginConfigurationUpdate(config);

        // start the state machine
        plugin.onRefresh();

        // should be in authorizing state now -- send a valid response
        assertTrue(plugin.getState() instanceof AuthorizingState);
        plugin.onHttpResponse(new MockHttpResponse(200, "{\"1\":{\"state\":{\"on\":true,\"bri\":144,\"hue\":13088,\"sat\":212,\"xy\":[0.5128,0.4147],\"ct\":467,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 1\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}},\"2\":{\"state\":{\"on\":false,\"bri\":0,\"hue\":0,\"sat\":0,\"xy\":[0,0],\"ct\":0,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 2\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}}}"), new GetAllLightsRequest());

        // should now be in running state -- send another valid response to load devices
        assertTrue(plugin.getState() instanceof RunningState);
        plugin.onHttpResponse(new MockHttpResponse(200, "{\"1\":{\"state\":{\"on\":true,\"bri\":144,\"hue\":13088,\"sat\":212,\"xy\":[0.5128,0.4147],\"ct\":467,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 1\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}},\"2\":{\"state\":{\"on\":false,\"bri\":0,\"hue\":0,\"sat\":0,\"xy\":[0,0],\"ct\":0,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 2\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}}}"), new GetAllLightsRequest());

        // check that both lights were published
        int pds = 0;
        int attempts = 0;
        while (pds != 2 && attempts < 3) {
            pds = dm.getPublishedDeviceCount(plugin.getContext());
            attempts++;
            Thread.sleep(500);
        }

        assertTrue("Unable to validate published devices", attempts < 3);

        // give the lights the opportunity to start up (this would normally be called automatically by the runtime)
        Collection<HobsonDeviceProxy> devices = plugin.getDeviceProxies();
        for (HobsonDeviceProxy device : devices) {
            device.onStartup(null, null);
        }

        DeviceVariableState v = dm.getDeviceVariable(DeviceVariableContext.create(DeviceContext.create(plugin.getContext(), "1"), VariableConstants.ON));
        assertNotNull(v);
        assertTrue((boolean)v.getValue());
        v = dm.getDeviceVariable(DeviceVariableContext.create(DeviceContext.create(plugin.getContext(), "2"), VariableConstants.ON));
        assertNotNull(v);
        assertFalse((boolean)v.getValue());
        v = dm.getDeviceVariable(DeviceVariableContext.create(DeviceContext.create(plugin.getContext(), "1"), VariableConstants.COLOR));
        assertNotNull(v);
        assertEquals("hsb(72,83,56)", v.getValue());
        v = dm.getDeviceVariable(DeviceVariableContext.create(DeviceContext.create(plugin.getContext(), "2"), VariableConstants.COLOR));
        assertNotNull(v);
        assertEquals("hsb(0,0,0)", v.getValue());
    }
}
