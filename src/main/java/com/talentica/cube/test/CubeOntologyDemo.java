package com.talentica.cube.test;

import com.bigdata.rdf.sail.webapp.client.RemoteRepository;
import com.talentica.cube.Blaze.Query;
import com.talentica.cube.Blaze.Repo;
import com.talentica.cube.DomainActions.DomainAction;
import com.talentica.cube.DomainActions.ReflectionUtil;
import org.openrdf.query.IncompatibleOperationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by aravindp on 18/2/16.
 */
public class CubeOntologyDemo {

    public static String cube = "http://www.cube.com/ontology/bank#";
    public static String bdSearch = "http://www.bigdata.com/rdf/search#";
    public static String DOMAIN_ACTION = "DomainAction";
    public static String DOMAIN_ACTION_PARAMETERS = "DomainActionParameters";
    public static String DOMAIN_ACTION_PARAMETER_VALUES = "DomainActionParameterValues";
    //public static String currentUser = "Sushant_Pradhan";
    public static String currentUser = "Aravind_Pilla";
    public static Map<String,Map<String,String>> account;
    private static RemoteRepository repoNameSpace;
    private static Query query;
    private static DomainAction domainActionObj;
    private static CubeOntologyDemo obj;
    private static void init(){
        obj = new CubeOntologyDemo();

        Repo repo = new Repo();
        repoNameSpace = repo.getInstance("cube");
        try {
            repo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        query = new Query(repoNameSpace);

        domainActionObj = new DomainAction(query);

        Map<String,String> prefixes = new HashMap<>();
        prefixes.put("cube",cube);
        prefixes.put("bds",bdSearch);
        query.setPrefixes(prefixes);

        Map<String,String> replacePrefixes = new HashMap<>();
        replacePrefixes.put(cube,"");
        replacePrefixes.put("\"","");
        query.setReplacePrefixesWith(replacePrefixes);

        account = new HashMap<>();
        Map<String,String> accountInfo = new HashMap<>();
        accountInfo.put("Name","Aravind Pilla");
        accountInfo.put("balance","200.05");
        account.put("54321",accountInfo);

        Map<String,String> accountInfo1 = new HashMap<>();
        accountInfo1.put("Name","Aravind Pilla");
        accountInfo1.put("balance","434.05");
        account.put("12345",accountInfo1);

        Map<String,String> accountInfo2 = new HashMap<>();
        accountInfo2.put("Name","Sushant Pradhan");
        accountInfo2.put("balance","2023.05");
        account.put("21354",accountInfo2);
    }

    public static void main(String args[]) throws Exception{
        try {
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
            String bye = "good bye exit cu later see you later";
            init();
            System.out.println("Hi "+currentUser+"! how can I help you.");
            while (true) {
                try {

                    String query = input.readLine();
                    if(bye.contains(query.toLowerCase())){
                        System.out.println("ok, c u later");
                        break;
                    }
                    Map<String, Object> actionParameters = getActionParameters(query);
                    invokeAction(actionParameters);

                } catch (Exception e){
                   if(e instanceof IncompatibleOperationException){
                        System.out.println(e.getMessage());
                   } else if (!(e instanceof InputMismatchException)){
                       throw e;
                   }
                }
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            System.exit(0);
        }

    }

    private static Map<String,Object> getActionParameters(String userQuery) throws Exception{
        Map<String,Object> actionParameters = new HashMap<>();
        Map<String,Object> values = null;

        String domainAction = domainActionObj.getDomainAction(userQuery);

        List<String> reqParams = domainActionObj.getRequiredParametersForDomainAction(domainAction);

        try {
            Object arguments[] = {userQuery, reqParams};
            values = (Map<String, Object>) ReflectionUtil.getReturnValueforFunction(query, domainAction, "fitRequirements", arguments);

            actionParameters.put(DOMAIN_ACTION, domainAction);
            actionParameters.put(DOMAIN_ACTION_PARAMETERS, reqParams);
            actionParameters.put(DOMAIN_ACTION_PARAMETER_VALUES, values);
        } catch (Exception e){
            throw new IncompatibleOperationException(e.getMessage());
        }
        return actionParameters;
    }

    private static void invokeAction(Map<String,Object> actionParameters) throws Exception{

        String action = (String) actionParameters.get(DOMAIN_ACTION);
        List<String> parameters = (List<String>) actionParameters.get(DOMAIN_ACTION_PARAMETERS);
        Map<String,Object> values = (Map<String, Object>) actionParameters.get(DOMAIN_ACTION_PARAMETER_VALUES);

        Object arguments[] = {parameters,values};
        boolean val = (boolean) ReflectionUtil.getReturnValueforFunction(query,action,"action",arguments);

    }

}
