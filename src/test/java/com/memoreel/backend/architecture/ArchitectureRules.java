package com.memoreel.backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;

final class ArchitectureRules {

  static final String BASE_PACKAGE = "com.memoreel.backend";

  private ArchitectureRules() {}

  static JavaClasses importProductionClasses() {
    return new ClassFileImporter()
        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
        .importPackages(BASE_PACKAGE);
  }

  /** Repository는 Controller에서 직접 호출하지 않는다. */
  static ArchRule repositoryNotAccessedByController() {
    return noClasses()
        .that()
        .haveSimpleNameEndingWith("Controller")
        .should()
        .dependOnClassesThat()
        .haveSimpleNameEndingWith("Repository")
        .allowEmptyShould(true);
  }

  /** 패키지 간 순환 의존성 금지. */
  static ArchRule noCyclesRule() {
    return slices().matching(BASE_PACKAGE + ".(*)..").should().beFreeOfCycles();
  }

  /** *Controller는 controller 패키지(혹은 단일 도메인 루트 패키지)에 있어야 한다. */
  static ArchRule controllerNaming() {
    return classes()
        .that()
        .haveSimpleNameEndingWith("Controller")
        .should()
        .resideInAnyPackage("..controller..", "..user..")
        .allowEmptyShould(true);
  }

  /** *Service는 service 패키지(혹은 단일 도메인 루트 패키지)에 있어야 한다. */
  static ArchRule serviceNaming() {
    return classes()
        .that()
        .haveSimpleNameEndingWith("Service")
        .should()
        .resideInAnyPackage("..service..", "..user..")
        .allowEmptyShould(true);
  }

  /** *Repository는 repository 패키지(혹은 단일 도메인 루트 패키지)에 있어야 한다. */
  static ArchRule repositoryNaming() {
    return classes()
        .that()
        .haveSimpleNameEndingWith("Repository")
        .should()
        .resideInAnyPackage("..repository..", "..user..")
        .allowEmptyShould(true);
  }
}
