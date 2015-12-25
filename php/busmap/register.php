<?php
/**
 * Bus map
 * 2015-12-01 K.OHWADA
 */

/**
 * BusRegister
 */
class BusRegister {
    var $db;
    var $util;

    var $SUBJECT = "BusMap";
    var $FILENAME = "log/log.txt";
    var $HTTP = "http://";

    var $is_show_search = false;
    var $is_show_register = false;
    var $msg = "";
    var $com_id = 0;
    var $com_name = "";
    var $com_home = "";
    var $com_search = "";
    var $content = "";

    /**
     * constractor
     */ 
    function BusRegister() {
        $this->util = new BusUtil();
        $this->db = new BusDb();
        $this->db->connect();
        mb_language("Japanese");
        mb_internal_encoding("UTF-8");
    }

    /**
     * searchCompany
     * @param string $name
     */ 
    function searchCompany( $name ) {
        $this->com_name = $name;
        if ( empty($name) )  {
            $this->is_show_search = true; 
            $this->msg = "<b><font color=\"red\">バス会社の名称を入力してください</font></b>";
        } else {
            $coms = $this->db->searchCompaniesName( $name, 0 );
            $case = 0;
            if ( $coms != null ) {
                $count = count($coms);
                if ( $count == 1 ) {
                    $case = 1;
                } else if ( $count > 1 ) {
                    $case = 2;
                }
            }
            switch ( $case ) {
                case 1:
                    $this->makeShowRegister( $coms[0] );
                    break;
                case 2:
                    $this->msg = "<b>バス会社を選んでください</b>";  
                    $this->content = $this->arrangeComapnyList( $coms );
                    break;
                case 0:
                default:
                    $this->makeShowSearchNotFound();
                    break;
            }
        }
    }

    /**
     * makeCompany
     * @param int $id 
     */ 
    function makeCompany( $id ) {
        $com = $this->db->getCachedCompanyById( $id );
        if ( $com == null ) {
            $this->makeShowSearchNotFound();
        } else {
            $this->makeShowRegister( $com );
        }
    }

    /**
     * registerCompany
     * @param int $id 
     * @param string $url_home
     * @param string $url_search
     * @param string $token
     * @param string $captcha
     */ 
    function registerCompany( $id, $url_home, $url_search, $token, $captcha ) {
        $url_home = ( $url_home == $this->HTTP )? "": $url_home;
        $url_search = ( $url_search == $this->HTTP )? "": $url_search;
        if ( empty($url_home) ) {
            $this->is_show_register = true;
            $this->msg = "<b><font color=\"red\">バス会社のホームページのURLを入力してください</font></b>";
            $com = $this->db->getCachedCompanyById( $id );
            if ( $com != null ) {
                $this->com_name = $com["name"];
            }
        } else {
            if ( empty($token) || !BusToken::validate($token) ) {
                $this->is_show_search = true;
                $this->msg = "<b><font color=\"red\">トークンがおかしい</font></b><br/><b>最初からお願いします</b>";
            } else {
                $image = new Securimage();
                if ( !$image->check($captcha) ) {
                    $this->is_show_register = true;
                    $this->msg = "<b><font color=\"red\">画像認証がおかしいです</font></b>";
                } else {
                    $this->updateCompany( $id, $url_home, $url_search );
                }
            }
        }
    }

    /**
     * makeShowSearchNotFound
     */ 
    function makeShowSearchNotFound() {
        $this->is_show_search = true; 
        $this->msg = "<b><font color=\"red\">バス会社が見つかりません</font></b>"; 
    }

    /**
     * makeShowRegister
     */ 
    function makeShowRegister( $com ) {
        $this->is_show_register = true;
        $this->msg = "<b>バス会社のホームページと時刻表のURLを入力してください</b>";
        $this->com_id = $com["id"];
        $this->com_name = $com["name"];
        $home = $com["url_home"];
        $search = $com["url_search"];
        if ( empty($home) ) {
            $home = $this->HTTP;
        }
        if ( empty($search) ) {
            $search = $this->HTTP;
        }
        $this->com_home = $home;
        $this->com_search = $search;
    }

