import org.newdawn.slick.AppGameContainer;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.state.StateBasedGame;


public class Game extends StateBasedGame {

	public static final String NAME = "GAME";
	public static final int MENU = 0;
	public static final int PLAY = 1;
	public static final int CLIENT = 2;
	
	public String ip, port;
	
	public Game(String NAME) {
		super(NAME);
		this.ip = "";
		this.port = "";
		this.addState(new Menu(MENU));
		this.addState(new Play(PLAY));
		this.addState(new Client(CLIENT));
		this.enterState(MENU);
	}
	
	@Override
	public void initStatesList(GameContainer gc) throws SlickException {
	}

	public static void main(String[] args) {
		try {
			AppGameContainer agc = new AppGameContainer(new Game(NAME));
			agc.setDisplayMode(800, 600, false);
//			agc.setTargetFrameRate(60);
			agc.setMinimumLogicUpdateInterval(16);
			agc.setMaximumLogicUpdateInterval(31);
			agc.start();
		} catch (SlickException e) {
			e.printStackTrace();
		}
	}
	
}
