package com.earldouglas.springhibernate;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
@SuppressWarnings("serial")
public class TestIdentifiable implements Identifiable<String> {

    private String id;
    private String data;

    @Id
    public String getId() { return id; }

    public void setId(String id) { this.id = id; }

    public String getData() { return data; }

    public void setData(String data) { this.data = data; }

    @Transient
    public String getIdentifier() { return getId(); }

    public void setIdentifier(String identifier) { setId(identifier); }
}
