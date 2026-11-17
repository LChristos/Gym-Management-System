package com.example.API.service;

public class DuplicateReservationException extends RuntimeException{
    public DuplicateReservationException(String message){
        super(message);
    }
}
