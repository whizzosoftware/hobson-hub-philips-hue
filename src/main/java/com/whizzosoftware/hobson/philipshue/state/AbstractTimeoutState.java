/*******************************************************************************
 * Copyright (c) 2014 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.hobson.philipshue.state;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A base class for states that send out a request that can timeout as part of their onRefresh() method. A
 * facility is provided to retry a certain number of times when timeouts occur and finally change to the
 * failed state if no response is ever received.
 *
 * @author Dan Noguerol
 */
abstract public class AbstractTimeoutState implements State {
    private final static Logger logger = LoggerFactory.getLogger(AbstractTimeoutState.class);

    private long lastRequestTime;
    private int retryCount;

    @Override
    public State onRefresh(StateContext ctx) {
        return onRefresh(ctx, System.currentTimeMillis());
    }

    public State onRefresh(StateContext ctx, long now) {
        // make a request if it's the first time or the timeout interval has passed
        if (lastRequestTime == 0 || now - lastRequestTime >= getTimeout()) {
            if (retryCount <= getMaximumRetryCount()) {
                if (retryCount > 0) {
                    logger.debug("Hue bridge timeout occurred; will retry...");
                }
                performRequest(ctx);
                this.lastRequestTime = now;
                retryCount++;
            } else {
                return new FailedState();
            }
        }

        return this;
    }

    /**
     * Performs the actual request.
     *
     * @param ctx the state context
     */
    abstract protected void performRequest(StateContext ctx);

    /**
     * Returns the timeout interval.
     *
     * @return a time interval in milliseconds
     */
    abstract protected long getTimeout();

    /**
     * Returns the maximum number of times a request will be retried.
     *
     * @return the retry count
     */
    abstract protected int getMaximumRetryCount();
}
