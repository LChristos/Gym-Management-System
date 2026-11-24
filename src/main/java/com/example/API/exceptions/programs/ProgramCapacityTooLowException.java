package com.example.API.exceptions.programs;

public class ProgramCapacityTooLowException extends RuntimeException{
    public ProgramCapacityTooLowException(String message){
        super(message);
    }
}
