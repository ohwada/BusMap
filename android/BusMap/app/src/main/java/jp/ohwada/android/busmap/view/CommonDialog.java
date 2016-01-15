/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import jp.ohwada.android.busmap.Constant;
import jp.ohwada.android.busmap.R;
import jp.ohwada.android.busmap.util.ToastMaster;

/**
 * Common Dialog
 */
public class CommonDialog extends Dialog {

   // debug
    protected String TAG_SUB = CommonDialog.class.getSimpleName();

    // constant
    private static final float WIDTH_RATIO_FULL = 0.98f;
    private static final float WIDTH_RATIO_HALF = 0.5f;

    // object
    protected View mView = null;
    protected ListView mListView;

    private Point mDisplaySize;
    private DisplayMetrics mDisplayMetrics;

    /**
     * === Constructor ===
     * @param Context context
     */ 	
    public CommonDialog( Context context ) {
        super( context );
        initDisplayParam();
    }

    /**
     * === Constructor ===
     * @param Context context
     * @param int theme
     */ 
    public CommonDialog( Context context, int theme ) {
        super( context, theme ); 
        initDisplayParam();
    }

    /**
     * === onWindowFocusChanged ===
     */ 
    @Override
    public void onWindowFocusChanged( boolean hasFocus ) {
        super.onWindowFocusChanged( hasFocus );
        if ( mView == null ) return;
        // enlarge width, if screen is small
        int width = calcWidth( WIDTH_RATIO_HALF );			
        if ( mView.getWidth() < width ) {
            setLayoutWidth( width );
        }
    }

    /**
     * === cancel ===
     */ 
    @Override
    public void cancel() {
        log_d("cancel");
        super.cancel();
    }

    /**
     * === dismiss ===
     */ 
    @Override
    public void dismiss() {
        log_d("dismiss");
        super.dismiss();
    }

    /**
     * initDisplayParam
     */ 
    protected void initDisplayParam() {
        WindowManager wm = (WindowManager)
            getContext().getSystemService( Context.WINDOW_SERVICE );
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize( size );
        mDisplaySize = size;
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        mDisplayMetrics = metrics;
     }

    /**
     * setMessage
     * @param String str
     */ 
    protected void setMessage( String str ) {
        TextView tv = (TextView) findViewById( R.id.TextView_dialog_message );
        tv.setText(str);
    }

    /**
     * createButtonClose
     */ 
    protected void createButtonClose() {
        Button btn = (Button) findViewById( R.id.Button_dialog_close );
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v) {
                dismiss();
            }
        });
    }

    /**
     * createButtonYes
     * @param int res_id
     */ 
    protected void createButtonYes( int res_id ) {
        Button btn = (Button) findViewById( R.id.Button_dialog_yes );
        btn.setText(res_id);
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v) {
                procClickYes();
            }
        });
    }

    /**
     * createButtonNo
     * @param int res_id
     */ 
    protected void createButtonNo( int res_id ) {
        Button btn = (Button) findViewById( R.id.Button_dialog_no );
        btn.setText(res_id);
        btn.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v) {
                procClickNo();
            }
        });
    }

    /**
     * procClickYes
     */ 
    protected void procClickYes() {
        // dummy
    }

    /**
     * procClickYes
     */ 
    protected void procClickNo() {
        // dummy
    }

    /**
     * setLayoutWidth
     * @param int res_id
     */ 
    protected void setLayoutWidthFromDimension( int res_id ) {
        int width = (int) adjustWidth( getDimension( res_id ));
        setLayoutWidth( width );
    }

    /**
     * setLayoutWidth
     */ 
    protected void setLayoutWidthHalf() {
        setLayoutWidth( calcWidth( WIDTH_RATIO_HALF ) );
    }

    /**
     * setLayoutWidth
     */ 
    protected void setLayoutWidthFull() {
        setLayoutWidth( calcWidth( WIDTH_RATIO_FULL ) );
    }

    /**
     * setLayoutWidth
     * @param int width
     */  
    protected void setLayoutWidth( int width ) {
        getWindow().setLayout( width, ViewGroup.LayoutParams.WRAP_CONTENT );
    }

    /**
     * calcWidth
     * @param float ratio
     * @return int
     */ 
    protected int calcWidth( float ratio ) {
        int width = (int)( mDisplaySize.x * ratio );
        return width;
    }

    /**
     * adjustWidth
     * @param float width
     * @return int
     */
    protected int adjustWidth( float width ) {
        int min_width = calcWidth( WIDTH_RATIO_HALF );
        int max_width = calcWidth( WIDTH_RATIO_FULL );
        if ( width < min_width ) {
            width = min_width;
        }
        if ( width > max_width ) {
            width = max_width;
        }
        return (int)width;
    }

    /**
     * getDimension
     * @param int res_id
     * @return float
     */
    protected float getDimension( int res_id ) {
        return getContext().getResources().getDimension( res_id );
    }

    /**
     * setGravity
     * show on the top of screen
     */ 
    protected void setGravityTop() {
        getWindow().getAttributes().gravity = Gravity.TOP;
    }


    /**
     * setGravity
     * show on the lower of screen
     */
    protected void setGravityBottom() {
        // show on the lower of screen. 
        getWindow().getAttributes().gravity = Gravity.BOTTOM;
    }

    /**
     * getString
     * @param int res_id 
     * @return String
     */
    protected String getString( int res_id ) {
        return getContext().getResources().getString( res_id );
    }

    /**
     * toast_short
     * @param int res_id 
     */
    protected void toast_short( int res_id ) {
        ToastMaster.makeText(getContext(), res_id, Toast.LENGTH_SHORT).show();
    }

    /**
     * log_d
     */
    protected void log_d( String str ) {
        if (Constant.DEBUG) Log.d( Constant.TAG, TAG_SUB + " " + str );
    }	
}
