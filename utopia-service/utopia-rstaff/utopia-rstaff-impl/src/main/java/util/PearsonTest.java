package util;

public class PearsonTest {

    public static void main(String[] args) {
        DataNode x = new DataNode(new double[] {60,79,85,90,70});
        DataNode y = new DataNode(new double[] {25,16,14,18,20});
        CorrelationScore score = new CorrelationScore(x, y);
        System.out.println(score.getPearsonCorrelationScore());
    }
}
