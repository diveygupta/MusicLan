<?php
/**
 * File to handle all API requests
 * Accepts GET and POST
 * 
 * Each request will be identified by TAG
 * Response will be JSON data
 * check for POST request 
 */

//$response["success"] = "divey";
//print(json_encode($response));

if (isset($_POST['tag']) && $_POST['tag'] != '') {
    // get tag
    $tag = $_POST['tag'];
 
    // include db handler
    require_once 'DB_Functions.php';
    $db = new DB_Functions();
 
    // response Array
    $response = array("tag" => $tag, "success" => 0, "error" => 0, "list" => 0);
 
    // check for tag type
    if ($tag == 'login') {
        // Request type is check Login
        $email = $_POST['email'];
        $password = $_POST['password'];
 
        // check for user
        $user = $db->getUserByEmailAndPassword($email, $password);
        if ($user != false) {
            // user found
            // echo json with success = 1
            $response["success"] = 1;
            $response["uid"] = $user["unique_id"];
            $response["user"]["name"] = $user["name"];
            $response["user"]["email"] = $user["email"];
            $response["user"]["created_at"] = $user["created_at"];
            $response["user"]["updated_at"] = $user["updated_at"];
            echo json_encode($response);
        } else {
            // user not found
            // echo json with error = 1
            $response["error"] = 1;
            $response["error_msg"] = "Incorrect email or password!";
            echo json_encode($response);
        }
    } else if ($tag == 'register') {
        // Request type is Register new user
        $name = $_POST['name'];
        $email = $_POST['email'];
        $password = $_POST['password'];
        $macAddr = $_POST['macAddr'];
        // check if user is already existed
        if ($db->isUserExisted($email)) {
            // user is already existed - error response
            $response["error"] = 2;
            $response["error_msg"] = "User already existed";
            echo json_encode($response);
        } else {
            // store user
            $user = $db->storeUser($name, $email, $password,$macAddr);
            if ($user) {
                // user stored successfully
                $response["success"] = 1;
                $response["uid"] = $user["unique_id"];
                $response["user"]["name"] = $user["name"];
                $response["user"]["email"] = $user["email"];
                $response["user"]["created_at"] = $user["created_at"];
                $response["user"]["updated_at"] = $user["updated_at"];
                echo json_encode($response);
            } else {
                // user failed to store
                $response["error"] = 1;
                $response["error_msg"] = "Error occured in Registartion";
                echo json_encode($response);
            }
        }
    } else if ($tag == 'shareSong'){
        $email = $_POST['email'];
        $songName = $_POST['songName'];
        $songUrl = $_POST['songUrl'];
        $genre = $_POST['genre'];
        $artist = $_POST['artist'];

        $user = $db->storeSong($email, $songName, $songUrl,$genre,$artist);
          if ($user) {
                // song stored successfully
                $response["success"] = 1;
               // $response["uid"] = $user["unique_id"];
               // $response["user"]["name"] = $user["name"];
              //  $response["user"]["email"] = $user["email"];
             
                echo json_encode($response);
            } else {
                // song failed to store
                $response["error"] = 1;
                $response["error_msg"] = "Error occured in saving song name";
                echo json_encode($response);
            }
    }
    else if ($tag == 'getSongList'){
        $macAddr = $_POST['macAddr'];

         $result = mysql_query("SELECT email FROM users WHERE macid = '$macAddr'") or die(mysql_error());
         $no_of_rows = mysql_num_rows($result); 
         if ($no_of_rows>0) {        
        }else{
            $response["success"] = 0;
            $response["error"] = 1;
            echo json_encode($response);
        }


         $list=array();

        $user = $db->getSongList($macAddr);
          if ($user) {
                // song stored successfully
                $response["success"] = 1;
               while($rows=mysql_fetch_assoc($user)){
                $list[] = $rows;
               }
               $response["list"] = $list;
              // $list = array("list" => $list );
              // $response = array($response, $list);
             
                echo json_encode($response);
            } else {
                // song failed to store
                $response["success"] = 0;
                $response["error"] = 1;
                $response["error_msg"] = "Error occured in getting song list: User doesn't exist on server";
                echo json_encode($response);
            }
    }
    else {
        echo "Invalid Request";
    }
} else {
    echo "Access Denied!!!";
}


?>