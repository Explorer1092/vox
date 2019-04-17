package com.voxlearning.utopia.service.campaign.impl.activity.twofour;


import com.voxlearning.utopia.service.campaign.impl.activity.twofour.type.TypeA;
import com.voxlearning.utopia.service.campaign.impl.activity.twofour.type.TypeB;
import com.voxlearning.utopia.service.campaign.impl.activity.twofour.type.TypeC;
import com.voxlearning.utopia.service.campaign.impl.activity.twofour.type.TypeD;

import java.io.IOException;

public class Generate24PointAnswer {

    public static void main(String[] args) throws IOException {
        //贵州
        gz();
    }

    private static void gz() throws IOException {
        int MAX = 13;

        TypeA typeA = new TypeA("13_plan_a");
        TypeB typeB = new TypeB("13_plan_b");
        TypeC typeC = new TypeC("13_plan_c");
        TypeD typeD = new TypeD("13_plan_d");

        for (int a = 1; a <= MAX; a++) {
            for (int b = 1; b <= MAX; b++) {
                for (int c = 1; c <= MAX; c++) {
                    for (int d = 1; d <= MAX; d++) {
                        System.out.println(a + " " + b + " " + c + " " + d);

                        typeA.genScheme(a, b, c, d);
                        typeB.genScheme(a, b, c, d);
                        typeC.genScheme(a, b, c, d);
                        typeD.genScheme(a, b, c, d);
                    }
                }
            }
        }

        typeA.flushFile();
        typeB.flushFile();
        typeC.flushFile();
        typeD.flushFile();
    }
}
