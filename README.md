<img src="https://raw.githubusercontent.com/Yoonit-Labs/android-yoonit-camera/development/logo_cyberlabs.png" width="300">

# android-yoonit-camera  

![Generic badge](https://img.shields.io/badge/version-v1.0.0-<COLOR>.svg) ![MIT license](https://img.shields.io/badge/License-MIT-blue.svg)

Face image capture and QR Code scanning library for android using the [Standart Google ML Kit](https://developers.google.com/ml-kit).

## Install
  
Add the JitPack repository to your root `build.gradle` at the end of repositories  

```groovy  
allprojects {
	repositories {  
	... 
	maven { url 'https://jitpack.io' }
	} 
}  
```  

Add the dependency  

```groovy  
dependencies {
	implementation 'com.github.Yoonit-Labs:android-yoonit-camera:v1.0.0-alpha1'
}
```  


## Usage  
 
All the functionalities that the `android-yoonit-camera` provides is accessed through the `CameraView`, that includes the camera preview.  Below we have the basic usage code, for more details, see the [**Methods**](#methods).


### Camera Preview

Do not forget request camera permission. Use like this in the your layout XML:

```kotlin
<ai.cyberlabs.yoonit.camera.CameraView  
  android:id="@+id/camera_view"  
  android:layout_width="match_parent"  
  android:layout_height="match_parent"/>
```

And inside your code:

```kotlin
var cameraView: CameraView
this.cameraView = camera_view
this.cameraView.startPreview()
```

### Start capturing face images

With camera preview, we can start capture detected face and generate images:

```kotlin
this.cameraView.startCaptureType("face")
```

Set camera event listener to get the result:

```kotlin
this.cameraView.setCameraEventListener(this.buildCameraEventListener())
...
fun buildCameraEventListener(): CameraEventListener = object : CameraEventListener {
...
	override fun onFaceImageCreated(count: Int, total: Int, imagePath: String) {  
		// YOUR CODE
	}
...
}
```

### Start scanning QR Codes

With camera preview, we can start scanning QR codes:

```kotlin
this.cameraView.startCaptureType("barcode")
```

Set camera event listener to get the result:

```kotlin
this.cameraView.setCameraEventListener(this.buildCameraEventListener())
...
fun buildCameraEventListener(): CameraEventListener = object : CameraEventListener {
...
	override fun onBarcodeScanned(content: String) {  
		// YOUR CODE
	}
...
}
```  
    
    
## API
  
### Methods   
  
| Function                       | Parameters                    | Return Type | Valid values                                                    | Description
| -                              | -                             | -           | -                                                               | -  
| **`startPreview`**             | -                             | void        | -                                                               | Start camera preview if has permission.
| **`startCaptureType`**         | `captureType: String`         | void        | <ul><li>`"none"`</li><li>`"face"`</li><li>`"barcode"`</li></ul> | Set capture type none, face or barcode.
| **`stopCapture`**              | -                             | void        | -                                                               | Stop any type of capture.
| **`toggleCameraLens`**         | -                             | void        | -                                                               | Set camera lens facing front or back.
| **`getCameraLens`**            | -                             | Int         | -                                                               | Return `Int` that represents lens face state: 0 for front 1 for back camera.  
| **`setFaceNumberOfImages`**    | `faceNumberOfImages: Int`     | void        | Any positive `Int` value                                        | Default value is 0. For value 0 is saved infinity images. When saved images reached the "face number os images", the `onEndCapture` is triggered.
| **`setFaceDetectionBox`**      | `faceDetectionBox: Boolean`   | void        | `True` or `False`                                               | Set to show face detection box when face detected.   
| **`setFaceTimeBetweenImages`** | `faceTimeBetweenImages: Long` | void        | Any positive number that represent time in milli seconds        | Set saving face images time interval in milli seconds.  
| **`setFacePaddingPercent`**    | `facePaddingPercent: Float`   | void        | Any positive `Float` value                                      | Set face image and bounding box padding in percent.  
| **`setFaceImageSize`**         | `width: Int, height: Int`     | void        | Any positive `Int` value                                        | Set face image size to be saved. 
  
### Events

| Event                    | Parameters                                  | Description
| -                        | -                                           | -
| **`onFaceImageCreated`** | `count: Int, total: Int, imagePath: String` | Must have started capture type of face (see `startCaptureType`). Emitted when the face image file is created: <ul><li>count: current index</li><li>total: total to create</li><li>imagePath: the face image path</li><ul>  
| **`onFaceDetected`**     | `x: Int, y: Int, width: Int, height: Int`   | Must have started capture type of face. Emit the detected face bounding box.
| **`onFaceUndetected`**   | -                                           | Must have started capture type of face. Emitted after `onFaceDetected`, when there is no more face detecting.
| **`onEndCapture`**       | -                                           | Must have started capture type of face. Emitted when the number of face image files created is equal of the number of images set (see the method `setFaceNumberOfImages`).   
| **`onBarcodeScanned`**   | `content: String`                           | Must have started capture type of barcode (see `startCaptureType`). Emitted when the camera scan a QR Code.   
| **`onError`**            | `error: String`                             | Emit message error.  
| **`onMessage`**          | `message: String`                           | Emit message.   
| **`onPermissionDenied`** | -                                           | Emit when try to `startPreview` but there is not camera permission.


## To contribute and make it better

Clone the repo, change what you want and send PR.

Contributions are always welcome!

---

Code with ❤ by the [**Cyberlabs AI**](https://cyberlabs.ai/) Front-End Team