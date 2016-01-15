/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import jp.ohwada.android.busmap.util.BusmapLocationManager;
import jp.ohwada.android.busmap.util.GeocoderManager;
import jp.ohwada.android.busmap.util.ToastMaster;

/**
 * LocationSearcher
 */
public class LocationSearcher {

   // debug
    private static final String TAG_SUB = LocationSearcher.class.getSimpleName();

    private static final long GPS_MIN_TIME = 10000; // 10 sec
    private static final float GPS_MIN_DISTANCE = 0f;   // 0 m

    private Activity mActivity;
    private Context mContext;
    private View mView;

    // object
    private GeocoderManager mGeocoderManager;
    private BusmapLocationManager mLocationManager;

    private boolean isGeocoderRequest = false; 

   // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onGeocoderFinished( double lat, double lon );
        void onGpsChanged(double lat, double lon);
    }

    /*
     * callback
     */ 
    public void setOnChangedListener( OnChangedListener listener ) {
        mListener = listener;
    }

    /**
     * === Constructor ===
     * @param Activity activity
     */ 	
    public LocationSearcher( Activity activity, View view ) {
        mActivity  = activity;
        mContext = activity;
        mView = view;
    }
	
    /**
      * create()
      */ 	
    public void create() {
        mGeocoderManager = new GeocoderManager( mActivity );
        mGeocoderManager.setOnChangedListener( 
            new GeocoderManager.OnChangedListener() {
            @Override
            public void onLoadFinished( boolean flag, double lat, double lon ) {
                log_d("geocoder response");
                procLoadFinished( flag, lat, lon );
            }
        });

        mLocationManager = new BusmapLocationManager( mContext );
        mLocationManager.setOnChangedListener(
            new BusmapLocationManager.OnChangedListener() {
            @Override
            public void onLocationChanged(double lat, double lon) {
                log_d("gps response");
                notifyGpsChanged(lat, lon);
            }
        });
		
    }

    /**
     * cancel
     */
    public void cancel() {
        log_d("cancel");
        if ( mGeocoderManager != null ) { 
            mGeocoderManager.cancel();
        }
    }

    /**
     * destroy
     */
    public void destroy() {
        log_d("destroy");
        if ( mGeocoderManager != null ) { 
            mGeocoderManager.destroy();
        }
    }

    /**
     * requestGeocode
     * @param String address
     */
    public void requestGeocode( String address ) {
        log_d("geocoder request");
        isGeocoderRequest = true;
        mGeocoderManager.request( address );
    }

    /**
     * startGps
     * @param startGps
     */
    public Location startGps() {
        mLocationManager.initBestProvider();
        Location location = mLocationManager.getLastKnownLocation();
        mLocationManager.requestUpdates( GPS_MIN_TIME, GPS_MIN_DISTANCE );
        return location;
    }

    /**
     * stopGps
     */
    public void stopGps() {
        mLocationManager.removeUpdates();
    }

    /**
     * procLoadFinished
     */
    private void procLoadFinished( boolean flag, double lat, double lon ) {
        // When Activity become onStart from onStop,
        // sometimes response is returned, if NOT request
        if ( !isGeocoderRequest ) {
            log_d( "procLoadFinished not running");
            return;
        }
        isGeocoderRequest = false;
        // if NOT found
        if ( !flag ) {
            toast_short(R.string.toast_search_not_found);
            return;
        }
        notifyGeocoderFinished(lat, lon);
    }

    /**
     * notifyGeocoderFinished
     */
    private void notifyGeocoderFinished( double lat, double lon ) {
        if ( mListener != null ) {
            mListener.onGeocoderFinished(lat, lon);
        }
    }

    /**
     * notifyGpsChanged
     */
    private void notifyGpsChanged(double lat, double lon) {
        if ( mListener != null ) {
            mListener.onGpsChanged(lat, lon);
        }
    }

    /**
     * toast_short
     */
    private void toast_short( int id ) {
        ToastMaster.makeText(mContext, id, Toast.LENGTH_SHORT).show();
    }

   /**
     * log_d
     */
    private void log_d( String str ) {
        if (Constant.DEBUG) Log.d(Constant.TAG, TAG_SUB + " " + str);
    }	
}
