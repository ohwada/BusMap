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
 * BusDbCurve
 * latitude and longitude of bus route
 * one route has one or more curve data
 */
public class BusDbCurve extends BusDbBase {

    private static final String TABLE = "bus_curve";

    private PreparedStatement mPsInsert;
    private PreparedStatement mPsUpdateKey;
    private PreparedStatement mPsUpdateId;
    private PreparedStatement mPsSelect;
    
    /**
     * constrctor
     */
    public BusDbCurve() {
        super();
   }

    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        return createTable( "CREATE TABLE " + TABLE + " ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, route_id INT, curve_key VARCHAR(256) NOT NULL, curve MEDIUMTEXT);" );
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        try {
            mPsInsert = mConnection.prepareStatement( 
                "INSERT INTO " + TABLE + " (curve_key, curve) VALUES (?,?)" );
            mPsUpdateKey = mConnection.prepareStatement( 
                "UPDATE  " + TABLE + " SET route_id=? WHERE curve_key=?" );
            mPsUpdateId = mConnection.prepareStatement( 
                "UPDATE  " + TABLE + " SET route_id=? WHERE id=?" );
            mPsSelect = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE curve_key=?  ORDER BY id" );
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
            mPsUpdateKey.close();
            mPsUpdateId.close();
            mPsSelect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insert
     * @param String curve_key
     * @param String curve
     */
    public void insert( String curve_key, String curve ) {
        printMsg( "insert curve: " + curve_key + " " + curve );
        PreparedStatement ps = mPsInsert;
        try {
            ps.setString(1, curve_key);
            ps.setString(2, curve);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * update
     * @param String curve_key
     * @param int route_id (parent)
     */
    public void updateByKey( String curve_key, int route_id ) {
        PreparedStatement ps = mPsUpdateKey;
        try {
            ps.setInt(1, route_id);
            ps.setString(2, curve_key);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * update
     * @param int id
     * @param int route_id (parent)
     */
    public void updateById( int id, int route_id ) {
        PreparedStatement ps = mPsUpdateId;
        try {
            ps.setInt(1, route_id);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * selectId
     * @param String curve_key
     * @return int
     */
    public int selectId( String curve_key ) {
        BusRecCurve rec = selectRec( curve_key );
        if ( rec == null ) {
            return 0;
        }
        return rec.id;
    }

    private BusRecCurve selectRec(  String curve_key ) {
        List<BusRecCurve> list = selectList( curve_key );
        if ( list.size() == 1 ) {
            return list.get(0);
        }
        if ( list.size() > 1 ) {
            System.out.println( "Error: select curve " + curve_key );
        }
        return null;
    }

    private List<BusRecCurve> selectList( String curve_key ) {
        List<BusRecCurve> list = new ArrayList<BusRecCurve>();
        ResultSet rset = null;
        PreparedStatement ps = mPsSelect;
        try {
            ps.setString(1, curve_key);
            rset = ps.executeQuery();
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

    private List<BusRecCurve> getRec( ResultSet rset ) {
        List<BusRecCurve> list = new ArrayList<BusRecCurve>();
        if ( rset == null ) return list;
        BusRecCurve rec;
        try {
            while (rset.next()) {
                rec = new BusRecCurve();
                rec.id = rset.getInt("id");
                rec.curve_key = rset.getString("curve_key");
                rec.curve = rset.getString("curve");
               list.add( rec );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
