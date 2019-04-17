package com.voxlearning.utopia.service.ai.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.constant.ChipsEnglishLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xuan.zhu
 * @date 2018/9/17 11:22
 * 薯条用户额外信息拆分(与大数据)
 */
@Setter
@Getter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-misc")
@DocumentDatabase(database = "vox-ai")
@DocumentCollection(collection = "vox_chips_english_user_ext_split")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20190403")
public class ChipsEnglishUserExtSplit implements Serializable {
    private static final long serialVersionUID = 2675365298147192854L;

    @DocumentId
    private Long id;                 //用户id
    @DocumentField(value = "wx_code")
    private String wxCode;           //微信号
    @DocumentField(value = "study_duration")
    private String studyDuration;   //学习年限，问卷第 2 题
    @DocumentField(value = "buy_competitor")
    private Boolean buyCompetitor;   //购买竞品
    @DocumentField(value = "show_play")
    private Boolean showPlay;        //是否显示电子教材
    @DocumentField(value = "level")
    private ChipsEnglishLevel level; //定级

    @DocumentField(value = "sentence_learn")
    private Long sentenceLearn; //句子学习的数量

    @DocumentField(value = "wx_add")
    private Boolean weAdd;  //是否加微信
    @DocumentField(value = "ep_wx_add")
    private Boolean epWxAdd;//是否添加企业微信

    // 问卷字段
    @DocumentField(value = "grade")
    private String grade;            //年级，问卷第 1 题
    @Deprecated
    @DocumentField(value = "interest")
    private String interest;         //兴趣，问卷第 3 题
    @DocumentField(value = "expect")
    private String expect;         //对薯条的期待，新问卷第 5 题
    @Deprecated
    @DocumentField(value = "mentor")
    private String mentor;           //谁辅导孩子，问卷第 4 题
    @DocumentField(value = "week_points")
    private String weekPoints;       //英语薄弱点，问卷第 5 题
    @DocumentField(value = "other_extra_registration")
    private String otherExtraRegistration; //其他课外报名，对应问卷第 6 题
    @DocumentField(value = "recently_score")
    private String recentlyScore;    //最近成绩，对应问卷第 7 题
    @DocumentField(value = "service_score")
    private Integer serviceScore;    // 服务价值分  根据各个题目的答案算出来。。太恶心


    @DocumentField(value = "wx_name")
    private String wxName; //微信昵称
    @DocumentField(value = "recipient_name")
    private String recipientName; //收货人姓名
    @DocumentField(value = "recipient_tel")
    private String recipientTel; //收货人电话
    @DocumentField(value = "recipient_addr")
    private String recipientAddr; //收货人地址
    @DocumentField(value = "course_level")
    private String courseLevel;//后续课程级别



    @DocumentUpdateTimestamp
    @DocumentField(value = "ut")
    private Date updateTime;
    @DocumentCreateTimestamp
    @DocumentField(value = "ct")
    private Date createTime;

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(ChipsEnglishUserExtSplit.class, id);
    }

    /**
     * 判断该用户是否填写了问卷
     * @return true代表填写了，false代表没填写
     */
    public boolean isFillQuestionnaire(){
        if(StringUtils.isNotBlank(studyDuration)){
            return true;
        }
        if(StringUtils.isNotBlank(grade)){
            return true;
        }
        if(StringUtils.isNotBlank(expect)){
            return true;
        }
        if(StringUtils.isNotBlank(weekPoints)){
            return true;
        }
        if(StringUtils.isNotBlank(otherExtraRegistration)){
            return true;
        }
        if(StringUtils.isNotBlank(recentlyScore)){
            return true;
        }
        if (serviceScore != null && serviceScore != 0) {
            return true;
        }
        return false;
    }

}
