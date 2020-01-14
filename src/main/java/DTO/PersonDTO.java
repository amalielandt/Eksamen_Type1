/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package DTO;

import entities.Hobby;
import entities.Person;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author sofieamalielandt
 */
public class PersonDTO {

    private long id;
    private String firstName;
    private String lastName;
    private String email;
    private int phone;
    private String street;
    private String city;
    private int zip;
    private Set<HobbyDTO> hobbies = new HashSet();

    public PersonDTO(Person person) {

        this.id = person.getId();
        this.firstName = person.getFirstName();
        this.lastName = person.getLastName();
        this.email = person.getEmail();
        this.phone = person.getPhone();
        this.street = person.getAddress().getStreet();
        this.city = person.getAddress().getCity();
        this.zip = person.getAddress().getZip();

        person.getHobbies().forEach((hobby) -> {
            this.hobbies.add(new HobbyDTO(hobby));
        });
    }

    public PersonDTO(String firstName, String lastName, String email, String phone, String street, String city, String zip) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = Integer.parseInt(phone);
        this.street = street;
        this.city = city;
        this.zip = Integer.parseInt(zip);

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getEmail() {
        return email;
    }

    public int getPhone() {
        return phone;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public int getZip() {
        return zip;
    }

    public Set<HobbyDTO> getHobbies() {
        return hobbies;
    }

}
