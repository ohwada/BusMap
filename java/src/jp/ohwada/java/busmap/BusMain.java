/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.busmap;

import java.io.File;

import jp.ohwada.java.ksj.*;
import jp.ohwada.java.ksj.busutil.*;

/**
 * BusMain
 */
public class BusMain {

    private BusDbFactory mDb;
    private BusFile mFile;
    private BusRoute mRoute;
    private BusStop mStop;
    private BusCalc mCalc;

    private boolean mCeateFlag = false;
    private boolean mRouteFlag = false;
    private boolean mStopFlag = false;
    private boolean mCalcPrefFlag = false;
    private boolean mCalcWholeFlag  = false;
    private boolean mFileFlag  = false;

    /**
     * constractor
     */
    public BusMain() {
        mDb = new BusDbFactory();
        mFile = new BusFile();
        mRoute = new BusRoute();
        mStop = new BusStop();
        mCalc = new BusCalc();
    }

    /**
     * init
     * @param String host
     * @param String port
     * @param String db
     * @param String user
     * @param String pass
     * @return boolean
     */
    public void initDb( String host, String port, String db, String user, String pass ) {
        mDb.init( host, port, db, user, pass );
        mFile.setDb( mDb );
        mRoute.setDb( mDb );
        mStop.setDb( mDb );
        mCalc.setDb( mDb );
    }

    /**
     * setDubugMsg
     * @param boolean flag
     */
    public void setDubugMsg( boolean flag ) {
        mDb.setDubugMsg( flag );
        mFile.setDubugMsg( flag );
        mRoute.setDubugMsg( flag );
        mStop.setDubugMsg( flag );
        mCalc.setDubugMsg( flag );
    }

    /**
     * setCeateFlag
     * @param boolean flag
     */
    public void setCeateFlag( boolean flag ) {
        mCeateFlag = flag;
    }

    /**
     * setBusRouteFlag
     * @param boolean flag
     */
    public void setBusRouteFlag( boolean flag ) {
        mRouteFlag = flag;
    }

    /**
     * setBusStopFlag
     * @param boolean flag
     */
    public void setBusStopFlag( boolean flag ) {
        mStopFlag = flag;
    }

    /**
     * setCountPrefFlag
     * @param boolean flag
     */
    public void setCountPrefFlag( boolean flag ) {
        mCalcPrefFlag = flag;
    }

    /**
     * setCountWholeFlag
     * @param boolean flag
     */
    public void setCountWholeFlag( boolean flag ) {
        mCalcWholeFlag = flag;
    }

    /**
     * setFileFlag
     * @param boolean flag
     */
    public void setFileFlag( boolean flag ) {
        mFileFlag = flag;
    }

    /**
     * execute
     */
    public void execute() {
        if ( mCeateFlag) {
            executeCeate();
        }
        if ( mRouteFlag ) {
            executeRoute();
        }
        if ( mStopFlag ) {
            executeStop();
        }
        if ( mCalcPrefFlag ) {
            executeCountPref();
        }
        if ( mCalcWholeFlag ) {
            mCalc.calcWhole();
        }
        if ( mFileFlag ) {
            executeFile();
        }
        System.out.println( "\nEND" );
    }

    private void executeCeate() {
        mDb.createTable();
        mDb.insertPref();
    }

    private void executeRoute() { 
        String name = "N07-11.xml";
        File file = new File( name );
        if ( file.exists() ) {
            mRoute.parse( name );
        } else {
            for ( int i=1; i <= 47; i++ ) {
                name = "N07-11_" + String.format("%1$02d", i) + ".xml";
                file = new File( name );
                if ( file.exists() ) {
                    mRoute.parse( name );
                }
            }
        }
    }

    private void executeStop() {
        String name;
        File file; 
        for ( int i=1; i <= 47; i++ ) {
            name = "P11-10_" + String.format("%1$02d", i) + "-jgd-g.xml";
            file = new File( name );
            if ( file.exists() ) {
                mStop.parse( name, i );
            }
        }
    }

    private void executeCountPref() {
        for ( int i=1; i <= 47; i++ ) {
            mCalc.calcPref( i );
        }
    }

    private void executeFile() {
        for ( int i=1; i <= 47; i++ ) {
            mFile.createPref( i );
        }
    }

}
