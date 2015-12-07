/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj.busutil.busdb;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * BusDbStop
 * name of bus stop & uniue id & latitude longitude
 */
public class BusDbStop extends BusDbBase {

    private static final String TABLE = "bus_stop";

    private PreparedStatement mPsInsert;
    private PreparedStatement mPsUpdateName;
    private PreparedStatement mPsUpdateE6;
    private PreparedStatement mPsSelectId;
    private PreparedStatement mPsSelectPref;
    private PreparedStatement mPsSelectByPrefPointKey;
    private PreparedStatement mPsSelectByPrefPointId;
    private PreparedStatement mPsSelectCountAll;
    private PreparedStatement mPsSelectCountPref;

    /**
     * constrctor
     */
    public BusDbStop() {
        super();
    }

    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        return createTable( "CREATE TABLE " + TABLE + " ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, pref_id INT, point_id INT, point_key VARCHAR(128) NOT NULL, stop_key VARCHAR(128), name VARCHAR(128), lat DOUBLE, lon DOUBLE, lat_e6 int, lon_e6 int);" );
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        try {
            mPsInsert = mConnection.prepareStatement( 
                "INSERT INTO " + TABLE + " (pref_id, point_id, point_key, lat, lon, lat_e6, lon_e6) VALUES (?,?,?,?,?,?,?)" );
            mPsUpdateName = mConnection.prepareStatement( 
                "UPDATE " + TABLE + " SET name=?, stop_key=?  WHERE id=?" );
            mPsUpdateE6 = mConnection.prepareStatement( 
                "UPDATE " + TABLE + " SET lat_e6=?, lon_e6=?  WHERE id=?" );
            mPsSelectByPrefPointKey = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE pref_id=? AND point_key=?" );
            mPsSelectByPrefPointId = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE pref_id=? AND point_id=?" );
            mPsSelectPref = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE pref_id=?" );
            mPsSelectId = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE id=?" );
            mPsSelectCountAll = mConnection.prepareStatement( 
                "SELECT COUNT(*) FROM " + TABLE );
            mPsSelectCountPref = mConnection.prepareStatement( 
                "SELECT COUNT(*) FROM " + TABLE + " WHERE pref_id=?" );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * prepareStatement
     */
    public void closeStatement() {
        try {
            mPsInsert.close();
            mPsUpdateName.close();
            mPsUpdateE6.close();
            mPsSelectId.close();
            mPsSelectPref.close();
            mPsSelectByPrefPointKey.close();
            mPsSelectByPrefPointId.close();
            mPsSelectCountAll.close();
            mPsSelectCountPref.close();
         } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insert
     * @param int prefId
     * @param int pointId
     * @param String pointKey
     * @param double lat
     * @param double lon
     * @param int lat_e6
     * @param int lon_e6
     */
    public void insert( int prefId, int pointId, String pointKey, double lat, double lon, int lat_e6, int lon_e6 ) {
        printMsg("INSERT bus_node: " + prefId + " " + pointKey + " " + lat +" "+ lon );
            PreparedStatement ps = mPsInsert;
        try { 
            ps.setInt(1, prefId); 
            ps.setInt(2, pointId);
            ps.setString(3, pointKey);
            ps.setDouble(4, lat);
            ps.setDouble(5, lon); 
            ps.setDouble(6, lat_e6);
            ps.setDouble(7, lon_e6); 
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * updateName
     * @param int id
     * @param String busStopKey
     * @param String name
     */
    public void updateName( int id, String busStopKey, String name ) {
        PreparedStatement ps = mPsUpdateName;
        try {
            ps.setString(1, name);
            ps.setString(2, busStopKey);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * updateName
     * @param int id
     * @param int lat_e6
     * @param int lon_e6
     */
    public void updateE6( int id, int lat_e6, int lon_e6 ) {
        printMsg( "updateE6 " + id + " " + lat_e6 + " " + lon_e6 );
        PreparedStatement ps = mPsUpdateE6;
        try {
            ps.setInt(1, lat_e6);
            ps.setInt(2, lon_e6);
            ps.setInt(3, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * selectCountAll
     * @return int
     */
    public int selectCountAll() {
        return selectCount( mPsSelectCountAll );
    }

    /**
     * selecNodeCount
     * @param int prefId 
     * @return int
     */
    public int selectCountByPrefId( int prefId ) {
         int count = 0;
        try {
            mPsSelectCountPref.setInt(1, prefId);
            count = selectCount( mPsSelectCountPref );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * selectRecById
     * @param int id 
     * @return BusRecStop
     */
    public BusRecStop selectNodeById( int id ) {
        List<BusRecStop> list = selectListId( id );
        if ( list.size() == 1 ) {
            return list.get(0);
        }
        if ( list.size() > 1 ) {
            // error
        }
        return null;
    }

    private List<BusRecStop> selectListId( int id ) {
        List<BusRecStop> list = new ArrayList<BusRecStop>();
        ResultSet rset = null;
        try {
            mPsSelectId.setInt(1, id);
            rset = mPsSelectId.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ( rset == null ) return list;
        list = getRec( rset );
        try {
            rset.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * selectNodeListByPrefId
     * @param int prefId
     * @return List<BusRecStop>
     */
    public List<BusRecStop> selectNodeListByPrefId( int prefId ) {
        List<BusRecStop> list = new ArrayList<BusRecStop>();
        ResultSet rset = null;
        try {
            mPsSelectPref.setInt(1, prefId);
            rset = mPsSelectPref.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ( rset == null ) return list;
        list = getRec( rset );
        try {
            rset.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * selectNodeListByIds
     * @param int[] ids
     * @return List<BusRecStop>
     */
    public List<BusRecStop> selectNodeListByIds( int[] ids ) {
        List<BusRecStop> list = new ArrayList<BusRecStop>();
        String str = getSqlIn( ids );
        if ( "".equals(str) ) return list;
        String sql = 
            "SELECT * FROM " + TABLE + " WHERE id IN (" + str + ") ORDER BY id";
        try {
            Statement stmt = mConnection.createStatement();
            ResultSet rset = stmt.executeQuery( sql );
            if ( rset != null ) {
                list = getRec( rset );
                rset.close();
            }
            stmt.close();
        } catch (SQLException e) {
            System.out.println( sql );
            e.printStackTrace();
        }
        return list;
    }

    /**
     * selectId
     * @param int prefId
     * @param String point_key
     * @return int
     */
    public int selectIdById(int prefId, int pointId ) {
        BusRecStop rec = selectRecById( prefId, pointId );
        if ( rec == null ) {
            return 0;
        }
        return rec.id;
    }

    private BusRecStop selectRecById( int prefId, int pointId ) {
        List<BusRecStop> list = selectListById( prefId, pointId );
        if ( list.size() == 1 ) {
            return list.get(0);
        }
        if ( list.size() > 1 ) {
            System.out.println( "Error: select node " + prefId + " " + pointId );
        }
        return null;
    }

    private List<BusRecStop> selectListById( int prefId, int pointId ) {
        List<BusRecStop> list = new ArrayList<BusRecStop>();
        ResultSet rset = null;
        try {
            mPsSelectByPrefPointId.setInt(1, prefId);
            mPsSelectByPrefPointId.setInt(2, pointId);
            rset = mPsSelectByPrefPointId.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ( rset == null ) return list;
        list = getRec( rset );
        try {
            rset.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * selectId
     * @param int prefId
     * @param String point_key
     * @return int
     */
    public int selectIdByKey(  int prefId, String point_key ) {
        BusRecStop rec = selectRecByKey( prefId, point_key );
        if ( rec == null ) {
            return 0;
        }
        return rec.id;
    }
 
    private BusRecStop selectRecByKey( int prefId,  String point_key ) {
        List<BusRecStop> list = selectListByKey( prefId, point_key );
        if ( list.size() == 1 ) {
            return list.get(0);
        }
        if ( list.size() > 1 ) {
            System.out.println( "Error: select node " + prefId + " " + point_key );
        }
        return null;
    }

    private List<BusRecStop> selectListByKey( int prefId, String point_key ) {
        List<BusRecStop> list = new ArrayList<BusRecStop>();
        ResultSet rset = null;
        try {
            mPsSelectByPrefPointKey.setInt(1, prefId);
            mPsSelectByPrefPointKey.setString(2, point_key);
            rset = mPsSelectByPrefPointKey.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ( rset == null ) return list;
        list = getRec( rset );
        try {
            rset.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<BusRecStop> getRec( ResultSet rset ) {
        List<BusRecStop> list = new ArrayList<BusRecStop>();
        if ( rset == null ) return list;
        BusRecStop rec;
        try {
            while (rset.next()) {
                rec = new BusRecStop();
                rec.id = rset.getInt("id");
                rec.point_key = rset.getString("point_key");
                rec.name = rset.getString("name");
                rec.lat = rset.getDouble("lat");
                rec.lon = rset.getDouble("lon");
                rec.lat_e6 = rset.getInt("lat_e6");
                rec.lon_e6 = rset.getInt("lon_e6");
                list.add( rec );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
