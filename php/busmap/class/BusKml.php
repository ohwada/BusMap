<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/*
 * <?xml version="1.0" encoding="UTF-8"?>
 * <kml xmlns="http://www.opengis.net/kml/2.2">
 *  <Document>
 *    <name><![CDATA[]]></name>
 *    <Folder>
 *      <name>Placemarks</name>
 *      <Placemark>
 *        <name>Simple placemark</name>
 *        <description>Attached to the ground. Intelligently places itself at the height of the
 *          underlying terrain.</description>
 *        <Point>
 *          <coordinates>-122.0822035425683,37.42228990140251,0</coordinates>
 *       </Point>
 *      </Placemark>
 *    </Folder>
 *  </Document>
 * </kml>
 */

/**
 * BusKml
 */
class BusKml {

    /**
     * constractor
     */ 
    function BusKml() {
         // dummy
    }

    /**
     * make
     * @param string $title
     * @param array of row $nodes
     * @return string
     */
    function make( $title, $nodes ) {
        $text = $this->makeHeader( $title );
        foreach($nodes as $node) {
            $text .= $this->makePoint($node);
        }
        $text .= $this->makeFooter();
        return $text;
    }

    /**
     * makeHeader
     */
    function makeHeader( $title ) {
$text =<<< EOT
<?xml version="1.0" encoding="UTF-8"?>
<kml xmlns="http://www.opengis.net/kml/2.2">
<Document>
<name><![CDATA[$title]]></name>
<Folder>
<name>Placemarks</name>

EOT;
        return $text;
    }

    /**
     * makePoint
     */
    function makePoint( $node ) {
$text =<<< EOT
<Placemark>
    <name>${node["name"]}</name>
    <description></description>
    <Point>
        <coordinates>${node["lon"]},${node["lat"]},0</coordinates>
    </Point>
</Placemark>

EOT;
        return $text;
    }

    /**
     * makeFooter
     */
    function makeFooter()  {
$text =<<< EOT
</Folder>
</Document>
</kml>
EOT;
        return $text;
    }

}
?>
