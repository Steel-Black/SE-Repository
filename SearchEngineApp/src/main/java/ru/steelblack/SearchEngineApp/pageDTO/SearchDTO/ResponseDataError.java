package ru.steelblack.SearchEngineApp.pageDTO.SearchDTO;

import lombok.Data;

@Data
public class ResponseDataError extends ResponseData {
    private boolean result;

    private String error;

    public ResponseDataError(boolean result, String error) {
        this.result = result;
        this.error = error;
    }
}
