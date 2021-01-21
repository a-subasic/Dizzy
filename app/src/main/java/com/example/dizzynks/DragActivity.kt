package com.example.dizzynks

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Pose
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.*
import kotlin.random.Random

class DragActivity : AppCompatActivity(), Scene.OnUpdateListener {
    private var arFragment: ArFragment? = null
    private var tvDistance: TextView? = null
    private var btnNext1: Button? = null
    private var btnRestart: Button? = null
    private var tvDescription: TextView? = null

    private var shapeRenderableA: ModelRenderable? = null
    private var shapeRenderableB: ModelRenderable? = null

    private var nodeA: TransformableNode? = null
    private var nodeB: TransformableNode? = null

    var greenMaterial: Material? = null
    var redMaterial: Material? = null
    var originalMaterial: Material? = null

    private var finalSizeA: Vector3? = null
    private var finalSizeB: Vector3? = null

    private var level: Int = 1
    private var sizeNodeA: Float = 0f
    private var sizeNodeB: Float = 0f
    private var distanceMeters: Float = 0f

    private var distanceFormatted: String = String()
    private var scaleFormatted: String = String()
    private var rotationFormatted: String = String()

    companion object {
        var handler: Handler = Handler()
        var tMiliSec: Long = 0L
        var tStart: Long = 0L
        var sec: Int = 0
        var min: Int = 0
        var miliSec: Int = 0
        private var chronometer: TextView? = null

        private const val MIN_OPENGL_VERSION = 3.0
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(applicationContext, "Device not supported", Toast.LENGTH_LONG).show()
        }

        setContentView(R.layout.activity_drag)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        tvDistance = findViewById(R.id.tv_distance)
        tvDescription = findViewById(R.id.tv_description)
        chronometer = findViewById(R.id.chronometer)

        btnNext1 = findViewById(R.id.btn_next1)
        btnRestart = findViewById(R.id.btn_restart)
        btnNext1?.isEnabled = false
        btnNext1?.isClickable = false
        btnRestart?.isEnabled = false
        btnRestart?.isClickable = false

        initModel()


        arFragment?.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            if (shapeRenderableA != null && shapeRenderableB != null && nodeA == null && nodeB == null) {
                val anchor = hitResult.createAnchor()
                var anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment!!.arSceneView.scene)


                val node = TransformableNode(arFragment!!.transformationSystem)
                val node2 = TransformableNode(arFragment!!.transformationSystem)

                val defaultDistance = sizeNodeA+sizeNodeA/2
                val start = defaultDistance
                val end = defaultDistance+sizeNodeA/3

                var rand1 = 0
                var rand2 = 0

                while(rand1*rand2 == 0) {
                    rand1 = Random.nextInt(3) - 1
                    rand2 = Random.nextInt(3) - 1
                }

                val poseTranslationX = (start + Math.random() * (end - start)).toFloat() * rand1
                val poseTranslationZ = (start + Math.random() * (end - start)).toFloat() * rand2

                val anchor2 = hitResult.trackable.createAnchor(hitResult.hitPose.compose(Pose.makeTranslation(poseTranslationX, 0f, poseTranslationZ)))
                var anchorNode2 = AnchorNode(anchor2)
                anchorNode2.setParent(arFragment!!.arSceneView.scene)

                arFragment!!.arSceneView.scene.addChild(anchorNode)
                arFragment!!.arSceneView.scene.addChild(anchorNode2)

                node.select()
                node2.renderable = shapeRenderableB


                nodeA = node

                node.renderable = shapeRenderableA
                node.scaleController.isEnabled = false
                node.translationController.isEnabled = false
                node.rotationController.isEnabled = false

                arFragment!!.arSceneView.scene.addOnUpdateListener(this)

                node2.renderable = shapeRenderableB
                node2.scaleController.minScale = 0.05f
                node2.scaleController.maxScale = 5.0f

                nodeB = node2

                node.setParent(anchorNode)
                node2.setParent(anchorNode2)

                if(level == 1) {
                    node2.rotationController.isEnabled = false
                    node2.scaleController.isEnabled = false
                }
                if(level == 2) {
                    node2.rotationController.isEnabled = false
                }
                if(level == 3) {
                    node2.scaleController.isEnabled = false
                }
                if(level == 3 || level == 4) {
                    val randomRotation = (8 + Math.random() * (100 - 8)).toFloat() * rand1 * rand2
                    val q1: Quaternion = node2.localRotation
                    val q2 = Quaternion.axisAngle(Vector3(0f, 1f, 0f), randomRotation)
                    node2.localRotation = Quaternion.multiply(q1, q2)
                }


