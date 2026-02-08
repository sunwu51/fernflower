// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.java.decompiler.util;

import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger.Severity;
import org.jetbrains.java.decompiler.struct.StructContext;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
public final class ClasspathScanner {

    public static void addAllClasspath(StructContext ctx) {
      Set<String> found = new HashSet<>();
      String[] props = { System.getProperty("java.class.path"), System.getProperty("sun.boot.class.path") };
      for (String prop : props) {
        if (prop == null)
          continue;

        for (final String path : prop.split(File.pathSeparator)) {
          File file = new File(path);
          if (found.contains(file.getAbsolutePath()))
            continue;

          if (file.exists() && (file.getName().endsWith(".class") || file.getName().endsWith(".jar"))) {
            DecompilerContext.getLogger().writeMessage("Adding File to context from classpath: " + file, Severity.INFO);
            ctx.addSpace(file, false);
            found.add(file.getAbsolutePath());
          }
        }
      }

      addAllModulePath(ctx);
    }

    private static void addAllModulePath(StructContext ctx) {
      try {
        Class<?> moduleFinderClass = Class.forName("java.lang.module.ModuleFinder");
        Class<?> moduleReferenceClass = Class.forName("java.lang.module.ModuleReference");
        Class<?> moduleReaderClass = Class.forName("java.lang.module.ModuleReader");

        Object finder = moduleFinderClass.getMethod("ofSystem").invoke(null);
        @SuppressWarnings("unchecked")
        Set<Object> modules = (Set<Object>)moduleFinderClass.getMethod("findAll").invoke(finder);

        Method descriptorMethod = moduleReferenceClass.getMethod("descriptor");
        Method nameMethod = null;
        Method openMethod = moduleReferenceClass.getMethod("open");
        Method listMethod = moduleReaderClass.getMethod("list");
        Method readMethod = moduleReaderClass.getMethod("read", String.class);
        Method closeMethod = moduleReaderClass.getMethod("close");

        for (Object module : modules) {
          Object descriptor = descriptorMethod.invoke(module);
          if (nameMethod == null) {
            nameMethod = descriptor.getClass().getMethod("name");
          }
          String name = (String)nameMethod.invoke(descriptor);

          Object reader = openMethod.invoke(module);
          DecompilerContext.getLogger().writeMessage("Reading Module: " + name, Severity.INFO);

          @SuppressWarnings("unchecked")
          Stream<String> stream = (Stream<String>)listMethod.invoke(reader);
          try {
            stream.forEach(cls -> {
              if (!cls.endsWith(".class") || cls.contains("module-info.class")) {
                return;
              }

              DecompilerContext.getLogger().writeMessage("  " + cls, Severity.INFO);
              try {
                @SuppressWarnings("unchecked")
                Optional<ByteBuffer> bb = (Optional<ByteBuffer>)readMethod.invoke(reader, cls);
                if (!bb.isPresent()) {
                  DecompilerContext.getLogger().writeMessage("    Error Reading Class: " + cls, Severity.ERROR);
                  return;
                }

                ByteBuffer buffer = bb.get();
                byte[] data;
                if (buffer.hasArray()) {
                  data = buffer.array();
                } else {
                  data = new byte[buffer.remaining()];
                  buffer.get(data);
                }
                ctx.addData(name, cls, data, false);
              } catch (Exception e) {
                DecompilerContext.getLogger().writeMessage("    Error Reading Class: " + cls, e);
              }
            });
          }
          finally {
            try {
              stream.close();
            } catch (Exception ignored) {
              // ignore
            }
            try {
              closeMethod.invoke(reader);
            } catch (Exception ignored) {
              // ignore
            }
          }
        }
      }
      catch (ClassNotFoundException e) {
        // Java 8 has no module system
      }
      catch (Exception e) {
        DecompilerContext.getLogger().writeMessage("Error loading modules", e);
      }
    }
}
