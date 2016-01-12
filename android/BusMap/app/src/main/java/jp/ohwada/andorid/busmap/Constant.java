/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap;

/**
 * Constant
 */
public class Constant {

    public static final String HOST = "http://busmap.ohwada.jp";
    public static final String URL_USAGE = "http://android.ohwada.jp/busmap";

// debug
    public static final String TAG = "BusMap";
    public static final boolean DEBUG = true; 

    // Preferences
    public static final String PREF_GEO_NAME = "geo_name";
    public static final String PREF_GEO_LAT = "geo_lat";
    public static final String PREF_GEO_LON = "geo_lon";	
    public static final String PREF_DISTANCE = "distance";
    public static final String PREF_DISPLAY_ROUTE = "display_route";
    public static final String PREF_CACHE_DIR = "cache_dir";
    public static final String PREF_CACHE_FILES = "cache_files";
    public static final String PREF_CACHE_DAYS = "cache_days";

    public static final String PREF_DEFAULT_DISTANCE = "5";
    public static final boolean PREF_DEFAULT_DISPLAY_ROUTE = true;
    public static final String PREF_DEFAULT_CACHE_DIR = "BusMap";
    public static final String PREF_DEFAULT_CACHE_FILES = "1000";
    public static final String PREF_DEFAULT_CACHE_DAYS = "7";

    // Kannai station
    public static final float GEO_LAT = 35.443233f;
    public static final float GEO_LON = 139.637134f;
    public static final int GEO_ZOOM = 14;

}
