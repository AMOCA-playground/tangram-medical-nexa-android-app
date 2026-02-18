package demo.nexa.clinical_transcription_demo.llm

import android.content.Context
import android.util.Log
import java.io.File
import java.io.IOException

/**
 * Manages LLM model storage and path resolution.
 * Models are bundled in assets and copied to app storage on first run.
 * Supports LFM2.5-1.2B-Instruct model for both summarization and SOAP creation.
 */
class LlmModelManager(private val context: Context) {
    
    enum class ModelType {
        LIQUID_SUMMARIZER,
        LFM_SOAP_CREATOR
    }
    
    private val modelsDir: File by lazy {
        File(context.filesDir, MODELS_FOLDER_NAME).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    /**
     * Get the path to the LFM model file (GGUF format).
     * Expected structure: filesDir/nexa_models/LFM2.5-1.2B-Instruct-GGUF/LFM2.5-1.2B-Instruct-Q4_K_M.gguf
     * 
     * @return File pointing to the model file
     */
    fun getLfmModelPath(): File {
        return File(File(modelsDir, LFM_MODEL_FOLDER), LFM_MODEL_FILENAME)
    }
    
    /**
     * Check if a model is available and ready to use.
     * 
     * @param modelType The type of model to check
     * @return true if the model is available (file exists for GGUF models)
     */
    fun isModelAvailable(modelType: ModelType): Boolean {
        // Both types now use the same LFM model
        val modelPath = getLfmModelPath()
        return modelPath.exists() && modelPath.isFile && modelPath.length() > 0
    }
    
    /**
     * Ensure a model is installed from assets to filesDir.
     * Copies from assets if target folder doesn't exist or is empty.
     * 
     * @param modelType The type of model to install
     * @return true if model is available, false if installation failed
     */
    fun ensureModelInstalled(modelType: ModelType): Boolean {
        if (isModelAvailable(modelType)) {
            return true
        }
        
        val assetsPath = ASSETS_LFM_PATH
        val modelDir = File(modelsDir, LFM_MODEL_FOLDER)
        
        try {
            val assetFiles = context.assets.list(assetsPath)
            if (assetFiles.isNullOrEmpty()) {
                Log.w(TAG, "No ${modelType.name} model found in assets")
                return false
            }
        } catch (e: IOException) {
            Log.w(TAG, "Assets folder not found: $assetsPath", e)
            return false
        }
        
        return try {
            if (modelDir.exists()) {
                modelDir.deleteRecursively()
            }
            
            modelDir.mkdirs()
            copyModelFromAssets(assetsPath, modelDir)
            
            val installed = isModelAvailable(modelType)
            if (!installed) {
                Log.w(TAG, "${modelType.name} model installation failed validation")
            }
            installed
        } catch (e: Exception) {
            Log.e(TAG, "Failed to install ${modelType.name} model from assets", e)
            
            if (modelDir.exists()) {
                modelDir.deleteRecursively()
            }
            
            false
        }
    }
    
    /**
     * Recursively copy model files from assets to filesDir.
     */
    private fun copyModelFromAssets(assetPath: String, destFile: File) {
        val assetManager = context.assets
        val files = assetManager.list(assetPath) ?: emptyArray()
        
        if (files.isNotEmpty()) {
            // Directory - recurse into children
            destFile.mkdirs()
            files.forEach { filename ->
                copyModelFromAssets("$assetPath/$filename", File(destFile, filename))
            }
        } else {
            // File - copy it
            destFile.parentFile?.mkdirs()
            assetManager.open(assetPath).use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
    
    /**
     * Get a human-readable status message about model availability.
     */
    fun getModelStatusMessage(): String {
        val available = isModelAvailable(ModelType.LIQUID_SUMMARIZER)
        
        return if (available) {
            "LFM-1.2B model ready"
        } else {
            "No models found. Please add LFM-1.2B to assets/nexa_models/"
        }
    }
    
    companion object {
        private const val TAG = "LlmModelManager"
        private const val MODELS_FOLDER_NAME = "nexa_models"
        
        // LFM2.5-1.2B-Instruct model (replaces Qwen)
        private const val LFM_MODEL_FOLDER = "LFM2.5-1.2B-Instruct-GGUF"
        private const val LFM_MODEL_FILENAME = "LFM2.5-1.2B-Instruct-Q4_K_M.gguf"
        private const val ASSETS_LFM_PATH = "nexa_models/$LFM_MODEL_FOLDER"
        
        @Volatile
        private var INSTANCE: LlmModelManager? = null
        
        fun getInstance(context: Context): LlmModelManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LlmModelManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
