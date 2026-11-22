package com.example.API.exceptions;

public class ProgramExpiredException extends  RuntimeException{
    public ProgramExpiredException(String message){
        super(message);
    }
}
