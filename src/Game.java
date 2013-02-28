import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;


public class Game extends StateBasedGame {

	public static final String NAME = "GAME";
	public static final int MENU = 0;
	public static final int PLAY = 1;
	public static final int CLIENT = 2;
	
	public Game(String NAME) {
		super(NAME);
		this.addState(new Menu(MENU));
		this.addState(new Play(PLAY));
		this.addState(new Client(CLIENT));
	}

	@Override
	public void initStatesList(GameContainer gc) throws SlickException {
		
		this.getState(MENU).init(gc, this);
		this.getState(PLAY).init(gc, this);
		this.getState(CLIENT).init(gc, this);
		this.enterState(CLIENT);
	}

	public static void main(String[] args) {
		try {
			AppGameContainer agc = new AppGameContainer(new Game(NAME));
			agc.setDisplayMode(800, 600, false);
		//	agc.setTargetFrameRate(60);
			agc.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
}
