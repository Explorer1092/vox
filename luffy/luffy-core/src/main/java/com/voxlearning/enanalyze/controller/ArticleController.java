package com.voxlearning.enanalyze.controller;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.enanalyze.Session;
import com.voxlearning.enanalyze.ViewBuilder;
import com.voxlearning.enanalyze.ViewCode;
import com.voxlearning.enanalyze.aggregate.ArticleAggregator;
import com.voxlearning.enanalyze.exception.BusinessException;
import com.voxlearning.enanalyze.view.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * 英文作业服务
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Controller
@RequestMapping(value = "/enanalyze/article")
public class ArticleController {

    @Resource
    private ArticleAggregator articleAggregator;


    /**
     * 客户端上传图片提交字符识别
     *
     * @param file
     * @return
     */
    @RequestMapping(value = "ocr.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ocr(@RequestParam("file") MultipartFile file) {
        MapMessage message;
        try {
            ArticleOCRView ocrResponse = articleAggregator.ocr(file.getBytes());
            message = ViewBuilder.success(ocrResponse);
        } catch (BusinessException e) {
            message = ViewBuilder.error(e);
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }

    /**
     * 英语作文分析
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "nlp.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage nlp(@RequestBody ArticleNLPRequest request) {
        MapMessage message;
        try {
            ArticleView response = articleAggregator.nlp(request);
            message = ViewBuilder.success(response);
        } catch (BusinessException e) {
            message = ViewBuilder.error(e);
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }

    /**
     * 查询批改记录
     *
     * @param articleId 作文批改记录id
     * @return
     */
    @RequestMapping(value = "detail.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage retrieve(@RequestParam(name = "articleId") String articleId) {
        MapMessage message;
        try {
            ArticleView view = articleAggregator.retrieve(articleId);
            message = ViewBuilder.success(view);
        } catch (BusinessException e) {
            message = ViewBuilder.error(e);
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }

    /**
     * 分页查询批改记录
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "load.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage page(@RequestBody ArticlePageRequest request) {
        MapMessage message;
        try {
            ArticlePageView response = articleAggregator.page(request.getPage(), request.getSize());
            message = ViewBuilder.success(response);
        } catch (BusinessException e) {
            message = ViewBuilder.error(e);
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }


    /**
     * 查询批改记录
     *
     * @param articleId 作文批改记录id
     * @return
     */
    @RequestMapping(value = "remove.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage delete(@RequestParam(name = "articleId") String articleId) {
        MapMessage message;
        try {
            articleAggregator.delete(articleId);
            message = ViewBuilder.success(null);
        } catch (BusinessException e) {
            message = ViewBuilder.error(e, String.format(("删除记录时发生错误"), articleId));
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }


    /**
     * 查看学情报告
     *
     * @return
     */
    @RequestMapping(value = "report.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage report() {
        MapMessage message;
        final String openId = Session.getOpenId();
        try {
            ArticleReportView view = articleAggregator.report(openId);
            message = ViewBuilder.success(view);
        } catch (BusinessException e) {
            message = ViewBuilder.error(e, String.format(("获取学情报告时发生错误,[openid=%s]"), openId));
        } catch (Exception e) {
            message = ViewBuilder.error(ViewCode.BIZ_ERROR.CODE, "服务器打盹了，请重试");
        }
        return message;
    }
}
