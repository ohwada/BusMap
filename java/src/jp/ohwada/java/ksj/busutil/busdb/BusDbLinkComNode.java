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
 * BusDbLinkComNode
 * associate route table and node table
 */
public class BusDbLinkComNode extends BusDbBase {

    private static final String TABLE = "bus_link_com_node";

    private PreparedStatement mPsInsert;
    private PreparedStatement mPsSelect;

    /**
     * constrctor
     */
    public BusDbLinkComNode() {
        super();
    }

    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        return createTable( "CREATE TABLE " + TABLE + " ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, company_id INT, node_id INT);" );
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        try {
           mPsInsert = mConnection.prepareStatement( 
                "INSERT INTO " + TABLE + " (company_id, node_id) VALUES (?,?)" );
            mPsSelect = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE company_id=?" );
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
     * @param int companyId 
     * @return int nodeId
     */
    public void insertLink( int companyId, int nodeId )  {
        PreparedStatement ps = mPsInsert;
        try {
            ps.setInt(1, companyId);
            ps.setInt(2, nodeId );
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * selectListByComId
     * @param int companyId 
     * @return List<BusRecLinkComNode>
     */
    public List<BusRecLinkComNode> selectListByComId( int companyId ) {
        List<BusRecLinkComNode> list = new ArrayList<BusRecLinkComNode>();
        ResultSet rset = null;
        try {
        	PreparedStatement ps = mPsSelect;
            ps.setInt(1, companyId);
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
     * selectListByComIds
     * @param int[] ids
     * @return List<BusRecLinkComNode>
     */
    public List<BusRecLinkComNode> selectListByComIds( int[] ids ) {
        List<BusRecLinkComNode> list = new ArrayList<BusRecLinkComNode>();
        String str = getSqlIn( ids );
        if ( "".equals(str) ) return list;
        String sql = 
            "SELECT * FROM " + TABLE + " WHERE company_id IN (" + str + ") ORDER BY node_id";
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

    private List<BusRecLinkComNode> getRec(ResultSet rset) {
        List<BusRecLinkComNode> list = new ArrayList<BusRecLinkComNode>();
        if ( rset == null ) return list;
        BusRecLinkComNode rec;
        try {
            while (rset.next()) {
                rec = new BusRecLinkComNode();
                rec.id = rset.getInt("id");
                rec.company_id = rset.getInt("company_id");
                rec.node_id = rset.getInt("node_id");
               list.add( rec );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
