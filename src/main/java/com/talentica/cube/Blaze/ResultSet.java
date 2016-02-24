package com.talentica.cube.Blaze;

import com.talentica.cube.BlazeUtil;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by aravindp on 22/2/16.
 */
public class ResultSet {

    public static String COLUMNS = "COLUMNS";
    public static String ROWS = "ROWS";

    private Map<String,Object> results;

    public ResultSet(){
        this.results = new HashMap<>();
    }

    public void setResults(Map<String, Object> results) {
        this.results = results;
    }

    public List<String> getResults(String columnName){
        List<String> resultList = new LinkedList<>();
        List<String> columns = (List) this.results.get(COLUMNS);
        List<List<String>> rows = (List<List<String>>) this.results.get(ROWS);

        int colIndex = columns.indexOf(columnName);

        if(colIndex >=0){
            for(List<String> row : rows){
                resultList.add(row.get(colIndex));
            }
        }

        return resultList;
    }


}
