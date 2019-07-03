package io.piotrjastrzebski.ld39.game.building;

public interface IPowerProducer {
    float storage();
    float consume (float power);
}
