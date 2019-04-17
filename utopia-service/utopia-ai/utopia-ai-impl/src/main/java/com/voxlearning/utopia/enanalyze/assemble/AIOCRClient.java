package com.voxlearning.utopia.enanalyze.assemble;

import lombok.Data;

import java.io.Serializable;

/**
 * ai组的字符识别客户端
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@FunctionalInterface
public interface AIOCRClient {

    Result ocr(Request request);

    @Data
    class Result implements Serializable {
        private String code;
        private String message;
        private String version;
        private String essay;

    }

    @Data
    class Request implements Serializable {

        /**
         * 文件二级制数据
         */
        private byte[] bytes;

    }
}
