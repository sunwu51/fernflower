// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.java.decompiler;

import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.util.HashMap;
import java.util.Map;

public class DisabledDebugNameSingleClassesTest extends SingleClassesTestBase {

  @Rule
  public Timeout globalTimeout = Timeout.seconds(60);

  @Override
  protected Map<String, Object> getDecompilerOptions() {
    Map<String, Object> options = new HashMap<>();
    options.put(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1");
    options.put(IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1");
    options.put(IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1");
    options.put(IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1");
    options.put(IFernflowerPreferences.INLINE_SIMPLE_LAMBDAS, "1");
    options.put(IFernflowerPreferences.CHECK_CLOSABLE_INTERFACE, "0");
    options.put(IFernflowerPreferences.HIDE_RECORD_CONSTRUCTOR_AND_GETTERS, "0");
    options.put(IFernflowerPreferences.REMOVE_BRIDGE, "0");
    options.put(IFernflowerPreferences.REMOVE_SYNTHETIC, "0");
    options.put(IFernflowerPreferences.DECOMPILE_INNER, "1");
    options.put(IFernflowerPreferences.DECOMPILE_ASSERTIONS, "0");
    options.put(IFernflowerPreferences.HIDE_EMPTY_SUPER, "0");
    options.put(IFernflowerPreferences.HIDE_DEFAULT_CONSTRUCTOR, "0");
    options.put(IFernflowerPreferences.DECOMPILE_GENERIC_SIGNATURES, "1");
    options.put(IFernflowerPreferences.NO_EXCEPTIONS_RETURN, "1");
    options.put(IFernflowerPreferences.DECOMPILE_ENUM, "0");
    options.put(IFernflowerPreferences.LITERALS_AS_IS, "1");
    options.put(IFernflowerPreferences.REMOVE_GET_CLASS_NEW, "0");
    options.put(IFernflowerPreferences.ASCII_STRING_CHARACTERS, "1");
    options.put(IFernflowerPreferences.BOOLEAN_TRUE_ONE, "0");
    options.put(IFernflowerPreferences.UNDEFINED_PARAM_TYPE_OBJECT, "1");
    options.put(IFernflowerPreferences.USE_DEBUG_VAR_NAMES, "0");
    options.put(IFernflowerPreferences.USE_METHOD_PARAMETERS, "1");
    options.put(IFernflowerPreferences.REMOVE_EMPTY_RANGES, "1");
    options.put(IFernflowerPreferences.FINALLY_DEINLINE, "1");
    options.put(IFernflowerPreferences.RENAME_ENTITIES, "0");
    options.put(IFernflowerPreferences.IDEA_NOT_NULL_ANNOTATION, "1");
    options.put(IFernflowerPreferences.LAMBDA_TO_ANONYMOUS_CLASS, "0");
    options.put(IFernflowerPreferences.CONVERT_RECORD_PATTERN, "1");
    options.put(IFernflowerPreferences.CONVERT_PATTERN_SWITCH, "0");
    options.put(IFernflowerPreferences.MAX_DIRECT_NODES_COUNT, 20000);
    options.put(IFernflowerPreferences.MAX_DIRECT_VARIABLE_NODE_COUNT, 30000);
    return options;
  }

  @Test
  public void testWithoutDebugNames() { doTest("pkg/TestWithoutDebugName"); }
}
