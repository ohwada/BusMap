<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

    /**
     * index
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    include_once("class/BusUtil.php");
    $util = new BusUtil();
    $db = new BusDb();
    $db->connect();
    $SEARCH_FORM = $util->makeSearchForm();
    $DIR_IMAGES = HOST . "/images/";

    // 長野
    $map_lat = 36.110446929932;
    $map_lon = 138.08520507812;
    $map_zoom = 7;  

    // prefs
    $is_first = true;
    $list = "";
    $map_markers = "[";
    $prefs = $db->getPrefs();
    foreach ( $prefs as $pref ) {
        $pref_id = $pref["id"];
        $pref_name = $pref["name"];
        $url = "<a href=\"prefecture.php?id=". $pref_id ."\">". $pref_name ."</a>";
        $list .= "<li>". $url ."</li>\n";
        list($lat, $lon, $zoom) = $util->calcMapCenter( $pref["max_lat"], $pref["min_lat"], $pref["max_lon"], $pref["min_lon"] );
        if (!$is_first) {
            $map_markers .= ",\n";
        }
        $map_markers .= "[". $lat .",". $lon .",". $pref_id .",\"". $pref_name."\"]";
        $is_first = false;
    }
    $map_markers .= "]";

    $com_num = $db->getCompanyCount();
    $route_num = $db->getRouteCount();
    $node_num = $db->getNodeCount();
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
<script src="data/japan.data"></script>
<script type="text/javascript">
var map;
var markers = [];
var circles = [];
var icon_normal = new google.maps.MarkerImage(
    "<?php echo $DIR_IMAGES; ?>pref_1.png",
    new google.maps.Size(52,52),
    new google.maps.Point(0,0),
    new google.maps.Point(26,26)
);
var icon_small = new google.maps.MarkerImage(
    "<?php echo $DIR_IMAGES; ?>pref_1_s.png",
    new google.maps.Size(36,16),
    new google.maps.Point(0,0),
    new google.maps.Point(18,8)
);
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
   map.addListener('zoom_changed', function() {
        showMakers();
    });
    showMakers();
}
/**
 * showMakers
 */
function showMakers() {
    // clear prev markers
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(null);
   }
    // draw new markers
    markers = [];
    var zoom = map.getZoom();
    for (var i = 0; i < markerList.length; i++) {
        var id = markerList[i][2];
        icon_normal.url =
            "<?php echo $DIR_IMAGES; ?>pref_" + id + ".png";
        icon_small.url = 
            "<?php echo $DIR_IMAGES; ?>pref_" + id + "_s.png";
        var icon = icon_normal;
        if ( zoom < 7 ) {
            icon = icon_small;
        }
        var latlng = new google.maps.LatLng(markerList[i][0],markerList[i][1]);
        var marker = new google.maps.Marker({
            position: latlng,
            title: markerList[i][3],
            icon: icon
        });
        marker.setMap(map);
        google.maps.event.addListener( marker, 'click', ( function( marker, i) {
            return function() {
                location.href = "prefecture.php?id=" + markerList[i][2];
            }
        }) ( marker, i ));
        markers.push(marker);
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
<b>日本全国</b> バス会社(<?php echo $com_num; ?>) バス路線(<?php echo $route_num; ?>) バス停(<?php echo $node_num; ?>)
<table><tr><td valign="top">
<div id="map"></div>
<form action="search.php">
<input type="hidden" name="mode" value="location"/>
中心 緯度 <input type="text" id="lat" name="lat" value="<?php echo $map_lat; ?>"/>
経度 <input type="text" id="lon" name="lon" value="<?php echo $map_lon; ?>"/>
<input type="submit" value="検索">
</form>
</td><td valign="top">
　<b>都道府県</b>
<ul><?php echo $list; ?></ul>
</td></tr></table>
<div id="copyright">
Copyright (c) 2015 Kenichi Ohwada All Rights Reserved
</div>
</body>
</html>
