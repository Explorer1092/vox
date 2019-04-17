package com.voxlearning.utopia.admin.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * crm用户问题跟踪工单
 * Created by xiaozhi.qian on 2017/2/17.
 */
@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-crm")
@DocumentDatabase(database = "vox-crm")
@DocumentCollection(collection = "vox_fault_order")
public class CrmFaultOrder implements Serializable {


    private static final long serialVersionUID = -5795449141153838784L;
    @DocumentId
    private String id;
    private Long userId;          // 用户id
    private String userName;      // 用户姓名
    private String userType;      // 用户类型 参照@Link com.voxlearning.alps.annotation.meta.UserType
    private Integer faultType;      // 故障类型
    private Integer status;      // 工单状态
    private String creator;    // 创建者
    private String createInfo;     // 创建备注
    private String closer;           // 关闭者
    private String closeInfo;        //关闭备注
    private String ext;             //扩展数据 目前只有作业编号
    private Date closeTime; //关闭时间
    @DocumentCreateTimestamp private Date createTime;
    @DocumentUpdateTimestamp private Date updateTime;

}
