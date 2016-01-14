import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendMessage {

	public static final int TYPE_DOWN = 0;
	public static final int TYPE_UP = 1;
	
    /**
     * 
     * @param type 0 down, 1 up
     * @param parsed parsed string
     * @param to_token array of device tokens
     */
    public static void send(int type, String phpresult, String to_token) {
    	
        String notification = "{\"sound\":\"default\",\"badge\":\"2\",\"title\":\"" + type + "\",\"body\":\"" + phpresult + "\"}";
        String messageToSend = "{\"registration_ids\":[" + to_token + "],\"data\":" + notification + "}";

        System.out.println("  >>>>\n" + messageToSend);
        try {
            URL url = new URL("https://android.googleapis.com/gcm/send");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + Main.API_KEY);
            conn.setDoOutput(true);
            
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            byte[] data = messageToSend.getBytes("UTF-8");
            wr.write(data);
            wr.flush();
            wr.close();

            int responseCode = conn.getResponseCode();
            System.out.println("\nSending 'POST' request to URL : " + url);
            System.out.println("Response Code : " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer response = new StringBuffer();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            System.out.println("  <<<<\n" + response.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}