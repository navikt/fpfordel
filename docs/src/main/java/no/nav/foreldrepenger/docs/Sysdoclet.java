package no.nav.foreldrepenger.docs;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarFile;

import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticCollector;
import javax.tools.DocumentationTool;
import javax.tools.DocumentationTool.DocumentationTask;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import jdk.javadoc.doclet.Doclet;
import jdk.javadoc.doclet.DocletEnvironment;
import jdk.javadoc.doclet.Reporter;
import no.nav.vedtak.feil.doc.FeilmeldingDoclet;
import no.nav.vedtak.felles.db.doc.JdbcDoclet;
import no.nav.vedtak.felles.integrasjon.felles.ws.doc.WebServiceDoclet;
import no.nav.vedtak.felles.prosesstask.doc.ProsessTaskDoclet;
import no.nav.vedtak.konfig.doc.KonfigverdiDoclet;

/** Aggregert doclet for å kunne håndtere alle sammen samtidig. */
public class Sysdoclet implements Doclet {

    static {
        // database connection settings
        System.setProperty("doc.plugin.jdbc.url", System.getProperty("doc.plugin.jdbc.url", "jdbc:oracle:thin:@localhost:1521:XE"));
        System.setProperty("doc.plugin.jdbc.dslist", System.getProperty("doc.plugin.jdbc.dslist", "defaultDS,dvhDS"));

        System.setProperty("doc.plugin.jdbc.db.migration.defaultDS", "./migreringer/src/main/resources/db/migration/defaultDS");
        System.setProperty("doc.plugin.jdbc.username.defaultDS", "fpsak");

        System.setProperty("doc.plugin.jdbc.db.migration.dvhDS", "./migreringer/src/main/resources/db/migration/dvhDS");
        System.setProperty("doc.plugin.jdbc.username.dvhDS", "fpsak_hist");
    }

    private List<Doclet> doclets = Arrays.asList(
        new FeilmeldingDoclet(),
        new JdbcDoclet(),
        new WebServiceDoclet(),
        new KonfigverdiDoclet(),
        new ProsessTaskDoclet()
        );

    @Override
    public void init(Locale locale, Reporter reporter) {
        doclets.forEach(d -> d.init(locale, reporter));
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public Set<? extends Option> getSupportedOptions() {
        return Collections.emptySet();
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean run(DocletEnvironment docEnv) {
        System.out.println("running Sysdoclets");
        return doclets.stream().allMatch(d -> {
            return d.run(docEnv);
        });
    }

    /** NB: Antar workingdir er satt til root av multi-module maven project. */
    public static void main(String[] args) throws IOException {
        String sourceDir = args[0];
        String destDir = args[1];
        System.setProperty("destDir", destDir);

        DocumentationTool documentationTool = ToolProvider.getSystemDocumentationTool();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();

        try (StandardJavaFileManager fm = compiler.getStandardFileManager(diagnostics, null, Charset.forName("UTF-8"))) {
            Set<JavaFileObject> fileObjects = new LinkedHashSet<>();

            fm.getJavaFileObjectsFromPaths(findSourceFiles(Paths.get(sourceDir))).forEach(fileObjects::add);

            Iterable<? extends Path> locationAsPaths = fm.getLocationAsPaths(StandardLocation.CLASS_PATH);
            Set<String> packageNames = new TreeSet<>();
            for (Path u : locationAsPaths) {
                String pathString = u.toString();
                // scanner maven classpath og class dirs for source files.
                if (pathString.contains("-sources.jar")) {
                    // maven source jar dependency
                    try (JarFile jarFile = new JarFile(u.toFile())) {
                        jarFile.stream().forEach(je -> {
                            if (je.isDirectory()) {
                                packageNames.add(je.getName().replaceAll("[\\/]", "."));
                            }
                        });
                    }
                } else if (pathString.matches("^.+target.classes.?$")) {
                    String srcDir = pathString.replaceAll("target.classes.*", "src/main/java");
                    File dir = new File(srcDir);
                    if (dir.exists()) {
                        fm.getJavaFileObjectsFromPaths(findSourceFiles(dir.toPath())).forEach(fileObjects::add);
                    }
                }
            }
            for (String pkg : packageNames) {
                fm.list(StandardLocation.CLASS_PATH, pkg, EnumSet.of(Kind.SOURCE), true).forEach(fileObjects::add);
            }

            DocumentationTask task = documentationTool.getTask(null, null, null, Sysdoclet.class, Arrays.asList("-Xmaxerrs", "1000", "-Xmaxwarns", "1000"),
                fileObjects);
            task.call();
        }

    }

    private static List<Path> findSourceFiles(Path start) throws IOException {
        System.out.println("Scanning from: " + start);
        List<Path> sourceFiles = new ArrayList<>(1000);
        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                boolean hiddenDir = dir.getFileName().startsWith(".") && !dir.getFileName().endsWith(".");
                if (dir.endsWith("target") || dir.endsWith("test") || dir.endsWith("testutil") || dir.endsWith(".git") || hiddenDir
                    || dir.endsWith("node_modules")) {
                    return FileVisitResult.SKIP_SUBTREE;
                } else if (dir.endsWith("./web/server/")) {
                    // gammel web server modul
                    return FileVisitResult.SKIP_SUBTREE;
                } else {
                    return super.preVisitDirectory(dir, attrs);
                }
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                if (file.toString().endsWith(".java")) {
                    sourceFiles.add(file);
                }
                return super.visitFile(file, attrs);
            }
        });
        return sourceFiles;
    }

}
