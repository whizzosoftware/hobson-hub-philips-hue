/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue;

import com.whizzosoftware.hobson.api.disco.DeviceAdvertisement;
import com.whizzosoftware.hobson.ssdp.SSDPPacket;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * A class that maintains a list of Hue bridges that have been discovered based on device advertisements.
 *
 * @author Dan Noguerol
 */
public class HueBridgeList {
    private final List<String> discoveredBridgeLocations = new ArrayList<>();

    public String addDeviceAdvertisement(DeviceAdvertisement advertisement) throws URISyntaxException {
        // make sure it's an SSDP advertisement
        if ("ssdp".equals(advertisement.getProtocolId())) {
            final SSDPPacket ssdp = (SSDPPacket)advertisement.getObject();
            if (ssdp != null) {
                // make sure it's a Hue bridge SSDP advertisement
                if (ssdp.getServer() != null && ssdp.getServer().contains("FreeRTOS/") && ssdp.getServer().contains("IpBridge/")) {
                    // make sure it's not a bridge we already know about
                    String host = new URI(ssdp.getLocation()).getHost();
                    if (!discoveredBridgeLocations.contains(host)) {
                        discoveredBridgeLocations.add(host);
                        return host;
                    }
                }
            }
        }
        return null;
    }

    public Collection<String> getDiscoveredBridgeLocations() {
        return discoveredBridgeLocations;
    }
}
