/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.ohwada.android.busmap.Constant;
import jp.ohwada.android.busmap.R;

/**
 * FileManager
 */ 
public class FileManager {

    // debug
    private static final String TAG_SUB = FileManager.class.getSimpleName();

    private static final String LF = "\n";
    private static final String COMMA = ",";
    private static final String EXT = ".txt";

    // 1 day
    private static final long ONE_DAY = 24 * 60 * 60 * 1000;

    private SharedPreferences mPreferences;
    private JsonParser mParser;
    private FileUtil mFile;

    private long mCacheTime = 0;
    private String mMessage = "";

    /**
     * === onCreate ===
     * @param Context context
     */
    public FileManager( Context context ) {
        mParser = new JsonParser();

        mPreferences = PreferenceManager.getDefaultSharedPreferences( context );
        String cacheDir = mPreferences.getString(
            Constant.PREF_CACHE_DIR, 
            Constant.PREF_DEFAULT_CACHE_DIR );
        int cacheDays = parseInt( mPreferences.getString(
            Constant.PREF_CACHE_DAYS, 
            Constant.PREF_DEFAULT_CACHE_DAYS ));

        mFile = new FileUtil( cacheDir );
        mCacheTime = ONE_DAY * cacheDays;
    }

    /**
     * makeSubDir
     */ 
    public void makeSubDir() {
        mFile.makeSubDir();
    }

    /**
     * clear all Cache
     */
    public void clearAllCache() {
        mFile.clearAllFiles();
    }

    /**
     * clear old Cache
     */
    public void clearOldCache() {
        int cacheFiles =  parseInt( mPreferences.getString(
            Constant.PREF_CACHE_FILES, 
            Constant.PREF_DEFAULT_CACHE_FILES ));
        if ( cacheFiles > 0 ) {
            mFile.clearOldFiles( cacheFiles );
        }
    }

    /**
     * getRoutesMultiple
     * @param NodeRecord node_rec
     * @return RoutesOnNode
     */
    public RoutesOnNode getRoutesMultiple( NodeRecord node_rec ) {
        ArrayList<RouteRecord> list_route = new ArrayList<RouteRecord>();
        ArrayList<Integer> list_request = new ArrayList<Integer>();
        for (int route_id : node_rec.route_ids) {
            RouteRecord route_rec = loadRouteRecord(route_id);
            // if missing, add request list  
            if (route_rec == null) {
                list_request.add(route_id);
                // otherwise, add route list
            } else {
                list_route.add(route_rec);
            }
        }
        RoutesOnNode res = new RoutesOnNode( list_route, list_request );
        return res;
    }

    /**
     * getComsOnRoutes
     *
     * @param List<RouteRecord> list_route
     * @return ComsOnRoutes
     */
    public ComsOnRoutes getComsOnRoutes( List<RouteRecord> list_route ) {
        ArrayList<CompanyRecord> list_com = new ArrayList<CompanyRecord>();
        ArrayList<Integer> list_request = new ArrayList<Integer>();
        Set<Integer> set = new HashSet<Integer>();
        for (RouteRecord rec : list_route) {
            int com_id = rec.company_id;
            if (!set.contains(com_id)) {
                // if not duplicate
                set.add(com_id);
                CompanyRecord com_rec = loadCompanyRecord(com_id);
                if (com_rec == null) {
                    // if missing, add request list  
                    list_request.add(com_id);
                } else {
                    // otherwise, add com list  
                    list_com.add(com_rec);
                }
            }
        }
        ComsOnRoutes res = new ComsOnRoutes( list_com, list_request );
        return res;
    }

    /**
     * parseResponseNodes
     * @param String response
     * @return List<Integer>
     */
    public List<Integer> parseResponseNodes( String response ) {
        List<Integer> list = new ArrayList<Integer>();
        JsonParser.ParseResult res = mParser.parse(response);
        if ( res.code != 1 ) {
            mMessage = res.message;
            return list;
        }
        // save data
        save(res);
        for (JsonParser.ParseRec parse_rec : res.list_node) {
            list.add(parse_rec.id);
        }
        return list;
    }

    /**
     * parseResponseRoutes
     * @param String response
     * @return List<Integer>
     */
    public List<Integer> parseResponseRoutes( String text ) {
        JsonParser.ParseResult res = mParser.parse( text );
        // save route & company data
        save( res );
        // save curve data
        List<Integer> list_id = new ArrayList<Integer>();
        List<JsonParser.ParseRec> list_curve = res.list_curve;
        List<String> list_text = null;
        CurveRecord curve_rec = null;
        String data = "";
        int id = 0;
        int size = 0;
        for( JsonParser.ParseRec parse_rec : list_curve ) {
            curve_rec = mParser.parseCurve( parse_rec.text );
            id = curve_rec.id;
            list_id.add( id );
            list_text = curve_rec.curves;
            size = list_text.size();
            if ( size > 0 ) {
                data = buildWriteCurveData( list_text );
                saveCurveData(id, data);
            }
            list_text = null; // release memory
            curve_rec = null;
            data = null;
        }
        return list_id;
    }

