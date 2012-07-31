package com.github.pfmiles.dropincc.impl.hotcompile;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import com.github.pfmiles.dropincc.DropinccException;

/**
 * Compiles code at runtime, using JDK1.6 compiler API.
 * 
 * @author pf-miles
 * 
 */
public class HotCompileUtil {

    public static CompilationResult compile(String qualifiedName, String parserCode) {
        JavaStringSource source = new JavaStringSource(qualifiedName, parserCode);
        List<JavaStringSource> ss = Arrays.asList(source);
        File dir = new File(HotCompileConstants.TARGETDIR);
        if (!dir.exists())
            dir.mkdirs();
        List<String> options = Arrays.asList("-d", HotCompileConstants.TARGETDIR, "-classpath", HotCompileConstants.CLASSPATH);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = null;
        try {
            fileManager = compiler.getStandardFileManager(null, Locale.getDefault(), Charset.forName("UTF-8"));
            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
            StringWriter out = new StringWriter();
            CompilationTask task = compiler.getTask(out, fileManager, diagnostics, options, null, ss);
            boolean sucess = task.call();
            if (!sucess) {
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
                    out.append("Error on line " + diagnostic.getLineNumber() + " in " + diagnostic).append('\n');
                }
                return new CompilationResult(out.toString());
            }
        } finally {
            try {
                fileManager.close();
            } catch (IOException e) {
                throw new DropinccException(e);
            }
        }
        // every parser class should be loaded by a new specific class loader
        HotCompileClassLoader loader = new HotCompileClassLoader(HotCompileUtil.class.getClassLoader());
        Class<?> cls = null;
        try {
            cls = loader.loadClass(qualifiedName);
        } catch (ClassNotFoundException e) {
            throw new DropinccException(e);
        }
        return new CompilationResult(cls, loader);
    }
}
