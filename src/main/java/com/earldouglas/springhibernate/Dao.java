package com.earldouglas.springhibernate;

import java.io.Serializable;
import java.util.List;

public interface Dao {

    public <I extends Identifiable<S>, S extends Serializable> List<I> get(Class<I> identifiableClass);

    public <I extends Identifiable<S>, S extends Serializable> I get(Class<I> identifiableClass, S identifier);

    public <I extends Identifiable<S>, S extends Serializable> int count(Class<I> identifiableClass);

    public <I extends Identifiable<S>, S extends Serializable> List<I> get(Class<I> identifiableClass, int firstResult, int maxResults);

    public <I extends Identifiable<S>, S extends Serializable> List<I> get(Class<I> identifiableClass, String[] names, Object[] values);

    public <I extends Identifiable<S>, S extends Serializable> int count(Class<I> identifiableClass, String[] names, Object[] values);

    public <I extends Identifiable<S>, S extends Serializable> List<I> get(Class<I> identifiableClass, String[] names, Object[] values, int firstResult, int maxResults);

    public <I extends Identifiable<S>, S extends Serializable> S save(I identifiable);

    public <I extends Identifiable<S>, S extends Serializable> void delete(I identifiable);
}
