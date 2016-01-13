/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import jp.ohwada.android.busmap.util.FileManager;
import jp.ohwada.android.busmap.util.MessageUtil;
import jp.ohwada.android.busmap.util.NodeRecord;
import jp.ohwada.android.busmap.util.RouteRecord;
import jp.ohwada.android.busmap.util.ToastMaster;
import jp.ohwada.android.busmap.util.VolleyUtil;

import static android.os.Message.obtain;

/**
 * BusmapService
 *
 * In order to reduce the load on the UI thread
 */
public class BusmapService extends Service {

    // debug
    private static final String TAG_SUB = BusmapService.class.getSimpleName();

    public static final String PACKAGE = "jp.ohwada.android.busmap";
    public static final String ACTION = "jp.ohwada.android.busmap.BusmapService";

    private static final int CMD_NONE = 0;
    public static final int CMD_CANCEL = 1;
    public static final int CMD_LOCATION = 2;
    public static final int CMD_MARKER_DIALOG = 3;
    public static final int CMD_ROUTE_DIALOG = 4;
    public static final int CMD_NODES_ON_ROUTE = 5;
    public static final int CMD_ROUTES_ON_MARKER = 6;

    public static final int RES_MARKERS_LOC = 1;
    public static final int RES_MARKERS_ON_ROUTE = 2;
    public static final int RES_MARKER_DIALOG = 3;
    public static final int RES_ROUTE_DIALOG = 4;
    public static final int RES_ROUTE_LOC = 5;
    public static final int RES_ROUTE_ON_ROUTE = 6;
    public static final int RES_ROUTE_ON_MARKER = 7;
    public static final int RES_MSG = 8;
    public static final int RES_VOLLEY_ERROR = 9;

    public static final String KEY_MODE = "mode";
    public static final String KEY_BUNDLE = "bundle";
    public static final String KEY_LAT = "lat";
    public static final String KEY_LON = "lon";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_DRAW_BG = "draw_bg";
    public static final String KEY_NODE_ID = "node_id";
    public static final String KEY_ROUTE_ID = "route_ld";
    public static final String KEY_NODE = "node";
    public static final String KEY_ROUTE = "route";
    public static final String KEY_LIST_NODE = "list_node";
    public static final String KEY_LIST_NODE_ID = "list_node_id";
    public static final String KEY_LIST_ROUTE_ID = "list_route_id";
    public static final String KEY_PROGRESS = "progress";
    public static final String KEY_MSG = "msg";
    public static final String KEY_ERROR = "error";

    public static final int MSG_NO_NODE = 1;
    public static final int MSG_ROUTES_FINISH = 2;

    private static final int REQ_LOCATION = 1;
    private static final int REQ_NODES_ON_ROUTE = 2;
    private static final int REQ_FETCH_ROUTES = 3;
    private static final int REQ_ROUTES_LOC = 4;
    private static final int REQ_MARKER_DIALOG = 5;
    private static final int REQ_ROUTE_DIALOG = 6;

    private static final int MAX_LEN = 10;

    // Timer
    private static final int TIMER_INTERVAL = 100; // 100 msec
    private static final int TIMER_WHAT = 100;

    private Messenger mMessenger;
    private LocalBroadcastManager mLocalBroadcastManager;
    private Context mContext;
    private FileManager mFile;
    private VolleyUtil mVolley;

    private Set<Integer> mSetNodeLoc = new HashSet<Integer>();
    private List<Integer> mListRouteIdBg = new ArrayList<Integer>();
    private List<Integer> mListRouteIdOnMarker = new ArrayList<Integer>();
    private HashMap<Integer, NodeRecord> mHushNodeAll = new HashMap<Integer, NodeRecord>();
    private RouteRecord mRouteRecordOn;
    private RouteRecord mRouteRecordDialog;
    private NodeRecord mNodeRecordDialog;

    private int mCmdStatus = CMD_NONE;

    // queue
    private int mProgressTotal = 0;
    private int mProgressCount = 0;
    private LinkedList<Bundle> mQueue = new LinkedList<Bundle>();

    // location
    private double mLat = 0;
    private double mLon = 0;
    private int mDistance = 0;

    // Timer
    private boolean isTimerStart = false;
    private boolean isTimerRunning = false;

