/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.api;

import com.whizzosoftware.hobson.api.plugin.http.Cookie;
import com.whizzosoftware.hobson.api.plugin.http.HttpRequest;

import java.net.URI;
import java.util.Collection;
import java.util.Map;

/**
 * An interface used by HueBridge objects to send HTTP requests to the Hue hub.
 *
 * @author Dan Noguerol
 */
public interface HttpContext {
    void sendHttpRequest(URI uri, HttpRequest.Method method, Map<String, String> headers, Collection<Cookie> cookies, byte[] body, Object context);
}
