package com.voxlearning.utopia.enanalyze.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import lombok.Data;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * 接口 - 好句子点赞
 *
 * @author xiaolei.li
 * @version 2018/7/24
 */
@ServiceTimeout(timeout = 1000, unit = TimeUnit.SECONDS)
@ServiceVersion(version = "20180701")
public interface SentenceLikeService {

    /**
     * 点赞或者取消点赞
     *
     * @param params 参数
     * @return 结果
     */
    MapMessage like(Params params);

    /**
     * 清空 - 用于数据初始化
     *
     * @return
     */
    MapMessage purge(String openId);


    /**
     * 请求参数 - 点赞
     *
     * @author xiaolei.li
     * @version 2018/7/24
     */
    @Data
    class Params implements Serializable {

        /**
         * 群id
         */
        private String openGroupId;

        /**
         * 谁点的赞
         */
        private String fromOpenId;

        /**
         * 点给谁的赞
         */
        private String toOpenId;
    }

    /**
     * 结果 - 点赞
     *
     * @author xiaolei.li
     * @version 2018/7/24
     */
    @Data
    class Result implements Serializable {

        /**
         * 所属群id
         */
        private String openGroupId;

        /**
         * 谁点的赞
         */
        private String fromOpenId;

        /**
         * 给谁点赞
         */
        private String toOpenId;

        /**
         * 当前点赞状态,true:被赞状态;false:取消点赞状态
         */
        private boolean likeStatus;

        /**
         * 当前累计点赞次数
         */
        private long likes;
    }
}
