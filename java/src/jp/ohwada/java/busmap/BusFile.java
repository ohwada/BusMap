/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.busmap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import jp.ohwada.java.busmap.busfile.*;
import jp.ohwada.java.ksj.busutil.BusDbFactory;
import jp.ohwada.java.ksj.busutil.busdb.*;

/**
 * Bus Stop
 */ 
public class BusFile {

    private File mDir;
    private BusDbFactory mDb;
    private BusFileOsm mOsm;
    private BusFileGpx mGpx;
    private BusFileKml mKml;
//    private BusFileJson mJson;
    private BusFileJavascript mJavascript;

    private String mTimeStamp = "";
    private boolean isDubugMsg = true;

    /**
     * constractor
     */ 
    public BusFile() {
        mDb = new BusDbFactory();
        mOsm = new BusFileOsm();
        mGpx = new BusFileGpx();
        mKml = new BusFileKml();
//        mJson = new BusFileJson();
        mJavascript= new BusFileJavascript();
        SimpleDateFormat timeStampFmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        mTimeStamp = timeStampFmt.format(new Date(Calendar.getInstance().getTimeInMillis()));
    }

    /**
     * setDb
     * @param BusDbFactory db
     */
    public void setDb( BusDbFactory db ) {
        mDb = db;
    }

    /**
     * setDubugMsg
     * @param boolean flag
     */
    public void setDubugMsg( boolean flag ) {
        isDubugMsg = flag;
    }

    /**
     * createPref
     * @param int prefId
     */
    public void createPref( int prefId ) {
        long start = System.currentTimeMillis();
        printAlways( "createPref " + prefId );
        mDir = new File( "data" );
        mDir.mkdir();
        mDb.prepareStatement();
        create( prefId  );
        long time = System.currentTimeMillis() - start;
        printAlways( "Excution Time: " + time );
    }

    private void create( int prefId ) {
        List<BusRecStop> listStop = mDb.selectNodeListByPrefId( prefId );
        BusRecPref recPref = mDb.selectPrefById( prefId );
        // open
        String filename = "prefecture_" + prefId;
        mKml.open( mDir, filename ); 
        mGpx.open( mDir, filename ); 
        mOsm.open( mDir, filename ); 
//        mJson.open( mDir, filename ); 
        mJavascript.open( mDir, filename ); 
        // header
        String title = "バス停 : " + recPref.name;
        int size = listStop.size();
        mKml.writeHeader( title );
        mGpx.writeHeader( title );
        mOsm.writeHeader( title );
//        mJson.writeHeader( size );
        mJavascript.writeHeader();
        for ( BusRecStop recStop: listStop ) {
            int id = recStop.id;
            String name = recStop.name;
            Double lat = recStop.lat;
            Double lon = recStop.lon;
            mKml.writePoint( lat, lon, name );
//            mJson.writePoint( lat, lon, name );
            mJavascript.writePoint( lat, lon, name, id );
            mGpx.writePoint( lat, lon, name, mTimeStamp );
            mOsm.writePoint( lat, lon, name, -id, mTimeStamp );
        }

        // close
        mKml.writeFooter();
        mKml.close();
        mGpx.writeFooter();
        mGpx.close();
        mOsm.writeFooter();
        mOsm.close();
//        mJson.writeFooter();
//        mJson.close();
        mJavascript.writeFooter();
        mJavascript.close();
    }

    private void printAlways( String str ) {
        if (!isDubugMsg) System.out.println();
        System.out.println( str );
    }
}
