package com.memoreel.backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class NamingConventionTest {

  private static JavaClasses importedClasses;

  @BeforeAll
  static void importClasses() {
    importedClasses = ArchitectureRules.importProductionClasses();
  }

  @Test
  void controllersShouldResideInControllerPackage() {
    ArchitectureRules.controllerNaming().check(importedClasses);
  }

  @Test
  void servicesShouldResideInServicePackage() {
    ArchitectureRules.serviceNaming().check(importedClasses);
  }

  @Test
  void repositoriesShouldResideInRepositoryPackage() {
    ArchitectureRules.repositoryNaming().check(importedClasses);
  }
}
