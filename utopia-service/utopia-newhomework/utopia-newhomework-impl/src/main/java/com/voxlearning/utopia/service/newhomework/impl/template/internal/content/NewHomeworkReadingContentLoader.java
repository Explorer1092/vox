package com.voxlearning.utopia.service.newhomework.impl.template.internal.content;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.newhomework.api.constant.PictureBookClazzLevel;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.base.helper.NewHomeworkContentDecorator;
import com.voxlearning.utopia.service.newhomework.impl.athena.AthenaPictureBookLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderMapper;
import com.voxlearning.utopia.service.newhomework.impl.template.NewHomeworkContentLoaderTemplate;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.question.api.entity.*;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/6/29
 */
@Named
public class NewHomeworkReadingContentLoader extends NewHomeworkContentLoaderTemplate {

    @Inject private AthenaPictureBookLoaderClient athenaPictureBookLoaderClient;

    private static final Map<Integer, String> CLAZZ_LEVEL_DESCRIPTION_MAP = new HashMap<>();

    static {
        CLAZZ_LEVEL_DESCRIPTION_MAP.put(3, "建议：三起三年级使用一起一、二年级绘本");
        CLAZZ_LEVEL_DESCRIPTION_MAP.put(4, "建议：三起四年级使用一起三年级绘本");
        CLAZZ_LEVEL_DESCRIPTION_MAP.put(5, "建议：三起五年级使用一起四年级绘本");
        CLAZZ_LEVEL_DESCRIPTION_MAP.put(6, "建议：三起六年级使用一起五年级绘本");
    }

    @Override
    public ObjectiveConfigType getObjectiveConfigType() {
        return ObjectiveConfigType.READING;
    }

