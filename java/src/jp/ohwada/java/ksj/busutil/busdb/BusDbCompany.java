/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj.busutil.busdb;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * BusDbCompany
 * company name & unique id
 */
public class BusDbCompany extends BusDbBase {

    private static final String TABLE = "bus_company";

    private PreparedStatement mPsInsert;
    private PreparedStatement mPsUpdate;
    private PreparedStatement mPsSelectName;
    private PreparedStatement mPsSelectAll;

    private BusRecCompany mPrevRec = new BusRecCompany();

    /**
     * constrctor
     */
    public BusDbCompany() {
        super();
    }

    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        return createTable( "CREATE TABLE " + TABLE + " ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(128) NOT NULL, url_home VARCHAR(128), url_search VARCHAR(128), node_num INT, route_num INT, max_lat DOUBLE, min_lat DOUBLE, max_lon DOUBLE, min_lon DOUBLE );" );
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        try {
           mPsInsert = mConnection.prepareStatement( 
                "INSERT INTO " + TABLE + " (name) VALUES (?)" );
            mPsUpdate = mConnection.prepareStatement( 
                "UPDATE " + TABLE + " SET node_num=?, route_num=?, max_lat=?, min_lat=?, max_lon=?, min_lon=? WHERE id=?" );
           mPsSelectName = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE name=? ORDER BY id" );
           mPsSelectAll = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " ORDER BY id" );
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
            mPsUpdate.close();
            mPsSelectName.close();
            mPsSelectAll.close();
       } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insertWhenNotExist
     * @param String name
     * @return int id
     */
    public int insertWhenNotExist( String name ) {
        // same as previous 
        if ( name.equals(mPrevRec.name) ) {
            return mPrevRec.id;
        }

        // exist record
        BusRecCompany rec = selectRec( name );
        if ( rec != null ) {
            mPrevRec = rec;
            return rec.id;
        }

        // create new record if not exists
        printMsg( "Create company: " +  name );
        insert( name );
        int id = selectLastInsertId();
        mPrevRec.id = id;
        mPrevRec.name = name;
        return id;
    }

    private void insert( String name ) {
        try {
            mPsInsert.setString(1, name);
            mPsInsert.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
 
    /**
     * updateCompanyNum
     * @param int prefNo
     * @param int nodeNum
     * @param int routeNum
     * @param double maxLat
     * @param double minLat
     * @param double maxLon
     * @param double minLon
     */
    public void updateCompanyNum( int companyId, int nodeNum, int routeNum, double maxLat, double minLat, double maxLon, double minLon ) {
        printMsg( "updateCompanyNum " + companyId + " " + nodeNum + " " + routeNum  + " " + maxLat + " " + minLat + " " + maxLon + " " + minLon ); 
        PreparedStatement ps = mPsUpdate;
        try {
            ps.setInt(1, nodeNum);
            ps.setInt(2, routeNum);
            ps.setDouble(3, maxLat);
            ps.setDouble(4, minLat);
            ps.setDouble(5, maxLon);
            ps.setDouble(6, minLon);
            ps.setInt(7, companyId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * selectId
     * @param String name
     * @return int
     */
    public int selectId( String name ) {
        BusRecCompany rec = selectRec( name );
        if ( rec == null ) {
            return 0;
        }
        return rec.id;
    }
 
    private BusRecCompany selectRec( String name ) {
        List<BusRecCompany> list = selectList( name );
        if ( list.size() == 1 ) {
            return list.get(0);
        }
        if ( list.size() > 1 ) {
            // error
        }
        return null;
    }

    private List<BusRecCompany> selectList( String name ) {
        List<BusRecCompany> list = new ArrayList<BusRecCompany>();
        ResultSet rs = null;
        try {
            mPsSelectName.setString(1, name);
            rs = mPsSelectName.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ( rs == null ) return list;
        list = getRec( rs );
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * selectListAll
     * @return List<BusRecCompany>
     */
    public List<BusRecCompany> selectListAll() {
        List<BusRecCompany> list = new ArrayList<BusRecCompany>();
        ResultSet rs = null;
        try {
            rs = mPsSelectAll.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if ( rs == null ) return list;
        list = getRec( rs );
        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<BusRecCompany> getRec( ResultSet rset ) {
        List<BusRecCompany> list = new ArrayList<BusRecCompany>();
        if ( rset == null ) return list;
        BusRecCompany rec;
        try {
            while (rset.next()) {
                rec = new BusRecCompany();
                rec.id = rset.getInt("id");
                rec.name = rset.getString("name");
                rec.url_home = rset.getString("url_home");
                rec.url_search = rset.getString("url_search");
                rec.node_num = rset.getInt("node_num");
                rec.route_num = rset.getInt("route_num");
                rec.max_lat = rset.getFloat("max_lat");
                rec.min_lat = rset.getFloat("min_lat");
                rec.max_lon = rset.getFloat("max_lon");
                rec.min_lon = rset.getFloat("min_lon");
                list.add( rec );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
