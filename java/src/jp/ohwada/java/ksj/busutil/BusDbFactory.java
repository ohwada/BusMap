/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj.busutil;

import java.util.ArrayList;
import java.util.List;
import jp.ohwada.java.ksj.busutil.busdb.*;

/**
 * BusDb
 */
public class BusDbFactory extends BusDbBase {

    private BusDbPref mPref;
    private BusDbCompany mCompany;
    private BusDbRoute mRoute;
    private BusDbCurve mCurve;
    private BusDbStop mNode;
    private BusDbLinkComPref mLinkComPref;
    private BusDbLinkComNode mLinkComNode;
    private BusDbLinkRoutePref mLinkRoutePref;
    private BusDbLinkRouteNode mLinkRouteNode;

    /**
     * constrctor
     */
    public BusDbFactory() {
        super();
        mPref = new BusDbPref();
        mCompany = new BusDbCompany();
        mRoute = new BusDbRoute();
        mCurve = new BusDbCurve();
        mNode = new BusDbStop();
        mLinkComPref = new BusDbLinkComPref();
        mLinkComNode = new BusDbLinkComNode();
        mLinkRoutePref = new BusDbLinkRoutePref();
        mLinkRouteNode = new BusDbLinkRouteNode();
    }

    /**
     * setDubugMsg
     * @param boolean flag
     */
    public void setDubugMsg( boolean flag ) {
        super.setDubugMsg( flag );
        mPref.setDubugMsg( flag );
        mCompany.setDubugMsg( flag );
        mRoute.setDubugMsg( flag );
        mCurve.setDubugMsg( flag );
        mNode.setDubugMsg( flag );
        mLinkComPref.setDubugMsg( flag );
        mLinkComNode.setDubugMsg( flag );
        mLinkRoutePref.setDubugMsg( flag );
        mLinkRouteNode.setDubugMsg( flag );
    }

    /**
     * init
     * @return boolean
     */
    public boolean init( String host, String port, String db, String user, String pass ) {
        boolean ret = initConnection( host, port, db, user, pass );
        mPref.setConnection( mConnection );
        mCompany.setConnection( mConnection ); 
        mRoute.setConnection( mConnection );
        mCurve.setConnection( mConnection );
        mNode.setConnection( mConnection );
        mLinkComPref.setConnection( mConnection );
        mLinkComNode.setConnection( mConnection );
        mLinkRoutePref.setConnection( mConnection );
        mLinkRouteNode.setConnection( mConnection );
        return ret;
    }

// create table
    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        boolean ret1 = mPref.createTable();
        boolean ret2 = mCompany.createTable();
        boolean ret3 = mRoute.createTable();
        boolean ret4 = mCurve.createTable();
        boolean ret5 = mNode.createTable();
        boolean ret6 = mLinkComPref.createTable();
        boolean ret7 = mLinkComNode.createTable();
        boolean ret8 = mLinkRoutePref.createTable();
        boolean ret9 = mLinkRouteNode.createTable();
        if ( !ret1 || !ret2 || !ret3 || !ret4 || !ret5 || !ret6 || !ret7 || !ret8 || !ret9 ) return false;
        return true;
    }

    /**
     * insertPref
     */
    public void insertPref() {
        mPref.insertPref();
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        mPref.prepareStatement();
        mCompany.prepareStatement();
        mNode.prepareStatement();
        mRoute.prepareStatement();
        mCurve.prepareStatement();
        mLinkComPref.prepareStatement();
        mLinkComNode.prepareStatement();
        mLinkRoutePref.prepareStatement();
        mLinkRouteNode.prepareStatement();
    }

    /**
     * prepareStatement
     */
    public void closeStatement() {
        mPref.closeStatement();
        mCompany.closeStatement();
        mNode.closeStatement();
        mRoute.closeStatement();
        mCurve.closeStatement();
        mLinkComPref.closeStatement();
        mLinkComNode.closeStatement();
        mLinkRoutePref.closeStatement();
        mLinkRouteNode.closeStatement();
    }

// parse bus route
    /**
     * execParseCurve
     * @param String curve_key
     * @param String curve
     */
    public void execRouteCurve( String curve_key, String curve ) {
        mCurve.insert( curve_key, curve );
    }

    /**
     * execBusRoute
     * @param BusRouteInformation info
     */
    public void execBusRoute( BusRouteInformation info )  {
        int company_id = mCompany.insertWhenNotExist( info.bus_operation_company );
        int route_id = mRoute.insertWhenNotExist( company_id, info );
        mCurve.updateById( info.bus_curve_id, route_id );
    }

// parse bus stop
    /**
     * execNodePoint
     * @param int prefId
     * @param String pointKey
     * @param double lat
     * @param double lon
     */
    public void execNodePoint( int prefId, int pointId, String pointKey, float lat, float lon ) {
        int lat_e6 = (int) ( lat * 1e6 );
        int lon_e6 = (int) ( lon * 1e6 );
        mNode.insert( prefId, pointId, pointKey, lat, lon, lat_e6, lon_e6 );
    }

    /**
     * execBusStop
     * @param int prefId
     * @param int pointId
     * @param String pointKey
     * @param String name
     * @param List<BusRouteInformation> infos
     */
    public void execBusStop( int prefId, int pointId, String pointKey, String busStopKey, String name, List<BusRouteInformation> infos )  {
        int node_id = mNode.selectIdById( prefId, pointId );
        mNode.updateName( node_id, busStopKey, name );
        for (BusRouteInformation info : infos) {
            int com_id = mCompany.insertWhenNotExist( info.bus_operation_company );
            int route_id = mRoute.insertWhenNotExist( com_id, info );
            mLinkComNode.insertLink( com_id, node_id );
            mLinkComPref.insertWhenNotExist( com_id, prefId );
            mLinkRouteNode.insertLink( route_id, node_id );
            mLinkRoutePref.insertWhenNotExist( route_id, prefId );
        }
    }

