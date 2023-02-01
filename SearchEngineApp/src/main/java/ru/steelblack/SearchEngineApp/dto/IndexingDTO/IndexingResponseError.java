package ru.steelblack.SearchEngineApp.dto.IndexingDTO;

import lombok.Data;

@Data
public class IndexingResponseError extends IndexingResponse{

   private String error;

    public IndexingResponseError(boolean result, String error) {
        super(result);
        this.error = error;
    }
}
