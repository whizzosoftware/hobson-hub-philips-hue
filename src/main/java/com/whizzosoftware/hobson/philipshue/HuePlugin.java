/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.device.DeviceContext;
import com.whizzosoftware.hobson.api.device.HobsonDevice;
import com.whizzosoftware.hobson.api.event.DeviceAdvertisementEvent;
import com.whizzosoftware.hobson.api.event.EventTopics;
import com.whizzosoftware.hobson.api.event.HobsonEvent;
import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.api.plugin.http.AbstractHttpClientPlugin;
import com.whizzosoftware.hobson.api.plugin.http.HttpResponse;
import com.whizzosoftware.hobson.api.property.PropertyConstraintType;
import com.whizzosoftware.hobson.api.property.PropertyContainer;
import com.whizzosoftware.hobson.api.property.TypedProperty;
import com.whizzosoftware.hobson.philipshue.api.HttpContext;
import com.whizzosoftware.hobson.philipshue.api.HueBridge;
import com.whizzosoftware.hobson.philipshue.api.HueException;
import com.whizzosoftware.hobson.philipshue.api.dto.*;
import com.whizzosoftware.hobson.philipshue.state.InitializingState;
import com.whizzosoftware.hobson.philipshue.state.State;
import com.whizzosoftware.hobson.philipshue.state.StateContext;
import com.whizzosoftware.hobson.ssdp.SSDPPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * The Philips Hue plugin. This uses a REST client to communicate with the Hue bridge in order to discover
 * and control Hue bulbs.
 *
 * @author Dan Noguerol
 */
public class HuePlugin extends AbstractHttpClientPlugin implements StateContext, HttpContext {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String PROP_BRIDGE_HOST = "bridge.host";
    private static final String HUE_DEVICE = "whizzohobson";
    private static final String HUE_USER = "whizzohobson";
    private static final long DEFAULT_REFRESH_INTERVAL_IN_SECONDS = 5;

    private HueBridge bridge;
    private State state = new InitializingState();
    private final HueBridgeList bridges = new HueBridgeList();

    public HuePlugin(String pluginId) {
        super(pluginId);
    }

    // ***
    // HobsonPlugin methods
    // ***

    @Override
    public void onStartup(PropertyContainer config) {
        try {
            String bridgeHost = (String)config.getPropertyValue(PROP_BRIDGE_HOST);
            // if the bridge host is already configured, use it; otherwise, look for a device advertisement
            if (bridgeHost != null) {
                setBridgeHost(bridgeHost);
            } else {
                requestDeviceAdvertisementSnapshot(SSDPPacket.PROTOCOL_ID);
            }
        } catch (HueException e) {
            logger.error("Error starting Hue plugin", e);
            setPluginStatus(PluginStatus.failed("Error starting Hue plugin. See the log for details."));
        }
    }

    @Override
    public void onShutdown() {

    }

    @Override
    protected TypedProperty[] createSupportedProperties() {
        return new TypedProperty[] {
            new TypedProperty.Builder(PROP_BRIDGE_HOST, "Hue Bridge", "The hostname or IP address of the Philips Hue Bridge. This should be detected automatically but you can enter it manually here if necessary.", TypedProperty.Type.STRING)
                .constraint(PropertyConstraintType.required, true)
                .build()
        };
    }

    @Override
    public String getName() {
        return "Philips Hue";
    }

    @Override
    public long getRefreshInterval() {
        return DEFAULT_REFRESH_INTERVAL_IN_SECONDS;
    }

    @Override
    public String[] getEventTopics() {
        // make sure we subscribe to SSDP device advertisement events
        return new String[] { EventTopics.createDiscoTopic(SSDPPacket.PROTOCOL_ID) };
    }

    @Override
    public void onRefresh() {
        setState(state.onRefresh(this));
    }

    @Override
    public void onPluginConfigurationUpdate(PropertyContainer config) {
        String host = (String)config.getPropertyValue(PROP_BRIDGE_HOST);
        if (bridge == null || !host.equals(bridge.getHost())) {
            try {
                setBridgeHost(host);
            } catch (HueException e) {
                // TODO: fail
            }
        }
    }

