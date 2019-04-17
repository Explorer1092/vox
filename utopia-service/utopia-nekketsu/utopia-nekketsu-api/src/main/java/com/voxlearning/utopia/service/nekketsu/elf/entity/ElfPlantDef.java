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
import java.util.Map;

/**
 * Created by Sadi.Wan on 2015/2/10.
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo")
@DocumentDatabase(database = "vox-walker-elf-def")
@DocumentCollection(collection = "elf_plant_def")
public class ElfPlantDef implements Serializable {
    private static final long serialVersionUID = -5362514323270794588L;

    @DocumentId
    /**
     * 由产品定义。初步规定格式为:P00001
     */
    private String plantId;

    private String name;

    /**
     * 由何种植物何种数量组成 key:植物id，value:数量
     */
    private Map<String, Integer> madeOf;

    /**
     * 星级(1-5)
     */
    private Integer star;
}
