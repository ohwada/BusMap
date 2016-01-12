/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap;

import android.app.Activity;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;

import jp.ohwada.andorid.busmap.view.CommonDialog;

/**
 * Option Dialog
 */
public class OptionDialog extends CommonDialog {

    private Activity mActivity;

    private LocationSearcher mSearcher;

   // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onButtonClick();
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
      * create
      */ 	
    public void create() {
        TAG_SUB = OptionDialog.class.getSimpleName();
        View view = getLayoutInflater().inflate( R.layout.dialog_option, null );
        setContentView( view );
        createButtonClose() ;
        setLayoutWidthFull();
        setGravityTop();

        mSearcher = new LocationSearcher( mActivity, view );
        mSearcher.create();
        mSearcher.setOnChangedListener( 
            new LocationSearcher.OnChangedListener() {
            @Override
            public void onGeocoderFinished( double lat, double lon ) {
                notifyGeocoderFinished(lat, lon);
            }
            @Override
            public void onGpsChanged(double lat, double lon) {
                mSearcher.stopGps();
                notifyGpsChanged(lat, lon);
            }
        });
	
        Button btnDefault = (Button) findViewById( R.id.Button_option_default );
        btnDefault.setText( mSearcher.getGeoName() );
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
                procClick();
            }
        });

    }

    /**
     * cancel
     */
    public void cancel() {
        if ( mSearcher != null ) {
            mSearcher.cancel();
        }
    }

    /**
     * destroy
     */
    public void destroy() {
        if ( mSearcher != null ) {
            mSearcher.destroy();
        }
    }

    /**
     * procClick
     */
    private void procClick() {
        Location loc = mSearcher.startGps();
        if ( loc != null ) {
            notifyGpsChanged(loc.getLatitude(), loc.getLongitude());
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
    private void notifyGpsChanged(double lat, double lon) {
        if ( mListener != null ) {
            mListener.onGpsChanged(lat, lon);
        }
    }
	
}
