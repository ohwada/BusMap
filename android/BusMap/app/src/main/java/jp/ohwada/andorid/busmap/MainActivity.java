/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Parcelable;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jp.ohwada.andorid.busmap.util.CompanyRecord;
import jp.ohwada.andorid.busmap.util.DelayUtil;
import jp.ohwada.andorid.busmap.util.FileManager;
import jp.ohwada.andorid.busmap.util.MessageUtil;
import jp.ohwada.andorid.busmap.util.NodeRecord;
import jp.ohwada.andorid.busmap.util.RouteRecord;
import jp.ohwada.andorid.busmap.util.ToastMaster;

/**
 * MainActivity
 */ 
public class MainActivity extends Activity
    implements ServiceConnection {

    /* Intent request codes */
    private static final int REQUEST_SETTING = 1;
    private static final int REQUEST_MAP_SETTING = 2;

    private static final int DELAY_TIME_FILE = 1000; 	// 1 sec
    private static final int DELAY_TIME_MARKER = 1000; 	// 1 sec
    private static final int NOT_MATCH_MARKER = -1;

    private GoogleMap mMap;
    private SharedPreferences mPreferences;
    private LocalBroadcastManager mLocalBroadcastManager;
    private Messenger mMessenger;
    private FileManager mFile;
    private DelayUtil mDelayUtil;
    private DelayUtil mDelayMarker;

    // view
    private View mView;
    private Button mButtonMove;
    private OptionDialog mOptionDialog;
    private BusmapProgressDialog mProgressDialog;

    private HashMap<Integer, Marker> mHushMarkerAll = new HashMap<Integer, Marker>();
    private List<Polyline> mListPolylineBg = new ArrayList<Polyline>();
    private List<Polyline> mListPolylineOn = new ArrayList<Polyline>();

    // setting
    private float mOptionLat = Constant.GEO_LAT;
    private float mOptionLon = Constant.GEO_LON;
    private int mDistance = 5;
    private boolean isDrawRouteBg = true;

    // map
    private Marker mMarkerCross;
    private BitmapDescriptor mIconBus;
    private BitmapDescriptor mIconCross;

    /**
     * === onCreate ===
     */ 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        log_d( "onCreate" );
        super.onCreate(savedInstanceState);
        mView = getLayoutInflater().inflate( R.layout.activity_main, null ); 
        setContentView(mView);

        Button btnGet = (Button) findViewById( R.id.Button_get );
        btnGet.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showProgressDialog();
                sendMessage(BusmapService.CMD_LOCATION, mMap.getCameraPosition());
            }
        });

        mButtonMove = (Button) findViewById( R.id.Button_move );
        mButtonMove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showOptionDialog();
            }
        });

        mOptionDialog = new OptionDialog(this);
        mOptionDialog.setOnChangedListener(
            new OptionDialog.OnChangedListener() {
            public void onButtonClick() {
                moveToPointAndMarker(mOptionLat, mOptionLon);
            }
            public void onGeocoderFinished( double lat, double lon ) {
                moveToPointAndMarker( lat, lon );
                toast_short(R.string.search_found);
            }
            public void onGpsChanged(double lat, double lon) {
                moveToPointAndMarker(lat, lon);
            }
        });

        mProgressDialog = new BusmapProgressDialog( this );
        mProgressDialog.setCancelable( false );  
        mProgressDialog.setOnKeyListener( new DialogInterface.OnKeyListener() {
            public boolean onKey( DialogInterface dialog, int id, KeyEvent key) {
                log_d( "ProgressDialog Key " + key.getKeyCode() );
                sendMessage(BusmapService.CMD_CANCEL);
                mProgressDialog.dismiss();
                return true; 
            }  
        });

        mDelayMarker = new DelayUtil();
        mDelayMarker.setOnChangedListener( new DelayUtil.OnChangedListener() {
            @Override
            public void onEvent() {
                moveMarkerCrossToCenter();
            }
        });

        mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mFile = new FileManager( this );
        mFile.makeSubDir();

        setupMap();
        connectService();
   }

    /**
     * === onStart ===
     */
    @Override
    protected void onStart() {
        log_d("onStart");
        super.onStart();
        registerReceiver();
    }

    /**
     * === onResume ===
     */
    @Override
    protected void onResume() {
        log_d( "onResume" );
        super.onResume();
        initPref();
        setupMap();
        connectService();
    }

    /**
     * === onPause ===
     */
    @Override
    protected void onPause() {
        log_d("onPause");
        super.onPause();
        mOptionDialog.cancel();
        sendMessage(BusmapService.CMD_CANCEL);
    }

    /**
     * === onStop ===
     */
    @Override
    protected void onStop() {
        log_d("onStop");
        super.onStop();
        unregisterReceiver();
    }

    /**
     * === onDestroy ===
     */
    @Override
    protected void onDestroy() {
        log_d( "onDestroy" );
        super.onDestroy();
        unregisterReceiver();
        disconnectService();
        mOptionDialog.destroy();
    }

    /**
     * === onCreateOptionsMenu ===
     */
    @Override
    public boolean onCreateOptionsMenu( Menu menu ) {
        log_d( "onCreateOptionsMenu" );
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
 
    /**
     * === onOptionsItemSelected ===
     */
    @Override
    public boolean onOptionsItemSelected( MenuItem item ) {
        log_d( "onOptionsItemSelected" );
        int id = item.getItemId();
        if ( id == R.id.menu_about ) {
            showAboutDialog();
        } else if ( id == R.id.menu_usage ) {
            startBrawser( Constant.URL_USAGE );
        } else if ( id == R.id.menu_center ) {
            showHideCenterMarker();
        } else if ( id == R.id.menu_setting ) {
            startSetting();
        } else if ( id == R.id.menu_map_setting ) {
            startMapSetting();
        } else if ( id == R.id.menu_clear ) {
            mFile.clearAllCache();
        }
        return true;
    }

    /**
     * === onActivityResult ===
     */
    @Override
    public void onActivityResult( int request, int result, Intent data ) {
        log_d( "onActivityResult" );
        // dummy
    }

    /**
     * === dispatchTouchEvent ===
     */
    @Override
    public boolean dispatchTouchEvent( MotionEvent event ) {
//        log_d( "dispatchTouchEvent" );
        // show marker in center, if finger is move up from a screen
        moveMarkerCrossToCenter();
        mDelayMarker.start(DELAY_TIME_MARKER);
        // dont return true
        return super.dispatchTouchEvent( event );
    }

    /**
     * === onServiceConnected ===
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        log_d("onServiceConnected");
        mMessenger = new Messenger(service);
    }

    /**
     * === onServiceDisconnected ===
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        log_d("onServiceDisconnected");
        mMessenger = null;
    }

    /**
     * connectService
     */
    private void connectService() {
        if ( mMessenger != null ) return;
        // Service Intent must be explicit
        Intent intent = new Intent(BusmapService.ACTION);
        intent.setPackage(BusmapService.PACKAGE);
        bindService(intent, this, Context.BIND_AUTO_CREATE);
    }

    /**
     * disconnectService
     */
    private void disconnectService() {
        if ( mMessenger != null ) {
            unbindService(this);
        }
        mMessenger = null;
    }

    /**
     * registerReceiver
     */
    private void registerReceiver() { 
        mLocalBroadcastManager.registerReceiver(
            mReceiver, new IntentFilter(BusmapService.ACTION));
    }

    /**
     * unregisterReceiver
     */ 
    private void unregisterReceiver() { 
        mLocalBroadcastManager.unregisterReceiver(mReceiver);
    }

    /**
     * initPref
     */
    private void initPref() {
        mOptionLat = mPreferences.getFloat(
            Constant.PREF_GEO_LAT, Constant.GEO_LAT);
        mOptionLon = mPreferences.getFloat(
            Constant.PREF_GEO_LON, Constant.GEO_LON);
        mDistance = parseInt( mPreferences.getString(
            Constant.PREF_DISTANCE, 
            Constant.PREF_DEFAULT_DISTANCE ));
        isDrawRouteBg = mPreferences.getBoolean(
            Constant.PREF_DISPLAY_ROUTE,
            Constant.PREF_DEFAULT_DISPLAY_ROUTE );
    }

    /**
     * setupMap
     */
    private void setupMap() {
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.main_map))
                    .getMap();
            mMap.setOnMarkerClickListener(
                new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick( Marker marker ) {
                        // sow title
                        return false;
                    }
            });
            mMap.setOnInfoWindowClickListener(
                new GoogleMap.OnInfoWindowClickListener() {
                    @Override
                    public void onInfoWindowClick( Marker marker ) {
                        prepareMarkerDialog( marker );
                    }
            });
            moveToPointAndMarker( mOptionLat, mOptionLon );
        }
        if (mIconBus == null) {
            mIconBus = BitmapDescriptorFactory.fromResource(R.drawable.marker_bus);
        }
        if (mIconCross == null) {
            mIconCross = BitmapDescriptorFactory.fromResource(R.drawable.marker_cross);
        }
    }

    /**
     * moveToPoint and drawMarker
     */
    private void moveToPointAndMarker( double lat, double lon ) {
        moveToPoint( lat, lon, Constant.GEO_ZOOM );
        if ( mMarkerCross == null ) {
            drawMarkerCross(lat, lon);
        } else {
            moveMarkerCrossToCenter();
        }
    }

    /**
     * moveToPoint
     */
    private void moveToPoint( double lat, double lon, int zoom ) {
        CameraUpdate cu =
            CameraUpdateFactory.newLatLngZoom( 
            new LatLng(lat, lon), zoom );
        mMap.moveCamera(cu);
    }

    /**
     * redrawMap
     */
    private void redrawMap() {
        //  Map を再描画しようと思ったが、効かないようだ
        CameraPosition pos = mMap.getCameraPosition();
        moveToPoint(pos.target.latitude, pos.target.longitude, (int)pos.zoom);
    }

    /**
     * matchMaker
     */
    private int matchMaker( Marker marker ) {
        if ( mHushMarkerAll == null ) return NOT_MATCH_MARKER;
        for ( int id : mHushMarkerAll.keySet() ) {
            if ( marker.equals( mHushMarkerAll.get(id) ) ) {
                return id;
            }
        }
        return NOT_MATCH_MARKER;
    }

    /**
     * sendMessage
     * CMD_CANCEL
     */ 
    private void sendMessage( int mode) {
        Bundle bundle = new Bundle();
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     * CMD_LOCATION
     */ 
    private void sendMessage( int mode, CameraPosition pos ) {
        Bundle bundle = new Bundle();
        bundle.putDouble(BusmapService.KEY_LAT, pos.target.latitude);
        bundle.putDouble(BusmapService.KEY_LON, pos.target.longitude);
        bundle.putInt(BusmapService.KEY_DISTANCE, mDistance);
        bundle.putBoolean(BusmapService.KEY_DRAW_BG, isDrawRouteBg);
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     * CMD_MARKER_DIALOG
     */
    private void sendMessage( int mode, int node_id, ArrayList<Integer> list_route_id ) {
        Bundle bundle = new Bundle();
        bundle.putInt(BusmapService.KEY_NODE_ID, node_id);
        bundle.putIntegerArrayList(BusmapService.KEY_LIST_ROUTE_ID, list_route_id);
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     * CMD_ROUTE_DIALOG
     */
    private void sendMessage( int mode, NodeRecord node_rec, RouteRecord route_rec) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BusmapService.KEY_NODE, node_rec);
        bundle.putParcelable(BusmapService.KEY_ROUTE, route_rec);
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     * CMD_NODES_ON_ROUTE
     */
    private void sendMessage( int mode,  RouteRecord rec ) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BusmapService.KEY_ROUTE, rec);
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     * CMD_ROUTES_ON_MARKER
     */
    private void sendMessage( int mode,  NodeRecord rec ) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(BusmapService.KEY_NODE, rec);
        sendMessage(mode, bundle);
    }

    /**
     * sendMessage
     */
    private void sendMessage( int mode,  Bundle bundle ) {
        if ( mMessenger == null ){
            log_d("Error: Messenger is null");
            return;
        }
        Handler handler = null;
        Message msg = Message.obtain(handler, mode);
        msg.setData(bundle);
        try {
            mMessenger.send(msg);
        } catch (RemoteException e) {
            if (Constant.DEBUG) e.printStackTrace();
        }
    }

