<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

    /**
     * prefecture
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    include_once("class/BusUtil.php");
    $util = new BusUtil();
    $db = new BusDb();
    $db->connect();
    $SEARCH_FORM = $util->makeSearchForm();

    // prefs default:東京
    $id = isset($_GET["id"]) ? intval($_GET["id"]): 13;
    $pref = $db->getPrefById( $id );
    $pref_name = $pref["name"];
    $node_num = $pref["node_num"];
    $route_num = $pref["route_num"];
    list( $map_lat, $map_lon, $map_zoom ) = 
        $util->calcMapCenter( $pref["max_lat"], $pref["min_lat"], $pref["max_lon"], $pref["min_lon"] );

    // companies
    $list = "";
    $companies = $db->getCompaniesByPrefId( $id );
    foreach ( $companies as $com ) {
        $url = "<a href=\"company.php?id=". $com["id"] ."\">". $com["name"] ."</a>";
        $list .= "<li>". $url ."</li>\n";
    }
    $com_num = count($companies);

    $map_use_cluster = "true";
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
</style>
<script src="http://maps.googleapis.com/maps/api/js?sensor=false&hl=ja"></script>
<script src="js/markerclusterer_compiled.js"></script>
<script src="data/prefecture_<?php echo $id; ?>.data"></script>
<script type="text/javascript">
var map;
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
<b><?php echo $pref_name; ?></b> バス会社(<?php echo $com_num; ?>) バス路線(<?php echo $route_num; ?>) バス停(<?php echo $node_num; ?>)
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
<a href="data.php?mode=prefecture&kind=kml&id=<?php echo $id; ?>">KML</a>
<a href="data.php?mode=prefecture&kind=gpx&id=<?php echo $id; ?>">GPX</a>
<!--
<a href="data.php?mode=prefecture&kind=osm&id=<?php echo $id; ?>">OSM</a>
-->
</td><td valign="top">
　<b>バス会社</b> (<?php echo $com_num; ?>)
<ul><?php echo $list; ?></ul>
</td></tr></table>
<div id="copyright">
<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="クリエイティブ・コモンズ・ライセンス" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a>
    Author: Kenichi Ohwada<br/>
</div>
</body>
</html>
