<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

class BusApi {
    var $db;
    var $util;

    var $MIN_DISTANCE = 1;  // 100m
    var $MAX_DISTANCE = 100;    // 10km
    var $MAX_LENGTH = 100;

    /**
     * constractor
     */ 
    function BusApi() {
        $this->util = new BusUtil();
        $this->db = new BusDb();
        $this->db->connect();
    }

    /**
     * makeLocation
     * @param float $lat
     * @param float $lon
     * @param int $dsitance
     */ 
    function makeLocation( $lat, $lon, $distance ) {
        // adjust distance (100m - 10Km)
        if ( $distance < $this->MIN_DISTANCE ) {
            $distance = $this->MIN_DISTANCE;
        }
        if ( $distance > $this->MAX_DISTANCE ) {
            $distance = $this->MAX_DISTANCE;
        }
        $nodes = $this->db->searchNodesPoint( $lat, $lon, $distance );
        return $this->makeNodesCommon( $nodes );
    }

    /**
     * makeNodeName
     * @param string $name
     * @param int $pref_id
     */
    function makeNodeName( $name, $pref_id ) {
        $nodes = $this->db->searchNodesName( $name, $pref_id );
        return $this->makeNodesCommon( $nodes );
    }

    /**
     * makeNodes
     * @param $ids
     */
    function makeNodes( $ids ) {
        $id_arr = $this->explodeIds($ids, $this->MAX_LENGTH);
        $nodes = $this->db->getNodesByIds( $id_arr );
        return $this->makeNodesCommon( $nodes );
    }

    /**
     * makeNodesCommon
     */
    function makeNodesCommon( $nodes ) {
        $code = 0;
        $msg = "";
        $res = array();
        if ( count($nodes) == 0 ) {
            $msg = "No Nodes";
        } else {
            $arr = $this->getNodeParam( $nodes );
            $res = $this->arrangeNodes( $arr );
            $code = 1;
        }
        $ret = array(
            "code" => $code,
            "message" => $msg,
            "nodes" => $res
        );
        return $ret;
    }

    /**
     * getNodeParam
     */
    function getNodeParam( $nodes ) {
        $arr = array();
        foreach( $nodes as $node ) {
            $tmp = $node;
            $tmp["pref"] = $this->db->getCachedPrefById( $node["pref_id"] );
            $tmp["routes"] = $this->db->getRoutesByNodeId( $node["id"] );
            $arr[] = $tmp;
        }
        return $arr;
    }

    /**
     * arrangeNodes
     */
    function arrangeNodes($nodes) {
        $arr = array();
        foreach($nodes as $node) {
            $arr[] = array(
                "id" => $node["id"],
                "name" => $node["name"],
                "lat" => $node["lat"],
                "lon" => $node["lon"],
                "pref" => $node["pref"]["name"],
                "route_ids" => $this->util->makeRouteIds( $node["routes"] )
            );
        }
        return $arr;
    }

    /**
     * makeRoutes
     * @param $ids
     */
    function makeRoutes($ids) {
        $code = 0;
        $msg = "";
        $res = array();
        $id_arr = $this->explodeIds($ids, $this->MAX_LENGTH);
        $routes = $this->db->getRoutesByIds( $id_arr );
        if ( count($routes) ==0 ) {
            $msg = "No Routes";
        } else {
            $code = 1;
            list( $arr_routes, $arr_coms ) = $this->getRouteParam($routes);
            list( $res_routes, $res_curves ) = $this->arrangeRoutes( $arr_routes );
            $res_coms = $this->arrangeComs( $arr_coms );
        }
        $ret = array(
            "code" => $code,
            "message" => $msg,
            "routes" => $res_routes,
            "companies" => $res_coms,
            "curves" => $res_curves
        );
        return $ret;
    }

    /**
     * getRouteParam
     */
    function getRouteParam($routes) {
        $arr_route = array();
        $arr_com = array();
        $arr_com_id = array();
        foreach($routes as $route) {
            $route_id = $route["id"];
            $com_id = $route["company_id"];
            $com = $this->db->getCachedCompanyById( $com_id );
            $tmp = $route;
            $tmp["curves"] = $this->db->getCurvesByRouteId( $route_id );
            $tmp["nodes"] = $this->db->getNodesByRouteId( $route_id );
            $tmp["com"] = $com;
            $arr_route[] = $tmp;
            if ( !in_array( $com_id, $arr_com_id ) ) {
                // add, if not exists
                $arr_com_id[] = $com_id;
                $arr_com[] = $com;
            }
        }
        return array( $arr_route, $arr_com );
    }

    /**
     * makeCompanies
     * @param $ids
     */
    function makeCompanies($ids) {
        $code = 0;
        $msg = "";
        $res = array();
        $id_arr = $this->explodeIds($ids, $this->MAX_LENGTH);
        $coms = $this->db->getCompaniesByIds( $id_arr );
        if ( count($coms) ==0 ) {
            $msg = "No Companies";
        } else {
            $code = 1;
            $res_coms = $this->arrangeComs( $coms );
        }
        $ret = array(
            "code" => $code,
            "message" => $msg,
            "companies" => $res_coms,
        );
        return $ret;
    }

