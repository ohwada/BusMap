/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj.busutil.busdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * BusDbBase
 */ 
public class BusDbBase {

    protected Connection mConnection;

    private Pattern mPattern;
    private boolean isDubugMsg = true;

    /**
     * constrctor
     */
    public BusDbBase() {
        mPattern = Pattern.compile( "Table.*already exists" );
    }

    /**
     * setDubugMsg
     * @param boolean flag
     */
    public void setDubugMsg( boolean flag ) {
        isDubugMsg = flag;
    }

    /**
     * setConnection
     * @param Connection con
     */
    public void setConnection( Connection con ) {
        mConnection = con;
    }

    /**
     * initConnection
     * @param String host
     * @param String port
     * @param String db
     * @param String user
     * @param String pass
     * @return boolean
     */
    protected boolean initConnection( String host, String port, String db, String user, String pass ) {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + db  + "?useUnicode=true&characterEncoding=utf8";
        Connection con = null;
        try {
            Class.forName( "com.mysql.jdbc.Driver" );
            con = DriverManager.getConnection( url, user, pass );
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        if ( con != null ) {
            mConnection = con;
            return true;
        }
        return false;
    }

    /**
     * createTable
     * @param String sql
     * @return boolean
     */
    protected boolean createTable( String sql ) {
        printMsg(sql);
        boolean flag = true;
        try {
            PreparedStatement ps = mConnection.prepareStatement(sql);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            e.printStackTrace();
            Matcher m = mPattern.matcher(e.toString());
            if (!m.find()) {
                flag = false;
            }
        }
        return flag;
    }

    /**
     * selectLastInsertId
     * @return int
     */
    protected int selectLastInsertId() {
        int id = 0;
        try {
            Statement stmt = mConnection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID() AS LAST");
            if (rs != null){
                rs.next();
                id = rs.getInt("LAST");
                rs.close();
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }

    /**
     * selectCount
     * @param PreparedStatement ps
     * @return int
     */
    protected int selectCount( PreparedStatement ps ) {
        int count = 0;
        try {
            ResultSet rs = ps.executeQuery();
            if (rs != null){
                rs.next();
                count = rs.getInt(1);
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    /**
     * getSqlIn
     * @param int[] ids
     * @return String (1,2,3,4)
     */
    protected String getSqlIn( int[] ids ) {
        int length = ids.length;
        if ( length == 0 ) return "";
        boolean isFirst = true;
        String str = "";
        for ( int i=0; i<length; i++ ) {
            if ( !isFirst ) {
                str += ",";
            }
            str += ids[i];
            isFirst = false;
        }
        return str;
    }

    /**
     * uniq
     * @param List<Integer> sList 
     * @return List<Integer>
     */
    protected List<Integer> uniq( List<Integer> sList ) {
        List<Integer> dList = new ArrayList<Integer>();
        Set<Integer> set = new HashSet<Integer>();
        for (int s : sList) {
            if (!set.contains(s)) {
                set.add(s);
                dList.add(s);
            }
        }
        return dList;
    }

    /**
     * printMsg (low level)
     * @param String str
     */
    protected void printMsg( String str ) {
        if (isDubugMsg) System.out.println( str );
    }

    /**
     * printAlways (high level)
     * @param String str
     */
    protected void printAlways( String str ) {
        if (!isDubugMsg) System.out.println();
        System.out.println( str );
    }
}
