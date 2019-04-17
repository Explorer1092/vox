package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkContentType;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.TotalAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/12/14
 */
@Named
public class NewHomeworkKeyPointsContentLoader extends NewHomeworkContentLoaderTemplate {
    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.KEY_POINTS;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        List<Map<String, Object>> content = new ArrayList<>();
        TeacherDetail teacher = mapper.getTeacher();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();
        List<Map<String, Object>> keyPointsContent = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(mapper.getObjectiveConfig().getContents())) {
            for (Map<String, Object> configContent : mapper.getObjectiveConfig().getContents()) {
                int type = SafeConverter.toInt(configContent.get("type"));
                if (type == ObjectiveConfig.KEY_POINTS_VIDEO) {
                    keyPointsContent.add(configContent);
                }
            }
        }
        if (CollectionUtils.isNotEmpty(keyPointsContent)) {
            TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
            List<Map<String, Object>> packageList = new ArrayList<>();
            Map<Integer, NewContentType> contentTypeMap = questionContentTypeLoaderClient.loadQuestionContentTypeAsMap();

            Set<String> allQuestionDocIds = new HashSet<>();
            Set<String> allVideoIds = new HashSet<>();
            keyPointsContent.forEach(map -> {
                List<String> questionDocIds = (List) map.get("question_ids");
                if (CollectionUtils.isNotEmpty(questionDocIds)) {
                    allQuestionDocIds.addAll(questionDocIds);
                }
                allVideoIds.add(SafeConverter.toString(map.get("video_id")));
            });
            Map<String, NewQuestion> allQuestionMap = questionLoaderClient.loadQuestionByDocIds(allQuestionDocIds)
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getId, Function.identity()));
            Map<String, NewQuestion> docIdQuestionMap = allQuestionMap.values()
                    .stream()
                    .collect(Collectors.toMap(NewQuestion::getDocId, Function.identity()));
            Map<String, Video> allVideoMap = videoLoaderClient.loadVideoByDocIds(allVideoIds);
            // 获得所有题的使用次数
            Map<String, TotalAssignmentRecord> totalAssignmentRecordMap = totalAssignmentRecordLoader.loadTotalAssignmentRecordByContentType(Subject.MATH,
                    allQuestionMap.keySet(), HomeworkContentType.QUESTION);
            EmbedBook book = new EmbedBook();
            book.setBookId(bookId);
            book.setUnitId(unitId);
            keyPointsContent.forEach(map -> {
                String videoDocId = SafeConverter.toString(map.get("video_id"));
                Video video = allVideoMap.get(videoDocId);
                List<String> questionDocIds = (List) map.get("question_ids");
                List<NewQuestion> questionList = questionDocIds.stream()
                        .filter(docIdQuestionMap::containsKey)
                        .map(docIdQuestionMap::get)
                        .collect(Collectors.toList());
                if (video != null && CollectionUtils.isNotEmpty(questionList)) {
                    List<Map<String, Object>> questionMapperList = questionList.stream()
                            .map(q -> NewHomeworkContentDecorator.decorateNewQuestion(q, contentTypeMap, totalAssignmentRecordMap, teacherAssignmentRecord, book))
                            .collect(Collectors.toList());
                    int questionSeconds = questionMapperList.stream()
                            .mapToInt(q -> SafeConverter.toInt(q.get("seconds")))
                            .sum();
                    packageList.add(MapUtils.m(
                            "videoId", video.getId(),
                            "videoUrl", video.getVideoUrl(),
                            "videoName", video.getVideoName(),
                            "coverUrl", video.getCoverUrl(),
                            "thumbUrl", generateThumbUrl(video.getCoverUrl()),
                            "videoSummary", video.getVideoSummary(),
                            "solutionTracks", video.getSolutionTricks(),
                            "videoSeconds", video.getVideoSeconds(),
                            "questions", questionMapperList,
                            "questionSeconds", questionSeconds,
                            "unitId", unitId,
                            "book", NewHomeworkContentDecorator.buildBookMapper(book))
                    );
                }
            });
            if (CollectionUtils.isNotEmpty(packageList)) {
                content.add(MapUtils.m("type", "package", "packages", packageList));
            }
        }
        return content;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadWaterfallContent(NewHomeworkContentLoaderMapper mapper) {
        ObjectiveConfig objectiveConfig = mapper.getObjectiveConfig();
        List<Map<String, Object>> content = loadContent(mapper);
        if (CollectionUtils.isNotEmpty(content)) {
            List<Map<String, Object>> packageList = Collections.emptyList();
            for (Map<String, Object> contentMapper : content) {
                String type = SafeConverter.toString(contentMapper.get("type"));
                if ("package".equals(type)) {
                    List<Map<String, Object>> packages = (List<Map<String, Object>>) contentMapper.get("packages");
                    if (CollectionUtils.isNotEmpty(packages)) {
                        packageList = packages;
                        break;
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(packageList)) {
                return MapUtils.m(
                        "objectiveConfigId", objectiveConfig.getId(),
                        "type", getObjectiveConfigType().name(),
                        "typeName", getObjectiveConfigType().getValue(),
                        "name", objectiveConfig.getName(),
                        "packages", packageList
                );
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        Map<String, String> questionIdVideoIdMap = new LinkedHashMap<>();
        if (CollectionUtils.isNotEmpty(contentIdList)) {
            contentIdList.forEach(contentId -> {
                if (StringUtils.isNotBlank(contentId)) {
                    String splitContentIds[] = StringUtils.split(contentId, "|");
                    if (splitContentIds.length == 2) {
                        String videoId = splitContentIds[0];
                        String questionId = splitContentIds[1];
                        questionIdVideoIdMap.put(questionId, videoId);
                    }
                }
            });
        }
        List<Map<String, Object>> questionList = Collections.emptyList();
        if (MapUtils.isNotEmpty(questionIdVideoIdMap)) {
            questionList = previewQuestions(new ArrayList<>(questionIdVideoIdMap.keySet()));
            questionList.forEach(q -> {
                String questionId = SafeConverter.toString(q.get("questionId"));
                q.put("videoId", questionIdVideoIdMap.get(questionId));
            });
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "questions", questionList
        );
    }

    private String generateThumbUrl(String coverUrl) {
        if (StringUtils.isBlank(coverUrl) || !coverUrl.contains("c.17zuoye.com")) {
            return coverUrl;
        }
        return UrlUtils.buildUrlQuery(coverUrl, MapUtils.m("x-oss-process", "image/resize,w_200"));
    }
}
