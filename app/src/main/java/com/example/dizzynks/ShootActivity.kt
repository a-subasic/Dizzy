package com.example.dizzynks

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.graphics.Point
import android.media.AudioAttributes
import android.media.SoundPool
import android.net.Uri
import android.widget.Button
import android.widget.TextView
import com.google.ar.sceneform.Camera
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.assets.RenderableSource
import com.google.ar.sceneform.collision.Ray
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.Material
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.rendering.Texture
import java.util.*

class ShootActivity : AppCompatActivity() {
    private var scene: Scene? = null
    private var camera: Camera? = null
    private var bulletRenderable: ModelRenderable? = null
    private var shouldStartTimer = true
    private var balloonsLeft = 20
    private var point: Point? = null
    private var balloonsLeftTxt: TextView? = null
    private var soundPool: SoundPool? = null
    private var sound = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shoot)

        val display = windowManager.defaultDisplay
        point = Point()
        display.getRealSize(point)

        loadSoundPool()
        balloonsLeftTxt = findViewById(R.id.balloonsCntTxt)
        val arFragment =
            supportFragmentManager.findFragmentById(R.id.arFragment) as CustomArFragment?
        scene = arFragment!!.arSceneView.scene
        camera = scene!!.camera
        addBalloonsToScene()
        buildBulletModel()
        val shoot = findViewById<Button>(R.id.shootButton)
        shoot.setOnClickListener {
            if (shouldStartTimer) {
                startTimer()
                shouldStartTimer = false
            }
            shoot()
        }
    }

    private fun loadSoundPool() {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setUsage(AudioAttributes.USAGE_GAME)
            .build()
        soundPool = SoundPool.Builder()
            .setMaxStreams(1)
            .setAudioAttributes(audioAttributes)
            .build()
        sound = soundPool!!.load(this, R.raw.blop_sound, 1)
    }

    private fun shoot() {
        val ray: Ray = camera!!.screenPointToRay(point!!.x / 2f, point!!.y / 2f)
        val node = Node()
        node.renderable = bulletRenderable
        scene!!.addChild(node)
        Thread(Runnable {
            for (i in 0..199) {
                runOnUiThread {
                    val vector3 = ray.getPoint(i * 0.1f)
                    node.worldPosition = vector3
                    val nodeInContact: Node? = scene!!.overlapTest(node)
                    if (nodeInContact != null) {
                        balloonsLeft--
                        balloonsLeftTxt!!.text = "Balloons Left: $balloonsLeft"
                        scene!!.removeChild(nodeInContact)
                        soundPool!!.play(
                            sound, 1f, 1f, 1, 0
                            , 1f
                        )
                    }
                }
                try {
                    Thread.sleep(10)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
            runOnUiThread { scene!!.removeChild(node) }
        }).start()
    }

    private fun startTimer() {
        val timer = findViewById<TextView>(R.id.timerText)
        Thread(Runnable {
            var seconds = 0
            while (balloonsLeft > 0) {
                try {
                    Thread.sleep(1000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                seconds++
                val minutesPassed = seconds / 60
                val secondsPassed = seconds % 60
                runOnUiThread { timer.text = "$minutesPassed:$secondsPassed" }
            }
        }).start()
    }

    private fun buildBulletModel() {
        Texture
            .builder()
            .setSource(this, R.drawable.texture)
            .build()
            .thenAccept { texture ->
                MaterialFactory
                    .makeOpaqueWithTexture(this, texture)
                    .thenAccept { material: Material? ->
                        bulletRenderable = ShapeFactory
                            .makeSphere(
                                0.01f,
                                Vector3(0f, 0f, 0f),
                                material
                            )
                    }
            }
    }

    private fun addBalloonsToScene() {
        ModelRenderable
            .builder()
            .setSource(this, RenderableSource.builder().setSource(this, Uri.parse("test.glb"), RenderableSource.SourceType.GLB).build())
            .build()
            .thenAccept { renderable: ModelRenderable? ->
                for (i in 0..19) {
                    val node = Node()
                    node.renderable = renderable
                    scene!!.addChild(node)
                    val random = Random()
                    val x = random.nextInt(10)
                    var z = random.nextInt(10)
                    val y = random.nextInt(20)
                    z = -z
                    node.worldPosition = Vector3(
                        x.toFloat(),
                        y / 10f,
                        z.toFloat()
                    )
                }
            }
    }
}