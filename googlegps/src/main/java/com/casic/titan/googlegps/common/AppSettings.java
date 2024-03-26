/*
 * Copyright (C) 2016 mendhak
 *
 * This file is part of GPSLogger for Android.
 *
 * GPSLogger for Android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * GPSLogger for Android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GPSLogger for Android.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.casic.titan.googlegps.common;

import android.app.Application;

import com.casic.titan.googlegps.common.slf4j.Logs;

import org.slf4j.Logger;

public class AppSettings {

    private static AppSettings instance;
    private static Logger LOG;
    private Application application;

    public void onCreate(Application application) {
        this.application = application;
        Logs.configure();
        LOG = Logs.of(this.getClass());
        LOG.debug("SLF4J logging configured");

        LOG.debug("EventBus configured");
    }


    public Application getApplication() {
        return application;
    }

    /**
     * Returns a singleton instance of this class
     */
    public static AppSettings getInstance() {
        if (instance == null) {
            instance = new AppSettings();
        }
        return instance;
    }


}
