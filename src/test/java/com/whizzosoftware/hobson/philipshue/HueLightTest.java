/*
 *******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.device.MockDeviceManager;
import com.whizzosoftware.hobson.api.event.MockEventManager;
import com.whizzosoftware.hobson.api.event.device.DeviceUnavailableEvent;
import com.whizzosoftware.hobson.api.event.device.DeviceVariablesUpdateEvent;
import com.whizzosoftware.hobson.api.variable.DeviceVariableState;
import com.whizzosoftware.hobson.api.variable.DeviceVariableUpdate;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.philipshue.api.HueException;
import com.whizzosoftware.hobson.philipshue.api.dto.Light;
import com.whizzosoftware.hobson.philipshue.api.dto.LightState;
import com.whizzosoftware.hobson.philipshue.state.MockStateContext;

import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.*;

public class HueLightTest {
    @Test
    public void testNullUpdateDetection() throws Exception {
        MockDeviceManager dm = new MockDeviceManager();
        MockEventManager em = new MockEventManager();
        HuePlugin plugin = new HuePlugin("id", "version", "description");
        plugin.setDeviceManager(dm);
        plugin.setEventManager(em);
        MockStateContext context = new MockStateContext(plugin, "host");
        HueLight light = new HueLight(plugin, "1", "model", "Hue Light 1", context);
        light.onStartup(null, null);
        assertEquals(1, em.getEventCount());
        light.refresh();
        assertEquals(1, em.getEventCount());
    }

    @Test
    public void testUpdateDetection() throws Exception {
        MockDeviceManager dm = new MockDeviceManager();
        MockEventManager em = new MockEventManager();
        HuePlugin plugin = new HuePlugin("id", "version", "description");
        plugin.setDeviceManager(dm);
        plugin.setEventManager(em);
        MockStateContext context = new MockStateContext(plugin, "host");

        LightState newState = new LightState(true, 65535, 254, 254, null, LightState.MODE_HS, null, null, true);

        // start with one light in a known state
        context.createHueLight(new Light("1", "Hue Light 1", "model", new LightState(true, null, null, 255, null, null, null, null, true)));

        // check that there is no variable updates prior to the refresh() call
        HueLight light = new HueLight(plugin, "1", null, "Hue Light 1", context);
        light.onStartup(null, null);
        assertEquals(1, em.getEventCount());

        DeviceVariableState var = light.getVariableState(VariableConstants.ON);
        assertNotNull(var);
        assertNull(var.getValue());
        var = light.getVariableState(VariableConstants.COLOR);
        assertNotNull(var);
        assertNull(var.getValue());

        // onLightState() should apply the current delegate light state resulting in two variable updates
        // (one for the update and one for the availability change)
        em.clearEvents();
        light.onLightState(newState);
        assertEquals(2, em.getEventCount());
        assertTrue(em.getEvent(1) instanceof DeviceVariablesUpdateEvent);
        for (DeviceVariableUpdate v : ((DeviceVariablesUpdateEvent) em.getEvent(1)).getUpdates()) {
            assertTrue((VariableConstants.COLOR.equals(v.getName()) && "hsb(360,100,100)".equals(v.getNewValue())) || (VariableConstants.ON.equals(v.getName()) && ((boolean) v.getNewValue())));
        }

        // set the device variable and check that there are no updates detected after refresh()
        em.clearEvents();
        light.onLightState(newState);
        assertEquals(0, em.getEventCount());

        // set the light state again to same values and verify still no updates detected
        light.onLightState(newState);
        assertEquals(0, em.getEventCount());

        // set the light state to new color value and verify one update detected
        light.onLightState(new LightState(true, 30000, 254, 254, null, LightState.MODE_HS, null, null, true));
        light.refresh();
        assertEquals(1, em.getEventCount());
        assertTrue(em.getEvent(0) instanceof DeviceVariablesUpdateEvent);
        Collection<DeviceVariableUpdate> updates = ((DeviceVariablesUpdateEvent)em.getEvent(0)).getUpdates();
        assertEquals(1, updates.size());
        DeviceVariableUpdate v = updates.iterator().next();
        assertEquals(VariableConstants.COLOR, v.getName());
        assertEquals("hsb(165,100,100)", v.getNewValue());
    }

    @Test
    public void testKnownLightStateToUnknown() throws HueException {
        MockDeviceManager dm = new MockDeviceManager();
        MockEventManager em = new MockEventManager();
        HuePlugin plugin = new HuePlugin("plugin", "version", "description");
        plugin.setDeviceManager(dm);
        plugin.setEventManager(em);
        MockStateContext context = new MockStateContext(plugin, "host");
        LightState state = new LightState(true, null, null, null, null, null, null, null, true);

        // check that there is no update prior to the refresh() call
        HueLight light = new HueLight(plugin, "1", null, "Hue Light 1", context);
        assertEquals(0, em.getEventCount());
        light.onStartup(null, null);
        assertEquals(1, em.getEventCount());
        DeviceVariableState var = light.getVariableState(VariableConstants.ON);
        assertNotNull(var);
        assertNull(var.getValue());
        em.clearEvents();

        // we expect one updates after refresh() since the device's variables are still null
        light.onLightState(state);
        assertEquals(2, em.getEventCount());
        DeviceVariableUpdate v = ((DeviceVariablesUpdateEvent)em.getEvent(1)).getUpdates().iterator().next();
        assertEquals(VariableConstants.ON, v.getName());
        assertEquals(true, v.getNewValue());

        em.clearEvents();

        // set light state to unreachable
        state.setReachable(false);
        light.onLightState(state);
        assertEquals(1, em.getEventCount());
        assertTrue(em.getEvent(0) instanceof DeviceUnavailableEvent);
    }

    @Test
    public void testConvertLightStateToColor() {
        MockDeviceManager dm = new MockDeviceManager();
        HuePlugin plugin = new HuePlugin("plugin", "version", "description");
        plugin.setDeviceManager(dm);
        HueLight light = new HueLight(plugin, null, null, null, null);
        assertNull(light.convertLightStateToColor(null));
        assertNull(light.convertLightStateToColor(new LightState(null, null, null, null, null, null, null, null, null)));
        assertEquals("hsb(360,100,100)", light.convertLightStateToColor(new LightState(null, 65535, 254, 254, null, LightState.MODE_HS, null, null, null)));
    }

    @Test
    public void testConvertBrightnessToLevel() {
        MockDeviceManager dm = new MockDeviceManager();
        HuePlugin plugin = new HuePlugin("plugin", "version", "description");
        plugin.setDeviceManager(dm);
        HueLight hl = new HueLight(plugin, null, null, null, null);
        assertEquals(0, (int)hl.convertBrightnessToLevel(0));
        assertEquals(1, (int)hl.convertBrightnessToLevel(2));
        assertEquals(2, (int)hl.convertBrightnessToLevel(5));
        assertEquals(3, (int)hl.convertBrightnessToLevel(7));
        assertEquals(4, (int)hl.convertBrightnessToLevel(10));
        assertEquals(8, (int)hl.convertBrightnessToLevel(20));
        assertEquals(50, (int)hl.convertBrightnessToLevel(127));
        assertEquals(100, (int)hl.convertBrightnessToLevel(255));
    }

    @Test
    public void testConvertLevelToBrightness() {
        MockDeviceManager dm = new MockDeviceManager();
        HuePlugin plugin = new HuePlugin("plugin", "version", "description");
        plugin.setDeviceManager(dm);
        HueLight hl = new HueLight(plugin, null, null, null, null);
        assertEquals(0, (int)hl.convertLevelToBrightness(0));
        assertEquals(2, (int)hl.convertLevelToBrightness(1));
        assertEquals(5, (int)hl.convertLevelToBrightness(2));
        assertEquals(7, (int)hl.convertLevelToBrightness(3));
        assertEquals(10, (int)hl.convertLevelToBrightness(4));
        assertEquals(12, (int)hl.convertLevelToBrightness(5));
        assertEquals(15, (int)hl.convertLevelToBrightness(6));
        assertEquals(17, (int)hl.convertLevelToBrightness(7));
        assertEquals(20, (int)hl.convertLevelToBrightness(8));
        assertEquals(22, (int)hl.convertLevelToBrightness(9));
        assertEquals(25, (int)hl.convertLevelToBrightness(10));
        assertEquals(28, (int)hl.convertLevelToBrightness(11));
        assertEquals(30, (int)hl.convertLevelToBrightness(12));
        assertEquals(33, (int)hl.convertLevelToBrightness(13));
        assertEquals(35, (int)hl.convertLevelToBrightness(14));
        assertEquals(38, (int)hl.convertLevelToBrightness(15));
        assertEquals(40, (int)hl.convertLevelToBrightness(16));
        assertEquals(43, (int)hl.convertLevelToBrightness(17));
        assertEquals(45, (int)hl.convertLevelToBrightness(18));
        assertEquals(48, (int)hl.convertLevelToBrightness(19));
        assertEquals(51, (int)hl.convertLevelToBrightness(20));
        assertEquals(127, (int)hl.convertLevelToBrightness(50));
        assertEquals(255, (int)hl.convertLevelToBrightness(100));
    }

    @Test
    public void testOnSetVariableOn() {
        MockDeviceManager dm = new MockDeviceManager();
        HuePlugin plugin = new HuePlugin("plugin", "version", "description");
        plugin.setDeviceManager(dm);
        MockStateContext context = new MockStateContext(plugin, "host");
        HueLight light = new HueLight(plugin, "id", "model", "name", context);
        assertEquals(0, context.getSetLightStateRequests().size());
        light.onSetVariables(Collections.singletonMap("on", (Object)true));
        assertEquals(1, context.getSetLightStateRequests().size());
        LightState state = context.getSetLightStateRequests().get(0).getState();
        assertEquals(true, state.getOn());
    }

    @Test
    public void testOnSetVariableColor() {
        MockDeviceManager dm = new MockDeviceManager();
        HuePlugin plugin = new HuePlugin("plugin", "version", "description");
        plugin.setDeviceManager(dm);
        MockStateContext context = new MockStateContext(plugin, "host");
        HueLight light = new HueLight(plugin, "id", "model", "name", context);
        assertEquals(0, context.getSetLightStateRequests().size());
        light.onSetVariables(Collections.singletonMap("color", (Object)"hsb(360,100,100)"));
        assertEquals(1, context.getSetLightStateRequests().size());
        LightState state = context.getSetLightStateRequests().get(0).getState();
        assertEquals(65535, (int)state.getHue());
        assertEquals(254, (int)state.getSaturation());
        assertEquals(254, (int)state.getBrightness());
    }
}
