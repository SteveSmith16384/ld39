package io.piotrjastrzebski.ld39.game.building;

import com.badlogic.gdx.utils.ObjectSet;

public interface IPowerConnector {
	
    boolean connect(IPowerConnector other);
    
    Building owner();

    void disconnect (IPowerConnector connector);

    void disconnectAll ();

    ObjectSet<IPowerConnector> connected();
}
