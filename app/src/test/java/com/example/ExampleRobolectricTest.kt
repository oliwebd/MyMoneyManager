package com.example

import org.junit.Assert.assertEquals
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun testAppNameConstant() {
    val expectedName = "myMoneymanager"
    assertEquals("myMoneymanager", expectedName)
  }
}
