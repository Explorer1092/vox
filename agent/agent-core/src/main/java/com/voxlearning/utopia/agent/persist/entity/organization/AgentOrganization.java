package com.voxlearning.utopia.agent.persist.entity.organization;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentOrganizationType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRegionRank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 *  机构实体
 * @author deliang.che
 * @since  2018/12/10
 */

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_ORGANIZATION")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20190115")
public class AgentOrganization extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {

    private String name;                // 名称

    private AgentOrganizationType type;//类别

    private AgentRegionRank regionRank; // 区域级别

    private Integer provinceCode;       // 省份CODE
    private String provinceName;        // 省份名称
    private Integer cityCode;           // 城市CODE
    private String cityName;            // 城市名称
    private Integer countyCode;         // 地区CODE
    private String countyName;          // 地区名称

    private String longitude;           // 地理坐标：经度
    private String latitude;            // 地理坐标：纬度
    private String coordinateType;      // GPS类型
    private String address;             // 办公地址

    private String webAddress;          //官网地址
    private String photoUrl;     // 照片
    private Long schoolId;          //学校id
    private Integer orgType;        //1机构  2  学校  1 时 organizationId 2 时 schoolId有值



    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("sid",this.schoolId),
        };
    }
}

