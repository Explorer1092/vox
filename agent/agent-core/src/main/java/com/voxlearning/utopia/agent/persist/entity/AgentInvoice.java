package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.constants.AgentLogisticsStatus;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * AgentInvoice
 *
 * @author song.wang
 * @date 2016/9/6
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_INVOICE")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180223")
public class AgentInvoice extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 1471449179500730823L;

    @UtopiaSqlColumn String logisticsCompany;              // 物流信息
    @UtopiaSqlColumn String logisticsId;  // 物流单号
    @UtopiaSqlColumn Float logisticsPrice;  // 物流价格
    @UtopiaSqlColumn AgentLogisticsStatus logisticsStatus;  // 物流状态
    @UtopiaSqlColumn String userId;  //  用户ID，CRM里传过来的使用admin.前缀
    @UtopiaSqlColumn String userName;  // 用户名（下单人）
    @UtopiaSqlColumn String consignee;              // 收货人姓名
    @UtopiaSqlColumn String mobile;  // 收货人电话
    @UtopiaSqlColumn String province;              // 收货地址 - 城市
    @UtopiaSqlColumn String city;              // 收货地址 - 城市
    @UtopiaSqlColumn String county;              // 收货地址 - 城市

    @UtopiaSqlColumn Date deliveryDate; // 发货日期

    @UtopiaSqlColumn String address; // 收货地址
    @UtopiaSqlColumn Boolean disabled;  // 禁用

    public Boolean isDisabled() {
        return SafeConverter.toBoolean(disabled);
    }
}
