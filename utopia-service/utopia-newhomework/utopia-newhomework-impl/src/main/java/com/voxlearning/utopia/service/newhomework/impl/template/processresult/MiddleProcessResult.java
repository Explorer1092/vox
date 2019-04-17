package com.voxlearning.utopia.service.newhomework.impl.template.processresult;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.entity.MiddleSchoolHomeworkDo;
import com.voxlearning.utopia.service.newhomework.api.entity.MiddleSchoolHomeworkDoQuestion;
import com.voxlearning.utopia.service.newhomework.api.entity.MiddleSchoolHomeworkDoQuestionData;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkProcessMapper;
import com.voxlearning.utopia.service.newhomework.impl.dao.MiddleSchoolHomeworkDoDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.MiddleSchoolHomeworkDoQuestionDao;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessResultTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author xuesong.zhang
 * @since 2016/8/5
 */
@Named
public class MiddleProcessResult extends ProcessResultTemplate {

    @Inject private MiddleSchoolHomeworkDoDao middleSchoolHomeworkDoDao;
    @Inject private MiddleSchoolHomeworkDoQuestionDao middleSchoolHomeworkDoQuestionDao;

    @Override
    public SchoolLevel getSchoolLevel() {
        return SchoolLevel.MIDDLE;
    }

    @Override
    public List<HomeworkProcessMapper> getProcessResult(Collection<String> processIds) {
        if (CollectionUtils.isEmpty(processIds)) {
            return Collections.emptyList();
        }

        String processId = processIds.iterator().next();
        if(StringUtils.contains(processId, '_')) {
            return this.getResultListDeprecated(processIds);
        } else {
            return this.getResultList(processIds);
        }
    }

