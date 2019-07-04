package io.piotrjastrzebski.ld39.game.building;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import com.badlogic.gdx.utils.StringBuilder;

import io.piotrjastrzebski.ld39.Settings;

public class UtilityPole extends Building<UtilityPole> implements IPowerConnector {
	
    private ObjectSet<IPowerConnector> connectors = new ObjectSet<>();

    public UtilityPole (int x, int y) {
        super("Utility Pole", 5, x, y, 1, 1);
        tint.set(Color.BROWN);
    }

    public void invalidate() {
        for (IPowerConnector connector : connectors) {
            connector.disconnect(this);
        }

        connectors.clear();
        Array<Building<?>> all = buildings.getAll();
        tmp.set(cx(), cy());
        for (int i = 0; i < all.size; i++) {
            Building other = all.get(i);
            if (!(other instanceof IPowerConnector)) {
            	continue;
            }
            if (other == this) {
            	continue;
            }
            Building owner = ((IPowerConnector)other).owner();
            if (tmp.dst(owner.cx(), owner.cy()) <= Settings.UtilityPoleMaxDistance) {
                if (((IPowerConnector)other).connect(this)) {
                    connect((IPowerConnector)other);
                }
            }
        }
    }

    @Override 
    public String info () {
        StringBuilder sb = new StringBuilder(name);
        if (connectors.size > 0) {
            sb.append("\nConnected=").append(connectors.size);
        } else {
            sb.append("\nNot connected!");
        }
        return sb.toString();
    }

    @Override 
    public void drawDebug (ShapeRenderer shapes) {
        super.drawDebug(shapes);

    }

    @Override 
    public void drawDebug2 (ShapeRenderer shapes) {
        super.drawDebug2(shapes);
        shapes.setColor(Color.BROWN);
        float cx = cx();
        float cy = cy();
        for (IPowerConnector connector : connectors) {
            Building building = connector.owner();
            shapes.rectLine(cx, cy, building.cx(), building.cy(), .05f);
        }
        if (flooded) {
            drawFlooded(shapes);
        }
    }

    @Override 
    public UtilityPole duplicate () {
        UtilityPole instance = new UtilityPole(bounds.x, bounds.y);
        return super.duplicate(instance);
    }

    
    @Override 
    public boolean connect (IPowerConnector other) {
        connectors.add(other);
        return true;
    }
    

    @Override 
    public void disconnect (IPowerConnector connector) {
        connectors.remove(connector);
    }

    @Override 
    public void disconnectAll () {
        for (IPowerConnector connector : connectors) {
            connector.disconnect(this);
        }
        connectors.clear();
    }

    @Override 
    public ObjectSet<IPowerConnector> connected () {
        return connectors;
    }

    @Override 
    public Building owner () {
        return this;
    }
    
}
