<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/**
 * BusUtil
 */
class BusUtil {

    // 長野
    var $DEFAULT_LAT = 36.110446929932;
    var $DEFAULT_LON = 138.08520507812;
    var $DEFAULT_ZOOM = 8;  

    /**
     * constractor
     */ 
    function BusUtil() {
         // dummy
    }

    /**
     * バス区分コード
     * 1	路線バス（民間）	バス事業者が運行しているもの
     * 2	路線バス（公営）	市営バス等自治体の交通部局が運行しているもの
     * 3	コミュニティバス	自治体が運営しているもの
     * 4	デマンドバス	予約がなければ運行しないもの
     * 5	その他	以上に含まれないもの
     */
    function getBusType( $type ) {
        switch ( $type ) {
            case 1:
                $text = "路線バス（民間）";
                break;
            case 2:
                $text = "路線バス（公営）";
                break;
            case 3:
                $text = "コミュニティバス";
                break;
            case 4:
                $text = "デマンドバス";
                break;
            case 5:
            default:
                $text = "その他";
                break;
        }
        return $text;
    }

    function calcMapCenterByComs( $coms ) {
        list( $max_lat, $min_lat, $max_lon, $min_lon ) = 
            $this->calcAreaByComs( $coms );
        return $this->calcMapCenter( $max_lat, $min_lat, $max_lon, $min_lon );
    }

    /**
     * calcMapCenterByRoutes
     * @param array of row $routes
     * @return array
     */
    function calcMapCenterByRoutes( $routes ) {
        list( $max_lat, $min_lat, $max_lon, $min_lon ) = 
            $this->calcAreaByRoutes( $routes );
        return $this->calcMapCenter( $max_lat, $min_lat, $max_lon, $min_lon );
    }

    /**
     * calcMapCenterByNodes
     * @param array of row $nodes
     * @return array
     */
    function calcMapCenterByNodes( $nodes ) {
        list( $max_lat, $min_lat, $max_lon, $min_lon ) = 
            $this->calcAreaByNodes( $nodes );
        return $this->calcMapCenter( $max_lat, $min_lat, $max_lon, $min_lon );
    }

    /**
     * calcAreaByNodes
     */
    function calcAreaByNodes( $nodes ) {
        $max_lat = -180.0;
        $min_lat =180.0;
        $max_lon = -180.0;
        $min_lon =180.0;
        foreach ( $nodes as $node ) { 
            $lat = $node["lat"];
            $lon = $node["lon"];
            if ($lat > $max_lat) {
                $max_lat = $lat;
            }
            if ($lon > $max_lon) {
                $max_lon = $lon;
            }
            if ($lat < $min_lat) {
                $min_lat = $lat;
            }
            if ($lon < $min_lon) {
                $min_lon = $lon;
            }
        }
        return array($max_lat, $min_lat, $max_lon, $min_lon);
    }

    /**
     * calcAreaByComs
     */
    function calcAreaByComs( $coms ) {
        return $this->calcAreaByMaxMin( $coms );
    }

    /**
     * calcAreaByRoutes
     */
    function calcAreaByRoutes( $routes ) {
        return $this->calcAreaByMaxMin( $routes );
    }

    /**
     * calcAreaByMaxMin
     */
    function calcAreaByMaxMin( $arr ) {
        $max_lat = -180.0;
        $min_lat =180.0;
        $max_lon = -180.0;
        $min_lon =180.0;
        foreach ( $arr as $a ) { 
            $a_max_lat = $a["max_lat"];
            $a_min_lat = $a["min_lat"];
            $a_max_lon = $a["max_lon"];
            $a_min_lon = $a["min_lon"];
            if ($a_max_lat > $max_lat) {
                $max_lat = $a_max_lat;
            }
            if ($a_max_lon > $max_lon) {
                $max_lon = $a_max_lon;
            }
            if ($a_min_lat < $min_lat) {
                $min_lat = $a_min_lat;
            }
            if ($a_min_lon < $min_lon) {
                $min_lon = $a_min_lon;
            }
        }
        return array($max_lat, $min_lat, $max_lon, $min_lon);
    }

    /**
     * calcMapCenter
     * @param float $max_lat
     * @param float $min_lat
     * @param float $max_lon
     * @param float $min_lon
     * @return array (lat, lon, zoom)
     */
    function calcMapCenter( $max_lat, $min_lat, $max_lon, $min_lon ) {
        $lat = ( $max_lat + $min_lat )/2;
        $lon = ( $max_lon + $min_lon )/2;
        if (( $lat > 10 )&&( $lon > 10 )) {
            $zoom = $this->calcZoom( $max_lat, $min_lat, $max_lon, $min_lon );
        } else {
            $lat = $this->DEFAULT_LAT;
            $lon = $this->DEFAULT_LON;
            $zoom = $this->DEFAULT_ZOOM;            
        }
        return array($lat, $lon, $zoom);
    }

