/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap.util;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jp.ohwada.android.busmap.Constant;

public class BusmapLocationManager 
    implements LocationListener {

   // debug
    private static final String TAG_SUB = BusmapLocationManager.class.getSimpleName();;

    private static final boolean LOC_ENABLED_ONLY = true;

    private LocationManager mLocationManager;

    private String mProvider = "";

    // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onLocationChanged( double lat, double lng );
    }

    /*
     * callback
     */ 
    public void setOnChangedListener( OnChangedListener listener ) {
        mListener = listener;
    }

    /**
     * === Constructor ===
     * @param Context context
     */ 
    public BusmapLocationManager( Context context ) {
        mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }


    /**
     * initBestProvider
     */ 
    public void initBestProvider() {
        Criteria criteria = new Criteria();
        mProvider = mLocationManager.getBestProvider(criteria, LOC_ENABLED_ONLY);
        log_d("provider " + mProvider);
    }

    /**
     * getLastKnownLocation
     * @return Location
     */ 
    public Location getLastKnownLocation() {
        return mLocationManager.getLastKnownLocation(mProvider);
    }

    /**
     * requestUpdates
     * @param long time
     * @param float distance
     */ 
    public void requestUpdates(long time, float distance) {
        mLocationManager.requestLocationUpdates(
            mProvider, time, distance, this );
   }

    /**
     * removeUpdates
     */ 
    public void removeUpdates() {
        mLocationManager.removeUpdates( this );
    }

    /**
     * === onLocationChanged ===
     */ 
    @Override
    public void onLocationChanged( Location location ) { 
        double lat = location.getLatitude();
        double lng = location.getLongitude();
        notifyLocationChanged( lat, lng );
    }
 
    /**
     * === onProviderDisabled ===
     */ 
    @Override
    public void onProviderDisabled( String provider ) {
        // dummy
    }
 
    /**
     * === onProviderEnabled ===
     */
    @Override
    public void onProviderEnabled( String provider ) {
        // dummy
    }

    /**
     * === onStatusChanged ===
     */ 
    @Override
    public void onStatusChanged( String provider, int status, Bundle extras ) {
        // dummy
    }

    /**
     * notifyLocationChanged
     */
    private void notifyLocationChanged( double lat, double lng )  {
        if ( mListener != null ) {
            mListener.onLocationChanged( lat, lng );
        }
    }

    /**
     * log_d
     */
    private void log_d( String str ) {
        if (Constant.DEBUG) Log.d( Constant.TAG, TAG_SUB + " " + str );
    }
}
