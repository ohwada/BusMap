/**
 * Bus Map
 * 2015-12-01 K.OHWADA
 */
package jp.ohwada.andorid.busmap.util;

import android.os.Parcelable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MessageUtil
 */ 
public class MessageUtil {

    /**
     * convListNode
     * @param ArrayList<Parcelable> list_p
     * @return ArrayList<NodeRecord>
     */
    public static ArrayList<NodeRecord> convListNode( ArrayList<Parcelable> list_p ) {
        ArrayList<NodeRecord> list_node = new ArrayList<NodeRecord>();
        for(Parcelable p: list_p){
            list_node.add( (NodeRecord)p );
        }
        return list_node;
    }

    /**
     * convListRoute
     * @param ArrayList<Parcelable> list_p
     * @return ArrayList<RouteRecord>
     */
    public static ArrayList<RouteRecord> convListRoute( ArrayList<Parcelable> list_p ) {
        ArrayList<RouteRecord> list_route = new ArrayList<RouteRecord>();
        for(Parcelable p: list_p){
            list_route.add( (RouteRecord)p );
        }
        return list_route;
    }

    /**
     * convListCompany
     * @param ArrayList<Parcelable> list_p
     * @return ArrayList<CompanyRecord>
     */
    public static ArrayList<CompanyRecord> convListCompany( ArrayList<Parcelable> list_p ) {
        ArrayList<CompanyRecord> list_com = new ArrayList<CompanyRecord>();
        for(Parcelable p: list_p){
            list_com.add( (CompanyRecord)p );
        }
        return list_com;
    }

    /**
     * getListUniqueRouteId
     * @param List<NodeRecord> list
     * @return List<Integer>
     */
    public static List<Integer> getListUniqueRouteId( List<NodeRecord> list) {
        List<Integer> list_id = new ArrayList<Integer>();
        if (list.size() == 0) return list_id;
        Set<Integer> set = new HashSet<Integer>();
        for (NodeRecord rec : list) {
            int[] id_array = rec.route_ids;
            if (id_array.length == 0) continue;
            for (int id : id_array) {
                if (!set.contains(id)) {
                    set.add(id);
                    list_id.add(id);
                }
            }
        }
        return list_id;
    }

    /**
     * splitListId
     * @param List<Integer> list_id
     * @param int length
     * @return ArrayList<ArrayList<Integer>>
     */
    public static ArrayList<ArrayList<Integer>> splitListId( List<Integer> list_id, int length ) {
        int size = list_id.size();
        ArrayList<ArrayList<Integer>> list_list = new ArrayList<ArrayList<Integer>>();
        for (int i = 0; i <= size / length; i++) {
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int j = 0; j < length; j++) {
                int k = length * i + j;
                if (k < size) {
                    list.add(list_id.get(k));
                }
            }
            list_list.add(list);
        }
        return list_list;
    }

}
