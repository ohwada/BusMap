/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap;

import android.content.Context;

import jp.ohwada.android.busmap.view.CommonDialog;

/**
 * Permission Dialog
 */
public class PermissionDialog extends CommonDialog {

    private static final String LF = "\n"; 

   // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onClickYes();
        void onClickNo();
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
    public PermissionDialog( Context context ) {
        super( context );
        intDialog();
    }

    /**
     * === Constructor ===
     * @param Context context
     * @param int theme
     */ 
    public PermissionDialog( Context context, int theme ) {
        super( context, theme ); 
        intDialog();
    }

    /**
     * intDialog
     */
    private void intDialog() {
        TAG_SUB = PermissionDialog.class.getSimpleName();
    }

    /**
     * === create ===
     */
    @Override	
    public void create() {
        log_d("create");
        mView = getLayoutInflater().inflate( R.layout.dialog_yes_no, null );
        setContentView(mView);
        setLayoutWidthFromDimension( R.dimen.dialog_permission_view_width );
        setTitle( R.string.dialog_permission_title );
        createButtonYes( R.string.button_dialog_perm_yes );
        createButtonNo( R.string.button_dialog_perm_no );
    }

    /**
     * setPerm
     * @param boolean hasStorage
     * @param boolean hasGps
     */
    public void setPerm(boolean hasStorage, boolean hasGps) {
        String msg = getString( R.string.dialog_permission_message ) + LF;
        if ( !hasStorage ) {
            msg += getString( R.string.dialog_permission_storage ) + LF;
        } 
        if ( !hasGps ) {
            msg += getString( R.string.dialog_permission_gps ) + LF;
        } 
        setMessage( msg );
    }

    /**
     * === cancel ===
     */
    @Override	
    public void cancel() {
        notifyClickNo();
    }

    /**
     * procClickYes
     */ 
    protected void procClickYes() {
        notifyClickYes();
    }

    /**
     * procClickYes
     */ 
    protected void procClickNo() {
        notifyClickNo();
    }

    /**
     * notifyClickYes
     */
    private void notifyClickYes() {
        if ( mListener != null ) {
            mListener.onClickYes();
        }
    }

    /**
     * notifyClickNo
     */
    private void notifyClickNo() {
        if ( mListener != null ) {
            mListener.onClickNo();
        }
    }

}
