import java.net.Socket;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;


class MsgThread extends Thread {
	
	Socket socket;
	MyConnection conn;
	String msg;
	
	public MsgThread(Socket socket) {
		this.socket = socket;
		this.conn = new MyConnection(socket);
		this.msg = "connected!";
	}
	
	public void run() {
		while (true) {
			msg = conn.getMessage();
			System.out.println("Message: " + msg);
		}
	}
}

public class Client extends BasicGameState {

	Socket socket;
	MyConnection conn;
	MsgThread thread;
	String msg;
	
	public Client(int state) {
		try {
			socket = new Socket("127.0.0.1", 8888);
			thread = new MsgThread(socket);
			thread.start();
		} catch (Exception e) {
			
		}
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		g.drawString(msg, 20, 20);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		msg = thread.msg;
		System.out.println("Message: " + msg);
	}

	@Override
	public int getID() {
		return 2;
	}
}
