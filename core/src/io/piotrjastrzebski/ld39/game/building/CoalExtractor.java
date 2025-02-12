package io.piotrjastrzebski.ld39.game.building;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.StringBuilder;
import io.piotrjastrzebski.ld39.game.Coal;
import io.piotrjastrzebski.ld39.game.Map;

public class CoalExtractor extends Building<CoalExtractor> {

	private static final float coalPerSecond = 5;
	private static final float coalCap = 50;
	private static final float coalSpawn = 10;
	private static final float ghgPerSecond = 0.00025f;

	private final static int ext_size = 6;
	private final static int[] extractions = new int[]{
			0, 1, 1, 1, 1, 0,
			1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1,
			1, 1, 1, 1, 1, 1,
			0, 1, 1, 1, 1, 0
	};
	private final static int out_size = 4;
	private final static int[] outputs = new int[]{
			0, 1, 1, 0,
			1, 0, 0, 1,
			1, 0, 0, 1,
			0, 1, 1, 0,
	};
	private static final int extractTileCount;
	static {
		int count = 0;
		for (int ox = 0; ox < ext_size; ox++) {
			for (int oy = 0; oy < ext_size; oy++) {
				count += extractions[ox + oy * ext_size];
			}
		}
		extractTileCount = count;
	}
	private float fieldTotal;
	private float coal;
	private boolean noOutlet;
	private boolean fieldEmpty;

	public CoalExtractor (int x, int y) {
		super("Coal Mine", 250, x, y, 2, 2);
		tint.set(Color.DARK_GRAY);

	}

	@Override 
	public void update (float delta) {
		super.update(delta);
		if (coal < coalCap) {
			int bx = bounds.x - 2;
			int by = bounds.y - 2;
			float extracted = 0;
			float extractPerTile = coalPerSecond / extractTileCount * delta;
			fieldTotal = 0;
			for (int x = 0; x < ext_size; x++) {
				for (int y = 0; y < ext_size; y++) {
					int extracts = extractions[x + y * ext_size];
					if (extracts == 1) {
						Map.Tile tile = map.getTile(bx + x, by + y);
						if (tile == null || tile.coal <= 0) {
							continue;
						}
						// we can extract nonexisting coal but who cares
						tile.coal -= extractPerTile;
						extracted += extractPerTile;
						fieldTotal += tile.coal;
					}
				}
			}
			if (extracted > 0) {
				// we dont really care if we go over cap in here
				coal += extracted;
				if (coal >= coalCap) {
					Gdx.app.log("", "Reached coal cap!");
				}
				buildings.GHG.addGHG(ghgPerSecond * delta);
			} else {
				fieldEmpty = true;
			}
		}
		//coalSpawn = 10;
		if (coal >= coalSpawn) {
			noOutlet = true;
			int bx = bounds.x - 1;
			int by = bounds.y - 1;
			for (int x = 0; x < out_size; x++) {
				for (int y = 0; y < out_size; y++) {
					int output = outputs[x + y * out_size];
					if (output == 1) {
						Map.Tile tile = map.getTile(bx + x, by + y);
						if (tile != null && tile.building instanceof ICoalConsumer) {
							ICoalConsumer belt = (ICoalConsumer)tile.building;
							if (belt.accept(new Coal(coalSpawn))) {
								coal -= coalSpawn;
								noOutlet = false;
								if (coal < coalSpawn) {
									return;
								}
							}
						}
					}
				}
			}
		}
	}

	
	@Override 
	public void drawDebug (ShapeRenderer shapes) {
		int bx = bounds.x - 2;
		int by = bounds.y - 2;
		for (int x = 0; x < ext_size; x++) {
			for (int y = 0; y < ext_size; y++) {
				int extracts = extractions[x + y * ext_size];
				if (extracts == 1) {
					Map.Tile tile = map.getTile(bx + x, by + y);
					if (tile == null) continue;
					if (tile.coal > 0) {
						shapes.setColor(0, 1, 0, .5f);
					} else {
						shapes.setColor(1, 0, 0, .5f);
					}
					shapes.rect(bx + x + .1f, by + y + .1f, .8f, .8f);
				}
			}
		}
		super.drawDebug(shapes);
	}

	@Override 
	public void drawDebug2 (ShapeRenderer shapes) {
		super.drawDebug2(shapes);
		if (fieldEmpty || coal >= coalCap) {
			float cx = cx();
			float cy = cy();
			shapes.setColor(Color.YELLOW);
			shapes.triangle(cx - .15f, cy + .6f, cx + .15f, cy + .6f, cx, cy - .3f);
			shapes.circle(cx, cy - .5f, .1f, 8);
		}
		if (flooded) {
			drawFlooded(shapes);
		}
	}

	@Override public String info () {
		StringBuilder sb = new StringBuilder(name);
		sb.append("\nCoal per sec = ").append(coalPerSecond);
		if (noOutlet) {
			sb.append("\nNo outlet!");
		}
		if (fieldEmpty) {
			sb.append("\nNo coal left!");
		} else {
			sb.append("\nCoal remaining = ").append(fieldTotal);
		}
		if (coal >= coalCap) {
			sb.append("\nStorage full!");
		}
		return sb.toString();
	}

	@Override 
	public CoalExtractor duplicate () {
		CoalExtractor instance = new CoalExtractor(bounds.x, bounds.y);
		return super.duplicate(instance);
	}
}
