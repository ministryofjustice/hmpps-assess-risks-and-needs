package uk.gov.justice.digital.hmpps.assessrisksandneeds.controllers

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController

@RestController
class SupplementaryRisksController {

  @RequestMapping(path = ["/risks/supplementary"], method = [RequestMethod.GET])
  fun getSupplementaryRisk() {
    TODO()
  }
}