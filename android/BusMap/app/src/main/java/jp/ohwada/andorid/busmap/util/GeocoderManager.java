/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap.util;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Loader;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.ohwada.andorid.busmap.Constant;

/**
 * GeocoderManager
 */ 
public class GeocoderManager
    implements LoaderManager.LoaderCallbacks<List<Address>> {

   // debug
    private static final String TAG_SUB = GeocoderManager.class.getSimpleName();;

    private static final int LOADER_ID = 0;
    private static final String KEY_LOCATION = "location";

    private Context mContext;
    private LoaderManager mLoaderManager;

    private boolean isLoaderFirst = true;
 
   // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onLoadFinished( boolean flag, double lat, double lon );
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
    public GeocoderManager( Activity activity ) {
        mContext = activity;
        mLoaderManager = activity.getLoaderManager();
    }

    /**
     * cancel
     */
    public void cancel() {
        log_d("cancel");
        Loader loader = mLoaderManager.getLoader( LOADER_ID );
        if ( loader != null ) {
            loader.cancelLoad();
        }
    }

    /**
     * destroy
     */
    public void destroy() {
        cancel();
        mLoaderManager.destroyLoader( LOADER_ID );
    }

    /**
     * === onCreateLoader ===
     * @param int id
     * @param Bundle args
     * @return Loader<LoaderResult>
     */ 
    @Override
    public Loader<List<Address>> onCreateLoader( int id, Bundle args ) {
        log_d("onCreateLoader");
        GeocoderLoader loader = new GeocoderLoader( mContext );
        loader.setLocation( args.getString( KEY_LOCATION ) );
        return loader;
    }

    /**
     * === onLoadFinished ===
     * @param Loader<LoaderResult> loader
     * @param LoaderResult data
     */
    @Override
    public void onLoadFinished( Loader<List<Address>> loader, List<Address> list ) {
        log_d("onLoadFinished");
        boolean flag = false;
        double lat = 0;
        double lon = 0;
        if ( list.size() > 0 ) {
            flag = true;
            Address addr = list.get(0);
            lat = addr.getLatitude();
            lon = addr.getLongitude() ;
        }
        notifyLoadFinished( flag, lat, lon );
    }

    /**
     * === onLoaderReset ===
     * @param Loader<LoaderResult> loader
     */
    @Override
    public void onLoaderReset( Loader<List<Address>> loader ) {
        log_d("onLoaderReset");
        // dummy
    }

    /**
     * request
     * @param String locaton
     */
    public void request( String locaton ) {
        Bundle bundle = new Bundle();
        bundle.putString( KEY_LOCATION, locaton );
        if ( isLoaderFirst ) {
            mLoaderManager.initLoader( LOADER_ID, bundle, this );
        } else {
            mLoaderManager.restartLoader( LOADER_ID, bundle, this );
        }
        isLoaderFirst = false;
    }

    /**
     * notifyLoadFinished
     */
    private void notifyLoadFinished( boolean flag, double lat, double lon ) {
        if ( mListener != null ) {
            mListener.onLoadFinished( flag, lat, lon );
        }
    }

    /**
     * log_d
     */
    private static void log_d( String str ) {
        if (Constant.DEBUG) Log.d( Constant.TAG, TAG_SUB + " " + str );
    }

    /**
     * --- class GeocoderLoader ---
     */
    private static class GeocoderLoader extends AsyncTaskLoader<List<Address>> {

        // object
        private Geocoder mGeocoder = null;
	
        // param
        private int mMaxRresults = 1;
        private int mMaxRetry = 3;
        private String mLocation = "";

        /**
         * === Constructor ===
         * @param Context context
         */ 
        public GeocoderLoader( Context context ) {
            super( context );
        }
 
        /*
         * setMaxResults
         * @param int results
         */
        public void setMaxResults( int results ) {
            mMaxRresults = results;
        }

        /*
         * setMaxRetry
         * @param int retry
         */
        public void setMaxRetry( int retry ) {
            mMaxRetry = retry ;
        }

        /*
         * setLocation
         * @param String location
         */
        public void setLocation( String location ) {
            mLocation = location ;
        }

        /**
         * === onStartLoading ===
         */
        @Override 
        protected void onStartLoading() {
            log_d("onStartLoading");
            // does not start without forceLoad
            forceLoad();
        }

        /**
         * === onStartLoading ===
         */
        @Override 
        protected boolean onCancelLoad() {
            // dummy
            log_d("onCancelLoad");
            return true;
        }

        /**
         * === loadInBackground ===
         * @return List<Address>
         */
        @Override 
        public List<Address> loadInBackground() {
            log_d("loadInBackground");
            return getAddressListRetry( mLocation, mMaxRresults, mMaxRetry );
        }

        /**
         * === deliverResult ===
         * @param List<Address> list
         */
        @Override 
        public void deliverResult( List<Address> list ) {
            log_d("deliverResult");
            super.deliverResult( list );
        }
					
        /**
         * search latitude and longitude  from location name 
         * repeats 3 times until get latitude and longitude
         */
        private List<Address> getAddressListRetry( String location, int maxResults, int maxRetry ) {
            List<Address> list = new ArrayList<Address>();
            if ( "".equals( location ) ) return list;
            // repeats 3 times until get latitude and longitude 
            for ( int i=0; i < mMaxRetry; i ++ ) {
                list = getAddressList(location, maxResults);
                // break, if get latitude and longitude
                if (( list != null ) && !list.isEmpty() ) break;
            }
            return list;
        }
	
        /**
         * search latitude and longitude  from location name 
         */
        private List<Address> getAddressList( String location, int maxResults ) {
            List<Address> list = new ArrayList<Address>();
            if ( "".equals( location ) ) return list;
            mGeocoder = new Geocoder( getContext(), Locale.getDefault() ) ;
            try {
                list = mGeocoder.getFromLocationName( location, maxResults );
            } catch ( IOException e ) {
                if (Constant.DEBUG) e.printStackTrace();
            }
            return list;
        }
		    		   
    } // class end

}
