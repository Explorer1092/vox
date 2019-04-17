package com.voxlearning.enanalyze.view;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.ArrayUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.StringUtils;
import com.voxlearning.enanalyze.Session;
import com.voxlearning.utopia.enanalyze.model.Article;
import com.voxlearning.utopia.enanalyze.model.ArticleBasicAbility;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 作文批改结果
 * @author xiaolei.li
 * @version 2018/7/6
 */
@Data
public class ArticleView implements Serializable {

    /**
     * openid
     */
    private String openId;

    /**
     * 作文记录流水
     */
    private String articleId;

    /**
     * 作文
     */
    private String text;

    /**
     * 总体得分
     */
    private int score;

    /**
     * 超越百分比
     */
    private int beyondRate;

    /**
     * 总体评价
     */
    private String evaluation;

    /**
     * 次要评价
     */
    private String subEvaluation;

    /**
     * 语法检查部分
     */
    private SyntaxCheckDetail[] syntaxCheckDetails;

    /**
     * 好句
     */
    private String[] goodSents;

    /**
     * 好词
     */
    private String[] goodWords;

    /**
     * 词法得分
     */
    private int lexicalScore;

    /**
     * 句法得分
     */
    private int sentenceScore;

    /**
     * 内容得分
     */
    private int contentScore;

    /**
     * 结构得分
     */
    private int structureScore;

    /**
     * 构建器
     */
    public static class Builder {

        /**
         * Article => ArticleView
         *
         * @param data 领域模型
         * @return 视图模型
         */
        public static ArticleView build(Article data) {
            ArticleView view = new ArticleView();
            view.setArticleId(data.getArticleId());
            view.setOpenId(Session.getOpenId());
            ArticleBasicAbility basicAbility = data.getBasicAbility();
            if (null != basicAbility) {
                view.setScore(Float.valueOf(basicAbility.getScore()).intValue());
                view.setLexicalScore(Float.valueOf(basicAbility.getLexicalScore()).intValue());
                view.setSentenceScore(Float.valueOf(basicAbility.getSentenceScore()).intValue());
                view.setContentScore(Float.valueOf(basicAbility.getContentScore()).intValue());
                view.setStructureScore(Float.valueOf(basicAbility.getStructureScore()).intValue());
            }
            view.setArticleId(data.getArticleId());
            view.setGoodSents(data.getGoodSents());
            view.setGoodWords(data.getGoodWords());
            view.setBeyondRate(data.getBeyondRate());
            view.setEvaluation(data.getEvaluation());
            view.setSubEvaluation(data.getSubEvaluation());
            view.setText(data.getText());
            Article.SyntaxCheckDetail[] checkDetails = data.getSyntaxCheckDetails();
            if (StringUtils.isNotBlank(data.getText()) && null != checkDetails && ArrayUtils.isNotEmpty(checkDetails)) {
                StringBuffer text = new StringBuffer(data.getText());
                final String SEPARATOR = "%" + UUID.randomUUID().toString().replaceAll("-", "") + "%";
                AtomicInteger offset = new AtomicInteger();
                List<SyntaxCheckDetail> incorrectSentences = Arrays.stream(checkDetails)
                        .sorted(Comparator.comparing(Article.SyntaxCheckDetail::getStart).reversed())
                        .map(i -> {
                            SyntaxCheckDetail detail = new SyntaxCheckDetail();
                            detail.setStart(i.getStart());
                            detail.setText(i.getText());
                            detail.setTip(i.getDescription());
                            detail.setType(SyntaxCheckDetail.Type.INCORRECT);
                            // 错误数据进行替换
                            text.replace(i.getStart(), i.getEnd(), SEPARATOR);
                            offset.addAndGet(SEPARATOR.length() - i.getText().length());
                            return detail;
                        }).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                incorrectSentences.sort(Comparator.comparing(SyntaxCheckDetail::getStart));
                List<String> correctSentences = Arrays.asList(text.toString().split(SEPARATOR));
                List<SyntaxCheckDetail> sentences = Lists.newArrayList();
                for (int i = 0, j = 0, x = 0; x < correctSentences.size() + incorrectSentences.size(); x++) {
                    if (0 == x % 2) {
                        SyntaxCheckDetail detail = new SyntaxCheckDetail();
                        detail.setText(correctSentences.get(i++));
                        detail.setType(SyntaxCheckDetail.Type.CORRECT);
                        sentences.add(detail);
                    } else
                        sentences.add(incorrectSentences.get(j++));

                }
                view.setSyntaxCheckDetails(sentences.toArray(new SyntaxCheckDetail[]{}));
            } else {
                SyntaxCheckDetail detail = new SyntaxCheckDetail();
                detail.setText(data.getText());
                detail.setType(SyntaxCheckDetail.Type.CORRECT);
                view.setSyntaxCheckDetails(new SyntaxCheckDetail[]{detail});
            }
            return view;
        }
    }

    /**
     * 检查结果
     */
    @Data
    public static class SyntaxCheckDetail implements Serializable {

        /**
         * 文本
         */
        private String text;

        /**
         * 类型
         */
        private Type type;

        /**
         * 提示
         */
        private String tip;

        @JsonIgnore
        private Integer start;

        @AllArgsConstructor
        public enum Type {
            CORRECT("正确的"),
            INCORRECT("不正确的");
            public final String DESC;
        }
    }

}
