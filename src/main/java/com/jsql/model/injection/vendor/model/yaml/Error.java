
package com.jsql.model.injection.vendor.model.yaml;

import java.util.ArrayList;
import java.util.List;

public class Error {

    private List<Method> method = new ArrayList<>();

    public List<Method> getMethod() {
        return this.method;
    }

    public void setMethod(List<Method> method) {
        this.method = method;
    }

}
