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

package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.base.BaseHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.shard.ShardHomeworkBook;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkBook;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class NewHomeworkBook extends BaseHomeworkBook implements Serializable {
    private static final long serialVersionUID = 7350018341732777223L;

    private String id;
    private Date createAt;
    private Date updateAt;

    public static NewHomeworkBook of(SubHomeworkBook subHomeworkBook) {
        if (subHomeworkBook == null) {
            return null;
        }
        NewHomeworkBook newHomeworkBook = new NewHomeworkBook();
        try {
            BeanUtils.copyProperties(newHomeworkBook, subHomeworkBook);
            return newHomeworkBook;
        } catch (Exception e) {
            return null;
        }
    }

    public static NewHomeworkBook of(ShardHomeworkBook shardHomeworkBook) {
        if (shardHomeworkBook == null) {
            return null;
        }
        NewHomeworkBook newHomeworkBook = new NewHomeworkBook();
        try {
            BeanUtils.copyProperties(newHomeworkBook, shardHomeworkBook);
            return newHomeworkBook;
        } catch (Exception e) {
            return null;
        }
    }
}
