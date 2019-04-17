package com.voxlearning.utopia.service.crm.tools; /**
 * @author fugui.chang
 * @since 2017/3/22
 */

import java.io.*;

public class TestDemo {

    public static void main(String[] args) throws IOException {
            String city = "北京郊区";
            String schoolNameUgc = "测试的";
            String schoolNameSys = "QWQW";

            String type = "小学";
            Double sim1 = SchoolNameSimilarityCalculator.calculateSimilarityValue(schoolNameUgc, schoolNameSys, city, type);
            System.out.println(schoolNameUgc + "," + schoolNameSys + "," + sim1 );

    }

}
