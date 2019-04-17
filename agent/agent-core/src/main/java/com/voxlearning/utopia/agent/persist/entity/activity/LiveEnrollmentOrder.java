package com.voxlearning.utopia.agent.persist.entity.activity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.spi.dao.CacheDimensionDocument;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "agent_live_enrollment_order")
public class LiveEnrollmentOrder implements CacheDimensionDocument {

    @DocumentId
    private String id;
    private String deliveryId;  //投放id
    private String orderId;     // 订单ID
    private Date payTime;         // 支付时间
    private Long parentId;        // 家长ID
    private Long studentId;       // 学生ID
    private Long schoolId;        // 学校ID
    private String schoolName;    // 学校名称
    private Integer courseType;   //课程类型（1: 低价课 2: 正价课）
    private Long payPrice;        //支付金额（单位分）

    private List<Integer> courseGrades;   //  1-9 分别表示 小学1-6年级及 初中 1-3 年级
    private String courseSubject;        // 英语、语文、数学、物理、化学、生物
    private String courseName;           // 课程名称
    private String courseStage;          // 小学、初中、高中

    @DocumentCreateTimestamp
    private Date createTime;
    @DocumentUpdateTimestamp
    private Date updateTime;


    @Override
    public String[] generateCacheDimensions() {
        return new String[0];
    }
}
