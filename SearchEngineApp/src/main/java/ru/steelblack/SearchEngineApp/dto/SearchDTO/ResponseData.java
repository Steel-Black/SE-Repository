package ru.steelblack.SearchEngineApp.dto.SearchDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@lombok.Data
public class ResponseData {

    private boolean result;

    private int count;

    private List<Data> data;

    public void addToSearchDataList (List<Data> searchData){
        if (data == null){
            data = new ArrayList<>();
        }
        data.addAll(searchData);
       data = data.stream().sorted(Data::compareTo).collect(Collectors.toList());
    }
}
