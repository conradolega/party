import java.net.Socket;
import java.util.Random;

import org.newdawn.slick.Animation;
import org.newdawn.slick.Color;
import org.newdawn.slick.GameContainer;
import org.newdawn.slick.Graphics;
import org.newdawn.slick.Image;
import org.newdawn.slick.Input;
import org.newdawn.slick.Music;
import org.newdawn.slick.SlickException;
import org.newdawn.slick.Sound;
import org.newdawn.slick.SpriteSheet;
import org.newdawn.slick.geom.Polygon;
import org.newdawn.slick.geom.Rectangle;
import org.newdawn.slick.opengl.shader.ShaderProgram;
import org.newdawn.slick.state.BasicGameState;
import org.newdawn.slick.state.StateBasedGame;

//Player class to hold image, position, drunk_level;
class Player {
	
	private static final float TERMINAL_VELOCITY = 5000.0f;
	float x,y;
	float speed_x;
	float speed_y;
	float acc_y; //acceleration along y
	int direction; //1: right, -1: left
	boolean is_jumping, is_moving, is_pushing;
	Rectangle hitbox, r_pushbox, l_pushbox;
	Polygon arrow;
	Image player_spritesheets[];
	Animation right_run, left_run, right_jump, left_jump, right_push, left_push, right_idle, left_idle;
	
	public Player(Image player_spritesheets[], float start_x, float start_y) throws SlickException{
		//Starting position
		x = start_x;
		y = start_y;
		
		speed_x = 0;
		speed_y = 0;
		acc_y = 0;
		
		direction = 0;
		is_jumping = false;
		is_moving = false;
		is_pushing = false;
		
		this.player_spritesheets = player_spritesheets;
		right_run = new Animation(new SpriteSheet(this.player_spritesheets[0], 64, 64), 100);
		left_run = new Animation(new SpriteSheet(this.player_spritesheets[0].getFlippedCopy(true, false), 64, 64), 100);

		right_jump = new Animation(new SpriteSheet(this.player_spritesheets[1], 64, 64), 50);
		left_jump = new Animation(new SpriteSheet(this.player_spritesheets[1].getFlippedCopy(true, false), 64, 64), 100);

		right_push = new Animation(new SpriteSheet(this.player_spritesheets[2], 64, 64), 100);
		right_push.setLooping(false);
		left_push = new Animation(new SpriteSheet(this.player_spritesheets[2].getFlippedCopy(true, false), 64, 64), 100);
		left_push.setLooping(false);

		right_idle = new Animation(new SpriteSheet(this.player_spritesheets[3], 64, 64), 100);
		left_idle = new Animation(new SpriteSheet(this.player_spritesheets[3].getFlippedCopy(true, false), 64, 64), 100);
		
		hitbox = new Rectangle(x,y,55,60); //hard coded hitbox size
		arrow = new Polygon();
		
		arrow.addPoint(0,0);
		arrow.addPoint(hitbox.getWidth(), 0);
		arrow.addPoint(hitbox.getWidth()/2,10);
		
		l_pushbox = new Rectangle(x-hitbox.getWidth()*0.5f, y+hitbox.getHeight()*0.25f, hitbox.getWidth()*0.5f, hitbox.getHeight()*0.5f);
		r_pushbox = new Rectangle(x+hitbox.getWidth(), y+hitbox.getHeight()*0.25f, hitbox.getWidth()*0.5f, hitbox.getHeight()*0.5f); 
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
		arrow.setX(newx);
		l_pushbox.setX(x-hitbox.getWidth()*0.5f);
		r_pushbox.setX(x+hitbox.getWidth());
	}
	
	public void setY(float newy){
		//limit maximum y
		if(newy>1000) newy = 1000;
		
		y = newy;
		hitbox.setY(newy);
		arrow.setY(newy- 20);
		r_pushbox.setY(y+hitbox.getHeight()*0.25f);
		l_pushbox.setY(y+hitbox.getHeight()*0.25f);
	}
	
	public Animation getAnimation(){
		if(direction==-1){
			if(is_pushing) return left_push;
			else if(is_jumping) return left_jump;
			else if(is_moving) return left_run;
			else return left_idle;
		}
		else{
			if(is_pushing) return right_push;
			else if(is_jumping) return right_jump;
			else if(is_moving) return right_run;
			else return right_idle;
		}
	}
	
