package com.talentica.cube.DomainActions;

import com.talentica.cube.Blaze.Query;
import com.talentica.cube.Blaze.ResultSet;
import com.talentica.cube.BlazeUtil;
import org.openrdf.query.IncompatibleOperationException;
import parsing.parse;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aravindp on 19/2/16.
 */
public class DomainAction {

    protected Query query;

    public DomainAction(){

    }

    public DomainAction(Query query){
        this.query = query;
    }

    public String getDomainAction(String userQuery) throws Exception{
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
            throw new IncompatibleOperationException("No data about the query in knowledge base");
        } else {
            domainAction = reqParams.get(0);
        }
        return domainAction;
    }

    public List<String> getRequiredParametersForDomainAction(String domainAction) throws Exception{
        Query domainActionParameters = query.construct(
                "select ?DomainActionParameters where {\n" +
                        "  ?s rdfs:range ?DomainActionParameters.\n" +
                        "  ?s rdfs:domain cube:"+ domainAction +"\n" +
                        "}\n" +
                        "BINDINGS ?s {\n" +
                        "   (cube:"+ domainAction +"Input)\n" +
                        "}");
        ResultSet resultSet = domainActionParameters.execute();

        List<String> reqParams = resultSet.getResults("DomainActionParameters");
        return reqParams;
    }

    protected boolean isAdmin(String user) throws Exception{
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

    protected List<String> getUsers(String userQuery) throws Exception{
        Query uesrsQuery = query.construct(
                "select distinct ?Users\n" +
                        "\twhere {\n" +
                        "      ?Users rdf:type cube:User.\n" +
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

    protected Map<String,List<String>> getUserAccounts(List<String> users) throws Exception{
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

    protected Set<String> getAmountFromQuery(String userQuery) {
        String posText = userQuery;

        Set<String> amount = new LinkedHashSet<>();

        Matcher m = Pattern.compile("[\\.0-9]{1,20}(\\\\/CD)").matcher(posText);
        while (m.find()) {
            String str = m.group();
            if(str.startsWith(".")) {
                str = str.replaceFirst("\\.", "");
            }
            str = str.replaceAll("(\\\\/)[A-Z]{2,5}", "");
            if(!str.contains("\\/")) {
                amount.add(str);
            }
        }

        m = Pattern.compile("([r|R]{0,1}[S|s]{0,1}[0-9]{1,50}[r|R]{0,1}[S|s]{0,1})(\\\\/[A-Z]+)([ ]+[\\\\.0-9]+\\\\/[A-Z]+)?").matcher(posText);
        while (m.find()) {
            String tmp = "";
            tmp = m.group().replaceFirst("[a-zA-Z]+", "");
            tmp = tmp.replaceAll(" ", "");
            if(tmp.startsWith(".")) {
                tmp = tmp.replaceFirst("\\.", "");
            }
            tmp = tmp.replaceAll("(\\\\/)[A-Z]{2,5}", "");
            if(!tmp.contains("\\/")) {
                amount.add(tmp);
            }
        }

        m = Pattern.compile("[\\.0-9]+[\\.0-9]{1,20}(\\\\/)[A-Z]+").matcher(posText);
        while (m.find()) {
            String str = m.group();
            if(str.startsWith(".")) {
                str = str.replaceFirst("\\.", "");
            }
            str = str.replaceAll("(\\\\/)[A-Z]{2,5}", "");
            if(!str.contains("\\/")) {
                amount.add(str);
            }
        }

        //System.out.println(amount);
        return amount;
    }

}
