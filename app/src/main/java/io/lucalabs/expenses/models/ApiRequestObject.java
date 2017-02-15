package io.lucalabs.expenses.models;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.lucalabs.expenses.models.annotations.Arg;

public class ApiRequestObject {
    private String url;
    private String requestMethod;
    private List<String[]> params;

    public ApiRequestObject() {
    }

    public ApiRequestObject(Object object) {
        try {
            buildParams(object);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void buildParams(Object object) throws IllegalArgumentException, IllegalAccessException {
        params = new ArrayList<>();

        for (Field f : object.getClass().getDeclaredFields()) {
            if (f.isAnnotationPresent(Arg.class)) {
                f.setAccessible(true);
                Object value = f.get(object);
                if(value instanceof Long) {
                    // We convert long to double by default. Perhaps this should be done somewhere else?
                    Long l = new Long((long) value);
                    String text = "" + l.doubleValue() / 100;
                    params.add(new String[]{f.getAnnotation(Arg.class).name(), text});
                }
                else if (value != null)
                    params.add(new String[]{f.getAnnotation(Arg.class).name(), value.toString()});
            }
        }
    }

    public void queue(){
//        FirebaseDatabase
    }

    public List<String[]> getParams() {
        return params;
    }

    public void setParams(List<String[]> params) {
        this.params = params;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }
}
