package com.voxlearning.washington.mapper.specialteacher.teacherimoprt;

import com.voxlearning.washington.mapper.specialteacher.base.ExcelExportData;
import com.voxlearning.washington.mapper.specialteacher.base.ExcelExport;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 教务老师导入老师账号结果
 *
 * @author yuechen.wang
 * @since 2017-7-11
 **/
@Getter
@Setter
public class TeacherImportResult implements ExcelExport {

    private List<TeacherImportData> successList;
    private List<TeacherImportData> failureList;

    public TeacherImportResult() {
        successList = new LinkedList<>();
        failureList = new LinkedList<>();
    }

    public void success(TeacherImportData data) {
        successList.add(data);
    }

    public void failed(TeacherImportData data) {
        failureList.add(data);
    }

    @Override
    public List<ExcelExportData> toExportExcelData() {
        List<ExcelExportData> exportData = new LinkedList<>();

        String[] successTitle = new String[]{"老师姓名", "学科", "老师手机", "老师ID", "老师密码"};
        int[] successWidth = new int[]{4000, 3000, 5000, 5000, 5000};

        List<List<String>> successData = successList.stream()
                .map(data -> Arrays.asList(data.getName(), data.getSubject(), data.getMobile(), String.valueOf(data.getTeacherId()), data.getPassword()))
                .collect(Collectors.toList());

        exportData.add(new ExcelExportData("成功列表", successTitle, successWidth, successData, 5));

        String[] failureTitle = new String[]{"老师姓名", "学科", "老师手机", "失败原因"};
        int[] failureWidth = new int[]{4000, 3000, 5000, 8000};

        List<List<String>> failureData = failureList.stream()
                .map(data -> Arrays.asList(data.getName(), data.getSubject(), data.getMobile(), data.getReason()))
                .collect(Collectors.toList());
        ExcelExportData failureExport = new ExcelExportData("失败列表", failureTitle, failureWidth, failureData, 4);
        failureExport.specialTitleStyle(3);
        exportData.add(failureExport);

        return exportData;
    }

}
