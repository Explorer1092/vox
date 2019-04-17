package com.voxlearning.washington.mapper.specialteacher.teacherclazz;

import com.voxlearning.washington.mapper.specialteacher.base.ExcelExport;
import com.voxlearning.washington.mapper.specialteacher.base.ExcelExportData;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Xiaochao.Wei
 * @since 2017/12/18
 */

@Setter
@Getter
public class TeacherClazzImportResult implements ExcelExport {

    private List<TeacherClazzImportData> successList;
    private List<TeacherClazzImportData> failedList;

    public TeacherClazzImportResult() {
        successList = new ArrayList<>();
        failedList = new ArrayList<>();
    }
    public void success(TeacherClazzImportData data) {
        successList.add(data);
    }

    public void failed(TeacherClazzImportData data) {
        failedList.add(data);
    }


    @Override
    public List<ExcelExportData> toExportExcelData() {
        List<ExcelExportData> exportData = new LinkedList<>();

        String[] successTitle = new String[]{"老师姓名", "学科", "年级", "班级", "班级类型"};
        int[] successWidth = new int[]{4000, 4000, 4000, 4000, 8000};

        List<List<String>> successData = successList.stream()
                .map(data -> Arrays.asList(data.getTeacherName(), data.getSubject(), data.getGrade(), data.getClazzName(), data.getClazzType()))
                .collect(Collectors.toList());

        exportData.add(new ExcelExportData("成功列表", successTitle, successWidth, successData, 5));

        String[] failedTitle = new String[]{"老师姓名", "学科", "年级", "班级", "班级类型", "失败原因"};
        int[] failedWidth = new int[]{4000, 4000, 4000, 4000, 4000, 8000};

        List<List<String>> failedData = failedList.stream()
                .map(data -> Arrays.asList(data.getTeacherName(), data.getSubject(), data.getGrade(), data.getClazzName(), data.getClazzType(), data.getReason()))
                .collect(Collectors.toList());

        exportData.add(new ExcelExportData("失败列表", failedTitle, failedWidth, failedData, 6));

        return exportData;
    }
}
