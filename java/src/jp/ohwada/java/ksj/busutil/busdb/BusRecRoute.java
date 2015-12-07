/**
 * Bus Stop
 * 2015-12-01 K.OHWADA
 */

package jp.ohwada.java.ksj.busutil.busdb;

/**
 * BusRecRoute
 */ 
public class BusRecRoute {
    public int id = 0;

    // １つの路線に複数の curve データがあるので、最初の１つが登録される
    public String curve_key = "";
    public String route_key = "";

    public int bus_type = 0;
    public int company_id = 0;
    public String company = "";
    public String bus_line = "";
    public float rate_per_day = 0f;
    public float rate_per_saturday = 0f;
    public float rate_per_holiday = 0f;
    public String remarks = "";
    public int num = 0;
    public double max_lat = 0.0;
    public double min_lat = 0.0;
    public double max_lon = 0.0;
    public double min_lon = 0.0;
}
