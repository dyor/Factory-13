package org.example.project.domain

import platform.UIKit.UIActivityViewController
import platform.UIKit.UIApplication
import platform.Foundation.NSURL

actual fun shareVideo(videoPath: String) {
    val url = NSURL.fileURLWithPath(videoPath)
    val activityViewController = UIActivityViewController(activityItems = listOf(url), applicationActivities = null)
    
    val rootViewController = UIApplication.sharedApplication.keyWindow?.rootViewController
    rootViewController?.presentViewController(activityViewController, animated = true, completion = null)
}