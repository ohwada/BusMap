/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.view.View;
import android.widget.Button;

import jp.ohwada.android.busmap.view.CommonDialog;
import jp.ohwada.android.busmap.view.SearchView;

/**
 * Option Dialog
 */
public class OptionDialog extends CommonDialog {

    public static final int GPS_LAST_KNOWN = 0;
    public static final int GPS_CHANGED = 1;

    private Activity mActivity;

    private LocationSearcher mSearcher;
    private SearchView mSearchView;

    private boolean mHasPermGps = false;

   // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onButtonClick();
        void onGeocoderFinished( double lat, double lon );
        void onGpsChanged(int mode, double lat, double lon);
        void onPermission();
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
    public OptionDialog( Activity activity ) {
        super( activity, R.style.Theme_OptionDialog );
        intDialog(activity);
    }

    /**
     * === Constructor ===
     * @param Activity activity
     * @param int theme
     */ 
    public OptionDialog( Activity activity, int theme ) {
        super( activity, theme ); 
        intDialog(activity);
    }

    /**
     * intDialog
     */
    private void intDialog(Activity activity) {
        TAG_SUB = OptionDialog.class.getSimpleName();
        mActivity  = activity;
    }
	
    /**
      * === create ===
      */
    @Override	 	
    public void create() {
        log_d("create");
        TAG_SUB = OptionDialog.class.getSimpleName();
        View view = getLayoutInflater().inflate( R.layout.dialog_option, null );
        setContentView( view );
        createButtonClose() ;
        setLayoutWidthFull();
        setGravityTop();

        mSearchView = new SearchView( getContext(), view );
        mSearchView.create();
        mSearchView.setOnChangedListener( new SearchView.OnChangedListener() {
            @Override
            public void onAddress( String address ) {
                mSearcher.requestGeocode( address );
            }
        });
	
        Button btnDefault = (Button) findViewById( R.id.Button_option_default );
        btnDefault.setText( mSearchView.getGeoName() );
        btnDefault.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v) {
                notifyButtonClick();
            }
        });

        Button btnGps = (Button) findViewById( R.id.Button_option_gps );
        btnGps.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v) {
                log_d("gps request");
                procClickGps();
            }
        });

        mSearcher = new LocationSearcher( mActivity, view );
        mSearcher.create();
        mSearcher.setOnChangedListener( 
            new LocationSearcher.OnChangedListener() {
            @Override
            public void onGeocoderFinished( double lat, double lon ) {
                mSearchView.hideInputMethod();
                notifyGeocoderFinished(lat, lon);
            }
            @Override
            public void onGpsChanged(double lat, double lon) {
                mSearcher.stopGps();
                notifyGpsChanged(GPS_CHANGED, lat, lon);
            }
        });

    }

    /**
     * setPermGps
     * @param boolean hasPermGps
     */
    public void setPermGps( boolean hasPermGps ) {
        mHasPermGps = hasPermGps;
    }

    /**
     * cancel
     */
    public void cancelGeocoder() {
        log_d("cancelGeocoder");
        if ( mSearcher != null ) {
            mSearcher.cancel();
        }
    }

    /**
     * destroy
     */
    public void destroy() {
        log_d("destroy");
        if ( mSearcher != null ) {
            mSearcher.destroy();
        }
    }

    /**
     * procClickGps
     */
    public void startGps() {
        Location loc = mSearcher.startGps();
        if ( loc != null ) {
            notifyGpsChanged(GPS_LAST_KNOWN, loc.getLatitude(), loc.getLongitude());
        }
    }

    /**
     * procClickGps
     */
    private void procClickGps() {
        if ( mHasPermGps ) {
            startGps();
        } else {
            notifyPermission();
        }
    }

    /**
     * notifyButtonClick
     */
    private void notifyButtonClick() {
        if ( mListener != null ) {
            mListener.onButtonClick();
        }
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
    private void notifyGpsChanged(int mode, double lat, double lon) {
        if ( mListener != null ) {
            mListener.onGpsChanged(mode, lat, lon);
        }
    }

    /**
     * notifyPermission
     */
    private void notifyPermission() {
        if ( mListener != null ) {
            mListener.onPermission();
        }
    }
	
}
