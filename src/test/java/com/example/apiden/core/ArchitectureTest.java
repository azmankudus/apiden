package com.example.apiden.core;

import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Enforces architectural rules using ArchUnit.
 */
@AnalyzeClasses(packages = "com.example.apiden", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    /**
     * Enforces that controllers reside in a subpackage of feature or management.
     */
    @ArchTest
    public static final ArchRule controllers_must_reside_in_feature_package = classes().that()
            .haveSimpleNameEndingWith("Controller")
            .should().resideInAnyPackage("..feature..", "..management..");

    /**
     * Enforce that classes in 'core' do not depend on classes in 'feature'.
     */
    @ArchTest
    public static final ArchRule core_should_not_depend_on_feature = noClasses().that().resideInAPackage("..core..")
            .should().dependOnClassesThat().resideInAPackage("..feature..");
}
