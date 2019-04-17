package com.voxlearning.washington.mapper.specialteacher.teacherimoprt;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.washington.mapper.specialteacher.base.SpecialTeacherConstants;
import lombok.Getter;
import lombok.Setter;


/**
 * 教务老师导入老师账号数据
 *
 * @author yuechen.wang
 * @since 2017-7-11
 **/
@Getter
@Setter
public class TeacherImportData {

    private String name;
    private String subject;
    private String mobile;
    // 导入成功之后回填的数据
    private Long teacherId;
    private String password;
    // 导入失败之后回填的数据
    private String reason;

    public TeacherImportData(String teacherName, String subjectName, String mobile) {
        this.name = StringUtils.deleteWhitespace(teacherName);
        this.subject = subjectName;
        this.mobile = mobile;
    }

    /**
     * 吾日三省吾身，学科、姓名和手机号
     */
    public boolean introspect() {
        // 检查学科
        if (subject == null || subject() == Subject.UNKNOWN) {
            return failed("暂时不支持该学科老师注册");
        }
        // 检查姓名
        if (!SpecialTeacherConstants.checkChineseName(name)) {
            return failed("姓名只支持10位以内的汉字和间隔符");
        }
        // 检查手机号
        if (!MobileRule.isMobile(mobile)) {
            return failed("手机号格式不正确");
        }
        return true;
    }

    public Subject subject() {
        return SpecialTeacherConstants.parseSubjectOfChinese(subject);
    }

    public void success(Long teacherId, String password) {
        this.teacherId = teacherId;
        this.password = password;
    }

    public boolean failed(String reason) {
        this.reason = reason;
        return false;
    }

}
