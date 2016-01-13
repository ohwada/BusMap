/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.ohwada.android.busmap.util.NodeRecord;
import jp.ohwada.android.busmap.util.RouteRecord;
import jp.ohwada.android.busmap.view.CommonDialog;
import jp.ohwada.android.busmap.view.MarkerAdapter;

/**
 * Marker Dialog
 */
public class MarkerDialog extends CommonDialog {

    private static final int ADPTOR_RESOURCE = 0;

    // list
    private MarkerAdapter mAdapter;
    private List<RouteRecord> mRouteList = new ArrayList<RouteRecord>();

    private NodeRecord mNodeRecord = null;

   // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onItemClick( RouteRecord rec );
    }

    /*
     * callback
     */ 
    public void setOnChangedListener( OnChangedListener listener ) {
        mListener = listener;
    }
	
    /**
     * === Constructor ===
     * @param Context contex
     */ 	
    public MarkerDialog( Context context ) {
        super( context, R.style.Theme_MarkerDialog );
        intDialog();
    }

    /**
     * === Constructor ===
     * @param Context contex
     * @param int theme
     */ 
    public MarkerDialog( Context context, int theme ) {
        super( context, theme );
        intDialog();
    }
 
    /**
     * intDialog
     */
    private void intDialog() {
        TAG_SUB = MarkerDialog.class.getSimpleName();
    }
   
    /**
     * setNodeRec
     * @param NodeRecord rec
     */ 
    public void setNodeRec( NodeRecord rec ) {
        mNodeRecord = rec ;
    }
	
    /**
     * setRouteList
     * @param List<RouteRecord> list
     */  
    public void setRouteList( List<RouteRecord> list ) {
        mRouteList = list;
    }
    						
    /**
     * === create ===
     */
    @Override	
    public void create() {
        log_d("create");
        mView = getLayoutInflater().inflate( R.layout.dialog_marker, null );
        setContentView(mView);
        createButtonClose() ;
        setLayoutWidthFromDimension( R.dimen.dialog_marker_view_width );

        TextView tvTitle = (TextView) findViewById( R.id.TextView_dialog_marker_title );
        tvTitle.setText( mNodeRecord.name );

        mAdapter = new MarkerAdapter( getContext(), ADPTOR_RESOURCE, mRouteList );
        mListView = (ListView) findViewById( R.id.ListView_dialog_marker);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick( AdapterView<?> parent, View view, int position, long id ) {
                procItemClick( parent, view, position, id );
            }
        });
    }

    /**
     * procItemClick
     */ 
    private void procItemClick( AdapterView<?> parent, View view, int position, long id ) {
        if (( position >= 0 )&&( position < mRouteList.size() )) {
            notifyItemClick( mRouteList.get(position) );
        }
    }

    /**
     * notifyItemClick
     */
    private void notifyItemClick( RouteRecord rec ) {
        if ( mListener != null ) {
            mListener.onItemClick( rec );
        }
    }

}