	public Rectangle getHitbox(){
		return hitbox;
	}
	
	public Rectangle getRightPushBox(){
		return r_pushbox;
	}
	
	public Rectangle getLeftPushBox(){
		return l_pushbox;
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
	
	public void setDirection(int NEW_DIRECTIONS){
		direction = NEW_DIRECTIONS;
	}
	
	public int getDirection(){
		return direction;
	}
	
	public Polygon getArrow(){
		return arrow;
	}
}

class PlayerThread extends Thread {
	Socket socket;
	MyConnection conn;
	String msg;
	int active, id, drunk_level; // active = number of connected players
	int dead_players = 0; 
	Player[] players; //reference to the players in the Client class
	boolean started, ready, cheer;
	
	public PlayerThread(Socket socket, Player[] players) {
		this.socket = socket;
		this.conn = new MyConnection(socket);
		this.msg = "connected!";
		this.active = 0;
		this.drunk_level = 0;
		this.players = players;
		this.started = false;
		this.ready = false;
		this.cheer = false;
	}
	
	//TODO manage protocols here 
	public void run() {
		while (true) {
			msg = conn.getMessage();
			System.out.println(msg);

			if (msg.equals("READY")) {
				ready = true;
			}
			else if (msg.equals("START ")) {
				started = true;
			}
			//PROTOCOL: DEAD <playerid>
			//Example: DEAD 1
			else if(msg.substring(0, 4).equals("DEAD")){
				System.out.println("DEAD");
				int dead_id = msg.charAt(5) - 48;
				
				if(dead_id != id){
					dead_players++;
					
					if(dead_players == active-1){
						System.out.println("YOU ARE THE WINNER");
					}
				}
			}
			else if (msg.substring(0, 7).equals("Number:")) {
				id = Integer.parseInt(msg.substring(8));
			}
			else if (msg.substring(0, 7).equals("Active ")) {
				active = Integer.parseInt(msg.substring(7));
			}
			//PROTOCOL: MOVE <PLAYERID> <X> <Y>
			//example: MOVE 2 356 45
			else if(msg.substring(0,4).equals("MOVE")){
				int player_id = msg.charAt(5) - 48;
				float x,y;
				if(player_id != id){
					String temp_x = "", temp_msg = msg.substring(7);
					
					//PARSE THE <X> AND <Y>
					temp_x = temp_msg.substring(0, temp_msg.indexOf(' '));
					x = Float.parseFloat(temp_x);
					y = Float.parseFloat(temp_msg.substring(temp_msg.indexOf(' ')));
					if(players[player_id].getX() > x){
						players[player_id].setDirection(-1);
					}
					else if(players[player_id].getX() < x){
						players[player_id].setDirection(1);
					}
					players[player_id].setX(x);
					players[player_id].setY(y);
				}
			}
			//PROTOCOL: PUSH <PLAYERID> <PLAYER_DIRECTION>
			//example: PUSH 1 -1
			else if(msg.substring(0,4).equals("PUSH")){
				System.out.println("PUSHING " + msg.charAt(5));
				String temp_msg = msg.substring(7);
				int pusher_id = msg.charAt(5) - 48;
				int direction = Integer.parseInt(temp_msg.substring(0, temp_msg.indexOf(' ')));
				
				int i = id;
				if(pusher_id != i){
					System.out.println("pusher_id: " + pusher_id + " direction: " + direction);
					if((direction == -1 && players[i].getHitbox().intersects(players[pusher_id].getLeftPushBox())) || (direction == 1 && players[i].getHitbox().intersects(players[pusher_id].getRightPushBox()))){
						float new_speed = direction * (1000f + drunk_level*100);
						System.out.println("new_speed: " + new_speed + " i: " + i);
						players[i].setSpeedX(new_speed);
					}
				}
			}
			//PROTOCOL: ALCOHOL
			else if (msg.equals("ALCOHOL") && started) {
				drunk_level += 1;
				cheer = true;
			}
		}
	}
}

public class Client extends BasicGameState {

	private static final float g = 300f; //GRAVITY
	private static final int NUM_OF_PLATFORMS = 9;
	
