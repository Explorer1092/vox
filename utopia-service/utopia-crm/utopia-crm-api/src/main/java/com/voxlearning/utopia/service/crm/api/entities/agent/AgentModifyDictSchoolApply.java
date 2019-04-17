package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.crm.api.constants.ApplyStatus;
import com.voxlearning.utopia.service.crm.api.constants.SystemPlatformType;
import com.voxlearning.utopia.service.crm.api.entities.AbstractBaseApply;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 字典表调整申请
 *
 * @author song.wang
 * @date 2016/12/28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_MODIFY_DICT_SCHOOL_APPLY")
@UtopiaCacheExpiration
public class AgentModifyDictSchoolApply extends AbstractBaseApply {

    private static final long serialVersionUID = 5053320311651272452L;

    @UtopiaSqlColumn private Integer modifyType;  // 1:添加学校  2：删除学校 3:业务变更
    @UtopiaSqlColumn private Long schoolId;  // 要添加和删除的学校ID
    @UtopiaSqlColumn private String schoolName; //学校名称
    @UtopiaSqlColumn private String regionName; // 学校所在的地区  北京市/东城区
    @UtopiaSqlColumn private Integer schoolLevel;

    /**
     * 字段已废弃，请勿使用
     */
    @Deprecated
    @UtopiaSqlColumn private Integer engMode; // 英语模式：0:模式为空 1:17zuoye模式  2：快乐学2+10模式  3:快乐学2+2模式
    /**
     * 字段已废弃，请勿使用
     */
    @Deprecated
    @UtopiaSqlColumn private Integer mathMode; // 数学模式：0:模式为空 1:17zuoye模式  2：快乐学2+10模式  3:快乐学2+2模式
    @UtopiaSqlColumn private String schoolPopularity; // 学校等级

    @UtopiaSqlColumn private String modifyDesc; // engMode, mathMode, schoolPopularity变更内容描述， 业务变更前后和字典表的变化对比


    @UtopiaSqlColumn private String comment;  // 调整原因

    @UtopiaSqlColumn private Boolean resolved; // 是否已更新到字典表

    @UtopiaSqlColumn private Long targetUserId; // 要分配给的专员ID

    public static String ck_id(Long schoolId) {
        return CacheKeyGenerator.generateCacheKey(AgentModifyDictSchoolApply.class, "sid", schoolId);
    }

    public static String ck_wid(Long workflowId) {
        return CacheKeyGenerator.generateCacheKey(AgentModifyDictSchoolApply.class, "wid", workflowId);
    }

    public static String ck_uid_status(String userId, ApplyStatus status) {
        return CacheKeyGenerator.generateCacheKey(AgentModifyDictSchoolApply.class,
                new String[]{"uid", "status"},
                new Object[]{userId, status});
    }

    public static String ck_platform_uid(SystemPlatformType userPlatform, String userAccount) {
        return CacheKeyGenerator.generateCacheKey(AgentModifyDictSchoolApply.class,
                new String[]{"platform", "uid"},
                new Object[]{userPlatform, userAccount});
    }

    @Override
    public String generateSummary() {
        String summary = "";
        if (modifyType == 1) {
            summary = "添加学校：";
        } else if (modifyType == 2) {
            summary = "删除学校：";
        } else if (modifyType == 3) {
            summary = "业务变更：";
        }

        String klxDesc = "";
        if (StringUtils.isNotBlank(modifyDesc) && modifyDesc.contains("快乐学")) {
            klxDesc = "（快乐学）";
        }
        SchoolLevel schoolLevel = SchoolLevel.safeParse(this.schoolLevel, null);
        return summary + schoolName + "[" + (schoolLevel == null ? "" : schoolLevel.getDescription()) + "]" + klxDesc;
    }

    @AllArgsConstructor
    public enum ModifyType {
        /**
         * 加入学校
         */
        ADD_SCHOOL(1),
        /**
         * 删除学校
         */
        DELETE_SCHOOL(2),
        /**
         * 变更学校等级
         */
        UPDATE_POPULARITY(3),
        /**
         * 变更负责人
         */
        UPDATE_RESPONSIBLE(4),
        ;
        @Getter
        private Integer type;
    }
}
