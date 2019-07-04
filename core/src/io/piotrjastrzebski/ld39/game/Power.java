package io.piotrjastrzebski.ld39.game;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectSet;
import io.piotrjastrzebski.ld39.game.building.*;

public class Power {
	
    private float totalPower;
    //private float requiredPower;
    private Buildings buildings;

    private Array<IPowerProducer> allProducers = new Array<>();
    private Array<IPowerConsumer> allConsumers = new Array<>();
    private Array<Grid> grids = new Array<>();

    public Power(Buildings buildings) {
        this.buildings = buildings;
    }

    public void update(float delta) {
        totalPower = 0;
        allConsumers.clear();
        allProducers.clear();
        
        for (Building building : buildings.getAll()) {
            if (building instanceof IPowerProducer) {
                allProducers.add((IPowerProducer)building);
            }
            if (building instanceof IPowerConsumer) {
                allConsumers.add((IPowerConsumer)building);
            }
        }

        grids.clear();
        for (IPowerProducer producer : allProducers) {
            if (!(producer instanceof IPowerConnector)) {
                throw new AssertionError("Welp");
            }
            IPowerConnector connector = (IPowerConnector)producer;
            Grid selected = null;
            for (Grid grid : grids) {
                if (grid.producers.contains(producer)) {
                    selected = grid;
                    break;
                }
            }
            if (selected == null) {
                selected = new Grid();
                grids.add(selected);
            }

            addConnectors(selected, connector);
        }

        for (Grid grid : grids) {
            float gridPower = 0;
            float gridRequired = 0;
            for (IPowerProducer producer : grid.producers) {
                totalPower += producer.storage();
                gridPower += producer.storage();
            }

            //requiredPower = 0;
            for (IPowerConsumer consumer : grid.consumers) {
                //requiredPower += consumer.required();
                gridRequired += consumer.required();
            }

            if (gridPower >= gridRequired) {
                for (IPowerProducer producer : grid.producers) {
                    gridRequired = producer.consume(gridRequired);
                }
                for (IPowerConsumer consumer : grid.consumers) {
                    consumer.provide();
                }
            }
        }
    }

    private void addConnectors (Grid grid, IPowerConnector source) {
        if (!grid.connectors.add(source)) {
        	return;
        }
        if (source instanceof IPowerProducer) {
            grid.producers.add((IPowerProducer)source);
        }
        if (source instanceof IPowerConsumer) {
            grid.consumers.add((IPowerConsumer)source);
        }
        for (IPowerConnector connector : source.connected()) {
            addConnectors(grid, connector);
        }
    }

    public void debugDraw(ShapeRenderer shapes) {
        if (false) return;
        shapes.setColor(Color.CYAN);

        for (Grid grid : grids) {
            for (IPowerConnector connector : grid.connectors) {
                Building cc = (Building)connector;
                for (IPowerConnector other : connector.connected()) {
                    Building oc = (Building)other;
                    shapes.line(cc.cx(), cc.cy(), oc.cx(), oc.cy());
                }
            }
        }

    }

    static class Grid {
        ObjectSet<IPowerProducer> producers = new ObjectSet<>();
        ObjectSet<IPowerConsumer> consumers = new ObjectSet<>();
        ObjectSet<IPowerConnector> connectors = new ObjectSet<>();
    }

    public float getTotalPower () {
        return totalPower;
    }
}
