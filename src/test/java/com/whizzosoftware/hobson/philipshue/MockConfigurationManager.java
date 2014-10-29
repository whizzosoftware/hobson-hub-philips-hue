/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.config.manager.ConfigurationManager;
import com.whizzosoftware.hobson.api.config.manager.DeviceConfigurationListener;
import com.whizzosoftware.hobson.api.config.manager.PluginConfigurationListener;

import java.io.File;
import java.util.Dictionary;

public class MockConfigurationManager implements ConfigurationManager {
    @Override
    public Dictionary getPluginConfiguration(String s) {
        return null;
    }

    @Override
    public void setPluginConfigurationProperty(String s, String s2, Object o) {

    }

    @Override
    public Dictionary getDeviceConfiguration(String s, String s2) {
        return null;
    }

    @Override
    public File getDataFile(String pluginId, String filename) {
        return null;
    }

    @Override
    public void setDeviceConfigurationProperty(String s, String s2, String s3, Object o, boolean b) {

    }

    @Override
    public void registerForPluginConfigurationUpdates(String s, PluginConfigurationListener pluginConfigurationListener) {

    }

    @Override
    public void registerForDeviceConfigurationUpdates(String s, String s2, DeviceConfigurationListener deviceConfigurationListener) {

    }

    @Override
    public void unregisterForConfigurationUpdates(String s, PluginConfigurationListener pluginConfigurationListener) {

    }
}