                btnRestart?.isEnabled = true
                btnRestart?.isClickable = true

                tStart = SystemClock.uptimeMillis()
                handler.postDelayed(UpdateTimer, 0)
            }
        }

        btnNext1?.setOnClickListener {
            levelUp()
        }

        btnRestart?.setOnClickListener {
            clearAnchors()
        }
    }

   object UpdateTimer: Runnable {
        override fun run() {
            tMiliSec = SystemClock.uptimeMillis() - tStart
            sec = (tMiliSec/1000).toInt()
            min = sec/60
            sec %= 60
            miliSec = (tMiliSec%1000).toInt()
            chronometer?.text = String.format("%02d", min) + ":" + String.format("%02d", sec) + ":" + String.format("%03d", miliSec)
            handler.postDelayed(this, 0)
        }
    }

    private fun clearAnchors() {
        tMiliSec = 0L
        tStart = 0L
        sec = 0
        min = 0
        miliSec = 0
        chronometer?.text = "00:00:000"
        handler.removeCallbacks(UpdateTimer)

        btnRestart?.isEnabled = false
        btnRestart?.isClickable = false

        if(arFragment != null && nodeA != null && nodeB != null) {
            arFragment!!.arSceneView.scene.removeChild(nodeA!!.parent!!)
            arFragment!!.arSceneView.scene.removeChild(nodeB!!.parent!!)
        }

        nodeA = null
        nodeB = null
    }

    private fun initModel() {
        sizeNodeA = if(Options.size == "Large") 0.2f else if (Options.size == "Medium") 0.1f else 0.05f
        if(level%2 != 0) sizeNodeB = sizeNodeA

        MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.GREEN))
            .thenAccept { material ->
                greenMaterial = material
            }

        MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.RED))
            .thenAccept { material ->
                redMaterial = material
            }

        MaterialFactory.makeTransparentWithColor(this, Color(android.graphics.Color.argb(200,20,20, 20)))
            .thenAccept { material ->
                if(Options.shape == "Cube") {
                    shapeRenderableA = ShapeFactory.makeCube(Vector3(sizeNodeA, sizeNodeA, sizeNodeA), Vector3.zero(), material)
                    shapeRenderableB = ShapeFactory.makeCube(Vector3(sizeNodeB, sizeNodeB, sizeNodeB), Vector3.zero(), redMaterial)
                }
                else if(Options.shape == "Cylinder") {
                    shapeRenderableA = ShapeFactory.makeCylinder(sizeNodeA/2, sizeNodeA, Vector3.zero(), material)
                    shapeRenderableB = ShapeFactory.makeCylinder(sizeNodeB/2, sizeNodeB, Vector3.zero(), redMaterial)
                }
                else {
                    shapeRenderableA = ShapeFactory.makeSphere(sizeNodeA/2, Vector3.zero(), material)
                    shapeRenderableB = ShapeFactory.makeSphere(sizeNodeB/2, Vector3.zero(), redMaterial)
                }

                originalMaterial = material

                shapeRenderableA!!.isShadowCaster = false
                shapeRenderableA!!.isShadowReceiver = false

                shapeRenderableB!!.isShadowCaster = false
                shapeRenderableB!!.isShadowReceiver = false
            }
    }

    private fun checkIsSupportedDeviceOrFinish(activity: Activity): Boolean {

        val openGlVersionString = (Objects.requireNonNull(activity.getSystemService(Context.ACTIVITY_SERVICE)) as ActivityManager)
            .deviceConfigurationInfo
            .glEsVersion
        if (java.lang.Double.parseDouble(openGlVersionString) < MIN_OPENGL_VERSION) {
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                .show()
            activity.finish()
            return false
        }
        return true
    }

    private fun levelUp() {
        level += 1

        if(level == 2) {
            tvDescription!!.text = "Nodes must be same size and distance must be 0"
            sizeNodeB = sizeNodeA/3
        }

        if(level == 3) {
            tvDescription!!.text = "Nodes must be same rotation and distance must be 0"
        }

        if(level == 4) {
            tvDescription!!.text = "Nodes must be same rotation, size and distance must be 0"
            sizeNodeB = sizeNodeA/3
            btnNext1!!.text = "Finish"
        }

        if(arFragment != null && nodeA != null && nodeB != null) {
            arFragment!!.arSceneView.scene.removeChild(nodeA!!.parent!!)
            arFragment!!.arSceneView.scene.removeChild(nodeB!!.parent!!)
        }

        nodeA = null
        nodeB = null

        btnNext1?.isEnabled = false
        btnNext1?.isClickable = false

        val levelTmp = level - 1
        Logs.data += Options.name + "," + Options.shape + "," + Options.size + ","+ Options.controls + "," + levelTmp + "," + tMiliSec + "\n"
        tMiliSec = 0L
        tStart = 0L
        sec = 0
        min = 0
        miliSec = 0
        chronometer?.text = "00:00:000"
        handler.removeCallbacks(UpdateTimer)

        if(level == 5) {
            btnRestart?.isEnabled = false
            btnRestart?.isClickable = false
            Handler().postDelayed(
                    { finish() },
                    1500)
        }

        else {
            initModel()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        nodeA = null
        nodeB = null
        tMiliSec = 0L
        tStart = 0L
        sec = 0
        min = 0
        miliSec = 0
        chronometer?.text = "00:00:000"
        handler.removeCallbacks(UpdateTimer)
    }

    override fun onUpdate(frameTime: FrameTime) {
        if (nodeA != null && nodeB != null) {
            finalSizeA = getNodeSize(nodeA!!)
            finalSizeB = getNodeSize(nodeB!!)

            val positionA = nodeA!!.worldPosition
            val positionB = nodeB!!.worldPosition

            val dx = positionA.x - positionB.x
            val dy = positionA.y - positionB.y
            val dz = positionA.z - positionB.z

            //Computing a straight-line distance.
            distanceMeters = kotlin.math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()

            val isNodesDistanceEqual = ("%.2f").format(distanceMeters) == "0.00" || ("%.2f").format(distanceMeters) == "0,00"
            val isNodesSizeEqual = ("%.2f").format(finalSizeB!!.x) == ("%.2f").format(finalSizeA!!.x) || scaleFormatted == "0.00" || scaleFormatted == "0,00"
            val isNodesRotationEqual = rotationFormatted == "0.00" || rotationFormatted == "0,00"

            if (level == 1 && isNodesDistanceEqual ||
                level == 2 && isNodesDistanceEqual && isNodesSizeEqual ||
                level == 3 && isNodesDistanceEqual && isNodesRotationEqual ||
                level == 4 && isNodesDistanceEqual && isNodesSizeEqual && isNodesRotationEqual
            ) {
                nodeA!!.renderable!!.material = greenMaterial
                nodeB!!.renderable!!.material = greenMaterial
                btnNext1?.isEnabled = true
                btnNext1?.isClickable = true
            }
            else {
                nodeA!!.renderable!!.material = originalMaterial
                nodeB!!.renderable!!.material = redMaterial

                btnNext1?.isEnabled = false
                btnNext1?.isClickable = false
            }

            displayValues()
        }
    }

    private fun getNodeSize(node: TransformableNode): Vector3 {
        val box: Box = node.renderable?.collisionShape as Box
        val renderableSize: Vector3 = box.size

        val transformableNodeScale: Vector3 = node.worldScale
        return Vector3(
            renderableSize.x * transformableNodeScale.x,
            renderableSize.y * transformableNodeScale.y,
            renderableSize.z * transformableNodeScale.z)
    }

    private fun displayValues() {
        distanceFormatted = String.format("%.2f", distanceMeters)

        scaleFormatted = if(String.format("%.2f", finalSizeB!!.x - finalSizeA!!.x) == "-0.00") "0.00"
        else if (String.format("%.2f", finalSizeB!!.x - finalSizeA!!.x) == "-0,00") "0,00"
        else String.format("%.2f", finalSizeB!!.x - finalSizeA!!.x)

        rotationFormatted = if(String.format("%.2f", nodeA!!.worldRotation.y - nodeB!!.worldRotation.y) == "-0.00") "0.00"
        else if (String.format("%.2f", nodeA!!.worldRotation.y - nodeB!!.worldRotation.y) == "-0,00") "0,00"
        else String.format("%.2f", nodeA!!.worldRotation.y - nodeB!!.worldRotation.y)

        tvDistance!!.text = "distance: $distanceFormatted meters, scale $scaleFormatted, rotation $rotationFormatted"
    }
}