package uk.gov.justice.digital.hmpps.assessrisksandneeds.config

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.io.Resource
import uk.gov.justice.digital.hmpps.assessrisksandneeds.services.OnnxCalculatorServiceImpl
import java.io.File

@Profile("onnx-rsr")
@Configuration
class ONNXRuntimeConfig(@Value("\${onnx-predictors.onnx-path}") private val onnxFile: Resource, private val objectMapper: ObjectMapper) {

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @Bean
  fun getOnnxEnvironment(): OrtEnvironment {
    return OrtEnvironment.getEnvironment()
  }

  @Bean
  fun getOnnxSession(): OrtSession {
    log.info("Creating ONNX Runtime Session from file ${onnxFile.file.absolutePath}")
    val session = getOnnxEnvironment().createSession(onnxFile.file.absolutePath)
    OnnxCalculatorServiceImpl.log.info("Loaded ONNX Runtime file from ${onnxFile.file.absolutePath} with ${session.numInputs} inputs and ${session.numOutputs}")
    return session
  }

  @Bean
  fun getSupportedOffenceCodes(@Value("\${onnx-predictors.offence-codes-path}") offenceCodesPath: Resource): List<Int> {
    return objectMapper.readValue(File(offenceCodesPath.file.path))
  }
}
