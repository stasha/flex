package com.stasha.info.flex.parsers;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class FlexStringTemplateOptionsTest {

    private FlexStringTemplateOptions to;

    @BeforeEach
    public void setUp() {
        to = new FlexStringTemplateOptions("21_=starts\\= with 21, _17-_19=starts with 17\\, 18 or 19, _11=ends with 11, _12-_14=ends with 12\\, 13 or 14, 1=one, 2=two, 3-5=few, other=many");
    }
    
    @Test
    public void createParserTest() {
        to = new FlexStringTemplateOptions();
    }

    @Test
    public void optionsParsingTest() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 23; ++i) {
            to.getValue(i);
        }
        long end = System.currentTimeMillis();
        System.out.println("optionsParsingTest: " + (end - start) + "ms");

        for (int i = 0; i < 23; ++i) {
            if (i == 1) {
                Assertions.assertEquals("one", to.getValue(i));
            } else if (i == 2) {
                Assertions.assertEquals("two", to.getValue(i));
            } else if (i >= 3 && i <= 5) {
                Assertions.assertEquals("few", to.getValue(i));
            } else if (i == 11) {
                Assertions.assertEquals("ends with 11", to.getValue(i));
            } else if (i >=12 && i <=14) {
                Assertions.assertEquals("ends with 12\\, 13 or 14", to.getValue(i));
            } else if (i >=17 && i <=19) {
                Assertions.assertEquals("starts with 17\\, 18 or 19", to.getValue(i));
            } else if (i == 21) {
                Assertions.assertEquals("starts\\= with 21", to.getValue(i));
            } else {
                Assertions.assertEquals("many", to.getValue(i));

            }
        }

    }

}
