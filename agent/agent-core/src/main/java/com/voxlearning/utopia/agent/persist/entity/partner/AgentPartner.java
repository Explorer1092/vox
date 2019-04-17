package com.voxlearning.utopia.agent.persist.entity.partner;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import lombok.Getter;
import lombok.Setter;

/**
 * @description: 合作伙伴持久层
 * @author: kaibo.he
 * @create: 2019-04-01 20:38
 **/
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_PARTNER")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20190401")
public class AgentPartner extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {

    private String name;                // 名称

    private Integer provinceCode;       // 省份CODE
    private String provinceName;        // 省份名称
    private Integer cityCode;           // 城市CODE
    private String cityName;            // 城市名称
    private Integer countyCode;         // 地区CODE
    private String countyName;          // 地区名称

    private String longitude;           // 地理坐标：经度
    private String latitude;            // 地理坐标：纬度
    private String address;             //地址
    private String type;//机构类别
    private String homePhotoUrl;     // 门头照片
    private String otherPhotoUrls; //其他图片,多张以 , 分割
    private Long createUserId;          //创建人
    private String createUserName;      //创建人姓名

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id),
                newCacheKey("name",this.name),
        };
    }
}
