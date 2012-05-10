package com.earldouglas.springhibernate;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.dao.DataAccessException;
import org.springframework.orm.hibernate3.HibernateCallback;

public class HibernateTemplate extends org.springframework.orm.hibernate3.HibernateTemplate {

    public int count(Class<?> entityClass) throws DataAccessException {
        return countByCriteria(DetachedCriteria.forClass(entityClass));
    }

    public int countByCriteria(final DetachedCriteria criteria) throws DataAccessException {
        Integer count = executeWithNativeSession(new HibernateCallback<Integer>() {
            public Integer doInHibernate(Session session) throws HibernateException {
                Criteria executableCriteria = criteria.getExecutableCriteria(session);
                executableCriteria.setProjection(Projections.rowCount());
                for (Object result : executableCriteria.list()) {
                    if (result instanceof Integer) {
                        return (Integer) result;
                    }
                }
                return -1;
            }
        });

        return count.intValue();
    }

    public <A> List<A> findByExample(Class<A> entityClass, String[] names, Object[] values) throws DataAccessException {
        return findByExample(entityClass, names, values, -1, -1);
    }

    @SuppressWarnings("unchecked")
    public <A> List<A> findByExample(Class<A> entityClass, String[] names, Object[] values, int firstResult, int maxResults) throws DataAccessException {
        DetachedCriteria detachedCriteria = DetachedCriteria.forClass(entityClass);
        for (int index = 0; index < Math.min(names.length, values.length); index++) {
            detachedCriteria.add(Restrictions.eq(names[index], values[index]));
        }
        return findByCriteria(detachedCriteria, firstResult, maxResults);
    }
}
