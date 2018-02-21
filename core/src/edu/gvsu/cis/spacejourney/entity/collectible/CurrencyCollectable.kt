package edu.gvsu.cis.spacejourney.entity.collectible

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.Stage
import edu.gvsu.cis.spacejourney.SpaceJourney

class CurrencyCollectable(stage: Stage?) : Collectible(stage) {

    init {
        this.setSize(25f, 25f)
        this.setPosition(1.0f, 0.5f)

        this.addAnimation("idle", SpaceJourney.assetManager.get<Texture>("rotating_pickup.png"), 0, 23, 48, 48)
    }

}