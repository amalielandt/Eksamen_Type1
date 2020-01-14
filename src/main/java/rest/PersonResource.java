/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rest;

import DTO.HobbyDTO;
import DTO.PersonDTO;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import errorhandling.NotFoundException;
import facades.PersonFacade;
import java.util.List;
import javax.annotation.security.RolesAllowed;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PUT;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import utils.EMF_Creator;

/**
 * REST Web Service
 *
 * @author sofieamalielandt
 */
@Path("person")
public class PersonResource {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory(EMF_Creator.DbSelector.DEV, EMF_Creator.Strategy.CREATE);
    private static final PersonFacade PF = PersonFacade.getPersonFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    @Context
    private UriInfo context;

    @POST
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    public PersonDTO addPerson(String personAsJSON) {

        try {
            PersonDTO person = GSON.fromJson(personAsJSON, PersonDTO.class);

            if (person.getFirstName() == null || person.getFirstName().isEmpty() || person.getFirstName().length() < 2) {

                throw new WebApplicationException("Firstname must be 2 characters", 400);
            }

            if (person.getFirstName().matches(".*\\d+.*")) {

                throw new WebApplicationException("Firstname must not contain digits", 400);
            }

            if (person.getLastName() == null || person.getLastName().isEmpty() || person.getLastName().length() < 2) {

                throw new WebApplicationException("Lastname must be 2 characters", 400);
            }

            if (person.getLastName().matches(".*\\d+.*")) {

                throw new WebApplicationException("Lastname must not contain digits", 400);
            }

            if (person.getEmail() == null || person.getEmail().isEmpty() || !person.getEmail().contains("@") || !person.getEmail().contains(".")) {

                throw new WebApplicationException("Please enter valid email", 400);
            }

            if (person.getStreet() == null || person.getStreet().isEmpty() || person.getStreet().matches(".*\\d+.*") || person.getStreet().length() < 3) {

                throw new WebApplicationException("Street must only contain letters, and be at least 3 characters", 400);
            }

            if (person.getCity() == null || person.getCity().isEmpty() || person.getCity().matches(".*\\d+.*") || person.getCity().length() < 3) {

                throw new WebApplicationException("City must be at least 3 characters", 400);
            }
            if (person.getZip() < 1000 || person.getZip() > 9999) {

                throw new WebApplicationException("Zipcode must be 4 digits", 400);
            }

            return PF.addPerson(person);

        } catch (JsonSyntaxException | NotFoundException | WebApplicationException e) {

            throw new WebApplicationException(e.getMessage(), 400);
        }
    }

