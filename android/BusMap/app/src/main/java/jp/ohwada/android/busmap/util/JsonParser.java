/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import jp.ohwada.android.busmap.Constant;

/*
 * JsonParser
 */ 
public class JsonParser {

   // debug
    private static final boolean D = Constant.DEBUG;
    private static final String TAG_SUB = JsonParser.class.getSimpleName();

    /**
     * JsonParser
     */
    public JsonParser() {
        // dummy
    }

    /**
     * parse
     * @param String text
     * @return ParseResult
     */
    public ParseResult parse( String text ) {
        int code = -1;
        String message = "";
        JSONArray nodes = null;
        JSONArray routes = null;
        JSONArray curves = null;
        JSONArray companies = null;
        try {
            JSONObject obj = new JSONObject( text );
            code = obj.getInt("code");
            message = obj.getString("message");
            if ( obj.has("nodes") ){ 
                nodes = obj.getJSONArray("nodes");
            }
            if ( obj.has("routes") ){ 
                routes = obj.getJSONArray("routes");
            }
            if ( obj.has("companies") ){ 
                companies = obj.getJSONArray("companies");
            }
            if ( obj.has("curves") ){ 
                curves = obj.getJSONArray("curves");
            }
        } catch (JSONException e) {
            if (D) e.printStackTrace();
        }

        ParseResult res = new ParseResult();
        res.code = code;
        res.message = message;
        if ( code != 1 ) {
            log_d( "Error:" + message );
            return res;
        }

        if ( nodes != null ) {
            res.list_node = parseArray( nodes );
        }
        if ( routes != null ) {
            res.list_route = parseArray( routes );
        }
        if ( curves != null ) {
            res.list_curve = parseArray( curves );
        }
        if ( companies != null ) {
            res.list_company = parseArray( companies );
        }
        return res;
    }

    /**
     * parseArray
     */
    private List<ParseRec> parseArray( JSONArray array ) {
        List<ParseRec> list = new ArrayList<ParseRec>();
            for (int i = 0; i < array.length(); i++) {
                int id = 0;
                String str = "";
                try {
                    JSONObject obj = array.getJSONObject(i);
                    id = obj.getInt("id");
                    str = obj.toString();
                } catch (JSONException e) {
                    if (D) e.printStackTrace();
                }
                list.add( new ParseRec( id, str ) );
            }
        return list;
    }

    /**
     * parseNode
     * @param String text
     * @return NodeRecord
     */
    public NodeRecord parseNode( String text ) {
        int id = 0;
        String name = "";
        String pref = "";
        double lat = 0;
        double lon = 0;
        int[] id_array = new int[0];
        try {
            JSONObject obj = new JSONObject( text );
            id = obj.getInt("id");
            name = arrangeString( obj.getString("name"));
            pref = arrangeString( obj.getString("pref"));
            lat = obj.getDouble("lat");
            lon = obj.getDouble("lon");
            JSONArray route_ids = obj.getJSONArray("route_ids");
            id_array = parseIds( route_ids );
        } catch (JSONException e) {
            if (D) e.printStackTrace();
        }
        NodeRecord rec = new NodeRecord( id, lat, lon, name, pref, id_array );
        return rec;
    }

    /**
     * parseRoute
     * @param String text
     * @return RouteRecord
     */
    public RouteRecord parseRoute( String text ) {
        int id = 0;
        String company = "";
        String name = "";
        int company_id = 0;
        int type = 0;
        float day = 0f;
        float saturday = 0f;
        float holiday = 0f;
        String remarks = "";
        int[] id_array = new int[0];
        try {
            JSONObject obj = new JSONObject(text);
            id = obj.getInt("id");
            company = arrangeString( obj.getString("company"));
            company_id = obj.getInt("company_id");
            name = arrangeString( obj.getString("name"));
            type = obj.getInt("type");
            day = (float)obj.getDouble("day");
            saturday = (float)obj.getDouble("saturday");
            holiday = (float)obj.getDouble("holiday");
            remarks = arrangeString( obj.getString("remarks"));
            JSONArray node_ids = obj.getJSONArray("node_ids");
            id_array = parseIds( node_ids );
        } catch (JSONException e) {
            if (D) e.printStackTrace();
        }
        RouteRecord rec = new RouteRecord( id, company_id, name, company, type, day,  saturday, holiday, remarks, id_array );
        return rec;
    }