    // setting
    private boolean isRequestRouteBg = false;
    private boolean isDrawRouteBg = false;

    /**
     * === onCreate ===
     */ 
    @Override
    public void onCreate() {
        log_d( "onCreate" );
        super.onCreate();
        mContext = getApplicationContext();
        mMessenger = new Messenger( msgHandler );
        mLocalBroadcastManager = LocalBroadcastManager.getInstance( mContext );

        mVolley = new VolleyUtil( mContext );
        mVolley.setOnChangedListener(new VolleyUtil.OnChangedListener() {
            @Override
            public void onResponse(int mode, String response) {
                procResponse(mode, response);
            }
            @Override
            public void onErrorResponse(String error) {
                procErrorResponse(error);
            }
        });
        mVolley.start();

        mFile  = new FileManager( mContext );
        mFile.clearOldCache();

        // heap size
        long native_heap = Debug.getNativeHeapAllocatedSize();
        long java_heap = Runtime.getRuntime().maxMemory();
        long free_memory = Runtime.getRuntime().freeMemory();
        log_d( "NativeHeap: " + native_heap + ", JavaHeap: " + java_heap + ", FreeMemory; " + free_memory );
    }

    /**
     * === onDestroy ===
     */ 
    @Override
    public void onDestroy() {
        log_d( "onDestroy" );
        super.onDestroy();
        mVolley.stop();
    }

    /**
     * === onBind ===
     */ 
    @Override
    public IBinder onBind(Intent i) {
        log_d( "onBind" );
        return mMessenger.getBinder();
    }

    /**
     * --- msgHandler ---
     */
    private final Handler msgHandler = new Handler() {
        @Override
        public void handleMessage( Message msg ) {
            procMessage( msg );
        }
    };

    /**
     * procMessage
     */
    private void procMessage( Message msg ) {
        Bundle bundle = msg.getData();
        NodeRecord node_rec = null;
        RouteRecord route_rec = null;
        ArrayList<Parcelable> list_p_route = null;
        int what = msg.what;
        switch(what) {
            case CMD_CANCEL:
                procCancel();
                break;
            case CMD_LOCATION:
                mCmdStatus = what;
                double lat = bundle.getDouble(KEY_LAT);
                double lon = bundle.getDouble(KEY_LON);
                int distance = bundle.getInt(KEY_DISTANCE);
                boolean is_draw = bundle.getBoolean(KEY_DRAW_BG);
                procCmdLocation(lat, lon, distance, is_draw);
                break;
            case CMD_MARKER_DIALOG:
                mCmdStatus = what;
                int node_id = bundle.getInt(KEY_NODE_ID);
                ArrayList<Integer> list_route_id = bundle.getIntegerArrayList(KEY_LIST_ROUTE_ID);
                prepareMarkerDialog( node_id, list_route_id  );
                break;
            case CMD_ROUTE_DIALOG:
                mCmdStatus = what;
                node_rec = (NodeRecord) bundle.getParcelable(KEY_NODE);
                route_rec = (RouteRecord) bundle.getParcelable(KEY_ROUTE);
                prepareRouteDialog( node_rec, route_rec );
                break;
            case CMD_NODES_ON_ROUTE:
                mCmdStatus = what;
                route_rec = (RouteRecord) bundle.getParcelable(KEY_ROUTE);
                prepareNodesOnRoute( route_rec );
                break;
            case CMD_ROUTES_ON_MARKER:
                mCmdStatus = what;
                node_rec = (NodeRecord) bundle.getParcelable( KEY_NODE );
                prepareRouteseOnMarker( node_rec );
                break;
        }
    }

    /**
     * procCancel
     */ 
    private void procCancel() {
        mVolley.cancel();
        if (mCmdStatus == CMD_LOCATION) {
            stopTimer();
            mQueue.clear();
        }
        mCmdStatus = CMD_NONE;
    }

    /**
     * procCmdLocation
     */ 
    private void procCmdLocation(double lat, double lon, int distance, boolean is_draw) {
        mVolley.cancel();
        mLat = lat;
        mLon = lon;
        mDistance = distance;
        isDrawRouteBg = is_draw;
        isRequestRouteBg = is_draw;
        List<Integer> list = mFile.loadLocation(lat, lon, distance, true);
        if ( list.size() > 0 ) {
            procResponseLocation( list );
            return;
        }
        mVolley.requestLocation(REQ_LOCATION, lat, lon, distance);
    }

