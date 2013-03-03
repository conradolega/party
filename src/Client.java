import java.net.Socket;

import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

//Player class to hold image, position, drunk_level;
class Player {
	
	private static final float TERMINAL_VELOCITY = 2.0f;
	float x,y;
	int drunk_level;
	boolean is_jumping;
	Rectangle hitbox;
	Image sprite;
	float speed_x;
	float speed_y;
	float acc_y; //acceleration along y
	
	public Player() throws SlickException{
		//Starting position
		x = 355.0f;
		y = 35.0f;
		
		speed_x = 0;
		speed_y = 0;
		acc_y = 0;
		drunk_level = 0;
		is_jumping = false;
		sprite = new Image("img/ball.png");
		hitbox = new Rectangle(x,y,sprite.getWidth(),sprite.getHeight());
	}
	
	public float getX(){
		return x;
	}
	
	public float getY(){
		return y;
	}
	
	public void setX(float newx){
		x = newx;
		hitbox.setX(newx);
	}
	
	public void setY(float newy){
		y = newy;
		hitbox.setY(newy);
	}
	
	public Image getImage(){
		return sprite;
	}
	
	public Rectangle getHitbox(){
		return hitbox;
	}
	
	public float getSpeedX(){
		return speed_x;
	}
	
	public float getSpeedY(){
		return speed_y;
	}
	
	public void setSpeedX(float newspeed_x){
		speed_x = newspeed_x;
	}
	
	public void setSpeedY(float newspeed_y){
		//set maximum downward movement
		if(newspeed_y > TERMINAL_VELOCITY) newspeed_y = TERMINAL_VELOCITY;
		speed_y = newspeed_y;
	}
	
	public boolean isJumping(){
		return is_jumping;
	}
	
	public void setJumping(boolean newjumping){
		is_jumping = newjumping;
	}
	
	public float getAccelerationY(){
		return acc_y;
	}
	
	public void setAccelerationY(float newAccY){
		acc_y = newAccY;
	}
}

class PlayerThread extends Thread {
	Socket socket;
	MyConnection conn;
	String msg;
	int active, id; // active = number of connected players
	Player[] players; //reference to the players in the Client class
	
	public PlayerThread(Socket socket, Player[] players) {
		this.socket = socket;
		this.conn = new MyConnection(socket);
		this.msg = "connected!";
		this.active = 0;
		this.players = players;
	}
	
	//TODO manage protocols here 
	public void run() {
		while (true) {
			msg = conn.getMessage();
			System.out.println("Message: " + msg);

			if (msg.substring(0, 7).equals("Active ")) {
				active = Integer.parseInt(msg.substring(7));
			} 
			else if (msg.substring(0, 7).equals("Number:")) {
				id = Integer.parseInt(msg.substring(8));
			}
			//PROTOCOL: MOVE <PLAYERID> <X> <Y>
			//example: MOVE 2 356 45
			else if(msg.substring(0,4).equals("MOVE")){
				System.out.println("Moveing " + msg.charAt(5));
				int player_id = msg.charAt(5) - 48;
				float x,y;
				String temp_x = "", temp_msg = msg.substring(7);
				//PARSE THE <X> AND <Y>
				temp_x = temp_msg.substring(0, temp_msg.indexOf(' '));
				x = Float.parseFloat(temp_x);
				y = Float.parseFloat(temp_msg.substring(temp_msg.indexOf(' ')));
				players[player_id].setX(x);
				players[player_id].setY(y);
			}
		}
	}
}

public class Client extends BasicGameState {

	//GRAVITY
	private static final float g = 0.02f;
	
	Socket socket;
	PlayerThread thread;
	Player[] players = new Player[4];
	Rectangle[] platforms = new Rectangle[3];
	float moveSpeed = 1.0f;
	int id, active;
	
