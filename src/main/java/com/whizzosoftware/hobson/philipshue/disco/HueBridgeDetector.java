/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.disco;

import com.whizzosoftware.hobson.api.disco.DeviceBridge;
import com.whizzosoftware.hobson.api.disco.DeviceBridgeDetectionContext;
import com.whizzosoftware.hobson.api.disco.DeviceBridgeDetector;
import com.whizzosoftware.hobson.api.disco.DeviceBridgeMetaData;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * An DeviceBridgeDetector implementation that can detect Philips Hue bridges via SSDP.
 *
 * @author Dan Noguerol
 */
public class HueBridgeDetector implements DeviceBridgeDetector {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String ID = "philipsHueBridge";

    private String pluginId;
    private final List<String> discoveredAddresses = new ArrayList<String>();
    private HueBridgeListener listener;

    public HueBridgeDetector() {
        Bundle bundle = FrameworkUtil.getBundle(getClass());
        if (bundle != null) {
            setPluginId(bundle.getSymbolicName());
        }
    }

    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    public void setListener(HueBridgeListener listener) {
        this.listener = listener;
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getPluginId() {
        return pluginId;
    }

    @Override
    public boolean identify(DeviceBridgeDetectionContext context, DeviceBridgeMetaData unknown) {
        String data = unknown.getData();
        if (unknown.getScannerId().equals("ssdp") && data.startsWith("NOTIFY *") && data.contains("FreeRTOS/") && data.contains("IpBridge/")) {
            int ix1 = data.indexOf("LOCATION: ");
            if (ix1 > -1) {
                ix1 += 10;
                int ix2 = data.indexOf("\n", ix1);
                URI uri = URI.create(data.substring(ix1, ix2).trim());
                String address = uri.getHost();
                if (address != null) {
                    boolean isNew = false;
                    synchronized (discoveredAddresses) {
                        if (!discoveredAddresses.contains(address)) {
                            discoveredAddresses.add(address);
                            isNew = true;
                        }
                    }
                    if (isNew) {
                        logger.info("Found Hue bridge at {}", address);
                        DeviceBridge bridge = new DeviceBridge(getPluginId(), getId(), "Philips Hue Bridge [" + address + "]", address);
                        context.addDeviceBridge(bridge);
                        if (listener != null) {
                            listener.onHueHubFound(bridge);
                        }
                    }
                }
                return true;
            } else {
                logger.debug("Found possible Hue bridge but no LOCATION header found");
            }
        }
        return false;
    }
}
