package com.voxlearning.utopia.enanalyze.convertor;

import com.voxlearning.utopia.enanalyze.assemble.AINLPClient;
import com.voxlearning.utopia.enanalyze.entity.ArticleEntity;
import com.voxlearning.utopia.enanalyze.model.Article;
import com.voxlearning.utopia.enanalyze.model.ArticleBasicAbility;
import com.voxlearning.utopia.enanalyze.service.support.ArticleEvaluator;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 作文记录构建器
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Service
public class ArticleBuilder {

    @Resource
    ArticleEvaluator evaluator;

    public Article build(ArticleEntity entity) {
        Article output = new Article();
        output.setOpenId(entity.getOpenId());
        output.setArticleId(entity.getId());
        output.setBasicAbility(entity.getBasicAbility());
        output.setCompositeAbility(entity.getCompositeAbility());
        AINLPClient.Result nlpResult = entity.getNlpResult();
        if (null != nlpResult) {
            if (null != nlpResult.getSyntaxCheck() && null != nlpResult.getSyntaxCheck().getContent() && null != nlpResult.getSyntaxCheck().getContent().get(0))
                output.setText(nlpResult.getSyntaxCheck().getContent().get(0).getOriginal());
            AINLPClient.Result.EssayRating essayRating = nlpResult.getEssayRating();
            if (null != essayRating) {
                ArticleBasicAbility ability = new ArticleBasicAbility();
                ability.setScore(essayRating.getOverall_score());
                ability.setLexicalScore(essayRating.getLexical_score());
                ability.setSentenceScore(essayRating.getSentence_score());
                ability.setContentScore(essayRating.getContent_score());
                ability.setStructureScore(essayRating.getStructure_score());
                entity.setBasicAbility(ability);
            }
            output.setEvaluation(evaluator.getEvaluation(nlpResult));
            int beyondRate = evaluator.getBeyondRate(nlpResult);
            output.setBeyondRate(beyondRate);
            output.setSubEvaluation(evaluator.getSubEvaluation(entity.getBasicAbility().getScore(), beyondRate));
            output.setGoodSents(evaluator.getGoodSents(nlpResult));
            output.setGoodWords(evaluator.getGoodWords(nlpResult));
            List<Article.SyntaxCheckDetail> detailList = nlpResult.getSyntaxCheck().getContent().get(0).getDetails().stream()
                    .map(i -> {
                        Article.SyntaxCheckDetail detail = new Article.SyntaxCheckDetail();
                        detail.setAnswer(i.getAnswer());
                        detail.setStart(i.getStart());
                        detail.setEnd(i.getEnd());
                        detail.setModified(i.isModified());
                        detail.setSentBegin(i.getSentBegin());
                        detail.setSentEnd(i.getSentEnd());
                        detail.setSentIndex(i.getSentIndex());
                        detail.setDescription(i.getDescription());
                        detail.setText(i.getText());
                        detail.setType(AINLPClient.SyntaxCheckType.of(i.getType()).DESC_CN);
                        return detail;
                    }).collect(Collectors.toList());
            output.setSyntaxCheckDetails(detailList.toArray(new Article.SyntaxCheckDetail[]{}));
        }
        return output;
    }


}
