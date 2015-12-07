/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.busmap;

import java.util.List;
import jp.ohwada.java.ksj.busutil.BusDbFactory;
import jp.ohwada.java.ksj.busutil.busdb.*;

/**
 * BusCalc
 */ 
public class BusCalc {

    private BusDbFactory mDb;

    private boolean isDubugMsg = true;
    private int mDebugCount = 0;

    /**
     * constractor
     */ 
    public BusCalc() {
        // dummy
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
        mDb.setDubugMsg( flag );
    }

    /**
     * calc number of bus stop
     * @param prefId
     */
    public void calcPref( int prefId ) { 
        long start = System.currentTimeMillis();
        printAlways( "calcPref " + prefId );
        mDb.prepareStatement();
        int routeSize = mDb.selectRouteCountByPrefId( prefId );
        List<BusRecStop> listNode = mDb.selectNodeListByPrefId( prefId );
        AreaCount ac = calcArea( listNode );
        mDb.updatePrefNum( prefId, listNode.size(), routeSize, ac.maxLat, ac.minLat,  ac.maxLon, ac.minLon );
        mDb.closeStatement();
        long time = System.currentTimeMillis() - start;
        printAlways( "Excution Time: " + time );
    }

    /**
     * calcWhole
     */
    public void calcWhole() { 
        long start = System.currentTimeMillis();
        System.out.println( "calcWhole" );
        mDb.prepareStatement();
        // Company
        List<BusRecCompany> listCompany = mDb.selectCompanyListAll();
        for (BusRecCompany recCompany: listCompany) {
        	printProgres();
            printMsg( recCompany.name );
            int companyId = recCompany.id;
            List<BusRecRoute> listRoute = mDb.selectRouteListByCompanyId( recCompany.id );
            List<BusRecStop> nodeList = mDb.selectNodeListByRouteList( listRoute );
            AreaCount ac = calcArea( nodeList );
            mDb.updateCompanyNum( companyId, nodeList.size(), listRoute.size(), ac.maxLat,  ac.minLat,  ac.maxLon,  ac.minLon );
        }
        // Route
        // TODO too big
        List<BusRecRoute> listRoute = mDb.selectRouteListAll();
        for (BusRecRoute recRoute: listRoute) {
        	printProgres();
            printMsg( recRoute.bus_line );
            int routeId = recRoute.id;
            List<BusRecStop> nodeList = mDb.selectNodeListByRoute( recRoute );
            AreaCount ac = calcArea( nodeList );
            mDb.updateRouteNum( routeId, nodeList.size(), ac.maxLat,  ac.minLat,  ac.maxLon,  ac.minLon );
        }
        mDb.closeStatement();
        long time = System.currentTimeMillis() - start;
        printAlways( "Excution Time: " + time );
    }

    private AreaCount calcArea( List<BusRecStop> nodeList ) {
        double maxLat = -180.0;
        double minLat = 180.0;
        double maxLon = -180.0;
        double minLon = 180.0;
        for ( BusRecStop recNode: nodeList ) {
            double lat = recNode.lat;
            double lon = recNode.lon;
            // calc area
            if (lat > maxLat) {
                maxLat = lat;
            }
            if (lon > maxLon) {
                maxLon = lon;
            }
            if (lat < minLat) {
                minLat = lat;
            }
            if (lon < minLon) {
                minLon = lon;
            }
        } // for
        AreaCount ac = new AreaCount();
        ac.maxLat = maxLat;
        ac.minLat = minLat;
        ac.maxLon = maxLon;
        ac.minLon = minLon;
        return ac;
    }

    /**
     * updateE6
     */
    public void updateE6() { 
        long start = System.currentTimeMillis();
        System.out.println( "updateE6" );
        mDb.prepareStatement();
        int count = mDb.selectNodeCountAll();
        for ( int i=1; i<=count; i++) {
            if (( i % 1000 ) == 0 ) printProgres();
            BusRecStop rec = mDb.selectNodeById(i);
            int lat_e6 = (int) ( rec.lat * 1e6 );
            int lon_e6 = (int) ( rec.lon * 1e6 );
            mDb.updateNodeE6( i, lat_e6, lon_e6 );
        }
        mDb.closeStatement();
        long time = System.currentTimeMillis() - start;
        printAlways( "Excution Time: " + time );
    }

    private void printMsg( String str ) {
        if (isDubugMsg) System.out.println( str );
    }

    /**
     * printAlways (high level)
     * @param String str
     */
    private void printAlways( String str ) {
        if (!isDubugMsg) System.out.println();
        System.out.println( str );
    }
    
    private void printProgres() {
        if (!isDubugMsg) {
            System.out.print(".");
            if (( mDebugCount % 1000 ) == 0 ) {
                System.out.print("\n");
            }
            mDebugCount ++;
        }
    }

    /**
     * class AreaCount
     */
    private class AreaCount {
        double maxLat = 0.0;
        double minLat = 0.0;
        double maxLon = 0.0;
        double minLon = 0.0;
    }

}
