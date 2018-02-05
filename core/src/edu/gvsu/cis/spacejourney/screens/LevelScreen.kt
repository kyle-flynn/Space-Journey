package edu.gvsu.cis.spacejourney.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.physics.box2d.Body
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage

import com.badlogic.gdx.utils.viewport.FillViewport
import com.badlogic.gdx.utils.viewport.FitViewport
import edu.gvsu.cis.spacejourney.Constants
import edu.gvsu.cis.spacejourney.SpaceJourney
import edu.gvsu.cis.spacejourney.entity.Graveyard
import edu.gvsu.cis.spacejourney.entity.PlayerSpaceship
import edu.gvsu.cis.spacejourney.entity.enemy.EvilSpaceship
import edu.gvsu.cis.spacejourney.input.GameContactListener
import edu.gvsu.cis.spacejourney.input.PlayerInputListener
import edu.gvsu.cis.spacejourney.managers.ActiveProjectileManager
import edu.gvsu.cis.spacejourney.screens.hud.DefaultHUD
import edu.gvsu.cis.spacejourney.screens.backgrounds.ParallaxBackground
import edu.gvsu.cis.spacejourney.util.ZIndex

/**
 * Where the magic happens
 */
class LevelScreen(game : SpaceJourney) : BaseScreen(game, "LevelScreen") {

    private var debugRenderer: Box2DDebugRenderer? = null

    private var camera: OrthographicCamera? = null
    private var viewport: FitViewport? = null
    private var stage : Stage? = null

    private var overlayCam: OrthographicCamera? = null
    private var overlayViewport: FillViewport? = null
    private var overlayStage: Stage? = null

    private var world: World? = null
    private var contactListener: GameContactListener? = null

    private var background : ParallaxBackground? = null
    private var player: PlayerSpaceship? = null

    private var hudTable: DefaultHUD? = null

    private var projManager: ActiveProjectileManager? = null
    private var inputListener: PlayerInputListener? = null

    override fun show() {
        super.show()

        camera = OrthographicCamera()
        viewport = FitViewport(Constants.VIRTUAL_WIDTH / Constants.PX_PER_M,
                Constants.VIRTUAL_HEIGHT / Constants.PX_PER_M, camera)
        stage = Stage(viewport)

        overlayCam = OrthographicCamera()
        overlayViewport = FillViewport(Constants.VIRTUAL_WIDTH, Constants.VIRTUAL_HEIGHT, overlayCam)
        overlayStage = Stage(overlayViewport)

        world = World(Vector2(0.0f, 0.0f), true)
        contactListener = GameContactListener()

        world?.setContactListener(contactListener)

        debugRenderer = Box2DDebugRenderer()

        // For whatever reason, we use PX_PER_M for position,
        // but we use regular for width/height.
        player = PlayerSpaceship(stage)
        player?.setPosition(1.5f, 0.0f)
        player?.width = 50.0f
        player?.height = 50.0f
        player?.createBody(world)

        stage?.addActor(player)

        background = ParallaxBackground()
        background?.zIndex = ZIndex.BACKGROUND
        stage?.addActor(background)

        projManager = ActiveProjectileManager.getInstance()
        projManager?.setStage(stage)
        projManager?.setWorld(world)
        projManager?.init()

        inputListener = PlayerInputListener(player)
        Gdx.input.inputProcessor = inputListener

        hudTable = DefaultHUD()

        overlayStage?.addActor(hudTable)

        // Add quick enemy
        val enemy: EvilSpaceship? = EvilSpaceship(stage)
        enemy?.setPosition(1.5f, 1.5f)
        enemy?.width = 50.0f
        enemy?.height = 50.0f
        enemy?.createBody(world)

        stage?.addActor(enemy)

        val music: Music? = SpaceJourney.assetManager.get("Space Background Music.mp3")
        music?.volume = 0.5f
        music?.isLooping = true
        music?.play()
    }

    // Be mindful about nullable-types, as resize is called before show
    override fun resize(width: Int, height: Int) {
        super.resize(width, height)

        viewport?.update(width, height, true)
        overlayViewport?.update(width, height)

        camera?.update()
    }

    override fun render(delta: Float) {
        super.render(delta)

        batch?.begin()

        viewport?.apply()

        hudTable?.poll()
        projManager?.poll()
        inputListener?.poll(delta)

        camera?.update()

//        debugRenderer?.render(world, camera?.combined)

        batch?.projectionMatrix = camera?.combined

        stage?.act()
        stage?.draw()

        overlayStage?.act()
        overlayStage?.draw()

        batch?.end()

        world?.step(1.0f/60.0f, 6, 2)

        getRidOfBodies()
    }

    private fun getRidOfBodies() {
        for (body: Body in Graveyard.bodies) {
            world?.destroyBody(body)
        }
        Graveyard.bodies.clear()

        for (actor: Actor in Graveyard.actors) {
            actor.remove()
        }

        Graveyard.actors.clear()
    }

    override fun dispose() {
        batch?.dispose()
        player?.dispose()
        stage?.dispose()
        overlayStage?.dispose()
    }

}