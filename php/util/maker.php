<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

    /**
     * maker
     * require IPA font
     * http://ipafont.ipa.go.jp/old/ipafont/download.html
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    $db = new BusDb();
    $db->connect();
    $FONT = 'ipag.ttf';

    // normal param 
    $angle = 0;
    $normal_size = 11;
    $circle_diameter = 50;
    $normal_width = $circle_diameter + 2;
    $normal_height = $circle_diameter + 2;
    $normal_center_x = $normal_width / 2;
    $normal_center_y = $normal_height / 2;
    $normal_y = $normal_center_y + $normal_size / 2;

    // small param 
    $small_size = 8;
    $small_width = 4.5 * $small_size;
    $small_height = 2 * $small_size;
    $small_center_x = $small_width / 2;
    $small_center_y = $small_height / 2;
    $small_y = $small_center_y + $normal_size / 2;

    // normal icon 
    $im_normal = imagecreatetruecolor($normal_width, $normal_height);
    $normal_black = imagecolorallocate($im_normal, 0, 0, 0);
    $normal_gray = imagecolorallocate($im_normal, 128, 128, 128);
    $normal_white = imagecolorallocate($im_normal, 255, 255, 255);
    $normal_yellow = imagecolorallocate($im_normal, 255, 255, 0);
    imagefill( $im_normal, 0, 0, $normal_white );
    imagefilledellipse($im_normal, $normal_center_x, $normal_center_y, $circle_diameter, $circle_diameter, $normal_yellow);
    imageellipse($im_normal, $normal_center_x, $normal_center_y, $circle_diameter, $circle_diameter, $normal_gray);

    // small icon 
    $im_small = imagecreatetruecolor($small_width, $small_height);
    $small_pink = imagecolorallocate($im_small, 254, 128, 128);
    $small_red = imagecolorallocate($im_small, 255, 0, 0);
    $small_yellow = imagecolorallocate($im_small, 255, 255, 0);
    imagefill( $im_small, 0, 0, $small_pink );

    // prefs
    $prefs = $db->getPrefs();
    foreach ($prefs as $pref) {
        $text = $pref["name"];
        $id = $pref["id"];
        $offset_x = ( strlen($text) - 1 ) / 4;
        $normal_x = $normal_center_x - $normal_size * $offset_x;
        $small_x = $small_center_x - $small_size * $offset_x;
        $normal_file = "pref_". $id .".png";
        $small_file = "pref_". $id ."_s.png";

        // normal icon 
        $im_normal_tmp = imagecreatetruecolor($normal_width, $normal_height);
        imagecopy( $im_normal_tmp, $im_normal, 0, 0 , 0 , 0, $normal_width, $normal_height );
        imagecolortransparent( $im_normal_tmp, $normal_white );
        imagettftext( $im_normal_tmp, $normal_size, $angle, $normal_x, $normal_y, $normal_black, $FONT, $text );
        imagepng($im_normal_tmp, $normal_file);
        imagedestroy($im_normal_tmp);

        // small icon 
        $im_small_tmp = imagecreatetruecolor($small_width, $small_height);
        imagecopy( $im_small_tmp, $im_small, 0, 0 , 0 , 0, $small_width, $small_height );
        imagecolortransparent( $im_small_tmp, $small_pink );
        imagettftext( $im_small_tmp, $small_size, $angle, $small_x, $small_y, $small_red, $FONT, $text );
        imagepng($im_small_tmp, $small_file);
        imagedestroy($im_small_tmp);
    }

    imagedestroy($im_normal);
    imagedestroy($im_small);
?>
