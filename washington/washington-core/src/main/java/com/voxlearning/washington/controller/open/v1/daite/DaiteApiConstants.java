package com.voxlearning.washington.controller.open.v1.daite;

public class DaiteApiConstants{

    // 请求参数
    public static final String REQ_TIMESTAMP = "timestamp";
    public static final String REQ_CLASS_ID = "class_id";
    public static final String REQ_CLASS_LEVEL = "class_level";
        public static final String REQ_CLASS_TYPE = "class_type";
    public static final String REQ_EDU_SYSTEM = "edu_system";
    public static final String REQ_CLASS_NAME = "name";
    public static final String REQ_REALNAME = "realname";
    public static final String REQ_AVATAR = "avatar";
    public static final String REQ_RELATIONS = "relations";


    // 返回参数
    public static final String RES_NO_SCHOOL = "没有找到学校，请核实";
    public static final String RES_NO_CLAZZ = "没有找到班级，请核实";
    public static final String RES_NO_USER = "没有找到用户，请核实";
    public static final String RES_NO_TEACHER = "没有找到老师用户，请核实";
    public static final String RES_NO_TEACHER_SUBJECT = "该老师没有学科，请核实";
    public static final String RES_NO_STUDENT = "没有找到学生用户，请核实";
    public static final String RES_LEVEL_STUDENT_DIFF = "学生不能加入不同级别的班级";
    public static final String RES_SCHOOL_STUDENT_DIFF = "学生不能加入不同学校的班级";
    public static final String RES_SYSTEM_STUDENT_DIFF = "学生只能在一个行政班";
    public static final String RES_CLAZZ_STUDENT_EXIST = "学生已经在该班级";
    public static final String RES_CLAZZ_STUDENT_NOT_EXIST = "学生不在该班级";
    public static final String RES_CLAZZ_TEACHER_EXIST = "老师已经在该班级";
    public static final String RES_NO_DAITE = "非戴特合作校，请核实";
    public static final String RES_NO_USER_DAITE = "用户所在学校，非戴特合作校，请核实";
    public static final String RES_SCHOOL_USER_DIFF = "不允许跨校换班";
    public static final String RES_KTWELVE_USER_DIFF = "不允许跨学段";
    public static final String RES_NO_TEACHER_CLAZZ = "非老师班级";
    public static final String RES_SCHOOL_EDU_DIFF = "非学校学制";
    public static final String RES_EDU_LEVEL_DIFF = "学制和级别不匹配";
}
