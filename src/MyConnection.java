package src;
import java.io.*;
import java.net.*;

public class MyConnection {
	Socket s;
	PrintWriter p;
	BufferedReader b;
	String msg;
	char c;
	
	public MyConnection(Socket s) {
		try {
			this.s = s;
			p = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
			b = new BufferedReader(new InputStreamReader(s.getInputStream()));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean sendMessage(String msg) {
		try {
			p.println(msg);
			p.flush();
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	public String getMessage() {
		try {
			return b.readLine();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
}