package io.lucalabs.expenses.models;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;

public class ApiRequestObjectTest {

    @Test
    public void buildParams_buildsCorrectReceiptParams() {
        Receipt receipt = new Receipt();
        receipt.setMerchant_name("my merchant");
        receipt.setAmount_cents(2040L);

        ApiRequestObject apiRequestObject = new ApiRequestObject(receipt);

        for(String[] s : apiRequestObject.getParams()){
            System.out.println("key: " + s[0] + ", value: " + s[1]);
        }

        assertEquals("adds all annotated set fields", 3, apiRequestObject.getParams().size());

        String[] firstArgument = apiRequestObject.getParams().get(0);
        assertEquals("sets correct arg name", "receipt[merchant_name]", firstArgument[0]);
        assertEquals("sets correct arg value", "my merchant", firstArgument[1]);
        assertEquals("converts long", "20.4", apiRequestObject.getParams().get(1)[1]);
    }
}
