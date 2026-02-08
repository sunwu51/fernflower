// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.java.decompiler;

import org.assertj.core.api.Assertions;
import org.jetbrains.java.decompiler.main.CancellationManager;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Timeout;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class CancellableTest {
  public static final int MIN_CALL_NUMBERS = 5;
  private DecompilerTestFixture fixture;

  /*
   * Set individual test duration time limit to 60 seconds.
   * This will help us to test bugs hanging decompiler.
   */
  @Rule
  public Timeout globalTimeout = Timeout.seconds(60);

  @Before
  public void setUp() throws IOException {
    fixture = new DecompilerTestFixture();
    CancellationManager cancellationManager = new CancellationManager() {
      private final AtomicInteger myAtomicInteger = new AtomicInteger(0);

      @Override
      public void checkCanceled() throws CanceledException {
        check();
      }

      @Override
      public void startMethod(String className, String methodName) {

      }

      @Override
      public void finishMethod(String className, String methodName) {

      }


      private void check() {
        if (myAtomicInteger.incrementAndGet() > MIN_CALL_NUMBERS) {
          throw new CanceledException(new IllegalArgumentException());
        }
      }
    };

    Map<String, Object> options = new HashMap<>();
    options.put(IFernflowerPreferences.BYTECODE_SOURCE_MAPPING, "1");
    options.put(IFernflowerPreferences.DUMP_ORIGINAL_LINES, "1");
    options.put(IFernflowerPreferences.IGNORE_INVALID_BYTECODE, "1");
    options.put(IFernflowerPreferences.VERIFY_ANONYMOUS_CLASSES, "1");
    fixture.setUp(options, cancellationManager);
  }

  @After
  public void tearDown() throws IOException {
    fixture.tearDown();
    fixture = null;
  }

  @Test
  public void testCancellablePrimitiveNarrowing() {
    doCancellableTest("pkg/TestPrimitiveNarrowing");
  }

  @Test
  public void testCancellableClassFields() { doCancellableTest("pkg/TestClassFields"); }

  @Test
  public void testCancellableInterfaceFields() { doCancellableTest("pkg/TestInterfaceFields"); }

  @Test
  public void testCancellableClassLambda() { doCancellableTest("pkg/TestClassLambda"); }

  private void doCancellableTest(String testFile, String... companionFiles) {
    ConsoleDecompiler decompiler = fixture.getDecompiler();

    Path classFile = fixture.getTestDataDir().resolve("classes/" + testFile + ".class");
    assertThat(classFile).isRegularFile();
    for (Path file : SingleClassesTest.collectClasses(classFile)) {
      decompiler.addSource(file.toFile());
    }

    for (String companionFile : companionFiles) {
      Path companionClassFile = fixture.getTestDataDir().resolve("classes/" + companionFile + ".class");
      assertThat(companionClassFile).isRegularFile();
      for (Path file : SingleClassesTest.collectClasses(companionClassFile)) {
        decompiler.addSource(file.toFile());
      }
    }
    Assertions.assertThatThrownBy(() -> {
        decompiler.decompileContext();
      })
      .isInstanceOf(CancellationManager.CanceledException.class)
      .matches(exception -> exception.getCause() instanceof IllegalArgumentException);
  }
}
