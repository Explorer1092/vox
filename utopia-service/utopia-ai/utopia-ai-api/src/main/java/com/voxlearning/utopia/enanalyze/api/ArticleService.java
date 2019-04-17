package com.voxlearning.utopia.enanalyze.api;

import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.enanalyze.model.ArticlePageParams;
import com.voxlearning.utopia.enanalyze.model.ArticleNLPParams;

import java.util.concurrent.TimeUnit;

/**
 * 作业服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@ServiceTimeout(timeout = 1000, unit = TimeUnit.SECONDS)
@ServiceVersion(version = "20180701")
public interface ArticleService {

    /**
     * 字符识别
     *
     * @param openId openid
     * @param bytes  图像二级制流
     * @return
     */
    MapMessage ocr(String openId, byte[] bytes);

    /**
     * 作文批改
     *
     * @param input 输入
     * @return
     */
    MapMessage nlp(ArticleNLPParams input);

    /**
     * 分页查询批改记录
     *
     * @param input
     * @return
     */
    MapMessage queryPage(ArticlePageParams input);

    /**
     * 获取批改记录
     *
     * @param articleId 批改记录id
     * @return 批改记录
     */
    MapMessage retrieve(String articleId);

    /**
     * 删除批改记录
     *
     * @param articleId 批改记录id
     */
    MapMessage delete(String articleId);

    /**
     * 查询学情报告
     *
     * @param openId openid
     * @return 学情报告
     */
    MapMessage report(String openId);
}
