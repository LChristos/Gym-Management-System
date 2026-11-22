package com.example.API.exceptions;

public class DuplicateReservationException extends RuntimeException{
    public DuplicateReservationException(String message){
        super(message);
    }
}
