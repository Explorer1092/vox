package com.voxlearning.enanalyze.aggregate;

import com.voxlearning.enanalyze.view.*;

/**
 * 作业聚合服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
public interface ArticleAggregator {

    /**
     * 图片提交ocr
     *
     * @param bytes 图片二进制数组
     * @return 识别结果
     */
    ArticleOCRView ocr(byte[] bytes);

    /**
     * 作文批改
     *
     * @param request 请求
     * @return 响应
     */
    ArticleView nlp(ArticleNLPRequest request);

    /**
     * 分页查询批改记录
     *
     * @param page 第几页
     * @param size 每页大小
     * @return 分页数据
     */
    ArticlePageView page(int page, int size);

    /**
     * 查询批改记录
     *
     * @param articleId 作文id
     * @return 批改记录
     */
    ArticleView retrieve(String articleId);

    /**
     * 删除批改记录
     *
     * @param articleId 作文id
     */
    void delete(String articleId);

    /**
     * 获取学情报告
     *
     * @param openId openid
     * @return 学情报告
     */
    ArticleReportView report(String openId);
}