// --- response from service ---
    /**
     * --- BroadcastReceiver ---
     */ 
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive( Context context, Intent intent ) {
            procReceive(intent);
        }
    };

    /**
     * procMessage
     */
    private void procReceive( Intent intent ) {
        int mode = intent.getIntExtra( BusmapService.KEY_MODE, 0 );
        Bundle bundle = intent.getBundleExtra(BusmapService.KEY_BUNDLE); 
        int route_id = bundle.getInt(BusmapService.KEY_ROUTE_ID);
        Parcelable node_p_rec = bundle.getParcelable(BusmapService.KEY_NODE);
        ArrayList<Parcelable> list_p_node =
            bundle.getParcelableArrayList(BusmapService.KEY_LIST_NODE);
        switch ( mode ) {
            case BusmapService.RES_MARKERS_LOC:
                procMarkersLoc( 
                    MessageUtil.convListNode(list_p_node) );
                break;
            case BusmapService.RES_MARKERS_ON_ROUTE:
                ArrayList<Integer> list_node_id =
                    bundle.getIntegerArrayList( BusmapService.KEY_LIST_NODE_ID );
                procMarkerOnRoute( 
                    list_node_id,
                    MessageUtil.convListNode(list_p_node) );
                break;
            case BusmapService.RES_MARKER_DIALOG:
                showMarkerDialog(
                    (NodeRecord) node_p_rec,
                    null );
                break;
            case BusmapService.RES_ROUTE_DIALOG:
                Parcelable route_p_rec = bundle.getParcelable(BusmapService.KEY_ROUTE);
                showRouteDialog(
                    (NodeRecord) node_p_rec,
                    (RouteRecord) route_p_rec,
                    null,
                    null );
                break;
            case BusmapService.RES_ROUTE_LOC:
                float progress = bundle.getFloat(BusmapService.KEY_PROGRESS);
                drawRouteLoc(route_id, progress);
                break;
            case BusmapService.RES_ROUTE_ON_ROUTE:
            case BusmapService.RES_ROUTE_ON_MARKER:
                drawRouteOn(route_id);
                break;
            case BusmapService.RES_MSG:
                int msg = bundle.getInt(BusmapService.KEY_MSG);
                procMsg( msg );
                break;
            case BusmapService.RES_VOLLEY_ERROR:
                String error = bundle.getString(BusmapService.KEY_ERROR);
                mProgressDialog.dismiss();
                String str = getString(R.string.toast_volley_error, error);
                toast_short(str);
                break;                
        }

    }

    /**
     * procMarkersLoc
     */
    private void procMarkersLoc( ArrayList<NodeRecord> list_node ) {
        clearPrevPolylineBg();
        clearPrevPolylineOn();
        clearMarkersAll();
        drawNewMarkers(list_node );
        redrawMap();
    }

    /**
     * procMarkerOnRoute
     */
    private void procMarkerOnRoute( ArrayList<Integer> list_node_id, ArrayList<NodeRecord> list_node ) {
        clearPrevMarkersOnMarker(list_node_id);
        drawNewMarkers(list_node);
        redrawMap();
    }

    /**
     * procMsg
     */
    private void procMsg( int msg ) {
        switch(msg) {
            case BusmapService.MSG_NO_NODE:
                toast_short(R.string.toast_no_node);
                break;
            case BusmapService.MSG_ROUTES_FINISH:
                mProgressDialog.dismiss();
                log_d("procMsg finish");
                break;
        }
    }

    /**
     * showHideCenterMarker
     */
    private void showHideCenterMarker() {
        if ( mMarkerCross == null ) {
            CameraPosition pos = mMap.getCameraPosition();
            drawMarkerCross(pos.target.latitude, pos.target.longitude);
        } else {
            mMarkerCross.remove();
            mMarkerCross = null;
        }
    }

    /**
     * drawMarkerCross
     */
    private void drawMarkerCross( double lat, double lon ) {
        mMarkerCross = drawMarker( lat, lon, "", mIconCross );
    }

    /**
     * moveMarkerCrossToCenter
     */
    private void moveMarkerCrossToCenter() {
        if ( mMarkerCross == null ) return;
        CameraPosition pos = mMap.getCameraPosition();
        mMarkerCross.setPosition(pos.target);
    }

    /**
     * clearMarkersAll
     */
    private void clearMarkersAll() {
        for ( int id : mHushMarkerAll.keySet() ) {
            Marker marker = mHushMarkerAll.get(id);
            marker.remove();
            marker = null;
        }
        mHushMarkerAll.clear();
    }

    /**
     * clearPrevMarkersOnMarker
     */
    private void clearPrevMarkersOnMarker( ArrayList<Integer> list_node_id) {
        Set<Integer> set = new HashSet<Integer>();
        for( int id: list_node_id ) {
            set.add(id);
        }
        List<Integer> list = new ArrayList<Integer>();
        for ( int id : mHushMarkerAll.keySet() ) {
            // remove marker, if not contains markers in location
            if ( !set.contains(id) ) {
                Marker marker = mHushMarkerAll.get(id);
                marker.remove();
                list.add(id);
            }
        }
        // java.util.ConcurrentModificationException
        // In order to avoid an exception occurs, 
        // divide this process
        for ( int id: list ) {
            mHushMarkerAll.remove(id);
        }
    }

    /**
     * drawNewMarkers
     */
    private void drawNewMarkers( List<NodeRecord> list ) {
        log_d("drawNewMarkers " + list.size());
        for( int i=0; i<list.size(); i++ ) {
            NodeRecord rec = list.get(i);
            if ( rec == null ) continue;
            int id = rec.id;
            // add, if not exists 
            if ( !mHushMarkerAll.containsKey( id )) {
                Marker marker = drawMarker( rec.lat, rec.lon, rec.name, mIconBus );
                mHushMarkerAll.put( id, marker );
            }
        }
    }

    /**
     * drawMarker
     */
    private Marker drawMarker( double lat, double lon, String title, BitmapDescriptor icon  ) {
        Marker marker = mMap.addMarker(
            new MarkerOptions()
                .position(new LatLng(lat, lon))
                .icon(icon)
        );
        if ( title.length() > 0 ) {
            marker.setTitle( title );
        }
        return marker;
    }

    /**
     * clearPrevPolylineBg
     */
    private void clearPrevPolylineBg() {
        if ( mListPolylineBg == null ) return;
        for ( Polyline line: mListPolylineBg ) {       
            line.remove();
        }
        mListPolylineBg.clear();
    }

    /**
     * clearPrevPolylineOn
     */
    private void clearPrevPolylineOn() {
        if ( mListPolylineOn == null ) return;
        for ( Polyline line: mListPolylineOn ) {       
            line.remove();
        }
        mListPolylineOn.clear();
    }

    /**
     * drawRouteLoc
     */
    private void drawRouteLoc( int route_id, float progress ) {
        log_d("drawRouteLoc " + route_id + " " + progress);
        List<Polyline> list = drawRoute( route_id, 5, Color.BLUE );
        for (Polyline line : list) {
            mListPolylineBg.add(line);
        }
        mProgressDialog.setProgress( progress );
    }

    /**
     * drawRouteon
     */
    private void drawRouteOn( int route_id ) {
        List<Polyline> list = drawRoute( route_id, 10, Color.RED );
        for (Polyline line : list) {
            mListPolylineOn.add(line);
        }
    }

    /**
     * drawRoute
     */
    private List<Polyline> drawRoute( int route_id, int width, int color ) {
        List<Polyline> list_poly = new ArrayList<Polyline>();
        List<float[]> list_list_curve = mFile.loadCurveData(route_id);
        if ((list_list_curve == null) || (list_list_curve.size() == 0)) {
            log_d("drawRoute missing " + route_id );
            return list_poly;
        }
        for (float[] array : list_list_curve) {
            Polyline line = drawPolyline(route_id, array, width, color);
            list_poly.add(line);
        }
        return list_poly;
    }

    /**
     * drawPolyline
     */
    private Polyline drawPolyline( int id, float[] array, int width, int color ) {
        // log_d("drawPolyline " + id + " " + array.length);
        return drawPolyline( 
            getPolylinePoints( array ), width, color );
    }

    /**
     * drawPolyline
     */
    private Polyline drawPolyline( List<LatLng> points, int width, int color ) {
        Polyline line = mMap.addPolyline( 
            new PolylineOptions()
                .addAll( points )
                .width( width )
                .color( color ));
        return line;
    }

    /**
     * getPolylinePoints
     */
    private List<LatLng> getPolylinePoints( float[] array ) {
        List<LatLng> list_latlng = new ArrayList<LatLng>();
        int length = array.length;
        if ( length == 0 ) return list_latlng;
        for ( int i=0; i < (length/2); i++ ) {
            double lat = (double) array[ 2*i ];
            double lng = (double) array[ 2*i + 1 ];
            list_latlng.add( new LatLng( lat, lng ) );
        }
        return list_latlng;
    }

