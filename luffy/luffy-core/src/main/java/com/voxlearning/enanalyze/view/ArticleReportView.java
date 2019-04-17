package com.voxlearning.enanalyze.view;

import com.voxlearning.utopia.enanalyze.model.ArticleCompositeAbility;
import com.voxlearning.utopia.enanalyze.model.ArticleReport;
import lombok.Data;

import java.io.Serializable;
import java.util.*;

/**
 * 学情报告视图
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Data
public class ArticleReportView implements Serializable {

    /**
     * openid
     */
    private String openId;

    /**
     * 昵称
     */
    private String nickName;

    /**
     * tag
     */
    private String tag;

    /**
     * 超越了多少人（百分比 [0, 100]
     */
    private int beyondRate;

    /**
     * 总体评价
     */
    private String evaluation;

    /**
     * 能力模型
     */
    private Ability ability;

    /**
     * 能力评价
     */
    private String abilityEvaluation;

    /**
     * 批改记录
     */
    private Record[] records;

    /**
     * 批改记录（面向于图表）
     */
    private _Record _records;

    /**
     * 历史成绩变化评价
     */
    private String recordEvaluation;


    /**
     * 能力模型
     */
    @Data
    public static class Ability implements Serializable {

        /**
         * 综合得分
         */
        private int score;

        /**
         * 语法知识评分
         */
        private int grammarScore;

        /**
         * 篇章知识评分
         */
        private int chapterScore;

        /**
         * 语言表达评分
         */
        private int languageScore;

        /**
         * 语用表达评分
         */
        private int pragmaticScore;

        /**
         * 写作策略评分
         */
        private int writingScore;
    }

    /**
     * 面向于前端视图的历史记录
     */
    @Data
    public static class _Record implements Serializable {
        private Integer[] lexicalScore;
        private Integer[] sentenceScore;
        private Integer[] contentScore;
        private Integer[] structureScore;
    }

    /**
     * 批改记录
     */
    @Data
    public static class Record implements Serializable {

        /**
         * 创建时间
         */
        private Date createDate;

        /**
         * 词法评分
         */
        private int lexicalScore;

        /**
         * 句法评分
         */
        private int sentenceScore;

        /**
         * 内容评分
         */
        private int contentScore;

        /**
         * 结构评分
         */
        private int structureScore;
    }

    public static class Builder {

        /**
         * ArticleReport => ArticleReportView
         *
         * @param data 领域模型
         * @return 视图模型
         */
        public static ArticleReportView build(ArticleReport data) {
            ArticleReportView view = new ArticleReportView();
            view.setOpenId(data.getOpenId());
            view.setNickName(data.getNickName());
            view.setTag(data.getTag());
            view.setBeyondRate(data.getBeyondRate());
            // 能力模型
            Ability a = new Ability();
            ArticleCompositeAbility _a = data.getAbility();
            a.setScore(Float.valueOf(_a.getScore()).intValue());
            a.setChapterScore(Float.valueOf(_a.getChapterScore()).intValue());
            a.setGrammarScore(Float.valueOf(_a.getGrammarScore()).intValue());
            a.setLanguageScore(Float.valueOf(_a.getLanguageScore()).intValue());
            a.setPragmaticScore(Float.valueOf(_a.getPragmaticScore()).intValue());
            a.setWritingScore(Float.valueOf(_a.getWritingScore()).intValue());
            view.setAbility(a);
            view.setAbilityEvaluation(data.getAbilityEvaluation());
            // 历史成绩
            view.setRecords(Arrays.stream(data.getRecords())
                    // 按照创建时间升序排列
                    .sorted(Comparator.comparing(ArticleReport.Record::getCreateDate).reversed())
                    // Record适配
                    .map(i -> {
                        Record record = new Record();
                        record.setCreateDate(i.getCreateDate());
                        record.setContentScore(Float.valueOf(i.getContentScore()).intValue());
                        record.setLexicalScore(Float.valueOf(i.getLexicalScore()).intValue());
                        record.setSentenceScore(Float.valueOf(i.getSentenceScore()).intValue());
                        record.setStructureScore(Float.valueOf(i.getStructureScore()).intValue());
                        return record;
                    }).toArray(Record[]::new));
            _Record _record = new _Record();
            List<Integer> lexicalScores = new ArrayList();
            List<Integer> sentenceScores = new ArrayList();
            List<Integer> contentScores = new ArrayList();
            List<Integer> structureScores = new ArrayList();
            for (ArticleReport.Record i : data.getRecords()) {
                lexicalScores.add(Float.valueOf(i.getLexicalScore()).intValue());
                sentenceScores.add(Float.valueOf(i.getSentenceScore()).intValue());
                contentScores.add(Float.valueOf(i.getContentScore()).intValue());
                structureScores.add(Float.valueOf(i.getStructureScore()).intValue());
            }
            _record.setLexicalScore(lexicalScores.toArray(new Integer[]{}));
            _record.setSentenceScore(sentenceScores.toArray(new Integer[]{}));
            _record.setContentScore(contentScores.toArray(new Integer[]{}));
            _record.setStructureScore(structureScores.toArray(new Integer[]{}));
            view.set_records(_record);
            view.setEvaluation(data.getEvaluation());
            view.setRecordEvaluation(data.getRecordEvaluation());
            return view;
        }
    }

}
