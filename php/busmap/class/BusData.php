<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/**
 * BusData
 */
class BusData {
    var $db;

    /**
     * constractor
     */
    function BusData() {
        $this->db = new BusDb();
    }

    /**
     * connect
     */
    function connect() {
        $this->db->connect();
    }

    /**
     * makeFilename
     * @param string $mode
     * @param string $kind
     * @param int $id
     * @return string
     */
    function makeFilename( $mode, $kind, $id ) {
        $name = $mode . "_". $id . "." .$kind;
        return $name;
    }

    /**
     * makeText
     * @param string $mode
     * @param string $kind
     * @param int $id
     * @return string
     */
    function makeText( $mode, $kind, $id ) {
        $title = $this->makeTitle( $mode, $id );
        $nodes = $this->getNodesData( $mode, $id );
        $text = $this->makeTextFromNode($kind, $title, $nodes);
        return $text;
    }

    /**
     * makeTitle
     */
    function makeTitle( $mode, $id ) {
        switch ( $mode ) {
            case "company":
                $title = $this->makeTitleCompany($id);
                break;
            case "node":
                $title = $this->makeTitleNode($id);
                break;
            case "route":
            default:
                $title = $this->makeTitleRoute($id);
                break;
        }
        return $title;
    }

    /**
     * makeTitleCompany
     */
    function makeTitleCompany( $id ) {
        $company = $this->db->getCompanyById( $id );
        $title = "バス停 : ". $company["name"];
        return $title;
    }

    /**
     * makeTitleRoute
     */
    function makeTitleRoute( $id ) {
        $route = $this->db->getRouteById( $id );
        $title = "バス停 : ". $route["company"] ." - ".  $route["bus_line"];
        return $title;
    }

    /**
     * makeTitleNode
     */
    function makeTitleNode( $id ) {
        $node = $this->db->getNodeById( $id );
        $pref = $this->db->getPrefById( $node["pref_id"] );
        $title = "バス停 : ". $pref["name"] ." - ".  $node["name"];
        return $title;
    }

    /**
     * getNodesData
     */
    function getNodesData( $mode, $id ) {
        switch ( $mode ) {
            case "company":
                $nodes = $this->getNodesCompany($id);
                break;
            case "node":
                $nodes = $this->getNodesNode($id);
                break;
            case "route":
            default:
                $nodes = $this->getNodesRoute($id);
                break;
        }
        return $nodes;
    }

    /**
     * getNodesCompany
     */
    function getNodesCompany( $id ) {
        $route_ids = array();
        $routes = $this->db->getRoutesByCompany( $id );
        foreach ( $routes as $route ) { 
            $route_ids[] = $route["id"];   
        }
        return $this->db->getNodesByRouteIds( $route_ids );
    }

    /**
     * getNodesRoute
     */
    function getNodesRoute($id) {
        return $this->db->getNodesByRouteId( $id );
    }

    /**
     * getNodesNode
     */
    function getNodesNode($id) {
        $node = $this->db->getNodeById( $id ); 
        return array( $node );
    }

    /**
     * makeTextFromNode
     */
    function makeTextFromNode($kind, $title, $nodes) {
        switch ( $kind ) {
            case "gpx":
                $gpx = new BusGpx();
                $text = $gpx->make($title, $nodes);
                break;                
            case "osm":
                $osm = new BusOsm();
                $text = $osm->make($title, $nodes);
                break;
            case "kml":
            default:
                $kml = new BusKml();
                $text = $kml->make($title, $nodes);
                break;
        }
        return $text; 
    }

    /**
     * downloadText
     * @param string $name
     * @param string $text
     */
    function downloadText($name, $text) {
        $this->outputHeader($name);
        echo $text;
    }

    /**
     * downloadFile
     * @param string $name
     * @param string $file
     */
    function downloadFile($name, $file) {
        $this->outputHeader($name);
        readfile($file);
    } 

    /**
     * outputHeader
     */
    function outputHeader($name) {
        header("Content-type: text/plane");
        header("Content-Disposition: attachment; filename=$name");
    } 
  
}
?>
