/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj.busutil.busdb;

import java.sql.ResultSet;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BusDbLinkRoutePref
 * associate route table and pref table
 */
public class BusDbLinkRoutePref extends BusDbBase {

    private static final String TABLE = "bus_link_route_pref";

    private PreparedStatement mPsInsert;
    private PreparedStatement mPsSelectRoute;
    private PreparedStatement mPsSelectPref;
    private PreparedStatement mPsSelectRoutePref;
    private PreparedStatement mPsSelectCount;

    /**
     * constrctor
     */
    public BusDbLinkRoutePref() {
        super();
    }

    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        return createTable( "CREATE TABLE " + TABLE + " ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, route_id INT, pref_id INT);" );
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        try {
           mPsInsert = mConnection.prepareStatement( 
                "INSERT INTO " + TABLE + " (route_id, pref_id) VALUES (?,?)" );
            mPsSelectRoutePref = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE route_id=? AND pref_id=?" );
            mPsSelectRoute = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE route_id=?" );
            mPsSelectPref = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE pref_id=?" );
            mPsSelectCount = mConnection.prepareStatement( 
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
            mPsSelectRoute.close();
            mPsSelectPref.close();
            mPsSelectRoutePref.close();
            mPsSelectCount.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insertWhenNotExist
     * @param int route_id
     * @param int prefId
     * @return int id
     */
    public int insertWhenNotExist( int route_id, int prefId ) {
        int id = selectId( route_id, prefId );
        if (id == 0) {
            // create new record if not exists
            insert( route_id, prefId );
            id = selectLastInsertId();
        }
        return id;
    }

    /**
     * insert
     * @param int routeId 
     * @return int routeId
     */
    private void insert( int routeId, int prefId )  {
        PreparedStatement ps = mPsInsert;
        try {
            ps.setInt(1, routeId);
            ps.setInt(2, prefId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int selectId( int routeId, int prefId ) {
    	BusRecLinkRoutePref rec = selectRec( routeId, prefId );
        if ( rec == null ) {
            return 0;
        }
        return rec.id;
    }

    private BusRecLinkRoutePref selectRec( int routeId, int prefId ) {
        List<BusRecLinkRoutePref> list = selectList( routeId, prefId );
        if ( list.size() == 1 ) {
            return list.get(0);
        }
        if ( list.size() > 1 ) {
            System.out.println( "Error: select LinkRoutePref " + routeId + " " + prefId  );
        }
        return null;
    }

    public List<BusRecLinkRoutePref> selectList( int routeId, int prefId ) {
        List<BusRecLinkRoutePref> list = new ArrayList<BusRecLinkRoutePref>();
        ResultSet rset = null;
        try {
        	PreparedStatement ps = mPsSelectRoutePref;
            ps.setInt(1, routeId);
            ps.setInt(2, prefId);
            rset = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ( rset == null ) return list;
        list = getLRec( rset );
        try {
            rset.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * selectLinkRoute
     * @param int routeId 
     * @return List<BusRecLinkRoutePref>
     */
    public List<BusRecLinkRoutePref> selectLinkRoute( int routeId ) {
        List<BusRecLinkRoutePref> list = new ArrayList<BusRecLinkRoutePref>();
        ResultSet rset = null;
        try {
        	PreparedStatement ps = mPsSelectRoute;
            ps.setInt(1, routeId);
            rset = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ( rset == null ) return list;
        list = getLRec( rset );
        try {
            rset.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * selectCountByPrefId
     * @param int prefId
     * @return int
     */
    public int selectCountByPrefId( int prefId ) {
         int count = 0;
        try {
            mPsSelectCount.setInt(1, prefId);
            count = selectCount( mPsSelectCount );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * selectIdsPref
     * @param int prefId
     * @return int[]
     */
    public int[] selectIdsPref( int prefId ) {
        List<BusRecLinkRoutePref> list = selectPref( prefId );
        int size = list.size();
        int[] ids = new int[ size ];
        for( int i=0; i<size; i++ ) {
            BusRecLinkRoutePref rec = list.get(i);
            ids[i] = rec.route_id;
        }
        return ids;
    }

    private List<BusRecLinkRoutePref> selectPref( int prefId ) {
        List<BusRecLinkRoutePref> list = new ArrayList<BusRecLinkRoutePref>();
        ResultSet rset = null;
        try {
        	PreparedStatement ps = mPsSelectPref;
            ps.setInt(1, prefId);
            rset = ps.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ( rset == null ) return list;
        list = getLRec( rset );
        try {
            rset.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<BusRecLinkRoutePref> getLRec( ResultSet rset ) {
        List<BusRecLinkRoutePref> list = new ArrayList<BusRecLinkRoutePref>();
        if ( rset == null ) return list;
        BusRecLinkRoutePref rec;
        try {
            while (rset.next()) {
                rec = new BusRecLinkRoutePref();
                rec.id = rset.getInt("id");
                rec.route_id = rset.getInt("route_id");
                rec.pref_id = rset.getInt("pref_id");
               list.add( rec );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
