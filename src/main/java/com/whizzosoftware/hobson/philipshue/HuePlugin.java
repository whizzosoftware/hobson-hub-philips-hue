/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.disco.DeviceBridge;
import com.whizzosoftware.hobson.api.plugin.AbstractHobsonPlugin;
import com.whizzosoftware.hobson.bootstrap.api.config.ConfigurationMetaData;
import com.whizzosoftware.hobson.bootstrap.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.disco.HueBridgeDetector;
import com.whizzosoftware.hobson.philipshue.disco.HueBridgeListener;
import com.whizzosoftware.hobson.philipshue.api.*;
import com.whizzosoftware.hobson.philipshue.state.InitializingState;
import com.whizzosoftware.hobson.philipshue.state.State;
import com.whizzosoftware.hobson.philipshue.state.StateContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Dictionary;

/**
 * The Philips Hue plugin. This uses a REST client to communicate with the Hue bridge in order to discover
 * and control Hue bulbs.
 *
 * @author Dan Noguerol
 */
public class HuePlugin extends AbstractHobsonPlugin implements StateContext, HueNetworkDelegate, HueBridgeListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String HUE_DEVICE = "whizzohobson";
    private static final String HUE_USER = "whizzohobson";
    private static final long DEFAULT_REFRESH_INTERVAL_IN_SECONDS = 5;

    private long refreshIntervalInSeconds;
    private HueBridgeDetector metaAnalyzer;
    private String bridgeHost;
    private HueBridge channel;
    private State state = new InitializingState();

    public HuePlugin(String pluginId) {
        this(pluginId, DEFAULT_REFRESH_INTERVAL_IN_SECONDS);

        addConfigurationMetaData(new ConfigurationMetaData("bridge.host", "Hue Bridge", "${philipsHueBridge}The hostname or IP address of the Philips Hue Bridge", ConfigurationMetaData.Type.STRING));
    }

    public HuePlugin(String pluginId, long refreshIntervalInSeconds) {
        super(pluginId);
        this.refreshIntervalInSeconds = refreshIntervalInSeconds;
    }

    // ***
    // HobsonPlugin methods
    // ***

    @Override
    public void onStartup(Dictionary config) {
        setBridgeHost((String)config.get("bridge.host"));

        // publish an analyzer that can detect Hue bridges via SSDP
        metaAnalyzer = new HueBridgeDetector();
        metaAnalyzer.setListener(this);
        publishDeviceBridgeDetector(metaAnalyzer);
    }

    @Override
    public void onShutdown() {
    }

    @Override
    public String getName() {
        return "Philips Hue";
    }

    @Override
    public long getRefreshInterval() {
        return refreshIntervalInSeconds;
    }

    protected void setState(State state) {
        if (this.state != state) {
            logger.debug("Changing to state: " + state);
            this.state = state;
            onRefresh();
        }
    }

    @Override
    public void onRefresh() {
        setState(state.onLoop(this));
    }

    @Override
    public void onPluginConfigurationUpdate(Dictionary config) {
        String bridgeHost = (String)config.get("bridge.host");
        if (bridgeHost == null || !bridgeHost.equals(this.bridgeHost)) {
            this.bridgeHost = bridgeHost;
            setState(state.onBridgeHostUpdate(this));
        }
    }

    @Override
    public void onSetDeviceVariable(String deviceId, String variableName, Object value) {
        setState(state.onSetVariable(this, deviceId, variableName, value));
    }

    // ***
    // HueBridgeMetaListener methods
    // ***

    @Override
    public void onHueHubFound(DeviceBridge bridge) {
        String newBridgeHost = bridge.getValue();
        if (newBridgeHost != null && bridgeHost == null) {
            setPluginConfigurationProperty(getId(), "bridge.host", bridge.getValue());
        }
    }

    // ***
    // HueNetworkDelegate methods
    // ***

    @Override
    public void setLightState(String id, LightState lightState) throws HueException {
        channel.setLightState(id, lightState);
    }

    @Override
    public LightState getLightState(String id) throws HueException {
        return channel.getLightAttributeAndState(id);
    }

    // ***
    // StateContext methods
    // ***

    @Override
    public String getHueDeviceString() {
        return HUE_DEVICE;
    }

    @Override
    public String getHueUserString() {
        return HUE_USER;
    }

    @Override
    public void setPluginStatus(PluginStatus status) {
        setStatus(status);
    }

    @Override
    public String getBridgeHost() {
        return bridgeHost;
    }

    public void setBridgeHost(String bridgeHost) {
        this.bridgeHost = bridgeHost;
    }

    @Override
    public HueBridge getHueChannel() {
        return channel;
    }

    @Override
    public void setHueChannel(HueBridge channel) {
        this.channel = channel;
    }

    @Override
    public void closeHueChannel() {
        unpublishAllDevices();
        channel = null;
    }

    @Override
    public void createHueLight(Light light) {
        HueLight hlight = new HueLight(this, light.getId(), light.getModel(), light.getName(), this);
        publishDevice(hlight);
        logger.debug("Added Hue light {} as {}", light.getId(), hlight.getId());
    }

    @Override
    public boolean hasHueLight(String deviceId) {
        return hasDevice(deviceId);
    }

    @Override
    public HueLight getHueLight(String deviceId) {
        return (HueLight)getDevice(deviceId);
    }

    @Override
    public void refreshAllLights() {
        executeInEventLoop(new Runnable() {
            @Override
            public void run() {
                for (HobsonDevice device : getAllDevices()) {
                    if (device instanceof HueLight) {
                        ((HueLight)device).refresh();
                    }
                }
            }
        });
    }
}
