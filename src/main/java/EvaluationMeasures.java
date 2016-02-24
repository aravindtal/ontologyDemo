/**
 * Created by aravindp on 10/2/16.
 */
public class EvaluationMeasures {

    private float accuracy;
    private float precision;
    private float recall;
    private float f1Score;

    private long tp;
    private long tn;
    private long fp;
    private long fn;

    public float getAccuracy() {
        return accuracy;
    }

    public float getPrecision(){
        return precision;
    }

    public float getRecall(){
        return recall;
    }

    public float getF1Score(){
        return f1Score;
    }

    public long getTruePositive() {
        return tp;
    }

    public void setTruePositive(long truePositive) {
        this.tp = truePositive;
    }

    public long getTrueNegative() {
        return tn;
    }

    public void setTrueNegative(long trueNegative) {
        this.tn = trueNegative;
    }

    public long getFalsePositive() {
        return fp;
    }

    public void setFalsePositive(long falsePositive) {
        this.fp = falsePositive;
    }

    public long getFalseNegative() {
        return fn;
    }

    public void setFalseNegative(long falseNegative) {
        this.fn = falseNegative;
    }

    public void apply(){
        accuracy = ((float) (tp+tn))/(tp+tn+fp+fn);
        precision = (float) tp/(tp+fp);
        recall = (float) tp/(tp+fn);
        f1Score = (float) 2*precision*recall/(precision + recall);
    }
}
