/**
 * Author:   xianlong.zhang
 * Date:     2018/12/13 16:30
 * Description: 上层资源扩展属性
 * History:
 */
package com.voxlearning.utopia.agent.persist.entity.organization;


import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_outer_resource_extend")
public class AgentOuterResourceExtend implements CacheDimensionDocument {
    @DocumentId
    private String id;
    private Long resourceId;  //外部资源 AgentOuterResource id

//    private String specificJob;
    private Long organizationId;    //工作单位id AgentOrganization
    private String department;         // 部门/主管业务
    private String gradeStr;           // 年级 json串
    private String remarks;       //备注  旧表的specificJob 也放到这
    private Date visitTime;       //最近拜访时间每次拜访后更新
    private Subject subject;      // 学科（教研员才有学科）
    private Integer job;          //职位

    private String weChatOrQq;  //微信/QQ
    private String email;       //邮箱
    private List<String> photoUrls;//名片/照片

    private Boolean registrationStatus;   //注册状态  只有job是非注册老师时有
    private Boolean disabled;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("ORGANIZATION_ID",this.organizationId),
                newCacheKey("resourceId",this.resourceId)
//                newCacheKey("schoolId",this.schoolId)
        };
    }
}
