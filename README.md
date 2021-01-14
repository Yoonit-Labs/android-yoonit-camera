<img src="https://raw.githubusercontent.com/Yoonit-Labs/android-yoonit-camera/development/logo_cyberlabs.png" width="300">

# android-yoonit-camera  

![GitHub tag (latest by date)](https://img.shields.io/github/v/tag/Yoonit-Labs/android-yoonit-camera?color=lightgrey&label=version&style=for-the-badge) ![GitHub](https://img.shields.io/github/license/Yoonit-Labs/android-yoonit-camera?color=lightgrey&style=for-the-badge)

A Android plugin to provide:
- Modern Android Camera API (Camera X)
- [Standart Google ML Kit](https://developers.google.com/ml-kit)
- Camera preview (Front & Back)
- Face detection (With Min & Max size)
- Landmark detection (Soon)
- Face crop
- Face capture
- Frame capture
- Face ROI
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
	implementation 'com.github.Yoonit-Labs:android-yoonit-camera:master-SNAPSHOT'
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
  android:layout_height="match_parent" />
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
	override fun onImageCaptured(type: String, count: Int, total: Int, imagePath: String) {  
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
	override fun onQRCodeScanned(content: String) {  
		// YOUR CODE
	}
...
}
```  
    
    
## API
  
### Methods   
  
| Function                        | Parameters                                                                     | Valid values                                                                      | Return Type  | Description
| -                               | -                                                                              | -                                                                                 | -            | -  
| startPreview                   | -                                                                              | -                                                                                 | void         | Start camera preview if has permission.
| startCaptureType               | `captureType: String`                                                          | <ul><li>`"none"`</li><li>`"face"`</li><li>`"qcode"`</li><li>`"frame"`</li></ul>   | void         | Set capture type none, face, QR Code or frame.
| stopCapture                    | -                                                                              | -                                                                                 | void         | Stop any type of capture.
| toggleCameraLens               | -                                                                              | -                                                                                 | void         | Set camera lens facing front or back.
| setCameraLens                  | `cameraLens: String`                                                           | <ul><li>`"front"`</li><li>`"back"`</li></ul>                                      | void         | Set camera to use "front" or "back" lens. Default value is "front".
| getCameraLens                  | -                                                                              | -                                                                                 | Int          | Return `Int` that represents lens face state: 0 for front 1 for back camera.
| setImageCaptureAmount          | `amount: Int`                                                                  | Any positive `Int` value                                                          | void         | Default value is 0. For value 0 is saved infinity images. When saved images reached the "number os images", the `onEndCapture` is triggered.
| setImageCaptureInterval        | `interval: Long`                                                               | Any positive number that represent time in milli seconds                          | void         | Set saving face/frame images time interval in milli seconds.
| setImageCaptureWidth           | `width: Int`                                                                   | Any positive `number` value that represents in pixels                             | void         | Set face image width to be created in pixels.
| setImageCaptureHeight          | `height: Int`                                                                  | Any positive `number` value that represents in pixels                             | void         | Set face image height to be created in pixels.
| setImageCapture                | `enable: Bool`                                                                 | `true` or `false`                                                                 | void         | Set to enable/disable save image when capturing face and frame.
| setFaceDetectionBox            | `enable: Bool`                                                                 | `true` or `false`                                                                 | void         | Set to show a detection box when face detected.
| setFacePaddingPercent          | `facePaddingPercent: Float`                                                    | Any positive `Float` value                                                        | void         | Set face image and bounding box padding in percent.
| setFaceCaptureMinSize          | `faceCaptureMinSize: Float`                                                    | Value between `0` and `1`. Represents the percentage.                             | void         | Set the minimum face capture based on the screen width.
| setFaceCaptureMaxSize          | `faceCaptureMaxSize: Float`                                                    | Value between `0` and `1`. Represents the percentage.                             | void         | Set the maximum face capture based on the screen width.
| setFaceROIEnable               | `enable: Bool`                                                                 | `true` or `false`                                                                 | void         | Enable/disable face region of interest capture.
| setFaceROITopOffset            | `topOffset: Float`                                                             | Values between `0` and `1`. Represents the percentage.                            | void         | Distance in percentage of the top face bounding box with the top of the camera preview.
| setFaceROIRightOffset          | `rightOffset: Float`                                                           | Values between `0` and `1`. Represents the percentage.                            | void         | Distance in percentage of the right face bounding box with the right of the camera preview.
| setFaceROIBottomOffset         | `bottomOffset: Float`                                                          | Values between `0` and `1`. Represents the percentage.                            | void         | Distance in percentage of the bottom face bounding box with the bottom of the camera preview.
| setFaceROILeftOffset           | `leftOffset: Float`                                                            | Values between `0` and `1`. Represents the percentage.                            | void         | Distance in percentage of the left face bounding box with the left of the camera preview.
| setFaceROIMinSize              | `minimumSize: Float`                                                           | Values between `0` and `1`. Represents the percentage.                            | void         | Set the minimum face size related with the region of interest.
| setBlurFaceDetectionBox        | `enable: Bool`                                                                 | `true` or `false`                                                                 | void         | Enable/disable blur in face detection box.
| setImageCaptureColorEncoding   | `colorEncoding: String`                                                        | <ul><li>`"RGB"`</li><li>`"YUV"`</li>                                              | void         | Set the color encoding for the saved images.

### Events

| Event                     | Parameters                                                | Description
| -                         | -                                                         | -
| **`onImageCaptured`**     | `type: String, count: Int, total: Int, imagePath: String` | Must have started capture type of face/frame (see `startCaptureType`). Emitted when the face image file is created: <ul><li>type: 'face' | 'frame'</li><li>count: current index</li><li>total: total to create</li><li>imagePath: the image path</li><ul>  
| **`onFaceDetected`**      | `x: Int, y: Int, width: Int, height: Int`                 | Must have started capture type of face. Emit the detected face bounding box.
| **`onFaceUndetected`**    | -                                                         | Must have started capture type of face. Emitted after `onFaceDetected`, when there is no more face detecting.
| **`onEndCapture`**        | -                                                         | Must have started capture type of face/frame. Emitted when the number of image files created is equal of the number of images set (see the method `setNumberOfImages`).   
| **`onQRCodeScanned`**     | `content: String`                                         | Must have started capture type of qrcode (see `startCaptureType`). Emitted when the camera scan a QR Code.   
| **`onError`**             | `error: String`                                           | Emit message error.
| **`onMessage`**           | `message: String`                                         | Emit message.
| **`onPermissionDenied`**  | -                                                         | Emit when try to `startPreview` but there is not camera permission.

### KeyError

Pre-define key error constants used by the `onError` event.

| KeyError                          | Description
| -                                 | -
| INVALID_CAPTURE_TYPE              | Tried to start a non-existent capture type.
| INVALID_CAMERA_LENS               | Tried to input invalid camera lens.
| INVALID_NUMBER_OF_IMAGES          | Tried to input invalid face/frame number of images to capture.
| INVALID_TIME_BETWEEN_IMAGES       | Tried to input invalid face time interval to capture face.
| INVALID_OUTPUT_IMAGE_WIDTH        | Tried to input invalid image width.
| INVALID_OUTPUT_IMAGE_HEIGHT       | Tried to input invalid image height.
| INVALID_FACE_PADDING_PERCENT      | Tried to input invalid face padding percent.
| INVALID_FACE_CAPTURE_MIN_SIZE     | Tried to input invalid face capture minimum size. 
| INVALID_FACE_CAPTURE_MAX_SIZE     | Tried to input invalid face capture maximum size.
| INVALID_FACE_ROI_TOP_OFFSET       | Tried to input invalid face region of interest top offset.
| INVALID_FACE_ROI_RIGHT_OFFSET     | Tried to input invalid face region of interest right offset.
| INVALID_FACE_ROI_BOTTOM_OFFSET    | Tried to input invalid face region of interest bottom offset.
| INVALID_FACE_ROI_LEFT_OFFSET      | Tried to input invalid face region of interest left offset.
| INVALID_FACE_ROI_MIN_SIZE         | Tried to input invalid face region of interest minimum size.

### Message

Pre-define message constants used by the `onMessage` event.

| Message                           | Description
| -                                 | -
| INVALID_CAPTURE_FACE_MIN_SIZE     | Face width percentage in relation of the screen width is less than the set (`setFaceCaptureMinSize`).
| INVALID_CAPTURE_FACE_MAX_SIZE     | Face width percentage in relation of the screen width is more than the set (`setFaceCaptureMaxSize`).
| INVALID_CAPTURE_FACE_OUT_OF_ROI   | Face bounding box is out of the set region of interest (`setFaceROIOffset`).
| INVALID_CAPTURE_FACE_ROI_MIN_SIZE | Face width percentage in relation of the screen width is less than the set (`setFaceROIMinSize`).

## To contribute and make it better

Clone the repo, change what you want and send PR.

Contributions are always welcome!

---

Code with ❤ by the [**Cyberlabs AI**](https://cyberlabs.ai/) Front-End Team