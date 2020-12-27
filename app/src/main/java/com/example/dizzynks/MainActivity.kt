package com.example.dizzynks

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.Node
import com.google.ar.sceneform.Scene
import com.google.ar.sceneform.collision.Box
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.*
import com.google.ar.sceneform.ux.*
import java.util.*


class MainActivity : AppCompatActivity(), Scene.OnUpdateListener {
    private var arFragment: ArFragment? = null
    private var tvDistance: TextView? = null
    private var btnNext1: Button? = null

    private var cubeRenderableA: ModelRenderable? = null
    private var cubeRenderableB: ModelRenderable? = null

    private var nodeA: TransformableNode? = null
    private var nodeB: TransformableNode? = null

    var greenMaterial: Material? = null
    var originalMaterial: Material? = null

    private var finalSizeA: Vector3? = null
    private var finalSizeB: Vector3? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!checkIsSupportedDeviceOrFinish(this)) {
            Toast.makeText(applicationContext, "Device not supported", Toast.LENGTH_LONG).show()
        }

        setContentView(R.layout.activity_main)

        arFragment = supportFragmentManager.findFragmentById(R.id.ux_fragment) as ArFragment?
        tvDistance = findViewById(R.id.tvDistance)
        btnNext1 = findViewById(R.id.btn_next1)
        btnNext1?.isEnabled = false
        btnNext1?.isClickable = false

        initModel()

        arFragment!!.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            if (cubeRenderableA != null && cubeRenderableB !== null) {

                val anchor = hitResult.createAnchor()
                val anchorNode = AnchorNode(anchor)
                anchorNode.setParent(arFragment!!.arSceneView.scene)

                if (nodeA != null && nodeB != null) {
                    clearAnchors()
                }

                val node = TransformableNode(arFragment!!.transformationSystem)

                arFragment!!.arSceneView.scene.addChild(anchorNode)
                node.select()

                if (nodeA == null) {
                    nodeA = node
                    node.renderable = cubeRenderableA
                    node.scaleController.isEnabled = false
                    arFragment!!.arSceneView.scene.addOnUpdateListener(this)
                } else if (nodeB == null) {
                    node.renderable = cubeRenderableB
                    node.scaleController.minScale = 0.05f
                    nodeB = node
                }

                node.setParent(anchorNode)
            }
        }

        btnNext1?.setOnClickListener {
            val intent = Intent(this, ControlActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initModel() {
        MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.GREEN))
            .thenAccept { material ->
                greenMaterial = material
            }

        MaterialFactory.makeOpaqueWithColor(this, Color(android.graphics.Color.RED))
            .thenAccept { material ->
                val vector3a = Vector3(0.05f, 0.05f, 0.05f)
                val vector3b = Vector3(0.07f, 0.07f, 0.07f)
                cubeRenderableA = ShapeFactory.makeCube(vector3a, Vector3.zero(), material)
                cubeRenderableB = ShapeFactory.makeCube(vector3b, Vector3.zero(), material)
                originalMaterial = material

                cubeRenderableA!!.isShadowCaster = false
                cubeRenderableA!!.isShadowReceiver = false

                cubeRenderableB!!.isShadowCaster = false
                cubeRenderableB!!.isShadowReceiver = false
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

    private fun clearAnchors() {

        arFragment!!.arSceneView.scene.removeChild(nodeA!!.parent!!)
        arFragment!!.arSceneView.scene.removeChild(nodeB!!.parent!!)

        nodeA = null
        nodeB = null
    }

    override fun onUpdate(frameTime: FrameTime) {

        if (nodeA != null && nodeB != null) {

            val box: Box = nodeB!!.renderable?.collisionShape as Box
            val renderableSize: Vector3 = box.size

            val transformableNodeScale: Vector3 = nodeB!!.worldScale
            finalSizeB = Vector3(
                    renderableSize.x * transformableNodeScale.x,
                    renderableSize.y * transformableNodeScale.y,
                    renderableSize.z * transformableNodeScale.z)

            val boxA: Box = nodeA!!.renderable?.collisionShape as Box
            val renderableSizeA: Vector3 = boxA.size
            val transformableNodeScaleA: Vector3 = nodeA!!.worldScale
            finalSizeA = Vector3(
                    renderableSizeA.x * transformableNodeScaleA.x,
                    renderableSizeA.y * transformableNodeScaleA.y,
                    renderableSizeA.z * transformableNodeScaleA.z)


            var node = arFragment!!.arSceneView.scene.overlapTest(nodeA)

            val positionA = nodeA!!.worldPosition
            val positionB = nodeB!!.worldPosition

            val dx = positionA.x - positionB.x
            val dy = positionA.y - positionB.y
            val dz = positionA.z - positionB.z


            //Computing a straight-line distance.
            val distanceMeters = kotlin.math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()

            val distanceFormatted = String.format("%.2f", distanceMeters)

            if (node != null) {
                if (("%.2f").format(distanceMeters) == "0.00" && ("%.3f").format(finalSizeB!!.x) == ("%.3f").format(finalSizeA!!.x)) {
                    nodeA!!.renderable!!.material = greenMaterial
                    nodeB!!.renderable!!.material = greenMaterial
                    btnNext1?.isEnabled = true
                    btnNext1?.isClickable = true
                }
                else {
                    nodeA!!.renderable!!.material = originalMaterial
                    nodeB!!.renderable!!.material = originalMaterial
                }
            }

            tvDistance!!.text = "Distance between nodes: $distanceFormatted metres"
        }
    }


    companion object {
        private val MIN_OPENGL_VERSION = 3.0
    }
}