// --- dialog ---
    /**
     * show Progress Dialog
     */
    private void showProgressDialog() {
        mProgressDialog.create();
        mProgressDialog.show();
    }

    /**
     * showOptionDialog
     */
    private void showOptionDialog() {
        mOptionDialog.create();
        mOptionDialog.show();
    }

    /**
     * showAboutDialog
     */
    private void showAboutDialog() {
        AboutDialog dialog = new AboutDialog( this );
        dialog.create();
        dialog.show();
    }

    /**
     * showMarkerDialog
     */
    private void showMarkerDialog( NodeRecord node_rec, ArrayList<RouteRecord> list_route ) {
        cancelToast(); 
        final NodeRecord final_node_rec = node_rec;
        if (( list_route == null )||( list_route.size() == 0 )) {
            FileManager.RoutesOnNode res = mFile.getRoutesMultiple( node_rec );
            list_route = res.list_route;
        }
        final ArrayList<RouteRecord> final_list_route = list_route;
        final MarkerDialog dialog = new MarkerDialog( this );		
        dialog.setNodeRec( node_rec );
        dialog.setRouteList( list_route );
        dialog.create();
        dialog.show();
        dialog.setOnChangedListener(
            new MarkerDialog.OnChangedListener() {
            public void onItemClick(RouteRecord route_rec) {
                dialog.dismiss();
                prepareRouteInfo(final_node_rec, route_rec, final_list_route);
            }
        });
    }

    /**
     * showRouteDialog
     */
    private void showRouteDialog( NodeRecord node_rec, RouteRecord route_rec, CompanyRecord com_rec, ArrayList<RouteRecord> list_route ) {
        cancelToast(); 
        if ( com_rec == null ) {
            int com_id = route_rec.company_id;
            com_rec = mFile.loadCompanyRecord( com_id );
            if ( com_rec == null ) {
                log_d("showRouteDialog company missing " + com_id );
                return;
            }            
        }
        final NodeRecord final_node_rec = node_rec;
        final ArrayList<RouteRecord> final_list_route = list_route;
        final RouteDialog dialog = new RouteDialog( this );
        dialog.setNodeRec( node_rec );
        dialog.setRouteRec(route_rec);
        dialog.setCompanyRec(com_rec);
        dialog.create();
        dialog.show();
        dialog.setOnChangedListener(
            new RouteDialog.OnChangedListener() {
            public void onInfoClick() {
                dialog.dismiss();
                showMarkerDialog(final_node_rec, final_list_route);
            }
            public void onUrlClick( String url ) {
                dialog.dismiss();
                startBrawser(url);
            }
        });
    }

    /**
     * when click InfoWindow
     * prepare to show dialog
     */
    private void prepareMarkerDialog( Marker marker ) {
        int id = matchMaker(marker);
        if (id == NOT_MATCH_MARKER) {
            log_d("Error: not match marker");
            return;
        }
        NodeRecord rec = mFile.loadNodeRecord( id );
        if ( rec == null ) {
            log_d("Error: not exist node on marker");
            return;
        }
        // get routes on marker
        FileManager.RoutesOnNode res = mFile.getRoutesMultiple(rec);
        // when get all data, show Dialog
        if ( res.list_request.size() == 0 ){
            showMarkerDialog( rec, res.list_route );
            return;
        }
        // get data from server
        sendMessage(BusmapService.CMD_MARKER_DIALOG, id, res.list_request);
        toast_short(R.string.toast_wait);   
    }

    /**
     * when click Item on maker dialog
     * prepare RouteDialog
     */
    private void prepareRouteInfo( NodeRecord node_rec, RouteRecord route_rec, ArrayList<RouteRecord> list_route ) {
        clearPrevPolylineOn();
        // get node data from server
        sendMessage(BusmapService.CMD_NODES_ON_ROUTE, route_rec);
        // show dialog, if company exists
        CompanyRecord com_rec = mFile.loadCompanyRecord(route_rec.company_id);
        if ( com_rec != null ) {
            showRouteDialog( node_rec, route_rec, com_rec, list_route );
            return;
        }
        // get company data from server
        sendMessage(BusmapService.CMD_ROUTE_DIALOG, node_rec, route_rec);
        // toast
        toast_short(R.string.toast_wait);       
    }

    /**
     * prepareRouteseOnMarker
     * for future
     */
    private void prepareRouteseOnMarker( NodeRecord rec ) {
        clearPrevPolylineOn();
        sendMessage(BusmapService.CMD_ROUTES_ON_MARKER, rec);
    }

