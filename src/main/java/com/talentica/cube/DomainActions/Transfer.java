package com.talentica.cube.DomainActions;

import com.talentica.cube.Blaze.Query;
import com.talentica.cube.test.CubeOntologyDemo;
import org.openrdf.query.IncompatibleOperationException;
import parsing.parse;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

/**
 * Created by aravindp on 22/2/16.
 */
public class Transfer extends DomainAction{

    public Transfer(){

    }

    public Transfer(Query query){
        super(query);
    }

    public boolean action(List<String> parameters, Map<String,Object> values){

        Map<String,List<String>> users = (Map<String,List<String>>) values.get("User");
        Map<String,Map<String,List<String>>> accounts = (Map<String,Map<String,List<String>>>) values.get("Account");
        Map<String,String> amount = (Map<String,String>) values.get("Amount");

        System.out.println("Hi " + CubeOntologyDemo.currentUser+"!");

        List<String> toUsers = users.get("To");
        List<String> fromUsers = users.get("From");

        Map<String,List<String>> toAccounts = accounts.get("To");
        Map<String,List<String>> fromAccounts = accounts.get("From");

        for (String fromUser : fromUsers){
            List<String> fromUserAcc = fromAccounts.get(fromUser);
            String fromUserac = fromUserAcc.get(0);
            float curUserBalance = Float.parseFloat(CubeOntologyDemo.account.get(fromUserac).get("balance"));

            try {
                for (String toUser : toUsers) {
                    List<String> accountNo = toAccounts.get(toUser);
                    for (String accNo : accountNo) {
                        float toUserBalance = Float.parseFloat(CubeOntologyDemo.account.get(accNo).get("balance"));

                        if (curUserBalance < Float.parseFloat(amount.get(accNo))) {
                            System.out.println("Could not initiate transfer to " + toUser +
                                    "\n as your available balance is low (bal : Rs." + curUserBalance + ")");
                            throw new Exception("");
                        }

                        toUserBalance = toUserBalance + Float.parseFloat(amount.get(accNo));
                        CubeOntologyDemo.account.get(accNo).put("balance", Float.toString(toUserBalance));

                        curUserBalance = curUserBalance - Float.parseFloat(amount.get(accNo));

                        if (!toUser.equalsIgnoreCase(CubeOntologyDemo.currentUser)) {
                            System.out.println("amount Rs." + amount.get(accNo) + " transferred to your friend " + toUser + " successfully.");
                        } else {
                            System.out.println("amount Rs." + amount.get(accNo) + " transferred to your account no : " + accNo + " successfully.");
                        }

                    }
                }
            } catch (Exception e) {

            }

            CubeOntologyDemo.account.get(fromUserac).put("balance",Float.toString(curUserBalance));
        }

        return true;
    }

    public Map<String,Object> fitRequirements(String userQuery,List<String> actionParameters) throws Exception{

        Map<String,Object> values = new HashMap<>();

        List<String> toUsers = getUsers(userQuery);
        List<String> fromUsers = new LinkedList<>();

        boolean admin = isAdmin(CubeOntologyDemo.currentUser);

        //If no user found getClarification
        boolean flag = false;
        boolean currentUserMultipleAcc = false;
        while(true) {
            if (toUsers == null || toUsers.size() == 0) {
                //Transfer to whom
                toUsers = getUsers(getUserClarification(flag));
                flag = true;
            } else {
                if (toUsers.size() > 1 && toUsers.contains(CubeOntologyDemo.currentUser)){
                    currentUserMultipleAcc = true;
                }
                break;
            }
        }

        fromUsers.add(CubeOntologyDemo.currentUser);

        //Accounts for fromUsers
        Map<String,List<String>> fromAccounts = getUserAccounts(fromUsers);

        for (String user : fromUsers) {
            List<String> userAccounts = fromAccounts.get(user);
            if (userAccounts.size() > 1) {
                String selectedAccount = getAccountClarification(null,user,userAccounts);
                List<String> clarifiedAccount = new LinkedList<>();
                clarifiedAccount.add(selectedAccount);
                fromAccounts.put(user, clarifiedAccount);

            } else if (userAccounts.size() <= 0) {
                System.out.println("Seems your friend "+user+" does'nt have an account.");
            }
        }

        //Accounts for toUsers
        Map<String,List<String>> toAccounts = getUserAccounts(toUsers);

        for (String user : toUsers) {
            List<String> userAccounts = toAccounts.get(user);
            if (userAccounts.size() > 1) {
                String selectedAccount = getAccountClarification(fromAccounts.get(CubeOntologyDemo.currentUser).get(0)
                                                                    ,user,userAccounts);

                List<String> clarifiedAccount = new LinkedList<>();
                clarifiedAccount.add(selectedAccount);
                toAccounts.put(user, clarifiedAccount);

            } else if (user.equalsIgnoreCase(CubeOntologyDemo.currentUser) && userAccounts.size() == 1 ) {

                if(toAccounts.size() == 1) {
                    throw new IncompatibleOperationException("Sorry!! you only have one account.");
                }else {
                    getCurrentUserSingleAccount();
                }

            } else if (userAccounts.size() <= 0) {
                System.out.println("Seems your friend "+user+" does'nt have an account.");
            }
        }

        //get Amount Maping for users
        Map<String,String> mentionedAmounts = getMentionedAmount(toUsers,userQuery);

        Map<String,String> Amount = new HashMap<>();

        Set<String> keySet = mentionedAmounts.keySet();
        for(String user : keySet){
            Amount.put(toAccounts.get(user).get(0),mentionedAmounts.get(user));
        }

        Map<String,List<String>> Users = new HashMap<>();
        Map<String,Map<String,List<String>>> Accounts = new HashMap<>();

        Users.put("To",toUsers);
        Users.put("From",fromUsers);

        Accounts.put("To",toAccounts);
        Accounts.put("From",fromAccounts);


        for(String parameter : actionParameters) {
            if(parameter.equalsIgnoreCase("User")) {
                values.put(parameter,Users);
            } else if(parameter.equalsIgnoreCase("Account")) {
                values.put(parameter,Accounts);
            } else if(parameter.equalsIgnoreCase("Transaction")) {
                values.put("Amount",Amount);
            }
        }

        return values;
    }