    /**
     * parseResponseComs
     *
     * @param String response
     */
    public void parseResponseComs( String response ) {
        JsonParser.ParseResult res = mParser.parse(response);
        // save data
        save(res);
    }

    /**
     * getListNode
     *
     * @param RouteRecord route_rec
     * @return ArrayList<NodeRecord>
     */
    public ArrayList<NodeRecord> getListNode( RouteRecord route_rec ) {
        ArrayList<NodeRecord> list_node = new ArrayList<NodeRecord>();
        for (int node_id : route_rec.node_ids) {
            NodeRecord node_rec = loadNodeRecord(node_id);
            if (node_rec != null) {
                list_node.add(node_rec);
            }
        }
        return list_node;
    }

    /**
     * getDrawReqNode
     *
     * @param RouteRecord rec
     * @return List<Integer>
     */
    public DrawReq getDrawReqNode( RouteRecord rec ) {
        ArrayList<Integer> list_draw = new ArrayList<Integer>();
        ArrayList<Integer> list_req = new ArrayList<Integer>();
        for (int id : rec.node_ids) {
            if (checkNode(id)) {
                list_draw.add(id);
            } else {
                list_req.add(id);
            }
        }
        DrawReq list = new DrawReq(list_draw, list_req);
        return list;
    }

    /**
     * getDrawReqRouteByIds
     *
     * @param List<Integer> list_id
     * @return DrawReq
     */
    public DrawReq getDrawReqRouteByIds( List<Integer> list_id ) {
        ArrayList<Integer> list_draw = new ArrayList<Integer>();
        ArrayList<Integer> list_req = new ArrayList<Integer>();
        for (int id : list_id) {
            if (checkRoute(id) && checkCurve(id)) {
                list_draw.add(id);
            } else {
                list_req.add(id);
            }
        }
        DrawReq list = new DrawReq(list_draw, list_req);
        return list;
    }

    /**
     * getDrawReqRouteByNode
     *
     * @param NodeRecord node_rec
     * @return DrawReq
     */
    public DrawReq getDrawReqRouteByNode( NodeRecord node_rec ) {
        ArrayList<Integer> list_draw = new ArrayList<Integer>();
        ArrayList<Integer> list_req = new ArrayList<Integer>();
        for (int route_id : node_rec.route_ids) {
            if (checkRoute(route_id) && checkCurve(route_id)) {
                list_draw.add(route_id);
            } else {
                list_req.add(route_id);
            }
        }
        DrawReq list = new DrawReq(list_draw, list_req);
        return list;
    }

    /**
     * checkNode
     *
     * @param int node_id
     * @return boolean
     */
    private boolean checkNode(int node_id) {
        return checkRecord( "node", node_id );
    }

    /**
     * checkRoute
     *
     * @param int route_id
     * @return boolean
     */
    private boolean checkRoute(int route_id) {
        return checkRecord( "route", route_id );
    }

    /**
     * checkCurve
     *
     * @param int route_id
     * @return boolean
     */
    private boolean checkCurve(int route_id) {
        return checkRecord( "curve", route_id );
    }

    /**
     * checkCompany
     *
     * @param int company_id
     * @return boolean
     */
    public boolean checkCompany(int company_id) {
        return checkRecord( "company", company_id );
    }

    /**
     * checkRecord
     */
    private boolean checkRecord( String name, int id ) {
        File file = getFile(name, id);
        if (!isExpired(file)) return true;
        return false;
    }

    /**
     * loadLocation
     * @param double lat
     * @param double lon
     * @param int distance
     * @param boolean is_expire
     * @return List<Integer>
     */
    public List<Integer> loadLocation(double lat, double lon, int distance, boolean is_expire) {
        List<Integer> list = new ArrayList<Integer>();
        File file = getFile( lat, lon, distance );
        if (( is_expire && isExpired(file) )|| !file.exists() ) {
            return list;
        }
        String data = mFile.read(file);
        String[] values = data.split(COMMA);
        for (String v : values) {
            list.add( parseInt(v) );        
        }
        return list;
    }

    /**
     * loadLocation
     * @param double lat
     * @param double lon
     * @param int distance
     */
    public void saveLocation( double lat, double lon, int distance, List<Integer> list ) {
        File file = getFile( lat, lon, distance ); 
        String data = TextUtils.join(COMMA, list);
        mFile.write(file, data);
    }

    /**
     * loadNodeRecord
     *
     * @param int node_id
     * @return Record or null
     * null: not exists or expired
     */
    public NodeRecord loadNodeRecord(int node_id) {
        NodeRecord rec = null;
        File file = getFile("node", node_id);
        if ( file.exists() ) {
            String data = mFile.read(file);
            rec = mParser.parseNode(data);
            data = null;  // release memory           
        }
        return rec;
    }

    /**
     * loadRouteRecord
     *
     * @param int route_id
     * @return Record or null
     * null: not exists or expired
     */
    private RouteRecord loadRouteRecord(int route_id) {
        RouteRecord rec = null;
        File file = getFile("route", route_id);
        if ( file.exists() ) {
            String data = mFile.read(file);
            rec = mParser.parseRoute(data);
            data = null;  // release memory
        }
        return rec;
    }

