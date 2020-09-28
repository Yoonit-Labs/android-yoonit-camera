# android-yoonit-camera
[![Generic badge](https://img.shields.io/badge/version-v1.0.0-<COLOR>.svg)](https://shields.io/)  

## Download

<br/>

Add the JitPack repository to your root build.gradle at the end of repositories
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

## Methods  

<br/>

| Function | Parameters | Return Type | Valid values | Description |  
|-----------|------|------------------------|--------------|-------------|
| **`startPreview`** | ```-``` | void | - | Start camera preview if has permission. 
| **`stopCapture`** | ```-``` | void | - | Stop the face image capture.
| **`setCaptureType`** | ```captureType : Int``` | void | 1 for face recognition or 2 for barcode recognition | Set capture type of face or barcode.
| **`toggleCameraLens`** | ```-``` | void | - | Set camera lens facing front or back.
| **`getCameraLens`** | ```-``` | Int | - | Return Integer that represents lens face state (0 for Front Camera, 1 for Back Camera).
| **`setFaceNumberOfImages`** | ```faceNumberOfImages: Int``` | void | Any Int value | Set number of images to save when face detected. 
| **`setFaceDetectionBox`** |``` faceDetectionBox: Boolean``` | void | True or False | Set to show face detection box when face detected. 
| **`setFaceTimeBetweenImages`** | ```faceTimeBetweenImages: Long``` | void | Any long that represent time in milli seconds | Set saving face images time interval in milli seconds.
| **`setFacePaddingPercent`** | ```facePaddingPercent: Float``` | void | Any Float value | Set face image and bounding box padding in percent.
| **`setFaceImageSize `** | ```faceImageSize: Int``` | void | Any Int value | Set face image size to be saved.  

<br/>

## Events  

<br/>

| Event | Parameters | Description |  
|-----------|------|-------------|
| **`onFaceImageCreated`** | ```count: Int, total: Int, imagePath: String``` | Emit when the camera save an image face.
| **`onFaceDetected`** | ```faceDetected: Boolean``` | Emit when a face is detected or hided.
| **`onEndCapture`** | ```-``` | Emit when the number of images saved is equal of the number of images set. 
| **`onBarcodeScanned`** | ```content: String``` | Emit content when detect a barcode. 
| **`onError`** |```error: String``` | Emit message error.
| **`onMessage`** | ```message: String``` | Emit message. 
| **`onPermissionDenied`** | ```-``` | Emit when try to startPreview but there is not camera permission.