	Socket socket;
	PlayerThread thread;
	Player[] players = new Player[4];
	Rectangle[] platforms = new Rectangle[NUM_OF_PLATFORMS];
	int id, active, t;
	Random random;
	float randX, randY, sway, swayY;
	float push_timer = 0;
	ShaderProgram hShader, vShader;
	Image hImage, vImage, bg, lose, win;
	Image[][] player_spritesheets = new Image[4][4];
	Graphics hGraphics, vGraphics;
	boolean ready, started, dead;
	Sound cheer, jump, scream, push;
	Music music;
	
	public Client(int state) {

	}

	@Override
	public void init(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		bg = new Image("img/bg.jpg");
		lose = new Image("img/lose.png");
		win = new Image("img/win.png");
		
		//SpriteSheet source:
		//http://10firstgames.wordpress.com/2012/02/25/hd-sprite-sheet/
		for (int i = 0; i < 4; i++) {
			player_spritesheets[i][0] = new Image("img/sprite_running" + i + ".png");
			player_spritesheets[i][1] = new Image("img/sprite_jumping" + i + ".png");
			player_spritesheets[i][2] = new Image("img/sprite_pushing" + i + ".png");
			player_spritesheets[i][3] = new Image("img/sprite_idle" + i + ".png");
		}
		

		cheer = new Sound("audio/cheer.ogg");
		jump = new Sound("audio/jump.wav");
		scream = new Sound("audio/scream.ogg");
		push = new Sound("audio/push.wav");
		
		music = new Music("audio/music.ogg");
		//SET STARTING LOCATIONS
		//160 120 - P1
		//555 120 - P2
		//160 440 - P3
		//555 440 - P4
		for (int i = 0; i < 4; i++){
			float start_y;
			float start_x;
			if(i==0 || i==1){
				start_y = 119;
			}
			else{
				start_y = 439;
			}
			if(i==0 || i==2){
				start_x = 160;
			}
			else{
				start_x = 555;
			}
			players[i] = new Player(player_spritesheets[i], start_x ,start_y);
		}
		platforms[0] = new Rectangle(300,100,200,10);
		platforms[1] = new Rectangle(100,180,200,10);
		platforms[2] = new Rectangle(500,180,200,10);
		platforms[3] = new Rectangle(300,260,200,10);
		platforms[4] = new Rectangle(100,340,200,10);
		platforms[5] = new Rectangle(500,340,200,10);
		platforms[6] = new Rectangle(300,420,200,10);
		platforms[7] = new Rectangle(100,500,200,10);
		platforms[8] = new Rectangle(500,500,200,10);
		
		started = false;
		ready = false;
		
		hImage = Image.createOffscreenImage(800, 600);
		hGraphics = hImage.getGraphics();
		vImage = Image.createOffscreenImage(800, 600);
		vGraphics = vImage.getGraphics();
		
		// Shaders as well as their application are from
		// the Slick2d tutorials
		
		String h = "shaders/hvs.frag";
		String v = "shaders/vvs.frag";
		String vert = "shaders/hvs.vert";
		
		hShader = ShaderProgram.loadProgram(vert, h);
		vShader = ShaderProgram.loadProgram(vert, v);
		
		hShader.bind();
		hShader.setUniform1i("tex0", 0); //texture 0
		hShader.setUniform1f("resolution", 800); //width of img
		hShader.setUniform1f("radius", 0f);
		
		vShader.bind();
		vShader.setUniform1i("tex0", 0); //texture 0
		vShader.setUniform1f("resolution", 600); //height of img
		vShader.setUniform1f("radius", 0f);
		
		ShaderProgram.unbindAll();
	}

	@Override
	public void enter(GameContainer gc, StateBasedGame sbg)
			throws SlickException {
		super.enter(gc, sbg);
		try {
			Game game = (Game) sbg;
			socket = new Socket(game.ip, Integer.parseInt(game.port));
		} catch (Exception e) {}
		thread = new PlayerThread(socket, players);
		thread.start();
		active = 1;
		t = 0;
		random = new Random();
		randX = 0.0f;
		randY = 0.0f;
		dead = false;
	}
	
	// While not drunk, do only this; otherwise do this and apply shader
	public void prerender(GameContainer gc, StateBasedGame sbg, Graphics g)
		throws SlickException {
		g.drawImage(bg, 0, 0);
		for (int i = 0; i < active; i++){
			g.drawAnimation(players[i].getAnimation(), players[i].getX(), players[i].getY()); //hard coded
//			g.draw(players[id].getHitbox());
//			g.draw(players[id].getLeftPushBox());
//			g.draw(players[id].getRightPushBox());
		}
		for (int i = 0; i < NUM_OF_PLATFORMS; i++){
			g.fill(platforms[i]);
			g.draw(platforms[i]);
		}
	}
	
