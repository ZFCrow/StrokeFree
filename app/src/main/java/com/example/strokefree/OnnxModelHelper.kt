package com.example.strokefree

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OnnxValue
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.content.Context
import android.util.Log
import java.lang.StackWalker.Option
import java.nio.FloatBuffer
import java.util.Optional

class OnnxModelHelper (private val context: Context){
    private lateinit var ortEnvironment: OrtEnvironment
    private lateinit var ortSession: OrtSession

    fun loadModel(modelName: String){
        ortEnvironment = OrtEnvironment.getEnvironment()
        val modelBytes = context.assets.open(modelName).readBytes()
        ortSession = ortEnvironment.createSession(modelBytes)

        Log.d("model input names", ortSession.inputNames.toString())
    }

    fun runInference(input : Array<FloatArray>) : Int? {
        return try {
            val floatBuffer = FloatBuffer.allocate(input.size * input[0].size)
            input.forEach { floatBuffer.put(it) }
            floatBuffer.rewind()


            val inputTensor = ai.onnxruntime.OnnxTensor.createTensor(ortEnvironment, floatBuffer, longArrayOf(1, 16)) // ✅ Match input dimensions
            val output = ortSession.run(mapOf(ortSession.inputNames.first() to inputTensor))
            Log.d("OnnxDebug", "Raw ONNX Output: $output, Type: ${output.javaClass.name}")

            val optionalResult = output[ortSession.outputNames.first()] as? Optional<OnnxValue>
            Log.d("optionalResult", "Raw ONNX Output: $optionalResult, Type: ${optionalResult?.javaClass?.name}")

            val resultTensor = optionalResult?.get() as? OnnxTensor
            Log.d("resultTensor", "Raw ONNX Output: $resultTensor, Type: ${resultTensor?.javaClass?.name}")
//! the stuff that works
// ! ===========================================================
            val Longbuffer = resultTensor?.longBuffer
            Longbuffer?.rewind()

            val extractedValue = Longbuffer?.get()
            Log.d("extractedValue", "Raw ONNX Output: $extractedValue, Type: ${extractedValue?.javaClass?.name}")
            extractedValue?.toInt()
//! ===========================================================
//            val floatBuffer1 = resultTensor?.floatBuffer
//            floatBuffer1?.rewind()
//            val prob = FloatArray(floatBuffer1?.remaining() ?: 0)
//            floatBuffer1?.get(prob)
//            Log.d("extractedValue", "Raw ONNX Output: ${prob.toList()}, Type: ${prob.javaClass.name}")

            //val ans = resultTensor.
//
//            val result = resultTensor.get().value
//            Log.d("OnnxDebug", "Raw ONNX Output: $result")


//            val resultTensor = output.entries.firstOrNull()?.value as? OnnxTensor
//                ?: throw IllegalStateException("No valid OnnxTensor found!")
//
//            val result: Float = resultTensor.floatBuffer.get(0)
//
//            // ✅ Close resources
//            resultValue.close()
//            inputTensor.close()
//
//            result // ✅ Return result

        } catch (e: Exception) {
            e.printStackTrace()
            null
        }

    }

    fun closeSession() {
        try {
            if (::ortSession.isInitialized) ortSession.close()
            if (::ortEnvironment.isInitialized) ortEnvironment.close()
            Log.d("OnnxModelHelper", "ONNX Session closed successfully!")
        } catch (e: Exception) {
            Log.e("OnnxModelHelper", "Error closing ONNX session: ${e.message}")
        }
    }

}