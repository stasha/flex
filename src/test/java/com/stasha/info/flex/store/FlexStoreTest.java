package com.stasha.info.flex.store;

import com.stasha.info.flex.store.FlexStoreImpl;
import com.stasha.info.flex.store.FlexStringStoreImpl;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 *
 * @author stasha
 */
public class FlexStoreTest {

    @Test
    public void flexStoreTest() {
        FlexStringStoreImpl store = new FlexStringStoreImpl();
        FlexStoreImpl<List> l = new FlexStoreImpl<List>();
    }
}
