package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.util.EnumMap

class View {
  open class CrsProvider
  class Hmpps : CrsProvider()

  companion object {
    var MAPPING: MutableMap<Role, Class<*>> = EnumMap(Role::class.java)

    init {
      MAPPING[Role.ROLE_PROBATION] = Hmpps::class.java
      MAPPING[Role.ROLE_OFFENDER_CATEGORISATION_RO] = Hmpps::class.java
      MAPPING[Role.ROLE_OFFENDER_RISK_RO] = Hmpps::class.java
      MAPPING[Role.ROLE_RISK_RESETTLEMENT_PASSPORT_RO] = Hmpps::class.java
      MAPPING[Role.ROLE_RISK_INTEGRATIONS_RO] = Hmpps::class.java
      MAPPING[Role.ROLE_CRS_PROVIDER] = CrsProvider::class.java
    }
  }

  enum class Role {
    ROLE_PROBATION,
    ROLE_CRS_PROVIDER,
    ROLE_OFFENDER_CATEGORISATION_RO,
    ROLE_OFFENDER_RISK_RO,
    ROLE_RISK_RESETTLEMENT_PASSPORT_RO,
    ROLE_RISK_INTEGRATIONS_RO,
  }

  open class RiskView
  class SingleRisksView : RiskView()
  class AllRisksView : RiskView()
}
