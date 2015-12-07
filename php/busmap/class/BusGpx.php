<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/*
 * <?xml version="1.0" encoding="UTF-8" ?>
 * <gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1" creator="osmtracker-android" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd ">
 * <metadata>
 *   <name><![CDATA[]]></name>
 *   <desc><![CDATA[]]></desc>
 * </metadata>
 * <wpt lat="35.14591397" lon="139.10569573">
 *    <time>2015-11-23T21:53:30Z</time>
 *    <name><![CDATA[城堀]]></name>
 * </wpt>
 * </gpx>
 */

/**
 * BusGpx
 */ 
class BusGpx {

    /**
     * constractor
     */ 
    function BusGpx() {
        // dummy
    }

    /**
     * make
     * @param string $title
     * @param array of row $nodes
     * @return string
     */
    function make($title, $nodes) {
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
<gpx xmlns="http://www.topografix.com/GPX/1/1" version="1.1" creator="osmtracker-android" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.topografix.com/GPX/1/1 http://www.topografix.com/GPX/1/1/gpx.xsd ">
<metadata>
    <name><![CDATA[$title]]></name>
    <desc><![CDATA[国土数値情報に基づく]]></desc>
</metadata>

EOT;
        return $text;
    }

    /**
     * makePoint
     */
    function makePoint( $node, $time ) {
$text =<<< EOT
<wpt lat="${node["lat"]}" lon="${node["lon"]}">
    <time>$time</time>
    <name><![CDATA[${node["name"]}]]></name>
</wpt>

EOT;
        return $text;
    }

    /**
     * makeFooter
     */
    function makeFooter()  {
$text =<<< EOT
</gpx>
EOT;
        return $text;
    }

}
