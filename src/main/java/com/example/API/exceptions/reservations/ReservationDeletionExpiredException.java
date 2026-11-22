package com.example.API.exceptions.reservations;

public class ReservationDeletionExpiredException extends RuntimeException{
    public ReservationDeletionExpiredException(String message){
        super(message);
    }
}