    @PUT
    @Path("/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces({MediaType.APPLICATION_JSON})
    public PersonDTO editPerson(@PathParam("id") long id, String personAsJSON) {

        try {
            PersonDTO person = GSON.fromJson(personAsJSON, PersonDTO.class);

            if (id == 0) {

                throw new WebApplicationException("Id not passed correctly", 400);
            }
            if (person.getFirstName() == null || person.getFirstName().isEmpty() || person.getFirstName().length() < 2) {

                throw new WebApplicationException("Firstname must be 2 characters", 400);
            }

            if (person.getFirstName().matches(".*\\d+.*")) {

                throw new WebApplicationException("Firstname must not contain digits", 400);
            }

            if (person.getLastName() == null || person.getLastName().isEmpty() || person.getLastName().length() < 2) {

                throw new WebApplicationException("Lastname must be 2 characters", 400);
            }

            if (person.getLastName().matches(".*\\d+.*")) {

                throw new WebApplicationException("Lastname must not contain digits", 400);
            }

            if (person.getEmail() == null || person.getEmail().isEmpty() || !person.getEmail().contains("@") || !person.getEmail().contains(".")) {

                throw new WebApplicationException("Please enter valid email", 400);
            }

            if (person.getStreet() == null || person.getStreet().isEmpty() || person.getStreet().matches(".*\\d+.*") || person.getStreet().length() < 3) {

                throw new WebApplicationException("Street must only contain letters, and be at least 3 characters", 400);
            }

            if (person.getCity() == null || person.getCity().isEmpty() || person.getCity().matches(".*\\d+.*") || person.getCity().length() < 3) {

                throw new WebApplicationException("City must be at least 3 characters", 400);
            }
            if (person.getZip() < 1000 || person.getZip() > 9999) {

                throw new WebApplicationException("Zipcode must be 4 digits", 400);
            }

            person.setId(id);
            return PF.editPerson(person);

        } catch (NotFoundException | WebApplicationException e) {

            throw new WebApplicationException(e.getMessage(), 400);
        }
    }

    @DELETE
    @Path("/{id}")
    @RolesAllowed("admin")
    @Produces({MediaType.APPLICATION_JSON})
    public String deletePerson(@PathParam("id") long id) {
        try {
            if (id == 0) {

                throw new WebApplicationException("Id not passed correctly", 400);
            }

            PF.deletePerson(id);

            return "{\"status\": \"The person has been deleted\"}";

        } catch (NotFoundException | WebApplicationException e) {

            throw new WebApplicationException(e.getMessage(), 400);
        }

    }

    @GET
    @Path("/{id}")
    @Produces({MediaType.APPLICATION_JSON})
    public PersonDTO getPerson(@PathParam("id") long id) {

        try {
            PersonDTO p = PF.getPerson(id);
            return p;

        } catch (NotFoundException e) {
            throw new WebApplicationException(e.getMessage(), 400);
        }
    }

    @GET
    @Path("/all")
    @Produces({MediaType.APPLICATION_JSON})
    public List<PersonDTO> getAllPerson() {

        return PF.getAllPersons();
    }

    @GET
    @Path("/hobby/{hobby}")
    @Produces({MediaType.APPLICATION_JSON})
    public List<PersonDTO> getPersonByHobby(@PathParam("hobby") String hobby) {

        return PF.getPersonsByHobby(hobby);
    }
    
    @GET
    @Path("/hobbies")
    @Produces({MediaType.APPLICATION_JSON})
    public List<HobbyDTO> getAllHobbies() {

        return PF.getAllHobbies();
    }
    
    @POST
    @Path("/hobby")
    @RolesAllowed("admin")
    @Produces(MediaType.APPLICATION_JSON)
    public HobbyDTO addHobby(String hobbyAsJSON) {
        
        try {
            HobbyDTO hobby = GSON.fromJson(hobbyAsJSON, HobbyDTO.class);

            
            if (hobby.getName() == null || hobby.getName().isEmpty() || hobby.getName().length() < 2) {

                throw new WebApplicationException("Hobby name must be 2 characters", 400);
            }

            if (hobby.getName().matches(".*\\d+.*")) {

                throw new WebApplicationException("Hobby name must not contain digits", 400);
            }

            if (hobby.getDescription() == null || hobby.getDescription().isEmpty() || hobby.getDescription().length() < 5) {

                throw new WebApplicationException("Description must be 5 characters", 400);
            }

            return PF.addHobby(hobby);

        } catch (NotFoundException | WebApplicationException e) {

            throw new WebApplicationException(e.getMessage(), 400);
        }
        
    }
    
    @PUT
    @Path("/hobby/{id}")
    @RolesAllowed("admin")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public HobbyDTO editHobby(@PathParam("id") long id, String hobbyAsJSON) {
        
        try {
            HobbyDTO hobby = GSON.fromJson(hobbyAsJSON, HobbyDTO.class);
            List<HobbyDTO> hobbies = PF.getAllHobbies();
            
            if (hobby.getName() == null || hobby.getName().isEmpty() || hobby.getName().length() < 2) {

                throw new WebApplicationException("Hobby name must be 2 characters", 400);
            }

            if (hobby.getName().matches(".*\\d+.*")) {

                throw new WebApplicationException("Hobby name must not contain digits", 400);
            }
            
            for (HobbyDTO h : hobbies) {
               
                if(h.getName().equals(hobby.getName()) && h.getId() != id)
                {
                    throw new WebApplicationException("Hobby alrerady exsists", 400);
                }
            }

            if (hobby.getDescription() == null || hobby.getDescription().isEmpty() || hobby.getDescription().length() < 5) {

                throw new WebApplicationException("Description must be 5 characters", 400);
            }

            hobby.setId(id);
            return PF.editHobby(hobby);

        } catch (NotFoundException | WebApplicationException e) {

            throw new WebApplicationException(e.getMessage(), 400);
        }
        
    }
    
    @DELETE
    @Path("/hobby/{id}")
    @RolesAllowed("admin")
    @Produces({MediaType.APPLICATION_JSON})
    public String deleteHobby(@PathParam("id") long id) {
        try {
            if (id == 0) {

                throw new WebApplicationException("Id not passed correctly", 400);
            }

            PF.deleteHobby(id);

            return "{\"status\": \"The hobby has been deleted\"}";

        } catch (NotFoundException | WebApplicationException e) {

            throw new WebApplicationException(e.getMessage(), 400);
        }

    }
    
    @PUT
    @Path("/{person_id}/{hobby_id}")
    @RolesAllowed("admin")
    @Produces({MediaType.APPLICATION_JSON})
    public String addHobbyToPerson(@PathParam("person_id") long person_id, @PathParam("hobby_id") long hobby_id ) {
        try {
            if (person_id == 0 || hobby_id == 0) {

                throw new WebApplicationException("Id not passed correctly", 400);
            }

            PF.addHobbyToPerson(person_id, hobby_id);

            return "{\"status\": \"The hobby has been added\"}";

        } catch (NotFoundException | WebApplicationException e) {

            throw new WebApplicationException(e.getMessage(), 400);
        }

    }
    
    @PUT
    @Path("hobby/{person_id}/{hobby_id}")
    @RolesAllowed("admin")
    @Produces({MediaType.APPLICATION_JSON})
    public String removeHobbyFromPerson(@PathParam("person_id") long person_id, @PathParam("hobby_id") long hobby_id ) {
        try {
            if (person_id == 0 || hobby_id == 0) {

                throw new WebApplicationException("Id not passed correctly", 400);
            }

            PF.removeHobbyFromPerson(person_id, hobby_id);

            return "{\"status\": \"The hobby has been removed\"}";

        } catch (NotFoundException | WebApplicationException e) {

            throw new WebApplicationException(e.getMessage(), 400);
        }

    }

}