    /**
     * loadCompanyRecord
     *
     * @param int com_id
     * @return Record or null
     * null: not exists or expired
     */
    public CompanyRecord loadCompanyRecord(int com_id) {
        CompanyRecord rec = null;
        File file = getFile("company", com_id);
        if ( file.exists() ) {
            String data = mFile.read(file);
            rec = mParser.parseCompany(data);
            data = null;  // release memory           
        }
        return rec;
    }

    /**
     * loadCurveData
     *
     * @param int route_id
     * @return List or null
     * null: not exists or expired
     */
    public List<float[]> loadCurveData(int route_id) {
        List<float[]> list_list = null;
        File file = getFile("curve", route_id);
        if ( file.exists() ) {
            String data = mFile.read(file);
            list_list = parseCurveData(data);
            data = null; // release memory
        }
        return list_list;
    }

    /**
     * parseCurveData
     */
    private List<float[]> parseCurveData(String data) {
        List<float[]> list_list = new ArrayList<float[]>();
        if ("".equals(data)) return list_list;
        String[] lines = data.split(LF);
        for (String line : lines) {
            if ("".equals(line)) continue;
            String[] values = line.split(COMMA);
            int length = values.length;
            float[] array = new float[ length ];
            for (int i=0; i<length; i++) {
                array[i] = parseFloat( values[i] );
            }
            list_list.add(array);
            values = null; // release memory
            array = null;
        }
        lines = null; // release memory
        return list_list;
    }

    /**
     * parseFloat
     */
    private float parseFloat(String str) {
        float v = 0f;
        try {
            v = Float.parseFloat(str);
        } catch (NumberFormatException e) {
            if (Constant.DEBUG) e.printStackTrace();
        }
        return v;
    }

    /**
     * buildWriteCurveData
     */
    private String buildWriteCurveData( List<String> list ) {
        String data = "";
        for ( String str: list ) {
            data += str + LF;
        }
        return data;
    }

    /**
     * save
     *
     * @param ParseResult res
     */
    public void save(JsonParser.ParseResult res) {
        save(res.list_node, "node");
        save(res.list_route, "route");
        save(res.list_company, "company");
    }

    /**
     * save
     */
    private void save(List<JsonParser.ParseRec> list, String prefix) {
        for (JsonParser.ParseRec rec : list) {
            File file = getFile(prefix, rec.id);
            mFile.write(file, rec.text);
        }
    }

    /**
     * saveCurveData
     *
     * @param int    id
     * @param String data
     */
    public void saveCurveData(int id, String data) {
        File file = getFile("curve", id);
        mFile.write(file, data);
    }

    /**
     * getFile
     */ 
    private File getFile( String prefix, int id ) {
        String name = getName(prefix, id);
        return mFile.getFile(name);
    }

    /**
     * getFile
     */ 
    private File getFile( double lat, double lon, int distance ) {
        int lat_e3 = (int)( lat * 1E3 );
        int lon_e3 = (int)( lon * 1E3 );
        String name = "loc_" + lat_e3 + "_" + lon_e3 + "_" + distance + EXT;
        return mFile.getFile(name);
    }

    /**
     * getName
     */ 
    private String getName( String prefix, int id ) {
        String name = prefix + "_" + id + EXT;
        return name;
    }

    /**
     * isExpired
     */
    private boolean isExpired(File file) {
        return mFile.isExpired(file, mCacheTime);
    }

   /**
     * parseInt
     */
    private int parseInt(String str) {
        int v = 0;
        try {
            v = Integer.parseInt(str);
        } catch (NumberFormatException e) {
            if (Constant.DEBUG) e.printStackTrace();
        }
        return v;
    }

    /**
     * log_d
     */
    private void log_d(String str) {
        if (Constant.DEBUG) Log.d(Constant.TAG, TAG_SUB + " " + str);
    }

    /**
     * --- class RoutesOnNode ---
     */
    public class RoutesOnNode {
        public ArrayList<RouteRecord> list_route = new ArrayList<RouteRecord>();
        public ArrayList<Integer> list_request = new ArrayList<Integer>();

        /**
         * Constractor
         */
        public RoutesOnNode(ArrayList<RouteRecord> route, ArrayList<Integer> request) {
            list_route = route;
            list_request = request;
        }
    } // class end

    /**
     * --- ComsOnRoutes ---
     */
    public class ComsOnRoutes {
        public ArrayList<CompanyRecord> list_com = new ArrayList<CompanyRecord>();
        public ArrayList<Integer> list_request = new ArrayList<Integer>();

        /**
         * Constractor
         */
        public ComsOnRoutes(ArrayList<CompanyRecord> com, ArrayList<Integer> request) {
            list_com = com;
            list_request = request;
        }
    } // class end

    /**
     * --- DrawReq ---
     */
    public class DrawReq {
        public ArrayList<Integer> list_draw = new ArrayList<Integer>();
        public ArrayList<Integer> list_request = new ArrayList<Integer>();

        /**
         * Constractor
         */
        public DrawReq(ArrayList<Integer> draw, ArrayList<Integer> request) {
            list_draw = draw;
            list_request = request;
        }
    } // class end

}