    /**
     * prepareMarkerDialog
     */ 
    private void prepareMarkerDialog( int node_id, ArrayList<Integer> list_route_id ) {
        mVolley.cancel();
        NodeRecord rec = mHushNodeAll.get(node_id);
        if ( rec == null ) {
            log_d("Error: not exist node on marker");
            mCmdStatus = CMD_NONE;
            return;
        }
        mNodeRecordDialog = rec;
        // get data from server
        mVolley.requestRoutes(REQ_MARKER_DIALOG, list_route_id );
    }

    /**
     * prepareRouteDialog
     */ 
    private void prepareRouteDialog( NodeRecord node_rec, RouteRecord route_rec ) {
        mVolley.cancel();
        mNodeRecordDialog = node_rec;
        mRouteRecordDialog = route_rec;
        List<Integer> list_id = new ArrayList<Integer>();
        list_id.add(route_rec.company_id);
        // get data from server
        mVolley.requestCompanies(REQ_ROUTE_DIALOG, list_id);
    }

    /**
     * prepareNodesOnRoute
     */ 
    private void prepareNodesOnRoute( RouteRecord rec ) {
        mVolley.cancel();
        mRouteRecordOn = rec;
        FileManager.DrawReq dr = mFile.getDrawReqNode( rec );            
        // draw marker
        if ( dr.list_draw.size() > 0 ) {        
            drawMarkersOnRoute(rec);
        }
        // get data from server
        if ( dr.list_request.size() > 0 ) {   
            mVolley.requestNodes( REQ_NODES_ON_ROUTE, dr.list_request );
        } else {
            mCmdStatus = CMD_NONE;
        }
    }

    /**
     * prepareRouteseOnMarker
     */ 
    private void prepareRouteseOnMarker( NodeRecord node_rec ) {
        mVolley.cancel();
        FileManager.DrawReq dr  = mFile.getDrawReqRouteByNode(node_rec);
        // draw route
        if ( dr.list_draw.size() > 0 ) {
            for( int id: dr.list_draw ) {
                sendMessageRoute( RES_ROUTE_ON_MARKER, id );
            }
        }
        // get data from server
        if ( dr.list_request.size() > 0 ) {
            mVolley.requestRoutes(REQ_FETCH_ROUTES, dr.list_request);
        } else {
            mCmdStatus = CMD_NONE;
        }
    }

    /**
     * getListNodeIdLoc
     */ 
    private ArrayList<Integer> getListNodeIdLoc() {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for(int id : mSetNodeLoc){
            list.add(id);
        }
        return list;
    }

    /**
     * procResponse
     */ 
    private void procResponse( int mode, String response ) {
        long memory = Runtime.getRuntime().freeMemory();
        log_d( "procResponse: " + mode + ", FreeMemory; " + memory );
        switch( mode ) {
            case REQ_LOCATION:
                List<Integer> list = mFile.parseResponseNodes(response);
                mFile.saveLocation( mLat, mLon, mDistance, list );
                procResponseLocation( list );
                break;
            case REQ_NODES_ON_ROUTE:
                mFile.parseResponseNodes(response);
                drawMarkersOnRoute( mRouteRecordOn );
                break;
            case REQ_FETCH_ROUTES:
                // save data, and nothing to do
                mFile.parseResponseRoutes(response );
                mCmdStatus = CMD_NONE;
                break; 
            case REQ_ROUTES_LOC:
                List<Integer> list_id = mFile.parseResponseRoutes(response );
                procResponseRoutesLoc( list_id );
                break;
            case REQ_MARKER_DIALOG:
                mFile.parseResponseRoutes(response);
                sendMessage(
                    RES_MARKER_DIALOG, 
                    mNodeRecordDialog );
                mCmdStatus = CMD_NONE;
                break;
            case REQ_ROUTE_DIALOG:
                mFile.parseResponseComs(response);
                sendMessage(
                    RES_ROUTE_DIALOG, 
                    mNodeRecordDialog, 
                    mRouteRecordDialog );
                mCmdStatus = CMD_NONE;
                break;
        }
    }

