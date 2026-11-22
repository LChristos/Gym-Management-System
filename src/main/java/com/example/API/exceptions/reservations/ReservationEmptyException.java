package com.example.API.exceptions.reservations;

public class ReservationEmptyException extends RuntimeException{
    public ReservationEmptyException(String message){
        super(message);
    }
}
