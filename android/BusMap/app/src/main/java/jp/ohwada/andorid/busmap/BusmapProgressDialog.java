/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap;

import android.content.Context;
import android.widget.ProgressBar;

import jp.ohwada.andorid.busmap.view.CommonDialog;

/**
 *Progress Dialog
 */
public class BusmapProgressDialog extends CommonDialog {

    private static final int MIN_PROGRESS = 10;
    private static final int MAX_PROGRESS = 1000;

    private ProgressBar mProgressBar;
    private int mPrevProgress = 0;

    /**
     * === Constructor ===
     * @param Context context
     */ 	
    public BusmapProgressDialog( Context context ) {
        super( context, R.style.Theme_ProgressDialog );
        intDialog();
    }

    /**
     * === Constructor ===
     * @param Context context
     * @param int theme
     */ 
    public BusmapProgressDialog( Context context, int theme ) {
        super( context, theme );
        intDialog();
    }

    /**
     * intDialog
     */
    private void intDialog() {
        TAG_SUB = BusmapProgressDialog.class.getSimpleName();
    }

    /**
     * create
     */ 	
    public void create() {
        setContentView( R.layout.dialog_progress );
        setLayoutWidthFull();
        setGravityBottom();

        mProgressBar = (ProgressBar) findViewById( R.id.ProgressBar_dlaog_progress );
        mProgressBar.setMax(MAX_PROGRESS);
        mProgressBar.setProgress(MIN_PROGRESS);
        mPrevProgress = MIN_PROGRESS;			
    }

    /**
     * setProgress
     * @param float progress (0.0 - 1.0)
     */ 	
    public void setProgress( float progress ) {
        int p = (int)( MAX_PROGRESS * progress );
        if ( p > MAX_PROGRESS ) {
            p = MAX_PROGRESS;
        }
        if ( p < MIN_PROGRESS ) {
            p = MIN_PROGRESS;
        }
        if ( p > mPrevProgress ) {	
            mProgressBar.setProgress(p);
            mPrevProgress = p;
        }
    }
	
}
