package com.earldouglas.springhibernate;

import java.io.Serializable;

public interface Identifiable<I extends Serializable> extends Serializable {

    public I getIdentifier();

    public void setIdentifier(I identifier);
}