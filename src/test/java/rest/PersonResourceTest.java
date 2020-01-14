/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import entities.Address;
import entities.Hobby;
import entities.Person;
import entities.Role;
import entities.User;
import facades.PersonFacade;
import io.restassured.RestAssured;
import static io.restassured.RestAssured.given;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import java.net.URI;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import utils.EMF_Creator;

/**
 *
 * @author sofieamalielandt
 */
public class PersonResourceTest {

    private static EntityManagerFactory emf;
    private static PersonFacade facade;

    private Person p1;
    private Person p2;
    private Hobby hobby1;
    private Hobby hobby2;
    private Hobby hobby3;
    private Address address1;
    private Address address2;

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {

        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactory(EMF_Creator.DbSelector.TEST, EMF_Creator.Strategy.CREATE);

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void tearDownClass() {

        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();
        httpServer.shutdownNow();
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

        Role adminRole = new Role("admin");
        User admin = new User("admin", "test");
        admin.addRole(adminRole);

        try {
            em.getTransaction().begin();
            em.createNamedQuery("Hobby.deleteAllRows").executeUpdate();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.createNamedQuery("Address.deleteAllRows").executeUpdate();
            em.createQuery("delete from User").executeUpdate();
            em.createQuery("delete from Role").executeUpdate();

            em.persist(adminRole);
            em.persist(admin);
            em.persist(p1);
            em.persist(p2);

            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }

    private static String securityToken;

    //Utility method to login and set the returned securityToken
    private static void login(String username, String password) {
        String json = String.format("{username: \"%s\", password: \"%s\"}", username, password);
        securityToken = given()
                .contentType("application/json")
                .body(json)
                //.when().post("/api/login")
                .when().post("/login")
                .then()
                .extract().path("token");
        System.out.println("TOKEN ---> " + securityToken);
    }

    private void logOut() {
        securityToken = null;
    }

    /**
     * Test of addPerson method, of class PersonResource.
     */
    @Test
    public void testAddPerson() {
        System.out.println("addPerson");

        String payload = "{\"firstName\": \"Test\","
                + "\"lastName\": \"Testen\","
                + "\"email\": \"bum@hotmail.com\","
                + "\"phone\": \"11993344\","
                + "\"street\": \"Testvej\","
                + "\"city\": \"Testby\","
                + "\"zip\": \"2230\"}";

        login("admin", "test");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .body(payload)
                .when()
                .post("/person").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstName", equalTo("Test"), "lastName", equalTo("Testen"));
    }

    /**
     * Test of addPerson method, of class PersonResource.
     */
    @Test
    public void testAddPersonNotAuthenticated() {
        System.out.println("addPerson - Not authenticated");

        String payload = "{\"firstName\": \"Test\","
                + "\"lastName\": \"Testen\","
                + "\"email\": \"bum@hotmail.com\","
                + "\"phone\": \"11993344\","
                + "\"street\": \"Testvej\","
                + "\"city\": \"Testby\","
                + "\"zip\": \"2230\"}";
        logOut();
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .body(payload)
                .when()
                .post("/person").then()
                .statusCode(403)
                .assertThat()
                .body("code", equalTo(403))
                .body("message", equalTo("Not authenticated - do login"));
        
    }

    /**
     * Test of editPerson method, of class PersonResource.
     */
    @Test
    public void testEditPerson() {
        System.out.println("editPerson");

        String payload = "{\"firstName\": \"" + p1.getFirstName() + "\","
                + "\"lastName\": \"Testen\","
                + "\"email\": \"" + p1.getEmail() + "\","
                + "\"phone\": \"" + p1.getPhone() + "\","
                + "\"street\": \"Ellevej\","
                + "\"city\": \"Solrød Strand\","
                + "\"zip\": \"2680\"}";

        login("admin", "test");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .body(payload)
                .when()
                .put("/person/" + p1.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstName", equalTo(p1.getFirstName()), "lastName", equalTo("Testen"), "street", equalTo("Ellevej"), "zip", equalTo(2680));
    }

    /**
     * Test of deletePerson method, of class PersonResource.
     */
    @Test
    public void testDeletePerson() {
        System.out.println("deletePerson");

        login("admin", "test");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .when()
                .delete("/person/" + p1.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("status", equalTo("The person has been deleted"));

    }

    /**
     * Test of getPerson method, of class PersonResource.
     */
    @Test
    public void testGetPerson() {
        System.out.println("getPerson");
        given()
                .contentType("application/json")
                .get("/person/" + p1.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstName", equalTo(p1.getFirstName()),
                        "lastName", equalTo(p1.getLastName()),
                        "hobbies.name", hasItems(hobby1.getName()),
                        "hobbies.name", hasItems(hobby2.getName()),
                        "street", equalTo(address1.getStreet()));
    }

    /**
     * Test of getAllPerson method, of class PersonResource.
     */
    @Test
    public void testGetAllPerson() {
        System.out.println("getAllPerson");

        given().contentType("application/json")
                .get("/person/all").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstName", hasItems(p1.getFirstName(), p2.getFirstName()),
                        "lastName", hasItems(p1.getLastName(), p2.getLastName()),
                        "street", hasItems(p1.getAddress().getStreet(), p2.getAddress().getStreet()));
    }

    /**
     * Test of getPersonByHobby method, of class PersonResource.
     */
    @Test
    public void testGetPersonByHobby() {
        System.out.println("getPersonByHobby");

        given()
                .contentType("application/json")
                .get("/person/hobby/" + hobby2.getName()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstName", hasItems(p1.getFirstName(), p2.getFirstName()),
                        "lastName", hasItems(p1.getLastName(), p2.getLastName()));
    }

    /**
     * Test of getAllHobbies method, of class PersonResource.
     */
    @Test
    public void testGetAllHobbies() {
        System.out.println("getAllHobbies");

        given()
                .contentType("application/json")
                .get("/person/hobby/" + hobby2.getName()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("firstName", hasItems(p1.getFirstName(), p2.getFirstName()),
                        "lastName", hasItems(p1.getLastName(), p2.getLastName()));

    }

    /**
     * Test of addHobby method, of class PersonResource.
     */
    @Test
    public void testAddHobby() {
        System.out.println("addHobby");

        String payload = "{\"name\": \"swimming\","
                + "\"description\": \"You will for sure be wet\"}";

        login("admin", "test");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .body(payload)
                .when()
                .post("/person/hobby").then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("name", equalTo("swimming"), "description", equalTo("You will for sure be wet"));
    }

    /**
     * Test of editHobby method, of class PersonResource.
     */
    @Test
    public void testEditHobby() {
        System.out.println("editHobby");
        String payload = "{\"name\": \"" + hobby1.getName() + "\","
                + "\"description\": \"Coding is super fun\"}";

        login("admin", "test");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .body(payload)
                .when()
                .put("/person/hobby/" + hobby1.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("name", equalTo(hobby1.getName()), "description", equalTo("Coding is super fun"));

    }

    /**
     * Test of deleteHobby method, of class PersonResource.
     */
    @Test
    public void testDeleteHobby() {
        System.out.println("deleteHobby");

        login("admin", "test");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .when()
                .delete("/person/hobby/" + hobby3.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("status", equalTo("The hobby has been deleted"));
    }

    /**
     * Test of addHobbyToPerson method, of class PersonResource.
     */
    @Test
    public void testAddHobbyToPerson() {
        System.out.println("addHobbyToPerson");

        login("admin", "test");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .when()
                .put("/person/" + p2.getId() + "/" + hobby3.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("status", equalTo("The hobby has been added"));
    }

    /**
     * Test of removeHobbyFromPerson method, of class PersonResource.
     */
    @Test
    public void testRemoveHobbyFromPerson() {
        System.out.println("removeHobbyFromPerson");

        login("admin", "test");
        given()
                .contentType("application/json")
                .accept(ContentType.JSON)
                .header("x-access-token", securityToken)
                .when()
                .put("/person/hobby/" + p2.getId() + "/" + hobby2.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("status", equalTo("The hobby has been removed"));
    }
}
