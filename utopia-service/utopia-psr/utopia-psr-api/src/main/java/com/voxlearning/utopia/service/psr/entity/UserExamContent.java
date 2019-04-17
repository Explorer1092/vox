package com.voxlearning.utopia.service.psr.entity;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/*
 * 学生 或者 班级 对知识点的掌握信息
 * uType:类型信息,包括class 和 student
 * userId:classId 或者 studentId
 * irtTheta: 学生能力值,irt模型计算出来的,
 * ek：知识点,包括grammar 和 word
 * master:对知识点ek的掌握程度,范围 [0,1]
 * userCount:人数,对班级有效.即uType=class,如果uType=student 则该值为1
 * level:掌握度分级, 0未掌握,1基本掌握,2掌握:该用户至少做对过一道该知识点下的题, 3掌握：未做过该知识点下的题
 * classId:班级Id,uType=student时 有效
 * schoolId:学校Id,uType=student时 有效
 * regionCode:地区代码
 * grade: 年级信息
 */
@Data
public class UserExamContent implements Serializable {

    private static final long serialVersionUID = 1502884096055665486L;

    private String uType;
    private Long userId;
    private double irtTheta;
    private double uc;
    private int classId;
    private int schoolId;
    private int regionCode;
    private int grade;
    /** 0：from learning_profile，1：from uc，2：no uc，只有补题逻辑的时候会 = 1 or = 2 */
    private int userInfoLevel;
    private List<UserEkContent> ekList;

    public boolean isEkListNull() {
        return (ekList == null);
    }

    public UserExamContent() {
        uType = "student";
        userId = 0L;
        irtTheta = 0.0D;
    }

    public UserExamContent(UserExamContent para) {
        if (para == null)
            return;
        this.uType = para.getUType();
        this.userId = para.getUserId();
        this.irtTheta = para.getIrtTheta();
        this.uc = para.getUc();
        this.classId = para.getClassId();
        this.schoolId = para.getSchoolId();
        this.regionCode = para.getRegionCode();
        this.grade = para.getGrade();
        this.userInfoLevel = para.getUserInfoLevel();
        this.ekList = new ArrayList<>();
        this.ekList.addAll(para.getEkList());
    }

    public UserExamContent(UserExamContent para, boolean isEkId) {
        if (para == null)
            return;
        this.uType = para.getUType();
        this.userId = para.getUserId();
        this.irtTheta = para.getIrtTheta();
        this.uc = para.getUc();
        this.classId = para.getClassId();
        this.schoolId = para.getSchoolId();
        this.regionCode = para.getRegionCode();
        this.grade = para.getGrade();
        this.userInfoLevel = para.getUserInfoLevel();
        this.ekList = new ArrayList<>();
        if (para.getEkList() == null)
            return;
        for (UserEkContent userEkContent : para.getEkList()) {
            if (isEkId ? !StringUtils.isNumeric(userEkContent.getEk()) : StringUtils.isNumeric(userEkContent.getEk()))
                continue;
            this.ekList.add(userEkContent);
        }
    }

    public List<String> getEks() {

        if (isEkListNull()) return null;

        List<String> list = new ArrayList<String>();

        for (UserEkContent userEkContent : ekList) {
            ((ArrayList<String>) list).add(userEkContent.getEk());
        }

        return list;
    }

    /*
     * 调试时 使用
     */
    public void printOut() {
        String strOut = uType == null ? "student" : uType;
        strOut += "\t" + userId.toString();
        strOut += "\t" + ((Double) irtTheta).toString();
        strOut += "\t";

        for (int i = 0; i < ekList.size(); i++) {
            strOut += ekList.get(i).getEk();
            strOut += ";" + ((Double) ekList.get(i).getMaster()).toString();
            strOut += ";" + ((Short) ekList.get(i).getCount()).toString();
            strOut += ";" + ((Short) ekList.get(i).getLevel()).toString() + ";";
        }


        strOut += "\t" + ((Integer) classId).toString();
        strOut += "\t" + ((Integer) schoolId).toString();
        strOut += "\t" + ((Integer) regionCode).toString();
        strOut += "\t" + ((Integer) grade).toString();

        System.out.println(strOut);
    }
}

