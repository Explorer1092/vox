package com.voxlearning.utopia.service.crm.api.entities.crm;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.utopia.service.crm.api.bean.OperationObject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 产品反馈的操作记录
 * Created by yaguang.wang
 * on 2017/3/3.
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "crm_product_feedback_record")
public class CrmProductFeedbackRecord implements Serializable {
    private static final long serialVersionUID = 7630894020540033654L;
    @DocumentId
    private String id;
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

    private String operatorUsername;    // 操作人用户命
    private String operatorName;        // 操作人姓名
    private String operationContent;    // 操作内容
    private OperationObject operationObject;    // 操作对象
}
