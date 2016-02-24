import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by aravindp on 10/2/16.
 */
public class EvaluationMeasureUtils {

    public static String TESTSET = "Test Set";
    public static String DESIRED_OUTPUT = "Desired Output";
    public static String ACTUAL_OUTPUT = "Actual Output";
    public static String XLS = "XLS";
    public static String XLSX = "XLSX";

    public Map<String,List<String>> readDataSet(String filePath) throws Exception{
        FileInputStream inputStream = new FileInputStream(new File(filePath));
        ExcelUtils excelFile = new ExcelUtils(inputStream);

        Map<String,List<String>> excelData = null;

        String extension = filePath.substring(filePath.lastIndexOf(".")+1);

        if (XLS.equalsIgnoreCase(extension)) {
            excelData = excelFile.openWorkbook(ExcelUtils.HSSF);
        } else if (XLSX.equalsIgnoreCase(extension)) {
            excelData = excelFile.openWorkbook(ExcelUtils.XSSF);
        }

        List<String> header = excelFile.getHeader();

        if (header == null) {
            throw new Exception("Data set must contain headers  \"Test Set\" & \"Desired Set\" & \"Actual Set\"");
        } else if (header != null && !header.contains(TESTSET)) {
            throw new Exception("Data set must contain header \"Test Set\"");
        } else if (header != null && !header.contains(DESIRED_OUTPUT)) {
            throw new Exception("Data set must contain header \"Desired Set\"");
        } else if (header != null && !header.contains(ACTUAL_OUTPUT)) {
            throw new Exception("Data set must contain header \"Actual Set\"");
        }

        inputStream.close();
        return excelData;
    }

    public EvaluationMeasures parseDataSet(List<String> testData,List<String> desiredOutput,List<String> actualOutput){
        long tp = 0;
        long tn = 0;
        long fp = 0;
        long fn = 0;

        tn = testData.size() - desiredOutput.size();

        Iterator<String> iterator = testData.iterator();
        Iterator<String> desiredOutputIterator = desiredOutput.iterator();
        Iterator<String> actualOutputIterator = actualOutput.iterator();

        while(iterator.hasNext()){
            if (actualOutputIterator.hasNext() && desiredOutputIterator.hasNext()){
                String actual = actualOutputIterator.next();
                String desired = desiredOutputIterator.next();

                if(desired.equalsIgnoreCase(actual)){
                    tp++;
                } else if((desired != null && "".equals(desired)) &&
                        (actual != null && !actual.isEmpty())){
                    fn++;
                } else if((desired != null && !desired.isEmpty()) &&
                        (actual != null && "".equals(actual))){
                    fp++;
                }
            }
            iterator.next();
        }

        EvaluationMeasures evalMeasures = new EvaluationMeasures();
        evalMeasures.setTruePositive(tp);
        evalMeasures.setTrueNegative(tn);
        evalMeasures.setFalsePositive(fp);
        evalMeasures.setFalseNegative(fn);

        return evalMeasures;
    }

    public EvaluationMeasures evaluateDataSet(String filePath) throws Exception{
        EvaluationMeasureUtils evalUtils = new EvaluationMeasureUtils();

        Map<String,List<String>> excelData = evalUtils.readDataSet(filePath);

        List<String> testData = excelData.get(TESTSET);
        List<String> desiredOutput = excelData.get(DESIRED_OUTPUT);
        List<String> actualOutput = excelData.get(ACTUAL_OUTPUT);

        EvaluationMeasures evalMeasures = evalUtils.parseDataSet(testData,desiredOutput,actualOutput);
        evalMeasures.apply();

        return evalMeasures;
    }

    public static void main(String args[]) throws Exception{

        EvaluationMeasureUtils evalUtils = new EvaluationMeasureUtils();
        ClassLoader classLoader = evalUtils.getClass().getClassLoader();

        EvaluationMeasures evalMeasures = evalUtils.evaluateDataSet(
                classLoader.getResource("POSTaggerValidation.xlsx").getFile());

        System.out.println("tp  : "+evalMeasures.getTruePositive());
        System.out.println("tn  : "+evalMeasures.getTrueNegative());
        System.out.println("fp  : "+evalMeasures.getFalsePositive());
        System.out.println("fn  : "+evalMeasures.getFalseNegative());

        System.out.println("Accuracy    : "+evalMeasures.getAccuracy());
        System.out.println("Precision   : "+evalMeasures.getPrecision());
        System.out.println("Recall      : "+evalMeasures.getRecall());
        System.out.println("F1 Score    : "+evalMeasures.getF1Score());


    }

}
