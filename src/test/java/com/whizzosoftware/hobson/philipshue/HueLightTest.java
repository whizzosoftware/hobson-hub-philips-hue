/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.util.UserUtil;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.HobsonVariableImpl;
import com.whizzosoftware.hobson.api.variable.VariableConstants;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.philipshue.api.HueException;
import com.whizzosoftware.hobson.philipshue.api.LightState;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import static org.junit.Assert.*;

public class HueLightTest {
    @Test
    public void testNullUpdateDetection() throws Exception {
        MockVariableManager varProvider = new MockVariableManager();
        HuePlugin driver = new HuePlugin("id");
        driver.setVariableManager(varProvider);
        MockHueNetworkDelegate delegate = new MockHueNetworkDelegate();
        delegate.setLightState("1", new LightState(null, null, null, null, null, null, null, null));
        HueLight light = new HueLight(driver, "1", null, "Hue Light 1", delegate);
        light.onStartup();
        assertEquals(0, varProvider.getVariableUpdates().size());
        light.refresh();
        assertEquals(0, varProvider.getVariableUpdates().size());
    }

    @Test
    public void testUpdateDetection() throws Exception {
        MockVariableManager varManager = new MockVariableManager();
        HuePlugin driver = new HuePlugin("id");
        driver.setVariableManager(varManager);
        MockHueNetworkDelegate delegate = new MockHueNetworkDelegate();

        // start with one light in a known state
        delegate.setLightState("1", new LightState(true, null, 255, 0, 0, null, null, true));

        // check that there is no variable updates prior to the refresh() call
        HueLight light = new HueLight(driver, "1", null, "Hue Light 1", delegate);
        light.onStartup();
        assertEquals(0, varManager.getVariableUpdates().size());
        HobsonVariable var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertNotNull(var);
        assertNull(var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertNotNull(var);
        assertNull(var.getValue());

        // refresh() should apply the current delegate light state resulting in two variable updates
        light.refresh();
        assertEquals(2, varManager.getVariableUpdates().size());
        VariableUpdate v = varManager.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, v.getName());
        assertEquals(true, v.getValue());
        v = varManager.getVariableUpdates().get(1);
        assertEquals(VariableConstants.COLOR, v.getName());
        assertEquals("rgb(255,0,0)", v.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertEquals(true, var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertEquals("rgb(255,0,0)", var.getValue());

        varManager.clearVariableUpdates();

        // set the device variable and check that there are no updates detected after refresh()
        HobsonVariableImpl mdv = (HobsonVariableImpl)varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, driver.getId(), "1", VariableConstants.COLOR);
        mdv.setValue("rgb(255,0,0)");
        mdv = (HobsonVariableImpl)varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, driver.getId(), "1", VariableConstants.ON);
        mdv.setValue(true);
        light.refresh();
        assertEquals(0, varManager.getVariableUpdates().size());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertEquals(true, var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertEquals("rgb(255,0,0)", var.getValue());

        // set the light state again to same values and verify still no updates detected
        delegate.setLightState("1", new LightState(true, null, 255, 0, 0, null, null, true));
        light.refresh();
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertEquals(true, var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertEquals("rgb(255,0,0)", var.getValue());

        // set the light state to new color value and verify one update detected
        delegate.setLightState("1", new LightState(true, null, 0, 255, 0, null, null, true));
        light.refresh();
        assertEquals(1, varManager.getVariableUpdates().size());
        v = varManager.getVariableUpdates().get(0);
        assertEquals(VariableConstants.COLOR, v.getName());
        assertEquals("rgb(0,255,0)", v.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertEquals(true, var.getValue());
        var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.COLOR);
        assertEquals("rgb(0,255,0)", var.getValue());
    }

    @Test
    public void testKnownLightStateToUnknown() throws HueException {
        MockVariableManager varManager = new MockVariableManager();
        HuePlugin driver = new HuePlugin("id");
        driver.setVariableManager(varManager);
        MockHueNetworkDelegate delegate = new MockHueNetworkDelegate();
        LightState state = new LightState(true, null, null, null, null, null, null, true);
        delegate.setLightState("1", state);

        // check that there is no update prior to the refresh() call
        HueLight light = new HueLight(driver, "1", null, "Hue Light 1", delegate);
        light.onStartup();
        assertEquals(0, varManager.getVariableUpdates().size());
        HobsonVariable var = varManager.getDeviceVariable(UserUtil.DEFAULT_USER, UserUtil.DEFAULT_HUB, light.getPluginId(), light.getId(), VariableConstants.ON);
        assertNotNull(var);
        assertNull(var.getValue());

        // we expect one updates after refresh() since the device's variables are still null
        light.refresh();
        assertEquals(1, varManager.getVariableUpdates().size());
        VariableUpdate v = varManager.getVariableUpdates().get(0);
        assertEquals(VariableConstants.ON, v.getName());
        assertEquals(true, v.getValue());

        varManager.clearVariableUpdates();

        // set light state to unreachable
        state.setReachable(false);
        light.refresh();
        assertEquals(1, varManager.getVariableUpdates().size());
        v = varManager.getVariableUpdates().get(0);
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
        MockHueNetworkDelegate delegate = new MockHueNetworkDelegate();
        HueLight light = new HueLight(null, "id", "model", "name", delegate);
        assertEquals(0, delegate.stateMap.size());
        light.onSetVariable("on", true);
        assertEquals(1, delegate.stateMap.size());
        LightState state = delegate.stateMap.get("id");
        assertEquals(true, state.getOn());
    }

    @Test
    public void testOnSetVariableColor() {
        MockHueNetworkDelegate delegate = new MockHueNetworkDelegate();
        HueLight light = new HueLight(null, "id", "model", "name", delegate);
        assertEquals(0, delegate.stateMap.size());
        light.onSetVariable("color", "rgb(255,255,255)");
        assertEquals(1, delegate.stateMap.size());
        LightState state = delegate.stateMap.get("id");
        assertEquals(0.31, state.getX(), 0.1);
        assertEquals(0.32, state.getY(), 0.1);
    }

    @Test
    public void testOnSetVariableLevel() {
        MockHueNetworkDelegate delegate = new MockHueNetworkDelegate();
        HueLight light = new HueLight(null, "id", "model", "name", delegate);
        assertEquals(0, delegate.stateMap.size());
        light.onSetVariable("level", 50);
        assertEquals(1, delegate.stateMap.size());
        LightState state = delegate.stateMap.get("id");
        assertEquals(127, (long)state.getBrightness());
    }

    public class MockHueNetworkDelegate implements HueNetworkDelegate {
        public Map<String,LightState> stateMap = new HashMap<String,LightState>();

        @Override
        public void setLightState(String id, LightState lightState) throws HueException {
            stateMap.put(id, lightState);
        }

        @Override
        public LightState getLightState(String id) throws HueException {
            return stateMap.get(id);
        }
    }
}