	public Client(int state) {
		try {
			socket = new Socket("127.0.0.1", 8888);
		} catch (Exception e) {}
	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {;
		for (int i = 0; i < 4; i++) players[i] = new Player();
		platforms[0] = new Rectangle(200,100,300,40);
		platforms[1] = new Rectangle(200,300,300,40);
		platforms[2] = new Rectangle(200,500,300,40);
		thread = new PlayerThread(socket, players);
		thread.start();
		active = 1;
	}

	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		g.drawString(" " + thread.msg, 20, 20);
		g.drawString("ID: " + id, 700, 20);
		
		//render all the images of the players
		//System.out.println("Thread.active: " + thread.active);
		for (int i = 0; i < active; i++){
			g.drawImage(players[i].getImage(), players[i].getX(), players[i].getY());
			g.draw(players[i].getHitbox());
		}
		for (int i = 0; i < 3; i++){
			g.fill(platforms[i]);
			g.draw(platforms[i]);
		}
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		active = thread.active;
		id = thread.id;
		float past_x = players[id].getX();
		float past_y = players[id].getY();
		players[id].setSpeedX(0);
		players[id].setSpeedY(0);
//		System.out.println("PLAYER IS JUMPING: "+players[id].isJumping());
//		System.out.println("PLAYER ACCELERATION: " + players[id].getAccelerationY());
		
		Input input = gc.getInput();
//		if (input.isKeyDown(Input.KEY_DOWN)) {
//			players[id].setSpeedY(1);
//		}
		if (input.isKeyDown(Input.KEY_RIGHT)) {
			players[id].setSpeedX(1);
		}
		if (input.isKeyDown(Input.KEY_LEFT)) {
			players[id].setSpeedX(-1);
		}
		if (input.isKeyDown(Input.KEY_UP) && !players[id].isJumping()) {
			players[id].setAccelerationY(-0.6f);
			players[id].setJumping(true);
		}

		//increment downward acceleration by g
		if(players[id].isJumping()) players[id].setAccelerationY(players[id].getAccelerationY() + g*(float)(delta)/10);
		
		//set new speed using the acceleration along y
		players[id].setSpeedY(players[id].getSpeedY() + players[id].getAccelerationY() * (float)(delta)/10);
		
		//set new x and y using the speed along x and y
		players[id].setX(players[id].getX() + players[id].getSpeedX() * (float)(delta));
		players[id].setY(players[id].getY() + players[id].getSpeedY() * (float)(delta));
		
		//set jumping to true by default then set to false only when landing on ground
		players[id].setJumping(true);
		
		//check for player-to-platform collisions
		for(int i=0; i<3; i++){
			if(platforms[i].intersects(players[id].getHitbox())){
				boolean flag = false; //fix corner collision
				
				if (past_y<platforms[i].getY()) {
					players[id].setY(platforms[i].getY()-1-players[id].getHitbox().getHeight());
					flag = true;
					
					//set jumping to false, speed to 0, acceleration along y to 0 when touching the ground
					players[id].setJumping(false);
					players[id].setSpeedY(0);
					players[id].setAccelerationY(0);
				}
				if (past_x<platforms[i].getX() && !flag) {
					players[id].setX(platforms[i].getX()-1-players[id].getHitbox().getWidth());
				}
				if (past_x>(platforms[i].getX()+platforms[i].getWidth())  && !flag) {
					players[id].setX(platforms[i].getX()+platforms[i].getWidth()+1);
				}
				if (past_y>(platforms[i].getY()+platforms[i].getHeight())) {
					players[id].setY(platforms[i].getY()+platforms[i].getHeight()+1);
				}
			}
		}
		
		
		// check for player-to-player collisions
		for (int i = 0; i < active; i++) {
			if (i != id) {
				if (players[i].getHitbox().intersects(players[id].getHitbox())) {
				//	boolean flag = false; //fix corner collision
					
					if (past_y < players[i].getY()) {
						players[id].setY(players[i].getY() - 1 - players[id].getHitbox().getHeight());
						
						//set jumping to false, speed to 0, acceleration along y to 0 when touching a player below
						players[id].setJumping(false);
						players[id].setSpeedY(0);
						players[id].setAccelerationY(0);
					}
					if (past_x < players[i].getX()) {
						players[id].setX(players[i].getX() - 1 - players[id].getHitbox().getWidth());
					}
					if (past_x > (players[i].getX() + players[i].getHitbox().getWidth())) {
						players[id].setX(players[i].getX() + players[i].getHitbox().getWidth() + 1);
					}
					if (past_y > (players[i].getY() + players[i].getHitbox().getHeight())) {
						players[id].setY(players[i].getY() + players[i].getHitbox().getHeight() + 1);
					}
				}
			}
		}
		
		//check for changes in coordinates then broadcast the MOVE message
		if(past_x!=players[id].getX() || past_y!=players[id].getY()){
			thread.conn.sendMessage("MOVE "+players[id].getX()+" "+players[id].getY());
		}
	}

	@Override
	public int getID() {
		return 2;
	}
}
