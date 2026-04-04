package org.example.project.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import platform.AVFoundation.*
import platform.CoreMedia.kCMTimeInvalid
import platform.CoreGraphics.CGRect
import platform.Foundation.NSDate
import platform.Foundation.NSError
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSURL
import platform.Foundation.timeIntervalSince1970
import platform.QuartzCore.CATransaction
import platform.UIKit.UIView
import platform.darwin.DISPATCH_QUEUE_PRIORITY_DEFAULT
import platform.darwin.NSObject
import platform.darwin.dispatch_async
import platform.darwin.dispatch_get_global_queue

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun NativeCameraView(
    modifier: Modifier,
    isRecording: Boolean,
    onVideoRecorded: (String) -> Unit
) {
    val cameraManager = remember { IOSCameraManager(onVideoRecorded) }

    DisposableEffect(Unit) {
        cameraManager.startSession()
        onDispose {
            cameraManager.stopSession()
        }
    }

    LaunchedEffect(isRecording) {
        if (isRecording) {
            cameraManager.startRecording()
        } else {
            cameraManager.stopRecording()
        }
    }

    @Suppress("DEPRECATION")
    UIKitView(
        factory = {
            // A custom UIView subclass that automatically resizes its preview layer
            val view = CameraPreviewView(cameraManager.previewLayer)
            view.clipsToBounds = true
            view
        },
        modifier = modifier,
        update = { view ->
            // Trigger layout if bounds change
            view.setNeedsLayout()
            view.layoutIfNeeded()
        }
    )
}

@OptIn(ExperimentalForeignApi::class)
private class CameraPreviewView(val previewLayer: AVCaptureVideoPreviewLayer) : UIView(frame = platform.CoreGraphics.CGRectZero.readValue()) {
    init {
        layer.addSublayer(previewLayer)
    }

    override fun layoutSubviews() {
        super.layoutSubviews()
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        previewLayer.frame = bounds
        CATransaction.commit()
    }
}

@OptIn(ExperimentalForeignApi::class)
private class IOSCameraManager(
    private val onVideoRecorded: (String) -> Unit
) : NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {

    private val captureSession = AVCaptureSession()
    val previewLayer = AVCaptureVideoPreviewLayer(session = captureSession)
    private val movieOutput = AVCaptureMovieFileOutput()

    init {
        previewLayer.videoGravity = AVLayerVideoGravityResizeAspectFill
        setupSession()
    }

    private fun setupSession() {
        captureSession.beginConfiguration()
        
        // Video Input
        var videoDevice = AVCaptureDevice.defaultDeviceWithDeviceType(
            AVCaptureDeviceTypeBuiltInWideAngleCamera,
            AVMediaTypeVideo,
            AVCaptureDevicePositionFront
        )
        
        // Fallback for older devices if the specific type fails
        if (videoDevice == null) {
            val devices = AVCaptureDeviceDiscoverySession.discoverySessionWithDeviceTypes(
                listOf(AVCaptureDeviceTypeBuiltInWideAngleCamera),
                AVMediaTypeVideo,
                AVCaptureDevicePositionFront
            ).devices
            videoDevice = devices.firstOrNull() as? AVCaptureDevice
        }

        if (videoDevice != null) {
            val videoInput = AVCaptureDeviceInput.deviceInputWithDevice(videoDevice, null)
            if (videoInput != null && captureSession.canAddInput(videoInput)) {
                captureSession.addInput(videoInput)
            } else {
                println("CameraK Error: Could not add video input.")
            }
        } else {
            println("CameraK Error: No front camera found on this device.")
        }

        // Audio Input
        val audioDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio)
        if (audioDevice != null) {
            val audioInput = AVCaptureDeviceInput.deviceInputWithDevice(audioDevice, null)
            if (audioInput != null && captureSession.canAddInput(audioInput)) {
                captureSession.addInput(audioInput)
            }
        }

        // Output
        if (captureSession.canAddOutput(movieOutput)) {
            captureSession.addOutput(movieOutput)
        }

        captureSession.commitConfiguration()
    }

    fun startSession() {
        if (!captureSession.isRunning()) {
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
                captureSession.startRunning()
            }
        }
    }

    fun stopSession() {
        if (captureSession.isRunning()) {
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
                captureSession.stopRunning()
            }
        }
    }

    fun startRecording() {
        if (movieOutput.isRecording()) return
        
        // The magic fix for the iOS audio drop bug!
        movieOutput.movieFragmentInterval = kCMTimeInvalid.readValue()

        val timestamp = NSDate().timeIntervalSince1970.toLong()
        val tempDir = NSTemporaryDirectory()
        val outputPath = "$tempDir/record_$timestamp.mp4"
        val outputUrl = NSURL.fileURLWithPath(outputPath)

        platform.Foundation.NSFileManager.defaultManager.removeItemAtPath(outputPath, null)

        val connection = movieOutput.connectionWithMediaType(AVMediaTypeVideo)
        if (connection != null && connection.isVideoOrientationSupported()) {
            connection.videoOrientation = AVCaptureVideoOrientationPortrait
        }

        movieOutput.startRecordingToOutputFileURL(outputUrl, this)
    }

    fun stopRecording() {
        if (movieOutput.isRecording()) {
            movieOutput.stopRecording()
        }
    }

    override fun captureOutput(
        output: AVCaptureFileOutput,
        didStartRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>
    ) {
        // Started
    }

    override fun captureOutput(
        output: AVCaptureFileOutput,
        didFinishRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>,
        error: NSError?
    ) {
        if (error != null) {
            val filePath = didFinishRecordingToOutputFileAtURL.path
            val fileExists = platform.Foundation.NSFileManager.defaultManager.fileExistsAtPath(filePath ?: "")
            // Sometimes it throws an error even if it saved correctly (e.g. if stopped programmatically).
            if (fileExists && filePath != null) {
                onVideoRecorded(filePath)
            } else {
                println("Recording error: ${error.localizedDescription}")
            }
        } else {
            didFinishRecordingToOutputFileAtURL.path?.let { onVideoRecorded(it) }
        }
    }
}
