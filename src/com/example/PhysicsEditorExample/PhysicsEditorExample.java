package com.example.PhysicsEditorExample;

/**
 * PhysicsEditor Importer Library
 *
 * Usage:
 * - Create an instance of this class
 * - Use the "open" method to load an XML file from PhysicsEditor
 * - Invoke "createBody" to create bodies from library.
 *
 * by Adrian Nilsson (ade at ade dot se)
 * BIG IRON GAMES (bigirongames.org)
 * Date: 2011-08-30
 * Time: 11:51
 * 
 * modified to GLES2-AnchorCenter by Agung Cahyawan (agung dot cahyawan at unud dot ac dot id)
 */

import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;

import android.hardware.SensorManager;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class PhysicsEditorExample extends SimpleBaseGameActivity implements IOnSceneTouchListener{

	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 800;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private BitmapTextureAtlas mBitmapTextureAtlas;
	private PhysicsEditorShapeLibrary physicsEditorShapeLibrary;
	private Scene mScene;
	private PhysicsWorld mPhysicsWorld;

	// Texture regions
	private TextureRegion textureBurger;
	private TextureRegion textureDrink;
	private TextureRegion textureHotdog;
	private TextureRegion textureIcecream;
	private TextureRegion textureIcecream2;
	private TextureRegion textureIcecream3;
	private HashMap<String, TextureRegion> textureRegionHashMap = new HashMap<String, TextureRegion>();

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Camera camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.LANDSCAPE_FIXED, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), camera);

		return engineOptions;
	}

	@Override
	protected void onCreateResources() throws IOException {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),
				512, 512, TextureOptions.DEFAULT);
		this.textureDrink = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "drink.png",
						0, 0);
		this.textureBurger = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"hamburger.png", 80, 89);
		this.textureHotdog = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this, "hotdog.png",
						0, 189);
		this.textureIcecream = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"icecream.png", 80, 0);
		this.textureIcecream2 = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"icecream2.png", 207, 0);
		this.textureIcecream3 = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mBitmapTextureAtlas, this,
						"icecream3.png", 183, 102);
        textureRegionHashMap.put("drink", this.textureDrink);
        textureRegionHashMap.put("hamburger", this.textureBurger);
        textureRegionHashMap.put("hotdog", this.textureHotdog);
        textureRegionHashMap.put("icecream", this.textureIcecream);
        textureRegionHashMap.put("icecream2", this.textureIcecream2);
        textureRegionHashMap.put("icecream3", this.textureIcecream3);
		this.mEngine.getTextureManager().loadTexture(this.mBitmapTextureAtlas);
		
        //Create the shape importer and open our shape definition file
        this.physicsEditorShapeLibrary = new PhysicsEditorShapeLibrary();
        this.physicsEditorShapeLibrary.open(this, "shapes/shapes.xml");
        

	}

	@Override
	protected Scene onCreateScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.mScene = new Scene();
		mScene.setBackground(new Background(0.85f, 0.85f, 0.85f));
		this.mScene.setOnSceneTouchListener(this);
		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, -SensorManager.GRAVITY_EARTH), false);
		
		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(CAMERA_WIDTH / 2, 1,
				CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(CAMERA_WIDTH / 2,
				CAMERA_HEIGHT - 1, CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle left = new Rectangle(1, CAMERA_HEIGHT / 2, 1,
				CAMERA_HEIGHT, vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 1,
				CAMERA_HEIGHT / 2, 2, CAMERA_HEIGHT, vertexBufferObjectManager);		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);

		this.mScene.attachChild(ground);
		this.mScene.attachChild(roof);
		this.mScene.attachChild(left);
		this.mScene.attachChild(right);

		this.mScene.registerUpdateHandler(this.mPhysicsWorld);

		return this.mScene;
		

	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		if(this.mPhysicsWorld != null) {
			if(pSceneTouchEvent.isActionDown()) {
				this.addFace(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
				return true;
			}
		} 
		return false;
	}

	private void addFace(final float pX, final float pY) {
        Random rnd = new Random();
        //Specify a shape name to import
        String[] shapes = new String[] {"drink", "hamburger", "icecream", "icecream2", "icecream3"};
        String shapeName = shapes[rnd.nextInt(shapes.length-1)];
      //Create the graphics
        Sprite sprite = new Sprite(0, 0, this.textureRegionHashMap.get(shapeName),getVertexBufferObjectManager());
        sprite.setPosition(pX ,pY);
        //Get physics body
        Body body = this.physicsEditorShapeLibrary.createBody(shapeName, sprite, this.mPhysicsWorld);

        //Attach to physics world & scene
		this.mScene.attachChild(sprite);
		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(sprite, body, true, true));
	}
}
