import java.net.Socket;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;


class PlayerThread extends Thread {
	
	Socket socket;
	MyConnection conn;
	String msg, name;
	int active, drunk, id;
	float x, y;

	public PlayerThread(Socket socket) {
		this.socket = socket;
		this.conn = new MyConnection(socket);
		this.msg = "connected!";
		this.active = 0;
	}
	
	public void run() {
		while (true) {
			msg = conn.getMessage();
			System.out.println("Message: " + msg);
			if (msg.substring(0, 4).equals("List")) {
				msg = msg.substring(7);
				for (int i = 0; i < msg.length(); i++) {
					if (msg.charAt(i) == '\0') active++;
				}
			} else if (msg.substring(0, 7).equals("Number")) {
				id = Integer.parseInt(msg.substring(10));
			}
			active = 0;
		}
	}
}

public class Client extends BasicGameState {

	Socket socket;
	MyConnection conn;
	PlayerThread thread;
	String msg;
	Image[] balls = new Image[4];
	PlayerThread[] players = new PlayerThread[4];
	float moveSpeed = 1.00f;
	int id;
	
	public Client(int state) {
		try {
			socket = new Socket("127.0.0.1", 8888);
			thread = new PlayerThread(socket);
			thread.start();
		} catch (Exception e) {
			
		}
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		for (int i = 0; i < 4; i++) balls[i] = new Image("img/ball.png");
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		g.drawString(thread.msg, 20, 20);
		for (int i = 0; i < thread.active; i++) g.drawImage(balls[i], players[i].x, players[i].y);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		Input input = gc.getInput();
		/*
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
		*/
	}

	@Override
	public int getID() {
		return 2;
	}
}
