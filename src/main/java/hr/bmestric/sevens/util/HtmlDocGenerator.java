package hr.bmestric.sevens.util;

import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class HtmlDocGenerator {

    private static final Logger logger = LoggerFactory.getLogger(HtmlDocGenerator.class);

    private static final String DEFAULT_OUTPUT_DIR = "docs/html";
    private static final String BASE_PACKAGE = "hr.bmestric.sevens";

    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void generateAllDocumentation() {
        try {
            Path outputPath = Paths.get(DEFAULT_OUTPUT_DIR);
            Files.createDirectories(outputPath);

            logger.info("Starting HTML documentation generation...");
            System.out.println("🔍 Scanning project classes using Reflection API...");

            Class<?>[] classes = discoverAllProjectClasses();

            for (Class<?> clazz : classes) {
                generateClassDocumentation(clazz, outputPath);
            }

            generateIndexPage(classes, outputPath);
            generateStylesheet(outputPath);

            logger.info("Documentation generation finished.");
            System.out.println("✅ HTML documentation generated successfully!");
            System.out.println("📁 Location: " + outputPath.toAbsolutePath());
            System.out.println("🌐 Open: " + outputPath.resolve("index.html").toAbsolutePath());

        } catch (Exception e) {
            logger.error("Documentation generation failed", e);
            throw new RuntimeException(e);
        }
    }

    private Class<?>[] discoverAllProjectClasses() {
        try {
            Reflections reflections = new Reflections(
                    new org.reflections.util.ConfigurationBuilder()
                            .forPackages(BASE_PACKAGE)
                            .addScanners(org.reflections.scanners.Scanners.SubTypes)
            );

            Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

            logger.info("Found {} classes before filtering", allClasses.size());

            Class<?>[] result = allClasses.stream()
                    .filter(c -> c.getPackageName().startsWith(BASE_PACKAGE))
                    .filter(c -> !c.isAnonymousClass())
                    .filter(c -> !c.isSynthetic())
                    .filter(c -> !c.isLocalClass())
                    .filter(c -> !c.getName().contains("module-info"))
                    .filter(c -> !c.getName().contains("$"))
                    .toArray(Class<?>[]::new);

            logger.info("Found {} classes after filtering", result.length);

            // Fallback to hardcoded list if no classes found
            if (result.length == 0) {
                logger.warn("Reflections found no classes, falling back to hardcoded list");
                return getHardcodedClassList();
            }

            return result;
        } catch (Exception e) {
            logger.error("Failed to discover classes dynamically, using fallback", e);
            return getHardcodedClassList();
        }
    }

    private Class<?>[] getHardcodedClassList() {
        try {
            return new Class<?>[]{
                    // Engine
                    Class.forName("hr.bmestric.sevens.engine.GameEngine"),
                    Class.forName("hr.bmestric.sevens.engine.MoveValidator"),
                    Class.forName("hr.bmestric.sevens.engine.TrickResolver"),
                    Class.forName("hr.bmestric.sevens.engine.MoveValidationResult"),
                    Class.forName("hr.bmestric.sevens.engine.InvalidMoveException"),
                    // Model
                    Class.forName("hr.bmestric.sevens.model.GameState"),
                    Class.forName("hr.bmestric.sevens.model.Player"),
                    Class.forName("hr.bmestric.sevens.model.Card"),
                    Class.forName("hr.bmestric.sevens.model.Hand"),
                    Class.forName("hr.bmestric.sevens.model.Deck"),
                    Class.forName("hr.bmestric.sevens.model.Trick"),
                    // Events
                    Class.forName("hr.bmestric.sevens.events.GameEventBus"),
                    Class.forName("hr.bmestric.sevens.events.GameEvent"),
                    Class.forName("hr.bmestric.sevens.events.CardPlayedEvent"),
                    Class.forName("hr.bmestric.sevens.events.TrickWonEvent"),
                    Class.forName("hr.bmestric.sevens.events.StateChangedEvent"),
                    // Persistence
                    Class.forName("hr.bmestric.sevens.persistence.ObjectStorageService"),
                    Class.forName("hr.bmestric.sevens.persistence.AsyncStorageService"),
                    // Network
                    Class.forName("hr.bmestric.sevens.network.rmi.RemoteGameEngineImpl"),
                    Class.forName("hr.bmestric.sevens.network.rmi.RmiGameServer"),
                    Class.forName("hr.bmestric.sevens.network.rmi.RmiClientApplication"),
                    Class.forName("hr.bmestric.sevens.network.tcp.TcpChatServer"),
                    Class.forName("hr.bmestric.sevens.network.tcp.TcpChatClient"),
                    Class.forName("hr.bmestric.sevens.network.chat.ChatServiceImpl"),
                    // Config
                    Class.forName("hr.bmestric.sevens.config.GameConfiguration"),
                    Class.forName("hr.bmestric.sevens.config.ConfigurationReader"),
                    Class.forName("hr.bmestric.sevens.config.XmlConfigProvider"),
                    // Session
                    Class.forName("hr.bmestric.sevens.session.LocalGameSession"),
                    Class.forName("hr.bmestric.sevens.session.RmiGameSession"),
                    Class.forName("hr.bmestric.sevens.session.RmiChatSession"),
                    Class.forName("hr.bmestric.sevens.session.TcpChatSession"),
                    // Utilities
                    Class.forName("hr.bmestric.sevens.util.ConfigSyncUtil"),
                    Class.forName("hr.bmestric.sevens.util.ThreadingHelper"),
                    // UI Services
                    Class.forName("hr.bmestric.sevens.ui.service.AsyncRmiService"),
                    Class.forName("hr.bmestric.sevens.ui.service.UiDataPreparationService"),
                    Class.forName("hr.bmestric.sevens.ui.service.FxDialogService"),
                    // App Context
                    Class.forName("hr.bmestric.sevens.AppContext")
            };
        } catch (ClassNotFoundException e) {
            logger.error("Failed to load hardcoded classes", e);
            return new Class<?>[0];
        }
    }

    private void generateClassDocumentation(Class<?> clazz, Path outputDir) throws IOException {

        logger.info("Documenting class: {}", clazz.getName());

        StringBuilder html = new StringBuilder();

        html.append("""
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>%s - API Documentation</title>
                    <link rel="stylesheet" href="style.css">
                </head>
                <body>
                """.formatted(clazz.getSimpleName()));

        /* ---------- HEADER ---------- */
        html.append("""
                <div class="header">
                    <h1>%s</h1>
                    <p class="package">Package: %s</p>
                    <p class="modifiers">%s class</p>
                </div>
                """.formatted(
                clazz.getSimpleName(),
                clazz.getPackageName(),
                Modifier.toString(clazz.getModifiers())
        ));

        html.append("""
                <div class="nav">
                    <a href="index.html">← Back to Index</a>
                    <span class="timestamp">Generated: %s</span>
                </div>
                """.formatted(LocalDateTime.now().format(TIMESTAMP_FORMAT)));

        html.append("<div class=\"content\">");

        /* ---------- CLASS HIERARCHY ---------- */
        html.append("<div class=\"section\"><h2>Class Hierarchy</h2>");

        if (clazz.getSuperclass() != null && clazz.getSuperclass() != Object.class) {
            html.append("<p>Extends: <code>")
                    .append(clazz.getSuperclass().getSimpleName())
                    .append("</code></p>");
        }

        if (clazz.getInterfaces().length > 0) {
            html.append("<p>Implements:</p><ul>");
            for (Class<?> i : clazz.getInterfaces()) {
                html.append("<li><code>").append(i.getSimpleName()).append("</code></li>");
            }
            html.append("</ul>");
        }

        html.append("</div>");

        /* ---------- FIELDS ---------- */
        Field[] fields = clazz.getDeclaredFields();
        if (fields.length > 0) {
            html.append("""
                    <div class="section">
                    <h2>Fields (%d)</h2>
                    <table class="members">
                    <thead><tr><th>Modifier</th><th>Type</th><th>Name</th></tr></thead>
                    <tbody>
                    """.formatted(fields.length));

            for (Field f : fields) {
                if (!f.isSynthetic()) {
                    html.append("""
                            <tr>
                                <td><code>%s</code></td>
                                <td><code>%s</code></td>
                                <td><strong>%s</strong></td>
                            </tr>
                            """.formatted(
                            Modifier.toString(f.getModifiers()),
                            f.getType().getSimpleName(),
                            f.getName()
                    ));
                }
            }
            html.append("</tbody></table></div>");
        }

        /* ---------- CONSTRUCTORS ---------- */
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length > 0) {
            html.append("<div class=\"section\"><h2>Constructors</h2>");

            for (Constructor<?> c : constructors) {
                html.append("<div class=\"method\"><h3><code>")
                        .append(Modifier.toString(c.getModifiers()))
                        .append("</code> ")
                        .append(clazz.getSimpleName())
                        .append("(");

                appendParameters(html, c.getParameters());

                html.append(")</h3></div>");
            }
            html.append("</div>");
        }

        /* ---------- METHODS ---------- */
        Method[] methods = clazz.getDeclaredMethods();
        if (methods.length > 0) {
            html.append("<div class=\"section\"><h2>Methods</h2>");

            for (Method m : methods) {
                if (!m.isSynthetic()) {
                    html.append("<div class=\"method\"><h3>");

                    for (Annotation a : m.getDeclaredAnnotations()) {
                        html.append("<span class=\"annotation\">@")
                                .append(a.annotationType().getSimpleName())
                                .append("</span> ");
                    }

                    html.append("<code>")
                            .append(Modifier.toString(m.getModifiers()))
                            .append("</code> ")
                            .append("<code>")
                            .append(m.getReturnType().getSimpleName())
                            .append("</code> ")
                            .append("<strong>")
                            .append(m.getName())
                            .append("</strong>(");

                    appendParameters(html, m.getParameters());

                    html.append(")</h3></div>");
                }
            }
            html.append("</div>");
        }

        html.append("</div>");

        html.append("""
                <div class="footer">
                    <p>Generated using Java Reflection API</p>
                </div>
                </body></html>
                """);

        Files.writeString(
                outputDir.resolve(clazz.getSimpleName() + ".html"),
                html.toString()
        );
    }

    private void appendParameters(StringBuilder html, Parameter[] params) {
        for (int i = 0; i < params.length; i++) {
            html.append(params[i].getType().getSimpleName())
                    .append(" ")
                    .append(params[i].getName());
            if (i < params.length - 1) html.append(", ");
        }
    }

    private void generateIndexPage(Class<?>[] classes, Path outputDir) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n");
        html.append("  <meta charset=\"UTF-8\">\n");
        html.append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("  <title>Sevens Card Game - API Documentation</title>\n");
        html.append("  <link rel=\"stylesheet\" href=\"style.css\">\n");
        html.append("</head>\n<body>\n");

        html.append("  <div class=\"header\">\n");
        html.append("    <h1>🎴 Sevens Card Game</h1>\n");
        html.append("    <h2>API Documentation</h2>\n");
        html.append("    <p class=\"subtitle\">Generated using Java Reflection API</p>\n");
        html.append("  </div>\n\n");

        html.append("  <div class=\"content\">\n");
        html.append("    <div class=\"section\">\n");
        html.append("      <h2>📚 Documented Classes (").append(classes.length).append(")</h2>\n");
        html.append("      <p class=\"info\">This documentation was automatically generated using Java Reflection API.</p>\n");
        html.append("      <p class=\"info\"><strong>Reflection Methods Used:</strong> Class.forName(), getDeclaredMethods(), getDeclaredFields(), getConstructors(), getParameters(), getModifiers()</p>\n");
        html.append("    </div>\n\n");

        // Group by package
        html.append("    <div class=\"section\">\n");
        html.append("      <h3>🎮 Game Engine</h3>\n");
        html.append("      <ul class=\"class-list\">\n");
        for (Class<?> clazz : classes) {
            if (clazz.getPackageName().contains("engine")) {
                html.append("        <li><a href=\"").append(clazz.getSimpleName()).append(".html\">");
                html.append(clazz.getSimpleName()).append("</a> - <span class=\"package\">").append(clazz.getPackageName()).append("</span></li>\n");
            }
        }
        html.append("      </ul>\n    </div>\n\n");

        html.append("    <div class=\"section\">\n");
        html.append("      <h3>📦 Model Classes</h3>\n");
        html.append("      <ul class=\"class-list\">\n");
        for (Class<?> clazz : classes) {
            if (clazz.getPackageName().contains("model")) {
                html.append("        <li><a href=\"").append(clazz.getSimpleName()).append(".html\">");
                html.append(clazz.getSimpleName()).append("</a> - <span class=\"package\">").append(clazz.getPackageName()).append("</span></li>\n");
            }
        }
        html.append("      </ul>\n    </div>\n\n");

        html.append("    <div class=\"section\">\n");
        html.append("      <h3>🌐 Network</h3>\n");
        html.append("      <ul class=\"class-list\">\n");
        for (Class<?> clazz : classes) {
            if (clazz.getPackageName().contains("network")) {
                html.append("        <li><a href=\"").append(clazz.getSimpleName()).append(".html\">");
                html.append(clazz.getSimpleName()).append("</a> - <span class=\"package\">").append(clazz.getPackageName()).append("</span></li>\n");
            }
        }
        html.append("      </ul>\n    </div>\n\n");

        html.append("    <div class=\"section\">\n");
        html.append("      <h3>⚡ Events & Persistence</h3>\n");
        html.append("      <ul class=\"class-list\">\n");
        for (Class<?> clazz : classes) {
            if (clazz.getPackageName().contains("events") || clazz.getPackageName().contains("persistence")) {
                html.append("        <li><a href=\"").append(clazz.getSimpleName()).append(".html\">");
                html.append(clazz.getSimpleName()).append("</a> - <span class=\"package\">").append(clazz.getPackageName()).append("</span></li>\n");
            }
        }
        html.append("      </ul>\n    </div>\n\n");

        html.append("  </div>\n");

        html.append("  <div class=\"footer\">\n");
        html.append("    <p>Generated: ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("</p>\n");
        html.append("    <p>Total Classes: ").append(classes.length).append(" | Generated by Java Reflection API</p>\n");
        html.append("  </div>\n");

        html.append("</body>\n</html>\n");

        Path indexFile = outputDir.resolve("index.html");
        Files.writeString(indexFile, html.toString());
        logger.info("Index page written to: {}", indexFile);
    }

    private void generateStylesheet(Path outputDir) throws IOException {
        String css = """
            * { margin: 0; padding: 0; box-sizing: border-box; }
            
            body {
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;
                line-height: 1.6;
                color: #333;
                background: #f5f5f5;
            }
            
            .header {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 2rem;
                text-align: center;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            
            .header h1 { font-size: 2.5rem; margin-bottom: 0.5rem; }
            .header h2 { font-size: 1.5rem; font-weight: 300; }
            .header .subtitle { font-size: 0.9rem; opacity: 0.9; margin-top: 0.5rem; }
            .header .package { font-size: 0.9rem; opacity: 0.9; }
            .header .modifiers { font-size: 1rem; opacity: 0.8; margin-top: 0.5rem; }
            
            .nav {
                background: white;
                padding: 1rem 2rem;
                border-bottom: 1px solid #e0e0e0;
                display: flex;
                justify-content: space-between;
                align-items: center;
            }
            
            .nav a {
                color: #667eea;
                text-decoration: none;
                font-weight: 500;
            }
            
            .nav a:hover { text-decoration: underline; }
            .nav .timestamp { color: #999; font-size: 0.85rem; }
            
            .content {
                max-width: 1200px;
                margin: 2rem auto;
                padding: 0 2rem;
            }
            
            .section {
                background: white;
                padding: 2rem;
                margin-bottom: 2rem;
                border-radius: 8px;
                box-shadow: 0 2px 4px rgba(0,0,0,0.1);
            }
            
            .section h2 {
                color: #667eea;
                margin-bottom: 1rem;
                padding-bottom: 0.5rem;
                border-bottom: 2px solid #667eea;
            }
            
            .section h3 {
                color: #555;
                margin: 1rem 0 0.5rem 0;
                font-size: 1.1rem;
            }
            
            .info {
                background: #e3f2fd;
                padding: 1rem;
                border-left: 4px solid #2196f3;
                margin: 0.5rem 0;
                border-radius: 4px;
            }
            
            .hierarchy { margin: 1rem 0; }
            .hierarchy code { background: #f5f5f5; padding: 0.2rem 0.5rem; border-radius: 3px; }
            .hierarchy ul { margin-left: 2rem; }
            
            .members {
                width: 100%;
                border-collapse: collapse;
                margin: 1rem 0;
            }
            
            .members th {
                background: #f5f5f5;
                padding: 0.75rem;
                text-align: left;
                font-weight: 600;
                border-bottom: 2px solid #ddd;
            }
            
            .members td {
                padding: 0.75rem;
                border-bottom: 1px solid #eee;
            }
            
            .members tr:hover { background: #f9f9f9; }
            
            .method {
                background: #fafafa;
                padding: 1rem;
                margin: 1rem 0;
                border-left: 4px solid #4caf50;
                border-radius: 4px;
            }
            
            .method h3 {
                margin: 0;
                line-height: 1.8;
                word-wrap: break-word;
            }
            
            .modifier {
                color: #9c27b0;
                font-weight: 600;
            }
            
            .type {
                color: #2196f3;
                font-weight: 500;
            }
            
            .param {
                color: #ff9800;
            }
            
            .annotation {
                background: #fff3e0;
                color: #f57c00;
                padding: 0.2rem 0.5rem;
                border-radius: 3px;
                font-size: 0.85rem;
            }
            
            .throws {
                color: #d32f2f;
                font-size: 0.9rem;
                margin-top: 0.5rem;
            }
            
            .class-list {
                list-style: none;
                margin: 1rem 0;
            }
            
            .class-list li {
                padding: 0.75rem;
                margin: 0.5rem 0;
                background: #f9f9f9;
                border-radius: 4px;
                border-left: 4px solid #667eea;
            }
            
            .class-list a {
                color: #667eea;
                text-decoration: none;
                font-weight: 600;
                font-size: 1.1rem;
            }
            
            .class-list a:hover { text-decoration: underline; }
            
            .class-list .package {
                color: #999;
                font-size: 0.85rem;
                margin-left: 0.5rem;
            }
            
            .footer {
                background: #333;
                color: white;
                padding: 2rem;
                text-align: center;
                margin-top: 4rem;
            }
            
            .footer p { margin: 0.5rem 0; opacity: 0.8; }
            
            code {
                font-family: 'Consolas', 'Monaco', monospace;
                font-size: 0.9em;
            }
            """;

        Path cssFile = outputDir.resolve("style.css");
        Files.writeString(cssFile, css);
        logger.info("Stylesheet written to: {}", cssFile);
    }

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║  HTML Documentation Generator (Reflection API)   ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println();

        HtmlDocGenerator generator = new HtmlDocGenerator();
        generator.generateAllDocumentation();

        System.out.println();
        System.out.println("✅ Documentation generation complete!");
        System.out.println("🌐 Open docs/html/index.html in your browser!");
    }
}

