<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

    /**
     * make download data
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    include_once("class/BusData.php");
    include_once("class/BusKml.php");
    include_once("class/BusGpx.php");
    include_once("class/BusOsm.php");
    $data = new BusData();

    $mode = isset($_GET["mode"]) ? $_GET["mode"]: "route";
    $kind = isset($_GET["kind"]) ? $_GET["kind"]: "kml";
    $id = isset($_GET["id"]) ? intval($_GET["id"]): 1;
    $name = $data->makeFilename( $mode, $kind, $id );

    switch ( $mode ) {
        case "prefecture":
            $file = "data/prefecture_". $id .".". $kind;
            $data->downloadFile($name, $file);
            break;
        default:
            $data->connect();
            $text = $data->makeText( $mode, $kind, $id );
            $data->downloadText($name, $text);
            break;
    }
?>
