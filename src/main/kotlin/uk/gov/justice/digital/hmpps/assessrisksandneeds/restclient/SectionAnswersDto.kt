package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

data class SectionAnswersDto(

  val assessmentId: Long,

  val sections: Map<String?, Collection<QuestionAnswerDto>>

)
