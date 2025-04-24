package com.stasha.info.flexlocalization;

import com.stasha.info.flex.FlexLocalization;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class FlexLocalizationTest {

    private final Map<String, String> stringStore = new HashMap<>(50);
    private final Map<String, String> localizationDefaults = new HashMap<>(50);

    private final Calendar cal = Calendar.getInstance();
    private FlexLocalization fl = new FlexLocalization();

    @BeforeEach
    public void setUp() {

        stringStore.put("my.name", "Fajfrić Ljubomir");
        stringStore.put("my.location", "Fajfrić Ljubomir {0}.fmt.uppercase");
        stringStore.put("different.items", "There are {0}.fmt.decimal different items");
        stringStore.put("my.sallary", "Sallary for {0} is {1}.fmt.currency");
        stringStore.put("my.sallary.to.date", "Sallary for {0} is {1}.fmt.currency to date {2}.fmt.date");
        stringStore.put("money.in.wallet", "You have {0 [0=zero dollars, 1={0}.fmt.currency dollar, 2-4=few dollars, other={0}.fmt.currency dollars]} to the date {1}.fmt.date");
        stringStore.put("fmt.date", "dd.MM.yyyy");

        localizationDefaults.put("fmt.locale", "en-US");
        localizationDefaults.put("fmt.date", "YYYY-MM-DD");
        localizationDefaults.put("fmt.time", "HH:mm:ss");
        localizationDefaults.put("fmt.decimal", "#,##0.###");
        localizationDefaults.put("fmt.currency", "¤#,##0.00");
        localizationDefaults.put("fmt.uppercase", "{0}");

        localizationDefaults.putAll(stringStore);

        fl.getLoader().load(localizationDefaults);

    }

    @Test
    public void getMessageTest() {
        cal.set(2025, 3, 18);

        long start = System.currentTimeMillis();
        fl.getMessage("my.name");
        fl.getMessage("my.location", "is at home");
        fl.getMessage("different.items", 2234000);
        fl.getMessage("my.sallary", "Fajfrić Ljubomir", 32);

        fl.getMessage("my.sallary.to.date", "Fajfrić Ljubomir", 32, cal.getTime());
        fl.getMessage("money.in.wallet", 0, cal.getTime());
        fl.getMessage("money.in.wallet", 1, cal.getTime());
        fl.getMessage("money.in.wallet", 2, cal.getTime());
        fl.getMessage("money.in.wallet", 3, cal.getTime());
        fl.getMessage("money.in.wallet", 4, cal.getTime());
        fl.getMessage("money.in.wallet", 32, cal.getTime());
        long end = System.currentTimeMillis();
        System.out.println("total time: " + (end - start));

        start = System.currentTimeMillis();
        for (int i = 0; i < 300000; i++) {
            fl.getMessage("welcome.msg", "Jon Doe", i); // Simple
            fl.getMessage("money.in.wallet", i % 5, new java.util.Date()); // Complex
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
    public void substringTest() {
        
    }
}
