package com.voxlearning.utopia.service.newhomework.api.constant;


public enum OralStarScoreLevel {

    A(100.0D, 3),
    B(90.0D, 2),
    C(75.0D, 1),
    D(60.0D, 0);


    private final double score;
    private final int startCount;

    OralStarScoreLevel(double score, int startCount) {
        this.score = score;
        this.startCount = startCount;
    }

    public static OralStarScoreLevel of(String value) {
        try {
            return valueOf(value);
        } catch (Exception var2) {
            return null;
        }
    }


    public double getScore() {
        return score;
    }

    public int getStartCount() {
        return startCount;
    }
}
