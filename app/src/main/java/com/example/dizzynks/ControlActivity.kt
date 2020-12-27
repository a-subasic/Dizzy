package com.example.dizzynks

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import java.util.*


class ControlActivity : AppCompatActivity(), Scene.OnUpdateListener {
    private var arFragment: ArFragment? = null
    private var tvDistance: TextView? = null
    private var btnLeft: Button? = null
    private var btnRight: Button? = null
    private var btnUp: Button? = null
    private var btnDown: Button? = null
    private var btnRotateLeft: Button? = null
    private var btnRotateRight: Button? = null

    private var cubeRenderable: ModelRenderable? = null

    private var nodeA: TransformableNode? = null
    private var nodeB: TransformableNode? = null

    var greenMaterial: Material? = null
    var originalMaterial: Material? = null

    var overlapIdle = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_control)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        tvDistance = findViewById(R.id.tvDistance)
        btnLeft = findViewById(R.id.btn_left)
        btnRight = findViewById(R.id.btn_right)
        btnUp = findViewById(R.id.btn_up)
        btnDown = findViewById(R.id.btn_down)
        btnRotateRight = findViewById(R.id.btn_rotate_right)
        btnRotateLeft = findViewById(R.id.btn_rotate_left)

        initModel()

        btnLeft!!.setOnClickListener {
//            nodeA?.localPosition?.x = nodeA?.localPosition?.x?.plus(1f)
            nodeA?.let { it1 -> modelMovement(it1, "left_move") }
        }
        btnRight!!.setOnClickListener {
            nodeA?.let { it1 -> modelMovement(it1, "right_move") }
        }
        btnUp!!.setOnClickListener {
            nodeA?.let { it1 -> modelMovement(it1, "zoom_in") }
        }
        btnDown!!.setOnClickListener {
            nodeA?.let { it1 -> modelMovement(it1, "zoom_out") }
        }
        btnRotateLeft!!.setOnClickListener {
            nodeA?.let { it1 -> modelMovement(it1, "rotate_left") }
        }
        btnRotateRight!!.setOnClickListener {
            nodeA?.let { it1 -> modelMovement(it1, "rotate_right") }
        }

        arFragment!!.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            if (cubeRenderable != null) {

                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment!!.arSceneView.scene)

                if (nodeA != null && nodeB != null) {
                    clearAnchors()
                }

                val node = TransformableNode(arFragment!!.transformationSystem)
                node.renderable = cubeRenderable
                node.setParent(anchorNode)

                arFragment!!.arSceneView.scene.addChild(anchorNode)
                node.select()

                if (nodeA == null) {
                    nodeA = node
                    arFragment!!.arSceneView.scene.addOnUpdateListener(this)
                } else if (nodeB == null) {
                    nodeB = node
                }
            }
        }
    }


    private fun initModel() {

        MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.GREEN))
            .thenAccept { material ->
                greenMaterial = material
            }

        MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.RED))
            .thenAccept { material ->
                val vector3 = Vector3(0.05f, 0.05f, 0.05f)
                cubeRenderable = ShapeFactory.makeCube(vector3, Vector3.zero(), material)
                originalMaterial = material

                cubeRenderable!!.isShadowCaster = false
                cubeRenderable!!.isShadowReceiver = false

            }
    }

    private fun clearAnchors() {

        arFragment!!.arSceneView.scene.removeChild(nodeA!!.parent!!)
        arFragment!!.arSceneView.scene.removeChild(nodeB!!.parent!!)

        nodeA = null
        nodeB = null
    }

    override fun onUpdate(frameTime: FrameTime) {

        if (nodeA != null && nodeB != null) {

            var node = arFragment!!.arSceneView.scene.overlapTest(nodeA)

            if (node != null) {

                if (overlapIdle) {
                    overlapIdle = false
                    nodeA!!.renderable!!.material = greenMaterial
                }

            } else {

                if (!overlapIdle) {
                    overlapIdle = true
                    nodeA!!.renderable!!.material = originalMaterial
                }
            }

            val positionA = nodeA!!.worldPosition
            val positionB = nodeB!!.worldPosition

            val dx = positionA.x - positionB.x
            val dy = positionA.y - positionB.y
            val dz = positionA.z - positionB.z


            //Computing a straight-line distance.
            val distanceMeters = kotlin.math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()

            val distanceFormatted = String.format("%.2f", distanceMeters)

            tvDistance!!.text = "Distance between nodes: $distanceFormatted metres"
        }
    }

    private fun modelMovement(node: TransformableNode, moveMent: String) {
        var currentPosition = Vector3()
        val move = Vector3()
        try {
            currentPosition = Objects.requireNonNull(node.localPosition)
            if (moveMent == "zoom_out") {
                move[currentPosition.x, (currentPosition.y - 0.1).toFloat()] = currentPosition.z
                node.localPosition = move
            }
            if (moveMent == "zoom_in") {
                move[currentPosition.x, (currentPosition.y + 0.1).toFloat()] = currentPosition.z
                node.localPosition = move
            }
            if (moveMent == "right_move") {
                move[(currentPosition.x + 0.1).toFloat(), currentPosition.y] = currentPosition.z
                node.localPosition = move
            }
            if (moveMent == "left_move") {
                move[(currentPosition.x - 0.1).toFloat(), currentPosition.y] = currentPosition.z
                node.localPosition = move
            }
            if (moveMent == "rotate_left") {
                val q1: Quaternion = node.localRotation
                val q2 =
                    Quaternion.axisAngle(Vector3(0F, 1f, 0f), 2.0f)
                node.localRotation = Quaternion.multiply(q1, q2)
            }
            if (moveMent == "rotate_right") {
                val q1: Quaternion = node.localRotation
                val q2 =
                    Quaternion.axisAngle(Vector3(0F, 1f, 0f), -2.0f)
                node.localRotation = Quaternion.multiply(q1, q2)
            }
            if (moveMent == "down") {
                move[currentPosition.x, currentPosition.y] = (currentPosition.z + 0.1).toFloat()
                node.localPosition = move
            }
            if (moveMent == "up") {
                move[currentPosition.x, currentPosition.y] = (currentPosition.z - 0.1).toFloat()
                node.localPosition = move
            }
//            node.localPosition = move
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}