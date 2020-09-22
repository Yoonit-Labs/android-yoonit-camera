# android-yoonit-camera
[![Generic badge](https://img.shields.io/badge/version-v1.0.0-<COLOR>.svg)](https://shields.io/)  
  
### Methods  

```java
startPreview()  
``` 
Start camera preview if has permission.

<br/>
  
```java
setCaptureType(captureType: Int)  
```  
Set capture type of face or barcode.  

<br/>

```java
stopCapture()  
```
Stops the face image capture.  
    
<br/>  
  
```java
toggleCameraLens()  
```  
Set camera lens facing front or back.  
  
<br/>  

```java
getCameraLens()  
```  
Return Integer that represents lens face state (0 for Front Camera, 1 for Back Camera).
  
<br/>  

```java
setFaceNumberOfImages(faceNumberOfImages: Int)  
```  
Set number of images to save when face detected.  
  
<br/>  
  
```java
setFaceDetectionBox(faceDetectionBox: Boolean)  
```
Set to show face detection box when face detected.  
  
<br/>  
  
```java
setFaceTimeBetweenImages(faceTimeBetweenImages: Long)
```
Set saving face images time interval in milli seconds.
  
<br/>  
  
```java
setFacePaddingPercent(facePaddingPercent: Float) 
```
Set face image and bounding box padding in percent.  
  
<br/>  
    
```java
setFaceImageSize(faceImageSize: Int)  
```
Set face image size to be saved.  
  
<br/>  
  
### Events  

```java
onFaceImageCreated(count: Int, total: Int, imagePath: String)
```  
> @param `count` current image index.
> @param `total` total images to create.
> @param `imagePath` image path.
  
Emit when the camera save an image face.
  
<br/>
  
```java
onFaceDetected(faceDetected: Boolean)  
```
> @param `faceDetected` indicates if detected or hided.

Emit when a face is detected or hided.

<br/>

```java
onEndCapture()
```
Emit when the number of images saved is equal of the number of images set.
  
<br/>  
  
```java
onBarcodeScanned(content: String)  
```
> @param `content`  
  
Emit content when detect a barcode.
  
<br/>  
  
```java
onError(error: String)
```
> @param `error`
  
Emit message error.   
  
<br/>  
  
```java
onMessage(message: String)
```
> @param `message`

Emit message.  
  
<br/>

```java
onPermissionDenied()  
```
Emit when try to `startPreview` but there is not camera permission.