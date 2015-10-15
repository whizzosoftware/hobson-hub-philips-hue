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
import org.junit.Test;

import java.util.Collection;

import static org.junit.Assert.*;

public class HueBridgeListTest {
    @Test
    public void testDiscoveryAdd() throws Exception {
        HueBridgeList repo = new HueBridgeList();
        String rawData = "NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=100\r\n" +
                "LOCATION: http://192.168.0.220:80/description.xml\r\n" +
                "SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "NT: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n" +
                "USN: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n";
        DeviceAdvertisement entity = new DeviceAdvertisement.Builder(null, SSDPPacket.PROTOCOL_ID).rawData(rawData).object(SSDPPacket.createWithData(rawData)).build();
        assertEquals(0, repo.getDiscoveredBridgeLocations().size());
        repo.addDeviceAdvertisement(entity);
        Collection<String> locs = repo.getDiscoveredBridgeLocations();
        assertEquals(1, locs.size());
        String e = locs.iterator().next();
        assertEquals("192.168.0.220", e);
    }

    @Test
    public void testDiscoveryWithNoLocationHeader() throws Exception {
        HueBridgeList repo = new HueBridgeList();
        DeviceAdvertisement entity = new DeviceAdvertisement.Builder(null, SSDPPacket.PROTOCOL_ID).rawData("NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=100\r\n" +
                "SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "NT: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n" +
                "USN: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n").build();
        assertEquals(0, repo.getDiscoveredBridgeLocations().size());
        repo.addDeviceAdvertisement(entity);
        assertEquals(0, repo.getDiscoveredBridgeLocations().size());
    }

    @Test
    public void testMalformedLocationHeader() throws Exception {
        HueBridgeList repo = new HueBridgeList();
        DeviceAdvertisement entity = new DeviceAdvertisement.Builder(null, SSDPPacket.PROTOCOL_ID).rawData("NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=100\r\n" +
                "LOCATION: ht\r\n" +
                "SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "NT: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n" +
                "USN: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n").build();
        assertEquals(0, repo.getDiscoveredBridgeLocations().size());
        repo.addDeviceAdvertisement(entity);
        assertEquals(0, repo.getDiscoveredBridgeLocations().size());
    }

    @Test
    public void testIgnoreOtherData() throws Exception {
        HueBridgeList repo = new HueBridgeList();
        DeviceAdvertisement entity = new DeviceAdvertisement.Builder(null, SSDPPacket.PROTOCOL_ID).rawData("NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=90\r\n" +
                "LOCATION: http://192.168.0.13:49153/nmsDescription.xml\r\n" +
                "NT: urn:schemas-upnp-org:service:ConnectionManager:1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "SERVER: Windows2000/0.0 UPnP/1.0 PhilipsIntelSDK/1.4 DLNADOC/1.50\r\n" +
                "X-User-Agent: redsonic\r\n" +
                "USN: uuid:5AFEF00D-BABE-DADA-FA5A-00113215F871::urn:schemas-upnp-org:service:ConnectionManager:1\r\n" +
                "CONTENT-LENGTH: 0\r\n").build();
        assertEquals(0, repo.getDiscoveredBridgeLocations().size());
        repo.addDeviceAdvertisement(entity);
        assertEquals(0, repo.getDiscoveredBridgeLocations().size());
    }

    @Test
    public void testNoDuplicates() throws Exception {
        HueBridgeList repo = new HueBridgeList();
        String rawData = "NOTIFY * HTTP/1.1\r\n" +
                "HOST: 239.255.255.250:1900\r\n" +
                "CACHE-CONTROL: max-age=100\r\n" +
                "LOCATION: http://192.168.0.220:80/description.xml\r\n" +
                "SERVER: FreeRTOS/6.0.5, UPnP/1.0, IpBridge/0.1\r\n" +
                "NTS: ssdp:alive\r\n" +
                "NT: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n" +
                "USN: uuid:2f402f80-da50-11e1-9b23-00178814bbdc\r\n";
        DeviceAdvertisement entity = new DeviceAdvertisement.Builder(null, SSDPPacket.PROTOCOL_ID).rawData(rawData).object(SSDPPacket.createWithData(rawData)).build();
        assertEquals(0, repo.getDiscoveredBridgeLocations().size());
        repo.addDeviceAdvertisement(entity);
        Collection<String> locs = repo.getDiscoveredBridgeLocations();
        assertEquals(1, locs.size());
        String e = locs.iterator().next();
        assertEquals("192.168.0.220", e);
        repo.addDeviceAdvertisement(entity);
        assertEquals(1, repo.getDiscoveredBridgeLocations().size());
    }
}
