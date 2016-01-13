/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.android.busmap.util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * RouteRecord
 */
public class RouteRecord
    implements Parcelable {

    public int id = 0;
    public int company_id = 0;
    public String company = "";
    public String name = "";
    public int type = 0;
    public float day = 0;
    public float saturday = 0;
    public float holiday = 0;
    public String remarks = "";
    public int[] node_ids;

    /**
     * Constructor
     */
    public RouteRecord( int _id, int _company_id, String _name, String _company, int _type, float _day, float _saturday, float _holiday, String _remarks, int[] _node_ids ) {
        id = _id;
        company_id = _company_id;
        company = _company;
        name = _name;
        type = _type;
        day = _day;
        saturday = _saturday;
        holiday = _holiday;
        remarks = _remarks;
        node_ids = _node_ids;
    }

    /**
     * Constructor
     */
   private RouteRecord(Parcel in) { 
        id = in.readInt();  
        company_id = in.readInt();
        company = in.readString();    
        name = in.readString();
        type = in.readInt();
        day = in.readFloat();
        saturday = in.readFloat();
        holiday = in.readFloat();
        remarks = in.readString();
        in.readIntArray(node_ids);
     }

    /**
     * writeToParcel
     * @param Parcel out
     * @param int flags  
     */
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);  
        out.writeInt(company_id);
        out.writeString(company);    
        out.writeString(name);
        out.writeInt(type);
        out.writeFloat(day);
        out.writeFloat(saturday);
        out.writeFloat(holiday);
        out.writeString(remarks);
        out.writeIntArray(node_ids);   
    }

    /**
     * describeContents
     * @return int
     */
    public int describeContents() {  
        return 0;  
    } 

    /**
     * --- Parcelable.Creator ---
     */
    public static final Parcelable.Creator<RouteRecord> CREATOR  
        = new Parcelable.Creator<RouteRecord>() {  
        /**
         * createFromParcel
         * @param Parcel in
         * @return RouteRecord
         */ 
        public RouteRecord createFromParcel(Parcel in) {  
            return new RouteRecord(in);  
        }
        /**
         * newArray
         * @param int size
         * @return RouteRecord[]
         */   
        public RouteRecord[] newArray(int size) {  
            return new RouteRecord[size];  
        }  
    };

}
