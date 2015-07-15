/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.variable.*;
import com.whizzosoftware.hobson.philipshue.api.HueException;
import com.whizzosoftware.hobson.philipshue.api.dto.Light;
import com.whizzosoftware.hobson.philipshue.api.dto.LightState;
import com.whizzosoftware.hobson.philipshue.state.MockStateContext;

import org.junit.Test;
import static org.junit.Assert.*;

public class HueLightTest {
    @Test
    public void testNullUpdateDetection() throws Exception {
        MockVariableManager vm = new MockVariableManager();
        HuePlugin plugin = new HuePlugin("id");
        plugin.setVariableManager(vm);
        MockStateContext context = new MockStateContext(plugin, "host");
        HueLight light = new HueLight(plugin, "1", "model", "Hue Light 1", context);
        light.onStartup(null);
        assertEquals(0, vm.getVariableUpdates().size());
        light.refresh();
        assertEquals(0, vm.getVariableUpdates().size());
    }

    @Test
    public void testUpdateDetection() throws Exception {
        MockVariableManager vm = new MockVariableManager();
        HuePlugin plugin = new HuePlugin("id");
        plugin.setVariableManager(vm);
        MockStateContext context = new MockStateContext(plugin, "host");

        LightState newState = new LightState(true, null, 255, 0, 0, null, null, true);

        // start with one light in a known state
        context.createHueLight(new Light("1", "Hue Light 1", "model", new LightState(true, null, 255, 0, 0, null, null, true)));

        // check that there is no variable updates prior to the refresh() call
        HueLight light = new HueLight(plugin, "1", null, "Hue Light 1", context);
        light.onStartup(null);
        assertEquals(0, vm.getVariableUpdates().size());
        HobsonVariable var = vm.getDeviceVariable(light.getContext(), VariableConstants.ON);
        assertNotNull(var);
        assertNull(var.getValue());
        var = vm.getDeviceVariable(light.getContext(), VariableConstants.COLOR);
        assertNotNull(var);
        assertNull(var.getValue());

        // onLightState() should apply the current delegate light state resulting in two variable updates
        light.onLightState(newState);
        assertEquals(2, vm.getVariableUpdates().size());
        VariableUpdate v = vm.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, v.getName());
        assertEquals(true, v.getValue());
        v = vm.getVariableUpdates().get(1);
        assertEquals(VariableConstants.COLOR, v.getName());
        assertEquals("rgb(255,0,0)", v.getValue());

        vm.clearVariableUpdates();

        // set the device variable and check that there are no updates detected after refresh()
        MockHobsonVariable mdv = (MockHobsonVariable)vm.getDeviceVariable(DeviceContext.create(plugin.getContext(), "1"), VariableConstants.COLOR);
        mdv.setValue("rgb(255,0,0)");
        mdv = (MockHobsonVariable)vm.getDeviceVariable(DeviceContext.create(plugin.getContext(), "1"), VariableConstants.ON);
        mdv.setValue(true);
        light.onLightState(newState);
        assertEquals(0, vm.getVariableUpdates().size());

        // set the light state again to same values and verify still no updates detected
        light.onLightState(newState);
        assertEquals(0, vm.getVariableUpdates().size());

        // set the light state to new color value and verify one update detected
        light.onLightState(new LightState(true, null, 0, 255, 0, null, null, true));
        light.refresh();
        assertEquals(1, vm.getVariableUpdates().size());
        v = vm.getVariableUpdates().get(0);
        assertEquals(VariableConstants.COLOR, v.getName());
        assertEquals("rgb(0,255,0)", v.getValue());
    }

    @Test
    public void testKnownLightStateToUnknown() throws HueException {
        MockVariableManager vm = new MockVariableManager();
        HuePlugin driver = new HuePlugin("id");
        driver.setVariableManager(vm);
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext context = new MockStateContext(plugin, "host");
        LightState state = new LightState(true, null, null, null, null, null, null, true);

        // check that there is no update prior to the refresh() call
        HueLight light = new HueLight(driver, "1", null, "Hue Light 1", context);
        light.onStartup(null);
        assertEquals(0, vm.getVariableUpdates().size());
        HobsonVariable var = vm.getDeviceVariable(light.getContext(), VariableConstants.ON);
        assertNotNull(var);
        assertNull(var.getValue());

        // we expect one updates after refresh() since the device's variables are still null
        light.onLightState(state);
        assertEquals(1, vm.getVariableUpdates().size());
        VariableUpdate v = vm.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, v.getName());
        assertEquals(true, v.getValue());

        vm.clearVariableUpdates();

        // set light state to unreachable
        state.setReachable(false);
        light.onLightState(state);
        assertEquals(0, vm.getVariableUpdates().size());
    }

    @Test
    public void testConvertLightStateToColor() {
        HuePlugin plugin = new HuePlugin("plugin");
        HueLight light = new HueLight(plugin, null, null, null, null);
        assertNull(light.convertLightStateToColor(null));
        assertNull(light.convertLightStateToColor(new LightState(null, null, null, null, null, null, null, null)));
        assertEquals("rgb(255,0,0)", light.convertLightStateToColor(new LightState(null, null, 255, 0, 0, null, null, null)));
    }

    @Test
    public void testConvertBrightnessToLevel() {
        HuePlugin plugin = new HuePlugin("plugin");
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
        HuePlugin plugin = new HuePlugin("plugin");
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
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext context = new MockStateContext(plugin, "host");
        HueLight light = new HueLight(plugin, "id", "model", "name", context);
        assertEquals(0, context.getSetLightStateRequests().size());
        light.onSetVariable("on", true);
        assertEquals(1, context.getSetLightStateRequests().size());
        LightState state = context.getSetLightStateRequests().get(0).getState();
        assertEquals(true, state.getOn());
    }

    @Test
    public void testOnSetVariableColor() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext context = new MockStateContext(plugin, "host");
        HueLight light = new HueLight(plugin, "id", "model", "name", context);
        assertEquals(0, context.getSetLightStateRequests().size());
        light.onSetVariable("color", "rgb(255,255,255)");
        assertEquals(1, context.getSetLightStateRequests().size());
        LightState state = context.getSetLightStateRequests().get(0).getState();
        assertEquals(0.31, state.getX(), 0.1);
        assertEquals(0.32, state.getY(), 0.1);
    }

    @Test
    public void testOnSetVariableLevel() {
        HuePlugin plugin = new HuePlugin("plugin");
        MockStateContext context = new MockStateContext(plugin, "host");
        HueLight light = new HueLight(plugin, "id", "model", "name", context);
        assertEquals(0, context.getSetLightStateRequests().size());
        light.onSetVariable("level", 50);
        assertEquals(1, context.getSetLightStateRequests().size());
        LightState state = context.getSetLightStateRequests().get(0).getState();
        assertEquals(127, (long)state.getBrightness());
    }
}
