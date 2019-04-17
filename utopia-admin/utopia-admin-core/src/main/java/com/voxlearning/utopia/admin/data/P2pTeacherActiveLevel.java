package com.voxlearning.utopia.admin.data;

/**
 * Created by Sadi.Wan on 2014/12/3.
 */
public enum P2pTeacherActiveLevel {
    AUTO(Integer.MAX_VALUE,Integer.MIN_VALUE,0),
    LEVEL_1(0,7,1),
    LEVEL_2(8,15,2),
    LEVEL_3(16,30,3),
    LEVEL_4(31,Integer.MAX_VALUE,4);
    private int lastHomeworkFrom;
    private int lastHomeworkTo;
    private int rank;
    private P2pTeacherActiveLevel(int dayFrom, int dayTo, int rank){
        this.lastHomeworkFrom = dayFrom;
        this.lastHomeworkTo = dayTo;
        this.rank = rank;
    }

    public static P2pTeacherActiveLevel pickFromDate(int dayDiff){
        for(P2pTeacherActiveLevel lv : P2pTeacherActiveLevel.values()){
            if(dayDiff >= lv.lastHomeworkFrom && dayDiff <= lv.lastHomeworkTo){
                return lv;
            }
        }
        return AUTO;
    }
}
