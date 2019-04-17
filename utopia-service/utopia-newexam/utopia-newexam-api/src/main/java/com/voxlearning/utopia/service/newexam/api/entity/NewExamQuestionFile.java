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

package com.voxlearning.utopia.service.newexam.api.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * Created by tanguohong on 2016/3/7.
 */
@Getter
@Setter
@ToString
public class NewExamQuestionFile implements Serializable {

    private static final long serialVersionUID = 5102254802935688907L;

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
        ALIYUN, UNKNOWN;

        /**
         * 根据完整url判断文件类型
         *
         * @param url 完整的url，例：http://image.oss.17zuoye.com/2016/01/16/20160116155033512486.jpg
         * @return 枚举
         */
        public static StorageSource loadSource(String url) {
            url = (url == null ? "" : url).trim().toLowerCase();
            if (url.contains("oss.17zuoye.com")) {
                return ALIYUN;
            }
            return UNKNOWN;
        }
    }

    private String fileName;                        // 文件名，当主键用，不带后缀
    private StorageSource source;                          // 存储来源 aliyun oss
    private FileType fileType;                        // 文件类型
    private String relativeUrl;                     // 相对路径 2016/01/16/20160116155033512486.jpg

}