    /**
     * procErrorResponse
     */ 
    private void procErrorResponse(String error) {
        sendMessage( RES_VOLLEY_ERROR, error );
        procCancel();
    }

    /**
     * procResponseLocation
     */ 
    private void procResponseLocation( List<Integer> list_node_id ) {
        mVolley.cancel();
        // show toast, if no node
        if ( list_node_id.size() == 0 ) {
            sendMessageMsg( RES_MSG, MSG_NO_NODE );
            stopTimer();
            mQueue.clear();
            mCmdStatus = CMD_NONE;
            return;
        }
        // set hush
        ArrayList<NodeRecord> list_node = new ArrayList<NodeRecord>();
        mSetNodeLoc.clear();
        mHushNodeAll.clear();
        for( int id: list_node_id ) {
            NodeRecord rec = mFile.loadNodeRecord(id);
            if ( rec == null ) {
                log_d("Error: procResponseLocation missing " + id);
                continue;
            }
            mSetNodeLoc.add(id);
            mHushNodeAll.put(id, rec);
            list_node.add(rec);
        }
        // draw maker
        sendMessage( RES_MARKERS_LOC, list_node );
        if ( !isDrawRouteBg && !isRequestRouteBg ) {
            finishLoc();
            return;
        }
        // get routes
        List<Integer> list_route_id = MessageUtil.getListUniqueRouteId( list_node );
        FileManager.DrawReq dr = mFile.getDrawReqRouteByIds( list_route_id );
        ArrayList<Integer> list_draw = dr.list_draw;
        ArrayList<Integer> list_request = dr.list_request;
        mProgressTotal = list_route_id.size();
        mProgressCount = 0;
        // push route to queue
        if ( isDrawRouteBg &&( list_draw.size() > 0 )) {
            for ( int id: list_draw ) {
                pushQueue( id );
            }
            startTimer();
        }
        // get data from server
        if ( isRequestRouteBg &&( list_request.size() > 0 )) {
            // split process to server
            ArrayList<ArrayList<Integer>> list_list_req_split = MessageUtil.splitListId(list_request, MAX_LEN);
            mVolley.requestRoutesMultiple( REQ_ROUTES_LOC, list_list_req_split );
        }
    }

    /**
     * procResponseRoutesLoc
     */
    private void procResponseRoutesLoc(List<Integer> list_id) {
        if ( !isDrawRouteBg ) return;
        for ( int id: list_id ) {
            pushQueue( id );
        }
        startTimer();
    }

    /**
     * drawMarkersOnRoute
     */
    private void drawMarkersOnRoute( RouteRecord route_rec ) {
        // get nodes on route
        int route_id = route_rec.id;
        ArrayList<NodeRecord> list_node = mFile.getListNode( route_rec ); 
        // set hush 
        for( int i=0; i<list_node.size(); i++ ) {
            NodeRecord rec = list_node.get(i);
            if ( !mHushNodeAll.containsKey(rec.id) ) {
                mHushNodeAll.put(rec.id, rec);
            }
        }
        // draw marker
        sendMessage( RES_MARKERS_ON_ROUTE, getListNodeIdLoc(), list_node );
        // draw route
        sendMessageRoute( RES_ROUTE_ON_ROUTE, route_id );
        // pre fetch route data
        List<Integer> list_uniq = MessageUtil.getListUniqueRouteId(list_node);
        FileManager.DrawReq dr = mFile.getDrawReqRouteByIds( list_uniq );
        mVolley.requestRoutes(REQ_FETCH_ROUTES, dr.list_request);
    }

