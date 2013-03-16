import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.gui.TextField;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;


public class Menu extends BasicGameState {

	TextField ipfield, portfield;
	
	public Menu(int state) {
		
	}
	
	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		// TODO Auto-generated method stub
		ipfield = new TextField(gc, gc.getDefaultFont(), 250, 100, 200, 30);
		portfield = new TextField(gc, gc.getDefaultFont(), 250, 200, 200, 30);
		ipfield.setFocus(true);
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		// TODO Auto-generated method stub
		g.drawString("PARTY ALL NIGHT!!!", 350, 50);
		g.drawString("IP address:", 100, 100);
		g.drawString("Port number:", 100, 200);
		g.drawRect(350, 450, 250, 30);
		g.drawString("Click here to go to the game", 350, 450);
		ipfield.render(gc, g);
		portfield.render(gc, g);
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		// TODO Auto-generated method stub
		Input input = gc.getInput();

		if ((input.isMouseButtonDown(Input.MOUSE_LEFT_BUTTON) &&
				input.getMouseX() >= 350 && input.getMouseX() <= 600 &&
				input.getMouseY() >= 450 && input.getMouseY() <= 480) || input.isKeyPressed(Input.KEY_ENTER)) {
			Game game = (Game) sbg;
			game.ip = ipfield.getText();
			game.port = portfield.getText();
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
