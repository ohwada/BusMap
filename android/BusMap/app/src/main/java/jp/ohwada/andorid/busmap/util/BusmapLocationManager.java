/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap.util;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import jp.ohwada.andorid.busmap.Constant;

public class BusmapLocationManager 
    implements LocationListener {

   // debug
    private static final String TAG_SUB = BusmapLocationManager.class.getSimpleName();;

    private static final long LOC_MIN_TIME = 20000; // 20 sec
    private static final float LOC_MIN_DISTANCE = 0f;   // 0 m

    private LocationManager mLocationManager;

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
     * start
     */ 
    public Location start() {
        Criteria criteria = new Criteria();
        String provider = mLocationManager.getBestProvider(criteria, true);            
        Location location = mLocationManager.getLastKnownLocation(provider);
        mLocationManager.requestLocationUpdates(
            provider, LOC_MIN_TIME, LOC_MIN_DISTANCE, this );
        return location;
   }

    /**
     * stop
     */ 
    public void stop() {
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
