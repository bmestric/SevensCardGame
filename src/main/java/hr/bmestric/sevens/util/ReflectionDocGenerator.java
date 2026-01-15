package hr.bmestric.sevens.util;

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
import java.util.Arrays;
import java.util.stream.Collectors;


public class ReflectionDocGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ReflectionDocGenerator.class);
    private static final String DEFAULT_OUTPUT_DIR = "docs/reflection";
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void generateAllDocumentation() {
        try {
            Path outputPath = Paths.get(DEFAULT_OUTPUT_DIR);
            Files.createDirectories(outputPath);

            logger.info("Starting Reflection API documentation generation...");

            Class<?>[] classes = {
                Class.forName("hr.bmestric.sevens.engine.GameEngine"),
                Class.forName("hr.bmestric.sevens.engine.MoveValidator"),
                Class.forName("hr.bmestric.sevens.engine.TrickResolver"),
                Class.forName("hr.bmestric.sevens.model.GameState"),
                Class.forName("hr.bmestric.sevens.model.Player"),
                Class.forName("hr.bmestric.sevens.model.Card"),
                Class.forName("hr.bmestric.sevens.events.GameEventBus"),
                Class.forName("hr.bmestric.sevens.persistence.ObjectStorageService"),
                Class.forName("hr.bmestric.sevens.network.rmi.RemoteGameEngineImpl"),
                Class.forName("hr.bmestric.sevens.network.tcp.TcpChatServer")
            };

            for (Class<?> clazz : classes) {
                generateClassDocumentation(clazz, outputPath);
            }

            // Generate index file
            generateIndexFile(classes, outputPath);

            logger.info("Documentation generation complete! Files saved to: {}", outputPath.toAbsolutePath());
            System.out.println("✅ Reflection documentation generated successfully!");
            System.out.println("📁 Location: " + outputPath.toAbsolutePath());

        } catch (Exception e) {
            logger.error("Failed to generate documentation", e);
            throw new RuntimeException("Documentation generation failed", e);
        }
    }

    private void generateClassDocumentation(Class<?> clazz, Path outputDir) throws IOException {
        logger.info("Documenting class: {}", clazz.getName());

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<classDocumentation>\n");
        xml.append("  <metadata>\n");
        xml.append("    <generatedBy>ReflectionDocGenerator</generatedBy>\n");
        xml.append("    <timestamp>").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("</timestamp>\n");
        xml.append("    <reflectionAPI>Java Reflection API</reflectionAPI>\n");
        xml.append("  </metadata>\n\n");

        // Class information
        xml.append("  <class>\n");
        xml.append("    <name>").append(clazz.getSimpleName()).append("</name>\n");
        xml.append("    <fullName>").append(clazz.getName()).append("</fullName>\n");
        xml.append("    <package>").append(clazz.getPackageName()).append("</package>\n");
        xml.append("    <modifiers>").append(Modifier.toString(clazz.getModifiers())).append("</modifiers>\n");

        // Superclass
        if (clazz.getSuperclass() != null && !clazz.getSuperclass().equals(Object.class)) {
            xml.append("    <superclass>").append(clazz.getSuperclass().getSimpleName()).append("</superclass>\n");
        }

        // Interfaces
        Class<?>[] interfaces = clazz.getInterfaces();
        if (interfaces.length > 0) {
            xml.append("    <implements>\n");
            for (Class<?> iface : interfaces) {
                xml.append("      <interface>").append(iface.getSimpleName()).append("</interface>\n");
            }
            xml.append("    </implements>\n");
        }

        xml.append("  </class>\n\n");

        // Fields
        Field[] fields = clazz.getDeclaredFields();
        if (fields.length > 0) {
            xml.append("  <fields count=\"").append(fields.length).append("\">\n");
            for (Field field : fields) {
                if (!field.isSynthetic()) { // Skip compiler-generated fields
                    documentField(field, xml);
                }
            }
            xml.append("  </fields>\n\n");
        }

        // Constructors
        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        if (constructors.length > 0) {
            xml.append("  <constructors count=\"").append(constructors.length).append("\">\n");
            for (Constructor<?> constructor : constructors) {
                documentConstructor(constructor, xml);
            }
            xml.append("  </constructors>\n\n");
        }

        // Methods
        Method[] methods = clazz.getDeclaredMethods();
        if (methods.length > 0) {
            xml.append("  <methods count=\"").append(methods.length).append("\">\n");
            for (Method method : methods) {
                if (!method.isSynthetic()) { // Skip compiler-generated methods
                    documentMethod(method, xml);
                }
            }
            xml.append("  </methods>\n\n");
        }

        xml.append("</classDocumentation>\n");

        // Write to file
        Path outputFile = outputDir.resolve(clazz.getSimpleName() + ".xml");
        Files.writeString(outputFile, xml.toString());
        logger.info("Documentation written to: {}", outputFile);
    }

    private void documentField(Field field, StringBuilder xml) {
        xml.append("    <field>\n");
        xml.append("      <name>").append(field.getName()).append("</name>\n");
        xml.append("      <type>").append(field.getType().getSimpleName()).append("</type>\n");
        xml.append("      <fullType>").append(field.getType().getName()).append("</fullType>\n");
        xml.append("      <modifiers>").append(Modifier.toString(field.getModifiers())).append("</modifiers>\n");

        // Generic type information
        Type genericType = field.getGenericType();
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) genericType;
            xml.append("      <genericType>").append(pType.getTypeName()).append("</genericType>\n");
        }

        xml.append("    </field>\n");
    }

    private void documentConstructor(Constructor<?> constructor, StringBuilder xml) {
        xml.append("    <constructor>\n");
        xml.append("      <modifiers>").append(Modifier.toString(constructor.getModifiers())).append("</modifiers>\n");

        // Parameters
        Parameter[] parameters = constructor.getParameters();
        if (parameters.length > 0) {
            xml.append("      <parameters count=\"").append(parameters.length).append("\">\n");
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                xml.append("        <parameter index=\"").append(i).append("\">\n");
                xml.append("          <name>").append(param.getName()).append("</name>\n");
                xml.append("          <type>").append(param.getType().getSimpleName()).append("</type>\n");
                xml.append("        </parameter>\n");
            }
            xml.append("      </parameters>\n");
        }

        // Exceptions
        Class<?>[] exceptions = constructor.getExceptionTypes();
        if (exceptions.length > 0) {
            xml.append("      <throws>\n");
            for (Class<?> exception : exceptions) {
                xml.append("        <exception>").append(exception.getSimpleName()).append("</exception>\n");
            }
            xml.append("      </throws>\n");
        }

        xml.append("    </constructor>\n");
    }

    private void documentMethod(Method method, StringBuilder xml) {
        xml.append("    <method>\n");
        xml.append("      <name>").append(method.getName()).append("</name>\n");
        xml.append("      <returnType>").append(method.getReturnType().getSimpleName()).append("</returnType>\n");
        xml.append("      <modifiers>").append(Modifier.toString(method.getModifiers())).append("</modifiers>\n");

        // Parameters
        Parameter[] parameters = method.getParameters();
        if (parameters.length > 0) {
            xml.append("      <parameters count=\"").append(parameters.length).append("\">\n");
            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                xml.append("        <parameter index=\"").append(i).append("\">\n");
                xml.append("          <name>").append(param.getName()).append("</name>\n");
                xml.append("          <type>").append(param.getType().getSimpleName()).append("</type>\n");
                xml.append("        </parameter>\n");
            }
            xml.append("      </parameters>\n");
        } else {
            xml.append("      <parameters count=\"0\"/>\n");
        }

        // Exceptions
        Class<?>[] exceptions = method.getExceptionTypes();
        if (exceptions.length > 0) {
            xml.append("      <throws>\n");
            for (Class<?> exception : exceptions) {
                xml.append("        <exception>").append(exception.getSimpleName()).append("</exception>\n");
            }
            xml.append("      </throws>\n");
        }

        // Annotations
        Annotation[] annotations = method.getDeclaredAnnotations();
        if (annotations.length > 0) {
            xml.append("      <annotations>\n");
            for (Annotation annotation : annotations) {
                xml.append("        <annotation>").append(annotation.annotationType().getSimpleName()).append("</annotation>\n");
            }
            xml.append("      </annotations>\n");
        }

        xml.append("    </method>\n");
    }

    private void generateIndexFile(Class<?>[] classes, Path outputDir) throws IOException {
        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<documentationIndex>\n");
        xml.append("  <metadata>\n");
        xml.append("    <generatedBy>ReflectionDocGenerator</generatedBy>\n");
        xml.append("    <timestamp>").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("</timestamp>\n");
        xml.append("    <totalClasses>").append(classes.length).append("</totalClasses>\n");
        xml.append("  </metadata>\n\n");

        xml.append("  <classes>\n");
        for (Class<?> clazz : classes) {
            xml.append("    <class>\n");
            xml.append("      <name>").append(clazz.getSimpleName()).append("</name>\n");
            xml.append("      <package>").append(clazz.getPackageName()).append("</package>\n");
            xml.append("      <file>").append(clazz.getSimpleName()).append(".xml</file>\n");
            xml.append("    </class>\n");
        }
        xml.append("  </classes>\n");
        xml.append("</documentationIndex>\n");

        Path indexFile = outputDir.resolve("index.xml");
        Files.writeString(indexFile, xml.toString());
        logger.info("Index file written to: {}", indexFile);
    }

    public static void main(String[] args) {
        System.out.println("╔═══════════════════════════════════════════════════╗");
        System.out.println("║  Reflection API Documentation Generator          ║");
        System.out.println("╚═══════════════════════════════════════════════════╝");
        System.out.println();

        ReflectionDocGenerator generator = new ReflectionDocGenerator();
        generator.generateAllDocumentation();

        System.out.println();
        System.out.println("✅ Documentation generation complete!");
        System.out.println("📁 Check the 'docs/reflection' directory for generated XML files.");
    }
}

