package facades;

import entities.Role;
import entities.User;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import errorhandling.AuthenticationException;
import java.util.List;
import javax.persistence.TypedQuery;

/**
 * @author amanda, benjamin, amalie
 */
public class UserFacade {

    private static EntityManagerFactory emf;
    private static UserFacade instance;

    private UserFacade() {
    }

    /**
     *
     * @param _emf
     * @return the instance of this facade.
     */
    public static UserFacade getUserFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new UserFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public User getVeryfiedUser(String username, String password) throws AuthenticationException {
        EntityManager em = getEntityManager();
        User user;
        try {
            user = em.find(User.class, username);
            if (user == null || !user.verifyPassword(password)) {
                throw new AuthenticationException("Invalid username or password! Please try again");
            }
        } finally {
            em.close();
        }
        return user;
    }

    public void createUser(String username, String password) throws AuthenticationException {

        EntityManager em = getEntityManager();

        try {
            em.getTransaction().begin();

            TypedQuery<Role> query = em.createQuery("select role from Role role WHERE role.roleName =:role", Role.class);
            List<Role> userRole = query.setParameter("role", "user").getResultList();

            User user = em.find(User.class, username);
            if (user != null) {
                throw new AuthenticationException("Username is already in use");
            }

            user = new User(username, password);
            user.setRoleList(userRole);
            userRole.get(0).getUserList().add(user);

            em.persist(user);
            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }

}
