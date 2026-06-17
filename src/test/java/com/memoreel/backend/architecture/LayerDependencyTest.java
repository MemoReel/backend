package com.memoreel.backend.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LayerDependencyTest {

  private static JavaClasses importedClasses;

  @BeforeAll
  static void importClasses() {
    importedClasses = ArchitectureRules.importProductionClasses();
  }

  @Test
  void controllerShouldNotDependOnRepository() {
    ArchitectureRules.repositoryNotAccessedByController().check(importedClasses);
  }

  @Test
  void packagesShouldBeFreeOfCycles() {
    ArchitectureRules.noCyclesRule().check(importedClasses);
  }
}
