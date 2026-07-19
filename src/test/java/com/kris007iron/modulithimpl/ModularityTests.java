package com.kris007iron.modulithimpl;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

public class ModularityTests {

    @Test
    void verifyModularStructure() {
        ApplicationModules.of(ModulithimplApplication.class).verify();
    }
}
