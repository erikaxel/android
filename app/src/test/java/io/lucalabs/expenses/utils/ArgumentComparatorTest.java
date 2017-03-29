package io.lucalabs.expenses.utils;

import org.junit.Test;

import io.lucalabs.expenses.models.Receipt;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

public class ArgumentComparatorTest {

    @Test
    public void haveEqualArgs_comparesReceiptsCorrectly() {
        Receipt receipt1 = new Receipt();
        Receipt receipt2 = new Receipt();
        assertTrue("new receipts are equal", ArgumentComparator.haveEqualArgs(receipt1, receipt2));

        receipt1.setMerchant_name("changed");
        assertFalse("Arg is different", ArgumentComparator.haveEqualArgs(receipt1, receipt2));

        receipt2.setMerchant_name("changed");
        assertTrue("Arg is same", ArgumentComparator.haveEqualArgs(receipt1, receipt2));
    }
}
