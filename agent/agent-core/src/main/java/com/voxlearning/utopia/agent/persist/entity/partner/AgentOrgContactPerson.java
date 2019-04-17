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
 *  异业机构联系人
 *
 * @author deliang.che
 * @since  2019/4/11
 */

@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_ORG_CONTACT_PERSON")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20190411")
public class AgentOrgContactPerson extends AbstractDatabaseEntityWithDisabledField implements CacheDimensionDocument {

    private static final long serialVersionUID = 9017237720193928864L;
    private Long honeycombId;   //蜂巢ID（异业机构关联粉丝ID）

    private String name;        //姓名
    private Integer gender;     //性别 1：男 2：女

    private String phoneNumber; //手机号码
    private String position;    //职务

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey(this.id),
                newCacheKey("hid",this.honeycombId),
        };
    }
}

