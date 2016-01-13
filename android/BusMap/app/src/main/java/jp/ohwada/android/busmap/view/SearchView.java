/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import jp.ohwada.android.busmap.Constant;
import jp.ohwada.android.busmap.R;
import jp.ohwada.android.busmap.util.ToastMaster;

/**
 * SearchView
 */
public class SearchView {

    private static final int IMM_FLAGS = 0;

    // object
    private Context mContext;
    private View mView;
    private InputMethodManager mInputMethodManager;

    // view
    private EditText mEditAddress;

   // callback 
    private OnChangedListener mListener;  

    /*
     * callback interface
     */    
    public interface OnChangedListener {
        void onAddress( String address );
    }

    /*
     * callback
     */ 
    public void setOnChangedListener( OnChangedListener listener ) {
        mListener = listener;
    }
	
    /**
     * === Constructor ===
     * @param Context context
     */
    public SearchView( Context context, View view ) { 	
        mContext = context;
        mView = view;
        mInputMethodManager = (InputMethodManager)
            context.getSystemService( Context.INPUT_METHOD_SERVICE );
    }
			
    /**
     * create
     */ 	
    public void create() {	    		
        mEditAddress = (EditText) mView.findViewById( R.id.EditText_search_address );
        Button btnSearch = (Button) mView.findViewById( R.id.Button_search );
        btnSearch.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick( View view ) {
                procClickSearch();
            }
        });
				
    }

    /**
     * getGeoName
     * @return String
     */ 	
    public String getGeoName() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences( mContext );
        String name = pref.getString( 
            Constant.PREF_GEO_NAME,  
            mContext.getResources().getString( R.string.geo_name ) );
        return name;
    }

    /**
     * setAddressEdit
     * @param String name 
     */ 	
    public void setAddressEdit( String name ) {
        mEditAddress.setText( name );
    }
	
    /**
     * getAddressEdit
     * @return String 
     */	
    public String getAddressEdit() {
        return mEditAddress.getText().toString().trim();
    }

    /**
     * procClickSearch
     */
    private void procClickSearch() {
        String address = getAddressEdit();
        // nothig if no input
        if ( address.length() == 0 ) {
            toast_short( R.string.toast_search_please_address );
            return;
        }
        notifyAddress(address);      
    }

    /**
     * hide software keyboard
     */
    public void hideInputMethod() {
        // dont work InputMethodManager.HIDE_IMPLICIT_ONLY 
        mInputMethodManager.hideSoftInputFromWindow(mView.getWindowToken(), IMM_FLAGS);
    }

    /**
     * toast_short
     */
    private void toast_short( int id ) {
        ToastMaster.makeText(mContext, id, Toast.LENGTH_SHORT).show();
  }

    /**
     * notifyAddress
     */
    private void notifyAddress( String address ) {
        if ( mListener != null ) {
            mListener.onAddress( address );
        }
    }
	
}
