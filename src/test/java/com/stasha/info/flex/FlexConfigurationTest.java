package com.stasha.info.flex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class FlexConfigurationTest {

    private FlexConfiguration config;

    @BeforeEach
    public void setUp() {
        config = new FlexConfiguration();
    }

    @Test
    public void configTest() {
        config.getLoader().load();
        
        System.out.println(config.getFlexStore().unwrapStore());
    }
}