    private String getUserClarification(boolean flag) throws Exception{
        String result = "";

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String yes = "yes ya y ok fine lets go proceed yup kk";
        String no = "nopes no na not at all";
        String str;

        if(!flag){
            System.out.println("to whom you want to transfer funds.?");
        }else {
            System.out.println("Hey!! No friends found with that name.\n");
            System.out.println("shall we try again?");
        }

        str = input.readLine();

        if (flag) {
            if (no.contains(str.toLowerCase())) {
                throw new InputMismatchException("new req");
            } else {
                System.out.println("Waiting for your input!!!");
                str = input.readLine();
            }
        }
        return str;
    }

    private String getAccountClarification(String fromUserAcc,String user,List<String> values) throws Exception{
        String result = "";

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String yes = "yes ya y ok fine lets go proceed yup kk";
        String no = "nopes no na not at all";

        while(true) {

            if(!user.equalsIgnoreCase(CubeOntologyDemo.currentUser)) {
                System.out.println(values.size() + " accounts found for your friend " + user + "!! \n" +
                        "Choose any one to initiate transfer.\n" +
                        values);
            } else {
                System.out.println("you have "+values.size() + " accounts!! \n" +
                        "which one you would like to use.\n" +
                        values);
            }

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
                if(user.equalsIgnoreCase(CubeOntologyDemo.currentUser) &&
                        str.equalsIgnoreCase(fromUserAcc)){
                    System.out.println("you cannot transfer funds within same account,\n" +
                            " please enter your choice again!!!");
                    continue;
                }
                return str;
            }
        }
    }

    private void getCurrentUserSingleAccount()throws Exception{
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String yes = "yes ya y ok fine lets go proceed yup kk";
        String no = "nopes no na not at all";

        System.out.println("Sorry!! we cannot transfer funds to your own account \n" +
                " as you have only one account.\n");

        System.out.println("shall we continue processing for others");
        String str = input.readLine();

        if (!yes.contains(str.toLowerCase())) {
            throw new InputMismatchException("new req");
        }
    }

    private void getClarificationForFromTo() throws Exception{
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String yes = "yes ya y ok fine lets go proceed yup kk";
        String no = "nopes no na not at all";

        System.out.println("Sorry!! we cannot transfer funds to your own account \n" +
                " as you have only one account.\n");
    }

    protected Map<String,String> getMentionedAmount(List<String> users,String userQuery) throws Exception{
        Map<String,String> mentionedAmounts = new HashMap<>();

        //List<String> users = getUsers(userQuery);
        String modifiedUserQuery = userQuery.replaceAll(" +", " ");
        String words[] = modifiedUserQuery.split(" ");

        Set<String> toUsers = new LinkedHashSet<>();
        //Replace users in query with actual users
        for (String user : users){
            for(String word : words){
                if(user.toLowerCase().contains(word.replace(",","").toLowerCase())){
                    toUsers.add(user);
                    if(!modifiedUserQuery.contains(user)) {
                        modifiedUserQuery = modifiedUserQuery.replace(word, user);
                    } else {
                        modifiedUserQuery = modifiedUserQuery.replace(word, "");
                    }

                }
            }
        }

        String pos = parse.getPos(userQuery.replaceAll(" +", " "));

        Set<String> amounts = getAmountFromQuery(pos);

        List<String> amtList = new LinkedList<>(amounts);
        if(amounts.size() == users.size()){
            for (String user : users) {
                mentionedAmounts.put(user, amtList.remove(0));
            }
        } else if(amounts.size() > users.size()){
            mentionedAmounts = getAmountClarification(toUsers);
        } else {
            if(amounts.size() == 1){
                for (String user : users) {
                    mentionedAmounts.put(user, amtList.get(0));
                }
            } else {
                mentionedAmounts = getAmountClarification(toUsers);
            }
        }

        return mentionedAmounts;
    }

    private Map<String,String> getAmountClarification(Set<String> users) throws Exception{

        Map<String,String> userAmt = new HashMap<>();
        BufferedReader input  = new BufferedReader(new InputStreamReader(System.in));
        for(String user : users){
            System.out.println("How much you would like to send to "+user+"?");
            boolean flag = false;
            do {
                String str = input.readLine();
                try {
                    float abc = Float.parseFloat(str);
                } catch (Exception e) {
                    System.out.println("Not a valid input, enter valid amount");
                    flag = true;
                }
                userAmt.put(user,str);
            } while (flag);
        }
        return userAmt;
    }
}
