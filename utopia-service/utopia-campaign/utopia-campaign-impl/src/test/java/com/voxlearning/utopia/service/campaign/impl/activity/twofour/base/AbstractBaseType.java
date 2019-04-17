package com.voxlearning.utopia.service.campaign.impl.activity.twofour.base;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public abstract class AbstractBaseType {

    protected AbstractBaseType(String fileNamePrefix) {
        String rootPath = "D:\\Project\\17zuoye\\vox-17zuoye\\utopia-service\\utopia-campaign\\utopia-campaign-impl\\src\\main\\resources\\24game\\";

        FILE_TYPE_ALL = new File(rootPath + fileNamePrefix + "_所有方案.txt");
        FILE_TYPE_24 = new File(rootPath + fileNamePrefix + "_等于24的方案.txt");
        FILE_TYPE_24_ANSWER = new File(rootPath + fileNamePrefix + ".txt");

        resetFile();
    }

    StringBuilder TYPE_ALL_STRING = new StringBuilder();
    StringBuilder TYPE_24_STRING = new StringBuilder();
    Set<String> TYPE_ANSWER = new LinkedHashSet<>();

    /**
     * 所有组合结果
     */
    File FILE_TYPE_ALL;
    /**
     * 等于24的组合结果
     */
    File FILE_TYPE_24;
    /**
     * 去重后的组合
     */
    File FILE_TYPE_24_ANSWER;


    protected void calc(int a, int b, int c, int d, String expression) {
        String result = TwoFourUtils.evaluateExpression(expression);
        record(a, b, c, d, expression, result);
    }

    public void flushFile() throws IOException {
        FileUtils.writeStringToFile(FILE_TYPE_ALL, TYPE_ALL_STRING.toString(), Charset.defaultCharset(), true);
        FileUtils.writeStringToFile(FILE_TYPE_24, TYPE_24_STRING.toString(), Charset.defaultCharset(), true);

        for (String item : TYPE_ANSWER) {
            FileUtils.writeStringToFile(FILE_TYPE_24_ANSWER, item + "\n", Charset.defaultCharset(), true);
        }
    }

    private void record(int a, int b, int c, int d, String string, String count) {
        StringBuilder sb = new StringBuilder().append(string).append("=").append(count).append("\n");

        if (count.equalsIgnoreCase("24")) {
            String compile = compile(a, b, c, d);
            TYPE_ANSWER.add(compile);
            TYPE_24_STRING.append(sb);
        }

        TYPE_ALL_STRING.append(sb);
    }

    private String compile(int i, int j, int k, int l) {
        int[] array = new int[]{i, j, k, l};
        Arrays.sort(array);
        return StringUtils.join(array, ',');
    }

    private void resetFile() {
        if (FILE_TYPE_ALL.exists()) {
            FILE_TYPE_ALL.delete();
        }
        if (FILE_TYPE_24.exists()) {
            FILE_TYPE_24.delete();
        }
        if (FILE_TYPE_24_ANSWER.exists()) {
            FILE_TYPE_24_ANSWER.delete();
        }
    }


}
