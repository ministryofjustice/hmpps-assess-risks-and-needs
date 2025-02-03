package uk.gov.justice.digital.hmpps.assessrisksandneeds.restclient.api.oasys.section

interface Threshold {
    val standard: Int
}

data class TierThreshold(override val standard: Int, val severe: Int) : Threshold
data class OasysThreshold(override val standard: Int) : Threshold
