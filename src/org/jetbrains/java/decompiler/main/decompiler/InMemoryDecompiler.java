package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IFernflowerPreferences;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.jar.Manifest;

public class InMemoryDecompiler {

  public static void main(String[] args) throws IOException {
  }

  /**
   *
   * @param classes className to bytecode, class name is in format: com.example.A
   * @param entrypoint com.example.A
   * @param options
   * @param logger
   * @param hookForGetInnerClass
   * @return
   */
  public static String decompileClass(Map<String, byte[]> classes, String entrypoint, Map<String, Object> options, IFernflowerLogger logger, Function<String, Object> hookForGetInnerClass) {
    if (classes.get(entrypoint) == null) {
      throw new IllegalArgumentException("classBytes must not be null");
    }
    Map<String, Object> safeOptions = options == null ? new HashMap<>() : options;
    IFernflowerLogger safeLogger = logger == null ? new SilentLogger() : logger;
    InMemoryResultSaver saver = new InMemoryResultSaver();

    IBytecodeProvider provider = (externalPath, internalPath) -> {
      throw new IOException("Bytecode provider is not available for in-memory decompilation");
    };
    Fernflower fernflower = new Fernflower(provider, saver, safeOptions, safeLogger);
    try {
      for (Map.Entry<String, byte[]> stringEntry : classes.entrySet()) {
        String className = stringEntry.getKey().replace(".", "/");
        className = className.endsWith(".class") ? className : className + ".class";
        byte[] classBytes = stringEntry.getValue();
        fernflower.addData("", className, classBytes, true);
      }
      if (hookForGetInnerClass != null) {
        fernflower.addHookWhenGet((path) -> {
          String name = path.replace(".class", "").replace("/", ".");
          return hookForGetInnerClass.apply(name);
        });
      }
      fernflower.decompileContext();
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to load in-memory class data", e);
    }
    finally {
      fernflower.clearContext();
    }
    return saver.getSingleResult(entrypoint);
  }


  private static final class InMemoryResultSaver implements IResultSaver {
    private final Map<String, String> results = new LinkedHashMap<>();

    @Override
    public void saveFolder(String path) {
      // No-op for in-memory output.
    }

    @Override
    public void copyFile(String source, String path, String entryName) {
      // No-op for in-memory output.
    }

    @Override
    public void saveClassFile(String path, String qualifiedName, String entryName, String content, int[] mapping) {
      if (content != null) {
        results.put(entryName, content);
      }
    }

    @Override
    public void createArchive(String path, String archiveName, Manifest manifest) {
      // No-op for in-memory output.
    }

    @Override
    public void saveDirEntry(String path, String archiveName, String entryName) {
      // No-op for in-memory output.
    }

    @Override
    public void copyEntry(String source, String path, String archiveName, String entryName) {
      // No-op for in-memory output.
    }

    @Override
    public void saveClassEntry(String path, String archiveName, String qualifiedName, String entryName, String content) {
      if (content != null) {
        results.put(entryName, content);
      }
    }

    @Override
    public void closeArchive(String path, String archiveName) {
      // No-op for in-memory output.
    }

    public String getSingleResult(String className) {
      String sourceName = className.replace(".", "/") + ".java";
      return results.get(sourceName);
    }
  }

  private static final class SilentLogger extends IFernflowerLogger {
    @Override
    public void writeMessage(String message, Severity severity) {
      // Intentionally blank.
    }

    @Override
    public void writeMessage(String message, Severity severity, Throwable t) {
      // Intentionally blank.
    }
  }
}
