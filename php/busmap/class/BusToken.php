<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/**
 * BusToken
 */
class BusToken {

    private static $NAME = "_BUSMAP_TOKEN_";
    private static $MAX = 10;

    /**
     * generate
     * @return string 
     */
    public static function generate() {
        self::initialize();
// openssl_random_pseudo_bytes: php 5.3
//        $token = base64_encode( openssl_random_pseudo_bytes(20) );
        $token = sha1( "busmap_". microtime() );
        $_SESSION[self::$NAME] =
            array($token => 1) +
            array_slice( $_SESSION[self::$NAME], 0, self::$MAX - 1, true );
        return $token;
    }

    /**
     * validate
     * @param string $token
     * @return boolean 
     */
    public static function validate($token) {
        self::initialize();
        $token = (string)filter_var($token);
        if (isset($_SESSION[self::$NAME][$token])) {
            unset($_SESSION[self::$NAME][$token]);
            return true;
        }
        return false;
    }

    /**
     * initialize 
     */
    private static function initialize() {
        if (!isset($_SESSION)) {
            throw new BadMethodCallException('セッションが開始されていません');
        }
        if (!isset($_SESSION[self::$NAME]) || !is_array($_SESSION[self::$NAME])) {
            $_SESSION[self::$NAME] = array();
        }
    }

}
?>
