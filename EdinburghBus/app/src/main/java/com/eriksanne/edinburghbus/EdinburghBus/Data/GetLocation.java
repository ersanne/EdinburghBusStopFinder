package com.eriksanne.edinburghbus.EdinburghBus.Data;

import android.location.Location;
import android.location.LocationManager;

import java.util.List;

/**
 * This class provides the location data of the user,
 * it is only used to get the location when the user opens the app.
 * Created by Erik on 14/03/2018.
 */

public class GetLocation {

    private static final int TWO_MINUTES = 120000;

    private GetLocation() {
        // Nothing to do here.
    }

    public static Location getBestInitialLocation(final LocationManager locationManager) {
        if (locationManager == null) {
            return null;
        }

        final List<String> matchingProviders = locationManager.getAllProviders();
        Location location, bestLocation = null;

        for (String provder : matchingProviders) {
            location = locationManager.getLastKnownLocation(provder);

            if (location != null && isBetterLocation(location, bestLocation)) {
                bestLocation = location;
            }
        }

        return bestLocation;
    }


    public static boolean isBetterLocation(final Location location,
                                           final Location currentBestLocation) {
        if (currentBestLocation == null) {
            return true;
        }

        final long timeDelta = location.getTime()
                - currentBestLocation.getTime();
        final boolean isNewer = timeDelta > 0;

        if (timeDelta > TWO_MINUTES) {
            return true;
        } else if (timeDelta < -TWO_MINUTES) {

            return false;
        }

        final int accuracyDelta = (int) (location.getAccuracy()
                - currentBestLocation.getAccuracy());
        final boolean isLessAccurate = accuracyDelta > 0;
        final boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        final boolean isFromSameProvider = isSameProvider(
                location.getProvider(), currentBestLocation.getProvider());

        if (accuracyDelta < 0) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate
                && isFromSameProvider) {
            return true;
        }

        return false;
    }

    public static boolean isSameProvider(final String provider1,
                                         final String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }

        return provider1.equals(provider2);
    }
}

