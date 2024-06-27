package com.fastporte.controller.fragments.ClientFragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.fastporte.R
import com.google.ar.core.*
import com.google.ar.core.exceptions.FatalException
import com.google.ar.core.exceptions.UnavailableApkTooOldException
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException
import com.google.ar.core.exceptions.UnavailableSdkTooOldException
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.math.Vector3
import com.google.ar.sceneform.rendering.MaterialFactory
import com.google.ar.sceneform.rendering.ShapeFactory
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import kotlin.math.sqrt
import kotlin.reflect.KMutableProperty0

class MeasureFragment : Fragment() {
    private lateinit var arFragment: ArFragment
    private var session: Session? = null
    private val CAMERA_PERMISSION_CODE = 100

    private lateinit var tvDistance: TextView
    private lateinit var tvDistanceWidth: TextView
    private lateinit var tvDistanceHeight: TextView
    private lateinit var tvDistanceLength: TextView
    private lateinit var iconWidth: ImageView
    private lateinit var iconHeight: ImageView
    private lateinit var iconLength: ImageView
    private lateinit var buttonConfirm: Button
    private lateinit var numberOfBoxes: EditText

    private var firstPointWidth: AnchorNode? = null
    private var secondPointWidth: AnchorNode? = null
    private var firstPointHeight: AnchorNode? = null
    private var secondPointHeight: AnchorNode? = null
    private var firstPointLength: AnchorNode? = null
    private var secondPointLength: AnchorNode? = null

    private var measuringMode = MeasuringMode.WIDTH

    private var distanceWidth: Float = 0f
    private var distanceHeight: Float = 0f
    private var distanceLength: Float = 0f

    private enum class MeasuringMode {
        WIDTH, HEIGHT, LENGTH
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_measure, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arFragment = childFragmentManager.findFragmentById(R.id.arFragment) as ArFragment
        tvDistance = view.findViewById(R.id.distanceTextView)
        tvDistanceWidth = view.findViewById(R.id.tvDistanceBetweenTwoPoints)
        tvDistanceHeight = view.findViewById(R.id.tvDistanceHeight)
        tvDistanceLength = view.findViewById(R.id.tvDistanceLength)
        iconWidth = view.findViewById(R.id.iconWidth)
        iconHeight = view.findViewById(R.id.iconHeight)
        iconLength = view.findViewById(R.id.iconLength)
        buttonConfirm = view.findViewById(R.id.buttonConfirm)
        numberOfBoxes = view.findViewById(R.id.numberOfBoxes)

        val buttonWidth: Button = view.findViewById(R.id.buttonWidth)
        val buttonHeight: Button = view.findViewById(R.id.buttonHeight)
        val buttonLength: Button = view.findViewById(R.id.buttonLength)

        buttonWidth.setOnClickListener {
            measuringMode = MeasuringMode.WIDTH
            iconWidth.visibility = View.VISIBLE
            iconHeight.visibility = View.INVISIBLE
            iconLength.visibility = View.INVISIBLE
        }

        buttonHeight.setOnClickListener {
            measuringMode = MeasuringMode.HEIGHT
            iconWidth.visibility = View.INVISIBLE
            iconHeight.visibility = View.VISIBLE
            iconLength.visibility = View.INVISIBLE
        }

        buttonLength.setOnClickListener {
            measuringMode = MeasuringMode.LENGTH
            iconWidth.visibility = View.INVISIBLE
            iconHeight.visibility = View.INVISIBLE
            iconLength.visibility = View.VISIBLE
        }

        buttonConfirm.setOnClickListener { confirmMeasurements() }

        numberOfBoxes.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                updateConfirmButtonState()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Verificar permisos de cámara
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            checkArCoreAndSetupSession()
        }

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