    @Override
    public void onHobsonEvent(HobsonEvent event) {
        super.onHobsonEvent(event);

        if (event instanceof DeviceAdvertisementEvent) {
            DeviceAdvertisementEvent adv = (DeviceAdvertisementEvent)event;
            try {
                String host = bridges.addDeviceAdvertisement(adv.getAdvertisement());
                if (host != null) {
                    logger.info("Found Hue bridge at {}", host);
                    // TODO: make sure not to overwrite this property
                    setPluginConfigurationProperty(getContext(), "bridge.host", host);
                }
            } catch (URISyntaxException e) {
                logger.debug("Ignoring invalid Philips Hue bridge data: {}", adv.getAdvertisement().getRawData());
            }
        }
    }

    protected void setState(State state) {
        if (this.state != state) {
            logger.debug("Changing to state: " + state);
            this.state = state;
            onRefresh();
        }
    }

    protected State getState() {
        return state;
    }

    // ***
    // AbstractHttpClientPlugin methods
    // ***

    @Override
    public void onHttpResponse(HttpResponse response, Object context) {
        logger.trace("Received HTTP response");
        try {
            BridgeResponse br = bridge.parseResponse(context, response.getStatusCode(), response.getBody());
            setState(state.onBridgeResponse(this, br));
        } catch (HueException e) {
            logger.error("Error processing bridge response", e);
        } catch (IOException e) {
            logger.error("Error processing HTTP response", e);
        }
    }

    @Override
    public void onHttpRequestFailure(Throwable cause, Object context) {
        state.onBridgeRequestFailure(this, context, cause);
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
    public void onLightState(String deviceId, LightState state) {
        HueLight light = (HueLight)getDevice(DeviceContext.create(getContext(), deviceId));
        if (light != null) {
            light.onLightState(state);
        } else {
            logger.error("Received state for unknown Hue device: {}", deviceId);
        }
    }

    @Override
    public void onLightStateFailure(String deviceId, Throwable t) {
        HueLight light = (HueLight)getDevice(DeviceContext.create(getContext(), deviceId));
        if (light != null) {
            light.onLightStateFailure(t);
        } else {
            logger.error("Received state failure for unknown Hue device: {}", deviceId);
        }
    }

    @Override
    public void onAllLightStateFailure(Throwable t) {
        for (HobsonDevice hd : getAllDevices()) {
            ((HueLight)hd).onLightStateFailure(t);
        }
    }

    @Override
    public void onSetVariable(String deviceId, String name, Object value) {
        HueLight light = (HueLight)getDevice(DeviceContext.create(getContext(), deviceId));
        if (light != null) {
            light.onSetVariable(name, value);
        } else {
            logger.error("Attempt to set variable for non-existent Hue device: {}", deviceId);
        }
    }

    @Override
    public String getBridgeHost() {
        return (bridge != null) ? bridge.getHost() : null;
    }

    @Override
    public void sendCreateUserRequest(CreateUserRequest request) {
        try {
            bridge.sendCreateUserRequest(this, request);
        } catch (HueException e) {
            logger.error("Error sending CreateUser request", e);
        }
    }

    @Override
    public void sendGetAllLightsRequest(GetAllLightsRequest request) {
        try {
            bridge.sendGetAllLightsRequest(this, request);
            logger.trace("Sent GetAllLights request");
        } catch (HueException e) {
            logger.error("Error sending GetAllLights request", e);
        }
    }

    @Override
    public void sendGetLightAttributeAndStateRequest(GetLightAttributeAndStateRequest request) {
        try {
            bridge.sendGetLightAttributeAndStateRequest(this, request);
        } catch (HueException e) {
            logger.error("Error sending GetLightAttributeAndState request", e);
        }
    }

    @Override
    public void sendSetLightStateRequest(SetLightStateRequest request) {
        try {
            bridge.sendSetLightStateRequest(this, request);
        } catch (HueException e) {
            logger.error("Error sending SetLightState request", request);
        }
    }

    public void setBridgeHost(String host) throws HueException {
        if (host != null) {
            bridge = new HueBridge(host, HUE_DEVICE, HUE_USER);
            setState(state.onBridgeHostUpdate(this));
        }
    }

    @Override
    public void createHueLight(Light light) {
        HueLight hlight = new HueLight(this, light.getId(), light.getModel(), light.getName(), this, light);
        hlight.setDeviceAvailability(false, null);
        publishDevice(hlight);
        logger.debug("Added Hue light {} as {}", light.getId(), hlight.getContext());
    }

    @Override
    public boolean hasHueLight(String deviceId) {
        return hasDevice(DeviceContext.create(getContext(), deviceId));
    }
}
