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

import com.voxlearning.alps.annotation.common.DateRangeType;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentId;
import com.voxlearning.alps.annotation.dao.DocumentRangeable;
import com.voxlearning.alps.annotation.dao.mongo.DocumentCollection;
import com.voxlearning.alps.annotation.dao.mongo.DocumentDatabase;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Sadi.Wan on 2015/4/7.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@DocumentConnection(configName = "mongo-journal")
@DocumentDatabase(database = "vox-elf-play-log-{}", dynamic = true)
@DocumentCollection(collection = "vox_elf_play_log")
@DocumentRangeable(range = DateRangeType.M)
public class ElfPlayLog implements Serializable {
    private static final long serialVersionUID = -667961796898154147L;

    @DocumentId
    private String id;
    private Long uid;
    private Date logTime;
    private ElfLogEnum logEnum;
    private String logContent;
}
