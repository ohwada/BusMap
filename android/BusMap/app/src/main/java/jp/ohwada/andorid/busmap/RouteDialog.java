/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import jp.ohwada.andorid.busmap.util.RouteRecord;
import jp.ohwada.andorid.busmap.util.NodeRecord;
import jp.ohwada.andorid.busmap.util.CompanyRecord;
import jp.ohwada.andorid.busmap.view.CommonDialog;

/**
 * Route Dialog
 */
public class RouteDialog extends CommonDialog {

    private static final int MIN_TYPE = 1;
    private static final int MAX_TYPE = 5;
    private static final float UNKNOWN_FREQ = 999f;

    // input data
    private NodeRecord mNodeRecord = null;
    private RouteRecord mRouteRecord = null;
    private CompanyRecord mCompanyRecord = null;

    private String[] mTypeArray = null;
    private String mFreqUnknown = "";

   // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onInfoClick();
        void onUrlClick( String url );
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
    public RouteDialog( Context context ) {
        super( context, R.style.Theme_RouteDialog );
        intDialog();
    }

    /**
     * === Constructor ===
     * @param Context contex
     * @param int theme
     */ 
    public RouteDialog( Context context, int theme ) {
        super( context, theme );
        intDialog();
    }

    /**
     * intDialog
     */
    private void intDialog() {
        TAG_SUB = RouteDialog.class.getSimpleName();
    }

    /**
     * setNodeRec
     * @param NodeRecord rec
     */ 
    public void setNodeRec( NodeRecord rec ) {
        mNodeRecord = rec ;
    }

    /**
     * setRouteRec
     * @param RouteRecord rec
     */ 
    public void setRouteRec( RouteRecord rec ) {
        mRouteRecord = rec ;
    }

    /**
     * setCompanyRec
     * @param CompanyRecord rec
     */ 
    public void setCompanyRec( CompanyRecord rec ) {
        mCompanyRecord = rec ;
    }
    						
    /**
     * create
     */ 	
    public void create() {
        TAG_SUB = RouteDialog.class.getSimpleName();
        mView = getLayoutInflater().inflate( R.layout.dialog_route, null );
        setContentView(mView);
        createButtonClose() ;
        setLayoutWidthFromDimension( R.dimen.dialog_route_view_width );

        final String home = mCompanyRecord.url_home;
        int homeVisibility = View.GONE;
        if ( home.length() > 0 ) {
            homeVisibility  = View.VISIBLE;
        }

        final String search = mCompanyRecord.url_search;
        int searchVisibility = View.GONE;
        if ( search.length() > 0 ) {
            searchVisibility  = View.VISIBLE;
        }

        mTypeArray = getContext().getResources().getStringArray(R.array.route_types);
        mFreqUnknown = getContext().getResources().getString(R.string.route_unknown);

        TextView tvBusstop = (TextView) findViewById( R.id.TextView_dialog_route_busstop );
        tvBusstop.setText( mNodeRecord.name );

        TextView tvTitle = (TextView) findViewById( R.id.TextView_dialog_route_title );
        tvTitle.setText( mRouteRecord.name );

        TextView tvCompany = (TextView) findViewById( R.id.TextView_dialog_route_company );
        tvCompany.setText( mCompanyRecord.name );

        TextView tvType = (TextView) findViewById( R.id.TextView_dialog_route_type );
        tvType.setText( convType( mRouteRecord.type ) );

        TextView tvDay = (TextView) findViewById( R.id.TextView_dialog_route_day );
        tvDay.setText( convFreq(mRouteRecord.day) );

        TextView tvSaturday = (TextView) findViewById( R.id.TextView_dialog_route_saturday );
        tvSaturday.setText( convFreq(mRouteRecord.saturday) );

        TextView tvHoliday = (TextView) findViewById( R.id.TextView_dialog_route_holiday );
        tvHoliday.setText( convFreq(mRouteRecord.holiday) );

        TextView tvRemarks = (TextView) findViewById( R.id.TextView_dialog_route_remarks );
        tvRemarks.setText( mRouteRecord.remarks );

        Button btnHome = (Button) findViewById( R.id.Button_dialog_route_home );
        btnHome.setVisibility(homeVisibility);
        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyUrlClick( home );
            }
        });

        Button btnSearch = (Button) findViewById( R.id.Button_dialog_route_search );
        btnSearch.setVisibility(searchVisibility);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyUrlClick( search );
            }
        });

        Button btnInfo = (Button) findViewById( R.id.Button_dialog_route_info );
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notifyInfoClick();
            }
        });
    }

    /**
     * convType
     */
    private String convType( int type ) {
        if (( type < MIN_TYPE )||( type > MAX_TYPE )) {
            log_d( "type invalid " + type );
            return "";
        }
        return mTypeArray[type-1];
    }

    /**
     * convFreq
     */
    private String convFreq( float freq ) {
        if ( freq > UNKNOWN_FREQ ) {
            return mFreqUnknown;
        }
        return String.valueOf(freq);
    }

    /**
     * notifyUrlClick
     */
    private void notifyUrlClick( String url ) {
        if ( mListener != null ) {
            mListener.onUrlClick(url);
        }
    }

    /**
     * notifyInfoClick
     */
    private void notifyInfoClick() {
        if ( mListener != null ) {
            mListener.onInfoClick();
        }
    }

}
