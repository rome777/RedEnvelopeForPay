package com.recruit.kakaopay.rest.api.exception;

public class CustomException extends Exception
{
    private String errorMessage;

    public CustomException()
    {
        super();
    }

    public CustomException(String errorMessage)
    {
        super(errorMessage);
    }

    public CustomException(Throwable cause)
    {
        super(cause);
    }

    public CustomException(String message, Throwable cause)
    {
        super(message, cause);
    }
}