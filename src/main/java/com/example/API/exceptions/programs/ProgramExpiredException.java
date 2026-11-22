package com.example.API.exceptions.programs;

public class ProgramExpiredException extends  RuntimeException{
    public ProgramExpiredException(String message){
        super(message);
    }
}
