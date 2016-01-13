import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Main {
	
	public static final String HOST_DB = "http://203.114.104.242/umbo/getRecord.php";
	
	// run checking within 1 minute
	private static final int LOOP_IN_MILLISECOND = 60 * 1000;
	private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private static int last_id_nu = -1;
	
	public static void main(String[] args) {
		System.out.println("Server started!");
		System.out.println("HOST_DB: " + HOST_DB);
		
		Timer timer = new Timer();
		CheckDownTast task = new CheckDownTast();
		timer.schedule(task, 0, LOOP_IN_MILLISECOND);
		
	}

	
	private static class CheckDownTast extends TimerTask {
	    public void run() {
	    	String parsed = "";
	    	if (last_id_nu == -1)
	    		parsed = Parser.parse(Request.request("SELECT n.*, s.province FROM nodeumbo n, sector s WHERE n.node_sector = s.umbo AND smsdown = 'yes' AND smsup = '' ORDER BY n.id_nu DESC LIMIT 1"));
	    	else
	    		parsed = Parser.parse(Request.request(String.format("SELECT n.*, s.province FROM nodeumbo n, sector s WHERE n.node_sector = s.umbo AND smsdown = 'yes' AND smsup = '' AND n.id_nu > %d ORDER BY n.id_nu ASC", last_id_nu)));
	    	

			System.out.println(dateFormat.format(new Date()));
			System.out.println(parsed);
			
			try {
				JSONArray js = new JSONArray(parsed);
				for (int i = 0; i < js.length(); i++) {
					JSONObject jo = js.getJSONObject(i);
					
					int id_nu = jo.getInt("id_nu");
					String node_id = jo.getString("node_id");
					String node_ip = jo.getString("node_ip");
					String node_time_down = jo.getString("node_time_down");
					String node_name = jo.getString("node_name");
					String temp = jo.getString("temp");
					String province = jo.getString("province");
					
					last_id_nu = id_nu;
					
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
	    }
	 }
}
