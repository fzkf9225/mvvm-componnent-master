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


import android.location.Location;

public class Locations {


    public static Location getLocationWithAdjustedAltitude(Location loc, PreferenceHelper ph) {
        if(!loc.hasAltitude()){ return loc; }

        if(ph.shouldAdjustAltitudeFromGeoIdHeight() && loc.getExtras() != null){
            String geoidheight = loc.getExtras().getString(BundleConstants.GEOIDHEIGHT);
            if (!Strings.isNullOrEmpty(geoidheight)) {
                loc.setAltitude(loc.getAltitude() - Double.parseDouble(geoidheight));
            }
            else {
                //If geoid height not present for adjustment, don't record an elevation at all.
                loc.removeAltitude();
            }
        }

        if(loc.hasAltitude() && ph.getSubtractAltitudeOffset() != 0){
            loc.setAltitude(loc.getAltitude() - ph.getSubtractAltitudeOffset());
        }

        return loc;
    }

    public static Location getLocationAdjustedForGPSWeekRollover(Location loc) {
        long recordedTime = loc.getTime();
        //If the date is before April 6, 23:59:59, there's a GPS week rollover problem
        if(recordedTime < 1554595199000L){
            recordedTime += 619315200000L;  //add 1024 weeks
            loc.setTime(recordedTime);
        }

        return loc;
    }

}
