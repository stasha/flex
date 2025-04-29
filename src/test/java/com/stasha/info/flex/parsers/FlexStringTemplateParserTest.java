package com.stasha.info.flex.parsers;

import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class FlexStringTemplateParserTest {

    private FlexStringTemplateParser parser;

    @BeforeEach
    public void setUp() {
        parser = new FlexStringTemplateParser();
    }

    @Test
    public void parserIncorrectTest() {

        String[] templates = new String[]{
            null,
            "",
            "   ",
            "string",
            "32135",
            "%$@@$%",
            "\\{0}",
            "\\{0 \\{1}}",
            "\\{0 \\{1 \\{2}}}",
            "\\{0 \\[1=one]}"
        };

//        long start = System.currentTimeMillis();
//        Stream.of(templates).forEach(s -> {
//            FlexParsedTemplate pt = parser.parse(s);
//            if (pt == null) {
//                System.out.println("Failed to parse template; " + s);
//            } else {
//                parser.parse(s).toString();
//            }
//        });
//        long end = System.currentTimeMillis();
//        System.out.println("parserIncorrectTest: " + (end - start) + "ms");
//
//        Stream.of(templates).forEach(s -> {
//            FlexParsedTemplate pt = parser.parse(s);
//            if (pt == null) {
//                System.out.println("Failed to parse template; " + s);
//            } else {
//                parser.parse(s).toString();
//            }
//            Assertions.assertNull(pt);
//        });
        System.out.println("");
        System.out.println("");
    }

    @Test
    public void parserTest() {

        String[][] templates = {
            // simple
            {"{0}", "value=0, options=null, formats=null"},
            {"{1}", "value=1, options=null, formats=null"},
            {"{2}", "value=2, options=null, formats=null"},
            {"{0} {1} {2}", "value=0, options=null, formats=null"},
            {"{3} {1} {2}", "value=3, options=null, formats=null"},
            {"{1}.fmt.uppercase", "value=1, options=null, formats=[fmt.uppercase]"},
            {"{0 [1=one]}", "value=0, options=[1=one], formats=null"},
            {"{0 [1=one, 2=two, 3-5=few, other=many]}.f&fmt.uppercase", "value=0, options=[1=one, 2=two, 3-5=few, other=many], formats=[f, fmt.uppercase]"},
            // escape
            {"\\{0 {1}}", "value=1, options=null, formats=null"},
            {"\\{0 \\{1 {2}}}", "value=2, options=null, formats=null"}, //
        //            // nested goes from inner most to outer most
        //            {"{0 {1}}", "value=1, options=null, formats=null"},
        //            {"{0 {1 {2}}", "value=2, options=null, formats=null"},
        //            {"{0 {1 {2 {3}}}", "value=3, options=null, formats=null"},
        //            // nested goes from inner most to outer most
        //            {"{0 [0=zero] {1 [1=one]}.fmt1}.fmt0", "value=1, options=[1=one], formats=[fmt1]"},
        //            {"{0 [0=zero] {1 [1=one] {2 [2=two]}}}", "value=2, options=[2=two], formats=null"},
        //            {"{0 [0=zero] {1 [1=one] {2 [2=two] {3 [3-5=few]}.fmt3}.fmt2}.fmt1}.fmt0", "value=3, options=[3-5=few], formats=[fmt3]"}
        };

        long start = System.currentTimeMillis();
        Stream.of(templates).forEach(t -> {
            parser.parse(t[0]).toString();
        });
        long end = System.currentTimeMillis();
        System.out.println("parserTest: " + (end - start) + "ms");

        Stream.of(templates).forEach(t -> {
            System.out.println(t[0] + " == " + t[1]);
            Assertions.assertEquals(t[1], parser.parse(t[0]).toString());
        });

        System.out.println("");
        System.out.println("");

    }
    
    
    public static void execute(String msg, Runnable call) {
        long start = System.currentTimeMillis();
        call.run();
        long end = System.currentTimeMillis();
        System.out.println(msg + ": " + (end - start) + "ns");
        System.out.println("");
    }

}
