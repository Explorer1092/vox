package com.voxlearning.washington.mapper.specialteacher.markstudent;

import com.voxlearning.washington.mapper.specialteacher.base.ExcelExport;
import com.voxlearning.washington.mapper.specialteacher.base.ExcelExportData;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaochao.Wei
 * @since 2017/10/23
 */
public class MarkStudentResult implements ExcelExport {

    private List<MarkStudentData> successList;
    private List<MarkStudentData> failureList;

    public MarkStudentResult() {
        successList = new LinkedList<>();
        failureList = new LinkedList<>();
    }

    public void success(MarkStudentData data) {
        successList.add(data);
    }

    public void failed(MarkStudentData data) {
        failureList.add(data);
    }

    @Override
    public List<ExcelExportData> toExportExcelData() {
        List<ExcelExportData> exportData = new LinkedList<>();

        String[] successTitle = new String[]{"年级", "班级", "学生姓名", "学生校内学号", "标记", "成功/失败（操作后回填）", "备注（操作后回填）"};
        int[] successWidth = new int[]{4000, 3000, 5000, 8000, 4000, 4000, 8000};

        List<List<String>> successData = successList.stream()
                .map(data -> Arrays.asList(data.getGrade(), data.getClassName(), data.getStudentName(), data.getStudentNumber(), data.getIsMarked(), data.getResult(), data.getReason()))
                .collect(Collectors.toList());

        exportData.add(new ExcelExportData("成功列表", successTitle, successWidth, successData, 7));

        String[] failureTitle = new String[]{"年级", "班级", "学生姓名", "学生校内学号", "标记", "成功/失败（操作后回填）", "备注（操作后回填）"};
        int[] failureWidth = new int[]{4000, 3000, 5000, 8000, 4000, 4000, 8000};

        List<List<String>> failureData = failureList.stream()
                .map(data -> Arrays.asList(data.getGrade(), data.getClassName(), data.getStudentName(), data.getStudentNumber(), data.getIsMarked(), data.getResult(), data.getReason()))
                .collect(Collectors.toList());
        ExcelExportData failureExport = new ExcelExportData("失败列表", failureTitle, failureWidth, failureData, 7);
        failureExport.specialTitleStyle(4);
        exportData.add(failureExport);

        return exportData;
    }
}
