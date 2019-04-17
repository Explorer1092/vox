package com.voxlearning.washington.mapper.specialteacher.studentimport;

import com.voxlearning.washington.mapper.specialteacher.base.ExcelExport;
import com.voxlearning.washington.mapper.specialteacher.base.ExcelExportData;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 教务老师导入学生账号结果
 *
 * @author yuechen.wang
 * @since 2017-7-11
 **/
@Getter
@Setter
public class StudentImportResult implements ExcelExport {

    private List<StudentImportData> successList;
    private List<StudentImportData> failureList;
    private List<StudentImportData> recoverList;

    public StudentImportResult() {
        successList = new LinkedList<>();
        failureList = new LinkedList<>();
        recoverList = new LinkedList<>();
    }

    public void success(StudentImportData data) {
        successList.add(data);
    }

    public void failed(StudentImportData data) {
        if (failureList.stream().noneMatch(d -> d.getRowIndex() == data.getRowIndex())) {
            failureList.add(data);
        }
    }

    public void recovered(StudentImportData data) {
        recoverList.add(data);
    }

    @Override
    public List<ExcelExportData> toExportExcelData() {
        List<ExcelExportData> exportData = new LinkedList<>();

        String[] successTitle = new String[]{"年级", "班级", "学生姓名", "学生校内学号", "生成填涂号"};
        int[] successWidth = new int[]{4000, 3000, 5000, 8000, 5000};

        List<List<String>> successData = successList.stream()
                .map(data -> Arrays.asList(data.getGrade(), data.getClassName(), data.getStudentName(), data.getStudentNumber(), data.getScanNumber()))
                .collect(Collectors.toList());

        exportData.add(new ExcelExportData("成功列表", successTitle, successWidth, successData, 5));

        String[] failureTitle = new String[]{"年级", "班级", "学生姓名", "学生校内学号", "失败原因"};
        int[] failureWidth = new int[]{4000, 3000, 5000, 8000, 15000};

        List<List<String>> failureData = failureList.stream()
                .map(data -> Arrays.asList(data.getGrade(), data.getClassName(), data.getStudentName(), data.getStudentNumber(), data.getReason()))
                .collect(Collectors.toList());
        ExcelExportData failureExport = new ExcelExportData("失败列表", failureTitle, failureWidth, failureData, 5);
        failureExport.specialTitleStyle(4);
        exportData.add(failureExport);

        // 恢复列表
        List<List<String>> recoverData = recoverList.stream()
                .map(data -> Arrays.asList(data.getGrade(), data.getClassName(), data.getStudentName(), data.getStudentNumber(), data.getScanNumber()))
                .collect(Collectors.toList());
        exportData.add(new ExcelExportData("恢复列表", successTitle, successWidth, recoverData, 5 ));

        return exportData;
    }

}
