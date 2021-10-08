package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api

import java.time.LocalDateTime

data class SectionAnswersDto(

  val assessmentId: Long,

  val sections: Map<String?, Collection<QuestionAnswerDto>>,

  val assessedOn: LocalDateTime?

)
