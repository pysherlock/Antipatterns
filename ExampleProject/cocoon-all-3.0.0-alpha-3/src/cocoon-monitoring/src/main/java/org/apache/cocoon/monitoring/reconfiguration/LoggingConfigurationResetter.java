/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.monitoring.reconfiguration;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * This class starts a new thread that restores the logging level for a particular class or package
 * (after a defined amount of time).
 */
public class LoggingConfigurationResetter {

    private final long delay;
    private final Logger logger;
    private final Level oldLogLevel;

    /**
     * @param logger instance of logger that logging level should changed
     * @param oldLevel logging level value that should be set after timeout
     * @param timeout value of timeout. Is should match regular expression: ^[0-9.]+[dhm]?$
     *          where <code>d</code> means day, <code>h</code> hours and <code>m</code>
     *          minutes
     */
    public LoggingConfigurationResetter(final Logger logger, Level oldLevel, String timeout) {

        this.logger = logger;
        this.oldLogLevel = oldLevel;

        long factor;
        char unit = timeout.charAt(timeout.length() - 1); // get last char, it should be our unit
        switch (unit) {
        case 's': // second
            factor = 1 * 1000;
            break;
        case 'm': // minute
            factor = 60 * 1000;
            break;
        case 'h': // hour
            factor = 60 * 60 * 1000;
            break;
        case 'd': // day
            factor = 24 * 60 * 60 * 1000;
            break;
        default:
            String message = "Unsupported unit: " + unit;
            throw new UnsupportedOperationException(message);
        }

        float multipler = Float.parseFloat(timeout.substring(0, timeout.length() - 1));
        this.delay = Math.round(multipler * factor);
    }

    /**
     * Starts thread
     */
    public void start() {
        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                LoggingConfigurationResetter.this.logger.setLevel(LoggingConfigurationResetter.this.oldLogLevel);
            }

            @Override
            public boolean cancel() {
                this.run(); // set old level on task cancel
                return super.cancel();
            }
        };

        Timer timer = new Timer("Restore " + this.logger.getName() + " to level" + this.oldLogLevel, true);
        timer.schedule(task, this.delay);
    }
}
