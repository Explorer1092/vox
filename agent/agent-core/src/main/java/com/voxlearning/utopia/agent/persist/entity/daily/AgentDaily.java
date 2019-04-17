package com.voxlearning.utopia.agent.persist.entity.daily;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 *  日报实体
 *
 * @author deliang.che
 * @since  2018/9/19
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_daily")
public class AgentDaily implements CacheDimensionDocument {

    public static final Integer ON_TIME_SUBMIT = 1; //按时提交
    public static final Integer BE_LATE_SUBMIT = 2; //迟交
    private static final long serialVersionUID = 5733403744051082336L;

    @DocumentId
    private String id;

    private Long userId;        //人员ID
    private Integer dailyTime;  //日报日期,格式：20180920

    private String name;        //日报名称

    private Double workload;            //工作量
    private Integer intoSchoolNum;      //进校数目
    private Integer visitTeaNum;        //拜访老师总数
    private Integer visitEngTeaNum;     //拜访英语老师数目
    private Integer visitMathTeaNum;    //拜访数学老师数目
    private Integer visitOtherTeaNum;   //拜访其他老师数目

    private Double perPersonIntoSchoolNum;      //人均进校数目
    private Double perPersonVisitTeaNum;        //人均拜访老师数目
    private Double perPersonVisitEngTeaNum;     //人均拜访英语老师数目
    private Double perPersonVisitMathTeaNum;    //人均拜访数学老师数目
    private Double perPersonVisitOtherTeaNum;   //人均拜访其他老师数目

    private Integer assignHomeWorkTeaNum;       //布置作业老师数目
    private Integer assignHomeWorkEngTeaNum;    //布置作业英语老师数目
    private Integer assignHomeWorkMathTeaNum;   //布置作业数学老师数目
    private Integer assignHomeWorkOtherTeaNum;  //布置作业其他老师数目

    private Double perPersonAssignHwTeaNum;        //人均布置作业老师数目
    private Double perPersonAssignHwEngTeaNum;     //人均布置作业英语老师数目
    private Double perPersonAssignHwMathTeaNum;    //人均布置作业数学老师数目
    private Double perPersonAssignHwOtherTeaNum;   //人均布置作业其他老师数目

    private String otherWorkResult; //其他工作达成结果

    private String content;         //今日工作内容（用于列表展示）

    //当天工作记录与前一天日报计划对比情况
    private List<AgentDailyCompareInfo> intoSchoolCompareList; //进校对比
    private List<AgentDailyCompareInfo> meetingCompareList;    //组会对比
    private List<AgentDailyCompareInfo> researcherCompareList; //拜访教研员对比
    private List<AgentDailyCompareInfo> partnerCompareList;    //陪同对象对比

    private Integer status;         //提交状态（1：按时提交  2：迟交）

    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
             newCacheKey("id", this.id),
             newCacheKey(new String[]{"uid","time"},new Object[]{this.userId,this.dailyTime})
        };
    }
}

