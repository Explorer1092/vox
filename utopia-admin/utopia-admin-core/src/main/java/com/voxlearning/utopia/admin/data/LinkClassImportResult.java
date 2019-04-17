package com.voxlearning.utopia.admin.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaochao.Wei
 * @since 2017/7/13
 */
public class LinkClassImportResult {
    public List<LinkClassImportData> successList;
    public List<LinkClassImportData> failedList;

    public LinkClassImportResult() {
        successList = new ArrayList<>();
        failedList = new ArrayList<>();
    }

    public void success(LinkClassImportData data) {
        successList.add(data);
    }

    public void failed(LinkClassImportData data) {
        failedList.add(data);
    }

    public List<ExcelExportData> toExportExcelData() {
        List<ExcelExportData> exportData = new LinkedList<>();

        String[] successTitle = new String[]{"年级", "学生姓名", "关联的教学班", "成功/失败（操作后回填）", "备注（操作后回填）"};
        int[] successWidth = new int[]{4000, 4000, 4000, 4000, 8000};

        List<List<String>> successData = successList.stream()
                .map(data -> Arrays.asList(data.getGrade(), data.getStudentName(), data.getTargetClazz(), data.getResult(), data.getRemark()))
                .collect(Collectors.toList());

        exportData.add(new ExcelExportData("成功列表", successTitle, successWidth, successData, 5));

        String[] failedTitle = new String[]{"年级", "学生姓名", "关联的教学班", "成功/失败（操作后回填）", "备注（操作后回填）"};
        int[] failedWidth = new int[]{4000, 4000, 4000, 4000, 8000};

        List<List<String>> failedData = failedList.stream()
                .map(data -> Arrays.asList(data.getGrade(), data.getStudentName(), data.getTargetClazz(), data.getResult(), data.getRemark()))
                .collect(Collectors.toList());

        exportData.add(new ExcelExportData("失败列表", failedTitle, failedWidth, failedData, 5));

        return exportData;
    }

}
