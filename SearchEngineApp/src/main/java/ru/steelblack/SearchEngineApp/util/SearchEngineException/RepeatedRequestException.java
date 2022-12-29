package ru.steelblack.SearchEngineApp.util.SearchEngineException;

import ru.steelblack.SearchEngineApp.util.SearchEngineException.SearchEngineException;

public class RepeatedRequestException extends SearchEngineException {
    public RepeatedRequestException(String message) {
        super(message);
    }
}
