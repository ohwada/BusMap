/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import jp.ohwada.andorid.busmap.R;
import jp.ohwada.andorid.busmap.util.RouteRecord;

/**
 * adapter for ListView
 */
public class MarkerAdapter extends ArrayAdapter<RouteRecord> {

    // Layout Inflater
    private LayoutInflater mInflater = null;
			
    /**
     * === constractor ===
     * @param Context context
     * @param int resource
     * @param List<TextView> objects     
     * @return void	 
     */
    public MarkerAdapter( Context context, int resource, List<RouteRecord> objects ) {
        super( context, resource, objects );
        mInflater = (LayoutInflater) context.getSystemService( 
            Context.LAYOUT_INFLATER_SERVICE ) ;
    }

    /**
     * === get view ===
     * @param int position 
     * @param View convertView    
     * @param  ViewGroup parent      
     * @return View	 
     */
    @Override
    public View getView( int position, View convertView, ViewGroup parent ) {
        View view = convertView;
        MarkerHolder h = null;              
        // once at first
        if ( view == null ) {
            // get view form xml
            view = mInflater.inflate( R.layout.item_row, null );
            // save 
            h = new MarkerHolder(); 
            h.tv_1 = (TextView) view.findViewById( R.id.TextView_item_row_1 );
            view.setTag( h ); 
        } else {
            // load  
            h = (MarkerHolder) view.getTag();  
        }       
        // get item form Adapter
        RouteRecord item = (RouteRecord) getItem( position );
        // set value
        if ( item != null ) {
            String com = item.company.replace("（株）", "");
            h.tv_1.setText( com + " : " + item.name ) ;
        }
        return view;
    }

    /**
     * holder class
     */	
    static class MarkerHolder { 
        public TextView tv_1;
    }
}
