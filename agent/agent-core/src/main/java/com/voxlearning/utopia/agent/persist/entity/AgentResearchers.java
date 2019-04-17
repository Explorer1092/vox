package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.*;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 教研员
 * Created by yaguang.wang on 2016/10/19.
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_RESEARCHERS")
public class AgentResearchers implements Serializable {

    private static final long serialVersionUID = -1724788341398357093L;
    @DocumentId(autoGenerator = DocumentIdAutoGenerator.AUTO_INC)
    @DocumentField("ID") private Long id;
    @DocumentCreateTimestamp
    @DocumentField("CREATE_DATETIME") Date createDatetime;
    @DocumentUpdateTimestamp
    @DocumentField("UPDATE_DATETIME") Date updateDatetime;
    @DocumentField private String name;             // 教研员姓名
    @DocumentField private Integer gender;          // 性别
    @DocumentField private String phone;            // 电话
    @DocumentField private Integer job;             // 职务
    @DocumentField private Integer level;           // 级别
    @DocumentField private Integer province;        // 所在省
    @DocumentField private Integer city;            // 所在市
    @DocumentField private Integer county;          // 所在地区
    @DocumentField private Integer schoolPhase;     // 学校阶段  1 小学 2 初中 3 高中 4 小学 加 初中 5 初中 加 高中 6 小学 加 初中 加 高中 7 小学 加 高中
    @DocumentField private String grade;           // 年级
    @DocumentField private String gradeStr;           // 年级新
    @DocumentField("SUBJECT") private Subject subject; // 学科
    @DocumentField private Long agentUserId;       // 所属人员ID
    @DocumentField private Boolean disabled;       // 所属人员ID
    @DocumentField private String specificJob;     //具体工作 （备注）
//    @DocumentField private String schoolPhaseStr;     // 学校阶段

//    @DocumentField private String organizationId;    //工作单位id AgentOrganization
//    @DocumentField private String telephone;         // 座机
//    @DocumentField private String department;         // 部门

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AgentResearchers.class, id);
    }

    public static String ck_u_id(Long userId) {
        return CacheKeyGenerator.generateCacheKey(AgentResearchers.class, "UID", userId);
    }
    public static String ck_phone(String phone) {
        return CacheKeyGenerator.generateCacheKey(AgentResearchers.class, "PHONE", phone);
    }
    public static String ck_province(Integer province) {
        return CacheKeyGenerator.generateCacheKey(AgentResearchers.class, "PROVINCE",province);
    }
    public static String ck_name(String name) {
        return CacheKeyGenerator.generateCacheKey(AgentResearchers.class, "NAME",name);
    }
//    public static String ck_organization(String organizationId) {
//        return CacheKeyGenerator.generateCacheKey(AgentResearchers.class, "ORGANIZATION_ID",organizationId);
//    }
}
