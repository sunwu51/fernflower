package org.jetbrains.java.decompiler.main.decompiler;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IFernflowerLogger;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.jar.Manifest;

public class InMemoryDecompiler {

  public static void main(String[] args) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(args[0]));
    System.out.println(decompileClass(bytes));
  }

  public static String decompileClass(byte[] classBytes) {
    return decompileClass(classBytes, null, null);
  }

  public static String decompileClass(byte[] classBytes, Map<String, Object> options, IFernflowerLogger logger) {
    if (classBytes == null) {
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
      fernflower.addData("", "InMemory.class", classBytes, true);
      fernflower.decompileContext();
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to load in-memory class data", e);
    }
    finally {
      fernflower.clearContext();
    }

    return saver.getSingleResult();
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

    public String getSingleResult() {
      if (results.isEmpty()) {
        return null;
      }
      return results.values().iterator().next();
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
