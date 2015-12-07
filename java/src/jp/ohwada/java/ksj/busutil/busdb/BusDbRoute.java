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

import jp.ohwada.java.ksj.busutil.*;

/**
 * BusDbRoute
 * name of bus stop & uniue id & Information
 * latitude and longitude of route are separated to curve table
 * one route has one or more curve data
 */
public class BusDbRoute extends BusDbBase {

    private static final String TABLE = "bus_route";

    private PreparedStatement mPsInsert;
    private PreparedStatement mPsUpdateNum;
    private PreparedStatement mPsSelectAll;
    private PreparedStatement mPsSelectBusInfo;
    private PreparedStatement mPsSelectCompany;
    private PreparedStatement mPsSelectIn;

    private BusRecRoute mPrevRec = new BusRecRoute();

    /**
     * constrctor
     */
    public BusDbRoute() {
        super();
   }

    /**
     * createTable
     * @return boolean
     */
    public boolean createTable()  {
        return createTable( "CREATE TABLE " + TABLE + " ( id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, route_key VARCHAR(256) NOT NULL, curve_key VARCHAR(256), type int, company_id INT, company VARCHAR(256), bus_line VARCHAR(512), day FLOAT, saturday FLOAT,  holiday FLOAT, remarks VARCHAR(512), num INT, max_lat DOUBLE, min_lat DOUBLE, max_lon DOUBLE, min_lon DOUBLE);" );
    }

    /**
     * prepareStatement
     */
    public void prepareStatement() {
        try {
            mPsInsert = mConnection.prepareStatement( 
                "INSERT INTO " + TABLE + " (route_key, curve_key, type, company_id, company, bus_line, day, saturday, holiday, remarks) VALUES (?,?,?,?,?,?,?,?,?,?)" );
            mPsUpdateNum = mConnection.prepareStatement( 
                "UPDATE " + TABLE + " SET num=?, max_lat=?, min_lat=?, max_lon=?, min_lon=? WHERE id=?" );
             mPsSelectAll = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " ORDER BY company_id,id" );            
             mPsSelectBusInfo = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE company_id=? AND type=? AND bus_line=? ORDER BY id" );
            mPsSelectCompany = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE company_id=? ORDER BY id" );
            mPsSelectIn = mConnection.prepareStatement( 
                "SELECT * FROM " + TABLE + " WHERE id IN (?) ORDER BY company_id,id" );
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
            mPsUpdateNum.close();
            mPsSelectBusInfo.close();
            mPsSelectCompany.close();
            mPsSelectIn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * insertWhenNotExist
     * @param int company_id
     * @param List<BusRouteInformation> infos
     * @return int id
     */
    public int insertWhenNotExist( int company_id, BusRouteInformation info ) {
        // same as previous 
        if (( company_id == mPrevRec.company_id )&&
            ( info.bus_type  == mPrevRec.bus_type )&&
            info.bus_line_name.equals(mPrevRec.bus_line) ) {
            return mPrevRec.id;
        }

        // exist record
        BusRecRoute rec = selectRec( company_id, info );
        if ( rec != null ) {
            mPrevRec = rec;
            return rec.id;
        }

        // create new record if not exists
        printMsg( "Create route: " +  info.bus_operation_company + " " + info.bus_line_name  );
        insert( company_id, info );
        int id = selectLastInsertId();
        mPrevRec.id = id;
        mPrevRec.company_id = company_id;
        mPrevRec.bus_type = info.bus_type;
        mPrevRec.bus_line = info.bus_line_name;
        return id;
    }

    private void insert( int company_id, BusRouteInformation info ) {
//        printMsg("route=" +" : "+ info.busType +" : "+ info.busOperationCompany +" : "+ info.busLineName );
       PreparedStatement ps = mPsInsert;
       try {
            ps.setString(1, info.bus_route_key);
            ps.setString(2, info.bus_curve_key);
           ps.setInt(3, info.bus_type);
           ps.setInt(4, company_id);
           ps.setString(5, info.bus_operation_company);
           ps.setString(6, info.bus_line_name);
           ps.setFloat(7, info.rate_per_day);
           ps.setFloat(8, info.rate_per_saturday);
           ps.setFloat(9, info.rate_per_holiday);
           ps.setString(10, info.remarks );
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
        printMsg( "updateRouteNum " + " " + id + " " + nodeNum + " " + maxLat + " " + minLat + " " + maxLon + " " + minLon );
        PreparedStatement ps = mPsUpdateNum;
        try {
            ps.setInt(1, nodeNum);
            ps.setDouble(2, maxLat);
            ps.setDouble(3, minLat);
            ps.setDouble(4, maxLon);
            ps.setDouble(5, minLon);
            ps.setInt(6, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * selectRouteListAll
     * @return List<BusRecRoute>
     */
    public List<BusRecRoute> selectRouteListAll() {
        List<BusRecRoute> list = new ArrayList<BusRecRoute>();
        ResultSet rset = null;
        try {
            rset = mPsSelectAll.executeQuery();
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
     * selectRouteListByCompanyId
     * @param int companyId
     * @return List<BusRecRoute>
     */
    public List<BusRecRoute> selectRouteListByCompanyId( int companyId ) {
        List<BusRecRoute> list = new ArrayList<BusRecRoute>();
        ResultSet rset = null;
        PreparedStatement ps = mPsSelectCompany;
        try {
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
     * selectId
     * @param int company_id
     * @param BusRouteInformation info
     * @return int
     */
    public int selectId( int company_id, BusRouteInformation info ) {
        BusRecRoute rec = selectRec( company_id, info );
        if ( rec == null ) {
            return 0;
        }
        return rec.id;
    }

    private BusRecRoute selectRec(  int company_id, BusRouteInformation info ) {
        List<BusRecRoute> list = selectList( company_id, info );
        if ( list.size() == 1 ) {
            return list.get(0);
        }
        if ( list.size() > 1 ) {
            System.out.println( "Error: select route " + info.bus_type + " " + info.bus_operation_company + " " +info.bus_line_name  );
        }
        return null;
    }

    private List<BusRecRoute> selectList( int company_id, BusRouteInformation info ) {
        List<BusRecRoute> list = new ArrayList<BusRecRoute>();
        ResultSet rset = null;
        PreparedStatement ps = mPsSelectBusInfo;
        try {
            ps.setInt(1, company_id);
            ps.setInt(2, info.bus_type);
            ps.setString(3, info.bus_line_name);
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

    private List<BusRecRoute> getRec( ResultSet rset ) {
        List<BusRecRoute> list = new ArrayList<BusRecRoute>();
        if ( rset == null ) return list;
        BusRecRoute rec;
        try {
            while (rset.next()) {
                rec = new BusRecRoute();
                rec.id = rset.getInt("id");
                rec.route_key = rset.getString("route_key");
                rec.curve_key = rset.getString("curve_key");
                rec.bus_type = rset.getInt("type");
                rec.company_id = rset.getInt("company_id");
                rec.company = rset.getString("company");
                rec.bus_line = rset.getString("bus_line");
                rec.rate_per_day = rset.getFloat("day");
                rec.rate_per_saturday = rset.getFloat("saturday");
                rec.rate_per_holiday = rset.getFloat("holiday");
                rec.remarks = rset.getString("remarks");
                rec.num = rset.getInt("num");
                rec.max_lat = rset.getDouble("max_lat");
                rec.min_lat = rset.getDouble("min_lat");
                rec.max_lon = rset.getDouble("max_lon");
                rec.min_lon = rset.getDouble("min_lon"); 
               list.add( rec );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

}
