<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/**
 * BusSearch
 */
class BusSearch {
    var $SELECTED = " selected=\"selected\" ";

    var $db;
    var $util;

    var $MIN_DISTANCE = 1;  // 100m
    var $MAX_DISTANCE = 100;    // 10km

    // 長野
    var $map_lat = 36.110446929932;
    var $map_lon = 138.08520507812;
    var $map_zoom = 7;
    var $map_markers = "[]";
    var $map_lines = "[]";

    var $list_title = "バス停";
    var $list_height = "auto";
    var $list_num = 0;
    var $list_navi = "";
    var $list = "";

    var $node_num = 0;
    var $msg = "";

    var $distance_selected_5 = "";
    var $distance_selected_10 = "";
    var $distance_selected_15 = "";
    var $distance_selected_30 = "";

    /**
     * constractor
     */ 
    function BusSearch() {
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
    function makeLocation( $lat, $lon, $dsitance ) {
        $dsitance = $this->adjustDistance( $dsitance );
        $nodes = $this->db->searchNodesPoint( $lat, $lon, $dsitance );
        $this->map_lat = $lat;
        $this->map_lon = $lon;
        $this->makeNodeCommon( $nodes );
    }

    /**
     * adjustDistance
     */ 
    function adjustDistance( $distance ) {
        // adjust distance (100m - 10Km)
        if ( $distance < $this->MIN_DISTANCE ) {
            $distance = $this->MIN_DISTANCE;
        }
        if ( $distance > $this->MAX_DISTANCE ) {
            $distance = $this->MAX_DISTANCE;
        }
        switch( $distance ) {
            case 5:
                $this->distance_selected_5 = $this->SELECTED;
                break;
            case 15:
                $this->distance_selected_15 = $this->SELECTED;
                break;
            case 30:
                $this->distance_selected_30 = $this->SELECTED;
                break;
            case 10:
            default:
                $this->distance_selected_10 = $this->SELECTED;
                break;                   
        }
        return $distance;
    }

    /**
     * makeNodeName
     * @param string $name
     * @param int $pref_id
     */ 
    function makeNodeName( $name, $pref_id ) {
        $nodes = $this->db->searchNodesName( $name, intval($pref_id) );
        $this->makeNodeCommon( $nodes );
    }

    /**
     * makeNodeCommon
     */ 
    function makeNodeCommon( $nodes ) {
        $this->list_title = "バス停";
        if (( $nodes == null )||( count($nodes) == 0) ) {
            $this->msg = "バス停が見つかりません";
        } else {
            list( $this->map_lat, $this->map_lon, $this->map_zoom ) = 
                $this->util->calcMapCenterByNodes( $nodes );
            $this->map_markers = $this->util->makeNodeMarkers( $nodes );
            $this->list = $this->arrangeListNode( $nodes );
            $this->node_num = count($nodes);
            $this->list_num = count($nodes);
            $this->msg = "<b>検索結果</b> バス停の数 " . $this->list_num ;
        }
    }

    /**
     * arrangeListNode
     */
    function arrangeListNode( $nodes ) {
        $list = "";
        foreach ( $nodes as $node ) { 
            $a_node = "<a href='node.php?id=". $node["id"] ."'>". $node["name"] ."</a>";
            // pref
            $pref = $this->db->getCachedPrefById( $node["pref_id"] );
            $a_pref = "<a href='prefecture.php?id=". $node["pref_id"] ."'>".$pref["name"] ."</a>"; 
            // routes
            $route_text = $a_pref ." : ";
            $routes = $this->db->getRoutesByNodeId( $node["id"] );
            $route_text .= $this->arrangeRoutes( $routes );
            $list .= "<li>". $a_node ." (". $route_text .")</li>\n";
        }
        return $list;
    }

    /**
     * arrangeRoutes
     */
    function arrangeRoutes( $routes ) {
        $is_first = true;
        $text = "";
        foreach ($routes as $route) {
            if ( !$is_first ) {
                $text .= ",\n";
            }
            $a_route = "<a href='route.php?id=". $route["id"] ."'>". $route["bus_line"] ."</a>";
            $text .= $a_route;
            $is_first = false;
        }
        return $text;
    }

    /**
     * makeRouteName
     * @param string $name
     * @param int $pref_id
     */ 
    function makeRouteName( $name, $pref_id  ) {
        $this->list_title = "バス路線";
        $routes = $this->db->searchRoutesName( $name, intval($pref_id) );
        if (( $routes == null )||( count($routes) == 0) ) {
            $this->msg = "バス路線が見つかりません";
        } else {
            $this->list_num = count($routes);
            $this->msg = "<b>検索結果</b> バス路線の数 " . $this->list_num ;
            list($this->list, $this->list_navi, $this->list_height) = 
                $this->util->makeRouteList( $routes, true );
            list( $this->map_lat, $this->map_lon, $this->map_zoom ) = 
                $this->util->calcMapCenterByRoutes( $routes );
            $map_corves = $this->db->getMapCorvesByRoutes( $routes );
            $this->map_lines = $this->util->makeLines( $map_corves );
        }
    }

    /**
     * makeCompanyName
     * @param string $name
     * @param int $pref_id
     */ 
    function makeCompanyName( $name, $pref_id ) {
        $this->list_title = "バス会社";
        $coms = $this->db->searchCompaniesName( $name, $pref_id );
        if (( $coms == null )||( count($coms) == 0) ) {
            $this->msg = "バス会社が見つかりません";
        } else {
            $this->list = $this->arrangeListComapny( $coms );
            $this->list_num = count($coms);
            $this->msg = "<b>検索結果</b> バス会社の数 " . $this->list_num ;
            $this->map_markers = $this->makeComMarkers( $coms );
            list( $this->map_lat, $this->map_lon, $this->map_zoom ) = 
                $this->util->calcMapCenterByComs( $coms );
        }
    }

    /**
     * arrangeListComapny
     */
    function arrangeListComapny( $coms ) {
        $list = "";
        foreach ( $coms as $com ) { 
            $a_com = "<a href='company.php?id=". $com["id"] ."'>". $com["name"] ."</a>";
            $list .= "<li>". $a_com ."</li>\n";
        }
        return $list;
    }

    /**
     * makeComMarkers
     */
    function makeComMarkers( $coms ) {
        if ( !is_array($coms) ) {
            $markers = "[]";
            return $markers;
        }
        $isFirst = true;
        $markers = "[";
        foreach ( $coms as $com ) {
            if ( !$isFirst ) {
                $markers .=  ",\n";
            }
            $isFirst = false;
            $lat = ($com["max_lat"] + $com["min_lat"] )/2;
            $lon = ($com["max_lon"] + $com["min_lon"] )/2;
            $markers .= "[". $lat .",". $lon  .",". $com["id"] .",\"". $com["name"] ."\"]";
        }
        $markers .= "]";
        return $markers;
    }

    /**
     * arrangePrefSelect
     * @param int $param_pref_id
     */
    function arrangePrefSelect( $param_pref_id ) {
        $text = "<option value=\"0\"";
        if ( $param_pref_id == 0 ) {
            $text .= $this->SELECTED;
        }
        $text .= ">全国</option>\n";
        $prefs = $this->db->getPrefs();
        foreach ( $prefs as $pref ) {
            $pref_id = $pref["id"];
            $text .= "<option value=\"". $pref_id ."\" ";
            if ( $param_pref_id == $pref_id ) {
                $text .= $this->SELECTED;
            }
            $text .= " >". $pref["name"] ."</option>\n";
        }
        return $text;    
    }
}

