# Database Access with Hibernate and Spring

_18 Mar 2009_

Hibernate's ability to simplify database access with Spring at the helm removes an almost unbelievable burden from enterprise development. Gone are the days of tedious SQL maintenance and overly agile trial-and-error development of homegrown data access objects. Hibernate and Spring take care of all but top-level configuration and implementation of database access. It is this level that will be demystified here, as a universally portable and minimalistic approach to database access is built on the shoulders of these giants.

Before diving into the intricacies of Hibernate or Spring, a general groundwork must be laid to define database access methods and persistable entities.

In practice, database persistence goes hand-in-hand with identifiable entities. Commonly in databases, tables will identify one or more columns as a primary key, effectively identifying each unique entity stored within the table.

```java
public interface Identifiable<I extends Serializable> extends Serializable {

    public I getIdentifier();

    public void setIdentifier(I identifier);
}
```

The `Identifiable` interface provides convenience methods for identifying any implementing class. This will enable primary key definition in any arbitrary business object, and can utilize any new or existing `Serializable` property as an identifier.

```java
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
```

An example of an `Identifiable` is in the sample `TestIdentifiable` class. It utilizes a `String` property called `id` to serve as the identifier, and will later serve as the primary key of the corresponding table. Note the annotations. With standard Java Persistence Annotations, `TestIdentifiable` is identified as a persistence entity, the `id` property is identified as the persistence identifier, and the `Identifiable` convenience methods have been identified as non-players in the database. This is important because for this class, the desired columns will store the `id` and `data` properties, with `id` as the identifier. Marking `getIdentifier()` as transient effectively removes it from consideration as its own column in the database. This is not necessarily required, and comes down to an object model design choice.

With the persistable object model out of the way, the persistence API can be defined. The idea is to leverage the generic nature of `Identifiable` entities and the power of Java generics to define an all-encompassing yet simple data access object interface.

```java
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
```

The `Dao` interface defines the common operations associated with database CRUD in a different, simplified way. There is no distinction between database inserts and updates, as both have been abstracted as a single save operation. Convenience methods for counting have been defined as well. The implementation of this interface will be the introduction of both Hibernate and Spring.

```java
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
```

`HibernateDao` makes heavy use of Spring's `HibernateTemplate` class, which has been extended to provide counting support and additional criteria-based searching.

```java
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
```

That's all there is to it. All that remains is to take it for a test drive.

```java
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration
public class HibernateDaoTest {

    @Autowired
    private HibernateDao hibernateDao;

    private Collection<TestIdentifiable> getTestIdentifiables() {
        Collection<TestIdentifiable> testIdentifiables = new ArrayList<TestIdentifiable>();

        for (int i = 0; i < 10; i++) {
            TestIdentifiable testIdentifiable = new TestIdentifiable();
            testIdentifiable.setId("test identifiable " + i);
            testIdentifiable.setData("some data for test identifiable " + i);

            testIdentifiables.add(testIdentifiable);
        }

        return testIdentifiables;
    }

    @Before
    public void populateDatabase() {
        for (TestIdentifiable testIdentifiable : getTestIdentifiables()) {
            hibernateDao.save(testIdentifiable);
        }
    }

    @After
    public void clearDatabase() {
        for (TestIdentifiable testIdentifiable : getTestIdentifiables()) {
            hibernateDao.delete(testIdentifiable);
        }
    }

    @Test
    public void testSaveGet() throws Exception {
        TestIdentifiable testIdentifiable = hibernateDao.get(TestIdentifiable.class, "test identifiable 5");

        TestCase.assertNotNull(testIdentifiable);
        TestCase.assertEquals(testIdentifiable.getId(), "test identifiable 5");
    }
}
```

```xml
<?xml version="1.0" encoding="ISO-8859-1"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.1.xsd">

    <bean id="hibernateDao" class="com.earldouglas.springhibernate.HibernateDao">
        <property name="hibernateTemplate" ref="hibernateTemplate" />
    </bean>

    <bean id="hibernateTemplate" class="com.earldouglas.springhibernate.HibernateTemplate">
        <property name="sessionFactory" ref="localSessionFactoryBean" />
    </bean>

    <bean id="localSessionFactoryBean"
        class="org.springframework.orm.hibernate3.annotation.AnnotationSessionFactoryBean"
        init-method="createDatabaseSchema">
        <property name="dataSource" ref="hsqlDataSource" />
        <property name="annotatedClasses">
            <list>
                <value>com.earldouglas.springhibernate.TestIdentifiable</value>
            </list>
        </property>
        <property name="hibernateProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.HSQLDialect</prop>
                <prop key="hibernate.show_sql">false</prop>
            </props>
        </property>
    </bean>

    <bean id="hsqlDataSource" class="org.apache.commons.dbcp.BasicDataSource"
        destroy-method="close">
        <property name="driverClassName" value="org.hsqldb.jdbcDriver" />
        <property name="url" value="jdbc:hsqldb:mem:mydatabase" />
        <property name="username" value="sa" />
        <property name="password" value="" />
    </bean>
</beans>
```

For more information, the [Spring Reference](http://static.springsource.org/spring/docs/3.1.x/spring-framework-reference/html/orm.html#orm-hibernate) goes into more detail about the underlying mechanisms that are used here. 
