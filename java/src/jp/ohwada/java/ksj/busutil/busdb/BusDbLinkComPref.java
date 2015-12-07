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
 * BusDbLinkComPref
 * associate company table and pref table
 */
public class BusDbLinkComPref extends BusDbBase {

    private static final String TABLE = "bus_link_com_pref";

    private PreparedStatement mPsInsert;
    private PreparedStatement mPsSelectCom;
    private PreparedStatement mPsSelectPref;
    private PreparedStatement mPsSelectComPref;
    private PreparedStatement mPsSelectCount;

    /**
     * constrctor
     */
    public BusDbLinkComPref() {
        super();
    }

    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        return createTable( "CREATE TABLE " + TABLE + " ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, company_id INT, pref_id INT);" );
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        try {
           mPsInsert = mConnection.prepareStatement( 
                "INSERT INTO " + TABLE + " (company_id, pref_id) VALUES (?,?)" );
            mPsSelectComPref = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE company_id=? AND pref_id=?" );
            mPsSelectCom = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE company_id=?" );
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
            mPsSelectCom.close();
            mPsSelectPref.close();
            mPsSelectComPref.close();
            mPsSelectCount.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insertWhenNotExist
     * @param int company_id
     * @param int prefId
     * @return int id
     */
    public int insertWhenNotExist( int company_id, int prefId ) {
        int id = selectId( company_id, prefId );
        if (id == 0) {
            // create new record if not exists
            insert( company_id, prefId );
            id = selectLastInsertId();
        }
        return id;
    }

    /**
     * insert
     * @param int companyId 
     * @param int prefId
     */
    private void insert( int companyId, int prefId )  {
        PreparedStatement ps = mPsInsert;
        try {
            ps.setInt(1, companyId);
            ps.setInt(2, prefId);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int selectId( int companyId, int prefId ) {
    	BusRecLinkComPref rec = selectRec( companyId, prefId );
        if ( rec == null ) {
            return 0;
        }
        return rec.id;
    }

    private BusRecLinkComPref selectRec( int companyId, int prefId ) {
        List<BusRecLinkComPref> list = selectList( companyId, prefId );
        if ( list.size() == 1 ) {
            return list.get(0);
        }
        if ( list.size() > 1 ) {
            System.out.println( "Error: select LinkComPref " + companyId + " " + prefId  );
        }
        return null;
    }

    public List<BusRecLinkComPref> selectList( int companyId, int prefId ) {
        List<BusRecLinkComPref> list = new ArrayList<BusRecLinkComPref>();
        ResultSet rset = null;
        try {
        	PreparedStatement ps = mPsSelectComPref;
            ps.setInt(1, companyId);
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
     * selectLinkCom
     * @param int companyId 
     * @return List<BusRecLinkComPref>
     */
    public List<BusRecLinkComPref> selectLinkCom( int companyId ) {
        List<BusRecLinkComPref> list = new ArrayList<BusRecLinkComPref>();
        ResultSet rset = null;
        try {
        	PreparedStatement ps = mPsSelectCom;
            ps.setInt(1, companyId);
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
        List<BusRecLinkComPref> list = selectPref( prefId );
        int size = list.size();
        int[] ids = new int[ size ];
        for( int i=0; i<size; i++ ) {
            BusRecLinkComPref rec = list.get(i);
            ids[i] = rec.company_id;
        }
        return ids;
    }

    private List<BusRecLinkComPref> selectPref( int prefId ) {
        List<BusRecLinkComPref> list = new ArrayList<BusRecLinkComPref>();
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

    private List<BusRecLinkComPref> getLRec( ResultSet rset ) {
        List<BusRecLinkComPref> list = new ArrayList<BusRecLinkComPref>();
        if ( rset == null ) return list;
        BusRecLinkComPref rec;
        try {
            while (rset.next()) {
                rec = new BusRecLinkComPref();
                rec.id = rset.getInt("id");
                rec.company_id = rset.getInt("company_id");
                rec.pref_id = rset.getInt("pref_id");
               list.add( rec );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
