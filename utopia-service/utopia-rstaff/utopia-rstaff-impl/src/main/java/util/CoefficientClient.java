package util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CoefficientClient {

    public static Double calcCoefficent(List<List> data){
        List<Double> averageScoreL = new ArrayList<Double>();
        List<Double> standardDeviationL = new ArrayList<Double>();
        for(List temp : data){
            Double averageScore = (Double) temp.get(0);
            averageScoreL.add(averageScore);
            Double standardDeviation = (Double) temp.get(1);
            standardDeviationL.add(standardDeviation);
        }

        DataNode x = new DataNode(averageScoreL.stream().mapToDouble(Double::doubleValue).toArray());
        DataNode y = new DataNode(standardDeviationL.stream().mapToDouble(Double::doubleValue).toArray());
        CorrelationScore score = new CorrelationScore(x, y);
        BigDecimal b = BigDecimal.ZERO;
        if(!Double.isNaN(score.getPearsonCorrelationScore())){
            b = new BigDecimal(score.getPearsonCorrelationScore());
        }
        double coefficent = b.setScale(2,   BigDecimal.ROUND_HALF_UP).doubleValue();
        return coefficent;
    }

}