    /**
     * arrangeComapnyList
     */
    function arrangeComapnyList( $coms ) {
        $content = "";
        foreach ( $coms as $com ) { 
            $a_com = "<a href='register.php?mode=company&id=". $com["id"] ."'>". $com["name"] ."</a>";
            $content .= "<li>". $a_com ."</li>\n";
        }
        return $content;
    }

    /**
     * updateCompany
     */ 
    function updateCompany( $id, $url_home, $url_search ) {
        $this->content = "";
        $this->saveLog( $id, $url_home, $url_search );
        $this->sendMail( $id, $url_home, $url_search );
        $res = $this->db->updateCompany( $id, $url_home, $url_search );
        if ( $res ) {
            $com = $this->db->getCachedCompanyById( $id );
            if ( $com != null ) {
                $com_name = $com["name"];
            }
            $this->msg = "<b><font color=\"blue\">登録しました</font></b><br/>";
            $this->content .= "<a href='company.php?id=". $id ."'>". $com_name ." へ</a><br/>";
        } else {
            $this->msg = "<b><font color=\"red\">登録に失敗しました</font></b>";
        }
        $this->content .= "<a href='register.php'>登録を続ける</a><br/>";
    }

    /**
     * saveLog
     */
    function saveLog( $id, $url_home, $url_search) {
        $data = date("Y-m-d H:i:s") .", ";
        $data .= $id .", ";
        $data .= $url_home .", ";
        $data .= $url_search .", ";
        $data .= $_SERVER["REMOTE_ADDR"] ."\n";
        file_put_contents( $this->FILENAME, $data, FILE_APPEND );
    }

    /**
     * sendMail
     */ 
    function sendMail( $id, $url_home, $url_search ) {
        $msg = date("Y-m-d H:i:s") ."\n";
        $msg .= "id: ". $id ."\n";
        $msg .= "home: ". $url_home ."\n";
        $msg .= "search: ". $url_search ."\n";
        $msg .= "addr: ". $_SERVER["REMOTE_ADDR"] ."\n";
        mb_send_mail(MAIL, $this->SUBJECT, $msg );
    }

}

/**
 * generateToken
 */
function generateToken( $is_show_register ) {
    if( !$is_show_register ) return ""; 
    return BusToken::generate();
}

/**
 * getCaptchaHtml
 */ 
function getCaptchaHtml( $is_show_register ) {
//    if( !$is_show_register ) return ""; 
    $options = array(
        'input_text' => "画像と同じ文字を入力してください:"
    );
    return Securimage::getCaptchaHtml( $options );
}

    /**
     * search
     */
    include_once("config.php");
    include_once("class/BusDb.php");
    include_once("class/BusUtil.php");
    include_once("class/BusToken.php");
    include_once("class/securimage/securimage.php");
    $register = new BusRegister();

    session_start();

    $is_show_search = false;
    $is_show_register = false;
    $msg = "";
    $content = "";
    $com_id = 0;
    $com_name = "";
    $com_home = "";
    $com_search = "";

    $mode = isset($_POST["mode"]) ? $_POST["mode"] : 
        ( isset($_GET["mode"]) ? $_GET["mode"] : "" );

    // search
    if ( $mode == "search" ) {
        $name = isset($_GET["name"])? $_GET["name"]: "";
        $register->searchCompany( $name );
        $is_show_search = $register->is_show_search; 
        $is_show_register = $register->is_show_register;
        $msg = $register->msg;
        $content = $register->content;
        $com_name = $name;
        $com_id = $register->com_id;
        $com_home = $register->com_home;
        $com_search = $register->com_search;

    // company
    } else if ( $mode == "company" ) {
        $id = isset($_GET["id"]) ? $_GET["id"] : 0;
        $register->makeCompany( $id );
        $is_show_search = $register->is_show_search; 
        $is_show_register = $register->is_show_register;
        $msg = $register->msg;
        $com_id = $id;
        $com_name = $register->com_name;
        $com_home = $register->com_home;
        $com_search = $register->com_search;

    // register
    } else if ( $mode == "register" ) {
        $id = isset($_POST["id"]) ? $_POST["id"] : 0;
        $url_home = isset($_POST["url_home"]) ? $_POST["url_home"] : "";
        $url_search = isset($_POST["url_search"]) ? $_POST["url_search"] : "";
        $token = isset($_POST['token']) ? $_POST['token'] : "";
        $captcha_code = isset($_POST['captcha_code']) ? $_POST['captcha_code'] : "";

        $register->registerCompany( $id, $url_home, $url_search, $token, $captcha_code );
        $is_show_search = $register->is_show_search; 
        $is_show_register = $register->is_show_register;
        $msg = $register->msg;
        $content = $register->content;
        $com_id = $id;
        $com_name = $register->com_name;
        $com_home = $url_home;
        $com_search = $url_search;

    // other
    } else {
        $is_show_search = true;
        $msg = "<b>バス会社の名称を入力して検索してください</b>";
    }

