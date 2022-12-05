package ru.dikoresearch.aridewarehouse.presentation.camera

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class CameraViewModel @Inject constructor(
): ViewModel() {

    private val _cameraScreenState = MutableStateFlow(CameraScreenState(0, false))
    val cameraScreenState: StateFlow<CameraScreenState> = _cameraScreenState.asStateFlow()

    private val imagesPathsBuffer = mutableListOf<String>()
    private val imagesPaths = mutableListOf<String>()

    fun addPicture(filePath: String){
        imagesPathsBuffer.add(filePath)
        _cameraScreenState.value = _cameraScreenState.value.copy(numberOfCapturedImages = imagesPathsBuffer.size, isOkBtnEnabled = true)
    }

    fun flushImagesPathsBuffer(){
        imagesPaths.addAll(imagesPathsBuffer)
//        imagesPathsBuffer.clear()
        _cameraScreenState.value = _cameraScreenState.value.copy(numberOfCapturedImages = imagesPathsBuffer.size, isOkBtnEnabled = false)
    }

    fun getImagesPaths(): List<String>{
        return imagesPaths.toList()
    }

    fun refreshImagePaths(){
        imagesPaths.clear()
        imagesPathsBuffer.clear()
        _cameraScreenState.value = _cameraScreenState.value.copy(numberOfCapturedImages = imagesPathsBuffer.size, isOkBtnEnabled = false)
    }

}