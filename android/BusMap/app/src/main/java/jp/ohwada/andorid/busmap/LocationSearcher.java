/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import jp.ohwada.andorid.busmap.util.BusmapLocationManager;
import jp.ohwada.andorid.busmap.util.GeocoderManager;
import jp.ohwada.andorid.busmap.util.ToastMaster;
import jp.ohwada.andorid.busmap.view.SearchView;

/**
 * LocationSearcher
 */
public class LocationSearcher {

   // debug
    private static final String TAG_SUB = LocationSearcher.class.getSimpleName();

    private Activity mActivity;
    private Context mContext;
    private View mView;

    // object
    private GeocoderManager mGeocoderManager;
    private BusmapLocationManager mLocationManager;
    private SearchView mSearchView; 

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
        mSearchView = new SearchView( mContext, mView );
        mSearchView.create();
        mSearchView.setOnChangedListener( new SearchView.OnChangedListener() {
            @Override
            public void onAddress( String address ) {
                log_d("geocoder request");
                isGeocoderRequest = true;
                mGeocoderManager.request( address );
            }
        });

        mGeocoderManager = new GeocoderManager( mActivity );
        mGeocoderManager.setOnChangedListener( 
            new GeocoderManager.OnChangedListener() {
            @Override
            public void onLoadFinished( boolean flag, double lat, double lon ) {
                log_d("geocoder response");
                // When Activity become onStart from onStop,
                // sometimes response is returned, if NOT request
                if ( isGeocoderRequest ) {
                    procLoadFinished( flag, lat, lon );
                }
                isGeocoderRequest = false;
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
        if ( mGeocoderManager != null ) { 
            mGeocoderManager.cancel();
        }
    }

    /**
     * destroy
     */
    public void destroy() {
        if ( mGeocoderManager != null ) { 
            mGeocoderManager.destroy();
        }
    }

    /**
     * startGps
     * @param startGps
     */
    public Location startGps() {
        return mLocationManager.start();
    }

    /**
     * stopGps
     */
    public void stopGps() {
        mLocationManager.stop();
    }

    /**
     * getGeoName
     * @return String
     */ 	
    public String getGeoName() {
        return mSearchView.getGeoName();
    }

    /**
     * setAddressEdit
     */ 	
    public void setAddressEdit() {
        mSearchView.setAddressEdit();
    }

    /**
     * getAddressEdit
     * @return String 
     */	
    public String getAddressEdit() {
        return mSearchView.getAddressEdit();
    }

    /**
     * procLoadFinished
     */
    private void procLoadFinished( boolean flag, double lat, double lon ) {
        // if NOT found
        if ( !flag ) {
            toast_short(R.string.search_not_found);
            return;
        }
        mSearchView.hideInputMethod();
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