    /**
     * search
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    include_once("class/BusUtil.php");
    $search = new BusSearch();
    $SELECTED = $search->SELECTED;

    $map_php = "node.php";

    $lat = $search->map_lat;
    $lon = $search->map_lon;
    $name = "";
    $msg = "";
    $list_height = "auto";

    $node_selected = $SELECTED;
    $route_selected = "";
    $company_selected = "";

    $distance_selected_5 = "";
    $distance_selected_10 = $SELECTED;
    $distance_selected_15 = "";
    $distance_selected_30 = "";

    $mode = isset($_GET["mode"]) ? $_GET["mode"] : "node";
    $param_pref = isset($_GET["pref"]) ? intval($_GET["pref"]) : 0;

    // location
    if ( $mode == "location" ) {
        if ( !isset($_GET["lat"]) || !isset($_GET["lon"]) || $_GET["lat"] == ""  || $_GET["lon"] == "")  {
            $msg = "緯度と経度を入力してください";
        } else {
            $lat = floatval( $_GET["lat"] );
            $lon = floatval( $_GET["lon"] );
            $distance = isset($_GET["distance"]) ? intval( $_GET["distance"] ): 10;
            $search->makeLocation( $lat, $lon, $distance );
            $msg = $search->msg;
            $distance_selected_5 = $search->distance_selected_5;
            $distance_selected_10 = $search->distance_selected_10;
            $distance_selected_15 = $search->distance_selected_15;
            $distance_selected_30 = $search->distance_selected_30;
       }

    // route
    } else if ( $mode == "route" ) {
        $route_selected = $SELECTED;
        if ( !isset($_GET["name"]) || $_GET["name"] == "" )  {
            $msg = "バス路線の名前を入力してください";
        } else {
            $name = $_GET["name"];
            $search->makeRouteName( $name, $param_pref );
            $msg = $search->msg;
            $list_height = "480px";
        }

    // company
    } else if ( $mode == "company" ) {
        $company_selected = $SELECTED;
        if ( !isset($_GET["name"]) || $_GET["name"] == "" )  {
            $msg = "バス会社の名前を入力してください";
        } else {
            $name = $_GET["name"];
            $search->makeCompanyName( $name, $param_pref );
            $msg = $search->msg;
            $map_php = "company.php";
        }

    // node
    } else {
        $node_selected = $SELECTED;
        if ( !isset($_GET["name"]) || $_GET["name"] == "" )  {
            $msg = "バス停の名前を入力してください";
        } else {
            $name = $_GET["name"];
            $search->makeNodeName( $name, $param_pref );
            $msg = $search->msg;
        }
    }

    $pref_select = $search->arrangePrefSelect( $param_pref );

    $map_lat = $search->map_lat;
    $map_lon = $search->map_lon;
    $map_zoom = $search->map_zoom;
    $map_markers = $search->map_markers;
    $map_lines = $search->map_lines;

    $list_title = $search->list_title;
    $list_num = $search->list_num;
    $list_navi =$search->list_navi;
    $list  = $search->list;
    $node_num = $search->node_num;

    $map_use_cluster = ( $node_num > 100 )? "true": "false";
    $use_caution = ( $node_num > 990 )? true: false;
?>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>バス停 地図</title>
<style >
#map {
    width: 640px;
    height: 480px;
    padding: 8px;
}
#copyright {
    font-size: 70%;
}
#list {
    overflow-y:scroll;
    overflow-x:hidden;
    height: <?php echo $list_height; ?>;
}
</style>
    <script src="http://maps.googleapis.com/maps/api/js?sensor=false&hl=ja"></script>
    <script src="js/markerclusterer_compiled.js"></script>
<script type="text/javascript">
var map;
var prevId = 0;
var polylines = [];
var markerList = <?php echo $map_markers; ?>;
var lineList = <?php echo $map_lines; ?>;
/**
 * initialize
 */
function initialize() {
    var center = new google.maps.LatLng(<?php echo $map_lat; ?>,<?php echo $map_lon; ?>);
    map = new google.maps.Map(document.getElementById('map'), {
        center: center,
        zoom: <?php echo $map_zoom; ?>,
        mapTypeId: google.maps.MapTypeId.ROADMAP
    });
   map.addListener('center_changed', function() {
        var latlng = map.getCenter();
        document.getElementById("lat").value =  latlng.lat();
        document.getElementById("lon").value = latlng.lng();
    });
    setTimeout(showMakers, 1000);
    setTimeout(showRoutes, 1000);
}
/**
 * showMakers
 */
function showMakers() {
    var useCluster = <?php echo $map_use_cluster; ?>;
    var infowindow = new google.maps.InfoWindow();
    var markers = [];
    for (var i = 0; i < markerList.length; i++) {
        var latlng = new google.maps.LatLng(markerList[i][0],markerList[i][1]);
        var marker = new google.maps.Marker({
            position: latlng,
            title: markerList[i][3],
            icon: "http://maps.google.co.jp/mapfiles/ms/icons/bus.png",
            map: map
        });
// マーカーの数が1000以上だと効かない
        if ( i < 990 ) {
            google.maps.event.addListener( marker, 'click', ( function( marker, i) {
                return function() {
                    infowindow.setContent( "<a href=\"<?php echo $map_php; ?>?id=" + markerList[i][2] + "\">" + markerList[i][3] + "</a>" );
                    infowindow.open( map, marker );
                }
            }) ( marker, i ));
        }
        markers.push(marker);
    }
    if ( useCluster ) {
        var mcOptions = { gridSize: 50, maxZoom: 14};
        var markerCluster = new MarkerClusterer(map, markers, mcOptions);
    }
}
function showRoutes() {
    for (var i = 0; i < lineList.length; i++) {
        drowPolyline( i, lineList[i][1], lineList[i][2], "#0000FF", 3, false );
    }
}
/**
 * drowPolyline
 */
function drowPolyline( n, name, corves, color, weight, is_push ) {
    var polylineWindow = new google.maps.InfoWindow();
    for (var i = 0; i < corves.length; i++) {
        var lines = corves[i];
        var paths = [];
        for (var j = 0; j < lines.length/2; j++) {
            paths.push( new google.maps.LatLng( lines[2*j], lines[2*j+1] ));
        }
        var polyline = new google.maps.Polyline({
            path: paths,
            strokeColor: color,
            strokeWeight: weight,
            strokeOpacity: 1.0
        });
        polyline.setMap(map);
        google.maps.event.addListener( polyline, 'mouseover', ( function( polyline, n ) {
            return function() {
                polylineWindow.setContent( name );
                // 位置が取得できないので、地図の中心に表示する
                polylineWindow.setPosition( map.getCenter() );
                polylineWindow.open( map );
            }
        }) ( polyline, n ));
        google.maps.event.addListener( polyline, 'mouseout', ( function( polyline, n ) {
            return function() {
                polylineWindow.close();
            }
        }) ( polyline, n ));
        google.maps.event.addListener( polyline, 'click', ( function( polyline, n ) {
            return function() {
                highligthLine( lineList[n][0] );
            }
        }) ( polyline, n ));
        if ( is_push ) {
            polylines.push( polyline );
        }
    }
}
/**
 * highligthLine
 * when click line on map, or check radio button in list
 */
function highligthLine( id ) {
    // clear prev lines
    for (var i = 0; i < polylines.length; i++) {
        polylines[i].setMap(null);
    }
    // draw new highligth lines
    polylines = [];
    for (var i = 0; i < lineList.length; i++) {
        if ( id == lineList[i][0] ) {
            drowPolyline( i, lineList[i][1], lineList[i][2], "#FF0000", 5, true );
        }
    }
    highligthList( id );
}
/**
 * highligthList
 */
function highligthList( id ) {
    // clear prev in list
    if ( prevId != 0 ) {
        document.getElementById( "route_" + prevId ).style.backgroundColor = "transparent";
    }
    // highligth new in list
    var ele = document.getElementById( "route_" + id );
    ele.style.backgroundColor = "#FFFF00";
    var nodes = ele.childNodes;
    for (var i = 0; i < nodes.length; i++) {
        var ch = nodes[i];
        if (ch.nodeName == "INPUT") {
            ch.checked = "checked";
        }
    }
    prevId = id;
} 
</script>
</head>
<body onload="initialize()">
<table><tr><td>
<a href="index.php">[TOP]</a>
</td><td>
<a href="about.html">[このサイトについて]</a>
</td><td>
<!--
<form action="search.php">
<input type="text" name="name" value=""/>
<input type="submit" value="バス停を検索する" />
-->
</form>
</td></tr></table>
<h3>バス停の検索</h3>
<form>
名前 
<select name="mode">
    <option value="node" <?php echo $node_selected; ?>>バス停</option>
    <option value="route" <?php echo $route_selected; ?>>バス路線</option>
    <option value="company" <?php echo $company_selected; ?>>バス会社</option>
</select>
<select name="pref">
    <?php echo $pref_select; ?>
</select>
 <input type="text" name="name" value="<?php echo $name; ?>"/>
<input type="submit" value="検索">
</form>
<form>
<input type="hidden" name="mode" value="location"/>
緯度 <input type="text" id="lat" name="lat" value="<?php echo $map_lat; ?>"/>
経度 <input type="text" id="lon" name="lon" value="<?php echo $map_lon; ?>"/>
<select name="distance">
    <option value="5" <?php echo $distance_selected_5; ?>>500m</option>
    <option value="10" <?php echo $distance_selected_10; ?>>1Km</option>
    <option value="15" <?php echo $distance_selected_15; ?>>1.5Km</option>
    <option value="30" <?php echo $distance_selected_30; ?>>3Km</option>
</select>
<input type="submit" value="検索">
</form>
<br/>
<?php echo $msg; ?><br/>
<table><tr><td valign="top">
<div id="map"></div>
<!--
<form action="search.php">
<input type="hidden" name="mode" value="location"/>
中心 緯度 <input type="text" id="lat" name="lat" value="<?php echo $lat; ?>"/>
経度 <input type="text" id="lon" name="lon" value="<?php echo $lat; ?>"/>
<input type="submit" value="検索">
</form>
-->
<?php if(!$use_caution) echo "<!--"; ?>
<b>注意</b> バス停の数が1000以上のときは、<br/>
マーカーをクリックしても、吹き出しは出ません。<br/>
検索機能をご利用ください。<br/>
<?php if(!$use_caution) echo "-->"; ?>
</td><td valign="top">
　<b><?php echo $list_title; ?></b> (<?php echo $list_num; ?>)
<ul id="list">
<?php echo $list; ?>
</ul>
　<?php echo $list_navi; ?>
</td></tr></table>
<div id="copyright">
<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="クリエイティブ・コモンズ・ライセンス" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a>
    Author: Kenichi Ohwada<br/>
</div>
</body>
</html>
