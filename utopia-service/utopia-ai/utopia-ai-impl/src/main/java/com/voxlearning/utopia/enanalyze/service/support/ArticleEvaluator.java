package com.voxlearning.utopia.enanalyze.service.support;

import com.voxlearning.alps.repackaged.org.apache.commons.lang3.ArrayUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.RandomUtils;
import com.voxlearning.utopia.enanalyze.assemble.AINLPClient;
import com.voxlearning.utopia.enanalyze.assemble.AINLPClient.Result.EssayRating;
import com.voxlearning.utopia.enanalyze.model.ArticleBasicAbility.Dimension;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * 作业评估器<br>
 * <ul>
 * <li>获取总体评估文案</li>
 * <li>好词的取舍</li>
 * <li>好句的取舍</li>
 * </ul>
 * 评价策略请参考 <a href="http://wiki.17zuoye.net/pages/viewpage.action?pageId=38921313">单次批改结果文案逻辑</href>
 *
 * @author xiaolei.li
 * @version 2018/7/21
 */
@Service
public class ArticleEvaluator {

    /**
     * 根据nlp结果获取总体评价
     *
     * @param nlpResult nlp结果
     * @return 总体评价
     */
    public String getEvaluation(AINLPClient.Result nlpResult) {
        StringBuilder sb = new StringBuilder();
        // 总评分
        final float score = nlpResult.getEssayRating().getOverall_score();
        if (score <= 100 && score >= 88)
            sb.append("你的作文已宇宙无人能敌了！");
        else if (score >= 74)
            sb.append("恭喜你获得成就【英语学霸】！");
        else if (score >= 59)
            sb.append("用心写出的作文才最动人。");
        else if (score >= 44)
            sb.append("用心写出的作文才最动人。");
        else
            sb.append("同学，你需要来自宇宙的能量。");

        // 如维度分数相同，则评价的优先级为:词法---句法----结构----内容
        float lexical_score = nlpResult.getEssayRating().getLexical_score();
        float sentence_score = nlpResult.getEssayRating().getSentence_score();
        float structure_score = nlpResult.getEssayRating().getStructure_score();
        float content_score = nlpResult.getEssayRating().getContent_score();
        DecimalFormat df = new DecimalFormat("000.00");
        // 采用加权排序
        String[] scores = Stream.of(
                String.format("%s@%s@%s", df.format(lexical_score), 4, Dimension.LEXICAL.name()),
                String.format("%s@%s@%s", df.format(sentence_score), 3, Dimension.SENTENCE.name()),
                String.format("%s@%s@%s", df.format(structure_score), 2, Dimension.STRUCTURE.name()),
                String.format("%s@%s@%s", df.format(content_score), 1, Dimension.CONTENT.name()))
                .sorted(Comparator.reverseOrder())
                .toArray(String[]::new);

        // 最高维度评分
        {
            Dimension dimension = Dimension.valueOf(scores[0].split("@")[2]);
            Float _score = Float.valueOf(scores[0].split("@")[0]);
            if (_score <= 100 && _score >= 85) {
                switch (dimension) {
                    case LEXICAL:
                        sb.append("作为行走的英文词典，用词准确漂亮，");
                        break;
                    case SENTENCE:
                        sb.append("地道的语法和多变的句式让人惊叹！");
                        break;
                    case CONTENT:
                        sb.append("文章的主题明确，细节丰富，可读性也很强呢！");
                        break;
                    case STRUCTURE:
                        sb.append("本文的写作线索清晰，起承转合得当；");
                        break;
                }
            } else if (_score >= 70) {
                switch (dimension) {
                    case LEXICAL:
                        sb.append("这里一些小词的运用准确且生动～");
                        break;
                    case SENTENCE:
                        sb.append("要为你比较扎实的语法基础打call～");
                        break;
                    case CONTENT:
                        sb.append("较为丰富的细节恰到好处的强化了内容的表述。");
                        break;
                    case STRUCTURE:
                        sb.append("篇章结构较为合理，如果更细致地谋篇布局会使主题更加清晰。");
                        break;
                }
            } else if (_score >= 55) {
                switch (dimension) {
                    case LEXICAL:
                        sb.append("词汇的选用能够完成任务，但还不够丰富。");
                        break;
                    case SENTENCE:
                        sb.append("句式变化不够丰富，要利用课余时间多多学习语法知识呀。");
                        break;
                    case CONTENT:
                        sb.append("内容表达基本过关，也没有出现比较夸张的表述错误。");
                        break;
                    case STRUCTURE:
                        sb.append("文章结构稍显简单，可以尝试合理使用连接词呀。");
                        break;
                }
            } else {
                switch (dimension) {
                    case LEXICAL:
                        sb.append("是不是词汇量限制了你的发挥呢？");
                        break;
                    case SENTENCE:
                        sb.append("这里的句式较为单一，不要只是简单的拼凑哦。");
                        break;
                    case CONTENT:
                        sb.append("文章内容较为空泛，缺乏必要的细节描述呢。");
                        break;
                    case STRUCTURE:
                        sb.append("篇章结构过于简单，致使主题呈现受到了一定限制。");
                        break;
                }
            }
        }

        // 次高维度评分
        {
            Dimension dimension = Dimension.valueOf(scores[1].split("@")[2]);
            Float _score = Float.valueOf(scores[1].split("@")[0]);
            if (_score <= 100 && _score >= 85) {
                switch (dimension) {
                    case LEXICAL:
                        sb.append("词汇选用恰当且词汇掌握程度不错！");
                        break;
                    case SENTENCE:
                        sb.append("你选用了较为丰富的句式，体现了不错的语言应用能力。");
                        break;
                    case CONTENT:
                        sb.append("此外，文章的起承转合比较完整细致，值得肯定。");
                        break;
                    case STRUCTURE:
                        sb.append("句子之间衔接自然得当，体现了较强的逻辑关系。");
                        break;
                }
            } else if (_score >= 70) {
                switch (dimension) {
                    case LEXICAL:
                        sb.append("这里一些小词的运用准确且生动～");
                        break;
                    case SENTENCE:
                        sb.append("这里体现出的语法功力和表达能力也较为优秀。");
                        break;
                    case CONTENT:
                        sb.append("句与句、层与层之间衔接合理，恰到好处的强化了内容的表述。");
                        break;
                    case STRUCTURE:
                        sb.append("篇章结构较为合理，如果更细致地谋篇布局会使主题更加清晰。");
                        break;
                }
            } else if (_score >= 55) {
                switch (dimension) {
                    case LEXICAL:
                        sb.append("词汇的选用能够完成任务，但还不够丰富。");
                        break;
                    case SENTENCE:
                        sb.append("另外不要偷偷把重要的语法知识还给老师哦。");
                        break;
                    case CONTENT:
                        sb.append("内容表达基本清楚，没有出现比较严重的表述错误。");
                        break;
                    case STRUCTURE:
                        sb.append("此外，句子间的连接有点生硬，或许动笔之前先规划一下会更好。");
                        break;
                }
            } else {
                switch (dimension) {
                    case LEXICAL:
                        sb.append("此外，简单的词汇掌握出现了较多的问题，买零食的钱要用来买词汇书才行～");
                        break;
                    case SENTENCE:
                        sb.append("此外，看起来你过分偏爱简单句了哦，重要语法点也没能很好的掌握呢。");
                        break;
                    case CONTENT:
                        sb.append("此外文章内容较为空泛，缺乏必要的细节描述哦。");
                        break;
                    case STRUCTURE:
                        sb.append("此外文章结构简单，缺乏自然有效的逻辑联系，看起来比较单薄。");
                        break;
                }
            }
        }

        // 最低维度评分
        {
            Dimension dimension = Dimension.valueOf(scores[3].split("@")[2]);
//            Float _score = Float.valueOf(scores[1].split("@")[0]);
            switch (dimension) {
                case LEXICAL:
                    sb.append("如果能再用心雕琢打磨词语就更好了！");
                    break;
                case SENTENCE:
                    sb.append("如果继续加强语法练习，你一定可以取得更高的成绩！");
                    break;
                case CONTENT:
                    sb.append("如果文章内容能更贴合题意就更棒了。");
                    break;
                case STRUCTURE:
                    sb.append("如果能更多的琢磨文章的层次和连贯性就完美了！");
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * 获取次级评价
     *
     * @param score
     * @param beyondRate 超越百分比
     * @return 次级评价
     */
    public String getSubEvaluation(float score, int beyondRate) {
        if (score >= 60) {
            return String.format("Not bad ~ 你已经打败了%d%%的同学！！！", beyondRate);
        } else {
            return String.format("请继续加油 ~ 你已经打败了%d%%的同学！！！", beyondRate);
        }
    }

    /**
     * 获取次级评价
     *
     * @param nlpResult nlp结果
     * @return 次级评价
     */
    public int getBeyondRate(AINLPClient.Result nlpResult) {
        float score = nlpResult.getEssayRating().getOverall_score();
        int rank;
        if (93 <= score && 100 >= score)
            rank = RandomUtils.nextInt(96, 100);
        else if (86 <= score)
            rank = RandomUtils.nextInt(90, 96);
        else if (79 <= score)
            rank = RandomUtils.nextInt(79, 90);
        else if (72 <= score)
            rank = RandomUtils.nextInt(64, 79);
        else if (66 <= score)
            rank = RandomUtils.nextInt(51, 64);
        else if (59 <= score)
            rank = RandomUtils.nextInt(40, 51);
        else if (53 <= score)
            rank = RandomUtils.nextInt(30, 40);
        else if (30 <= score)
            rank = RandomUtils.nextInt(15, 30);
        else
            rank = RandomUtils.nextInt(1, 15);
        return rank;
    }

    /**
     * 获取好句
     *
     * @param nlpResult nlp结果
     * @return 好句
     */
    public String[] getGoodSents(AINLPClient.Result nlpResult) {
        if (nlpResult != null && nlpResult.getEssayRating() != null) {
            EssayRating essayRating = nlpResult.getEssayRating();
            EssayRating.GoodSent[] sents = essayRating.getGood_sents();
            EssayRating.GoodWord[] words = essayRating.getGood_words();
            if (essayRating.getOverall_score() >= 60) {
                if (ArrayUtils.isNotEmpty(sents))
                    return Arrays.stream(sents)
                            .sorted(Comparator.comparing(EssayRating.GoodSent::getScore).reversed())
                            .map(EssayRating.GoodSent::getSentence)
                            .toArray(String[]::new);
                if (ArrayUtils.isEmpty(sents) && ArrayUtils.isEmpty(words))
                    return new String[]{"(๑•́ ₃ •̀๑) 暂时没发现呢"};
            }
        }
        return new String[]{};
    }

    /**
     * 获取好词
     *
     * @param nlpResult nlp结果
     * @return 好词
     */
    public String[] getGoodWords(AINLPClient.Result nlpResult) {
        if (nlpResult != null && nlpResult.getEssayRating() != null) {
            EssayRating essayRating = nlpResult.getEssayRating();
            EssayRating.GoodSent[] sents = essayRating.getGood_sents();
            EssayRating.GoodWord[] words = essayRating.getGood_words();
            float overall_score = essayRating.getOverall_score();
            if (overall_score >= 60) {
                if (ArrayUtils.isEmpty(sents) && ArrayUtils.isNotEmpty(words))
                    return Arrays.stream(words)
                            .sorted(Comparator.comparing(EssayRating.GoodWord::getScore).reversed())
                            .map(EssayRating.GoodWord::getWord)
                            .toArray(String[]::new);
            } else if (overall_score < 60 && overall_score >= 20) {
                if (ArrayUtils.isNotEmpty(words))
                    return Arrays.stream(words)
                            .sorted(Comparator.comparing(EssayRating.GoodWord::getScore).reversed())
                            .map(EssayRating.GoodWord::getWord)
                            .toArray(String[]::new);
                else {
                    return new String[]{"(๑•́ ₃ •̀๑) 暂时没发现呢"};
                }
            } else if (overall_score < 20) {
                return new String[]{"(๑•́ ₃ •̀๑) 暂时没发现呢"};
            }
        }
        return new String[]{};
    }
}