    /**
     * calcZoom
     */
    function calcZoom( $max_lat, $min_lat, $max_lon, $min_lon ) {
        $lat = (int) ceil( 1.2 * ( $max_lat - $min_lat ) / 0.000271658 );
        $lon = (int) ceil( 1.2 * ( $max_lon - $min_lon ) / 0.000439882 );
        $renge = max( $lat,  $lon );
        if ( $renge > 131072 ) {
            $zoom = 3;
        } else if ( $renge > 65536 ) {
            $zoom = 4;
        } else if ( $renge > 32768 ) {
            $zoom = 5;
        } else if ( $renge > 16384 ) {
            $zoom = 6;
        } else if ( $renge > 8192 ) {
            $zoom = 7;
        } else if ( $renge > 4096 ) {
            $zoom = 8;
        } else if ( $renge > 2048 ) {
            $zoom = 9;
        } else if ( $renge > 1024 ) {
            $zoom = 10;
        } else if ( $renge > 512 ) {
            $zoom = 11;
        } else if ( $renge > 256 ) {
            $zoom = 12;
        } else if ( $renge > 128 ) {
            $zoom = 13;
        } else if ( $renge > 64 ) {
            $zoom = 14; 
        } else if ( $renge > 32 ) {
            $zoom = 15;
        } else if ( $renge > 16 ) {
            $zoom = 16;  
        } else if ( $renge > 8 ) {
            $zoom = 17;
        } else if ( $renge > 4 ) {
            $zoom = 18;   
        } else if ( $renge > 2 ) {
            $zoom = 19;   
        } else if ( $renge > 1 ) {
            $zoom = 20;  
        } else {
            $zoom = 21;
        }
        if ( $zoom < 6 ) {
            $zoom = 6;
        }
        if ( $zoom > 16 ) {
            $zoom = 16;
        }
        return $zoom;
    }

    /**
     * makeComMarkers
     * @param array of row $coms
     * @return string
     */
    function makeComMarkers( $coms ) {
        if ( !is_array($coms) ) {
            $markers = "[]";
            return $markers;
        }
        $arr = array();
        foreach ( $coms as $com ) {
            $tmp = $com;
            $tmp["lat"] = ($com["max_lat"] + $com["min_lat"] )/2;
            $tmp["lon"] = ($com["max_lon"] + $com["min_lon"] )/2;
            $arr[] = $tmp;
        }
        return $this->makeMarkersCommon( $arr );
    }

    /**
     * makeNodeMarkers
     * @param array of row $nodes
     * @return string
     */
    function makeNodeMarkers( $nodes ) {
        return $this->makeMarkersCommon( $nodes );
    }

   /**
     * makeMarkersCommon
     */
    function makeMarkersCommon( $arr ) {
        if ( !is_array($arr) ) {
            $markers = "[]";
            return $markers;
        }
        $isFirst = true;
        $markers = "[";
        foreach ( $arr as $a ) {
            if ( !$isFirst ) {
                $markers .=  ",\n";
            }
            $isFirst = false;
            $markers .= "[". $a["lat"] .",". $a["lon"]  .",". $a["id"] .",\"". $a["name"] ."\"]";
        }
        $markers .= "]";
        return $markers;
    }

    /**
     * makeLines
     * @param array of row $routes
     * @return string
     */
    function makeLines( $routes ) {
        $line = "[";
        $isFirst = true;
        foreach( $routes as $route ) {
            if ( !$isFirst ) {
                $line .= ",\n";
            }
            $line .= "[";
            $line .= "". $route["id"] .",";
            $line .= "\"". $route["bus_line"] ."\",";
            $line .= $this->makeCurves( $route["curves"] );
            $line .= "]";
            $isFirst = false;
        }
        $line .= "]";
        return $line;
    }

    /**
     * makeCurves
     * @param array of row $curves
     * @return string
     */
    function makeCurves( $curves ) {
        $line = "[";
        $isFirst = true;
        foreach( $curves as $curve ) {
            if ( !$isFirst ) {
                $line .= ",\n";
            }
            $line .= "[" . $curve . "]";
            $isFirst = false;
        }
        $line .= "]";
        return $line;
    }

    /**
     * makeNodeList
     * @param array of row $nodes
     * @return string
     */
    function makeNodeList( $nodes ) {
        return $this->makeNodeListExcept( $nodes, 0 );
    }

    /**
     * makeNodeListExcept
     * @param array of row $nodes
     * @param int $id
     * @return string
     */
    function makeNodeListExcept( $nodes, $id ) {
        $list = "";
        if ( !is_array($nodes) ) {
            return $list;
        }
        foreach ( $nodes as $node ) { 
            $node_id = $node["id"];
            // except the specified node
            if ( $node_id == $id ) continue;
            $url = "<a href='node.php?id=". $node_id ."'>". $node["name"] ."</a>";
            $list .= "<li>". $url ."</li>\n";
        }
        return $list;
    }

