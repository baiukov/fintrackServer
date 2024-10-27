package me.vse.fintrackserver;

import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class StructureTest {

    private static final String PACKAGE_TO_TEST = "me.vse.fintrackserver";

    private static final Set<String> EXCLUDED_CLASSES = Set.of(
            "FintrackServerApplication",
            "Account",
            "UserGroupRelationId",
            "AccountUserRights",
            "Asset",
            "Category",
            "Group",
            "StandingOrder",
            "Transaction",
            "User",
            "UserGroupRelation",
            "AccountType",
            "ErrorMessages",
            "Frequencies",
            "TransactionTypes",
            "UserRights",
            "PersistenceConfig"
    );
    private static final Set<String> EXCLUDED_METHODS = Set.of();

    @Test
    public void testAllClassesHaveCorrespondingTest() {
        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(PACKAGE_TO_TEST))
                .setScanners(new SubTypesScanner(false))
        );
        Set<Class<?>> allClasses = reflections.getSubTypesOf(Object.class);

        List<String> missingTests = new ArrayList<>();
        List<String> missingTestMethods = new ArrayList<>();

        for (Class<?> clazz : allClasses) {
            if (EXCLUDED_CLASSES.contains(clazz.getSimpleName())) {
                continue;
            }

            String className = clazz.getSimpleName();
            boolean isExcluded = className.contains("$")
                    || className.contains("Dto")
                    || className.contains("Builder")
                    || className.contains("Test")
                    || className.contains("Request")
                    || className.contains("Response")
                    || className.contains("Impl");

            if (isExcluded) continue;

            String testClassName = clazz.getName() + "Test";
            Class<?> testClass;
            try {
                testClass = Class.forName(testClassName);
            } catch (ClassNotFoundException e) {
                missingTests.add("Missing test class for: " + clazz.getSimpleName());
                continue;
            }

            for (Method method : clazz.getDeclaredMethods()) {
                if (EXCLUDED_METHODS.contains(method.getName()) || method.getName().contains("builder")) {
                    continue;
                }
                String testMethodName = method.getName() + "Test";
                boolean hasTestMethod = false;

                for (Method testMethod : testClass.getDeclaredMethods()) {
                    if (!Modifier.isPublic(method.getModifiers())) {
                        hasTestMethod = true;
                        break;
                    }
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

        List<String> allMissingTests = new ArrayList<>();
        allMissingTests.addAll(missingTests);
        allMissingTests.addAll(missingTestMethods);

        assertTrue(allMissingTests.isEmpty(), "Missing tests: \n" + String.join("\n", allMissingTests));
    }
}
