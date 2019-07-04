package io.piotrjastrzebski.ld39;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.VisDialog;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import io.piotrjastrzebski.ld39.game.*;
import io.piotrjastrzebski.ld39.game.building.Building;
import io.piotrjastrzebski.ld39.game.building.Buildings;
import io.piotrjastrzebski.ld39.game.utils.IntRect;

/**
 * Created by PiotrJ on 16/04/16.
 */
public class GameScreen extends ScreenAdapter {

	private final ExtendViewport gameViewport;
	private final ScreenViewport guiViewport;
	private final ShapeRenderer shapes;
	private final SpriteBatch batch;
	private CameraController cameraController;
	private Table root;
	private Stage stage;
	private VisLabel hoverInfo;
	private VisLabel envInfo;
	private VisLabel researchInfo;
	private VisTextButton toggleCoal;

	private Map map;
	private Buildings buildings;
	private GreenhouseGasses GHG;
	private Power power;
	private Research research;
	private boolean drawGui = true;

	private boolean buildShown;
	private boolean gameStartShown;
	private boolean gameOverShown;
	private boolean gameWinShown;
	private Building selected;
	private Vector2 tp = new Vector2();

	public GameScreen (LD39Game game) {
		batch = game.batch;

		//        Gdx.app.setLogLevel(Application.LOG_DEBUG);

		gameViewport = new ExtendViewport(1280 / 32f, 720/32f);
		guiViewport = new ScreenViewport();
		shapes = new ShapeRenderer();
		stage = new Stage(guiViewport, batch);
		root = new Table();
		root.setFillParent(true);
		stage.addActor(root);

		map = new Map();
		buildings = new Buildings(gameViewport, map);
		GHG = new GreenhouseGasses(gameViewport, map, buildings);
		power = new Power(buildings);
		research = new Research(buildings);

		InputMultiplexer multiplexer = new InputMultiplexer();
		cameraController = new CameraController(map, gameViewport);
		multiplexer.addProcessor(stage);
		multiplexer.addProcessor(cameraController);
		multiplexer.addProcessor(buildings);
		Gdx.input.setInputProcessor(multiplexer);

		{
			Table buildMenu = new Table();
			root.add(buildMenu).expand().bottom();
			for (final Building building : buildings.templates()) {
				VisTextButton build = new VisTextButton(building.name);
				buildMenu.add(build).pad(4);
				build.addListener(new ClickListener(){
					@Override public void clicked (InputEvent event, float x, float y) {
						Gdx.app.log("build", building.name);
						showBuildDialog();
						buildings.build(building);
					}
				});
			}
			VisTextButton demolish = new VisTextButton("Demolish");
			demolish.setColor(Color.RED);
			buildMenu.add(demolish).pad(4);
			demolish.addListener(new ClickListener(){
				@Override public void clicked (InputEvent event, float x, float y) {
					buildings.demolish();
				}
			});
		}
		SpriteDrawable drawable = new SpriteDrawable((SpriteDrawable)VisUI.getSkin().getDrawable("dialogDim"));
		drawable.getSprite().setColor(0, 0, 0, .5f);
		{
			VisTable infoContainer = new VisTable(true);
			infoContainer.setFillParent(true);
			root.addActor(infoContainer);
			hoverInfo = new VisLabel();
			hoverInfo.getStyle().background = drawable;
			infoContainer.add(hoverInfo).expand().left().pad(10);
		}
		{
			VisTable infoContainer = new VisTable(true);
			infoContainer.setFillParent(true);
			root.addActor(infoContainer);
			envInfo = new VisLabel();
			envInfo.getStyle().background = drawable;
			infoContainer.add(envInfo).expand().left().bottom().pad(10).padBottom(60);
		}
		{
			VisTable infoContainer = new VisTable(true);
			infoContainer.setFillParent(true);
			root.addActor(infoContainer);
			researchInfo = new VisLabel();
			researchInfo.getStyle().background = drawable;
			infoContainer.add(researchInfo).expand().left().top().pad(10);
		}
		{
			VisTable infoContainer = new VisTable(true);
			infoContainer.setFillParent(true);
			root.addActor(infoContainer);
			toggleCoal = new VisTextButton("Show coal", "toggle");
			toggleCoal.setChecked(true);
			toggleCoal.addListener(new ClickListener(){
				@Override public void clicked (InputEvent event, float x, float y) {
					map.showCoal(toggleCoal.isChecked());
				}
			});
			infoContainer.add(toggleCoal).expand().right().bottom().pad(10).padBottom(60);
		}
	}

