package me.vse.fintrackserver;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StructureTest {

    private static final String PACKAGE_TO_TEST = "me.vse.fintrackserver";
    private static final String TEST_PACKAGE = "me.vse.fintrackserver";

    private static final Set<String> EXCLUDED_CLASSES = Set.of();
    private static final Set<String> EXCLUDED_METHODS = Set.of();

    @Test
    public void testAllClassesHaveCorrespondingTest() throws ClassNotFoundException {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(PACKAGE_TO_TEST))
                .setScanners(new SubTypesScanner(false))
        );
        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

        // List to collect missing classes and methods
        List<String> missingTests = new ArrayList<>();
        List<String> missingTestMethods = new ArrayList<>();

        for (Class<?> clazz : allClasses) {
            if (EXCLUDED_CLASSES.contains(clazz.getSimpleName())) {
                continue;
            }

            String testClassName = TEST_PACKAGE + "." + clazz.getSimpleName() + "Test";
            Class<?> testClass = null;
            try {
                testClass = Class.forName(testClassName);
            } catch (ClassNotFoundException e) {
                missingTests.add("Missing test class for: " + clazz.getSimpleName());
                continue; // Skip further checking if the class itself is missing
            }

            for (Method method : clazz.getDeclaredMethods()) {
                if (EXCLUDED_METHODS.contains(method.getName())) {
                    continue;
                }
                String testMethodName = method.getName() + "Test";
                boolean hasTestMethod = false;

                for (Method testMethod : testClass.getDeclaredMethods()) {
                    if (testMethod.getName().equals(testMethodName)) {
                        hasTestMethod = true;
                        break;
                    }
                }

                if (!hasTestMethod) {
                    missingTestMethods.add("Missing test method: " + testMethodName + " in class " + testClass.getSimpleName());
                }
            }
        }

        // Combine missing test classes and methods
        List<String> allMissingTests = new ArrayList<>();
        allMissingTests.addAll(missingTests);
        allMissingTests.addAll(missingTestMethods);

        // Make a single assertion at the end with all missing tests
        assertTrue(allMissingTests.isEmpty(), "Missing tests: \n" + String.join("\n", allMissingTests));
    }
}
