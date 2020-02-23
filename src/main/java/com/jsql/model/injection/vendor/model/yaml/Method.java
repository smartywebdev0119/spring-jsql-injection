
package com.jsql.model.injection.vendor.model.yaml;

import java.io.Serializable;

@SuppressWarnings("serial")
public class Method implements Serializable {

    private String name = "";
    private String query = "";
    private Integer capacity = 0;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getQuery() {
        return this.query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public Integer getCapacity() {
        return this.capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

}
