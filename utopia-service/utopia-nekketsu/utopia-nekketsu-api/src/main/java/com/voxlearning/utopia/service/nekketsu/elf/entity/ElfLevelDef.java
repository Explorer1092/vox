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

package com.voxlearning.utopia.service.nekketsu.elf.entity;

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by Sadi.Wan on 2015/2/10.
 */

/**
 * 每个level的配置 需要导表
 */
@DocumentConnection(configName = "mongo")
@DocumentDatabase(database = "vox-walker-elf-def")
@DocumentCollection(collection = "elf_level_def")
@Getter
@Setter
public class ElfLevelDef implements Serializable {
    private static final long serialVersionUID = -2234298601834183568L;

    @DocumentId
    /**
     * 格式:SAVE_PRINCE,SAVE_QUEEN,SAVE_KING
     */
    private String id;

    /**
     * 顺序
     */
    private Integer order;
    /**
     * 关卡植物谱
     * key:STAR_1,STAR_2,STAR_3,STAR_4,STAR_5
     * value:P00001,P00002
     */
    private Map<String, List<String>> plantStruct;
}
