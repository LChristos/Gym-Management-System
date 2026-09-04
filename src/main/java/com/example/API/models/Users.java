package com.example.API.models;
import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class Users {
//When i use a SET i need to update also the table
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String surname;
    private String country;
    private String town;
    private String address;
    private String email;

    @Column(name = "username", unique = true)
    private String username;

    private String password;
    private String role;
    private boolean accepted=false;

    public Users() {}

    public Users(String name, String surname, String country, String town, String address, String email, String username, String password) {
        this.name = name;
        this.surname = surname;
        this.country = country;
        this.town = town;
        this.address = address;
        this.email = email;
        this.username = username;
        this.password = password;
        //also create in the table
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
