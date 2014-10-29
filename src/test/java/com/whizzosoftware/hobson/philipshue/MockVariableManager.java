/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.plugin.HobsonPlugin;
import com.whizzosoftware.hobson.api.variable.HobsonVariable;
import com.whizzosoftware.hobson.api.variable.HobsonVariableImpl;
import com.whizzosoftware.hobson.api.variable.VariableUpdate;
import com.whizzosoftware.hobson.api.variable.manager.VariableManager;

import java.util.*;

public class MockVariableManager implements VariableManager {
    private Map<String,PluginVariables> driverMap = new HashMap<String,PluginVariables>();
    private List<VariableUpdate> updates = new ArrayList<VariableUpdate>();

    @Override
    public void publishGlobalVariable(String pluginId, HobsonVariable var) {

    }

    @Override
    public void publishDeviceVariable(String pluginId, String deviceId, HobsonVariable var) {
        PluginVariables dv = driverMap.get(pluginId);
        if (dv == null) {
            dv = new PluginVariables();
            driverMap.put(pluginId, dv);
        }
        dv.addVariable(deviceId, var);
    }

    @Override
    public void unpublishGlobalVariable(String pluginId, String name) {

    }

    @Override
    public void unpublishAllPluginVariables(String pluginId) {
        driverMap.remove(pluginId);
    }

    @Override
    public void unpublishDeviceVariable(String pluginId, String deviceId, String name) {

    }

    @Override
    public void unpublishAllDeviceVariables(String pluginId, String deviceId) {

    }

    @Override
    public Collection<HobsonVariable> getGlobalVariables() {
        return null;
    }

    @Override
    public Collection<HobsonVariable> getDeviceVariables(String pluginId, String deviceId) {
        return null;
    }

    @Override
    public HobsonVariable getDeviceVariable(String pluginId, String deviceId, String name) {
        PluginVariables dv = driverMap.get(pluginId);
        if (dv != null) {
            return dv.getVariable(deviceId, name);
        }
        return null;
    }

    @Override
    public boolean hasDeviceVariable(String pluginId, String deviceId, String name) {
        return false;
    }

    @Override
    public Long setDeviceVariable(String pluginId, String deviceId, String name, Object value) {
        return null;
    }

    @Override
    public void fireVariableUpdateNotification(HobsonPlugin plugin, VariableUpdate update) {
        PluginVariables dv = driverMap.get(update.getPluginId());
        if (dv != null) {
            dv.updateVariable(update);
            updates.add(update);
        }
    }

    @Override
    public void fireVariableUpdateNotifications(HobsonPlugin plugin, List<VariableUpdate> updates) {
        for (VariableUpdate update : updates) {
            fireVariableUpdateNotification(plugin, update);
        }
    }

    public List<VariableUpdate> getVariableUpdates() {
        return updates;
    }

    public void clearVariableUpdates() {
        updates.clear();
    }

    private class PluginVariables {
        private Map<String,DeviceVariables> deviceMap = new HashMap<String,DeviceVariables>();

        public void addVariable(String deviceId, HobsonVariable v) {
            DeviceVariables dv = deviceMap.get(deviceId);
            if (dv == null) {
                dv = new DeviceVariables();
                deviceMap.put(deviceId, dv);
            }
            dv.addVariable(v);
        }

        public HobsonVariable getVariable(String deviceId, String name) {
            DeviceVariables dv = deviceMap.get(deviceId);
            if (dv != null) {
                return dv.getVariable(name);
            }
            return null;
        }

        public void updateVariable(VariableUpdate update) {
            DeviceVariables dv = deviceMap.get(update.getDeviceId());
            if (dv != null) {
                dv.updateVariable(update);
            }
        }
    }

    private class DeviceVariables {
        private Map<String,HobsonVariable> variableMap = new HashMap<String,HobsonVariable>();

        public void addVariable(HobsonVariable v) {
            variableMap.put(v.getName(), v);
        }

        public HobsonVariable getVariable(String name) {
            return variableMap.get(name);
        }

        public void updateVariable(VariableUpdate update) {
            HobsonVariableImpl dv = (HobsonVariableImpl)variableMap.get(update.getName());
            if (dv != null) {
                dv.setValue(update.getValue());
            }
        }
    }
}
