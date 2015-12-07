<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/**
 * BusDb
 */
class BusDb {

    var $cachedPref = array();

    /**
     * constractor
     */
    function BusDb() {
        // dummy
    }

    /**
     * connect
     */
    function connect() {
        $link = mysql_connect(DB_HOST, DB_USER, DB_PASS);
        if (!$link) {
            die("not connect \n" . mysql_error());
        }
        mysql_select_db(DB_NAME);
        mysql_query("SET NAMES utf8");
    }

    /**
     * getCompanyCount
     * @return int
     */
    function getCompanyCount() {
        return $this->selectCountAll( "bus_company" );
    }

    /**
     * getCompaniesByPrefId
     * @param int $pref_id
     * @return int
     */
    function getCompaniesByPrefId( $pref_id ) {
        $id_arr = $this->selectComIdsByPrefId( $pref_id );
        return $this->selectComsByIds( $id_arr );
    }

    /**
     * selectComsByIds
     */
    function selectComsByIds( $id_arr ) {
        if ( count($id_arr) == 0 ) return array();
        $ids = implode( ",", $id_arr );
        $sql = "SELECT * FROM bus_company WHERE id IN (". $ids .") ORDER BY name,id";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * selectComIdsByPrefId
     */
    function selectComIdsByPrefId( $pref_id ) {
        $id_arr = array();
        $links = $this->selectLinkComPrefByPrefId( $pref_id );
        foreach ( $links as $link ) {
            $id_arr[] = $link["company_id"];
        }
        if ( count($id_arr) == 0 ) return $id_arr;
        $unique_id_arr = array_unique( $id_arr, SORT_NUMERIC );
        return $unique_id_arr;
    }

    /**
     * getCompany
     * @param int $id
     * @return row
     */
    function getCompanyById( $id ) {
        return $this->selectById( "bus_company", $id );
    }

    /**
     * searchCompaniesName
     * @param string $name
     * @param int $pref_id
     * @return array of row
     */
    function searchCompaniesName( $name, $pref_id ) {
        $sql = "SELECT * FROM bus_company WHERE ";
        if ( $pref_id > 0 ) {
            $id_arr = $this->selectComIdsByPrefId( $pref_id );
            $ids = implode( ",", $id_arr );
            $sql .= " id IN (". $ids .") AND ";
        }
        $sql .= " name LIKE '%" . $name. "%' ORDER BY name, id";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * getCachedPrefById
     * @param int $pref_id
     * @return array of row
     */
    function getCachedPrefById( $pref_id ) {
        if ( isset( $this->cachedPref[ $pref_id ] )) {
            return $this->cachedPref[ $pref_id ];
        }
        $pref = $this->getPrefById( $pref_id );
        $this->cachedPref[ $pref_id ] = $pref;
        return $pref;
    }

    /**
     * getPref
     * @param int $id
     * @return row
     */
    function getPrefById( $id ) {
        return $this->selectById( "bus_pref", $id );
    }

    /**
     * getPrefs
     * @return array of row
     */
    function getPrefs() {
        $sql = "SELECT * FROM bus_pref ORDER BY id";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * getPrefsByRouteId
     * @param int $route_id
     * @return array of row
     */
    function getPrefsByRouteId( $route_id ) {
        $links = $this->selectLinkRoutePrefByRouteId( $route_id );
        if ( count($links) == 0 ) return array();
        $id_arr = $this->makePrefIdsByLinks( $links );
        return $this->selectPrefsByIds( $id_arr );
    }

    /**
     * getPrefsByCompanyId
     * @param int $com_id
     * @return array of row
     */
    function getPrefsByCompanyId( $com_id ) {
        $links = $this->selectLinkComPrefByComId( $com_id );
        if ( count($links) == 0 ) return array();
        $id_arr = $this->makePrefIdsByLinks( $links );
        return $this->selectPrefsByIds( $id_arr );
    }

    /**
     * makePrefIdsByLinks
     */
    function makePrefIdsByLinks( $links ) {
        $id_arr = array();
        if ( count($links) == 0 ) return $id_arr;
        foreach ( $links as $link ) {
            $id_arr[] = $link["pref_id"];
        }
        $unique_id_arr = array_unique( $id_arr, SORT_NUMERIC );
        return $unique_id_arr;
    }

    /**
     * selectPrefsByIds
     */
    function selectPrefsByIds( $id_arr ) {
        $ids = implode( ",", $id_arr );
        $sql = "SELECT * FROM bus_pref WHERE id IN (". $ids .") ORDER BY id";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * getRouteCount
     * @return int
     */
    function getRouteCount() {
        return $this->selectCountAll( "bus_route" );
        $sql = "SELECT * FROM bus_company ORDER BY name";
    }

    /**
     * getRoute
     * @param int $id
     * @return row
     */
    function getRouteById( $id ) {
        return $this->selectById( "bus_route", $id );
    }

    /**
     * selcectRouteIdsByPrefId
     */
    function selcectRouteIdsByPrefId( $pref_id ) {
        $id_arr = array();
        $links = $this->selectLinkRoutePrefByPrefId( $pref_id );
        if ( count($links) == 0 ) return $id_arr;
        foreach( $links as $link ) {
            $id_arr[] = $link["route_id"];
        }
        $unique_id_arr = array_unique( $id_arr, SORT_NUMERIC );
        return $unique_id_arr;
    }

    /**
     * getRoutesByCompany
     * @param int $com_id
     * @return arary of row
     */
    function getRoutesByCompany( $com_id ) {
        $sql = "SELECT * FROM bus_route WHERE company_id=". $com_id ." ORDER BY bus_line";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * getRoutesByCompany
     * @param arary of int $id_arr
     * @return arary of row
     */
    function selectRoutesByIds( $id_arr ) {
        $ids = implode( ",", $id_arr );
        $sql = "SELECT * FROM bus_route WHERE id IN (". $ids .") ORDER BY company_id, bus_line";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * getRoutesByNode
     * @param int $node_id
     * @return arary of row
     */
    function getRoutesByNodeId( $node_id ) {
        $id_arr = $this->selectRouteIdsByNodeId( $node_id );
        return $this->selectRoutesByIds( $id_arr );
    }

    /**
     * selectRouteIdsByNodeId
     */
    function selectRouteIdsByNodeId( $node_id ) {
        $id_arr = array(); 
        $links = $this->selectLinkRouteNodeByNodeId( $node_id ); 
        if ( count($links) == 0 ) return $id_arr;
        foreach ( $links as $link ) {
            $id_arr[] = $link["route_id"];      
        }
        $unique_id_arr = array_unique( $id_arr, SORT_NUMERIC );
        return $unique_id_arr;
    }

    /**
     * searchRoutesName
     * @param string $name
     * @param int $pref_id
     * @return array of row
     */
    function searchRoutesName( $name, $pref_id ) {
         $sql = "SELECT * FROM bus_route WHERE ";
        if ( $pref_id > 0 ) {
            $id_arr = $this->selcectRouteIdsByPrefId( $pref_id );
            $ids = implode( ",", $id_arr );
            $sql .= " id IN (". $ids .") AND ";
        }
        $sql .= " bus_line LIKE '%" . $name. "%' ORDER BY bus_line, id";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * getRoutesByCompany
     * @param arary of row $routes 
     * @return arary of row
     */
    function getMapCorvesByRoutes( $routes ) {
        $ret_arr = array();
        foreach ( $routes as $route ) { 
            $curve_arr = array();
            $curves = $this->getCurvesByRoute( $route["id"] );
            foreach ( $curves as $curve ) {
                $curve_arr[] = $curve["curve"];
            }
            $route_arr = $route;
            $route_arr["curves"] = $curve_arr;    
            $ret_arr[] = $route_arr;
        }
        return $ret_arr;
    }

    /**
     * getCurvesByRoute
     * @param int $route_id
     * @return arary of row
     */
    function getCurvesByRoute( $route_id ) {
        return $this->selectRows( "bus_curve", "route_id", $route_id );
    }

    /**
     * getNodeCount
     * @return int
     */
    function getNodeCount() {
        return $this->selectCountAll( "bus_stop" );
    }

    /**
     * getNode
     * @param int $id
     * @return row
     */
    function getNodeById( $id ) {
        return $this->selectById( "bus_stop", $id );
    }

    /**
     * getNodesByIds
     * @param arary of int $id_arr
     * @return arary of row
     */
    function getNodesByIds( $id_arr ) {
        $unique_id_arr = array_unique( $id_arr, SORT_NUMERIC );
        $ids = implode( ",", $unique_id_arr );
        $sql = "SELECT * FROM bus_stop WHERE id IN (". $ids .") ORDER BY name,id";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * searchNodesName
     * @param string $name
     * @param int $pref_id
     * @return arary of row
     */
    function searchNodesName( $name, $pref_id ) {
        $sql = "SELECT * FROM bus_stop WHERE ";
        if ( $pref_id > 0 ) {
            $sql .= " pref_id=". $pref_id ." AND ";
        }
        $sql .= " name LIKE '%" . $name. "%' ORDER BY pref_id, name, id";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * searchNodesPoint
     * @param float $lat
     * @param float $lon
     * @return arary of row
     */
    function searchNodesPoint( $lat, $lon ) {
        $lat_e6 = intval( $lat * 1e6 );
        $lon_e6 = intval( $lon * 1e6 );
        $max_lat = $lat_e6 + 11000;
        $min_lat = $lat_e6 - 11000;
        $max_lon = $lon_e6 + 9000;
        $min_lon = $lon_e6 - 9000;
        $sql = "SELECT * FROM bus_stop WHERE lat_e6 < ". $max_lat. " AND lat_e6 > ". $min_lat ." AND lon_e6 < ". $max_lon ." AND lon_e6 > ". $min_lon ." ORDER BY pref_id, name, id";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * getNodesByRoute
     * @param int $route_id
     * @return arary of row
     */
    function getNodesByRouteId( $route_id ) {
        return $this->getNodesByRouteIds( array( $route_id ) );
    }

    /**
     * getNodesByRouteIds
     * @param arary of int $route_id_array
     * @return arary of row
     */
    function getNodesByRouteIds( $route_id_array ) {
        $links = $this->selectLinkRouteNodeByRouteIdIds( $route_id_array );
        if (( $links == null )||( count($links) == 0 )) return null;
        $node_id_arr = array();
        foreach ( $links as $link ) { 
            $node_id_arr[] = $link["node_id"];   
        }
        return $this->getNodesByIds( $node_id_arr );
    }

    /**
     * selectLinkComPrefByComId
     */
    function selectLinkComPrefByComId( $id ) {
        return $this->selectLinkComPref( "company_id", $id );
    }

    /**
     * selectLinkComPrefByPrefId
     */
    function selectLinkComPrefByPrefId( $id ) {
        return $this->selectLinkComPref( "pref_id", $id );
    }

    /**
     * selectLinkComPref
     */
    function selectLinkComPref( $key, $id ) {
        return $this->selectRows( "bus_link_com_pref", $key, $id );
    }

    /**
     * selectLinkComNodeByComId
     */
    function selectLinkComNodeByComId( $id ) {
        return $this->selectLinkComNode( "company_id", $id );
    }

    /**
     * selectLinkComNodeByNodeId
     */
    function selectLinkComNodeByNodeId( $id ) {
        return $this->selectLinkComNode( "node_id", $id );
    }

    /**
     * selectLinkComNode
     */
    function selectLinkComNode( $key, $id ) {
        return $this->selectRows( "bus_link_com_node", $key, $id );
    }

    /**
     * selectLinkRoutePrefByRouteId
     */
    function selectLinkRoutePrefByRouteId( $id ) {
        return $this->selectLinkRoutePref( "route_id", $id );
    }

    /**
     * selectLinkRoutePrefByPrefId
     */
    function selectLinkRoutePrefByPrefId( $id ) {
        return $this->selectLinkRoutePref( "pref_id", $id );
    }

    /**
     * selectLinkRoutePref
     */
    function selectLinkRoutePref( $key, $id ) {
        return $this->selectRows( "bus_link_route_pref", $key, $id );
    }
 
    /**
     * selectLinkRouteNodeByRouteIdIds
     */
    function selectLinkRouteNodeByRouteIdIds( $id_arr ) {
        $unique_id_arr = array_unique( $id_arr, SORT_NUMERIC );
        $ids = implode( ",", $unique_id_arr );
        $sql = "SELECT * FROM bus_link_route_node WHERE route_id IN (". $ids .") ORDER BY id";
        return $this->excuteRowsSql( $sql );
    }

   /**
     * selectLinkRouteNodeByRouteId
     */
    function selectLinkRouteNodeByRouteId( $route_id ) {
        return $this->selectLinkRouteNode( "route_id", $route_id );
    }

    /**
     * selectLinkRouteNodeByNodeId
     */
    function selectLinkRouteNodeByNodeId( $node_id ) {
        return $this->selectLinkRouteNode( "node_id", $node_id );
    }

    /**
     * selectLinkRouteNode
     */
    function selectLinkRouteNode( $key, $id ) {
        return $this->selectRows( "bus_link_route_node", $key, $id );
    }

    /**
     * selectById
     */
    function selectById( $table, $id ) {
        $sql = "SELECT * FROM ".$table." WHERE id=". intval($id) ." ORDER BY id";
        $result = mysql_query($sql);
        if (!$result) {
            echo $sql;
            echo mysql_error();
            return null;
        }
        return mysql_fetch_array($result);
    }

    /**
     * selectCountAll
     */
    function selectCountAll( $table ) {
        $sql = "SELECT count(*) FROM ".$table;
        $result = mysql_query($sql);
        if (!$result) {
            echo $sql;
            echo mysql_error();
            return null;
        }
        $row = mysql_fetch_array($result);
        return $row[0];
    }

    /**
     * selectRows
     */
    function selectRows( $table, $key, $id ) {
        $sql = "SELECT * FROM ".$table." WHERE ". $key ."=". intval($id) ." ORDER BY id";
        return $this->excuteRowsSql( $sql );
    }

    /**
     * excuteRowsSql
     */
    function excuteRowsSql( $sql ) {
        $result = mysql_query($sql);
        if (!$result) {
            echo $sql;
            echo mysql_error();
            return null;
        }
        $arr = array();
        while( $row = mysql_fetch_array( $result ) ){
            $arr[] = $row;
        }
        return $arr;
    }

}  	
?>