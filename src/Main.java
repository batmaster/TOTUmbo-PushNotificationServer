import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
	
    public static final String API_KEY = "AIzaSyALcD9lkt-DatzdKa0aO5qgAQ3yDavRD1o"; // Put here your API key
//	public static final String HOST_DB = "http://203.114.104.242/umbo/getRecord.php";
    public static final String HOST_DB = "http://192.168.20.123:8888/reg.php";
	
	// run checking within 1 minute
	private static final int LOOP_IN_MILLISECOND = 60 * 1000;
	private static final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
	
	private static int last_id_nu = -1;
	private static ArrayList<Integer> upListeningList = new ArrayList<Integer>();
	
	private static final String NOW = dateFormat.format(new Date());
	
	
	public static void main(String[] args) {
		System.out.println("Server started!");
		System.out.println("HOST_DB: " + HOST_DB);
		
		Timer timer = new Timer();
		CheckTask task = new CheckTask();
		timer.schedule(task, 0, LOOP_IN_MILLISECOND);
		
	}
	
//	public static void System.out.println(String str) {
//		File f = new File(NOW + ".txt");
//		if(!f.exists())
//			try {
//				f.createNewFile();
//			} catch (IOException e1) {
//				e1.System.out.printlnStackTrace();
//			}
//		
//		
//		try {
//			System.out.System.out.printlnln(str);
//		    Files.write(Paths.get(NOW + ".txt"), (str + "\n").getBytes(), StandardOpenOption.APPEND);
//		} catch (IOException e) {
//			e.System.out.printlnStackTrace();
//			System.out.System.out.printlnln("Writing file error!!");
//		}
//	}
	
	private static class CheckTask extends TimerTask {
	    public void run() {
			System.out.println(dateFormat.format(new Date()));
			
			// get DOWN
	    	String result = "";
	    	if (last_id_nu == -1)
	    		result = Request.request("SELECT n.*, s.province FROM nodeumbo n, sector s WHERE n.node_sector = s.umbo AND smsdown = 'yes' AND smsup = '' ORDER BY n.id_nu DESC LIMIT 1");
	    	else
	    		result = Request.request(String.format("SELECT n.*, s.province FROM nodeumbo n, sector s WHERE n.node_sector = s.umbo AND smsdown = 'yes' AND smsup = '' AND n.id_nu > %d ORDER BY n.id_nu ASC", last_id_nu));
	    	String parsed = Parser.parse(result);
			System.out.println("  Down:");
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
					
					System.out.println("    " + id_nu + " " + node_name);
					
					String regIds = "";
					String result2 = Request.request(String.format("SELECT p.regId FROM pushdevice p, (SELECT DISTINCT pushdevice_id id FROM province_pref WHERE province = '%s') pl WHERE p.id = pl.id", province));
					String parsed2 = Parser.parse(result2);
					JSONArray js2 = new JSONArray(parsed2);
					if (js2.length() != 0) {
						for (int j = 0; j < js2.length(); j++) {
							JSONObject jo2 = js2.getJSONObject(j);
							String regId = jo2.getString("regId");
							
							regIds += "\"" + regId + "\"";
							if (j != js2.length() - 1) {
								regIds += ", ";
							}
						}
						
						SendMessage.send(SendMessage.TYPE_DOWN, result, regIds);
					}
					else {
						System.out.println("    xxxx");
					}
					
					upListeningList.add(id_nu);
					Collections.sort(upListeningList);
					last_id_nu = id_nu;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
			
			// get UP
			String arg = "";
			for (int i = 0; i < upListeningList.size(); i++) {
				arg += upListeningList.get(i);
				if (i != upListeningList.size() - 1)
					arg += ",";
			}
			if (upListeningList.size() == 0)
				arg += "0";
			
			result = Request.request(String.format("SELECT n.*, s.province FROM nodeumbo n, sector s WHERE n.node_sector = s.umbo AND n.id_nu IN (%s) AND smsdown = 'yes' AND smsup = 'yes' ORDER BY n.id_nu ASC", arg));
			parsed = Parser.parse(result);
			System.out.println("  Up:");
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
					
					System.out.println("    " + id_nu + " " + node_name);
					
					String regIds = "";
					String provinces = Parser.parse(Request.request(String.format("SELECT p.regId FROM pushdevice p, (SELECT DISTINCT pushdevice_id id FROM province_pref WHERE province = '%s') pl WHERE p.id = pl.id", province)));
					String parsed2 = Parser.parse(provinces);
					JSONArray js2 = new JSONArray(parsed2);
					if (js2.length() != 0) {
						for (int j = 0; j < js2.length(); j++) {
							JSONObject jo2 = js2.getJSONObject(j);
							String regId = jo2.getString("regId");
							
							regIds += "\"" + regId + "\"";
							if (j != js2.length() - 1) {
								regIds += ", ";
							}
						}
						
						SendMessage.send(SendMessage.TYPE_DOWN, result, regIds);
					}
					else {
						System.out.println("    xxxx");
					}
					
					for (int k = 0; k < upListeningList.size(); k++) {
						if (((int) upListeningList.get(k)) == id_nu) {
							upListeningList.remove(k);
						}
					}
					last_id_nu = id_nu;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			
			
			// reconsider
			arg = "";
			for (int i = 0; i < upListeningList.size(); i++) {
				arg += upListeningList.get(i);
				if (i != upListeningList.size() - 1)
					arg += ",";
			}
			if (upListeningList.size() == 0)
				arg += "0";
			System.out.println("  Waiting: " + arg);
	    }
	 }
}
