/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import jp.ohwada.andorid.busmap.util.DelayUtil;
import jp.ohwada.andorid.busmap.util.ToastMaster;

/*
 * MapSetting Activity
 */
public class MapSettingActivity extends Activity {

    // constant
    private static final int DELAY_TIME = 1000; 	// 1 sec

    // object
    private GoogleMap mMap;
    private SharedPreferences mPreferences;
    private DelayUtil mDelayUtil;
    private LocationSearcher mSearcher;

    // map	
    private Marker mMarker;
    private BitmapDescriptor mIcon;

    // delay timer
    private boolean isRunnig = false;
			
    /*
     * === onCreate ===
     */
    @Override 
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        View view = getLayoutInflater().inflate( R.layout.activity_map_setting, null ); 
        setContentView(view);

        Button btnSet = (Button) findViewById(R.id.Button_map_set);
        btnSet.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                setLocation();
            }
        });

        mPreferences = PreferenceManager.getDefaultSharedPreferences( this );

        mDelayUtil = new DelayUtil();
        mDelayUtil.setOnChangedListener( new DelayUtil.OnChangedListener() {
            @Override
            public void onEvent() {
                moveMarkerToCenter();
            }
        });

        mSearcher = new LocationSearcher( this, view );
        mSearcher.create();
        mSearcher.setAddressEdit();
        mSearcher.setOnChangedListener( 
            new LocationSearcher.OnChangedListener() {
            @Override
            public void onGeocoderFinished( double lat, double lon ) {
                moveToPoint((float) lat, (float) lon, Constant.GEO_ZOOM);
                toast_show(R.string.search_found);
            }
            @Override
            public void onGpsChanged(double lat, double lon) {
                // dummy
            }
        });
    }

    /**
     * === onResume ===
     */
    @Override
    protected void onResume() {
        super.onResume();
        setUpMap();
    }

    /**
     * === onPause ===
     */
    @Override
    protected void onPause() {
        super.onPause();
        mSearcher.cancel();
    }

    /**
     * === onDestroy ===
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSearcher.destroy();
    }

    /**
     * === dispatchTouchEvent ===
     */
    @Override
    public boolean dispatchTouchEvent( MotionEvent event ) {
        // show marker in center, if finger is move up from a screen
        moveMarkerToCenter();
        mDelayUtil.start(DELAY_TIME);
        return super.dispatchTouchEvent( event );
    }

    /**
     * setUpMap
     */
    private void setUpMap() {
        float lat = mPreferences.getFloat(
            Constant.PREF_GEO_LAT, Constant.GEO_LAT);
        float lon = mPreferences.getFloat(
            Constant.PREF_GEO_LON, Constant.GEO_LON );
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager()
                .findFragmentById(R.id.setting_map))
                .getMap();
        }
        moveToPoint(lat, lon, Constant.GEO_ZOOM);
        BitmapDescriptor icon = 
            BitmapDescriptorFactory.fromResource(R.drawable.marker_cross);
        drawMarkerOnce((double) lat, (double) lon, icon);
    }

    /**
     * moveToPoint
     */
    private void moveToPoint( float lat, float lng, int zoom ) {
        moveToPoint(new LatLng(lat, lng), zoom);
    }

    /**
     * moveToPoint
     */
    private void moveToPoint( LatLng latlng, int zoom ) {
        CameraUpdate cu =
            CameraUpdateFactory.newLatLngZoom(latlng, zoom);
        mMap.moveCamera(cu);
    }

    /**
     * moveMarkerToCenter
     */
    private void moveMarkerToCenter() {
        CameraPosition cp = mMap.getCameraPosition();
        LatLng target = cp.target;
        mMarker.setPosition(target);
    }

    /**
     * drawMarkerOnce
     */
    private void drawMarkerOnce( double lat, double lon, BitmapDescriptor icon ) {
        if ( mMarker != null ) return;
        mMarker = mMap.addMarker(
            new MarkerOptions()
                .position( new LatLng(lat, lon) )
               .icon( icon )
        );
    }

    /**
     * setLocation
     */
    private void setLocation() {
        String name = mSearcher.getAddressEdit();
        // nothig if no input
        if (( name.length() == 0 )||( name.equals("") )) {
            toast_show( R.string.search_please_name );
            return;
        } 

        // save center point
        CameraPosition cp = mMap.getCameraPosition();
        LatLng target = cp.target;
        float lat = (float)target.latitude;
        float lon = (float)target.longitude;
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString( Constant.PREF_GEO_NAME, name );
        editor.putFloat(Constant.PREF_GEO_LAT, lat);
        editor.putFloat(Constant.PREF_GEO_LON, lon);
        editor.commit(); 

        // return to main
        finish();
    }

    /**
     * toast_show
     * @param int id
     */
    private void toast_show( int id ) {
        ToastMaster.makeText(this, id, Toast.LENGTH_SHORT).show();
    }
}
