import java.net.Socket;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;


public class Client extends BasicGameState {

	String msg = "Initial message";
	Socket socket;
	MyConnection conn;
	
	public Client(int state) {
		try {
			socket = new Socket("127.0.0.1", 8888);
			conn = new MyConnection(socket);
			msg = "Connected!";
		} catch (Exception e) {
			
		}
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		// TODO Auto-generated method stub
		g.drawString(msg, 20, 20);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		// TODO Auto-generated method stub
		msg = conn.getMessage();
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return 2;
	}
}
