package org.hibernate.bugs;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.UUID;

/**
 * ISSUE 12069
 * This template demonstrates how to develop a test case for Hibernate ORM, using the Java Persistence API.
 */
public class JPAUnitTestCase {


	private final UUID uuidUserWhichNotWork_A = UUID.randomUUID();
	private final UUID uuidUserWhichWork_B = UUID.randomUUID();

	private EntityManagerFactory entityManagerFactory;
	private EntityManager em;


	private User generateUserWhichDoAError(){

		final User userNotWork = new User();
		userNotWork.setId(1);
		userNotWork.setUuid(uuidUserWhichNotWork_A);
		userNotWork.setName("A Not work");

		return userNotWork;
	}

	private User generateUserWithNoError(){

		final User userWork = new User();
		userWork.setId(2);
		userWork.setUuid(uuidUserWhichWork_B);
		userWork.setName("B work");

		final Team team = new Team();
		team.setName("A team");
		userWork.setTeams(Collections.singletonList(team));
		return userWork;
	}

	@Before
	public void init() {
		entityManagerFactory = Persistence.createEntityManagerFactory( "templatePU" );
		em = entityManagerFactory.createEntityManager();
		em.getTransaction().begin();
		em.persist(generateUserWhichDoAError());
		em.persist(generateUserWithNoError());
		em.flush();
		em.getTransaction().commit();
		em.clear();
	}

	@After
	public void destroy() {
		em.close();
		entityManagerFactory.close();
	}

	/**
	 * The user has no team. The collection teams is empty or null
	 * ERROR due to CollectionKey generation
	 * @throws Exception
	 */
	@Test
	public void hhh12069_TestFail_Test() throws Exception {
		final User u = em.find(User.class, uuidUserWhichNotWork_A);
		Assert.assertNotNull(u);
		Assert.assertEquals(u.getUuid(), uuidUserWhichNotWork_A);
	}

	/**
	 * The user has a team. The collection teams is not empty.
	 * NO ERROR
	 * @throws Exception
	 */
	@Test
	public void hhh12069_TestSuccess_Test() throws Exception {
		final User u = em.find(User.class, uuidUserWhichWork_B);
		Assert.assertNotNull(u);
		Assert.assertEquals(u.getUuid(), uuidUserWhichWork_B);
	}

	/**
	 * Work ! If we use a query builder
	 */
	@Test
	public void hhh12069_TestSuccess_Test2() {
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
		final Root<User> criteriaFrom = criteriaQuery.from(User.class);
		final Predicate condition = criteriaBuilder.equal(criteriaFrom.get("uuid"), uuidUserWhichNotWork_A);
		criteriaQuery.where(condition);
		final TypedQuery<User> query = em.createQuery(criteriaQuery);
		final User result = query.getSingleResult();
	}

	/**
	 * Work because user has a team and we set on entity User the fetch mode to EAGER
	 */
	@Test
	public void hhh12069_TestSuccess_Test3() {
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
		final Root<User> criteriaFrom = criteriaQuery.from(User.class);
		final Predicate condition = criteriaBuilder.equal(criteriaFrom.get("uuid"), uuidUserWhichWork_B);
		criteriaQuery.where(condition);
		final TypedQuery<User> query = em.createQuery(criteriaQuery);
		final User result = query.getSingleResult();
	}

	/**
	 * Work ! If we use a query builder
	 */
	@Test
	public void hhh12069_TestSuccess_Test4() {
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
		final Root<User> criteriaFrom = criteriaQuery.from(User.class);
		criteriaFrom.fetch("teams", JoinType.LEFT);
		final Predicate condition = criteriaBuilder.equal(criteriaFrom.get("uuid"), uuidUserWhichNotWork_A);
		criteriaQuery.where(condition);
		final TypedQuery<User> query = em.createQuery(criteriaQuery);
		final User result = query.getSingleResult();
	}

	/**
	 * Work because user has a team and we set on entity User the fetch mode to EAGER
	 */
	@Test
	public void hhh12069_TestSuccess_Test5() {
		final CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
		final CriteriaQuery<User> criteriaQuery = criteriaBuilder.createQuery(User.class);
		final Root<User> criteriaFrom = criteriaQuery.from(User.class);
		criteriaFrom.fetch("teams", JoinType.LEFT);
		final Predicate condition = criteriaBuilder.equal(criteriaFrom.get("uuid"), uuidUserWhichWork_B);
		criteriaQuery.where(condition);
		final TypedQuery<User> query = em.createQuery(criteriaQuery);
		final User result = query.getSingleResult();
	}
}
