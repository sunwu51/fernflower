// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.HashMap;
import java.util.Map;

public class KotlinSingleClassesTest extends SingleClassesTestBase {
  /*
   * Set individual test duration time limit to 60 seconds.
   * This will help us to test bugs hanging decompiler.
   */
  @Rule
  public Timeout globalTimeout = Timeout.seconds(60);

  @Override
  protected Map<String, Object> getDecompilerOptions() {
    Map<String, Object> options = new HashMap<>();
    options.put(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1");
    options.put(IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1");
    options.put(IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1");
    options.put(IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1");
    options.put(IFernflowerPreferences.CONVERT_PATTERN_SWITCH, "1");
    options.put(IFernflowerPreferences.CONVERT_RECORD_PATTERN, "1");
    options.put(IFernflowerPreferences.INLINE_SIMPLE_LAMBDAS, "1");
    options.put(IFernflowerPreferences.CHECK_CLOSABLE_INTERFACE, "0");
    options.put(IFernflowerPreferences.HIDE_RECORD_CONSTRUCTOR_AND_GETTERS, "0");
    options.put(IFernflowerPreferences.REMOVE_SYNTHETIC, "0");
    //IFernflowerPreferences.INCLUDE_ENTIRE_CLASSPATH, "1"
    return options;
  }
@Test public void testKotlinConstructor() { doTest("pkg/TestKotlinConstructorKt"); }
  @Test public void testKotlinDefaultValueConstructor() {doTest("pkg/KotlinDefaultValue"); }
}
