package com.voxlearning.utopia.enanalyze.model;

import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 作文综合能力模型
 *
 * @author xiaolei.li
 * @version 2018/7/20
 */
@Data
public class ArticleCompositeAbility implements Serializable {

    /**
     * 能力总分
     */
    private Float score;

    /**
     * 语法知识评分
     */
    private Float grammarScore;

    /**
     * 语篇能力评分
     */
    private Float chapterScore;

    /**
     * 表达能力评分
     */
    private Float languageScore;

    /**
     * 语用能力评分
     */
    private Float pragmaticScore;

    /**
     * 写作策略评分
     */
    private Float writingScore;

    /**
     * 能力维度
     */
    @AllArgsConstructor
    public enum Dimension {
        GRAMMAR("语法知识"),
        CHAPTER("语篇能力"),
        LANGUAGE("表达能力"),
        PRAGMATIC("语用能力"),
        WRITING("写作策略"),;
        public final String DESC;

    }

    /**
     * 多维度相同
     *
     * @return
     */
    @DocumentFieldIgnore
    public boolean isSideBySide() {
        return grammarScore.equals(chapterScore)
                && grammarScore.equals(languageScore)
                && grammarScore.equals(pragmaticScore)
                && grammarScore.equals(writingScore);
    }

    /**
     * 获取最高分维度
     *
     * @return 最高分维度
     */
    @DocumentFieldIgnore
    public Dimension getHighestDimension() {
        String[] scores = Stream.of(
                String.format("%d@%s", grammarScore.intValue(), Dimension.GRAMMAR.name()),
                String.format("%d@%s", chapterScore.intValue(), Dimension.CHAPTER.name()),
                String.format("%d@%s", languageScore.intValue(), Dimension.LANGUAGE.name()),
                String.format("%d@%s", pragmaticScore.intValue(), Dimension.PRAGMATIC.name()),
                String.format("%d@%s", writingScore.intValue(), Dimension.WRITING.name()))
                .sorted(Comparator.reverseOrder())
                .toArray(String[]::new);
        return Dimension.valueOf(scores[0].split("@")[1]);
    }

    /**
     * 获取最高分维度
     *
     * @return 最高分维度
     */
    @DocumentFieldIgnore
    public Dimension getLowestDimension() {
        String[] scores = Stream.of(
                String.format("%d@%s", grammarScore.intValue(), Dimension.GRAMMAR.name()),
                String.format("%d@%s", chapterScore.intValue(), Dimension.CHAPTER.name()),
                String.format("%d@%s", languageScore.intValue(), Dimension.LANGUAGE.name()),
                String.format("%d@%s", pragmaticScore.intValue(), Dimension.PRAGMATIC.name()),
                String.format("%d@%s", writingScore.intValue(), Dimension.WRITING.name()))
                .sorted(Comparator.naturalOrder())
                .toArray(String[]::new);
        return Dimension.valueOf(scores[0].split("@")[1]);
    }

}
