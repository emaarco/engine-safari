package de.emaarco.example

import de.emaarco.example.konsist.BasicCodingGuidelinesTest
import de.emaarco.example.konsist.EngineNeutralArchitectureTest
import de.emaarco.example.konsist.EngineNeutralArchitectureTest.Engine
import de.emaarco.example.konsist.HexagonalArchitectureTest
import org.junit.jupiter.api.Nested

class KonsistArchitectureTest {

    private val rootPackage = "de.emaarco.example"

    @Nested
    inner class Architecture : HexagonalArchitectureTest(rootPackage)

    @Nested
    inner class CodingGuidelines : BasicCodingGuidelinesTest(rootPackage)

    @Nested
    inner class EngineArchitecture : EngineNeutralArchitectureTest(rootPackage, Engine.CIB_SEVEN)
}