    private fun handleTap(hitResult: HitResult) {
        when (measuringMode) {
            MeasuringMode.WIDTH -> handleMeasurement(hitResult, ::firstPointWidth, ::secondPointWidth, tvDistanceWidth, "Ancho", Color.RED)
            MeasuringMode.HEIGHT -> handleMeasurement(hitResult, ::firstPointHeight, ::secondPointHeight, tvDistanceHeight, "Alto", Color.GREEN)
            MeasuringMode.LENGTH -> handleMeasurement(hitResult, ::firstPointLength, ::secondPointLength, tvDistanceLength, "Largo", Color.BLUE)
        }
        updateConfirmButtonState()
    }

    private fun handleMeasurement(hitResult: HitResult, firstPointRef: KMutableProperty0<AnchorNode?>, secondPointRef: KMutableProperty0<AnchorNode?>, textView: TextView, label: String, color: Int) {
        if (firstPointRef.get() == null) {
            firstPointRef.set(createAnchorNode(hitResult, color))
            textView.visibility = View.INVISIBLE
        } else if (secondPointRef.get() == null) {
            secondPointRef.set(createAnchorNode(hitResult, color))
            calculateDistanceBetweenPoints(firstPointRef.get(), secondPointRef.get(), textView, label)
        } else {
            clearAnchors(firstPointRef, secondPointRef)
            firstPointRef.set(createAnchorNode(hitResult, color))
            textView.visibility = View.INVISIBLE
        }
    }

    private fun createAnchorNode(hitResult: HitResult, color: Int): AnchorNode {
        val anchor = hitResult.createAnchor()
        val anchorNode = AnchorNode(anchor)
        anchorNode.setParent(arFragment.arSceneView.scene)

        MaterialFactory.makeOpaqueWithColor(requireContext(), com.google.ar.sceneform.rendering.Color(color))
            .thenAccept { material ->
                val sphere = ShapeFactory.makeSphere(0.02f, Vector3.zero(), material)
                val node = TransformableNode(arFragment.transformationSystem)
                node.renderable = sphere
                node.setParent(anchorNode)
                node.select()
            }
        return anchorNode
    }

    private fun clearAnchors(vararg anchorNodes: KMutableProperty0<AnchorNode?>) {
        anchorNodes.forEach {
            it.get()?.anchor?.detach()
            it.set(null)
        }
    }

    private fun calculateDistanceBetweenPoints(first: AnchorNode?, second: AnchorNode?, textView: TextView, label: String) {
        if (first != null && second != null) {
            val distance = Vector3.subtract(first.worldPosition, second.worldPosition).length()
            requireActivity().runOnUiThread {
                textView.text = "%.2f m".format(distance)
                textView.visibility = View.VISIBLE
            }

            when (measuringMode) {
                MeasuringMode.WIDTH -> {
                    distanceWidth = distance
                }
                MeasuringMode.HEIGHT -> {
                    distanceHeight = distance
                }
                MeasuringMode.LENGTH -> {
                    distanceLength = distance
                }
            }

            updateConfirmButtonState()
        }
    }

    private fun updateConfirmButtonState() {
        val numberOfBoxesText = numberOfBoxes.text.toString()
        buttonConfirm.isEnabled = firstPointWidth != null && secondPointWidth != null &&
                firstPointHeight != null && secondPointHeight != null &&
                firstPointLength != null && secondPointLength != null &&
                numberOfBoxesText.isNotEmpty() && numberOfBoxesText.toIntOrNull() != null
    }

