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
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.ws.rs.WebApplicationException;
import utils.EMF_Creator;

/**
 *
 * @author sofieamalielandt
 */
public class PersonFacade {

    private static EntityManagerFactory emf;
    private static PersonFacade instance;

    //Private Constructor to ensure Singleton
    private PersonFacade() {
    }

    public static PersonFacade getPersonFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public PersonDTO addPerson(PersonDTO p) throws NotFoundException {

        EntityManager em = getEntityManager();

        try {
            em.getTransaction().begin();

            //Checking if information is not already used
            List<PersonDTO> persons = getAllPersons();
            for (PersonDTO person : persons) {

                if (p.getEmail().equals(person.getEmail()) && person.getId() != p.getId()) {
                    throw new NotFoundException("Email is already in use");
                }

                if (p.getPhone() == person.getPhone() && person.getId() != p.getId()) {
                    throw new NotFoundException("Phonenumber is already in use");
                }
            }

            //Checking if address already exsists in Database
            Address address;
            List<Address> addressDB = getAddress(p.getStreet(), p.getCity(), p.getZip());
            if (addressDB.size() > 0) {
                address = addressDB.get(0);
            } else {
                address = new Address(p.getStreet(), p.getCity(), p.getZip());
            }

            //Merging address
            Address mergedAddress = em.merge(address);

            Person person = new Person(p.getEmail(), p.getFirstName(), p.getLastName(), p.getPhone(), mergedAddress);
            mergedAddress.addPerson(person);

            em.persist(person);
            em.getTransaction().commit();

            return new PersonDTO(person);

        } finally {
            em.close();
        }
    }

    public PersonDTO editPerson(PersonDTO p) throws NotFoundException {

        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, p.getId());
        if (person == null) {

            throw new WebApplicationException("No person with the given id was found");
        }

        //Checking if information is not already used
        List<PersonDTO> persons = getAllPersons();
        for (PersonDTO per : persons) {

            if (p.getEmail().equals(per.getEmail()) && per.getId() != p.getId()) {
                throw new NotFoundException("Email is already in use");
            }

            if (p.getPhone() == per.getPhone() && per.getId() != p.getId()) {
                throw new NotFoundException("Phonenumber is already in use");
            }
        }

        //Checking if address already exsists in Database
        Address newAddress;
        List<Address> addressDB = getAddress(p.getStreet(), p.getCity(), p.getZip());
        if (addressDB.size() > 0) {
            newAddress = addressDB.get(0);
        } else {
            newAddress = new Address(p.getStreet(), p.getCity(), p.getZip());
        }

        //Adding edits on the person with the given id
        person.setFirstName(p.getFirstName());
        person.setLastName(p.getLastName());
        person.setEmail(p.getEmail());
        person.setPhone(p.getPhone());

        //Removing old address relations, for it to be deleted, if no one lives there
        Address oldAddress = person.getAddress();

        if (oldAddress.getPersons().size() == 1 && !oldAddress.equals(newAddress)) {
            oldAddress.removePerson(person);
        }

