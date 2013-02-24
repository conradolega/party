import java.io.*;
import java.net.*;
import java.util.ArrayList;

class ClientThread extends Thread {

	Socket socket;
	String msg, name, status, command, oldname;
	MyConnection conn;
	MyServer server;
	boolean quitted;

	public ClientThread(Socket socket, int number, MyServer server) {
		this.socket = socket;
		this.conn = new MyConnection(socket);
		this.name = "Client" + number;
		this.server = server;
		this.status = "";
		this.quitted = false;
	}
	
	public String getID() {
		if (!this.status.equals("")) return this.name + " - " + this.status;
		else return this.name;
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
				if (this.msg.charAt(0) == '/') {
					this.command = this.msg.substring(1, this.msg.indexOf(' '));
					if (this.command.equals("quit")) {
						quitted = true;
						this.server.quit(this.name);
						this.sendMessage("QUIT", false);
					}
					else if (this.command.equals("whisper")) {
						this.msg = msg.substring(9).trim();
						if (!this.server.whisper(msg, this.name)) this.sendMessage("This user does not exist.", true);
					}
					else if (this.command.equals("changestatus")) {
						this.status = this.msg.substring(14).trim();
						this.server.sendToAll(this.name + " has changed status to \"" + this.status + "\"", true);
						this.server.sendList();
					}
					else if (this.command.equals("changename")) {
						this.oldname = this.name;
						this.name = this.msg.substring(12).trim();
						if (this.name.indexOf(' ') < this.name.length() && this.name.indexOf(' ') > -1) {
							this.name = this.oldname;
							this.sendMessage("Usernames should only consist of one word", true);
						}
						else {
							this.server.sendToAll(this.oldname + " has changed name to " + this.name, true);
							this.server.sendList();
						}
					}
					else {
						this.sendMessage("Invalid command " + this.msg, true);
					}
				}
				else this.server.sendToAll(this.name + ": " + this.msg, false);
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
	int connected = 1;
	public void sendList() {
		list = "List: ";
		for (int i = 0; i < clients.size(); i++) {
			list += "\0" + clients.get(i).getID();
		}
		sendToAll(list, false);	
	}
	
	public void sendToAll(String s, boolean serverMessage) {
		for (int i = 0; i < clients.size(); i++) {
			clients.get(i).sendMessage(s, serverMessage);
		}
	}

	public boolean whisper(String s, String sender) {
		boolean whispered = false;
		name = s.substring(0, s.indexOf(' '));
		msg = s.substring(s.indexOf(' ')).trim();
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).name.equals(name)) {
				whispered = true;
				clients.get(i).sendMessage("[" + sender + " whispers]: " + msg, false);
			}
		}
		return whispered;
	}
	
	public void quit(String name) {
		for (int i = 0; i < clients.size(); i++) {
			if (clients.get(i).name.equals(name)) {
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
			while (true) {
				Socket socket = ssocket.accept();
				System.out.println("Server: " + socket.getInetAddress() + " connected!");
				clients.add(new ClientThread(socket, connected, this));
				sendToAll(clients.get(clients.size() - 1).name + " has connected", true);
				clients.get(clients.size() - 1).start();
				sendList();
				connected++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public static void main(String[] args) {
		MyServer serve = new MyServer();
	}
	

}