package com.voxlearning.utopia.admin.data;


import lombok.Getter;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Yuechen.Wang on 2017/7/21.
 */
public enum ExcelTemplate {

    // 创建老师账号
    CREATE_TEACHER_ACCOUNT(
            "lxy",
            "添加老师账号模板.xls",
            "/config/templates/create_teacher_account_template.xls"
    ),

    // 为老师建班授课-初中
    CREATE_TEACHER_CLASS_MIDDLE(
            "lkh",
            "初中-为老师建班授课模板.xls",
            "/config/templates/create_teacher_class_middle_template.xls"
    ),

    // 为老师建班授课-高中
    CREATE_TEACHER_CLASS_SENIOR(
            "lkl",
            "高中-为老师建班授课模板.xls",
            "/config/templates/create_teacher_class_senior_template.xls"
    ),

    // 创建学生账号
    CREATE_STUDENT_ACCOUNT(
            "zlr",
            "添加学生账号模板.xls",
            "/config/templates/create_student_account_template.xls"
    ),

    // 复制教学班学生
    COPY_TEACHING_CLASS(
            "jt",
            "复制教学班学生模板.xls",
            "/config/templates/copy_teaching_class_template.xls"
    ),

    // 校内打散换班
    CHANGE_STUDENT_CLASS(
            "xhjx",
            "校内打散换班（文理分班）模板.xls",
            "/config/templates/change_student_class_template.xls"
    ),

    // 班级导入学生
    IMPORT_CLASS_STUDENT(
            "xcx",
            "批量导入学生数据模板.xls",
            "/config/templates/special_teacher_students_template.xls"
    ),

    // 标记借读生
    MARK_STUDENTS(
            "mst",
            "批量标记借读生数据模板.xls",
            "/config/templates/mark_students_template.xls"
    ),

    // 标记借读生
    UPDATE_STUDENTS_NUM(
            "usm",
            "更新学生学号导入模板.xls",
            "/config/templates/update_students_num_template.xlsx"
    );

    @Getter private final String id;
    @Getter private final String fileName;
    @Getter private final String templatePath;

    ExcelTemplate(String id, String fileName, String templatePath) {
        this.id = id;
        this.fileName = fileName;
        this.templatePath = templatePath;
    }

    private static final Map<String, ExcelTemplate> templateMap;

    static {
        templateMap = Stream.of(values()).collect(Collectors.toMap(ExcelTemplate::getId, Function.identity()));
    }

    public static ExcelTemplate safeParse(String id) {
        if (id == null || !templateMap.containsKey(id)) {
            return null;
        }
        return templateMap.getOrDefault(id, null);
    }

}
