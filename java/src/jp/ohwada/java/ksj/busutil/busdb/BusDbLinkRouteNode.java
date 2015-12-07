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
 * BusDbLinkRouteNode
 * associate route table and node table
 */
public class BusDbLinkRouteNode extends BusDbBase {

    private static final String TABLE = "bus_link_route_node";

    private PreparedStatement mPsInsert;
    private PreparedStatement mPsSelect;

    /**
     * constrctor
     */
    public BusDbLinkRouteNode() {
        super();
    }

    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        return createTable( "CREATE TABLE " + TABLE + " ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, route_id INT, node_id INT);" );
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        try {
           mPsInsert = mConnection.prepareStatement( 
                "INSERT INTO " + TABLE + " (route_id, node_id) VALUES (?,?)" );
            mPsSelect = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE route_id=?" );
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
            mPsSelect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insertLink
     * @param int routeId 
     * @return int nodeId
     */
    public void insertLink( int routeId, int nodeId )  {
        PreparedStatement ps = mPsInsert;
        try {
            ps.setInt(1, routeId);
            ps.setInt(2, nodeId );
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * selectListByRouteId
     * @param int routeId 
     * @return List<BusRecLinkRouteNode>
     */
    public List<BusRecLinkRouteNode> selectListByRouteId( int routeId ) {
        List<BusRecLinkRouteNode> list = new ArrayList<BusRecLinkRouteNode>();
        ResultSet rset = null;
        try {
        	PreparedStatement ps = mPsSelect;
            ps.setInt(1, routeId);
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

    /**
     * selectListByRouteIds
     * @param int[] ids
     * @return List<BusRecLinkRouteNode>
     */
    public List<BusRecLinkRouteNode> selectListByRouteIds( int[] ids ) {
        List<BusRecLinkRouteNode> list = new ArrayList<BusRecLinkRouteNode>();
        String str = getSqlIn( ids );
        if ( "".equals(str) ) return list;
        String sql = 
            "SELECT * FROM " + TABLE + " WHERE route_id IN (" + str + ") ORDER BY node_id";
        try {
            Statement stmt = mConnection.createStatement();
            ResultSet rset = stmt.executeQuery( sql );
            if ( rset != null ) {
                list = getRec( rset );
                rset.close();
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    private List<BusRecLinkRouteNode> getRec(ResultSet rset) {
        List<BusRecLinkRouteNode> list = new ArrayList<BusRecLinkRouteNode>();
        if ( rset == null ) return list;
        BusRecLinkRouteNode rec;
        try {
            while (rset.next()) {
                rec = new BusRecLinkRouteNode();
                rec.id = rset.getInt("id");
                rec.route_id = rset.getInt("route_id");
                rec.node_id = rset.getInt("node_id");
               list.add( rec );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
