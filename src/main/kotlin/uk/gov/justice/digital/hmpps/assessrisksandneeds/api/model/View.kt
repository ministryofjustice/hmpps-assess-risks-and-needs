package uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model

import java.util.EnumMap

class View {
  open class CrsProvider
  class Hmpps : CrsProvider()

  companion object {
    var MAPPING: MutableMap<Role, Class<*>> = EnumMap(Role::class.java)

    init {
      MAPPING[Role.ROLE_PROBATION] = Hmpps::class.java
      MAPPING[Role.ROLE_PRISON] = Hmpps::class.java
      MAPPING[Role.ROLE_CRS_PROVIDER] = CrsProvider::class.java
    }
  }

  enum class Role {
    ROLE_PROBATION, ROLE_CRS_PROVIDER, ROLE_PRISON
  }

  open class RiskView
  class SingleRisksView : RiskView()
  class AllRisksView : RiskView()
}
