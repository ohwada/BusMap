<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

    /**
     * route
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    include_once("class/BusUtil.php");
    $util = new BusUtil();
    $db = new BusDb();
    $db->connect();
    $SEARCH_FORM = $util->makeSearchForm();

    // route
    $id = isset($_GET["id"]) ? intval($_GET["id"]): 1;
    $route = $db->getRouteById( $id );
    $com_id = $route["company_id"];

    $route_name = $route["bus_line"];
    $route_day  = $route["day"];
    $route_saturday = $route["saturday"];
    $route_holiday = $route["holiday"];
    $route_type =  $util ->getBusType( $route["type"] );
    list( $map_lat, $map_lon, $map_zoom ) = 
        $util->calcMapCenter( $route["max_lat"], $route["min_lat"], $route["max_lon"], $route["min_lon"] );
    $map_corves = $db->getMapCorvesByRoutes( array($route) );
    $map_lines = $util->makeLines( $map_corves );

    // company
    $com_id = $route["company_id"];
    $com = $db->getCompanyById( $route["company_id"] );
    $url_corp = "<a href=\"company.php?id=". $com_id. "\">". $com["name"] ."</a>";
    $url_home = "";
    if ( $com["url_home"] ) {
        $url_home = "<a href=\"". $com["url_home"] ."\" target=\"_blank\">[ホームページ]</a>";
    }
    $url_search = "";
    if ( $com["url_search"] ) {
        $url_search = "<a href=\"". $com["url_search"] ."\" target=\"_blank\">[時刻表]</a>";
    }

    // nodes
    $nodes = $db->getNodesByRouteId( $id );
    $map_markers = $util->makeNodeMarkers( $nodes );
    $list = $util->makeNodeList( $nodes );
    $node_num = count( $nodes );

    // prefs
    $prefs = $db->getPrefsByRouteId( $id );
    list($pref_renge, $pref_delmita) = $util->makePrefRenge( $prefs );

    $map_use_cluster = "false";
    $use_caution = false;
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
</style>
    <script src="http://maps.googleapis.com/maps/api/js?sensor=false&hl=ja"></script>
    <script src="js/markerclusterer_compiled.js"></script>
<script type="text/javascript">
var map;
var polylines = [];
//var prevId = 0;
var markerList = <?php echo $map_markers; ?>;
var lineList = <?php echo $map_lines; ?>;
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
function showRoutes() {
    for (var i = 0; i < lineList.length; i++) {
        drowPolyline( i, lineList[i][1], lineList[i][2], "#0000FF", 3 );
    }
}
function drowPolyline( n, name, corves, color ) {
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
            strokeOpacity: 1.0,
            strokeWeight: 3
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
        polylines.push( polyline );
    }
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
バス路線 <b><?php echo $route_name; ?></b><br/>
バス会社 <?php echo $url_corp; ?>
 <?php echo $url_home; ?>
 <?php echo $url_search; ?><br/> 
バス区分 <?php echo $route_type; ?>
<?php echo $pref_delmita; ?>
範囲 <?php echo $pref_renge; ?><br/>
一日当たりの運行本数
 : 平日 <?php echo $route_day; ?>
 : 土曜日 <?php echo $route_saturday; ?>
 : 日祝日 <?php echo $route_holiday; ?><br/>
<table><tr><td valign="top">
<div id="map"></div>
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
<a href="data.php?mode=route&kind=kml&id=<?php echo $id; ?>">KML</a>
<a href="data.php?mode=route&kind=gpx&id=<?php echo $id; ?>">GPX</a>
<!--
<a href="data.php?mode=route&kind=osm&id=<?php echo $id; ?>">OSM</a>
-->
</td><td valign="top">
　<b>バス停</b> (<?php echo $node_num; ?>)
<ul><?php echo $list; ?></ul>
</td></tr></table>
<div id="copyright">
<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="クリエイティブ・コモンズ・ライセンス" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a>
    Author: Kenichi Ohwada<br/>
</div>
</body>
</html>