    private fun confirmMeasurements() {
        if (firstPointWidth != null && secondPointWidth != null && firstPointHeight != null && secondPointHeight != null && firstPointLength != null && secondPointLength != null) {
            // Obtener el contexto del activity
            val sharedPreferences = com.fastporte.helpers.SharedPreferences(requireContext())
            sharedPreferences.save("width", distanceWidth.toString())
            sharedPreferences.save("height", distanceHeight.toString())
            sharedPreferences.save("length", distanceLength.toString())
            sharedPreferences.save("quantity", numberOfBoxes.text.toString())
            sharedPreferences.save("mode", "AR")

            // Reemplazar el fragmento
            Navigation.findNavController(requireView()).navigate(R.id.action_measureFragment_to_searchResultFragment)

            // Confirmar medidas
            Toast.makeText(requireContext(), "Medidas confirmadas", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Faltan medidas. Por favor, mida ancho, alto y largo.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun calculateDistance(cameraPos: Vector3, hitPos: FloatArray): Float {
        val dx = cameraPos.x - hitPos[0]
        val dy = cameraPos.y - hitPos[1]
        val dz = cameraPos.z - hitPos[2]
        return sqrt((dx * dx + dy * dy + dz * dz).toDouble()).toFloat()
    }

    private fun checkArCoreAndSetupSession() {
        when (ArCoreApk.getInstance().checkAvailability(requireContext())) {
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
                    requireContext(),
                    "Este dispositivo no es compatible con ARCore",
                    Toast.LENGTH_LONG
                ).show()
            }

            else -> {
                Toast.makeText(
                    requireContext(),
                    "ARCore no está disponible en este dispositivo",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupArSession() {
        try {
            session = Session(requireContext())
            val config = Config(session)
            config.planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL
            config.updateMode = Config.UpdateMode.LATEST_CAMERA_IMAGE
            session?.configure(config)
            arFragment.arSceneView.setupSession(session)

            arFragment.arSceneView.scene.addOnUpdateListener { frameTime ->
                val frame = arFragment.arSceneView.arFrame
                val updatedPlanes = frame?.getUpdatedTrackables(Plane::class.java)

                updatedPlanes?.forEach { plane ->
//                    if (plane.trackingState == TrackingState.TRACKING) {
//                        when (plane.type) {
//                            Plane.Type.VERTICAL -> Log.d(
//                                "ARCore",
//                                "Plano vertical detectado durante la actualización"
//                            )
//
//                            else -> Log.d(
//                                "ARCore",
//                                "Tipo de plano desconocido durante la actualización"
//                            )
//                        }
//                    }
                }
            }

        } catch (e: UnavailableArcoreNotInstalledException) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "ARCore no está instalado. Instalando ahora...",
                Toast.LENGTH_LONG
            ).show()
            requestInstallArCore()
        } catch (e: UnavailableDeviceNotCompatibleException) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Este dispositivo no es compatible con ARCore",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: UnavailableSdkTooOldException) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "ARCore SDK es demasiado antiguo, actualiza la aplicación",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: UnavailableApkTooOldException) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "La APK de ARCore es demasiado antigua, actualiza ARCore",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: FatalException) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Error fatal al inicializar ARCore: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                requireContext(),
                "Ocurrió un error inicializando ARCore: ${e.localizedMessage}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun requestInstallArCore() {
        // ARCore SDK no está disponible, solicitar instalación
        val intent =
            Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.google.ar.core"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permiso concedido, configurar sesión AR
                checkArCoreAndSetupSession()
            } else {
                // Permiso denegado, mostrar mensaje al usuario
                Toast.makeText(
                    requireContext(),
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

        Log.d("ARCore", "ARCore session resumed")

        // Mostrar las distancias guardadas y ajustar visibilidad
        updateTextViews()
        updateConfirmButtonState()
    }

    override fun onPause() {
        super.onPause()
        arFragment.arSceneView.pause()
        session?.pause()

        Log.d("ARCore", "ARCore session paused")
    }

    override fun onDestroy() {
        super.onDestroy()
        arFragment.arSceneView.destroy()
        session?.close()
        session = null

        Log.d("ARCore", "ARCore session destroyed")
    }

    private fun updateTextViews() {
        if (distanceWidth > 0) {
            tvDistanceWidth.text = "%.2f m".format(distanceWidth)
            tvDistanceWidth.visibility = View.VISIBLE
        } else {
            tvDistanceWidth.visibility = View.INVISIBLE
        }

        if (distanceHeight > 0) {
            tvDistanceHeight.text = "%.2f m".format(distanceHeight)
            tvDistanceHeight.visibility = View.VISIBLE
        } else {
            tvDistanceHeight.visibility = View.INVISIBLE
        }

        if (distanceLength > 0) {
            tvDistanceLength.text = "%.2f m".format(distanceLength)
            tvDistanceLength.visibility = View.VISIBLE
        } else {
            tvDistanceLength.visibility = View.INVISIBLE
        }
    }
}