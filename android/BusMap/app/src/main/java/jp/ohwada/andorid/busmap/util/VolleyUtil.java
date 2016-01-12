/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap.util;

import android.content.Context;
import android.util.Log;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.ohwada.andorid.busmap.Constant;

/*
 * VolleyUtil
 */ 
public class VolleyUtil {

   // debug
    private static final String TAG_SUB = VolleyUtil.class.getSimpleName();

    private static final String URL_API = Constant.HOST + "/api.php?";
    private static final String REQ_TAG = "volley";

    private static final String DEFAULT_CACHE_DIR = "volley";
    private static final int CACHE_SIZE = 16 * 1024 * 1024;  // 16 MB

    private RequestQueue mQueue;

    // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onResponse( int mode, String response );
        void onErrorResponse( String error );
    }

    /*
     * callback
     */ 
    public void setOnChangedListener( OnChangedListener listener ) {
        mListener = listener;
    }

    /**
     * Constractor
     * @param Context context
     */
    public VolleyUtil( Context context ) {
        mQueue = newRequestQueue(context, CACHE_SIZE);
    }

    /**
     * newRequestQueue
     */
    private static RequestQueue newRequestQueue(Context context, int cacheSize) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);
        Network network = new BasicNetwork(
            new HurlStack());
        RequestQueue queue = new RequestQueue(
            new DiskBasedCache(cacheDir, cacheSize),
            network);
        return queue;
    }

   /**
     * start
     */
    public void start() {
        log_d("start");
        mQueue.start();
    }

   /**
     * stop
     */
    public void stop() {
        log_d("stop");
        mQueue.stop();
    }

   /**
     * cancel
     */
    public void cancel() {
        log_d("cancel");
        mQueue.cancelAll(REQ_TAG);
    }

    /**
     * requestLocation
     * @param int mode
     * @param double lat
     * @param double lon
     * @param int distance
     */
    public void requestLocation( int mode, double lat, double lon, int distance ) {
        String url = URL_API  + "mode=location&lat=" + lat + "&lon=" + lon + "&distance=" + distance;
       requestServer(url, mode);
    }

    /**
     * requestNodes
     * @param int mode
     * @param List<Integer> list
     */
    public void requestNodes( int mode, List<Integer> list ) {
        if( list.size() == 0 ) return;
        String str = getStringUniqIds( list );
        String url = URL_API  + "mode=nodes&ids=" + str;
        requestServer( url, mode ); 
    }

    /**
     * requestRoutesMultiple
     * @param int mode
     * @param List<ArrayList<Integer>> list_list
     */
    public void requestRoutesMultiple( int mode, List<ArrayList<Integer>> list_list ) {
        // In order to avoid out of memory, 
        // split process to server
        if ( list_list.size() == 0 ) return;
        for( ArrayList<Integer> list: list_list ) {
            requestRoutes( mode, list );
        }
    }

    /**
     * requestRoutes
     * @param int mode
     * @param List<Integer> list
     */
    public void requestRoutes( int mode, List<Integer> list ) {
        if( list.size() == 0 ) return;
        String str = getStringUniqIds( list );
        String url = URL_API  + "mode=routes&ids=" + str;
        requestServer( url, mode ); 
    }

    /**
     * requestCompanies
     * @param int mode
     * @param List<Integer> list
     */
    public void requestCompanies( int mode, List<Integer> list ) {
        if( list.size() == 0 ) return;
        String str = getStringUniqIds( list );
        String url = URL_API  + "mode=companies&ids=" + str;
        requestServer( url, mode ); 
    }

    /**
     * getStringUniqIds
     */
    private String getStringUniqIds( List<Integer> list ) {
        boolean is_first = true;
        String str = "";
        Set<Integer> set = new HashSet<Integer>();
        for( int i=0; i<list.size(); i++ ) {
            int id = list.get(i);
           if ( !set.contains(id) ) {
                // add, if NOT contains
                set.add(id);
                if (!is_first) {
                    str += ",";
                }
                str += list.get(i);
                is_first = false;
            }
        }
        return str;
    }

    /**
     * getStringUniqIds
     */
    private String getStringUniqIds( int[] ids ) {
        boolean is_first = true;
        String str = "";
        Set<Integer> set = new HashSet<Integer>();
        for( int i=0; i<ids.length; i++ ) {
            int id = ids[i];
           if ( !set.contains(id) ) {
                set.add(id);
                if (!is_first) {
                    str += ",";
                }
                str += ids[i];
                is_first = false;
            }
        }
        return str;
    }

    /**
     * requestServer
     */
    private void requestServer( String url, int mode ) {
        log_d("requestServer " + url);
        final int final_mode = mode;
        StringRequest request =
            new StringRequest( 
                Request.Method.GET, 
                url, 
                new Response.Listener<String>() {
                    @Override
                    public void onResponse( String response ) {
                        notifyResponse( final_mode, response );
                    }
                },
                new Response.ErrorListener() {
                    @Override 
                    public void onErrorResponse( VolleyError e ) {
                        if (Constant.DEBUG) e.printStackTrace();
                        notifyErrorResponse( e.getMessage() );
                    }
                }
            );
        request.setTag( REQ_TAG );
        mQueue.add( request );
    }

    /**
     * notifyResponse
     */
    private void notifyResponse( int mode, String response ) {
        if ( mListener != null ) {
            mListener.onResponse( mode, response );
        }
    }

    /**
     * notifyErrorResponse
     */
    private void notifyErrorResponse( String error ) {
        if ( mListener != null ) {
            mListener.onErrorResponse( error );
        }
    }

    /**
     * log_d
     */
    private void log_d( String str ) {
        if (Constant.DEBUG) Log.d( Constant.TAG, TAG_SUB + " " + str );
    }

}