    public List<Map<String, Object>> getHomeworkContent(TeacherDetail teacher, Set<Long> groupIds, List<String> sectionIds,
                                                        String unitId, String bookId, Integer currentPageNum) {

        NewHomeworkContentLoaderMapper mapper = new NewHomeworkContentLoaderMapper();
        mapper.setTeacher(teacher);
        mapper.setGroupIds(groupIds);
        mapper.setObjectiveConfig(null);
        mapper.setSectionIds(sectionIds);
        mapper.setUnitId(unitId);
        mapper.setBookId(bookId);
        mapper.setCurrentPageNum(currentPageNum);
        mapper.setHomeworkSourceType(HomeworkSourceType.Web);
        mapper.setSys("");
        mapper.setAppVersion("");
        return loadContent(mapper);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> loadContent(NewHomeworkContentLoaderMapper mapper) {
        TeacherDetail teacher = mapper.getTeacher();
        String unitId = mapper.getUnitId();
        String bookId = mapper.getBookId();
        Subject subject = teacher.getSubject();
        if (subject == null) {
            return Collections.emptyList();
        }
        // 学前学科id为 501,502,503
        int subjectId = teacher.isInfantTeacher() ? subject.getId() + 400 : subject.getId();
        List<Map<String, Object>> content = new ArrayList<>();
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries()
                .stream()
                .filter(pictureBookSeries -> Objects.equals(pictureBookSeries.getSubjectId(), subjectId))
                .collect(Collectors.toList());
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics()
                .stream()
                .filter(pictureBookTopic -> Objects.equals(pictureBookTopic.getSubjectId(), subjectId))
                .collect(Collectors.toList());
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));
        NewBookProfile newBookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        List<Map<String, Object>> readingContent = Collections.emptyList();
        if (newBookProfile != null) {
            readingContent = teachingObjectiveLoaderClient.loadReadingContentByBook(newBookProfile.getId(), newBookProfile.getClazzLevel(), newBookProfile.getTermType());
        }
        TeacherAssignmentRecord teacherAssignmentRecord = teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
        EmbedBook book = new EmbedBook();
        book.setBookId(bookId);
        book.setUnitId(unitId);
        if (CollectionUtils.isNotEmpty(readingContent)) {
            Map<String, Object> readingMap = MiscUtils.firstElement(readingContent);
            List<String> pictureBookIdList = (List<String>) readingMap.get("picture_book_ids");
            String description = SafeConverter.toString(readingMap.get("description"));
            Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBookByDocIds(pictureBookIdList);
            if (MapUtils.isNotEmpty(pictureBookMap)) {
                List<Map<String, Object>> readingMapperList = pictureBookMap.values()
                        .stream()
                        .filter(Objects::nonNull)
                        .map(pictureBook -> NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord))
                        .collect(Collectors.toList());
                content.add(MapUtils.m("type", "weeklyRecommend", "readingList", readingMapperList, "recommendResult", description));
            }
        }
        List<Map<String, Object>> pictureBookTopics = pictureBookTopicList
                .stream()
                .sorted(Comparator.comparingInt(PictureBookTopic::getRank))
                .map(topic -> MapUtils.m("topicId", topic.getId(), "topicName", topic.getName()))
                .collect(Collectors.toList());
        List<Map<String, Object>> pictureBookSeries = pictureBookSeriesList
                .stream()
                .map(series -> MapUtils.m("seriesId", series.getId(), "seriesName", series.fetchName()))
                .collect(Collectors.toList());
        List<Map<String, Object>> pictureBookClazzLevels = Arrays.stream(PictureBookClazzLevel.values())
                .map(pictureBookClazzLevel -> MapUtils.m("clazzLevel", pictureBookClazzLevel, "name", pictureBookClazzLevel.getShowName()))
                .collect(Collectors.toList());

        // 学前没有这个模块
        if (!teacher.isInfantTeacher()) {
            List<String> pictureBookIds = null;
            try {
                pictureBookIds = athenaPictureBookLoaderClient.getPictureBookLoader().loadPictureBookIdsByUnitId(unitId);
            } catch (Exception e) {
                logger.error("NewHomeworkReadingContentLoader call athena error:", e);
            }
            if (CollectionUtils.isNotEmpty(pictureBookIds)) {
                List<PictureBook> synchronousPictureBookList = new ArrayList<>(pictureBookLoaderClient.loadPictureBooksIncludeDisabled(pictureBookIds).values());
                List<Map<String, Object>> synchronousList = synchronousPictureBookList
                        .stream()
                        .filter(Objects::nonNull)
                        .map(pictureBook -> NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, book, teacherAssignmentRecord))
                        .limit(6)
                        .collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(synchronousList)) {
                    content.add(MapUtils.m("type", "synchronous", "readingList", synchronousList));
                }
            }
        }
        String description = "";
        NewBookProfile bookProfile = newContentLoaderClient.loadBookProfilesIncludeDisabled(Collections.singleton(bookId)).get(bookId);
        if (bookProfile != null) {
            int startClazzLevel = SafeConverter.toInt(bookProfile.getStartClazzLevel());
            int clazzLevel = SafeConverter.toInt(bookProfile.getClazzLevel());
            if (startClazzLevel == 3) {
                description = CLAZZ_LEVEL_DESCRIPTION_MAP.getOrDefault(clazzLevel, "");
            }
        }
        content.add(MapUtils.m(
                "type", "search",
                "topics", pictureBookTopics,
                "series", pictureBookSeries,
                "clazzLevels", pictureBookClazzLevels,
                "showClazzLevel", !teacher.isInfantTeacher(),
                "description", description
        ));
        return content;
    }

    @Override
    public Map<String, Object> previewContent(Teacher teacher, String bookId, List<String> contentIdList) {
        List<Map<String, Object>> pictureBookList = Collections.emptyList();
        List<PictureBookSeries> pictureBookSeriesList = pictureBookLoaderClient.loadAllPictureBookSeries();
        Map<String, PictureBookSeries> pictureBookSeriesMap = pictureBookSeriesList.stream()
                .collect(Collectors.toMap(PictureBookSeries::getId, Function.identity()));
        List<PictureBookTopic> pictureBookTopicList = pictureBookLoaderClient.loadAllPictureBookTopics();
        Map<String, PictureBookTopic> pictureBookTopicMap = pictureBookTopicList.stream()
                .collect(Collectors.toMap(PictureBookTopic::getId, Function.identity()));

        Map<String, PictureBook> pictureBookMap = pictureBookLoaderClient.loadPictureBooksIncludeDisabled(contentIdList);
        if (MapUtils.isNotEmpty(pictureBookMap)) {
            TeacherAssignmentRecord teacherAssignmentRecord = StringUtils.isBlank(bookId) ? null :
                    teacherAssignmentRecordLoader.loadTeacherAssignmentRecord(teacher.getSubject(), teacher.getId(), bookId);
            pictureBookList = pictureBookMap.values().stream()
                    .filter(Objects::nonNull)
                    .map(pictureBook -> NewHomeworkContentDecorator.decoratePictureBook(pictureBook, pictureBookSeriesMap, pictureBookTopicMap, null, teacherAssignmentRecord))
                    .filter(MapUtils::isNotEmpty)
                    .collect(Collectors.toList());
        }
        return MapUtils.m(
                "type", getObjectiveConfigType(),
                "typeName", getObjectiveConfigType().getValue(),
                "pictureBooks", pictureBookList);
    }
}
