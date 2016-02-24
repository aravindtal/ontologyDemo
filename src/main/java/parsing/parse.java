package parsing;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CorefAnnotator;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Pair;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by aravindp on 18/2/16.
 */
public class parse {
    public static  void main(String args[]) throws Exception {
        /*Properties prop = new Properties();
        prop.put( "annotators", "tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref" );

        StanfordCoreNLP pipeline = new StanfordCoreNLP(prop);

        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" );
        String currentTime = formatter.format( System.currentTimeMillis() );
        String sample = "I want to see my balance.";

        Annotation document = new Annotation(sample);

        document.set(CoreAnnotations.DocDateAnnotation.class,currentTime);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        String posText = "";
        for(CoreMap sentence : sentences){
            for(CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)){
                String text = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                //String ner = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                //String coref = token.get(CoreAnnotations.SemanticTagAnnotation.class);
                //String lemma = token.get(CoreAnnotations.LemmaAnnotation.class);

                //System.out.println("Text=" + text + " : NER=" + ner +" : POS=" + pos+
                  //      " : LEMMA=" + lemma );
                //System.out.println();
                //System.out.println( );
                //System.out.println();
                //System.out.println("coref=" + coref);
                posText = posText + " " + text +"\\/" + pos;
            }

            /*SemanticGraph dependencies = sentence.get
                    (SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class);
            IndexedWord firstRoot = dependencies.getFirstRoot();
            List<Pair<GrammaticalRelation,IndexedWord>> incomingEdgesSorted =
                    dependencies.childPairs(firstRoot);

            for(Pair edge : incomingEdgesSorted)
            {
                // Getting the target node with attached edges
                System.out.println(edge.asList());
                /*IndexedWord dep = edge.getDependent();
                System.out.println("Dependent=" + dep);
                // Getting the source node with attached edges
                IndexedWord gov = edge.getGovernor();
                System.out.println("Governor=" + gov);
                // Get the relation name between them
                GrammaticalRelation relation = edge.getRelation();
                System.out.println("Relation=" + relation);
            }

            // this section is same as above just we retrieve the OutEdges
            List<SemanticGraphEdge> outEdgesSorted = dependencies.getOutEdgesSorted(firstRoot);
            for(SemanticGraphEdge edge : outEdgesSorted)
            {
                IndexedWord dep = edge.getDependent();
                System.out.println("Dependent=" + dep);
                IndexedWord gov = edge.getGovernor();
                System.out.println("Governor=" + gov);
                GrammaticalRelation relation = edge.getRelation();
                System.out.println("Relation=" + relation);
            }*/
        //}

        //System.out.println(posText);
        String query = "pay 346.4$, $100, rs 200, 345rs to Aravind, Abc_dc";
        String pos = getPos(query);
        System.out.println(getAmountFromQuery(query));


        System.out.println(pos);

    }

    public static String getPos(String userQuery) {
        Properties prop = new Properties();
        prop.put( "annotators", "tokenize, ssplit, pos, lemma, ner, regexner, parse, dcoref" );
        StanfordCoreNLP pipeline = new StanfordCoreNLP(prop);

        SimpleDateFormat formatter = new SimpleDateFormat( "yyyy-MM-dd" );
        String currentTime = formatter.format( System.currentTimeMillis() );

        String sample = userQuery;

        Annotation document = new Annotation(sample);

        document.set(CoreAnnotations.DocDateAnnotation.class,currentTime);
        pipeline.annotate(document);

        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        String posText = "";
        for(CoreMap sentence : sentences){
            for(CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)){
                String text = token.get(CoreAnnotations.TextAnnotation.class);
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                posText = posText + " " + text +"\\/" + pos;
            }

        }

        //System.out.println(posText);
        return posText;
    }

    public static Set<String> getAmountFromQuery(String userQuery) {
        String posText = parse.getPos(userQuery.replaceAll(" +", " "));

        Set<String> amount = new LinkedHashSet<>();

        List<String> posTokens = Arrays.asList(posText.split(" "));
        Matcher m = Pattern.compile("[\\.0-9]{1,20}(\\\\/CD)").matcher(posText);
        while (m.find()) {
            String str = m.group();
            if(str.startsWith(".")) {
                str = str.replaceFirst("\\.", "");
            }
            str = str.replaceAll("(\\\\/)[A-Z]{2,5}", "");
            if(!str.contains("\\/")) {
                int index = posTokens.indexOf(m.group());
                int left = index - 1;
                int right = index + 1;
                if(index > 1 && (posTokens.get(left).contains("\\$") || posTokens.get(right).contains("\\$"))) {
                    amount.add("$:"+str);
                } else {
                    amount.add("Rs:"+str);
                }
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
                amount.add("Rs:"+tmp);
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
                int index = posTokens.indexOf(m.group());
                int left = index - 1;
                int right = index + 1;
                if(index > 1 && (posTokens.get(left).contains("\\$") || posTokens.get(right).contains("\\$"))) {
                    amount.add("$:"+str);
                } else {
                    amount.add("Rs:"+str);
                }
            }
        }

        return amount;
    }

}