	@Override
	public void render(GameContainer gc, StateBasedGame sbg, Graphics g)
			throws SlickException {
		if (ready && started) {
			g.translate(sway, swayY);
			
			if (thread.drunk_level > 0) {
				Graphics.setCurrent(hGraphics);
				hGraphics.clear();
				
				hGraphics.flush();
				
				prerender(gc, sbg, hGraphics);
				
				hShader.bind();
				hShader.setUniform1f("radius", 0.2f * thread.drunk_level);
				
				Graphics.setCurrent(vGraphics);
				vGraphics.clear();
				vGraphics.drawImage(hImage, 0f, 0f);
				
				vGraphics.flush();
				hShader.unbind();
				
				vShader.bind();
				vShader.setUniform1f("radius", 0.2f * thread.drunk_level);
				
				Graphics.setCurrent(g);
				g.drawImage(vImage, 0f, 0f);
				
				ShaderProgram.unbindAll();
			}
			else {
				prerender(gc, sbg, g);	
			}
			
			// DRAW ORANGE TRIANGLE
			g.setColor(Color.orange); //GO ENGG
			g.fill(players[id].getArrow());
			g.setColor(Color.white);
			
			g.translate(-sway, swayY);
			
			//PRINT WIN
			if(thread.dead_players == active-1 && !dead){
				g.drawImage(win, 0, 0);
			}
			
			if (dead && thread.dead_players != active - 1) {
				g.drawImage(lose, 0, 0);
			}
		}
		else if (!started && ready) {
			g.drawString("Press ENTER to start", 100, 100);
		}
		else if (!started && !ready) {
			g.drawString("Please wait for other players", 100, 100);
		}
	}

