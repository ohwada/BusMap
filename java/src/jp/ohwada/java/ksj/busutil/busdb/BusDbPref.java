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
 * BusDbPref
 * name of Prefecture & unique id
 * id is defined by a file name of National Land Numerical Information
 */
public class BusDbPref extends BusDbBase {

    private static final String TABLE = "bus_pref";

    private PreparedStatement mPsUpdate;
    private PreparedStatement mPsSelect;

    /**
     * constrctor
     */
    public BusDbPref() {
        super();
    }

    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        return  createTable(
            "CREATE TABLE " + TABLE + " ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, name VARCHAR(256) NOT NULL, romaji VARCHAR(256) NOT NULL, node_num INT, route_num INT, max_lat DOUBLE, min_lat DOUBLE, max_lon DOUBLE, min_lon DOUBLE);" );
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        try {
            mPsUpdate = mConnection.prepareStatement( 
                "UPDATE " + TABLE + " SET node_num=?, route_num=?, max_lat=?, min_lat=?, max_lon=?, min_lon=? WHERE id=?" );
           mPsSelect = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE id=?" );
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * prepareStatement
     */
    public void closeStatement() {
        try {
            mPsUpdate.close();
            mPsSelect.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insert
     */
    public void insertPref() {
        PreparedStatement ps = null;
        try {
            ps = mConnection.prepareStatement( 
                "INSERT INTO " + TABLE + " (id,name,romaji) VALUES (?,?,?)" );
        } catch (SQLException e) {
            e.printStackTrace();
        }
        insert( ps, 1, "北海道", "hokkaido" );
        insert( ps, 2, "青森", "aomori" );
        insert( ps, 3, "岩手", "iwate" );
        insert( ps, 4, "宮城", "miyagi" );
        insert( ps, 5, "秋田", "akita" );
        insert( ps, 6, "山形", "yamagata" );
        insert( ps, 7, "福島", "fukushima" );
        insert( ps, 8, "茨城", "ibaraki" );
        insert( ps, 9, "栃木", "tochigi" );
        insert( ps, 10, "群馬", "gunma" );
        insert( ps, 11, "埼玉", "saitama" );
        insert( ps, 12, "千葉", "chiba" );
        insert( ps, 13, "東京", "tokyo" );
        insert( ps, 14, "神奈川", "kanagawa" );
        insert( ps, 15, "新潟", "niigata" );
        insert( ps, 16, "富山", "toyama" );
        insert( ps, 17, "石川", "ishikawa" );
        insert( ps, 18, "福井", "fukui" );
        insert( ps, 19, "山梨", "yamanashi" );
        insert( ps, 20, "長野", "nagano" );
        insert( ps, 21, "岐阜", "gifu" );
        insert( ps, 22, "静岡", "shizuoka" );
        insert( ps, 23, "愛知", "aichi" );
        insert( ps, 24, "三重", "mie" );
        insert( ps, 25, "滋賀", "shiga" );
        insert( ps, 26, "京都", "kyoto" );
        insert( ps, 27, "大阪", "osaka" );
        insert( ps, 28, "兵庫", "hyuogo" );
        insert( ps, 29, "奈良", "nara" );
        insert( ps, 30, "和歌山", "wakayama" );
        insert( ps, 31, "鳥取", "tottori" );
        insert( ps, 32, "島根", "shimane" );
        insert( ps, 33, "岡山", "okayama" );
        insert( ps, 34, "広島", "hiroshima" );
        insert( ps, 35, "山口", "yamaguchi" );
        insert( ps, 36, "徳島", "tokushima" );
        insert( ps, 37, "香川", "kagawa" );
        insert( ps, 38, "愛媛", "ehime" );
        insert( ps, 39, "高知", "kouchi" );
        insert( ps, 40, "福岡", "fukuoka" );
        insert( ps, 41, "佐賀", "saga" );
        insert( ps, 42, "長崎", "nagasaki" );
        insert( ps, 43, "熊本", "kumamoto" );
        insert( ps, 44, "大分", "oita" );
        insert( ps, 45, "宮崎", "miyazaki" );
        insert( ps, 46, "鹿児島", "kagoshima" );
        insert( ps, 47, "沖縄", "okinawa" );
        try {
            if ( ps != null ) {
                ps.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void insert( PreparedStatement ps, int id, String name, String romaji )  {
        try {
            ps.setInt(1, id);
            ps.setString(2, name);
            ps.setString(3, romaji);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * updatePrefNum
     * @param int prefNo
     * @param int nodeNum
     * @param int routeNum
     * @param double maxLat
     * @param double minLat
     * @param double maxLon
     * @param double minLon
     */
    public void updatePrefNum( int prefNo, int nodeNum, int routeNum, double maxLat, double minLat, double maxLon, double minLon ) {
        printMsg( "updatePrefNum " + prefNo + " " + nodeNum + " " + routeNum  + " " + maxLat + " " + minLat + " " + maxLon + " " + minLon ); 
        PreparedStatement ps = mPsUpdate;
        try {
            ps.setInt(1, nodeNum);
            ps.setInt(2, routeNum);
            ps.setDouble(3, maxLat);
            ps.setDouble(4, minLat);
            ps.setDouble(5, maxLon);
            ps.setDouble(6, minLon);
            ps.setInt(7, prefNo);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public BusRecPref selectPrefById( int id ) {
        List<BusRecPref> list = selectList( id );
        if ( list.size() == 1 ) {
            return list.get(0);
        }
        if ( list.size() > 1 ) {
            // error
        }
        return null;
    }

    private List<BusRecPref> selectList( int id ) {
        List<BusRecPref> list = new ArrayList<BusRecPref>();
        ResultSet rs = null;
        try {
            mPsSelect.setInt(1, id);
            rs = mPsSelect.executeQuery();
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

    private List<BusRecPref> getRec( ResultSet rset ) {
        List<BusRecPref> list = new ArrayList<BusRecPref>();
        if ( rset == null ) return list;
        BusRecPref rec;
        try {
            while (rset.next()) {
                rec = new BusRecPref();
                rec.id = rset.getInt("id");
                rec.name = rset.getString("name");
                rec.romaji = rset.getString("romaji");
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