    /**
     * parseCompany
     * @param String text
     * @return CompanyRecord
     */
    public CompanyRecord parseCompany( String text ) {
        int id = 0;
        String url_home = "";
        String url_search = "";
        String name = "";
        int[] id_array = new int[0];
        try {
            JSONObject obj = new JSONObject(text);
            id = obj.getInt("id");
            url_home = arrangeString( obj.getString("url_home"));
            url_search = arrangeString( obj.getString("url_search"));
            name = arrangeString( obj.getString("name"));
        } catch (JSONException e) {
            if (D) e.printStackTrace();
        }
        CompanyRecord rec = new CompanyRecord( id, name, url_home, url_search );
        return rec;
    }

    /**
     * parseCurve
     * @param String text
     * @return CurveRecord
     */
    public CurveRecord parseCurve( String text ) {
        int id = 0;
        List<String> list = new ArrayList<String>();
        try {
            JSONObject obj = new JSONObject( text );
            id = obj.getInt("id");
            JSONArray curves = obj.getJSONArray("curves");
            list = parseCurves( curves );
        } catch (JSONException e) {
            if (D) e.printStackTrace();
        }
        CurveRecord rec = new CurveRecord( id, list );
        list = null;  // release memory
        return rec;
    } 

    /**
     * parseIds
     */
    private int[] parseIds( JSONArray ids ) {
        int len = ids.length();
        int[] array = new int[ len ];
        try {
            for (int i = 0; i < len; i++) {
                int id  = ids.getInt(i);
                array[i] = id;
            }
        } catch (JSONException e) {
            if (D) e.printStackTrace();
        }
        return array;
    }

    /**
     * parseCurve
     */
    private  List<String> parseCurves( JSONArray curves ) {
        List<String> list_list = new ArrayList<String>();
        if ( curves == null ) return list_list;
        JSONArray curve = null;
        String str1 = "";
        String str2 = "";
        String str3 = "";
        try {
            for (int i = 0; i <  curves.length(); i++) {
                curve = curves.getJSONArray(i);
                str1 = curve.toString();
                // [35.444494,139.634508] -> 35.444494,139.634508
                str2 = str1.replace( "[", "" );
                str3 = str2.replace( "]", "" );
                list_list.add( str3 );
            }
        } catch (JSONException e) {
            if (D) e.printStackTrace();
        }
        curve = null;  // release memory
        str1 = null;
        str2 = null;
        str3 = null;
        return list_list;
    }

    /**
     * arrangeString
     */
    private String arrangeString( String str ) {
        if ( str == null ) {
            return "";
        }
        if ( "null".equals(str) ) {
            return "";
        }
        return str;
    }

    /**
     * log_d
     */
    private void log_d( String str ) {
        if (Constant.DEBUG) Log.d( Constant.TAG, TAG_SUB + " " + str );
    }

    /**
     * --- class ParseResult ---
     */
    public class ParseResult {
        public int code = 0;
        public String message = "";
        public List<ParseRec> list_route = new ArrayList<ParseRec>();
        public List<ParseRec> list_node = new ArrayList<ParseRec>();
        public List<ParseRec> list_company = new ArrayList<ParseRec>();
        public List<ParseRec> list_curve = new ArrayList<ParseRec>();
    } // class end

    /**
     * --- class ParseRec ---
     */
    public class ParseRec {
        public int id = 0;
        public String text = "";

        /**
         * Constructor
         */
        public ParseRec( int _id, String _text ) {
            id = _id;
            text = _text;
        }
    } // class end

}
