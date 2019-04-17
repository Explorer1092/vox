package com.voxlearning.enanalyze.aggregate.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.enanalyze.MessageFetcher;
import com.voxlearning.enanalyze.Session;
import com.voxlearning.enanalyze.ViewCode;
import com.voxlearning.enanalyze.aggregate.ArticleAggregator;
import com.voxlearning.enanalyze.exception.BusinessException;
import com.voxlearning.enanalyze.view.*;
import com.voxlearning.utopia.enanalyze.api.ArticleRankService;
import com.voxlearning.utopia.enanalyze.api.ArticleService;
import com.voxlearning.utopia.enanalyze.api.FileRecordService;
import com.voxlearning.utopia.enanalyze.api.UserService;
import com.voxlearning.utopia.enanalyze.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 作业聚合服务实现
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Service
public class ArticleAggregatorImpl implements ArticleAggregator {

    @ImportService(interfaceClass = FileRecordService.class)
    @ServiceVersion(version = "20180701")
    private FileRecordService fileService;

    @ImportService(interfaceClass = ArticleService.class)
    @ServiceVersion(version = "20180701")
    private ArticleService articleService;

    @ImportService(interfaceClass = UserService.class)
    @ServiceVersion(version = "20180701")
    private UserService userService;

    @ImportService(interfaceClass = ArticleRankService.class)
    @ServiceVersion(version = "20180701")
    private ArticleRankService articleRankService;

    @Override
    public ArticleOCRView ocr(byte[] bytes) {
        if (bytes.length > 8 * 1024 * 1024 * 3)
            throw new BusinessException(ViewCode.AI_ERROR, "图片大小超过3M了，请减小图片大小");
        ArticleOCRView view = new ArticleOCRView();
        MapMessage ocrMsg = articleService.ocr(Session.getOpenId(), bytes);
        ArticleOCRResult ocrResult = MessageFetcher.get(ocrMsg, ArticleOCRResult.class);
        view.setImageId(ocrResult.getImageId());
        view.setText(ocrResult.getText());
        return view;
    }

    @Override
    public ArticleView nlp(ArticleNLPRequest request) {
        final String text = request.getText();
        if (StringUtils.isBlank(text)) {
            throw new BusinessException(ViewCode.AI_ERROR, "这篇文章字数太少啦，再多写点吧！");
        } else {
            String[] tokens = text.split(" ");
            if (tokens.length < 20)
                throw new BusinessException(ViewCode.AI_ERROR, "这篇文章字数太少啦，再多写点吧！");
            if (tokens.length > 300)
                throw new BusinessException(ViewCode.AI_ERROR, "这篇文章字数太多啦，请少写点吧！");
        }
        ArticleNLPParams params = new ArticleNLPParams();
        params.setOpenId(Session.getOpenId());
        params.setArticleId(request.getArticleId());
        params.setText(text);
        MapMessage nlpResult = articleService.nlp(params);
        Article article = MessageFetcher.get(nlpResult, Article.class);
        return ArticleView.Builder.build(article);
    }

    @Override
    public ArticleView retrieve(String articleId) {
        MapMessage articleMsg = articleService.retrieve(articleId);
        Article article = MessageFetcher.get(articleMsg, Article.class);
        return ArticleView.Builder.build(article);
    }

    @Override
    public ArticlePageView page(int page, int size) {
        ArticlePageParams input = new ArticlePageParams();
        input.setPage(page);
        input.setSize(size);
        input.setOpenId(Session.getOpenId());
        MapMessage pageResult = articleService.queryPage(input);
        ArrayList<ArticleGeneralInfo> arrayList = MessageFetcher.get(pageResult, ArrayList.class);
        ArticlePageView view = new ArticlePageView();
        view.setPage(page);
        view.setSize(size);
        view.setData(
                arrayList.stream()
                        .map(ArticlePageView.Record.Builder::build)
                        .collect(ArrayList::new, List::add, List::addAll));
        return view;
    }

    @Override
    public void delete(String articleId) {
        articleService.delete(articleId);
    }

    @Override
    public ArticleReportView report(String openId) {
        MapMessage message = articleService.report(openId);
        ArticleReport report = MessageFetcher.get(message, ArticleReport.class);
        return ArticleReportView.Builder.build(report);
    }
}