        try {
            em.getTransaction().begin();

            //Merging the new address
            Address mergedAddress = em.merge(newAddress);
            person.setAddress(mergedAddress);

            em.merge(person);
            em.persist(person);

            if (oldAddress.getPersons().isEmpty()) {
                em.remove(oldAddress);
            }
            em.getTransaction().commit();

            return new PersonDTO(person);
        } finally {
            em.close();
        }
    }

    public void deletePerson(long id) throws NotFoundException {
        EntityManager em = getEntityManager();
        Person person;

        try {
            em.getTransaction().begin();
            person = em.find(Person.class, id);

            //If this person is the only one living on the address, the address is deleted aswell
            if (person.getAddress().getPersons().size() == 1) {
                em.remove(person.getAddress());
            }

            em.remove(person);
            em.getTransaction().commit();
        } catch (Exception e) {
            throw new NotFoundException("The person could not be deleted");
        } finally {
            em.close();
        }
    }

    public PersonDTO getPerson(long id) throws NotFoundException {

        EntityManager em = getEntityManager();
        try {
            Person person = em.find(Person.class, id);
            if (person == null) {
                throw new NotFoundException("No person found with the given id");
            }
            PersonDTO personDTO = new PersonDTO(person);
            return personDTO;

        } finally {
            em.close();
        }
    }

    public List<PersonDTO> getAllPersons() {

        EntityManager em = getEntityManager();
        List<PersonDTO> personsDTO = new ArrayList();

        TypedQuery<Person> query = em.createQuery("SELECT p FROM Person p", Person.class);
        List<Person> persons = query.getResultList();

        persons.forEach((person) -> {
            personsDTO.add(new PersonDTO(person));
        });

        return personsDTO;

    }

    public List<PersonDTO> getPersonsByHobby(String hobby) {

        EntityManager em = getEntityManager();

        List<PersonDTO> personsDTO = new ArrayList();

        TypedQuery<Person> query = em.createQuery("SELECT p FROM Person p INNER JOIN p.hobbies pho WHERE pho.name = :hobby", Person.class);
        List<Person> persons = query.setParameter("hobby", hobby).getResultList();

        persons.forEach((person) -> {
            personsDTO.add(new PersonDTO(person));
        });
        return personsDTO;

    }

    public List<Hobby> getHobby(String name) {

        EntityManager em = getEntityManager();
        List<Hobby> hobby;
        TypedQuery<Hobby> query = em.createQuery("SELECT h FROM Hobby h WHERE h.name = :name", Hobby.class);
        hobby = query.setParameter("name", name).getResultList();
        return hobby;
    }

    public List<HobbyDTO> getAllHobbies() {

        EntityManager em = getEntityManager();
        List<HobbyDTO> hobbiesDTO = new ArrayList();

        TypedQuery<Hobby> query = em.createQuery("SELECT h FROM Hobby h", Hobby.class);
        List<Hobby> hobbies = query.getResultList();

        hobbies.forEach((hobby) -> {
            hobbiesDTO.add(new HobbyDTO(hobby));
        });

        return hobbiesDTO;

    }

    public HobbyDTO addHobby(HobbyDTO h) throws NotFoundException {

        EntityManager em = getEntityManager();

        try {
            em.getTransaction().begin();

            //Checking if hobby already exsists in Database
            Hobby newHobby;
            List<Hobby> hobbyDB = getHobby(h.getName().toLowerCase());
            if (hobbyDB.size() > 0) {
                throw new NotFoundException("Hobby already exsists");

            } else {
                newHobby = new Hobby(h.getName().toLowerCase(), h.getDescription());
                em.persist(newHobby);
            }

            em.getTransaction().commit();

            return new HobbyDTO(newHobby);

        } finally {
            em.close();
        }
    }

    public HobbyDTO editHobby(HobbyDTO h) throws NotFoundException {

        EntityManager em = getEntityManager();

        try {
            em.getTransaction().begin();
            Hobby hobby = em.find(Hobby.class, h.getId());
            if (hobby == null) {

                throw new NotFoundException("Hobby does not exsist");
            }

            hobby.setDescription(h.getDescription());
            hobby.setName(h.getName().toLowerCase());

            em.merge(hobby);
            em.getTransaction().commit();

            return new HobbyDTO(hobby);

        } finally {
            em.close();
        }

    }

    public void deleteHobby(long id) throws NotFoundException {
        EntityManager em = getEntityManager();
        List<Person> persons = getPersons();

        try {
            em.getTransaction().begin();
            Hobby hobby = em.find(Hobby.class, id);

            for (Person person : persons) {
                person.removeHobby(hobby);
            }

            em.remove(hobby);
            em.getTransaction().commit();

        } catch (Exception e) {

            throw new NotFoundException("The hobby could not be deleted");
        } finally {
            em.close();
        }
    }

    public void addHobbyToPerson(long person_id, long hobby_id) throws NotFoundException {
        EntityManager em = getEntityManager();

        try {
            em.getTransaction().begin();
            Hobby hobby = em.find(Hobby.class, hobby_id);
            if (hobby == null) {

                throw new NotFoundException("Hobby does not exsist");
            }

            Person person = em.find(Person.class, person_id);
            if (person == null) {

                throw new NotFoundException("Person does not exsist");
            }

            person.addHobby(hobby);

            em.merge(person);
            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }

    public void removeHobbyFromPerson(long person_id, long hobby_id) throws NotFoundException {
        EntityManager em = getEntityManager();
        Hobby hobbyToRemove = null;

        try {
            em.getTransaction().begin();
            Person person = em.find(Person.class, person_id);
            if (person == null) {

                throw new NotFoundException("Person does not exsist");
            }

            for (Hobby h : person.getHobbies()) {

                if (h.getId() == hobby_id) {
                    hobbyToRemove = h;
                }
            }

            if (hobbyToRemove != null) {
                person.removeHobby(hobbyToRemove);
            }

            em.merge(person);
            em.getTransaction().commit();

        } finally {
            em.close();
        }
    }

    private List<Address> getAddress(String street, String city, int zip) {

        EntityManager em = getEntityManager();
        List<Address> adr;
        TypedQuery<Address> query = em.createQuery("SELECT a FROM Address a WHERE a.street = :street AND a.city = :city AND a.zip = :zip", Address.class);
        adr = query.setParameter("street", street).setParameter("city", city).setParameter("zip", zip).getResultList();
        return adr;
    }

    private List<Person> getPersons() {

        EntityManager em = getEntityManager();

        TypedQuery<Person> query = em.createQuery("SELECT p FROM Person p", Person.class);
        List<Person> persons = query.getResultList();

        return persons;

    }

}
