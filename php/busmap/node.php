<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

    /**
     * node
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    include_once("class/BusUtil.php");
    $util = new BusUtil();
    $db = new BusDb();
    $db->connect();
    $SEARCH_FORM = $util->makeSearchForm();
    $map_zoom = "14";
    $map_use_cluster = "false";
    $use_caution = false;  

    // node
    $id = isset($_GET["id"]) ? intval($_GET["id"]): 1;
    $node = $db->getNodeById( $id );
    $node_name = $node["name"];
    $map_lat = $node["lat"];
    $map_lon = $node["lon"]; 
    $pref_id = $node["pref_id"];
    $pref = $db->getPrefById( $pref_id );
    $pref_title = "<a href=\"prefecture.php?id=". $pref_id ."\">". $pref["name"] ."</a>";

    // routes
    $routes = $db->getRoutesByNodeId( $id );
    list($list_route, $list_route_navi, $list_height, $route_id_arr) = 
        $util->makeRouteList( $routes, true );
    $map_corves = $db->getMapCorvesByRoutes( $routes );
    $map_lines = $util->makeLines( $map_corves );
    $route_num = count($routes);

    // nodes
    $nodes = $db->searchNodesPoint( $map_lat, $map_lon );
    $map_markers = $util->makeMarkers( $nodes );
    $list_node = $util->makeNodeListExcept( $nodes, $id );
    $node_count = count( $nodes );
    $node_num = ( $node_count > 0 ) ? $node_count - 1: 0;
	
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
var polylines = [];
var prevId = 0;
var markerList = <?php echo $map_markers; ?>;
var lineList = <?php echo $map_lines; ?>;
/**
 * initialize
 */
function initialize() {
    var useCluster = <?php echo $map_use_cluster; ?>;
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
    setTimeout(showMakers, 100);
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
                    infowindow.setContent( "<a href='node.php?id=" + markerList[i][2] + "'>" + markerList[i][3] + "</a>" );
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
/**
 * showRoutes
 */
function showRoutes() {
    for (var i = 0; i < lineList.length; i++) {
        drowPolyline( i, lineList[i][1], lineList[i][2], "#0000FF", 3 );
    }
}
/**
 * drowPolyline
 */
function drowPolyline( n, name, corves, color, weight ) {
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
        polylines.push( polyline );
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
    // draw new lines
    polylines = [];
    for (var i = 0; i < lineList.length; i++) {
        if ( id != lineList[i][0] ) {
            drowPolyline( i, lineList[i][1], lineList[i][2], "#0000FF", 3 );
        }
    }
    // draw highligt lines
    for (var i = 0; i < lineList.length; i++) {
        if ( id == lineList[i][0] ) {
            drowPolyline( i, lineList[i][1], lineList[i][2], "#FF0000", 5 );
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
<?php echo $SEARCH_FORM; ?>
</td></tr></table>
<h3>バス停の地図</h3>
バス停 <?php echo $pref_title; ?> >> <b><?php echo $node_name; ?></b><br/>
<table><tr><td valign="top">
<div id="map"></div>
<div id="line"></div>
<form action="search.php">
<input type="hidden" name="mode" value="location"/>
中心 緯度 <input type="text" id="lat" name="lat" value="<?php echo $map_lat; ?>"/>
経度 <input type="text" id="lon" name="lon" value="<?php echo $map_lon; ?>"/>
<input type="submit" value="検索">
</form>
<?php if(!$use_caution) echo "<!--"; ?>
<b>注意</b> バス停の数が1000以上のときは、<br/>
マーカーをクリックしても、吹き出しは出ません。<br/>
検索機能をご利用ください。<br/>
<?php if(!$use_caution) echo "-->"; ?>
<b>ダウンロード</b>
<a href="terms.html">利用規約</a>
<a href="data.php?mode=node&kind=kml&id=<?php echo $id; ?>">KML</a>
<a href="data.php?mode=node&kind=gpx&id=<?php echo $id; ?>">GPX</a>
<!--
<a href="data.php?mode=node&kind=osm&id=<?php echo $id; ?>">OSM</a>
-->
</td><td valign="top">
<div>
　<b>バス路線</b> (<?php echo $route_num; ?>) <br/>
<ul id="list">
<?php echo $list_route; ?>
</ul>
　<?php echo $list_route_navi; ?>
　<b>近くのバス停</b> (<?php echo $node_num; ?>) <br/>
<ul>
<?php echo $list_node; ?>
</ul>
</div>
</td></tr></table>
<div id="copyright">
Copyright (c) 2015 Kenichi Ohwada All Rights Reserved
</div>
</body>
</html>
