/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import com.whizzosoftware.hobson.api.plugin.PluginStatus;
import com.whizzosoftware.hobson.philipshue.HueLight;
import com.whizzosoftware.hobson.philipshue.api.dto.*;

import java.util.*;

public class MockStateContext implements StateContext {
    private String host;
    private PluginStatus status;
    private int getAllLightsRequests;
    private int createUserRequests;
    private List<GetLightAttributeAndStateRequest> getLightAttributeAndStateRequests = new ArrayList<>();
    private List<SetLightStateRequest> setLightStateRequests = new ArrayList<>();
    private Map<String,HueLight> hueLights = new HashMap<>();

    public MockStateContext(String host) {
        this.host = host;
    }

    @Override
    public String getHueDeviceString() {
        return null;
    }

    @Override
    public String getHueUserString() {
        return null;
    }

    @Override
    public String getBridgeHost() {
        return host;
    }

    @Override
    public void sendGetAllLightsRequest(GetAllLightsRequest request) {
        getAllLightsRequests++;
    }

    public int getGetAllLightRequestsCount() {
        return getAllLightsRequests;
    }

    @Override
    public void sendGetLightAttributeAndStateRequest(GetLightAttributeAndStateRequest request) {
        getLightAttributeAndStateRequests.add(request);
    }

    public List<GetLightAttributeAndStateRequest> getGetLightAttributeAndStateRequests() {
        return getLightAttributeAndStateRequests;
    }

    @Override
    public void sendCreateUserRequest(CreateUserRequest request) {
        createUserRequests++;
    }

    @Override
    public void sendSetLightStateRequest(SetLightStateRequest request) {
        setLightStateRequests.add(request);
    }

    public List<SetLightStateRequest> getSetLightStateRequests() {
        return setLightStateRequests;
    }

    public int getSendCreateUserRequestCount() {
        return createUserRequests;
    }

    @Override
    public void setPluginStatus(PluginStatus status) {
        this.status = status;
    }

    @Override
    public void onLightState(String deviceId, LightState state) {
    }

    @Override
    public void onLightStateFailure(String deviceId, Throwable t) {
    }

    @Override
    public void onAllLightStateFailure(Throwable t) {
    }

    @Override
    public void onSetVariable(String deviceId, String name, Object value) {
    }

    public PluginStatus getPluginStatus() {
        return status;
    }

    @Override
    public void createHueLight(Light light) {
        hueLights.put(light.getId(), new HueLight(null, light.getId(), "", "", this));
    }

    @Override
    public boolean hasHueLight(String deviceId) {
        return hueLights.containsKey(deviceId);
    }

    public Collection<HueLight> getHueLights() {
        return hueLights.values();
    }
}