	private void showBuildDialog () {
		if (buildShown) {
			return;
		}
		buildShown = true;

		VisDialog dialog = new VisDialog("Building things!");
		dialog.setMovable(false);
		dialog.addCloseButton();
		dialog.text("Left click to build, right click or ESC or X to cancel!\nRotate belts with Q and E");
		dialog.button("OK");
		dialog.show(stage);
	}


	@Override 
	public void render (float delta) {
		Gdx.gl.glClearColor(.25f, .25f, .25f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		gameViewport.getCamera().update();
		tp.set(Gdx.input.getX(), Gdx.input.getY());
		gameViewport.unproject(tp);

		// cap delta so it never goes too low
		delta = Math.min(delta, 1f/15f);
		map.update(tp, delta);
		power.update(delta);
		buildings.update(delta);
		GHG.update(delta);
		research.update(delta);


		batch.setProjectionMatrix(gameViewport.getCamera().combined);
		batch.enableBlending();
		batch.begin();

		batch.end();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		shapes.setProjectionMatrix(gameViewport.getCamera().combined);
		shapes.begin(ShapeRenderer.ShapeType.Filled);
		map.drawDebug(shapes);
		buildings.drawDebug(shapes);
		GHG.drawDebug(shapes);
		shapes.end();

		shapes.setProjectionMatrix(gameViewport.getCamera().combined);
		shapes.begin(ShapeRenderer.ShapeType.Line);
		if (selected != null) {
			shapes.setColor(Color.YELLOW);
			IntRect bounds = selected.bounds;
			shapes.rect(bounds.x - .15f, bounds.y - .15f, bounds.width + .3f, bounds.height + .3f);
		}
		power.debugDraw(shapes);
		shapes.end();

		if (selected != null) {
			hoverInfo.setText(selected.info());
		}
		if (Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
			selected = null;
			for (Building building : buildings.getAll()) {
				if (building.bounds.contains(tp.x, tp.y)) {
					selected = building;
					break;
				}
			}
		}

		if (selected == null) {
			Map.Tile tile = map.getTile((int)tp.x, (int)tp.y);
			if (tile != null) {
				hoverInfo.setText("Elevation = " + format(tile.elevation) + "\nCoal = " + format(tile.coal));
			}
			for (Building<?> building : buildings.getAll()) {
				if (building.bounds.contains(tp.x, tp.y)) {
					hoverInfo.setText(building.info());
					break;
				}
			}
		}

		envInfo.setText("Sea level = " + format(map.seaLevel()) + "\nGreenhouse gasses = " + format(GHG.ghgLevel()));
		researchInfo.setText(research.info());

		if (Gdx.input.isKeyJustPressed(Input.Keys.SLASH)) {
			drawGui = !drawGui;
		}

		stage.act(delta);
		if (drawGui) {
			stage.draw();
		}

		if (map.isSeaLevelHigh() && !gameOverShown) {
			gameOverShown = true;
			VisDialog dialog = new VisDialog("Game Over!");
			dialog.setMovable(false);
			dialog.addCloseButton();
			dialog.text("Sea consumed our small island.\nWith time, sea will recede.\nIs it the end?");
			dialog.button("OK");
			dialog.show(stage);
		}
		if (research.lastResearched() && !gameWinShown) {
			gameWinShown = true;
			VisDialog dialog = new VisDialog("You win!");
			dialog.setMovable(false);
			dialog.addCloseButton();
			dialog.text("You researched all the things and won.\nCongrats!");
			dialog.button("OK");
			dialog.show(stage);
		}
		if (!gameStartShown) {
			gameStartShown = true;
			VisDialog dialog = new VisDialog("Power is running out!") {
				@Override protected void result (Object object) {
					VisDialog dialog = new VisDialog("Move");
					dialog.setMovable(false);
					dialog.addCloseButton();
					dialog.text("Move the map with by holding left mouse button and dragging.\nZoom with scroll.");
					dialog.button("OK");
					dialog.show(stage);
				}
			};
			dialog.setMovable(false);
			dialog.text("We need to research high efficiency panels to survive!\nDont overdo the coal or bad things might happen...");
			dialog.button("OK");
			dialog.show(stage);
		}
	}

	private String format(float value) {
		int val = (int)(value * 100);
		int rem = val % 100;
		return (val/100) + "." + rem;
	}

	
	@Override 
	public void resize(int width, int height) {
		gameViewport.update(width, height, false);
		guiViewport.update(width, height);
	}

	
	@Override 
	public void dispose() {
		shapes.dispose();
		Gdx.input.setInputProcessor(null);
	}
}
