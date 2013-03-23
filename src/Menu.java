import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.TextField;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;


public class Menu extends BasicGameState {

	TextField ipfield, portfield;
	Image logo;
	Music splash;
	
	public Menu(int state) {
		
	}
	
	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		// TODO Auto-generated method stub
		ipfield = new TextField(gc, gc.getDefaultFont(), 325, 350, 200, 30);
		portfield = new TextField(gc, gc.getDefaultFont(), 325, 400, 200, 30);
		ipfield.setFocus(true);
		ipfield.setText("localhost");
		portfield.setText("8888");
		logo = new Image("img/logo.png");
		splash = new Music("audio/splash.ogg");
		splash.loop();
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		// TODO Auto-generated method stub
		g.drawImage(logo, 0, 0);
		g.drawString("IP:", 275, 350);
		g.drawString("Port:", 275, 400);
		ipfield.render(gc, g);
		portfield.render(gc, g);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		// TODO Auto-generated method stub
		Input input = gc.getInput();

		if (input.isKeyPressed(Input.KEY_ENTER)) {
			Game game = (Game) sbg;
			game.ip = ipfield.getText();
			game.port = portfield.getText();
			splash.stop();
			sbg.enterState(Game.CLIENT);
		}
		if(input.isKeyPressed(Input.KEY_TAB)){
			portfield.setFocus(true);
		}
	}

	@Override
	public int getID() {
		// TODO Auto-generated method stub
		return 0;
	}

}
