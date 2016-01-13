/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import jp.ohwada.android.busmap.view.CommonDialog;

/**
 * About Dialog
 */
public class AboutDialog extends CommonDialog {

    // url
    private static final String URL_CREDIT = "http://android.ohwada.jp/";

    private Activity mActivity;

    /**
     * === Constructor ===
     * @param Activity activity
     */ 	
    public AboutDialog( Activity activity ) {
        super( activity );
        TAG_SUB = AboutDialog.class.getSimpleName();
        intDialog( activity );
    }

    /**
     * === Constructor ===
     * @param Context context
     * @param int theme
     */ 
    public AboutDialog( Activity activity, int theme ) {
        super( activity, theme ); 
        intDialog( activity );
    }

    /**
     * intDialog
     */
    private void intDialog( Activity activity ) {
        TAG_SUB = AboutDialog.class.getSimpleName();
        mActivity  = activity;
    }

    /**
     * === create ===
     */
    @Override	
    public void create() {
        log_d("create");
        mView = getLayoutInflater().inflate( R.layout.dialog_about, null );
        setContentView(mView);
        createButtonClose() ;
        setLayoutWidthFromDimension( R.dimen.dialog_about_view_width );
 
        setTitle( R.string.dialog_about_title );
		
        TextView tvCredit = (TextView) findViewById( R.id.TextView_dialog_about_credit );
        tvCredit.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View v) {
                startBrawser( URL_CREDIT );
            }
        });

    }

    /**
     * startBrawser
     * @param String url
     */
    private void startBrawser( String url ) {
        dismiss();
        Uri uri = Uri.parse( url );
        Intent intent = new Intent( Intent.ACTION_VIEW, uri );
        mActivity.startActivity( intent );
    }
	
}
