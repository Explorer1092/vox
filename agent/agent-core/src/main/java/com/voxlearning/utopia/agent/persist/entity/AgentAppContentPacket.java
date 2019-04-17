/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentCreateTimestamp;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentUpdateTimestamp;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.spi.common.TimestampTouchable;
import com.voxlearning.utopia.agent.bean.CityInfo;
import com.voxlearning.utopia.agent.constants.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * 资料包，推荐书籍，市场最新活动，平台更新日志
 * Created by yaguang.wang on 2016/8/2.
 */

@Getter
@Setter
@NoArgsConstructor
@DocumentConnection(configName = "mongo-agent")
@DocumentDatabase(database = "vox-agent")
@DocumentCollection(collection = "vox_data_packet")
public class AgentAppContentPacket implements Serializable, TimestampTouchable {

    private static final long serialVersionUID = -8439697522702011471L;

    @DocumentId private String id;
    private Boolean disabled;                                   //是否禁用
    @DocumentCreateTimestamp private Date createTime;           //创建时间
    @DocumentUpdateTimestamp private Date updateTime;           //更新时间
    private AgentAppContentType contentType;                    //内容的具体类型
    //--------------------------- 平台日志--------------------------------
    private List<AgentUsedProductType> referProduct;            //设计产品
    private Set<Long> authorityUser;                            //有权查看的人
    //--------------------------- 平台日志 END----------------------------

    //---------------------------  活动 -----------------------------------
    private String activityName;                                //活动名称
    private Date activityStartDate;                             //活动开始时间
    private Date activityEndDate;                               //活动结束时间
    private List<AgentUsedProductType> activityEntrance;        //活动入口
    private List<SchoolLevel> activityScope;                    //活动范围
    private List<CityInfo> activityCity;                        //活动城市
    //private String thumbnailsUrl                              //活动缩略图
    //---------------------------  活动 END--------------------------------

    //---------------------------- 推荐书籍 -------------------------------
    private AgentRecommendBookRoleType role;                    //推荐对象角色
    private String bookName;                                    //书籍名称
    private String bookCoverUrl;                                //图书封面地址
    //---------------------------- 推荐书籍 END-------------------------------

    //---------------------------- 资料包属性--------------------------------
    private AgentDataPacketType datumType;                      //资料包类型
    private Set<AgentDataPacketRole>  applyRole;                //资料包适用角色
    //---------------------------- 资料包属性 END----------------------------

    //-----------------------------共有属性 ----------------------------------
    private String fileName;                                    //所有需要上传
    private String fileUrl;                                     //文件名称 上传的PPT
    private String contentTitle;                                //内容Title
    private String content;                                     //富文本内容
    private AppContentStateType state;                          //内容的状态

    public static String ck_content_type(AgentAppContentType contentType) {
        return CacheKeyGenerator.generateCacheKey(AgentAppContentPacket.class, "type", contentType);
    }

    public static String ck_data_packet_type(AgentDataPacketType datumType) {
        return CacheKeyGenerator.generateCacheKey(AgentAppContentPacket.class, "d_type", datumType);
    }
}
