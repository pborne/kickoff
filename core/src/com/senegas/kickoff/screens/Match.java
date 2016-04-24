package com.senegas.kickoff.screens;

import tactics.Tactic;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.senegas.kickoff.entities.Ball;
import com.senegas.kickoff.entities.Player;
import com.senegas.kickoff.pitches.ClassicPitch;
import com.senegas.kickoff.pitches.FootballDimensions;
import com.senegas.kickoff.pitches.Pitch;
import com.senegas.kickoff.pitches.PlayerManagerPitch;
import com.senegas.kickoff.pitches.Scanner;
import com.senegas.kickoff.pitches.SoggyPitch;
import com.senegas.kickoff.pitches.SyntheticPitch;
import com.senegas.kickoff.pitches.WetPitch;
import com.senegas.kickoff.utils.Joystick;
import com.senegas.kickoff.utils.OrthoCamController;

public class Match implements Screen {
	private OrthogonalTiledMapRenderer renderer;
    public OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;
	private SpriteBatch batch;
	private Pitch pitch;
	public Ball ball;
	public Player player;
	private Scanner scanner;
	private Tactic tactic;
	
	private static final boolean DEBUG = true;
	
	@Override
	public void render(float deltaTime) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);   
        
        player.update(deltaTime);
        ball.update(deltaTime);
        
        checkCollisions();
        
        camera.position.x = MathUtils.clamp(ball.getPosition().x, camera.viewportWidth/2 * camera.zoom, Pitch.WIDTH - camera.viewportWidth/2 * camera.zoom);
        camera.position.y = MathUtils.clamp(ball.getPosition().y, camera.viewportHeight/2 * camera.zoom, Pitch.HEIGHT - camera.viewportHeight/2 * camera.zoom);
        camera.update();
        
        renderer.setView(camera);
		renderer.render();
		
		if (DEBUG)
			tactic.showRegionAndExpectedPlayerLocation(this);
		
        renderer.getBatch().begin();
        player.draw(renderer.getBatch());
        ball.draw(renderer.getBatch());
        renderer.getBatch().end();
		
        scanner.draw(shapeRenderer);
        
		batch.begin();		
		font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
		font.draw(batch, "Player: " + (int)player.getPosition().x + ", " + (int)player.getPosition().y, 10, 40);
		font.draw(batch, "Ball: " + (int)ball.getPosition().x + ", " + (int)ball.getPosition().y + ", " + (int)ball.getPosition().z, 10, 60);
		font.draw(batch, tactic.getName(), 10, 80);
		batch.end(); 

		handleInput();
	}
	
	private void checkCollisions() {
		if (player.getBounds().contains(ball.getPosition().x, ball.getPosition().y)) {
			if (ball.getPosition().z < player.height()/FootballDimensions.CM_PER_PIXEL) { //!Reimp move constant elsewhere
				//System.out.format("collide%n");
				ball.dribble(player.speed(), player.getDirection()); // dribble
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		camera.viewportHeight = height;
		camera.viewportWidth = width;
	}

	@Override
	public void show() {
		pitch = new ClassicPitch();
		renderer = new OrthogonalTiledMapRenderer(pitch.getTiledMap());
        camera = new OrthographicCamera();
        shapeRenderer = new ShapeRenderer();
        //camera.setToOrtho(true);
        camera.zoom = .45f;
        
        player = new Player(Pitch.WIDTH/3, Pitch.HEIGHT/2);
        ball = new Ball((int) (Pitch.PITCH_WIDTH_IN_PX/2 + Pitch.OUTER_TOP_EDGE_X - 8),
        		        (int) (Pitch.PITCH_HEIGHT_IN_PX/2 + Pitch.OUTER_TOP_EDGE_Y + 8), 80);        
        scanner = new Scanner(this);
        tactic = new Tactic("tactics/4-3-3.xml");
           
        //cameraController = new OrthoCamController(camera);
		Gdx.input.setInputProcessor(player);
		
		font = new BitmapFont();
		batch = new SpriteBatch();
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		pitch.dispose();
		renderer.dispose();
		player.getTexture().dispose();
		ball.getTexture().dispose();
		shapeRenderer.dispose();
		tactic.dispose();
	}
	
	private void handleInput() {
        if(Gdx.input.isKeyPressed(Input.Keys.O)) {
                camera.zoom += 0.02;
        }
        if(Gdx.input.isKeyPressed(Input.Keys.I)) {
        	camera.zoom -= 0.02;
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.S)) {
        	ball.dribble(400, 6);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.D)) {
        	ball.dribble(400, 2);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.E)) {
        	ball.dribble(400, 0);
        }
        if(Gdx.input.isKeyJustPressed(Input.Keys.X)) {
        	ball.dribble(400, 4);
        }    
        // handle scanner zoom
        if(Gdx.input.isKeyJustPressed(Input.Keys.A)) {
        	scanner.toggleZoom();
        }
//        if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
//                if (camera.position.x > 0 + camera.viewportWidth)
//                	camera.translate(-3, 0, 0);
//        }
//        if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
//                if (camera.position.x < Pitch.WIDTH - camera.viewportWidth)
//                	camera.translate(3, 0, 0);
//        }
//        if(Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
//                if (camera.position.y > 0 + camera.viewportHeight)
//                	camera.translate(0, -3, 0);
//        }
//        if(Gdx.input.isKeyPressed(Input.Keys.UP)) {
//                if (camera.position.y < Pitch.HEIGHT - camera.viewportHeight)
//                	camera.translate(0, 3, 0);
//        }
}	

}
