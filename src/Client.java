import java.net.Socket;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
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
	Image ball;
	float x = 0.00f, y = 0.00f, moveSpeed = 1.00f;
	
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
		ball = new Image("img/ball.png");
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		g.drawString(thread.msg, 20, 20);
		g.drawImage(ball, x, y);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		Input input = gc.getInput();
		
		if (input.isKeyDown(Input.KEY_DOWN)) {
			y += moveSpeed * delta;
		}
		if (input.isKeyDown(Input.KEY_RIGHT)) {
			x += moveSpeed * delta;
		}
		if (input.isKeyDown(Input.KEY_LEFT)) {
			x -= moveSpeed * delta;
		}
		if (input.isKeyDown(Input.KEY_UP)) {
			y -= moveSpeed * delta;
		}
	}

	@Override
	public int getID() {
		return 2;
	}
}
