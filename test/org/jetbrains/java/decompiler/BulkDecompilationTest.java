// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.java.decompiler;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.jetbrains.java.decompiler.DecompilerTestFixture.assertFilesEqual;

public class BulkDecompilationTest {
  private DecompilerTestFixture fixture;

  @Before
  public void setUp() throws IOException {
    fixture = new DecompilerTestFixture();
    fixture.setUp(Collections.<String, Object>emptyMap());
  }

  @After
  public void tearDown() throws IOException {
    fixture.tearDown();
    fixture = null;
  }

  @Test
  public void testDirectory() {
    Path classes = fixture.getTempDir().resolve("classes");
    unpack(fixture.getTestDataDir().resolve("bulk.jar"), classes);

    ConsoleDecompiler decompiler = fixture.getDecompiler();
    decompiler.addSource(classes.toFile());
    decompiler.decompileContext();

    assertFilesEqual(fixture.getTestDataDir().resolve("bulk"), fixture.getTargetDir());
  }

  @Test
  public void testJar() {
    doTestJar("bulk");
  }

  @Test
  public void testKtJar() {
    doTestJar("kt25937");
  }

  @Test
  public void testObfuscated() {
    doTestJar("obfuscated");
  }

  private void doTestJar(String name) {
    ConsoleDecompiler decompiler = fixture.getDecompiler();
    String jarName = name + ".jar";
    decompiler.addSource(fixture.getTestDataDir().resolve(jarName).toFile());
    decompiler.decompileContext();

    Path unpacked = fixture.getTempDir().resolve("unpacked");
    unpack(fixture.getTargetDir().resolve(jarName), unpacked);

    assertFilesEqual(fixture.getTestDataDir().resolve(name), unpacked);
  }

  private static void unpack(Path archive, Path targetDir) {
    try (ZipFile zip = new ZipFile(archive.toFile())) {
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory()) {
          if (entry.getName().contains("..")) throw new IllegalArgumentException("Invalid entry: " + entry.getName());
          Path file = targetDir.resolve(entry.getName());
          Files.createDirectories(file.getParent());
          try (InputStream in = zip.getInputStream(entry)) {
            Files.copy(in, file);
          }
        }
      }
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
