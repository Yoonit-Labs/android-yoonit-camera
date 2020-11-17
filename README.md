<img src="https://raw.githubusercontent.com/Yoonit-Labs/android-yoonit-camera/development/logo_cyberlabs.png" width="300">

# android-yoonit-camera  

![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/Yoonit-Labs/android-yoonit-camera?color=lightgrey&label=version&style=for-the-badge) ![GitHub](https://img.shields.io/github/license/Yoonit-Labs/android-yoonit-camera?color=lightgrey&style=for-the-badge)

A Android plugin to provide:
- Modern Android Camera API (Camera X)
- [Standart Google ML Kit](https://developers.google.com/ml-kit)
- Camera preview (Front & Back)
- Face detection (With Min & Max size (Soon))
- Landmark detection (Soon)
- Face crop
- Face capture
- Frame capture
- Face ROI (Soon)
- QR Code scanning

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
	implementation 'com.github.Yoonit-Labs:android-yoonit-camera:v1.0.2'
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
  
| Function                        | Parameters                     | Valid values                                                                      | Return Type | Description
| -                               | -                              | -                                                                                 | -           | -  
| **`startPreview`**              | -                              | -                                                                                 | void        | Start camera preview if has permission.
| **`startCaptureType`**          | `captureType: String`          | <ul><li>`"none"`</li><li>`"face"`</li><li>`"barcode"`</li><li>`"frame"`</li></ul> | void        | Set capture type none, face, barcode or frame.
| **`stopCapture`**               | -                              | -                                                                                 | void        | Stop any type of capture.
| **`toggleCameraLens`**          | -                              | -                                                                                 | void        | Set camera lens facing front or back.
| **`getCameraLens`**             | -                              | -                                                                                 | Int         | Return `Int` that represents lens face state: 0 for front 1 for back camera.  
| **`setFaceNumberOfImages`**     | `faceNumberOfImages: Int`      | Any positive `Int` value                                                          | void        | Default value is 0. For value 0 is saved infinity images. When saved images reached the "face number os images", the `onEndCapture` is triggered.
| **`setFaceDetectionBox`**       | `faceDetectionBox: Boolean`    | `True` or `False`                                                                 | void        | Set to show face detection box when face detected.   
| **`setFaceTimeBetweenImages`**  | `faceTimeBetweenImages: Long`  | Any positive number that represent time in milli seconds                          | void        | Set saving face images time interval in milli seconds.  
| **`setFacePaddingPercent`**     | `facePaddingPercent: Float`    | Any positive `Float` value                                                        | void        | Set face image and bounding box padding in percent.  
| **`setFaceImageSize`**          | `width: Int, height: Int`      | Any positive `Int` value                                                          | void        | Set face image size to be saved.
| **`setFaceCaptureMinSize`**     | `faceCaptureMinSize: Float`    | Value between `0` and `1`. Represents the percentage.                             | void        | Set the minimum face capture based on the screen width limit.
| **`setFaceCaptureMaxSize`**     | `faceCaptureMaxSize: Float`    | Value between `0` and `1`. Represents the percentage.                             | void        | Set the maximum face capture based on the screen width limit.
| **`setFrameTimeBetweenImages`** | `frameTimeBetweenImages: Long` | Any positive number that represent time in milli seconds                          | void        | Set saving frame images time interval in milli seconds.
| **`setFrameNumberOfImages`**    | `frameNumberOfImages: Int`     | Any positive `Int` value                                                          | void        | Default value is 0. For value 0 is saved infinity images. When saved images reached the "frame number os images", the `onEndCapture` is triggered. 
  
### Events

| Event                     | Parameters                                  | Description
| -                         | -                                           | -
| **`onFaceImageCreated`**  | `count: Int, total: Int, imagePath: String` | Must have started capture type of face (see `startCaptureType`). Emitted when the face image file is created: <ul><li>count: current index</li><li>total: total to create</li><li>imagePath: the face image path</li><ul>  
| **`onFrameImageCreated`** | `count: Int, total: Int, imagePath: String` | Must have started capture type of frame (see `startCaptureType`). Emitted when the frame image file is created: <ul><li>count: current index</li><li>total: total to create</li><li>imagePath: the frame image path</li><ul>  
| **`onFaceDetected`**      | `x: Int, y: Int, width: Int, height: Int`   | Must have started capture type of face. Emit the detected face bounding box.
| **`onFaceUndetected`**    | -                                           | Must have started capture type of face. Emitted after `onFaceDetected`, when there is no more face detecting.
| **`onEndCapture`**        | -                                           | Must have started capture type of face or frame. Emitted when the number of face or frame image files created is equal of the number of images set (see the method `setFaceNumberOfImages` for face and `setFrameNumberOfImages` for frame).   
| **`onBarcodeScanned`**    | `content: String`                           | Must have started capture type of barcode (see `startCaptureType`). Emitted when the camera scan a QR Code.   
| **`onError`**             | `error: String`                             | Emit message error. Argument may be a string or an pre-defined[**KeyError**](###KeyError).
| **`onMessage`**           | `message: String`                           | Emit message. Argument may be a string or an pre-defined[**Message**](###Message).
| **`onPermissionDenied`**  | -                                           | Emit when try to `startPreview` but there is not camera permission.

### KeyError

Pre-define key error constants used by the `onError`event.

| KeyError                          | Description
| -                                 | -
| NOT_STARTED_PREVIEW               | Tried to start a process that depends on to start the camera preview.
| INVALID_CAPTURE_TYPE              | Tried to start a non-existent capture type.
| INVALID_FACE_NUMBER_OF_IMAGES     | Tried to input invalid face number of images to capture. 
| INVALID_FACE_TIME_BETWEEN_IMAGES  | Tried to input invalid face time interval to capture face.
| INVALID_FACE_PADDING_PERCENT      | Tried to input invalid face padding percent.
| INVALID_FACE_IMAGE_SIZE           | Tried to input invalid image width or height.
| INVALID_FACE_CAPTURE_MIN_SIZE     | Tried to input invalid face capture minimum size. 
| INVALID_FACE_CAPTURE_MAX_SIZE     | Tried to input invalid face capture maximum size.
| INVALID_FRAME_NUMBER_OF_IMAGES    | Tried to input invalid frame number of images to capture.
| INVALID_FRAME_TIME_BETWEEN_IMAGES | Tried to input invalid frame time interval to capture face.

### Message

Pre-define message constants used by the `onMessage`event.

| Message                       | Description
| -                             | -
| INVALID_CAPTURE_FACE_MIN_SIZE | Face bounding box width percentage in relation of the screen width is less than the setted (`setFaceCaptureMinSize`).
| INVALID_CAPTURE_FACE_MAX_SIZE | Face bounding box width percentage in relation of the screen width is more than the setted (`setFaceCaptureMaxSize`).


## To contribute and make it better

Clone the repo, change what you want and send PR.

Contributions are always welcome!

---

Code with ❤ by the [**Cyberlabs AI**](https://cyberlabs.ai/) Front-End Team