package io.lucalabs.expenses.utils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import android.util.Log;
import java.util.Objects;

import io.lucalabs.expenses.models.annotations.Arg;

public class ArgumentComparator {

    /**
     * Compares two objects based on their @Arg annotated fields
     * @return true if equal the objects are equal; else false.
     */
    public static boolean haveEqualArgs(Object o1, Object o2) {

        if(!o1.getClass().equals(o2.getClass()))
            return false;

        for(Field f : getAnnotatedFields(o1.getClass())) {
            f.setAccessible(true);
            try {
                Log.d("ArgumentComparator", "Compared " + f.getName() + ": " + f.get(o1) + " vs " + f.get(o2));
                if (!Objects.equals(f.get(o1), f.get(o2))) {
                    Log.d("ArgumentComparator", "Not equal");
                    return false;
                }
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    /**
     * @return List of all @Arg annotated fields for a given class
     */
    private static ArrayList<Field> getAnnotatedFields(Class objectClass) {
        ArrayList<Field> fieldList = new ArrayList();

        for (Field f : objectClass.getDeclaredFields())
            if (f.isAnnotationPresent(Arg.class))
                fieldList.add(f);

        return fieldList;
    }
}
