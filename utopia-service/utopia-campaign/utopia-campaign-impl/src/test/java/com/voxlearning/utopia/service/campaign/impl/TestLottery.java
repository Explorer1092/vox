package com.voxlearning.utopia.service.campaign.impl;

import com.voxlearning.utopia.service.campaign.api.enums.ActivityCardEnum;
import com.voxlearning.utopia.service.campaign.impl.service.lottery.*;

import java.util.HashMap;
import java.util.Map;

public class TestLottery {

    public static void main(String[] args) {
        LotteryCase caseA = new CaseA();
        LotteryCase caseB = new CaseB();
        LotteryCase caseC = new CaseC();
        LotteryCase caseD = new CaseD();

        Map<String, Integer> mapA = initMap();
        Map<String, Integer> mapB = initMap();
        Map<String, Integer> mapC = initMap();
        Map<String, Integer> mapD = initMap();

        for (int i = 0; i < 1000000; i++) {
            String descA = caseA.draw().getDesc();
            String descB = caseB.draw().getDesc();
            String descC = caseC.draw().getDesc();
            String descD = caseD.draw().getDesc();

            mapA.put(descA, mapA.get(descA) + 1);
            mapB.put(descB, mapB.get(descB) + 1);
            mapC.put(descC, mapC.get(descC) + 1);
            mapD.put(descD, mapD.get(descD) + 1);
        }

        System.out.println("case A:" + mapA);
        System.out.println("case B:" + mapB);
        System.out.println("case C:" + mapC);
        System.out.println("case D:" + mapD);
    }

    private static Map<String, Integer> initMap() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put(ActivityCardEnum.tian.getDesc(), 0);
        map.put(ActivityCardEnum.da.getDesc(), 0);
        map.put(ActivityCardEnum.de.getDesc(), 0);
        map.put(ActivityCardEnum.guan.getDesc(), 0);
        map.put(ActivityCardEnum.hai.getDesc(), 0);
        map.put(ActivityCardEnum.shen.getDesc(), 0);
        map.put(ActivityCardEnum.zhan.getDesc(), 0);
        map.put(ActivityCardEnum.jian.getDesc(), 0);
        return map;
    }
}
