/**
 * Author:   xianlong.zhang
 * Date:     2018/10/17 15:50
 * Description: 客服工单操作记录
 * History:
 */
package com.voxlearning.utopia.agent.persist.entity.worksheel;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_work_sheet_log")
public class WorkSheetLog implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private Long sheetId;//工单id

    private Integer event; //工单类型

    private Map<String,Object> operator; //事件操作者

    private String remark;//备注

    private List<String> attachList;//附件

    private Date time ;//事件发生时间

    private Map<String,Object> toStaff;

    private Long customId;// 如果修改的是自定义字段那么这个字段出现

    private Object pre;// 修改前的值

    private Object after;// 修改后的值
    private Map<String,Object> toGroup; //

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;

    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("sheetId", this.sheetId)
        };
    }
}
