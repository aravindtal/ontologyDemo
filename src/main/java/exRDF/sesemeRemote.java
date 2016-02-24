package exRDF;

import com.bigdata.rdf.sail.webapp.client.RemoteRepository;
import com.talentica.cube.Blaze.Query;
import com.talentica.cube.Blaze.Repo;
import com.talentica.cube.Blaze.ResultSet;
import org.openrdf.query.IncompatibleOperationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by aravindp on 18/2/16.
 */
public class sesemeRemote {

    public static String cube = "http://www.cube.com/ontology/bank#";
    public static String bdSearch = "http://www.bigdata.com/rdf/search#";
    public static String DOMAIN_ACTION = "DomainAction";
    public static String DOMAIN_ACTION_PARAMETERS = "DomainActionParameters";
    public static String DOMAIN_ACTION_PARAMETER_VALUES = "DomainActionParameterValues";
    public static String currentUser = "Sushant_Pradhan";
    //public static String currentUser = "Aravind_Pilla";
    public static Map<String,Map<String,String>> account;
    private static RemoteRepository repoNameSpace;
    private static Query query;

    private static void init(){
        Repo repo = new Repo();
        repoNameSpace = repo.getInstance("cube");
        try {
            repo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        query = new Query(repoNameSpace);

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
        accountInfo.put("balance","Rs200.05");
        account.put("54321",accountInfo);

        Map<String,String> accountInfo1 = new HashMap<>();
        accountInfo1.put("Name","Aravind Pilla");
        accountInfo1.put("balance","Rs434.05");
        account.put("12345",accountInfo1);

        Map<String,String> accountInfo2 = new HashMap<>();
        accountInfo2.put("Name","Sushant Pradhan");
        accountInfo2.put("balance","Rs2023.05");
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

        String domainAction = getDomainAction(userQuery);

        List<String> reqParams = getRequiredParametersForDomainAction(domainAction);

        actionParameters.put(DOMAIN_ACTION,domainAction);
        actionParameters.put(DOMAIN_ACTION_PARAMETERS,reqParams);

        Class<?> act = Class.forName("com.talentica.cube.DomainActions."+domainAction);
        Method methods[] = act.getMethods();
        for (Method m: methods) {
            System.out.println(m.getName());
        }
        fitRequirements(userQuery,actionParameters);

        return actionParameters;
    }

    private static String getDomainAction(String userQuery) throws Exception{
        String domainAction  = "";
        Query domainActionQuery = query.construct(
                "\tselect distinct ?DomainAction\n" +
                "\twhere {\n" +
                "\t\t?DomainAction rdfs:subClassOf cube:DomainAction .\n" +
                "\t  \t?DomainAction rdfs:label ?label.\n" +
                "\t    ?label bds:search \""+ userQuery +"\" .\n" +
                "\t    ?label bds:relevance ?score .\n" +
                "\t    ?label bds:maxRank '1000' .\n" +
                "\t    ?label bds:rank ?rank .\n" +
                "\t    ?DomainAction ?p ?o.\n" +
                "\t}");


        ResultSet resultSet = domainActionQuery.execute();

        List<String> reqParams = resultSet.getResults("DomainAction");

        if(reqParams.size() > 1){
            System.out.println("Ambiguity in action selection : "+reqParams);
        } else if (reqParams.size() <= 0) {
            System.out.println("No data about the query in knowledge base");
        } else {
            domainAction = reqParams.get(0);
        }
        return domainAction;
    }

    private static List<String> getRequiredParametersForDomainAction(String domainAction) throws Exception{
        Query domainActionParameters = query.construct(
                "select ?DomainActionParameters where {\n" +
                        "  ?s rdfs:range ?DomainActionParameters.\n" +
                        "  ?s rdfs:domain cube:"+ domainAction +"\n" +
                        "}\n" +
                        "BINDINGS ?s {\n" +
                        "   (cube:input)\n" +
                        "}");
        ResultSet resultSet = domainActionParameters.execute();

        List<String> reqParams = resultSet.getResults("DomainActionParameters");
        return reqParams;
    }

    /*
    */
    private static void fitRequirements(String userQuery,Map<String,Object> actionParameters) throws Exception{

        Map<String,Object> values = new HashMap<>();

        String action = (String) actionParameters.get(DOMAIN_ACTION);
        List<String> parameters = (List<String>) actionParameters.get(DOMAIN_ACTION_PARAMETERS);

        if(action.equalsIgnoreCase("ShowBalance")) {

            List<String> users = getUsers(action,userQuery);
            boolean admin = isAdmin(currentUser);

            if(users == null || users.size() == 0){
                users = new ArrayList<>();
                users.add(currentUser);
            } else {
                //check if the user is superuser
                if(!(users.size() == 1 && users.get(0).equalsIgnoreCase(currentUser))){
                    if(!admin) {
                        throw new IncompatibleOperationException("Sorry!! you can check only your balance.");
                    }
                }
            }


            Map<String,List<String>> accounts = getUserAccounts(users);


            if(accounts.size() == 1){

                List<String> userAccounts  = accounts.get(users.get(0));
                if (userAccounts.size() > 1) {
                    String selectedAccount = getClarification(userAccounts);
                    List<String> clarifiedAccount = new ArrayList<>();
                    clarifiedAccount.add(selectedAccount);
                    accounts.put(users.get(0),clarifiedAccount);

                } else if (userAccounts.size() <= 0) {
                    throw new Exception("No accounts available for current user");
                }
            }

            for(String parameter : parameters) {
                if(parameter.equalsIgnoreCase("User")) {
                    values.put(parameter,users);
                } else if(parameter.equalsIgnoreCase("Account")) {
                    values.put(parameter,accounts);
                }
            }
        }

        actionParameters.put(DOMAIN_ACTION_PARAMETER_VALUES,values);
    }

    private static boolean isAdmin(String user) throws Exception{
        boolean admin = false;

        Query adminQuery = query.construct(
                "select ?Admin \n" +
                "where { \n"+
                "?a cube:Admin ?Admin \n"+
                "}\n" +
                "BINDINGS ?a {\n" +
                "(cube:" + user + ") "+
                "}");

        ResultSet resultSet = adminQuery.execute();
        List<String> reqParams = resultSet.getResults("Admin");

        if(reqParams.size() == 1 && reqParams.get(0).equalsIgnoreCase("true")){
            admin = true;
        }

        return admin;
    }
    private static List<String> getUsers(String domainAction, String userQuery) throws Exception{
        Query uesrsQuery = query.construct(
                "select distinct ?Users\n" +
                "\twhere {\n" +
                "    \n" +
                "      ?input rdfs:domain cube:"+ domainAction +" .\n" +
                "      ?input rdfs:range ?user.\n" +
                "      ?Users rdf:type ?user.\n" +
                "      ?Users rdfs:label ?label.\n" +
                "         \n" +
                "      ?label bds:search \"" + userQuery + "\".\n" +
                "      ?label bds:relevance ?score.\n" +
                "      ?label bds:maxRank '1000'.\n" +
                "      ?label bds:rank ?rank\n" +
                "}");

        ResultSet resultSet = uesrsQuery.execute();

        List<String> reqParams = resultSet.getResults("Users");
        return reqParams;
    }

    private static Map<String,List<String>> getUserAccounts(List<String> users) throws Exception{
        Map<String,List<String>> accounts = new HashMap<>();

        String bindings = "";

        if(users == null || users.size() == 0){
            return null;
        }
        for(String user : users){
            accounts.put(user,new LinkedList<>());
            bindings = bindings + "   (cube:" + user + ") \n ";
        }


        Query accountsQuery = query.construct(
                "select ?User ?AccountNumber\n" +
                "\twhere {\n" +
                "    \n" +
                "      ?a cube:accountNumber ?AccountNumber.\n" +
                "      ?a ?b ?User.\n" +
                "}\n" +
                "BINDINGS ?User {\n" +
                bindings   +
                "}");

        ResultSet resultSet = accountsQuery.execute();

        List<String> userList = resultSet.getResults("User");
        List<String> accList = resultSet.getResults("AccountNumber");

        for (String account : accList) {
            String User = userList.get(accList.indexOf(account));
            List<String> accNo = accounts.get(User);
            accNo.add(account);
            accounts.put(User,accNo);
        }

        return accounts;
    }
    /*
    */
    public static String getClarification(List<String> values) throws Exception{
        String result = "";

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String yes = "yes ya y ok fine lets go proceed yup kk";
        String no = "nopes no na not at all";

        while(true) {
            System.out.println("[ " + values.size() + " ] accounts found!! \n" +
                    "Select any one below for your balance \n" +
                    values);

            String str = input.readLine();

            if (!values.toString().contains(str)) {
                System.out.println("Hey! its not in current context.");
                System.out.println("Would you like to continue with other request?");
                str = input.readLine();

                if (yes.contains(str.toLowerCase())) {
                    throw new InputMismatchException("new req");
                } else {
                    System.out.println("Waiting for your input!!!");
                    continue;
                }

            } else {
                return str;
            }
        }
        //return result;
    }

    private static void invokeAction(Map<String,Object> actionParameters){

        sesemeRemote obj = new sesemeRemote();
        String action = (String) actionParameters.get(DOMAIN_ACTION);
        List<String> parameters = (List<String>) actionParameters.get(DOMAIN_ACTION_PARAMETERS);
        Map<String,List<String>> values = (HashMap) actionParameters.get(DOMAIN_ACTION_PARAMETER_VALUES);

        try {
            Method methods[] = obj.getClass().getDeclaredMethods();
            for (Method m: methods) {
                if(m.getName().equalsIgnoreCase(action)){
                    m.invoke(obj,parameters,values);
                }
            }

        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

    }

    private static void showBalance(List<String> parameters,Map<String,Object> values){

        List<String> users = (List<String>) values.get("User");
        Map<String,List<String>> accounts = (HashMap) values.get("Account");

        System.out.println("Hi " + currentUser+"!");

        for (String user : users){
            List<String> accountNo = accounts.get(user);
            for(String accNo : accountNo){
                String balance = account.get(accNo.replace("\"","")).get("balance");
                if (!user.equalsIgnoreCase(currentUser)){
                    System.out.println("for user "+user);
                }
                System.out.println("available balance for Account no : "+accNo+" is " + balance);
            }
        }

    }
}
