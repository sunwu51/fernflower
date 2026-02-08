// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class JADNamingTest extends SingleClassesTestBase {

    @Override
    protected Map<String, Object> getDecompilerOptions() {
      Map<String, Object> options = new HashMap<>();
      options.put(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1");
      options.put(IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1");
      options.put(IFernflowerPreferences.USE_JAD_VARNAMING, "1");
      return options;
    }

    @Test public void testClassFields() { doTest("pkg/TestJADNaming"); }

}
