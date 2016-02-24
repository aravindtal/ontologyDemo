package com.talentica.cube.Blaze;

import com.bigdata.rdf.sail.webapp.client.RemoteRepository;
import com.talentica.cube.BlazeUtil;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResult;

import java.util.*;

/**
 * Created by aravindp on 22/2/16.
 */
public class Query {

    private Map<String, String> prefixes;
    private static String queryPrefix = "";
    private String query;
    private Map<String, String> replacePrefixesWith;

    private RemoteRepository repo;

    /*public Query(){

    }*/

    public Query(RemoteRepository repo){
        this.repo = repo;
    }

    public void setReplacePrefixesWith(Map<String, String> replacePrefixesWith) {
        this.replacePrefixesWith = replacePrefixesWith;
    }

    public void setRepo(RemoteRepository repo){
        this.repo = repo;
    }

    public void setPrefixes(Map<String, String> prefixes) {
        this.prefixes = prefixes;
        Set<String> keySet = prefixes.keySet();
        for(String key : keySet){
            Query.queryPrefix = "PREFIX "+key+": <"+prefixes.get(key)+"> \n";
        }
    }

    public Query construct(String query){
        this.query = queryPrefix + query;
        return this;
    }

    public ResultSet execute() throws Exception{
        ResultSet resultSet = null;

        //Execute query
        TupleQueryResult result = this.repo.prepareTupleQuery(query)
                .evaluate();

        //result processing
        resultSet = parseResults(result);

        return resultSet;
    }

    /*
       Parse Result
    */
    public ResultSet parseResults(TupleQueryResult result) throws Exception{
        Map<String,Object> data = new HashMap<>();
        List<List<String>> rows = new LinkedList<>();

        try {
            List<String> bindingNames = result.getBindingNames();
            data.put(ResultSet.COLUMNS, bindingNames);

            while (result.hasNext()) {
                BindingSet bs = result.next();
                List<String> row = new LinkedList<>();
                for (String bindingName : bindingNames) {
                    Value columnValue = bs.getBinding(bindingName).getValue();
                    if (columnValue != null) {
                        String colValue = columnValue.toString();
                        Set<String> keySet = this.replacePrefixesWith.keySet();
                        for (String replaceKey : keySet) {
                            colValue = colValue.replaceAll(replaceKey, this.replacePrefixesWith.get(replaceKey));
                        }
                        row.add(colValue);
                    }
                }
                rows.add(row);
            }

        } catch (Exception e) {
            throw e;
        } finally {
            result.close();
        }

        data.put(ResultSet.ROWS,rows);

        ResultSet resultSet = new ResultSet();
        resultSet.setResults(data);

        return resultSet;
    }
}
