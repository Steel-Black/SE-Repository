package ru.steelblack.SearchEngineApp.util.SearchEngineException;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SearchEngineException extends RuntimeException {

    String message;

}
