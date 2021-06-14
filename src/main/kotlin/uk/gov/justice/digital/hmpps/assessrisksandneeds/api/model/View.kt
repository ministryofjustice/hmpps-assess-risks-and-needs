package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

class View {
  open class CrsProvider
  class Probation : CrsProvider()

  companion object {
    var MAPPING: MutableMap<Role, Class<*>> = HashMap()

    init {
      MAPPING[Role.ROLE_PROBATION] = Probation::class.java
      MAPPING[Role.ROLE_CRS_PROVIDER] = CrsProvider::class.java
    }
  }

  enum class Role {
    ROLE_PROBATION, ROLE_CRS_PROVIDER
  }
}
