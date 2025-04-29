package com.stasha.info.flex;

import java.io.IOException;
import com.ibm.icu.text.MessageFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class FlexLocalizationTest {

    private final Map<String, String> stringStore = new LinkedHashMap<>(50);
    private final Map<String, String> localizationDefaults = new LinkedHashMap<>(50);

    private final Calendar cal = Calendar.getInstance();
    private FlexLocalization fl;

    @BeforeEach
    public void setUp() {

        fl = new FlexLocalization();

        stringStore.put("welcome.msg", "Welcome {0}, {1}.fmt.decimal");
        stringStore.put("my.name", "Fajfrić Ljubomir");
        stringStore.put("my.gender", "m");
        stringStore.put("my.location", "Fajfrić Ljubomir {0}.fmt.uppercase");
        stringStore.put("different.items", "There are {0}.fmt.decimal different items");
        stringStore.put("my.sallary", "Sallary for {0} is {1}.fmt.currency");
        stringStore.put("my.sallary.to.date", "Sallary for {0} is {1}.fmt.currency to date {2}.fmt.date");
        stringStore.put("money.in.wallet", "You have {0 [0=zero dollars, 1={0}.fmt.currency dollar, 2-4=few dollars, other={0}.fmt.currency dollars]} to the date {1}.fmt.date");
        stringStore.put("money.in.wallet.s", "You have {0}.fmt.currency to the date {1}.fmt.date");
        stringStore.put("fmt.date", "dd.MM.yyyy");
        stringStore.put("inline.var", "{my.name}.");
        stringStore.put("inline.name", "My name is {my.name}.");
        stringStore.put("inline.gender", "My name is {my.name} and my gender is {my.gender [m=Male, f=Female, other=Uni]}.fmt.uppercase.");
        stringStore.put("inline.var.with.inline.options", "{my.gender [m=Osvojio sam 1. mesto, f=Osvojila sam 1. mesto, other=Osvojio je 1. mesto]}.");
        stringStore.put("dynamic.var.with.inline.options", "{0 [m=Osvojio sam 1. mesto, f=Osvojila sam 1. mesto, other=Osvojio je 1. mesto]}.");
        stringStore.put("inline.ordinal", "{ordinal[1]}.m.");
        stringStore.put("inline.ordinal.gender", "{1}.ordinal&f.");
        stringStore.put("outline.ordinal", "{0}.ordinal&{1}");
        stringStore.put("inline.text.upper", "{my text}.fmt.uppercase.");

        stringStore.put("escaped.template", "\\{0 [m=1, f=2]}.fmt.uppercase");
        stringStore.put("escaped.options", "{0 [m=a\\=2, f=2\\,1]}.fmt.uppercase");

        stringStore.put("nesting", "{0 [] {1 [] {2 [] {3 []}}}}");

        stringStore.put("place[1]", "place");
        stringStore.put("place[other]", "places");
        stringStore.put("position[1]", "position");
        stringStore.put("position[other]", "positions");
        stringStore.put("award[1]", "award");
        stringStore.put("award[2-4]", "awards");
        stringStore.put("award[other]", "awards");

        stringStore.put("ordinal[1]", "first [male|female|neutral]");
        stringStore.put("ordinal[2]", "second");
        stringStore.put("ordinal[3]", "third");
        stringStore.put("ordinal[4]", "fourth");
        stringStore.put("ordinal[5]", "fifth");
        stringStore.put("ordinal[6]", "sixth");
        stringStore.put("ordinal[7]", "firstseventh");
        stringStore.put("ordinal[8]", "eighth");
        stringStore.put("ordinal[9]", "ninth");
        stringStore.put("ordinal[10]", "tenth");
        stringStore.put("ordinal[11]", "eleventh");
        stringStore.put("ordinal[12]", "twelfth");
        stringStore.put("ordinal[13]", "thirteenth");
        stringStore.put("ordinal[_1]", "{0}st");
        stringStore.put("ordinal[_2]", "{0}nd");
        stringStore.put("ordinal[_3]", "{0}rd");
        stringStore.put("ordinal[other]", "{0}th");

        localizationDefaults.put("fmt.locale", "en-US");
        localizationDefaults.put("fmt.date", "YYYY-MM-DD");
        localizationDefaults.put("fmt.time", "HH:mm:ss");
        localizationDefaults.put("fmt.decimal", "#,##0.###");
        localizationDefaults.put("fmt.currency", "¤#,##0.00");
        localizationDefaults.put("fmt.uppercase", "{0}");

        localizationDefaults.putAll(stringStore);

        long start = System.currentTimeMillis();
        fl.getLoader().load(localizationDefaults);
        long end = System.currentTimeMillis();
        System.out.println("Startup time: " + (end - start) + "ms");
    }

    @Test
    public void getMessageTest() {
        cal.set(2025, 3, 18);

        long start = System.currentTimeMillis();
        for (int i = 0; i < 2; ++i) {
            fl.getMessage("my.name");
            fl.getMessage("my.location", "is at home");
            fl.getMessage("different.items", i);
            fl.getMessage("my.sallary", "Fajfrić Ljubomir", i);

            fl.getMessage("my.sallary.to.date", "Fajfrić Ljubomir", i, cal.getTime());
            fl.getMessage("money.in.wallet", i, cal.getTime());
            fl.getMessage("money.in.wallet", i, cal.getTime());
            fl.getMessage("money.in.wallet", i + 1, cal.getTime());
            fl.getMessage("money.in.wallet", i + 100, cal.getTime());
            fl.getMessage("money.in.wallet", i + 4, cal.getTime());
            fl.getMessage("money.in.wallet", i + 12, cal.getTime());

            fl.getMessage("inline.var");
            fl.getMessage("inline.name");
            fl.getMessage("inline.gender");
            fl.getMessage("inline.var.with.inline.options");
            fl.getMessage("dynamic.var.with.inline.options", "f");
            fl.getMessage("dynamic.var.with.inline.options", "n");
            fl.getMessage("inline.ordinal.gender", "f");
            fl.getMessage("inline.text.upper");
            fl.getMessage("inline.ordinal");

            fl.getMessage("escaped.template");
            fl.getMessage("escaped.options", "m");
            fl.getMessage("escaped.options", "f");

            fl.getMessage("outline.ordinal", 1, "m");
            fl.getMessage("outline.ordinal", 1, "f");
            fl.getMessage("outline.ordinal", 2, "n");
        }

        long end = System.currentTimeMillis();
        System.out.println("total time: " + (end - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 300000; i++) {
            fl.getMessage("welcome.msg", "Jon Doe", i); // Simple
            fl.getMessage("money.in.wallet", i, new java.util.Date()); // Complex
//            System.out.println(msg1);
//            System.out.println(msg2);
        }
        end = System.currentTimeMillis();

        System.out.println("total: " + (end - start));

        Assertions.assertEquals("Fajfrić Ljubomir", fl.getMessage("my.name"));
        Assertions.assertEquals("Fajfrić Ljubomir IS AT HOME", fl.getMessage("my.location", "is at home"));
        Assertions.assertEquals("There are 2,234,000 different items", fl.getMessage("different.items", 2234000));
        Assertions.assertEquals("Sallary for Fajfrić Ljubomir is $32.00", fl.getMessage("my.sallary", "Fajfrić Ljubomir", 32));

        Assertions.assertEquals("Sallary for Fajfrić Ljubomir is $32.00 to date 18.04.2025", fl.getMessage("my.sallary.to.date", "Fajfrić Ljubomir", 32, cal.getTime()));
        Assertions.assertEquals("You have zero dollars to the date 18.04.2025", fl.getMessage("money.in.wallet", 0, cal.getTime()));
        Assertions.assertEquals("You have $1.00 dollar to the date 18.04.2025", fl.getMessage("money.in.wallet", 1, cal.getTime()));
        Assertions.assertEquals("You have few dollars to the date 18.04.2025", fl.getMessage("money.in.wallet", 2, cal.getTime()));
        Assertions.assertEquals("You have few dollars to the date 18.04.2025", fl.getMessage("money.in.wallet", 3, cal.getTime()));
        Assertions.assertEquals("You have few dollars to the date 18.04.2025", fl.getMessage("money.in.wallet", 4, cal.getTime()));
        Assertions.assertEquals("You have $32.00 dollars to the date 18.04.2025", fl.getMessage("money.in.wallet", 32, cal.getTime()));

        Assertions.assertEquals("Fajfrić Ljubomir.", fl.getMessage("inline.var"));
        Assertions.assertEquals("My name is Fajfrić Ljubomir.", fl.getMessage("inline.name"));
        Assertions.assertEquals("My name is Fajfrić Ljubomir and my gender is MALE", fl.getMessage("inline.gender"));
        Assertions.assertEquals("Osvojio sam 1. mesto.", fl.getMessage("inline.var.with.inline.options"));
        Assertions.assertEquals("Osvojila sam 1. mesto.", fl.getMessage("dynamic.var.with.inline.options", "f"));
        Assertions.assertEquals("Osvojio je 1. mesto.", fl.getMessage("dynamic.var.with.inline.options", "n"));
        Assertions.assertEquals("first female", fl.getMessage("inline.ordinal.gender", "f"));
        Assertions.assertEquals("MY TEXT", fl.getMessage("inline.text.upper"));
        Assertions.assertEquals("first male", fl.getMessage("inline.ordinal"));

        Assertions.assertEquals("{0 [m=1, f=2]}.fmt.uppercase", fl.getMessage("escaped.template"));
        Assertions.assertEquals("A=2", fl.getMessage("escaped.options", "m"));
        Assertions.assertEquals("2,1", fl.getMessage("escaped.options", "f"));

//        Assertions.assertEquals("a b c d", fl.getMessage("nesting", "a", "b", "c", "d"));
        Assertions.assertEquals("first male", fl.getMessage("outline.ordinal", 1, "m"));
        Assertions.assertEquals("first female", fl.getMessage("outline.ordinal", 1, "f"));
        Assertions.assertEquals("second", fl.getMessage("outline.ordinal", 2, "n"));
    }

    @Test
    public void storeTest() {
        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            fl.getFlexStore().put("str_key" + i, "value" + i);
            fl.getFlexStore().getObjectStore().put("obj_key" + i, new Object());
            fl.getFlexStore().get("str_key" + (i % 300));
            fl.getFlexStore().getObjectStore().get("obj_key" + (i % 10));
        }
        long end = System.currentTimeMillis();
        System.out.println("total: " + (end - start));
    }

    @Test
    public void ordinalsTest() {
        long start = System.currentTimeMillis();
        String msg = String.valueOf(fl.getMessage("test.message.m", 21));
        System.out.println(msg);

        long end = System.currentTimeMillis();
        System.out.println("total time: " + (end - start));
    }

    @Test
    public void loadOrdinalsTest() throws IOException {
        long start = System.currentTimeMillis();
        fl.getLoader().load("ordinals_plurals_sr-RS.properties");
        for (int i = 0; i < 100000; ++i) {
            String msg = fl.getMessage("my.message", i, "m");
//            System.out.println(msg);
             msg = fl.getMessage("my.message", i, "f");
//            System.out.println(msg);
             msg = fl.getMessage("my.message", i, "n");
//            System.out.println(msg);
        }
        long end = System.currentTimeMillis();
        System.out.println("loadOrdinalsTest end time: " + (end - start) + "ms");
    }

    @Test
    public void variableTest() {
        long start = System.currentTimeMillis();

        String msg = fl.getMessage("direct.ordinal");
        System.out.println(msg);

        long end = System.currentTimeMillis();
        System.out.println("loadOrdinalsTest end time: " + (end - start) + "ms");
    }

    @Test
    public void t() {

        String welcomePattern = "Welcome {0}, {1}";
        String moneyPattern = "You have {0, plural, "
                + "zero {zero dollars} "
                + "one {{0, number, currency} dollar} "
                + "few {few dollars} "
                + "other {{0, number, currency} dollars}} to the date {1, date, short}";
        MessageFormat welcomeMsg = new MessageFormat(welcomePattern);
        MessageFormat moneyMsg = new MessageFormat(moneyPattern);

        int startLoop = 1000;
        int loopSize = startLoop + 1000;
        long start;
        long end;

       

        start = System.currentTimeMillis();
        for (int i = startLoop; i < loopSize; i++) {
            String out = welcomeMsg.format(new Object[]{"Jon Doe", i});
            String out2 = moneyMsg.format(new Object[]{i, new Date()}); // Use i (unbounded) to match your test
//            System.out.println(out);
//            System.out.println(out2);
        }
        end = System.currentTimeMillis();
        System.out.println("ICU4J time: " + (end - start) + " ms");
        
         start = System.currentTimeMillis();
        for (int i = startLoop; i < loopSize; i++) {
            String out = fl.getMessage("welcome.msg", "Jon Doe", i); // Simple
            
//            System.out.println(new Double(112343));
            String out2 = fl.getMessage("money.in.wallet.s", i, new Date()); // Complex
//            System.out.println(out);
//            System.out.println(out2);
        }

        end = System.currentTimeMillis();
        System.out.println("Fllex time: " + (end - start) + " ms");

    }

}