?>
<!doctype html>
<html>
<head>
<meta charset="utf-8">
<title>バス停 地図</title>
<style>
#securimage {
    margin: 10px;
}
#copyright {
    font-size: 70%;
}
</style>
</head>
<body>
<table><tr><td>
<a href="index.php">[TOP]</a>
</td><td>
<a href="about.html">[このサイトについて]</a>
</td><td>
</td></tr></table>
<h3>バス停の地図</h3>
<h1>時刻表の登録</h1>
<?php echo $msg; ?><br/>
<?php if(!$is_show_search) echo "<!-- "; ?>
<form>
<input type="hidden" name="mode" value="search"/>
バス会社の名称 <input type="text" name="name" value="<?php echo $com_name; ?>"/>
<input type="submit" value="バス会社を検索する">
</form>
<?php if(!$is_show_search) echo "--> "; ?>
<?php if(!$is_show_register) echo "<!-- "; ?>
<form action="register.php" method="post" >
<input type="hidden" name="mode" value="register"/>
<input type="hidden" name="token" value="<?php echo generateToken( $is_show_register ); ?>" />
<input type="hidden" name="id" value="<?php echo $com_id; ?>"/>
<table>
<tr><td colspan="2"><?php echo $com_name; ?></td></tr>
<tr><td>ホームページのURL</td>
<td><input type="text" size="100" name="url_home" value="<?php echo $com_home; ?>"/></td></tr>
<tr><td>時刻表のURL</td>
<td><input type="text" size="100" name="url_search" value="<?php echo $com_search; ?>"/></td></tr>
<tr><td colspan="2"><div id="securimage">
<?php echo getCaptchaHtml( $is_show_register ); ?>
</div></td></tr>
<tr><td colspan="2"><input type="submit" value="登録する"></td></tr>
</table>
</form>
<?php if(!$is_show_register) echo "--> "; ?>
<?php echo $content; ?><br/>
<h3>登録の手順</h3>
登録は誰でも自由に出来ます<br/>
(1) バス会社を名称から検索する<br/>
(2) 候補のバス会社から１つを選択する<br/>
(3) 選択したバス会社のホームページと時刻表のURLを入力して登録する<br/>
<br/>
<div id="copyright">
<a rel="license" href="http://creativecommons.org/licenses/by-nc/4.0/"><img alt="クリエイティブ・コモンズ・ライセンス" style="border-width:0" src="https://i.creativecommons.org/l/by-nc/4.0/88x31.png" /></a>
    Author: Kenichi Ohwada<br/>
</div>
</body>
</html>
