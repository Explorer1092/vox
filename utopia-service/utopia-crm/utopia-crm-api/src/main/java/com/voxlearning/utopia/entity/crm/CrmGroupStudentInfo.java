package com.voxlearning.utopia.entity.crm;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.voxlearning.alps.lang.convert.SafeConverter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by song.wang on 2016/4/25.
 */

@Getter
@Setter
@NoArgsConstructor
public class CrmGroupStudentInfo implements Serializable {
    private Long studentid;            // 学生ID FIXME 线上数据没有这个字段...要取的话..还是用 Map 的 Key
    private String studentname;        // 学生姓名
    private Long registertime;         // 学生注册时间
    private Boolean englishauthed;     // 英语科目是否已经认证
    private Long englishauthedtime;    // 英语科目认证时间
    private Boolean mathauthed;        // 数学科目是否已经认证
    private Long mathauthedtime;       // 数学科目认证时间
    private Integer authhwcount;       // 完成的有效认证作业数量, 注意：是本组老师名下的作业
    private Long latestauthhwtime;     // 最近一次完成有效认证作业时间,  注意：是本组老师名下的作业
    private Long latesthwtime;         // 最近一次完成所有作业时间
    private Boolean hasparent;         // 是否有家长
    private Boolean mobilebinded;      // 学生 or 家长是否绑定了手机

//    private Boolean sascstudent;       // 是否单活学生
//    private Boolean dascstudent;       // 是否双活学生
//    private Boolean englishsasc;       // 英语是否活跃
//    private Boolean mathsasc;          // 数学是否活跃

    // ------------------------------------------------------------------------------------------------
    // Alex 20160711
    // 以下是老字段，为了兼容历史数据所以保留下来，新的数据生产逻辑可以忽略以下字段
    // ------------------------------------------------------------------------------------------------
//    @Deprecated private Boolean hcastudent;//是否是本月高质量学生
//    @Deprecated private Integer hcahwcount;//完成本月高质量作业数量 Integer 注意：换月要清零
//    @Deprecated private Long latesthcahwtime;//最近一次完成本月高质量作业时间 Long    注意：换月要清零


    public Boolean checkEnglishAuthed() {
        return englishauthed == null ? false : englishauthed;
    }

    public Boolean checkMathAuthed() {
        return mathauthed == null ? false : mathauthed;
    }

//    @Deprecated
//    public Boolean checkHcaStudent() {
//        return hcastudent == null ? false : hcastudent;
//    }

    public String getRegisterTimeStr() {
        return this.registertime != null ? dateToString(new Date(this.registertime), "yyyy-MM-dd") : "";
    }

//    @Deprecated
//    public String getLatestHcaHwTimeStr() {
//        return this.latesthcahwtime != null ? dateToString(new Date(this.latesthcahwtime), "yyyy-MM-dd") : "";
//    }

    public String getLatestAuthHwTimeStr() {
        return this.latestauthhwtime != null ? dateToString(new Date(this.latestauthhwtime), "yyyy-MM-dd") : "";
    }

    public String getEnglishAuthedTimeStr() {
        return this.englishauthedtime != null ? dateToString(new Date(this.englishauthedtime), "yyyy-MM-dd") : "";
    }

    public String getEnglishMathAuthedTimeStr() {
        return this.mathauthedtime != null ? dateToString(new Date(this.mathauthedtime), "yyyy-MM-dd") : "";
    }

    public Long getAuthTime() {
        if (englishauthedtime != null && mathauthedtime != null) {
            return englishauthedtime < mathauthedtime ? englishauthedtime : mathauthedtime;
        }

        if (englishauthedtime != null) {
            return englishauthedtime;
        }

        if (mathauthedtime != null) {
            return mathauthedtime;
        }
        return 0L;
    }

    public String getAuthTimeStr() {
        Long authTime = getAuthTime();
        if (authTime.equals(0L)) {
            return "";
        }
        return dateToString(new Date(authTime), "yyyy-MM-dd");
    }

    @JsonIgnore
    public boolean hasRegisted() {
        return registertime != null && registertime > 0L;
    }

    @JsonIgnore
    public boolean hasAuthed() {
        return SafeConverter.toBoolean(this.isauthed);
        //return Boolean.TRUE.equals(englishauthed) || Boolean.TRUE.equals(mathauthed);
    }

    private static String dateToString(Date date, String format) {
        try {
            SimpleDateFormat dft = new SimpleDateFormat(format);
            return dft.format(date);
        }catch(Exception e) {
            return "";
        }
    }


    // ---------------------------------  20170112 wangsong add -------------------//
    private Boolean chnauthed; // 语文科目是否已经认证
    private Long chnauthedtime;// 语文科目认证时间
    private Boolean isauthed; // 是否认证
    private Boolean islmcsactive;// 上月当前科目是否活跃
    private Boolean istmcsactive;//本月当前科目是否活跃
    private Integer tmcshwsc;//本月完成当前科目作业套数
    private Integer lmcshwsc;//上月完成当前科目作业套数
    // ---------------------------------- 20170531 wyg add--------------------------//
    private Integer issinglesubjectactive;     //单科最大完成套数 大于3为活跃用户

//    private Long klxtn; // 学生快乐学考号
//    private Long tmcsanshcount;//学生当月答题卡作答此科目试卷数
//    private Long lmcsanshcount;//学生上月答题卡作答此科目试卷数
//
//    @JsonIgnore
//    public boolean hasKlxTn() {
//        return this.klxtn != null;
//    }
}
