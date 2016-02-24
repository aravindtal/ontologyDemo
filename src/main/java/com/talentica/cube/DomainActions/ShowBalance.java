package com.talentica.cube.DomainActions;

import com.talentica.cube.Blaze.Query;
import com.talentica.cube.test.CubeOntologyDemo;
import org.openrdf.query.IncompatibleOperationException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by aravindp on 22/2/16.
 */
public class ShowBalance extends DomainAction{

    public ShowBalance(){

    }

    public ShowBalance(Query query){
        super(query);
    }

    public boolean action(List<String> parameters, Map<String,Object> values){

        List<String> users = (List<String>) values.get("User");
        Map<String,List<String>> accounts = (HashMap) values.get("Account");

        System.out.println("Hi " + CubeOntologyDemo.currentUser+"!");

        for (String user : users){
            List<String> accountNo = accounts.get(user);
            for(String accNo : accountNo){
                String balance = CubeOntologyDemo.account.get(accNo.replace("\"","")).get("balance");
                if (!user.equalsIgnoreCase(CubeOntologyDemo.currentUser)){
                    System.out.println("for user "+user);
                }
                System.out.println("available balance for Account no : "+accNo+" is " +"Rs."+ balance);
            }
        }

        return true;
    }

    public Map<String,Object> fitRequirements(String userQuery,List<String> actionParameters) throws Exception{

        Map<String,Object> values = new HashMap<>();

        List<String> users = getUsers(userQuery);
        boolean admin = isAdmin(CubeOntologyDemo.currentUser);

        if(users == null || users.size() == 0){
            users = new ArrayList<>();
            users.add(CubeOntologyDemo.currentUser);
        } else {
            //check if the user is superuser
            if(!(users.size() == 1 && users.get(0).equalsIgnoreCase(CubeOntologyDemo.currentUser))){
                if(!admin) {
                    throw new Exception("Sorry!! you can check only your balance.");
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

        for(String parameter : actionParameters) {
            if(parameter.equalsIgnoreCase("User")) {
                values.put(parameter,users);
            } else if(parameter.equalsIgnoreCase("Account")) {
                values.put(parameter,accounts);
            }
        }

        return values;
    }

    private String getClarification(List<String> values) throws Exception{
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
}
