package com.voxlearning.utopia.service.newhomework.impl.template.daite;

import com.google.common.collect.Lists;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.constant.DaiTeType;
import com.voxlearning.utopia.service.newhomework.api.util.NewHomeworkUtils;
import com.voxlearning.utopia.service.newhomework.impl.template.DaiTeTypeTemplate;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.question.api.QuestionLoader;
import com.voxlearning.utopia.service.question.api.entity.NewQuestion;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsContent;
import com.voxlearning.utopia.service.question.api.entity.NewQuestionsSubContents;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/2/28
 * \* Time: 10:59 AM
 * \* Description: 字卡
 * \
 */
@Named
public class DaiTeZiKaTypeTemple implements DaiTeTypeTemplate {

    @Inject
    private QuestionLoader questionLoader;

    @Override
    public DaiTeType getDaiTeType() {
        return DaiTeType.ZI_KA;
    }

    @Override
    public Object getDaiTeDataByType(NewHomeworkContentLoaderMapper mapper, Map params) {
        String word = SafeConverter.toString(params.get("ziKaWord"), "");
        Map<String, NewQuestion> questionMap = questionLoader.loadShenZiRenDuQuestionsByWord(word);
        if (MapUtils.isEmpty(questionMap)) {
            return Lists.newArrayList();
        }
        List<Map<String, Object>> zikaList = questionMap.values().stream()
                .filter(Objects::nonNull)
                .map(NewQuestion::getContent)
                .filter(Objects::nonNull)
                .map(NewQuestionsContent::getSubContents)
                .filter(Objects::nonNull)
                .filter(CollectionUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(NewQuestionsSubContents::getExtras)
                .filter(Objects::nonNull)
                .filter(NewHomeworkUtils.distinctByKey(e -> e.get("chineseWordId")))
                .map(e -> MapUtils.m(
                        "chineseWordRadical", SafeConverter.toString(e.get("chineseWordRadical"), ""), //部首
                        "chineseWordStrokesOrder", SafeConverter.toString(e.get("chineseWordStrokesOrder"), ""), // 笔划
                        "wordExplain", SafeConverter.toString(e.get("wordExplain"), ""), //  释义
                        "chineseWordStructure", SafeConverter.toString(e.get("chineseWordStructure"), ""), //  结构
                        "chineseWordContent", SafeConverter.toString(e.get("chineseWordContent"), ""), //  结构
                        "chineseWordStrokes", SafeConverter.toString(e.get("chineseWordStrokes"), ""), //  共有几画
                        "wordContentPinyinMark", SafeConverter.toString(e.get("wordContentPinyinMark"), ""), //  拼音
                        "chineseWordImgUrl", SafeConverter.toString(e.get("chineseWordImgUrl"), ""), //  字的gif图
                        "chineseWordAudioUrl", SafeConverter.toString(e.get("chineseWordAudioUrl"), ""), //  音频地址
                        "wordWords", SafeConverter.toString(e.get("wordWords"), "") //  组词
                ))
                .collect(Collectors.toList());
        return zikaList;
    }
}