    /**
     * makePrefRenget
     * @param array of row $prefs
     * @return string
     */
    function makePrefRenge( $prefs ) {
        $is_first = true;
        $renge = "";
        foreach ( $prefs as $pref ) {
            if ( !$is_first ) {
                $renge .= ", \n";
            }
            $renge .= "<a href=\"prefecture.php?id=". $pref["id"] ."\">". $pref["name"] ."</a>";
            $is_first = false;
        }
        $delmita = ( count($prefs) < 6 )? " : " : "<br/>\n"; 
        return array($renge, $delmita);
    }

    /**
     * makeRouteList
     * @param array of row $routes
     * @param boolean $flag
     * @return array
     */
    function makeRouteList( $routes, $flag ) {
        $list = "";
        foreach ( $routes as $route ) {
            $route_id = $route["id"];  
            $li = "<li id=\"route_". $route_id ."\">";
            $input = "<input type=\"radio\" name=\"highligth\" onclick=\"highligthLine(". $route_id .");\" /> ";
            $a_company = "<a href=\"company.php?id=". $route["company_id"]. "\">". $route["company"] ."</a>";
            $a_route = "<a  href=\"route.php?id=". $route_id ."\" >". $route["bus_line"] ."</a>";
            $list .= $li. $input ." ";
            if ( $flag ) {
                $list .= $a_company ." - ";
            }
            $list .= $a_route ."</li>\n";
        }
        $list_navi = "";
        $list_height = "auto";
        if ( count($routes) > 16 ) {
            $list_navi = "スクロールします<br/><br/>\n";
            $list_height = "480px";
        }
        return array($list, $list_navi, $list_height);
    }

    /**
     * makeComIds
     * @param array of row $coms
     * @return array of int
     */
    function makeComIds($coms) {
        return $this->makeIds($coms);
    }

    /**
     * makeRouteIds
     * @param array of row $routes
     * @return array of int
     */
    function makeRouteIds($routes) {
        return $this->makeIds($routes);
    }

    /**
     * makeNodeIds
     * @param array of row $nodes
     * @return array of int
     */
    function makeNodeIds($nodes) {
        return $this->makeIds($nodes);
    }

    /**
     * makeIds
     */
    function makeIds($arr) {
        $ret = array();
        foreach( $arr as $a ) {
            $ret[] = $a["id"];
        }
        return $ret;
    }

   /**
     * makeSearchForm
     * @return string
     */
    function makeSearchForm() {
        $select = $this->makeSelectPref();
$text =<<< EOT
<form action="search.php">
<input type="text" name="name" value=""/>
$select
<input type="submit" value="バス停を検索する" />
</form>
EOT;
        return $text;
    }

   /**
     * makeSelectPref
     * @return string
     */
    function makeSelectPref() {
$text =<<< EOT
<select name="pref">
    <option value="0" selected="selected" >全国</option>
    <option value="1">北海道</option>
    <option value="2">青森</option>
    <option value="3">岩手</option>
    <option value="4">宮城</option>
    <option value="5">秋田</option>
    <option value="6">山形</option>
    <option value="7">福島</option>
    <option value="8">茨城</option>
    <option value="9">栃木</option>
    <option value="10">群馬</option>
    <option value="11">埼玉</option>
    <option value="12">千葉</option>
    <option value="13">東京</option>
    <option value="14">神奈川</option>
    <option value="15">新潟</option>
    <option value="16">富山</option>
    <option value="17">石川</option>
    <option value="18">福井</option>
    <option value="19">山梨</option>
    <option value="20">長野</option>
    <option value="21">岐阜</option>
    <option value="22">静岡</option>
    <option value="23">愛知</option>
    <option value="24">三重</option>
    <option value="25">滋賀</option>
    <option value="26">京都</option>
    <option value="27">大阪</option>
    <option value="28">兵庫</option>
    <option value="29">奈良</option>
    <option value="30">和歌山</option>
    <option value="31">鳥取</option>
    <option value="32">島根</option>
    <option value="33">岡山</option>
    <option value="34">広島</option>
    <option value="35">山口</option>
    <option value="36">徳島</option>
    <option value="37">香川</option>
    <option value="38">愛媛</option>
    <option value="39">高知</option>
    <option value="40">福岡</option>
    <option value="41">佐賀</option>
    <option value="42">長崎</option>
    <option value="43">熊本</option>
    <option value="44">大分</option>
    <option value="45">宮崎</option>
    <option value="46">鹿児島</option>
    <option value="47">沖縄</option>
</select>

EOT;
        return $text;
    }
}
?>
