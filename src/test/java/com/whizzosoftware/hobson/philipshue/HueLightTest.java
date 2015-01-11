/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.util.UserUtil;
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
        MockVariablePublisher vp = new MockVariablePublisher();
        MockVariableManager varProvider = new MockVariableManager(vp);
        HuePlugin plugin = new HuePlugin("id");
        plugin.setVariableManager(varProvider);
        MockStateContext context = new MockStateContext("host");
        HueLight light = new HueLight(plugin, "1", "model", "Hue Light 1", context);
        light.onStartup();
        assertEquals(0, vp.getVariableUpdates().size());
        light.refresh();
        assertEquals(0, vp.getVariableUpdates().size());
    }

    @Test
    public void testUpdateDetection() throws Exception {
        MockVariablePublisher vp = new MockVariablePublisher();
        MockVariableManager varManager = new MockVariableManager(vp);
        HuePlugin plugin = new HuePlugin("id");
        plugin.setVariableManager(varManager);
        MockStateContext context = new MockStateContext("host");

        LightState newState = new LightState(true, null, 255, 0, 0, null, null, true);

        // start with one light in a known state
        context.createHueLight(new Light("1", "Hue Light 1", "model", new LightState(true, null, 255, 0, 0, null, null, true)));

        // check that there is no variable updates prior to the refresh() call
        HueLight light = new HueLight(plugin, "1", null, "Hue Light 1", context);
        light.onStartup();
        assertEquals(0, vp.getVariableUpdates().size());
        HobsonVariable var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertNotNull(var);
        assertNull(var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertNotNull(var);
        assertNull(var.getValue());

        // onLightState() should apply the current delegate light state resulting in two variable updates
        light.onLightState(newState);
        assertEquals(2, vp.getVariableUpdates().size());
        VariableUpdate v = vp.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, v.getName());
        assertEquals(true, v.getValue());
        v = vp.getVariableUpdates().get(1);
        assertEquals(VariableConstants.COLOR, v.getName());
        assertEquals("rgb(255,0,0)", v.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertEquals(true, var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertEquals("rgb(255,0,0)", var.getValue());

        vp.clearVariableUpdates();

        // set the device variable and check that there are no updates detected after refresh()
        MockHobsonVariable mdv = (MockHobsonVariable)varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, plugin.getId(), "1", VariableConstants.COLOR);
        mdv.setValue("rgb(255,0,0)");
        mdv = (MockHobsonVariable)varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, plugin.getId(), "1", VariableConstants.ON);
        mdv.setValue(true);
        light.onLightState(newState);
        assertEquals(0, vp.getVariableUpdates().size());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertEquals(true, var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertEquals("rgb(255,0,0)", var.getValue());

        // set the light state again to same values and verify still no updates detected
        light.onLightState(newState);
        assertEquals(0, vp.getVariableUpdates().size());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertEquals(true, var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertEquals("rgb(255,0,0)", var.getValue());

        // set the light state to new color value and verify one update detected
        light.onLightState(new LightState(true, null, 0, 255, 0, null, null, true));
        light.refresh();
        assertEquals(1, vp.getVariableUpdates().size());
        v = vp.getVariableUpdates().get(0);
        assertEquals(VariableConstants.COLOR, v.getName());
        assertEquals("rgb(0,255,0)", v.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertEquals(true, var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertEquals("rgb(0,255,0)", var.getValue());
    }

    @Test
    public void testKnownLightStateToUnknown() throws HueException {
        MockVariablePublisher vp = new MockVariablePublisher();
        MockVariableManager varManager = new MockVariableManager(vp);
        HuePlugin driver = new HuePlugin("id");
        driver.setVariableManager(varManager);
        MockStateContext context = new MockStateContext("host");
        LightState state = new LightState(true, null, null, null, null, null, null, true);

        // check that there is no update prior to the refresh() call
        HueLight light = new HueLight(driver, "1", null, "Hue Light 1", context);
        light.onStartup();
        assertEquals(0, vp.getVariableUpdates().size());
        HobsonVariable var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertNotNull(var);
        assertNull(var.getValue());

        // we expect one updates after refresh() since the device's variables are still null
        light.onLightState(state);
        assertEquals(1, vp.getVariableUpdates().size());
        VariableUpdate v = vp.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, v.getName());
        assertEquals(true, v.getValue());

        vp.clearVariableUpdates();

        // set light state to unreachable
        state.setReachable(false);
        light.onLightState(state);
        assertEquals(1, vp.getVariableUpdates().size());
        v = vp.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, v.getName());
        assertNull(v.getValue());
    }

    @Test
    public void testConvertLightStateToColor() {
        HueLight light = new HueLight(null, null, null, null, null);
        assertNull(light.convertLightStateToColor(null));
        assertNull(light.convertLightStateToColor(new LightState(null, null, null, null, null, null, null, null)));
        assertEquals("rgb(255,0,0)", light.convertLightStateToColor(new LightState(null, null, 255, 0, 0, null, null, null)));
    }

    @Test
    public void testConvertBrightnessToLevel() {
        HueLight hl = new HueLight(null, null, null, null, null);
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
        HueLight hl = new HueLight(null, null, null, null, null);
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
        MockStateContext context = new MockStateContext("host");
        HueLight light = new HueLight(null, "id", "model", "name", context);
        assertEquals(0, context.getSetLightStateRequests().size());
        light.onSetVariable("on", true);
        assertEquals(1, context.getSetLightStateRequests().size());
        LightState state = context.getSetLightStateRequests().get(0).getState();
        assertEquals(true, state.getOn());
    }

    @Test
    public void testOnSetVariableColor() {
        MockStateContext context = new MockStateContext("host");
        HueLight light = new HueLight(null, "id", "model", "name", context);
        assertEquals(0, context.getSetLightStateRequests().size());
        light.onSetVariable("color", "rgb(255,255,255)");
        assertEquals(1, context.getSetLightStateRequests().size());
        LightState state = context.getSetLightStateRequests().get(0).getState();
        assertEquals(0.31, state.getX(), 0.1);
        assertEquals(0.32, state.getY(), 0.1);
    }

    @Test
    public void testOnSetVariableLevel() {
        MockStateContext context = new MockStateContext("host");
        HueLight light = new HueLight(null, "id", "model", "name", context);
        assertEquals(0, context.getSetLightStateRequests().size());
        light.onSetVariable("level", 50);
        assertEquals(1, context.getSetLightStateRequests().size());
        LightState state = context.getSetLightStateRequests().get(0).getState();
        assertEquals(127, (long)state.getBrightness());
    }
}
