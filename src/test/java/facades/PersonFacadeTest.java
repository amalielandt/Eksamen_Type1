/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package facades;

import DTO.HobbyDTO;
import DTO.PersonDTO;
import entities.Address;
import entities.Hobby;
import entities.Person;
import errorhandling.NotFoundException;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.WebApplicationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import utils.EMF_Creator;

/**
 *
 * @author sofieamalielandt
 */
public class PersonFacadeTest {

    private static EntityManagerFactory emf;
    private static PersonFacade facade;

    private Person p1;
    private Person p2;
    private Hobby hobby1;
    private Hobby hobby2;
    private Hobby hobby3;
    private Address address1;
    private Address address2;

    public PersonFacadeTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactory(EMF_Creator.DbSelector.TEST, EMF_Creator.Strategy.DROP_AND_CREATE);
        facade = PersonFacade.getPersonFacade(emf);
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {

        EntityManager em = emf.createEntityManager();

        address1 = new Address("Gåhjemvej", "Roskilde", 4000);
        address2 = new Address("Gadevejen", "Maribo", 4930);

        p1 = new Person("jim@gmail.com", "Jim", "Carrey", 33445566, address1);
        p2 = new Person("bill@gmail.com", "Bill", "Cosby", 11223344, address2);
        hobby1 = new Hobby("programming", "the future of mankind is programming, also good for making a blog about your dog pictures");
        hobby2 = new Hobby("jumping", "super fun and easy");
        hobby3 = new Hobby("handball", "Team sport");

        address1.addPerson(p1);
        p1.addHobby(hobby1);
        p1.addHobby(hobby2);
        p1.addHobby(hobby3);

        address2.addPerson(p2);
        p2.addHobby(hobby2);
        p2.addHobby(hobby1);

        try {
            em.getTransaction().begin();
            em.createNamedQuery("Hobby.deleteAllRows").executeUpdate();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.createNamedQuery("Address.deleteAllRows").executeUpdate();

            em.persist(p1);
            em.persist(p2);

            em.getTransaction().commit();

        } finally {
            em.close();
        }

    }

    /**
     * Test of addPerson method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testAddPerson() throws NotFoundException {
        System.out.println("addPerson");

        int personsbefore = facade.getAllPersons().size();
        PersonDTO p = new PersonDTO("joe", "ordenary", "test@testmail.dk", "44556677", address1.getStreet(), address1.getCity(), Integer.toString(address1.getZip()));
        facade.addPerson(p);
        int personsafter = facade.getAllPersons().size();

        assertTrue(personsbefore < personsafter);
    }

    /**
     * Test of editPerson method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testEditPerson() throws NotFoundException {
        System.out.println("editPerson");

        p1.setFirstName("Bob");
        p1.setLastName("Marley");
        p1.setAddress(address2);
        p1.getAddress().setStreet("Langgade");

        PersonDTO person = new PersonDTO(p1);

        facade.editPerson(person);
        person = facade.getPerson(p1.getId());

        assertEquals("Bob", person.getFirstName());
        assertEquals("jim@gmail.com", person.getEmail());
        assertEquals(address2.getStreet(), person.getStreet());
    }

    /**
     * Test of deletePerson method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testDeletePerson() throws NotFoundException {
        System.out.println("deletePerson");

        int personsbefore = facade.getAllPersons().size();
        facade.deletePerson(p1.getId());
        int personsafter = facade.getAllPersons().size();

        assertTrue(personsbefore > personsafter);
    }

    /**
     * Test of getPerson method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testGetPerson() throws NotFoundException {
        System.out.println("getPerson");

        PersonDTO result = facade.getPerson(p2.getId());

        assertEquals(p2.getFirstName(), result.getFirstName());
        assertEquals(p2.getEmail(), result.getEmail());
        assertEquals(2, result.getHobbies().size());
    }

    /**
     * Test of getAllPersons method, of class PersonFacade.
     */
    @Test
    public void testGetAllPersons() {
        System.out.println("getAllPersons");
        List<PersonDTO> persons = facade.getAllPersons();
        assertEquals(persons.size(), 2);
    }

    /**
     * Test of getPersonsByHobby method, of class PersonFacade.
     */
    @Test
    public void testGetPersonsByHobby() {
        System.out.println("getPersonsByHobby");

        int result = facade.getPersonsByHobby(hobby1.getName()).size();
        assertEquals(2, result);
        
        result = facade.getPersonsByHobby("Not a hobby").size();
        assertEquals(0, result);
    }

    /**
     * Test of getAllHobbies method, of class PersonFacade.
     */
    @Test
    public void testGetAllHobbies() {
        System.out.println("getAllHobbies");

        int result = facade.getAllHobbies().size();
        assertEquals(3, result);
    }

    /**
     * Test of addHobby method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testAddHobby() throws NotFoundException {
        System.out.println("addHobby");
        
        int hobbiesbefore = facade.getAllHobbies().size();
        HobbyDTO h = new HobbyDTO("singing", "Very loud and fake");
        facade.addHobby(h);
        int hobbiesafter = facade.getAllHobbies().size();

        assertTrue(hobbiesbefore < hobbiesafter);
    }
    
    /**
     * Test of addHobby method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testAddHobbyAlreadyExsists() throws NotFoundException {
        System.out.println("addHobby - already exsists");

        try {
            facade.addHobby(new HobbyDTO(hobby1));
            fail();
        } catch (NotFoundException ex) {

            assertEquals(ex.getMessage(), "Hobby already exsists");

        }
    }

    /**
     * Test of editHobby method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testEditHobby() throws NotFoundException {
        System.out.println("editHobby");
        
        hobby3.setDescription("Fun game like football but with hands, and a smaller ball");
        HobbyDTO hobby = new HobbyDTO(hobby3);

        facade.editHobby(hobby);
        List<Hobby> result = facade.getHobby(hobby3.getName());

        assertEquals("Fun game like football but with hands, and a smaller ball", result.get(0).getDescription());
        
    }

    /**
     * Test of deleteHobby method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testDeleteHobby() throws NotFoundException {
        System.out.println("deleteHobby");
        
        int hobbiesbefore = facade.getAllHobbies().size();
        facade.deleteHobby(hobby3.getId());
        int hobbiesafter = facade.getAllHobbies().size();

        assertTrue(hobbiesbefore > hobbiesafter);
    
    }

    /**
     * Test of addHobbyToPerson method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testAddHobbyToPerson() throws NotFoundException {
        System.out.println("addHobbyToPerson");
        
        int before = p2.getHobbies().size();
        facade.addHobbyToPerson(p2.getId(), hobby3.getId());
        PersonDTO person = facade.getPerson(p2.getId());
        int after = person.getHobbies().size();
        
        assertTrue(before < after);
        
        
    }

    /**
     * Test of removeHobbyFromPerson method, of class PersonFacade.
     * @throws errorhandling.NotFoundException
     */
    @Test
    public void testRemoveHobbyFromPerson() throws NotFoundException {
        System.out.println("addHobbyToPerson");
        
        int before = p2.getHobbies().size();
        facade.removeHobbyFromPerson(p2.getId(), hobby2.getId());
        PersonDTO person = facade.getPerson(p2.getId());
        int after = person.getHobbies().size();
        
        assertTrue(before > after);
        
    }
}
