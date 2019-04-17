/**
 * Author:   xianlong.zhang
 * Date:     2018/10/17 14:35
 * Description: 客服工单  创建工单时推送的对象
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
@DocumentCollection(collection = "agent_work_sheet")
public class WorkSheet implements CacheDimensionDocument {

    @DocumentId
    private String id;

    private Long sheetId; //工单id

    private Integer event; // 事件类型id

    private String title; //工单标题
    
    private  String content;//工单内容

    private List<String> attachList; //附件地址列表

    private Map<String,Object> creator;//创建的客服

    private Integer status;//工单状态

    private Integer priority;//优先级

    private  Map<String,Object> toStaff;//被分配的客服

    private Map<String,Object> category;//工单分类

    private Map<String,Object> template;//工单模板

    private Date time; //事件发生时间

    private Map<String,Object> user;//user对象

    private List<Map<String,Object>> customFiledList;

    private Boolean marketingPerson;
    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;


    @Override
    public String[] generateCacheDimensions() {
        return new String[]{
                newCacheKey("sheetId",this.sheetId)
        };
    }
}
