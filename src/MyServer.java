import java.net.*;
import java.util.ArrayList;

class ClientThread extends Thread {

	Socket socket;
	String msg, status, command, oldname;
	MyConnection conn;
	MyServer server;
	boolean quitted;
	int name;

	public ClientThread(Socket socket, int number, MyServer server) {
		this.socket = socket;
		this.conn = new MyConnection(socket);
		this.name = number;
		this.server = server;
		this.status = "";
		this.quitted = false;
	}
	
	public int getID() {
		return this.name;
	}
	
	public void sendMessage(String s, boolean serverMessage) {
		try {
			if (serverMessage) {
				this.conn.sendMessage("Server message: " + s);
			}
			else this.conn.sendMessage(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while (!quitted) {
				this.msg = this.conn.getMessage() + " ";
				//handles force quit
				if(this.msg.equals("null ")){
					socket.close();
					break;
				}
				if (this.msg.charAt(0) == '/') {
					this.command = this.msg.substring(1, this.msg.indexOf(' '));
					if (this.command.equals("quit")) {
						quitted = true;
						this.server.quit(this.name);
						this.sendMessage("QUIT", false);
					}
					else {
						this.sendMessage("Invalid command " + this.msg, true);
					}
				}
				else if (this.msg.substring(0, 4).equals("MOVE")) {
					this.server.sendToAll(this.msg.substring(0, 4) + " " + this.name + this.msg.substring(4), false);
				}
				else this.server.sendToAll(this.msg, false);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

public class MyServer {

	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();
	boolean sent = false;
	String list, name, msg;
	int connected = 0;
	
	public void sendList() {
		sendToAll("Active " + clients.size(), false);
	}
	
	public void sendToAll(String s, boolean serverMessage) {
		for (int i = 0; i < clients.size(); i++) {
			clients.get(i).sendMessage(s, serverMessage);
		}
	}
	
	public void quit(int name) {
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).name == name) {
				clients.remove(i);
				i = clients.size();
			}
		}
		sendToAll(name + " has disconnected", true);
		sendList();
	}
	
	public MyServer() {
		try {
			System.out.println("Server: Starting server...");
			ServerSocket ssocket = new ServerSocket(8888);
			System.out.println("Server: Waiting for connections...");
			//TODO give the new client his ID
			while (true) {
				Socket socket = ssocket.accept();
				System.out.println("Server: " + socket.getInetAddress() + " connected!");
				clients.add(new ClientThread(socket, connected, this));
				sendToAll(clients.get(clients.size() - 1).name + " has connected", true);
				clients.get(clients.size() - 1).start();
				clients.get(clients.size() - 1).sendMessage("Number: " + (clients.size() - 1), false);
				sendList();			
				connected++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args) {
		new MyServer();
	}
}