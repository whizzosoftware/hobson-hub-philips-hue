/*******************************************************************************
 * Copyright (c) 2015 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.hub.HubContext;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.MockVariableManager;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.philipshue.api.dto.GetAllLightsRequest;
import com.whizzosoftware.hobson.philipshue.state.AuthorizingState;
import com.whizzosoftware.hobson.philipshue.state.RunningState;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Collection;

public class HuePluginTest {
    @Test
    public void testHubResponseFailure() {
        MockDeviceManager dm = new MockDeviceManager();
        MockVariableManager vm = new MockVariableManager();

        HuePlugin plugin = new HuePlugin("pluginId");
        plugin.setDeviceManager(dm);
        plugin.setVariableManager(vm);

        PropertyContainer config = new PropertyContainer();
        config.setPropertyValue("bridge.host", "localhost:8081");
        plugin.onPluginConfigurationUpdate(config);

        // start the state machine
        plugin.onRefresh();

        // should be in authorizing state now -- send a valid response
        assertTrue(plugin.getState() instanceof AuthorizingState);
        plugin.onHttpResponse(200, null, "{\"1\":{\"state\":{\"on\":true,\"bri\":144,\"hue\":13088,\"sat\":212,\"xy\":[0.5128,0.4147],\"ct\":467,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 1\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}},\"2\":{\"state\":{\"on\":false,\"bri\":0,\"hue\":0,\"sat\":0,\"xy\":[0,0],\"ct\":0,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 2\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}}}", new GetAllLightsRequest());

        // should now be in running state -- send another valid response to load devices
        assertTrue(plugin.getState() instanceof RunningState);
        plugin.onHttpResponse(200, null, "{\"1\":{\"state\":{\"on\":true,\"bri\":144,\"hue\":13088,\"sat\":212,\"xy\":[0.5128,0.4147],\"ct\":467,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 1\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}},\"2\":{\"state\":{\"on\":false,\"bri\":0,\"hue\":0,\"sat\":0,\"xy\":[0,0],\"ct\":0,\"alert\":\"none\",\"effect\":\"none\",\"colormode\":\"hs\",\"reachable\":true},\"type\":\"Extended color light\",\"name\":\"Hue Lamp 2\",\"modelid\":\"LCT001\",\"swversion\":\"66009461\",\"pointsymbol\":{\"1\":\"none\",\"2\":\"none\",\"3\":\"none\",\"4\":\"none\",\"5\":\"none\",\"6\":\"none\",\"7\":\"none\",\"8\":\"none\"}}}", new GetAllLightsRequest());

        // check that both lights were published
        assertEquals(2, dm.getPublishedDevices().size());

        // give the lights the opportunity to start up (this would normally be called automatically by the runtime)
        Collection<HobsonDevice> devices = dm.getAllDevices(HubContext.createLocal());
        for (HobsonDevice device : devices) {
            ((HueLight)device).onStartup(null);
        }

        Collection<HobsonVariable> vars = vm.getPublishedDeviceVariables();

        // check that variables were published for both devices
        assertEquals(4, vars.size());

        // verify that the initial "on" variable is set correctly
        for (HobsonVariable hv : vars) {
            if (hv.getContext().getDeviceId().equals("1") && hv.getContext().getName().equals("on")) {
                assertEquals(true, hv.getValue());
            } else if (hv.getContext().getDeviceId().equals("2") && hv.getContext().getName().equals("on")) {
                assertEquals(false, hv.getValue());
            } else if (hv.getContext().getDeviceId().equals("1") && hv.getContext().getName().equals("color")) {
                assertEquals("hsb(72,83,56)", hv.getValue());
            } else if (hv.getContext().getDeviceId().equals("2") && hv.getContext().getName().equals("color")) {
                assertEquals("hsb(0,0,0)", hv.getValue());
            } else {
                fail("Unknown variable encountered");
            }
        }

        // send a HTTP request failure
        plugin.onHttpRequestFailure(new Exception(), new GetAllLightsRequest());

        // verify that the "on" variables are now null
        assertEquals(4, vm.getVariableUpdates().size());

        for (VariableUpdate vu : vm.getVariableUpdates()) {
            assertTrue(vu.getDeviceId().equals("1") || vu.getDeviceId().equals("2"));
            assertNull(vu.getValue());
        }
    }
}