	@Override
	public void update(GameContainer gc, StateBasedGame sbg, int delta)
			throws SlickException {
		float past_x = players[id].getX();
		float past_y = players[id].getY();
		
		//limit delta
		if(delta>20) delta = 20;
		
		float seconds = (float)delta/1000;
		
		active = thread.active;
		id = thread.id;
		
		started = thread.started;
		ready = thread.ready;
		
		if (started && ready && !dead) {
			if (thread.drunk_level >= 5) {
				randX = (thread.drunk_level - 4) * (random.nextInt(2) + 1);
				randY = (thread.drunk_level - 4)* (random.nextInt(5) + 1);
			}
			
			if (thread.cheer) {
				thread.cheer = false;
				cheer.play();
			}
			
			if (!music.playing()) music.loop();
			
			Input input = gc.getInput();
			if (input.isKeyDown(Input.KEY_RIGHT)) {
				if(players[id].getSpeedX() + 400f + randX <= 600f){
					players[id].setSpeedX(players[id].getSpeedX() + 400f + randX);
				}
				players[id].setDirection(1);
				players[id].is_moving = true;
			}
				
			if (input.isKeyDown(Input.KEY_LEFT)) {
				if(players[id].getSpeedX() - 400f + randX >= -600f){
					players[id].setSpeedX(players[id].getSpeedX() - 400f + randX);
				}
				players[id].setDirection(-1);
				players[id].is_moving = true;
			}
			
			if(!input.isKeyDown(Input.KEY_LEFT) && !input.isKeyDown(Input.KEY_RIGHT)){
				players[id].is_moving = false;
			}
			
			if (input.isKeyDown(Input.KEY_UP) && !players[id].isJumping()) {
				players[id].setSpeedY(-600f + randY);
				players[id].setJumping(true);
				jump.play();
			}
			if (input.isKeyPressed(Input.KEY_SPACE)){
				thread.conn.sendMessage("PUSH "+players[id].getDirection());
				push.play();
				players[id].is_pushing = true;
			}
			else{
				if(players[id].is_pushing == true){
					if(push_timer > 100){
						players[id].is_pushing = false;
						push_timer = 0;
					}
					else{
						push_timer += delta;
					}
				}
			}
	
			//increment downward acceleration by g if player is on air
			if(players[id].isJumping()) players[id].setAccelerationY(players[id].getAccelerationY() + g * seconds);
			//set new speed using the acceleration along y
			players[id].setSpeedY(players[id].getSpeedY() + players[id].getAccelerationY());
	
			//set new speed along X because of friction
			if(players[id].getSpeedX() != 0){
				if(players[id].getSpeedX()>0){
					players[id].setSpeedX(players[id].getSpeedX() - 100f - randX);
				}
				else{
					players[id].setSpeedX(players[id].getSpeedX() + 100f + randX);
				}
			}
			
			//set new x and y using the speed along x and y
			players[id].setX(players[id].getX() + players[id].getSpeedX() * seconds);
			players[id].setY(players[id].getY() + players[id].getSpeedY() * seconds);
			
			//set jumping to true by default then set to false only when landing on ground
			players[id].setJumping(true);
			
			// check for player-to-player collisions
			for (int i = 0; i < active; i++) {
				if (i != id) {
					if (players[i].getHitbox().intersects(players[id].getHitbox())) {
						boolean flag = true;
	
						if (past_x > (players[i].getX() + players[i].getHitbox().getWidth()) - 1) {
							players[id].setX(players[i].getX() + players[i].getHitbox().getWidth());
							flag = false;	
						}
						if (past_x < players[i].getX()) {
							players[id].setX(players[i].getX() - players[id].getHitbox().getWidth());
							flag = false;
						}
						if (past_y < players[i].getY() && flag) {
							players[id].setY(players[i].getY() - players[id].getHitbox().getHeight() - 1);
							
							//set jumping to false, speed to 0, acceleration along y to 0 when touching a player below
							players[id].setJumping(false);
							players[id].setSpeedY(0);
							players[id].setAccelerationY(0);
						}
						if (past_y > (players[i].getY() + players[i].getHitbox().getHeight())) {
							players[id].setY(players[i].getY() + players[i].getHitbox().getHeight() + 1);
							players[id].setSpeedY(0);
						}
					}
				}
			}
			
			//check for player-to-platform collisions
			for(int i=0; i<NUM_OF_PLATFORMS; i++){
				if(platforms[i].intersects(players[id].getHitbox())){
	
					if (past_x>(platforms[i].getX()+platforms[i].getWidth() - 1) && (past_y + players[id].getHitbox().getHeight())>platforms[i].getY()) {
						players[id].setX(platforms[i].getX() + platforms[i].getWidth() + 1);
					}
					else if (past_y<platforms[i].getY() && (past_x + players[id].getHitbox().getWidth()) > platforms[i].getX()) {
						players[id].setY(platforms[i].getY() - players[id].getHitbox().getHeight());
						
						//set jumping to false, speed to 0, acceleration along y to 0 when touching the ground
						players[id].setJumping(false);
						players[id].setSpeedY(0);
						players[id].setAccelerationY(0);
					}
					if (past_y>(platforms[i].getY()+platforms[i].getHeight() - 1) && (past_x + players[id].getHitbox().getWidth()) > platforms[i].getX()) {
						players[id].setY(platforms[i].getY() + platforms[i].getHeight() + 1);
						players[id].setSpeedY(0);
					}
					else if (past_x<platforms[i].getX() && (past_y + players[id].getHitbox().getHeight()) > platforms[i].getY()) {
						players[id].setX(platforms[i].getX() - players[id].getHitbox().getWidth());
					}
				}
			}
			
			//sway
			t++;
			sway = 5 * thread.drunk_level * (float)Math.cos(0.1f * t);
			swayY = 3 * thread.drunk_level * (float)Math.cos(0.1f * t - 2.1f);
			
			//SEND DEATH MESSAGE TO SERVER
			if(players[id].getY()>=600){
				thread.conn.sendMessage("DEAD");
				dead = true;
				scream.play();
			}
			//check for changes in coordinates then broadcast the MOVE message
			if(past_x!=players[id].getX() || past_y!=players[id].getY()){
				thread.conn.sendMessage("MOVE "+players[id].getX()+" "+players[id].getY());
			}
		}
		else if (ready && !started) {
			Input input = gc.getInput();
			
			if (input.isKeyDown(Input.KEY_ENTER)) {
				thread.conn.sendMessage("START");
			}
		}
	}

	@Override
	public int getID() {
		return 2;
	}
}
