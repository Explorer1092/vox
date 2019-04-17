package com.voxlearning.washington.mapper.specialteacher.studentimport;

import com.voxlearning.washington.mapper.specialteacher.base.ExcelExport;
import com.voxlearning.washington.mapper.specialteacher.base.ExcelExportData;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class StudentImportResultV2 implements ExcelExport {
    private List<StudentImportData> successList;
    private List<StudentImportData> failureList;
    private String[] successTitle;
    private String[] failureTitle;

    public StudentImportResultV2(String[] successTitle, String[] failureTitle) {
        successList = new LinkedList<>();
        failureList = new LinkedList<>();
        this.successTitle = successTitle;
        this.failureTitle = failureTitle;
    }

    public void success(StudentImportData data) {
        successList.add(data);
    }

    public void failed(StudentImportData data) {
        if (failureList.stream().noneMatch(d -> d.getRowIndex() == data.getRowIndex())) {
            failureList.add(data);
        }
    }

    @Override
    public List<ExcelExportData> toExportExcelData() {
        List<ExcelExportData> exportData = new LinkedList<>();
        int[] successWidth = new int[successTitle.length];
        Arrays.fill(successWidth, 5000);
        List<List<String>> successData = successList.stream()
                .map(data -> Arrays.asList(data.getGrade(), data.getClassName(), data.getStudentName(), data.getStudentNumber(), data.getScanNumber()))
                .collect(Collectors.toList());

        exportData.add(new ExcelExportData("成功列表", successTitle, successWidth, successData, successTitle.length));

        int[] failureWidth = new int[failureTitle.length];
        Arrays.fill(failureWidth, 5000);
        List<List<String>> failureData = failureList.stream()
                .map(data -> Arrays.asList(data.getGrade(), data.getClassName(), data.getStudentName(), data.getStudentNumber(), data.getReason()))
                .collect(Collectors.toList());
        ExcelExportData failureExport = new ExcelExportData("失败列表", failureTitle, failureWidth, failureData, failureTitle.length);
        failureExport.specialTitleStyle(4);
        exportData.add(failureExport);

        return exportData;
    }
}
