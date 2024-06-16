package com.fastporte.controller.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.fastporte.R
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.FatalException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Quaternion
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlin.math.pow
import kotlin.math.sqrt

class MeasureActivity : AppCompatActivity() {
    private lateinit var arFragment: ArFragment
    private var session: Session? = null
    private val CAMERA_PERMISSION_CODE = 100

    private lateinit var tvDistance: TextView
    private lateinit var tvDistanceTwoPoints: TextView

    private var firstPoint: AnchorNode? = null
    private var secondPoint: AnchorNode? = null
    private var lineNode: AnchorNode? = null

    private var firstPointD: HitResult? = null
    private var secondPointD: HitResult? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_measure)

        arFragment = supportFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        tvDistance = findViewById(R.id.distanceTextView)
        tvDistanceTwoPoints = findViewById(R.id.tvDistanceBetweenTwoPoints)

        //val resetButton: Button = findViewById(R.id.resetButton)

        // Verificar permisos de cámara
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            checkArCoreAndSetupSession()
        }

        /*resetButton.setOnClickListener {
            resetArSession()
        }*/

        arFragment.setOnTapArPlaneListener { hitResult, plane, motionEvent ->

            // Obtener la posición de la cámara
            val cameraPos = arFragment.arSceneView.scene.camera.worldPosition

            // Obtener la posición del punto tocado en el plano
            val hitPos = hitResult.hitPose.translation

            // Calcular la distancia Euclidiana entre la cámara y el punto tocado
            val distance = calculateDistance(cameraPos, hitPos)

            // Formatear la distancia a 3 decimales
            val txtDistance = String.format("%.3f", distance)

            tvDistance.text = "Distancia: \n$txtDistance metros"

            handleTap(hitResult)
        }
    }

    private fun calculateDistance(cameraPos: Vector3, hitPos: FloatArray): Float {
        val dx = cameraPos.x - hitPos[0]
        val dy = cameraPos.y - hitPos[1]
        val dz = cameraPos.z - hitPos[2]
        return Math.sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    private fun handleTap(hitResult: HitResult) {
        if (firstPoint == null) {
            firstPoint = createAnchorNode(hitResult)
            Toast.makeText(this, "Primer punto seleccionado", Toast.LENGTH_SHORT).show()
        } else if (secondPoint == null) {
            secondPoint = createAnchorNode(hitResult)
            Toast.makeText(this, "Segundo punto seleccionado", Toast.LENGTH_SHORT).show()
            //drawLineBetweenPoints()
            calculateDistanceBetweenPoints()
        } else {
            clearAnchors()
            firstPoint = createAnchorNode(hitResult)
            Toast.makeText(this, "Primer punto seleccionado", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createAnchorNode(hitResult: HitResult): AnchorNode {
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)

        MaterialFactory.makeOpaqueWithColor(this,
            com.google.ar.sceneform.rendering.Color(Color.RED)
        )
            .thenAccept { material ->
                val sphere = ShapeFactory.makeSphere(0.02f, Vector3.zero(), material)
                val node = TransformableNode(arFragment.transformationSystem)
                node.renderable = sphere
                node.setParent(anchorNode)
                node.select()
            }
        return anchorNode
    }

    private fun drawLineBetweenPoints() {
        if (firstPoint != null && secondPoint != null) {
            val start = firstPoint!!.worldPosition
            val end = secondPoint!!.worldPosition
            val difference = Vector3.subtract(start, end)
            val directionFromTopToBottom = difference.normalized()
            val rotationFromAToB =
                Quaternion.lookRotation(directionFromTopToBottom, Vector3.up())

            MaterialFactory.makeOpaqueWithColor(this,
                com.google.ar.sceneform.rendering.Color(Color.BLUE)
            )
                .thenAccept { material ->
                    val lineModel = ShapeFactory.makeCylinder(
                        0.01f,
                        difference.length(),
                        Vector3.zero(),
                        material
                    )
                    val node = AnchorNode()
                    node.worldPosition = Vector3.add(start, end).scaled(0.5f)
                    node.worldRotation = rotationFromAToB
                    node.renderable = lineModel
                    arFragment.arSceneView.scene.addChild(node)
                    lineNode = node
                }
        }
    }

    private fun clearAnchors() {
        firstPoint?.anchor?.detach()
        secondPoint?.anchor?.detach()
        lineNode?.let { arFragment.arSceneView.scene.removeChild(it) }

        firstPoint = null
        secondPoint = null
        lineNode = null
    }

    private fun calculateDistanceBetweenPoints() {
        val first = firstPoint
        val second = secondPoint

        if (first != null && second != null) {
            val distance = sqrt(
                ((second.worldPosition.x - first.worldPosition.x).pow(2) +
                        (second.worldPosition.y - first.worldPosition.y).pow(2) +
                        (second.worldPosition.z - first.worldPosition.z).pow(2)).toDouble()
            )

            runOnUiThread {
                tvDistanceTwoPoints.text =
                    "Distancia entre puntos: \n%.2f metros".format(distance)
            }
        }
    }

    private fun checkArCoreAndSetupSession() {
        when (ArCoreApk.getInstance().checkAvailability(this)) {
            ArCoreApk.Availability.SUPPORTED_INSTALLED -> {
                setupArSession()
                arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
                    val frame = arFragment.arSceneView.arFrame
                    val updatedPlanes = frame?.getUpdatedTrackables(Plane::class.java)
                }
            }

            ArCoreApk.Availability.SUPPORTED_APK_TOO_OLD,
            ArCoreApk.Availability.SUPPORTED_NOT_INSTALLED -> {
                requestInstallArCore()
            }

            ArCoreApk.Availability.UNSUPPORTED_DEVICE_NOT_CAPABLE -> {
                Toast.makeText(
                    this,
                    "Este dispositivo no es compatible con ARCore",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    this,
                    "ARCore no está disponible en este dispositivo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupArSession() {
        try {
            session = Session(this)
            val config = Config(session)
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session?.configure(config)
            arFragment.arSceneView.setupSession(session)

            arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
                val frame = arFragment.arSceneView.arFrame
                val updatedPlanes = frame?.getUpdatedTrackables(Plane::class.java)

                updatedPlanes?.forEach { plane ->
                    if (plane.trackingState == TrackingState.TRACKING) {
                        when (plane.type) {
                            Plane.Type.VERTICAL -> Log.d(
                                "ARCore",
                                "Plano vertical detectado durante la actualización"
                            )

                            else -> Log.d(
                                "ARCore",
                                "Tipo de plano desconocido durante la actualización"
                            )
                        }
                    }
                }
            }

        } catch (e: UnavailableArcoreNotInstalledException) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "ARCore no está instalado. Instalando ahora...",
                Toast.LENGTH_LONG
            ).show()
            requestInstallArCore()
        } catch (e: UnavailableDeviceNotCompatibleException) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Este dispositivo no es compatible con ARCore",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: UnavailableSdkTooOldException) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "ARCore SDK es demasiado antiguo, actualiza la aplicación",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: UnavailableApkTooOldException) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "La APK de ARCore es demasiado antigua, actualiza ARCore",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: FatalException) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Error fatal al inicializar ARCore: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this,
                "Ocurrió un error inicializando ARCore: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun resetArSession() {
        arFragment.arSceneView.pause()
        session?.close()
        setupArSession()
        arFragment.arSceneView.resume()
        Toast.makeText(this, "Detección de planos reiniciada", Toast.LENGTH_SHORT).show()
    }

    private fun requestInstallArCore() {
        // ARCore SDK no está disponible, solicitar instalación
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.ar.core"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permiso concedido, configurar sesión AR
                checkArCoreAndSetupSession()
            } else {
                // Permiso denegado, mostrar mensaje al usuario
                Toast.makeText(
                    this,
                    "Permiso de cámara denegado. La aplicación no puede funcionar sin este permiso.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        try {
            session?.resume()
            arFragment.arSceneView.resume()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onPause() {
        super.onPause()
        arFragment.arSceneView.pause()
        session?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        arFragment.arSceneView.destroy()
        session?.close()
    }
}