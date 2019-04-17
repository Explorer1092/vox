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

package com.voxlearning.utopia.service.newhomework.api.entity.base;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Setter
@ToString
public class NewHomeworkQuestionFile implements Serializable {

    private static final long serialVersionUID = 5743901570792172605L;

    public enum FileType {
        IMAGE, AUDIO, UNKNOWN;

        public static FileType of(String name) {
            try {
                return FileType.valueOf(name);
            } catch (Exception ex) {
                return UNKNOWN;
            }
        }
    }

    public enum StorageSource {
        ALIYUN, UNKNOWN
    }

    private String fileName;                        // 文件名，当主键用，不带后缀
    private StorageSource source;                          // 存储来源 aliyun oss
    private FileType fileType;                        // 文件类型
    private String relativeUrl;                     // 相对路径 2016/01/16/20160116155033512486.jpg

}
