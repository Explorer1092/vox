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

/**
 * Created by Sadi.Wan on 2015/2/10.
 */
@Getter
@Setter
@DocumentConnection(configName = "mongo")
@DocumentDatabase(database = "vox-walker-elf-def")
@DocumentCollection(collection = "elf_book_def")
public class ElfBookDef implements Serializable {
    private static final long serialVersionUID = 4150596208901466345L;

    @DocumentId
    /**
     * 格式：B4000 其中4000为VOX_READING的主键
     */
    private String bookId;

    private String bookName;

    /**
     * 读本时长 毫秒
     */
    private Long duration;

    /**
     * 关卡id
     */
    private String levelId;

    /**
     * 是所属关的第几本书
     */
    private Integer indexOfLevel;

    /**
     * 读完获得植物
     */
    private String plantId;

    /**
     * 封面相对路径
     */
    private String cover;

}
