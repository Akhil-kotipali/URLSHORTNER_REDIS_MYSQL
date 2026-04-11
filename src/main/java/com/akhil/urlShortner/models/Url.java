package com.akhil.urlShortner.models;

import java.io.Serializable;

import org.hibernate.validator.constraints.URL;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;

@Entity
public class Url implements Serializable{
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;
    
    @NotBlank(message = "URL cannot be empty")
    @URL(message = "Please provide a valid URL (e.g., https://google.com)")
    String longUrl;

    @Column(unique = true, nullable = false)
    String shortCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    public Url() {
    }

    public long getId() {
        return id;
    }

    public String getLongUrl() {
        return longUrl;
    }

    public void setLongUrl(String longUrl) {
        this.longUrl = longUrl;
    }

    public String getShortCode() {
        return shortCode;
    }

    public void setShortCode(String shortCode) {
        this.shortCode = shortCode;
    }

    public User getUser() { 
        return user; 
    }

    public void setUser(User user) {
         this.user = user; 
    }
    
}