// --- startActivity ---
    /**
     * startSetting
     */
    private void startSetting( ) {
        Intent intent = new Intent( this, SettingActivity.class );
        startActivityForResult( intent, REQUEST_SETTING );
    }

    /**
     * startMapSetting
     */
    private void startMapSetting( ) {
        Intent intent = new Intent( this, MapSettingActivity.class );
        startActivityForResult( intent, REQUEST_MAP_SETTING );
    }

    /**
     * startBrawser
     */
    private void startBrawser( String url ) {
        if (( url == null )|| url.equals("") ) return;
        Uri uri = Uri.parse( url );
        Intent intent = new Intent( Intent.ACTION_VIEW, uri );
        startActivity(intent);
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
     * toast_short
     */
    private void toast_short( int id ) {
        ToastMaster.makeText( this, id, Toast.LENGTH_SHORT ).show();
    }

    /**
     * toast_short
     */
    private void toast_short( String msg ) {
        ToastMaster.makeText( this, msg, Toast.LENGTH_SHORT ).show();
    }

    /**
     * cancelToast
     */
    private void cancelToast()  {
        ToastMaster.cancelToast() ;
    }

    /**
     * log_d
     */
    private void log_d( String str ) {
        if (Constant.DEBUG) Log.d( Constant.TAG, getLocalClassName() + " " + str );
    }

}
