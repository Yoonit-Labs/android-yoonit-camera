
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

<br/>

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
this.cameraView.startCaptureType("qrcode")
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
    
<br/>
  
## Methods   
  
| Function | Parameters | Return Type | Valid values | Description |
|-|-|-|-|-|  
| **`startPreview`** | - | void | - | Start camera preview if has permission.
| **`startCaptureType`** | `captureType: String` | void | `none` default capture type. `face` for face recognition. `barcode` to read barcode content. | Set capture type none, face or barcode.
| **`stopCapture`** | - | void | - | Stop any type of capture.
| **`toggleCameraLens`** | - | void | - | Set camera lens facing front or back.
| **`getCameraLens`** | - | Int | - | Return `Int` that represents lens face state: 0 for front 1 for back camera.  
| **`setFaceNumberOfImages`** | `faceNumberOfImages: Int` | void | Any positive `Int` value | Default value is 0. For value 0 is saved infinity images. When saved images reached the "face number os images", the `onEndCapture` is triggered.
| **`setFaceDetectionBox`** |`faceDetectionBox: Boolean` | void | `True` or `False` | Set to show face detection box when face detected.   
| **`setFaceTimeBetweenImages`** | `faceTimeBetweenImages: Long` | void | Any positive number that represent time in milli seconds | Set saving face images time interval in milli seconds.  
| **`setFacePaddingPercent`** | `facePaddingPercent: Float` | void | Any positive `Float` value | Set face image and bounding box padding in percent.  
| **`setFaceImageSize`** | `faceImageSize: Int` | void | Any positive `Int` value | Set face image size to be saved.    
  
<br/>  
  
## Events

| Event | Parameters | Description |
|-|-|-|
| **`onFaceImageCreated`** | `count: Int, total: Int, imagePath: String` | Emit when the camera save an image face.  
| **`onFaceDetected`** | `faceDetected: Boolean` | Emit when a face is detected or hided.  
| **`onEndCapture`** | - | Emit when the number of images saved is equal of the number of images set.   
| **`onBarcodeScanned`** | `content: String` | Emit content when detect a barcode.   
| **`onError`** |`error: String` | Emit message error.  
| **`onMessage`** | `message: String` | Emit message.   
| **`onPermissionDenied`** | - | Emit when try to `startPreview` but there is not camera permission.