    /**
     * explodeIds
     */
    function explodeIds($ids, $length) {
        $arr = array();
        $id_arr = explode( ",", $ids );
        $len = min( count($id_arr), $length);
        for( $i=0; $i<$len; $i++ ) {
            $arr[$i] = intval( $id_arr[$i] );
        }
        return $arr;
    }

    /**
     * arrangeRoutes
     */
    function arrangeRoutes($routes) {
        $arr_route = array();
        $arr_curve = array();
        foreach($routes as $route) {
            $company = $route["com"];
            $id = $route["id"];
            $arr_route[] = array(
                "id" => $id,
                "company_id" => $company["id"],
                "company" => $company["name"],
                "name" => $route["bus_line"],
                "type" => $route["type"],
                "day" => $route["day"],
                "saturday" => $route["saturday"],
                "holiday" => $route["holiday"],
                "remarks" => $route["remarks"],
                "node_ids" => $this->util->makeNodeIds( $route["nodes"] )
            );
            $arr_curve[] = array(
                "id" => $id,
                "curves" => $this->arrangeCurves( $route["curves"] )
            );
        }
        return array( $arr_route, $arr_curve );
    }

    /**
     * arrangeCurves
     */
    function arrangeCurves($curves) {
        $arr_ret = array();
        foreach($curves as $curve) {
            $arr_str = explode( ",", $curve["curve"] );
            $arr_float = array();
            foreach( $arr_str as $str ) {
                $arr_float[] = floatval($str);
            }
            $arr_ret[] = $arr_float;
        }
        return $arr_ret;
    }

    /**
     * arrangeComs
     */
    function arrangeComs($coms) {
        $arr = array();
        $arr_id = array();
        foreach($coms as $com) {
            $id = $com["id"];
            // skip, if exists
            if ( in_array($id, $arr_id) ) continue;
            $arr_id[] = $id;
            $arr[] = array(
                "id" => $id,
                "name" => $com["name"],
                "url_home" => $this->arrangeString( $com["url_home"] ),
                "url_search" => $this->arrangeString( $com["url_search"] )
            );
        }
        return $arr;
    }

    function arrangeString( $str ) {
        if ( empty($str) ) return "";  
        return $str;
    }

    /**
     * outputMessage
     * @param int $code
     * @param string $msg
     */
    function outputMessage($code, $msg) {
        $arr = array(
            "code" => $code,
            "message" => $msg
        );
        $this->outputResult($arr);
   }

    /**
     * outputResult
     * @param array $arr
     */
    function outputResult($arr) {
        $text = json_encode( $arr );
        $this->output($text);
    }

    /**
     * output
     * @param string $text
     */
    function output($text) {
        header('Content-type: text/plain; charset=utf-8');
        echo $text;
    }

}

    /**
     * api
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    include_once("class/BusUtil.php");
    $api = new BusApi();

    $mode = isset($_GET["mode"]) ? $_GET["mode"] : "node";
    $result = "";
    $msg = "";

    // location
    if ( $mode == "location" ) {
        if ( !isset($_GET["lat"]) || !isset($_GET["lon"]) || $_GET["lat"] == ""  || $_GET["lon"] == "")  {
            $msg = "No param location: lat, lon";
        } else {
            $distance = isset($_GET["distance"]) ? intval( $_GET["distance"] ): 10;
            $result = $api->makeLocation( $_GET["lat"], $_GET["lon"], $distance );
        }

    // node_name
    } else if ( $mode == "node_name" ) {
        if ( !isset($_GET["name"]) || $_GET["name"] == "" )  {
            $msg = "No param node_name: name";
        } else {
            $pref_id = isset($_GET["pref_id"]) ? intval($_GET["pref_id"]) : 0;
            $result = $api->makeNodeName( $_GET["name"], $pref_id );
        }

    //nodes
    } else if ( $mode == "nodes" ) {
        if ( !isset($_GET["ids"]) || $_GET["ids"] == "")  {
            $msg = "No param nodes: ids";
        } else {
            $result = $api->makeNodes( $_GET["ids"] );
        }

    // routes
    } else if ( $mode == "routes" ) {
        if ( !isset($_GET["ids"]) || $_GET["ids"] == "")  {
            $msg = "No param routes: ids";
        } else {
            $result = $api->makeRoutes( $_GET["ids"] );
        }

    // companies
    } else if ( $mode == "companies" ) {
        if ( !isset($_GET["ids"]) || $_GET["ids"] == "")  {
            $msg = "No param companies: ids";
        } else {
            $result = $api->makeCompanies( $_GET["ids"] );
        }

    // others
    } else  {
        $msg = "No param : mode";
    }

    if ( $result ) {
        $api->outputResult($result);
    } else if ( $msg ) {
        $api->outputMessage(0, $msg);
    }

?>