    /**
     * sendMessage
     * RES_MSG
     */
    private void sendMessageMsg( int mode, int msg ) {
        Bundle bundle = new Bundle();
        bundle.putInt( KEY_MSG, msg );
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     * RES_ROUTE_ON_ROUTE
     */
    private void sendMessageRoute( int mode, int route_id ) {
        Bundle bundle = new Bundle();
        bundle.putInt( KEY_ROUTE_ID, route_id );
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     * RES_VOLLEY_ERROR
     */
    private void sendMessage( int mode, String error ) {
        Bundle bundle = new Bundle();
        bundle.putString( KEY_ERROR, error );
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     * RES_ROUTE_LOC
     */
    private void sendMessage( int mode, int route_id, float progress ) {
        Bundle bundle = new Bundle();
        bundle.putInt( KEY_ROUTE_ID, route_id );
        bundle.putFloat( KEY_PROGRESS, progress );
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     * RES_MARKERS_LOC
     */
    private void sendMessage( int mode, ArrayList<NodeRecord> list ) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(KEY_LIST_NODE, list);
        sendMessage( mode, bundle );
    }

    /**
     * sendMessage
     * RES_MARKER_DIALOG
     */
    private void sendMessage( int mode, NodeRecord rec ) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_NODE, rec);
        sendMessage( mode, bundle );   
    }

    /**
     * sendMessage
     * RES_ROUTE_DIALOG
     */
   private void sendMessage( int mode, NodeRecord node_rec, RouteRecord route_rec ) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(KEY_NODE, node_rec);
        bundle.putParcelable(KEY_ROUTE, route_rec);
        sendMessage( mode, bundle );   
    }


    /**
     * sendMessage
     * RES_MARKERS_ON_ROUTE
     */
    private void sendMessage( int mode, ArrayList<Integer> list_node_id, ArrayList<NodeRecord> list ) {
        Bundle bundle = new Bundle();
        bundle.putIntegerArrayList(KEY_LIST_NODE_ID, list_node_id);
        bundle.putParcelableArrayList(KEY_LIST_NODE, list);
        sendMessage( mode, bundle );
    }

    /**
     * sendBroadcast
     */
    private void sendMessage( int mode, Bundle bundle ) {
        Intent intent = new Intent( ACTION );
        intent.putExtra(KEY_MODE, mode);
        intent.putExtra(KEY_BUNDLE, bundle);
        mLocalBroadcastManager.sendBroadcast(intent);
    }

// --- Timer ---
    /**
     * startTimer
     */
    private void startTimer() {
        log_d( "startTimer" );
        isTimerStart = true;
        updateTimerRunning();
    }

    /**
     * stopTimer
     */
    private void stopTimer() {
        log_d( "stopTimer" );
        isTimerStart = false;
        updateTimerRunning();
    }

    /**
     * updateTimerRunning
     */
    private void updateTimerRunning() {
        boolean running = isTimerStart;
        if (running != isTimerRunning) {
            if (running) {
                pollQueue();
                timerHandler.sendMessageDelayed(
                    obtain(timerHandler, TIMER_WHAT), TIMER_INTERVAL );
             } else {
                timerHandler.removeMessages(TIMER_WHAT);
            }
            isTimerRunning = running;
        }
    }

    /**
     * timerHandler
     */    
    private Handler timerHandler = new Handler() {
        public void handleMessage(Message m) {
            if (isTimerRunning) {
                pollQueue();
                sendMessageDelayed(
                    obtain(this, TIMER_WHAT), TIMER_INTERVAL);
            }
        }
    };

    /**
     * pushQueue
     */
    private void pushQueue( int route_id ) {
        Bundle bundle = new Bundle();
        bundle.putInt( KEY_ROUTE_ID, route_id );
        mQueue.add( bundle );
    }

    /**
     * pollQueue
     */
    private synchronized void pollQueue() {
        Bundle bundle = mQueue.poll();
        if ( bundle != null ) {
            mProgressCount ++;
            float progress = (float)mProgressCount / (float)( mProgressTotal + 1 );
            int route_id = bundle.getInt( KEY_ROUTE_ID );
            sendMessage(RES_ROUTE_LOC, route_id, progress);
            if ( mProgressCount >= mProgressTotal ) {
                finishLoc();
            }
        }
    }

    /**
     * clearStatus
     */
    private void finishLoc() {
        sendMessageMsg( RES_MSG, MSG_ROUTES_FINISH );
        stopTimer();
        mQueue.clear();
        mCmdStatus = CMD_NONE;
    }

    /**
     * toast_short
     */
    private void toast_short( String msg ) {
        ToastMaster.makeText( mContext, msg, Toast.LENGTH_SHORT ).show();
    }

    /**
     * log_d
     */
    private static void log_d(String str) {
        if (Constant.DEBUG) Log.d(Constant.TAG, TAG_SUB + " " + str);
    }

}
