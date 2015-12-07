<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/**
 * makeListNode
 */
function makeListNode( $db, $nodes ) {
    $list = "";
    foreach ( $nodes as $node ) { 
        $a_node = "<a href='node.php?id=". $node["id"] ."'>". $node["name"] ."</a>";

        // pref
        $pref = $db->getCachedPrefById( $node["pref_id"] );
        $a_pref = "<a href='prefecture.php?id=". $node["pref_id"] ."'>". $pref["name"] ."</a>";
  
        // routes
        $is_first = true;
        $route_text = $a_pref ." : ";
        $routes = $db->getRoutesByNodeId( $node["id"] );
        foreach ($routes as $route) {
            if ( !$is_first ) {
                $route_text .= ",\n";
            }
            $a_route = "<a href='route.php?id=". $route["id"] ."'>". $route["bus_line"] ."</a>";
            $route_text .= $a_route;
            $is_first = false;
        }
        $list .= "<li>". $a_node ." (". $route_text .")</li>\n";
    }
    return $list;
}

/**
 * makeListRoute
 */
function makeListRoute( $routes ) {
    $list = "";
    foreach ( $routes as $route ) { 
        $a_com = "<a href='company.php?id=". $route["company_id"] ."'>". $route["company"] ."</a>";
        $a_route = "<a href='route.php?id=". $route["id"] ."'>". $route["bus_line"] ."</a>";
        $list .= "<li>". $a_com ." - ". $a_route ."</li>\n";
    }
    return $list;
}

/**
 * makeListComapny
 */
function makeListComapny( $coms ) {
    $list = "";
    foreach ( $coms as $com ) { 
        $a_com = "<a href='company.php?id=". $com["id"] ."'>". $com["name"] ."</a>";
        $list .= "<li>". $a_com ."</li>\n";
    }
    return $list;
}

    /**
     * search
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    include_once("class/BusUtil.php");
    $util = new BusUtil();
    $db = new BusDb();
    $db->connect();
    $SELECTED = " selected=\"selected\" ";

    // 長野
    $map_lat = 36.110446929932;
    $map_lon = 138.08520507812;
    $map_zoom = 10;

    $map_markers = "[]";
    $list_title = "バス停";
    $list_num = 0;
    $list = "";
    $msg = "";
    $name = "";
    $lat = $map_lat;
    $lon = $map_lon;
    $node_num = 0;
    $flag_node = false;
    $node_selected = "";
    $route_selected = "";
    $company_selected = "";

    $mode = isset($_GET["mode"]) ? $_GET["mode"] : "node";
    $param_pref = isset($_GET["pref"]) ? intval($_GET["pref"]) : 0;

    // location
    if ( $mode == "location" ) {
        if ( !isset($_GET["lat"]) || !isset($_GET["lon"]) || $_GET["lat"] == ""  || $_GET["lon"] == "")  {
            $msg = "緯度と経度を入力してください";
        } else {
            $flag_node = true;
            $lat = floatval( $_GET["lat"] );
            $lon = floatval( $_GET["lon"] );
            $map_lat = $lat;
            $map_lon = $lon;
//            $start_time =  microtime(true);
            $nodes = $db->searchNodesPoint( $lat, $lon );
//            $elapsed_time =  microtime(true) - $start_time;
//            echo $elapsed_time;
        }

    // route
    } else if ( $mode == "route" ) {
        $route_selected = $SELECTED;
        if ( !isset($_GET["name"]) || $_GET["name"] == "" )  {
            $msg = "バス路線の名前を入力してください";
        } else {
            $list_title = "バス路線";
            $name = $_GET["name"];
            $routes = $db->searchRoutesName( $name, $param_pref );
            if (( $routes == null )||( count($routes) == 0) ) {
                $msg = "バス路線が見つかりません";
            } else {
                $list = makeListRoute( $routes );
                $list_num = count($routes);
                $msg = "<b>検索結果</b> バス路線の数 " . $list_num ;
            }
        }

    // company
    } else if ( $mode == "company" ) {
        $company_selected = $SELECTED;
        if ( !isset($_GET["name"]) || $_GET["name"] == "" )  {
            $msg = "バス会社の名前を入力してください";
        } else {
            $list_title = "バス会社";
            $name = $_GET["name"];
            $coms = $db->searchCompaniesName( $name, $param_pref );
            if (( $coms == null )||( count($coms) == 0) ) {
                $msg = "バス会社が見つかりません";
            } else {
                $list = makeListComapny( $coms );
                $list_num = count($coms);
                $msg = "<b>検索結果</b> バス会社の数 " . $list_num ;
            }
        }

    // node
    } else {
        $node_selected = $SELECTED;
        if ( !isset($_GET["name"]) || $_GET["name"] == "" )  {
            $msg = "バス停の名前を入力してください";
        } else {
            $flag_node = true;
            $name = $_GET["name"];
            $nodes = $db->searchNodesName( $name, $param_pref );
        }
    }

    if ( $flag_node ) {
        $list_title = "バス停";
        if (( $nodes == null )||( count($nodes) == 0) ) {
            $msg = "バス停が見つかりません";
        } else {
            list( $max_lat, $min_lat, $max_lon, $min_lon) = $util->calcArea( $nodes );
            list( $map_lat, $map_lon, $map_zoom ) = 
                $util->calcMapCenter( $max_lat, $min_lat, $max_lon, $min_lon );
            $map_markers = $util->makeMarkers( $nodes );
            $list = makeListNode( $db, $nodes );
            $node_num = count($nodes);
            $list_num = count($nodes);
            $msg = "<b>検索結果</b> バス停の数 " . $list_num ;
        }
    }

    // pref select
    $pref_select = "<option value=\"0\"";
    if ( $param_pref == 0 ) {
        $pref_select .= $SELECTED;
    }
    $pref_select .= ">全国</option>\n";
    $prefs = $db->getPrefs();
    foreach ( $prefs as $pref ) {
        $pref_id = $pref["id"];
        $pref_select .= "<option value=\"". $pref_id ."\" ";
        if ( $param_pref == $pref_id ) {
            $pref_select .= $SELECTED;
        }
        $pref_select .= " >". $pref["name"] ."</option>\n";
    }

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
</style>
    <script src="http://maps.googleapis.com/maps/api/js?sensor=false&hl=ja"></script>
    <script src="js/markerclusterer_compiled.js"></script>
<script type="text/javascript">
var map;
var markerList = <?php echo $map_markers; ?>;
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
<ul><?php echo $list; ?></ul>
</td></tr></table>
<div id="copyright">
Copyright (c) 2015 Kenichi Ohwada All Rights Reserved
</div>
</body>
</html>
