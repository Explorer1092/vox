package com.voxlearning.utopia.enanalyze.service.support;

import com.voxlearning.utopia.enanalyze.assemble.AINLPClient;
import com.voxlearning.utopia.enanalyze.model.ArticleBasicAbility;
import com.voxlearning.utopia.enanalyze.model.ArticleCompositeAbility;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 能力计算服务
 *
 * @author xiaolei.li
 * @version 2018/7/20
 */
@Service
public final class ArticleAbilityServiceImpl {

    /**
     * 根据ai的nlp结果计算基础能力
     *
     * @param result nlp结果
     * @return 基础能力
     */
    public ArticleBasicAbility getBasicAbility(AINLPClient.Result result) {
        ArticleBasicAbility ability = new ArticleBasicAbility();
        if (null != result && null != result.getEssayRating()) {
            AINLPClient.Result.EssayRating essayRating = result.getEssayRating();
            ability.setScore(essayRating.getOverall_score());
            ability.setLexicalScore(essayRating.getLexical_score());
            ability.setSentenceScore(essayRating.getSentence_score());
            ability.setContentScore(essayRating.getContent_score());
            ability.setStructureScore(essayRating.getStructure_score());
        }
        return ability;
    }

    /**
     * 根据ai的nlp结果计算综合能力
     *
     * @param result nlp结果
     * @return 综合能力
     */
    public ArticleCompositeAbility getCompositeAbility(AINLPClient.Result result) {
        return getCompositeAbility(getBasicAbility(result));
    }


    /**
     * 计算综合能力模型
     *
     * @param basicAbility 基本能力
     * @return 综合能力
     */
    public ArticleCompositeAbility getCompositeAbility(ArticleBasicAbility basicAbility) {
        ArticleCompositeAbility compositeAbility = new ArticleCompositeAbility();
        float lexicalScore = Optional.ofNullable(basicAbility).map(ArticleBasicAbility::getLexicalScore).filter(e -> e != null).orElse(0.0f);
        float sentenceScore = Optional.ofNullable(basicAbility).map(ArticleBasicAbility::getSentenceScore).filter(e -> e != null).orElse(0.0f);
        float structScore = Optional.ofNullable(basicAbility).map(ArticleBasicAbility::getStructureScore).filter(e -> e != null).orElse(0.0f);
        float contentScore = Optional.ofNullable(basicAbility).map(ArticleBasicAbility::getContentScore).filter(e -> e != null).orElse(0.0f);
        // 语法知识
        compositeAbility.setGrammarScore(
                Double.valueOf(0 + lexicalScore * 0.4 + sentenceScore * 0.6)
                        .floatValue());
        // 篇章知识
        compositeAbility.setChapterScore(structScore);
        // 语言表达
        compositeAbility.setLanguageScore(
                Double.valueOf(0 + structScore * 0.3  + sentenceScore * 0.7)
                        .floatValue());
        // 语用表达
        compositeAbility.setPragmaticScore(sentenceScore);
        // 写作策略
        compositeAbility.setWritingScore(
                Double.valueOf(0 + lexicalScore * 0.2 + structScore * 0.4 + contentScore * 0.4)
                        .floatValue());

        // 计算能力平均分
        float score = Double.valueOf(0
                + compositeAbility.getGrammarScore() * 0.4
                + compositeAbility.getChapterScore() * 0.15
                + compositeAbility.getLanguageScore() * 0.05
                + compositeAbility.getPragmaticScore() * 0.3
                + compositeAbility.getWritingScore() * 0.1
        ).floatValue();
        compositeAbility.setScore(score);

        return compositeAbility;
    }

    /**
     * 根据多个nlp计算结果计算综合能力模型
     *
     * @param results 多个nlp结果
     * @return 综合能力
     */
    public ArticleCompositeAbility getAverageCompositeAbility(List<AINLPClient.Result> results) {
        ArticleCompositeAbility ability = new ArticleCompositeAbility();
        // 语法知识
        ability.setGrammarScore(
                Double.valueOf(results.stream()
                        .filter(i -> null != i.getEssayRating())
                        .map(AINLPClient.Result::getEssayRating)
                        .mapToDouble(i -> i.getLexical_score() * 0.4 + i.getSentence_score() * 0.6)
                        .average().getAsDouble()).floatValue());
        // 篇章知识
        ability.setChapterScore(
                Double.valueOf(results.stream()
                        .filter(i -> null != i.getEssayRating())
                        .map(AINLPClient.Result::getEssayRating)
                        .mapToDouble(i -> i.getStructure_score())
                        .average().getAsDouble()).floatValue());
        // 语言表达
        ability.setLanguageScore(
                Double.valueOf(results.stream()
                        .filter(i -> null != i.getEssayRating())
                        .map(AINLPClient.Result::getEssayRating)
                        .mapToDouble(i -> i.getStructure_score() * 0.3 + i.getContent_score() * 0.7)
                        .average().getAsDouble()).floatValue());
        // 语用表达
        ability.setPragmaticScore(
                Double.valueOf(results.stream()
                        .filter(i -> null != i.getEssayRating())
                        .map(AINLPClient.Result::getEssayRating)
                        .mapToDouble(i -> i.getContent_score())
                        .average().getAsDouble()).floatValue());
        // 写作策略
        ability.setWritingScore(
                Double.valueOf(results.stream()
                        .filter(i -> null != i.getEssayRating())
                        .map(AINLPClient.Result::getEssayRating)
                        .mapToDouble(i -> i.getLexical_score() * 0.2 + i.getStructure_score() * 0.4 + i.getContent_score() * 0.4)
                        .average().getAsDouble()).floatValue());

        // 计算能力平均分
        float score = Double.valueOf(0
                + ability.getGrammarScore() * 0.4
                + ability.getChapterScore() * 0.15
                + ability.getLanguageScore() * 0.05
                + ability.getPragmaticScore() * 0.3
                + ability.getWritingScore() * 0.1
        ).floatValue();
        ability.setScore(score);
        return ability;
    }


}
