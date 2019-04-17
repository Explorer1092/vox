package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * AgentWorkRecordStatistics
 *
 * @author song.wang
 * @date 2018/1/23
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_work_record_statistics")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20180603")
public class AgentWorkRecordStatistics implements CacheDimensionDocument {

    @DocumentId
    private String id;
    private Integer date;                            // 格式： 20180125    dateType=2时 date为周一所在的日期（20180122）， dataType=3时date为当月的第一天（20180101）
    private Integer dateType;                        // 日期类型  1： 日  2：周  3：月

    private Integer groupOrUser;                     // 团队，个人   1：团队  2：个人

    private Long groupId;                            // 部门ID
    private String groupName;                        // 部门名称
    private Integer groupRoleId;        // 部门角色

    private Long parentGroupId;                      // 上级部门ID
    private String parentGroupName;                  // 上级部门名称

    private Map<Integer, AgentWorkRecordStatisticsRoleData> roleDataMap;

    @Deprecated
    private Integer groupUserCount;                  // 部门下所有人员数量
    @Deprecated
    private Integer fillInWorkRecordUserCount;       // 填写工作记录的人员数量

    // 下属工作情况
    @Deprecated private Integer bdUserCount;                     // 专员数量
    @Deprecated private Integer bdFillInWorkRecordUserCount;     // 填写工作记录的专员数量
    @Deprecated private Integer bdUnrecordedUserCount;           // 未录入专员数（当天未录入， 3天未录入， 5天未录入）
    @Deprecated private Double bdPerCapitaWorkload;             //  专员人均日均工作量

    @Deprecated private Integer cmUserCount;                     // 市经理数量
    @Deprecated private Integer cmFillInWorkRecordUserCount;     // 填写工作记录的市经理数量
    @Deprecated private Integer cmUnrecordedUserCount;           // 未录入市经理数（当天未录入， 3天未录入， 5天未录入）
    @Deprecated private Double cmPerCapitaWorkload;             // 市经理人均日均工作量



    @Deprecated private Integer amUserCount;                     // 区域经理数量
    @Deprecated private Integer amFillInWorkRecordUserCount;     // 填写工作记录的区域经理数量
    @Deprecated private Integer amUnrecordedUserCount;           // 未录入区域经理数（当天未录入， 3天未录入， 5天未录入）
    @Deprecated private Double amPerCapitaWorkload;             // 区域经理人均日均工作量

    @Deprecated private Integer rmUserCount;                     // 大区经理数量
    @Deprecated private Integer rmFillInWorkRecordUserCount;     // 填写工作记录的大区经理数量
    @Deprecated private Integer rmUnrecordedUserCount;           // 未录入大区经理数（当天未录入， 3天未录入， 5天未录入）
    @Deprecated private Double rmPerCapitaWorkload;             // 大区经理人均日均工作量


    // 专员进校情况
    @Deprecated private Double bdPerCapitaIntoSchool;             // 专员人均日均进校次数
    @Deprecated private Double bdVisitSchoolAvgTeaCount;           // 校均拜访老师数
    @Deprecated private Integer bdVisitTeaCount;                   // 专员拜访的老师总数
    @Deprecated private Integer bdVisitEngTeaCount;                 // 专员拜访英语老师总数
    @Deprecated private Integer bdVisitMathTeaCount;              // 专员拜访的数学老师总数

    // 我的工作
    private Long userId;
    private String userName;                             // 用户名
    private Integer userRoleId;                      // 用户角色

    private Integer userVisitSchoolCount;                    // 进校数或者陪访数
    private Double userIntoSchoolWorkload;                   // 进校工作量
    private Double userVisitWorkload;                        // 陪访工作量
    private Double userMeetingWorkload;                      // 组会&参与组会工作量
    private Double userTeachingWorkload;                     // 拜访教研员工作量
    private Double userWorkload;                             // 全部工作量（部门经理或者专员）
    private Integer userWorkDays;                            // 工作天数
    private Integer userNeedWordDays;                        // 需要工作的天数（工作日）

    @Deprecated private Double userAvgDayIntoSchool;                 // 用户日均进校次数
    @Deprecated private Double userVisitSchoolAvgTeaCount;           // 校均拜访老师数
    @Deprecated private Integer userVisitTeaCount;                   // 用户拜访的老师总数
    @Deprecated private Integer userVisitEngTeaCount;                 // 用户拜访英语老师总数
    @Deprecated private Integer userVisitMathTeaCount;              // 用户拜访的数学老师总数
    @Deprecated private Double userVisitAndAssignHwTeaPct;         // 当月拜访并布置作业的老师数比例

    //团队专员进校统计
    private Double perPersonIntoSchoolCount;        //人均进校数（部门）
    private Double perPersonVisitTeacherCount;      //人均见师量（部门）
    private Double perPersonVisitMathTeacherCount;  //人均见师-数学（部门）
    private Double perPersonVisitEngTeacherCount;   //人均见师-英语（部门）
    private Double perPersonVisitOtherTeacherCount; //人均见师-其他（部门）
    private Integer bdIntoSchoolCount;              // 专员进校数
    private Integer bdVisitTeacherCount;            // 专员见师量
    private Integer bdVisitChiTeacherCount;         // 专员语文老师见师量
    private Integer bdVisitMathTeacherCount;        // 专员数学老师见师量
    private Integer bdVisitEngTeacherCount;         // 专员英语老师见师量
    private Integer bdVisitOtherTeacherCount;       // 专员其他老师见师量

    private Integer userIntoSchoolNum;              // 人员进校数
    private Integer userVisitTeacherNum;            // 人员见师量
    private Integer userVisitChiTeacherNum;         // 人员见师量（语文）
    private Integer userVisitMathTeacherNum;        // 人员见师量（数学）
    private Integer userVisitEngTeacherNum;         // 人员见师量（英语）
    private Integer userVisitResearcherNum;         // 人员拜访教研员数量
    private Integer userMeetingNum;                 // 人员组会数
    private Integer userAccompanyVisitNum;          //人员陪访数


    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;


    public AgentWorkRecordStatistics(Integer date, Integer dateType, Integer groupOrUser, Long groupId, String groupName, Integer groupRoleId){
        this.date = date;
        this.dateType = dateType;
        this.groupOrUser = groupOrUser;
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupRoleId = groupRoleId;
        this.disabled = false;
    }

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"gid", "d","dt"}, new Object[]{groupId, date,dateType}),
                newCacheKey(new String[]{"uid", "d","dt"}, new Object[]{userId, date,dateType})
        };
    }
}
