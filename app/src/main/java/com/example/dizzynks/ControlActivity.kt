package com.example.dizzynks

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.SystemClock
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
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


class ControlActivity : AppCompatActivity(), Scene.OnUpdateListener {
    private var arFragment: ArFragment? = null
    private var tvDistance: TextView? = null
    private var btnNext1: Button? = null
    private var btnRestart: Button? = null
    private var tvDescription: TextView? = null

    private var btnLeft: ImageButton? = null
    private var btnRight: ImageButton? = null
    private var btnUp: ImageButton? = null
    private var btnDown: ImageButton? = null
    private var btnRotateLeft: ImageButton? = null
    private var btnRotateRight: ImageButton? = null
    private var btnZoomIn: ImageButton? = null
    private var btnZoomOut: ImageButton? = null

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

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_control)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        tvDistance = findViewById(R.id.tv_distance)
        tvDescription = findViewById(R.id.tv_description)
        chronometer = findViewById(R.id.chronometer)

        btnLeft = findViewById(R.id.btn_left)
        btnRight = findViewById(R.id.btn_right)
        btnUp = findViewById(R.id.btn_up)
        btnDown = findViewById(R.id.btn_down)
        btnRotateRight = findViewById(R.id.btn_rotate_right)
        btnRotateLeft = findViewById(R.id.btn_rotate_left)
        btnZoomIn = findViewById(R.id.btn_zoom_in)
        btnZoomOut = findViewById(R.id.btn_zoom_out)

        btnNext1 = findViewById(R.id.btn_next1)
        btnRestart = findViewById(R.id.btn_restart)
        btnNext1?.isEnabled = false
        btnNext1?.isClickable = false
        btnRestart?.isEnabled = false
        btnRestart?.isClickable = false

        initModel()

        btnLeft!!.setOnClickListener {
            nodeB?.let { it1 -> modelMovement(it1, "left_move") }
        }
        btnLeft!!.setOnTouchListener(object : View.OnTouchListener {
            private var mHandler: Handler? = null
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler != null) return true
                        mHandler = Handler()
                        mHandler!!.postDelayed(mAction, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mHandler == null) return true
                        mHandler!!.removeCallbacks(mAction)
                        mHandler = null
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    nodeB?.let { it1 -> modelMovement(it1, "left_move") }
                    mHandler!!.postDelayed(this, 100)
                }
            }
        })

        btnRight!!.setOnClickListener {
            nodeB?.let { it1 -> modelMovement(it1, "right_move") }
        }
        btnRight!!.setOnTouchListener(object : View.OnTouchListener {
            private var mHandler: Handler? = null
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler != null) return true
                        mHandler = Handler()
                        mHandler!!.postDelayed(mAction, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mHandler == null) return true
                        mHandler!!.removeCallbacks(mAction)
                        mHandler = null
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    nodeB?.let { it1 -> modelMovement(it1, "right_move") }
                    mHandler!!.postDelayed(this, 100)
                }
            }
        })

        btnUp!!.setOnClickListener {
            nodeB?.let { it1 -> modelMovement(it1, "up") }
        }
        btnUp!!.setOnTouchListener(object : View.OnTouchListener {
            private var mHandler: Handler? = null
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler != null) return true
                        mHandler = Handler()
                        mHandler!!.postDelayed(mAction, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mHandler == null) return true
                        mHandler!!.removeCallbacks(mAction)
                        mHandler = null
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    nodeB?.let { it1 -> modelMovement(it1, "up") }
                    mHandler!!.postDelayed(this, 100)
                }
            }
        })
        btnDown!!.setOnClickListener {
            nodeB?.let { it1 -> modelMovement(it1, "down") }
        }
        btnDown!!.setOnTouchListener(object : View.OnTouchListener {
            private var mHandler: Handler? = null
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler != null) return true
                        mHandler = Handler()
                        mHandler!!.postDelayed(mAction, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mHandler == null) return true
                        mHandler!!.removeCallbacks(mAction)
                        mHandler = null
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    nodeB?.let { it1 -> modelMovement(it1, "down") }
                    mHandler!!.postDelayed(this, 100)
                }
            }
        })
        btnZoomIn!!.setOnClickListener {
            nodeB?.let { it1 -> modelMovement(it1, "zoom_in") }
        }
        btnZoomIn!!.setOnTouchListener(object : View.OnTouchListener {
            private var mHandler: Handler? = null
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler != null) return true
                        mHandler = Handler()
                        mHandler!!.postDelayed(mAction, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mHandler == null) return true
                        mHandler!!.removeCallbacks(mAction)
                        mHandler = null
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    nodeB?.let { it1 -> modelMovement(it1, "zoom_in") }
                    mHandler!!.postDelayed(this, 100)
                }
            }
        })
        btnZoomOut!!.setOnClickListener {
            nodeB?.let { it1 -> modelMovement(it1, "zoom_out") }
        }
        btnZoomOut!!.setOnTouchListener(object : View.OnTouchListener {
            private var mHandler: Handler? = null
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler != null) return true
                        mHandler = Handler()
                        mHandler!!.postDelayed(mAction, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mHandler == null) return true
                        mHandler!!.removeCallbacks(mAction)
                        mHandler = null
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    nodeB?.let { it1 -> modelMovement(it1, "zoom_out") }
                    mHandler!!.postDelayed(this, 100)
                }
            }
        })
        btnRotateLeft!!.setOnClickListener {
            nodeB?.let { it1 -> modelMovement(it1, "rotate_left") }
        }
        btnRotateLeft!!.setOnTouchListener(object : View.OnTouchListener {
            private var mHandler: Handler? = null
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler != null) return true
                        mHandler = Handler()
                        mHandler!!.postDelayed(mAction, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mHandler == null) return true
                        mHandler!!.removeCallbacks(mAction)
                        mHandler = null
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    nodeB?.let { it1 -> modelMovement(it1, "rotate_left") }
                    mHandler!!.postDelayed(this, 100)
                }
            }
        })
        btnRotateRight!!.setOnClickListener {
            nodeB?.let { it1 -> modelMovement(it1, "rotate_right") }
        }
        btnRotateRight!!.setOnTouchListener(object : View.OnTouchListener {
            private var mHandler: Handler? = null
            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        if (mHandler != null) return true
                        mHandler = Handler()
                        mHandler!!.postDelayed(mAction, 100)
                    }
                    MotionEvent.ACTION_UP -> {
                        if (mHandler == null) return true
                        mHandler!!.removeCallbacks(mAction)
                        mHandler = null
                    }
                }
                return false
            }

            var mAction: Runnable = object : Runnable {
                override fun run() {
                    nodeB?.let { it1 -> modelMovement(it1, "rotate_right") }
                    mHandler!!.postDelayed(this, 100)
                }
            }
        })

        arFragment!!.setOnTapArPlaneListener { hitResult, plane, motionEvent ->
            if (shapeRenderableA != null && shapeRenderableB !== null && nodeA == null && nodeB == null) {

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
                node2.scaleController.maxScale = 10.0f

                nodeB = node2

                node.setParent(anchorNode)
                node2.setParent(anchorNode2)

                node2.translationController.isEnabled = false
                node2.rotationController.isEnabled = false
                node2.scaleController.isEnabled = false

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
           sec = (tMiliSec /1000).toInt()
           min = sec /60
           sec %= 60
           miliSec = (tMiliSec %1000).toInt()
           chronometer?.text = String.format("%02d",min) + ":" + String.format("%02d", sec) + ":" + String.format("%03d", miliSec)
           handler.postDelayed(this, 0)
        }
    }

    private fun clearAnchors() {
        btnRestart?.isEnabled = false
        btnRestart?.isClickable = false

        arFragment!!.arSceneView.scene.removeChild(nodeA!!.parent!!)
        arFragment!!.arSceneView.scene.removeChild(nodeB!!.parent!!)

        nodeA = null
        nodeB = null
    }


    private fun initModel() {
        sizeNodeA = if(Options.size == "Large") 0.3f else if (Options.size == "Medium") 0.15f else 0.08f
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

        arFragment!!.arSceneView.scene.removeChild(nodeA!!.parent!!)
        arFragment!!.arSceneView.scene.removeChild(nodeB!!.parent!!)

        nodeA = null
        nodeB = null

        btnNext1?.isEnabled = false
        btnNext1?.isClickable = false

        Log.i("time", chronometer?.text.toString())
        Log.i("miliseconds", tMiliSec.toString())
        // @TODO Save level and miliseconds to logs
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
           finish()
        }

        else {
            initModel()
        }
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

            val isNodesDistanceEqual = ("%.2f").format(distanceMeters) == "0.00"
            val isNodesSizeEqual = ("%.2f").format(finalSizeB!!.x) == ("%.2f").format(finalSizeA!!.x) || scaleFormatted == "0.00"
            val isNodesRotationEqual = ("%.2f").format(nodeA!!.worldRotation.y) == ("%.2f").format(nodeB!!.worldRotation.y) || rotationFormatted == "0.00"

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
                Log.i("distance", distanceMeters.toString())
                Log.i("distance bool", isNodesDistanceEqual.toString())
                Log.i("rotation", isNodesRotationEqual.toString())
                Log.i("distance", isNodesSizeEqual.toString())
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
        val distanceFormatted = String.format("%.2f", distanceMeters)

        val scaleFormatted = if(String.format("%.2f", finalSizeB!!.x - finalSizeA!!.x) == "-0.00") "0.00"
        else String.format("%.2f", finalSizeB!!.x - finalSizeA!!.x)

        val rotationFormatted = if(String.format("%.2f", nodeA!!.worldRotation.y - nodeB!!.worldRotation.y) == "-0.00") "0.00"
        else String.format("%.2f", nodeA!!.worldRotation.y - nodeB!!.worldRotation.y)

        tvDistance!!.text = "distance: $distanceFormatted meters, scale $scaleFormatted, rotation $rotationFormatted"
    }

    private fun modelMovement(node: TransformableNode, moveMent: String) {
        var currentPosition = Vector3()
        val move = Vector3()
        try {
            currentPosition = Objects.requireNonNull(node.localPosition)
            if (moveMent == "zoom_out") {
                if(node.localScale.x >= 0.1F) {
                    val vector3 = Vector3(node.localScale.x - 0.1f, node.localScale.y - 0.1f, node.localScale.z - 0.1f)
                    node.localScale = vector3
                }
            }
            if (moveMent == "zoom_in") {
                if(node.localScale.x <= 3.0F) {
                    val vector3 = Vector3(node.localScale.x + 0.1f, node.localScale.y + 0.1f, node.localScale.z + 0.1f)
                    node.localScale = vector3
                }
            }
            if (moveMent == "right_move") {
                move[currentPosition.x + 0.01f, currentPosition.y] = currentPosition.z
                node.localPosition = Vector3(node.localPosition.x + 0.005f, node.localPosition.y, node.localPosition.z)
            }
            if (moveMent == "left_move") {
                move[currentPosition.x - 0.01f, currentPosition.y] = currentPosition.z
                node.localPosition = Vector3(node.localPosition.x - 0.005f, node.localPosition.y, node.localPosition.z)
            }
            if (moveMent == "rotate_left") {
                val q1: Quaternion = node.localRotation
                val q2 =
                    Quaternion.axisAngle(Vector3(0f, 1f, 0f), 1.0f)
                node.localRotation = Quaternion.multiply(q1, q2)
            }
            if (moveMent == "rotate_right") {
                val q1: Quaternion = node.localRotation
                val q2 =
                    Quaternion.axisAngle(Vector3(0F, 1f, 0f), -1.0f)
                node.localRotation = Quaternion.multiply(q1, q2)
            }
            if (moveMent == "down") {
                move[currentPosition.x, currentPosition.y] = currentPosition.z + 0.005f
                node.localPosition = move
            }
            if (moveMent == "up") {
                move[currentPosition.x, currentPosition.y] = currentPosition.z - 0.005f
                node.localPosition = move
            }
//            node.localPosition = move
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}