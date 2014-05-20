package library;

import java.util.ArrayList;
import java.util.List;
 
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
 
import android.content.Context;
 
public class UserFunctions {
     
    private JSONParser jsonParser;
     
    // Testing in localhost using wamp 
    // use http://10.0.2.2/ to connect to your localhost ie http://localhost/ 10.120.103.103
    private static String loginURL = "http://192.168.1.73/MusicLan/";
    private static String registerURL = "http://192.168.1.73/MusicLan/";
    private static String songShareURL = "http://192.168.1.73/MusicLan/";
     
    private static String login_tag = "login";
    private static String register_tag = "register";
    private static String song_register_tag = "shareSong";
    private static String  get_song_list_tag = "getSongList";
    private static String  get_peer_list_tag = "searchSong";
    private static String  get_artist_list_tag ="searchArtist";
    // constructor
    public UserFunctions(){
        jsonParser = new JSONParser();
    }
     
    /**
     * function make Login Request
     * @param email
     * @param password
     * */
    public JSONObject loginUser(String email, String password){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", login_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        JSONObject json = jsonParser.getJSONFromUrl(loginURL, params);
        // return json
        // Log.e("JSON", json.toString());
        return json;
    }
     
    /**
     * function make Login Request
     * @param name
     * @param email
     * @param password
     * */
    public JSONObject registerUser(String name, String email, String password, String macAddr, String deviceName){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", register_tag));
        params.add(new BasicNameValuePair("name", name));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("password", password));
        params.add(new BasicNameValuePair("macAddr", macAddr));
        params.add(new BasicNameValuePair("deviceName", deviceName));
         
        // getting JSON Object
        JSONObject json = jsonParser.getJSONFromUrl(registerURL, params);
        // return json
        return json;
    }
     
    /**
     * function share songs list
     * @param email
     * @param songName
     * @param songUrl
     * @param genre
     * @param artist
     * */
    public JSONObject shareSong(String email, String songName, String songUrl,String genre,String artist){
        // Building Parameters
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("tag", song_register_tag));
        params.add(new BasicNameValuePair("email", email));
        params.add(new BasicNameValuePair("songName", songName));
        params.add(new BasicNameValuePair("songUrl", songUrl));
        params.add(new BasicNameValuePair("genre", genre));
        params.add(new BasicNameValuePair("artist", artist));
         
        // getting JSON Object
        JSONObject json = jsonParser.getJSONFromUrl(songShareURL, params);
        // return json
        return json;
    }
    
    /**
     * function get song list
     * @param macAddr
     * */
	public JSONObject getSongsList(String macAddr) {
		 List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("tag", get_song_list_tag));
	        params.add(new BasicNameValuePair("macAddr", macAddr));
	    	         
	        // getting JSON Object
	        JSONObject json = jsonParser.getJSONFromUrl(songShareURL, params);
	        // return json
	        return json;
	}
    
	 /**
     * function get peer list
     * @param songName
     * */
	public JSONObject getPeerList(String songName) {
		 List<NameValuePair> params = new ArrayList<NameValuePair>();
	        params.add(new BasicNameValuePair("tag", get_peer_list_tag));
	        params.add(new BasicNameValuePair("songName", songName));
	    	         
	        // getting JSON Object
	        JSONObject json = jsonParser.getJSONFromUrl(songShareURL, params);
	        // return json
	        return json;
	}
	
	   
		 /**
	     * function get song list
	     * @param artistName
	     * */
		public JSONObject getArtistList(String artistName) {
			 List<NameValuePair> params = new ArrayList<NameValuePair>();
		        params.add(new BasicNameValuePair("tag", get_artist_list_tag));
		        params.add(new BasicNameValuePair("artistName", artistName));
		    	         
		        // getting JSON Object
		        JSONObject json = jsonParser.getJSONFromUrl(songShareURL, params);
		        // return json
		        return json;
		}
	
    /**
     * Function get Login status
     * */
    public boolean isUserLoggedIn(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        int count = db.getRowCount();
        if(count > 0){
            // user logged in
            return true;
        }
        return false;
    }
     
    /**
     * Function to logout user
     * Reset Database
     * */
    public boolean logoutUser(Context context){
        DatabaseHandler db = new DatabaseHandler(context);
        db.resetTables();
        return true;
    }


     
}