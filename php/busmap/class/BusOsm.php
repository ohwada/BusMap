<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/*
 * <?xml version="1.0" encoding="UTF-8" ?>
 * <osm version="0.6" generator="ReadKIBAN">
 * <node id="-1" timestamp="2015-11-20T21:32:50Z" lat="35.14591397" lon="139.10569573">
 *      <tag k="name" v="城堀"/>
 *      <tag k="fixme" v="platform/stop_positionを選択して、正しい位置に移動させてください"/>
 *      <tag k="source" v="KSJ2"/>
 *      <tag k="source_ref" v="http://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-P11.html"/>
 *      <tag k="created_by" v="National-Land-Numerical-Information_MLIT_Japan"/>
 *      <tag k="note" v="National-Land Numerical Information (Bus stop) 2012, MLIT Japan"/>
 *      <tag k="note:ja" v="国土数値情報（バス停留所）平成２４年　国土交通省"/>
 *      <tag k="public_transport" v="platform"/>
 *      <tag k="public_transport" v="stop_position"/>
 *      <tag k="highway" v="bus_stop"/>
 *      <tag k="bus" v="yes"/>
 * </node>
 * </osm>
 */

/**
 * BusOsm
 */ 
class BusOsm {

    /**
     * constractor
     */ 
    function BusOsm() {
        // dummy
    }

    /**
     * make
     * @param string $title
     * @param array of row $nodes
     * @return string
     */
    function make( $title, $nodes ) {
        $time = date("Y-m-d\TH:i:s\Z"); 
        $text = $this->makeHeader( $title );
        foreach($nodes as $node) {
            $text .= $this->makePoint($node, $time);
        }
        $text .= $this->makeFooter();
        return $text;
    }

    /**
     * makeHeader
     */
    function makeHeader( $title ) {
$text =<<< EOT
<?xml version="1.0" encoding="UTF-8" ?>
<osm version="0.6" generator="ReadKIBAN">

EOT;
        return $text;
    }

    /**
     * makePoint
     */
    function makePoint( $node, $time ) {
$text =<<< EOT
<node id="-${node["id"]}" timestamp="$time" lat="${node["lat"]}" lon="${node["lon"]}">
    <tag k="name" v="${node["name"]}"/>
    <tag k="fixme" v="platform/stop_positionを選択して、正しい位置に移動させてください"/>
    <tag k="source" v="KSJ2"/>
    <tag k="source_ref" v="http://nlftp.mlit.go.jp/ksj/gml/datalist/KsjTmplt-P11.html"/>
    <tag k="created_by" v="National-Land-Numerical-Information_MLIT_Japan"/>
    <tag k="note" v="National-Land Numerical Information (Bus stop) 2012, MLIT Japan"/>;
    <tag k="note:ja" v="国土数値情報（バス停留所）平成２４年　国土交通省"/>
    <tag k="public_transport" v="platform"/>
    <tag k="public_transport" v="stop_position"/>
    <tag k="highway" v="bus_stop"/>
    <tag k="bus" v="yes"/>
</node>

EOT;
        return $text;
    }

    /**
     * makeFooter
     */
    function makeFooter()  {
$text =<<< EOT
</osm>
EOT;
        return $text;
    }

}
