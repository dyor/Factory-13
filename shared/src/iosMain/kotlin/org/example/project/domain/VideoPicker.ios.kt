package org.example.project.domain

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import platform.Foundation.NSURL
import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.UIKit.UIApplication
import platform.UIKit.UIImagePickerController
import platform.UIKit.UIImagePickerControllerDelegateProtocol
import platform.UIKit.UINavigationControllerDelegateProtocol
import platform.darwin.NSObject
import platform.UniformTypeIdentifiers.UTTypeMovie
import platform.UniformTypeIdentifiers.UTTypeVideo
import kotlinx.cinterop.ExperimentalForeignApi

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun VideoPicker(
    show: Boolean,
    onVideoPicked: (String?) -> Unit
) {
    val delegate = remember {
        object : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {
            override fun imagePickerController(
                picker: UIImagePickerController,
                didFinishPickingMediaWithInfo: Map<Any?, *>
            ) {
                val url = didFinishPickingMediaWithInfo["UIImagePickerControllerMediaURL"] as? NSURL
                if (url != null) {
                    val fileManager = NSFileManager.defaultManager
                    val tempDir = NSTemporaryDirectory()
                    val fileName = "picked_video_${kotlin.random.Random.nextInt()}.mp4"
                    val destUrl = NSURL.fileURLWithPath("$tempDir/$fileName")
                    
                    try {
                        fileManager.copyItemAtURL(url, destUrl, null)
                        onVideoPicked(destUrl.path)
                    } catch (e: Exception) {
                        println("Failed to copy video: ${e.message}")
                        onVideoPicked(url.path) // Fallback to original
                    }
                } else {
                    onVideoPicked(null)
                }
                picker.dismissViewControllerAnimated(true, null)
            }

            override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
                onVideoPicked(null)
                picker.dismissViewControllerAnimated(true, null)
            }
        }
    }

    LaunchedEffect(show) {
        if (show) {
            val picker = UIImagePickerController()
            picker.sourceType = platform.UIKit.UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
            picker.mediaTypes = listOf("public.movie")
            picker.delegate = delegate
            UIApplication.sharedApplication.keyWindow?.rootViewController?.presentViewController(picker, true, null)
        }
    }
}
