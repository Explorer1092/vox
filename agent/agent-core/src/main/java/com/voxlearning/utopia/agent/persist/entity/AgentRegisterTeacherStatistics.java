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
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndex;
import com.voxlearning.alps.annotation.dao.mongo.DocumentIndexes;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Map;

/**
 *AgentRegisterTeacherStatistics
 *
 * Author:   xianlong.zhang
 * Date:     2018/11/23 18:34
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_register_teacher_statistics")
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
@UtopiaCacheRevision("20181123")
@DocumentIndexes({
        @DocumentIndex(def = "{'groupId':1,'date':1,'dateType':1,'disabled':1}", background = true),
        @DocumentIndex(def = "{'userId':1,'date':1,'dateType':1,'groupOrUser':1,'disabled':1}", background = true)
})
public class AgentRegisterTeacherStatistics implements CacheDimensionDocument {

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

    private Long userId;
    private String userName;                             // 用户名
    private Integer userRoleId;                      // 用户角色

    //团队注册人均值
    private Double perPersonRegisterTeacherCount;        //人均注册（部门）
    private Double perPersonRegisterMathTeacherCount;  //人均注册-数学（部门）
    private Double perPersonRegisterEngTeacherCount;   //人均注册-英语（部门）
    private Double perPersonRegisterChnTeacherCount;   //人均注册-语文（部门）
    private Double perPersonRegisterOtherTeacherCount; //人均注册-其他（部门）

    //团队注册总数
    private Integer groupRegisterTeacherCount;            // 部门注册老师
    private Integer groupRegisterMathTeacherCount;        // 部门数学老师注册
    private Integer groupRegisterEngTeacherCount;         // 部门英语老师注册
    private Integer groupRegisterChnTeacherCount;         // 部门语文老师注册
    private Integer groupRegisterOtherTeacherCount;       // 部门其他老师注册

    //专员注册数据
    private Integer userRegisterTeacherCount;            // 专员注册老师
    private Integer userRegisterMathTeacherCount;        // 专员数学老师注册
    private Integer userRegisterEngTeacherCount;         // 专员英语老师注册
    private Integer userRegisterChnTeacherCount;         // 专员语文老师注册
    private Integer userRegisterOtherTeacherCount;       // 专员其他老师注册


    private Boolean disabled;

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(new String[]{"gid", "d","dt"}, new Object[]{groupId, date,dateType}),
                newCacheKey(new String[]{"uid", "d","dt"}, new Object[]{userId, date,dateType}),
                newCacheKey(new String[]{"gid", "d","dt","gu"}, new Object[]{groupId, date,dateType,groupOrUser}),

        };
    }
    public AgentRegisterTeacherStatistics(Integer date, Integer dateType, Integer groupOrUser, Long groupId, String groupName, Integer groupRoleId,Long userId,String userName,Integer userRoleId){
        this.date = date;
        this.dateType = dateType;
        this.groupOrUser = groupOrUser;
        this.groupId = groupId;
        this.groupName = groupName;
        this.groupRoleId = groupRoleId;
        this.disabled = false;
        this.userId = userId;
        this.userName = userName;
        this.userRoleId = userRoleId;
    }
}
