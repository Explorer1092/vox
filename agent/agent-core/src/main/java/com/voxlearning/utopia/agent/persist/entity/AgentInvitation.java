package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.cache.UtopiaCacheRevision;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;


/**
 * 邀请函实体类
 *
 * @author deliang.che
 * @date 2018-04-13
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_INVITATION")
@UtopiaCacheExpiration
@UtopiaCacheRevision("20180413")
public class AgentInvitation extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 2043337761268228744L;

    @UtopiaSqlColumn String name;       //姓名
    @UtopiaSqlColumn String tel;        //电话
    @UtopiaSqlColumn String email;      //邮箱
    @UtopiaSqlColumn String company;    //邮箱
    @UtopiaSqlColumn String address;    //地址
    @UtopiaSqlColumn Boolean disabled;  //禁用
    @UtopiaSqlColumn String city;       //城市
    @UtopiaSqlColumn String position;   //职位
}
