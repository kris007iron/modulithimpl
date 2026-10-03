package com.kris007iron.modulithimpl;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModulithArchitectureTests {

    @Test
    void verifyModularStructure() {
        ApplicationModules.of(ModulithimplApplication.class).verify();
    }
}