    private List<HomeworkProcessMapper> getResultList(Collection<String> processIds) {
        Map<String, MiddleSchoolHomeworkDoQuestion> map = middleSchoolHomeworkDoQuestionDao.loads(processIds);
        List<HomeworkProcessMapper> resultList = new ArrayList<>();
        map.values().forEach(o -> {

            HomeworkProcessMapper mapper = new HomeworkProcessMapper();
            try {
                mapper.setProcessId(o.getId());
                mapper.setQuestionId(o.getQuestionId());
                mapper.setWordId(o.getWordId());
                mapper.setContentTypeId(o.getContentTypeId());
                mapper.setTagId(o.getTagId());
                mapper.setClazzGroupId(o.getClazzId());
                mapper.setHomeworkId(o.getHomeworkId());
                mapper.setUserId(o.getStudentId());
                mapper.setGrasp(true);
                mapper.setSubGrasp(new ArrayList<>());
                mapper.setUserAnswers(new ArrayList<>());
                mapper.setDuration(o.getDuration());
                mapper.setSchoolLevel(this.getSchoolLevel());
                mapper.setSubject(o.getSubject());
                mapper.setCreateAt(o.getCreatedAt());
                mapper.setOralScore(o.getScore());
                mapper.setSubOralScores(new ArrayList<>());
                mapper.setIsRightList(new ArrayList<>());

                o.getSubContentDatas().forEach(subContentData -> {
                    Integer isRight = subContentData.getIsRight();
                    List<String> answers = subContentData.getAnswers();
                    List<Integer> isBlankRightList = subContentData.getIsBlankRightList();
                    Integer subOralScore = subContentData.getScore();
                    if(null == isRight) {
                        isRight = -2;
                    }
                    if(null == answers) {
                        answers = new ArrayList<>();
                    }
                    if(null == isBlankRightList) {
                        isBlankRightList = new ArrayList<>();
                    }

                    if (isRight != 1) {
                        mapper.setGrasp(false);
                    }

                    List<Boolean> subGraspItem = new ArrayList<>();
                    isBlankRightList.forEach(ir -> subGraspItem.add(1 == ir));
                    mapper.getSubGrasp().add(subGraspItem);

                    mapper.getSubOralScores().add(subOralScore);
                    mapper.getIsRightList().add(Long.valueOf(isRight));
                    mapper.getUserAnswers().add(answers);
                });

                resultList.add(mapper);

            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
        });
        return resultList;
    }

    @Deprecated
    private List<HomeworkProcessMapper> getResultListDeprecated(Collection<String> processIds) {
        List<HomeworkProcessMapper> resultList = new ArrayList<>();

        // 需要保证processIds来自且仅来自同一份作业
        String processId = processIds.iterator().next();

        String[] segments = StringUtils.split(processId, "_");
        if (segments == null) {
            return Collections.emptyList();
        }

        String[] middleSchoolHomeworkDoid = StringUtils.split(segments[0], "-");
        if (middleSchoolHomeworkDoid == null || middleSchoolHomeworkDoid.length != 4) {
            return Collections.emptyList();
        }

        MiddleSchoolHomeworkDo hDo = middleSchoolHomeworkDoDao.load(segments[0]);
        if (hDo == null) {
            return Collections.emptyList();
        }

        hDo.getPractices().forEach((pType, hDoPractice) ->
                hDoPractice.getQuestionDatas().forEach(map -> {
                    MiddleSchoolHomeworkDoQuestionData qData = this.buildData(map);
                    HomeworkProcessMapper mapper = new HomeworkProcessMapper();
                    try {
                        mapper.setProcessId(segments[0] + "_" + pType + "_" + qData.getQuestionId());
                        mapper.setQuestionId(qData.getQuestionId());
                        mapper.setWordId(qData.getWordId());
                        mapper.setContentTypeId(qData.getContentTypeId());
                        mapper.setTagId(qData.getTagId());
                        mapper.setClazzGroupId(hDo.getClazzId());
                        mapper.setHomeworkId(hDo.getHomeworkId());
                        mapper.setUserId(hDo.getStudentId());
                        mapper.setGrasp(qData.getGrasp());
                        mapper.setSubGrasp(qData.getSubGrasp());
                        mapper.setUserAnswers(qData.getUserAnswers());
                        mapper.setDuration(qData.getDuration());
                        mapper.setSchoolLevel(this.getSchoolLevel());
                        mapper.setSubject(hDo.parseID().getSubject());
                        mapper.setCreateAt(hDo.getCreateTime());
                        mapper.setOralScore(qData.getOralScore());
                        mapper.setSubOralScores(qData.getSubOralScores());
                        mapper.setIsRightList(qData.getIsRightList());
                        resultList.add(mapper);
                    } catch (Exception ignored) {
                        ignored.printStackTrace();
                    }
                })
        );
        return resultList;
    }

    private MiddleSchoolHomeworkDoQuestionData buildData(LinkedHashMap<String, Object> origin) {
        MiddleSchoolHomeworkDoQuestionData questionData = new MiddleSchoolHomeworkDoQuestionData();
        questionData.setQuestionId(origin.getOrDefault("question_id", "").toString());
        Object wordId = origin.get("word_id");
        if (null != wordId) {
            questionData.setWordId(wordId.toString());
        }
        Object oralScore = origin.get("score");
        if (null != oralScore) {
            questionData.setOralScore(SafeConverter.toInt(oralScore));
        } else {
            questionData.setOralScore(null);
        }
        questionData.setContentTypeId(SafeConverter.toInt(origin.getOrDefault("content_type_id", 0)));
        questionData.setTagId(SafeConverter.toInt(origin.getOrDefault("tag_id", 0)));
        questionData.setDuration(SafeConverter.toLong(origin.getOrDefault("duration", 0)));
        questionData.setGrasp(true);
        questionData.setSubGrasp(new ArrayList<>());
        questionData.setUserAnswers(new ArrayList<>());
        questionData.setSubOralScores(new ArrayList<>());
        questionData.setIsRightList(new ArrayList<>());

        origin.forEach((k, v) -> {
            if (!StringUtils.isNumeric(k)) return;
            HashMap<String, Object> sub = (HashMap<String, Object>) v;
            Long isRight = SafeConverter.toLong(sub.get("is_right"), -2);
            List<String> answers = (null != sub.get("answers")) ? (List<String>) sub.get("answers") : new ArrayList<>();
            List<Integer> isBlankRightList = new ArrayList<>();
            if (null != sub.get("is_blank_right_list")) {
                List<Object> tempList = (List<Object>) sub.get("is_blank_right_list");
                isBlankRightList = tempList.stream()
                        .map(SafeConverter::toInt)
                        .collect(Collectors.toList());
            }
            Object subOralScore = sub.get("score");
            List<Boolean> subGraspItem = new ArrayList<>();
            isBlankRightList.forEach(ir -> subGraspItem.add(1 == ir));
            if (isRight != 1) {
                questionData.setGrasp(false);
            }
            if (null != subOralScore) {
                questionData.getSubOralScores().add(SafeConverter.toInt(subOralScore));
            } else {
                questionData.getSubOralScores().add(null);
            }
            questionData.getIsRightList().add(isRight);
            questionData.getUserAnswers().add(answers);
            questionData.getSubGrasp().add(subGraspItem);
        });

        if (questionData.getOralScore() == null && questionData.getSubOralScores().size() == 1) {
            if (questionData.getSubOralScores().get(0) != null) {
                questionData.setOralScore(questionData.getSubOralScores().get(0));
            }
        }
        return questionData;
    }
}
