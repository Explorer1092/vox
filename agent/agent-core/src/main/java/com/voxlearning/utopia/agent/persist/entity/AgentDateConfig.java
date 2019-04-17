package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import com.voxlearning.utopia.agent.constants.AgentDateConfigType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * 日期控制
 * Created by yaguang.wang
 * on 2017/3/27.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "vox_date_config")
public class AgentDateConfig implements Serializable, TimestampTouchable {

    private static final long serialVersionUID = 967212868306785789L;

    @DocumentId private String id;
    private Boolean disabled;                                   //是否禁用
    @DocumentCreateTimestamp private Date createTime;           //创建时间
    @DocumentUpdateTimestamp private Date updateTime;           //更新时间

    private AgentDateConfigType configType;

    private Integer startDay;                                   // 开始时间
    private Integer endDay;                                     // 结束时间

    // 检测当前日期是不是在市经理配置的时间范围之内
    public boolean checkCityManagerConfigSchool(Date day) {
        Calendar checkDay = Calendar.getInstance();
        checkDay.setTime(day);
        Integer dayOfMonth = checkDay.get(Calendar.DAY_OF_MONTH);
        return configType == AgentDateConfigType.CITY_MANAGER_CONFIG_SCHOOL && startDay != null && endDay != null && dayOfMonth >= startDay && dayOfMonth <= endDay;
    }
}
