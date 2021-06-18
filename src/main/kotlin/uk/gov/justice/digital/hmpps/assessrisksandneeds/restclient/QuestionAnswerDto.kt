package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient

data class QuestionAnswerDto(
  val refQuestionCode: String? = null,
  val questionText: String? = null,
  val refAnswerCode: String? = null,
  val staticText: String? = null,
  val freeFormText: String? = null,
)
