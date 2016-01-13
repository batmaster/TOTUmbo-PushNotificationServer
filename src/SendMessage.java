import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class SendMessage {

    //config
    static String apiKey = "AIzaSyALcD9lkt-DatzdKa0aO5qgAQ3yDavRD1o"; // Put here your API key
//    static String apiKey = "AIzaSyA3YH3c_hoiO9fVIxPvuvxdCE3Bcqhbr5M"; // Put here your API key IOS
    
    public static void send(String title, String body, String to_token) {
    	
        String notification = "{\"sound\":\"default\",\"badge\":\"2\",\"title\":\"" + title +"\",\"body\":\"" + body + "!\"}"; // put the message you want to send here
        String messageToSend = "{\"registration_ids\":[\"" + to_token + "\"],\"data\":" + notification + "}"; // Construct the message.

        try {

            // URL
            URL url = new URL("https://android.googleapis.com/gcm/send");

            System.out.println(messageToSend);
            // Open connection
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            // Specify POST method
            conn.setRequestMethod("POST");

            //Set the headers
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", "key=" + apiKey);
            conn.setDoOutput(true);

            //Get connection output stream
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

            byte[] data = messageToSend.getBytes("UTF-8");
            wr.write(data);

            //Send the request and close
            wr.flush();
            wr.close();

            //Get the response
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

            //Print result
            System.out.println(response.toString()); //this is a good place to check for errors using the codes in http://androidcommunitydocs.com/reference/com/google/android/gcm/server/Constants.html

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}