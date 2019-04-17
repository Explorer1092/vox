package com.voxlearning.utopia.admin.data;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * 校内打散换班，复制教学班学生导入模板
 *
 * @author Xiaochao.Wei
 * @since 2017/7/13
 */

@Getter
@Setter
public class LinkClassImportData {

    private String grade;           // 年级
    private String studentName;     // 学生姓名
    private String targetClazz;     // 目标班级 打散换班中叫换班后新班级，复制教学班学生中叫关联的教学班
    @Deprecated private String mobile;   // 老师手机号  FIXME Feature #51293 老师手机号不用了
    @Deprecated private Long teacherId;  // 老师ID  FIXME Feature #51293 老师手机号不用了,那么teacherId也就没用了

    private String result;          // 回填数据,成功/失败
    private String remark;          // 回填数据，备注


    public LinkClassImportData(String grade, String studentName, String targetClazz) {
        this.grade = grade;
        this.studentName = studentName;
        this.targetClazz = targetClazz;
    }

    public void success(String remark) {
        this.result = "1.操作成功";
        this.remark = remark;
    }

    public void failed(String remark) {
        this.result = "2.操作失败";
        this.remark = remark;
    }

    public String classKey() {
        return StringUtils.join(grade, "_", targetClazz);
    }

    public String classStudentKey() {
        return StringUtils.join(grade, "_", studentName);
    }

}