// count
    /**
     * updatePrefNum
     * @param int prefId
     * @param int nodeNum
     * @param int routeNum
     * @param double maxLat
     * @param double minLat
     * @param double maxLon
     * @param double minLon
     */
    public void updatePrefNum( int prefId, int nodeNum, int routeNum, double maxLat, double minLat, double maxLon, double minLon ) {
        mPref.updatePrefNum( prefId, nodeNum, routeNum, maxLat,  minLat, maxLon, minLon );
    }

    /**
     * updateCompanyNum
     * @param int prefId
     * @param int nodeNum
     * @param int routeNum
     * @param double maxLat
     * @param double minLat
     * @param double maxLon
     * @param double minLon
     */
    public void updateCompanyNum( int prefId, int nodeNum, int routeNum, double maxLat, double minLat, double maxLon, double minLon ) {
        mCompany.updateCompanyNum( prefId, nodeNum, routeNum, maxLat,  minLat, maxLon, minLon );
    }

    /**
     * updateRouteNum
     * @param int id 
     * @param int nodeNum 
     * @param double maxLat 
     * @param double minLat 
     * @param double maxLon 
     * @param double minLon 
     */
    public void updateRouteNum( int id, int nodeNum, double maxLat, double minLat, double maxLon, double minLon ) { 
        mRoute.updateRouteNum( id, nodeNum, maxLat, minLat, maxLon, minLon );
    }

    /**
     * selectCompanyListAll
     * @return List<BusRecCompany>
     */
    public List<BusRecCompany> selectCompanyListAll() {
        return mCompany.selectListAll();
    }

    /**
     * selectRouteListAll
     * @return List<BusRecRoute>
     */
    public List<BusRecRoute> selectRouteListAll() {
        return mRoute.selectRouteListAll();
    }

   /**
     * selectRouteListByCompanyId
     * @param int companyId
     * @return List<BusRecRoute>
     */
    public List<BusRecRoute> selectRouteListByCompanyId( int companyId ) {
       return  mRoute.selectRouteListByCompanyId( companyId );
    }

    /**
     * selectRouteCountByPrefId
     * @param int prefId
     * @return int
     */
    public int selectRouteCountByPrefId( int prefId ) {
        return mLinkRoutePref.selectCountByPrefId( prefId );
    }

    /**
     * 
     */
    public BusRecStop selectNodeById( int id ) {
        return mNode.selectNodeById( id );
    }

    /**
     * 
     */
    public int selectNodeCountAll() {
        return mNode.selectCountAll();
    }

    /**
     * selectNodeListByPrefId
     * @param int prefId
     * @return List<BusRecStop>
     */
    public List<BusRecStop> selectNodeListByPrefId( int prefId ) {
        return mNode.selectNodeListByPrefId( prefId );
    }

    /**
     * selectNodeListByRoute
     * @param BusRecRoute route
     * @return List<BusRecStop>
     */
    public List<BusRecStop> selectNodeListByRoute( BusRecRoute route ) {
        List<BusRecRoute> listRoute = new ArrayList<BusRecRoute>();
        listRoute.add( route );
        return selectNodeListByRouteList( listRoute );
    }

    /**
     * selectNodeListByRouteList
     * @param List<BusRecRoute> listRoute
     * @return List<BusRecStop>
     */
    public List<BusRecStop> selectNodeListByRouteList( List<BusRecRoute> listRoute ) {
        int[] routeIds = getRouteIds( listRoute );
        List<BusRecLinkRouteNode> listLink = mLinkRouteNode.selectListByRouteIds( routeIds );
        List<Integer> listId = getNodeIdByLinkNode( listLink );
        List<Integer> listUniq = uniq( listId );
        int[] ids = getNodeIdsByNodeIdList( listUniq );
        List<BusRecStop> listNode = mNode.selectNodeListByIds( ids );
        return listNode;
    }

    private int[] getRouteIds( List<BusRecRoute> listRoute ) {
        int size = listRoute.size();
        int[] ids = new int[ size ];
        for (int i=0; i<size; i++) {
        	BusRecRoute rec = listRoute.get(i);
            ids[i] = rec.id;
        }
        return ids;
    }

    private List<Integer> getNodeIdByLinkNode( List<BusRecLinkRouteNode> listLink ) {
        List<Integer> listId = new ArrayList<Integer>();
        for (BusRecLinkRouteNode rec: listLink) {
           listId.add( rec.node_id );
        }
        return listId;
    }
 
    private int[] getNodeIdsByNodeIdList( List<Integer> idList ) {   
        int size = idList .size();
        int[] ids = new int[ size ];
        for (int i=0; i<size; i++) {
            ids[i] = idList.get(i);
        }
        return ids;
    }

    /**
     * updateNodeE6
     * @param int node_id
     * @param int lat_e6
     * @param int lon_e6
     */
    public void updateNodeE6( int node_id, int lat_e6, int lon_e6 ) { 
        mNode.updateE6( node_id, lat_e6, lon_e6 );
    }

// file
    /**
     * selectPrefById
     * @param int prefId
     * @return BusRecPref
     */
    public BusRecPref selectPrefById( int prefId ) {
        return mPref.selectPrefById( prefId );
    }

}
