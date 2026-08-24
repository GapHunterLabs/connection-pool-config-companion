package dev.gaphunter.connectionpoolconfigcompanion.inspection

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class UnsafePoolConfigInspectionTest : BasePlatformTestCase() {

    override fun setUp() {
        super.setUp()
        myFixture.enableInspections(UnsafePoolConfigInspection::class.java)
    }

    fun `test a connectionTimeout below the minimum produces a warning`() {
        myFixture.configureByText(
            "application.properties",
            "spring.datasource.hikari.connection-timeout=100\n",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.any { it.description?.contains("250ms") == true })
    }

    fun `test a healthy config produces no warning`() {
        myFixture.configureByText(
            "application.properties",
            "spring.datasource.hikari.connection-timeout=30000\n",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("hikari", ignoreCase = true) == true })
    }

    fun `test a non-config file is never scanned`() {
        myFixture.configureByText(
            "Config.java",
            "String x = \"connectionTimeout=100\";",
        )
        val highlights = myFixture.doHighlighting()
        assertTrue(highlights.none { it.description?.contains("250ms") == true })
    }
}
