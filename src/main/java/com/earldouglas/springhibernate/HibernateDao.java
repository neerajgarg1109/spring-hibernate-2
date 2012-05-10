package com.earldouglas.springhibernate;

import java.io.Serializable;
import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

@SuppressWarnings("unchecked")
public class HibernateDao implements Dao {

    private HibernateTemplate hibernateTemplate;

    public <I extends Identifiable<S>, S extends Serializable> List<I> get(Class<I> identifiableClass, String[] names, Object[] values) {
        return hibernateTemplate.findByExample(identifiableClass, names, values);
    }

    public <I extends Identifiable<S>, S extends Serializable> I get(Class<I> identifiableClass, S identifier) {
        return (I) hibernateTemplate.load(identifiableClass, identifier);
    }

    public <I extends Identifiable<S>, S extends Serializable> int count(Class<I> identifiableClass, String[] names, Object[] values) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(identifiableClass);
        for (int index = 0; index < Math.min(names.length, values.length); index++) {
            detachedCriteria.add(Restrictions.ilike(names[index], values[index]));
        }
        return hibernateTemplate.countByCriteria(detachedCriteria);
    }

    public <I extends Identifiable<S>, S extends Serializable> List<I> get(Class<I> identifiableClass, String[] names, Object[] values, int firstResult,
            int maxResults) {
        return hibernateTemplate.findByExample(identifiableClass, names, values, firstResult, maxResults);
    }

    public <I extends Identifiable<S>, S extends Serializable> List<I> get(Class<I> identifiableClass) {
        return hibernateTemplate.loadAll(identifiableClass);
    }

    public <I extends Identifiable<S>, S extends Serializable> int count(Class<I> identifiableClass) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(identifiableClass);
        return hibernateTemplate.countByCriteria(detachedCriteria);
    }

    public <I extends Identifiable<S>, S extends Serializable> List<I> get(Class<I> identifiableClass, int firstResult, int maxResults) {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(identifiableClass);
        return hibernateTemplate.findByCriteria(detachedCriteria, firstResult, maxResults);
    }

    public <I extends Identifiable<S>, S extends Serializable> S save(I identifiable) {
        if (identifiable.getIdentifier() == null) {
            return (S) hibernateTemplate.save(identifiable);
        } else {
            hibernateTemplate.saveOrUpdate(identifiable);
            return identifiable.getIdentifier();
        }
    }

    public <I extends Identifiable<S>, S extends Serializable> void delete(I identifiable) {
        hibernateTemplate.delete(identifiable);
    }

    public <I extends Identifiable<S>, S extends Serializable> void setHibernateTemplate(HibernateTemplate hibernateTemplate) {
        this.hibernateTemplate = hibernateTemplate;
    }
}
