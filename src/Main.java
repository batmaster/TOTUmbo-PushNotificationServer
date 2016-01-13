import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		
		Scanner s = new Scanner(new BufferedReader(new InputStreamReader(System.in)));
		
		int i = 0;
		while (true) {
			i++;
			String str = s.nextLine();
			
			SendMessage.send(Integer.toString(i), str, "APA91bHsByQVz3Jr444irokmTLLwXPrd6qMlA3CKGwBFcEjks_mlvkDQG1e-UV7iGtqH60wrGlPiEVzZMDZtNViN8rveNIm8gJNLKV9qL5bP-Jpz5yHfsD1ErGRep-wF2WXVlgMmXnrV");
		}
	}

}
