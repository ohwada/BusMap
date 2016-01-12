/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap.util;

import android.os.Handler;

/*
 * DelayUtil
 */
public class DelayUtil { 

    // delay timer
    private boolean isRunnig = false;

    // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onEvent();
    }

    /*
     * callback
     */ 
    public void setOnChangedListener( OnChangedListener listener ) {
        mListener = listener;
    }

    /**
     * === onCreate ===
     */
    public DelayUtil( ) {
        // dummy
    }

    /**
     * start
     */
    public void start( long time ) {
        if ( !isRunnig ) {
            isRunnig = true;
            delayHandler.postDelayed( delayRunnable, time ); 
        }
    }

// --- Handler class ----
    private final Handler delayHandler = new Handler();
    
// --- Runnable class ----  	
    private final Runnable delayRunnable = new Runnable() {
        @Override
        public void run() {
            isRunnig = false;
            notifyEvent();
        }
    };

    /**
     * notifyEvent
     */
    private void notifyEvent()  {
        if ( mListener != null ) {
            mListener.onEvent();
        }
    }

}
