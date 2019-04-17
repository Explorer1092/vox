package com.voxlearning.utopia.service.parent.homework.impl.template.bookList;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupStudentTuple;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.api.entity.NewClazzBookRef;
import com.voxlearning.utopia.service.content.consumer.NewClazzBookLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.template.base.Processors;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.AbstractSubProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * 构建返回值
 */
@Named
@Processors({
        ExamResultProcessorSub.class,
        MentalResultProcessorSub.class
})
public class ResultBookProcessor extends AbstractSubProcessor implements HomeworkProcessor {

    @Inject private RaikouSDK raikouSDK;

    @Inject private GroupLoaderClient groupLoaderClient;
    @Inject private NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject private NewContentLoaderClient newContentLoaderClient;
    @Inject private StudentLoaderClient studentLoaderClient;

    @Override
    public void process(HomeworkContext hc) {
        HomeworkParam homeworkParam = hc.getHomeworkParam();
        List<String> bookIds = hc.getBookIds();
        if (CollectionUtils.isEmpty(bookIds)) {
            hc.setMapMessage(MapMessage.errorMessage("没有获取到教材"));
            return;
        }
        Consumer processor = subProcessor.get(homeworkParam.getBizType());
        if (processor != null) {
            processor.accept(hc);
        }

        Map<String, Object> data = hc.getData();
        int clazzLevel = SafeConverter.toInt(homeworkParam.getData().get("clazzLevel"));
        List<Map<String, Object>> bookList = ObjectUtils.get(() -> (List<Map<String, Object>>) data.get("bookList"));
        if (CollectionUtils.isEmpty(bookList)) {
            hc.setMapMessage(MapMessage.errorMessage("未获取到教材"));
            return;
        }
        // 默认教材
        String defaultBookId = ObjectUtils.get(hc::getBookId,
                loadDefaultBook(homeworkParam.getStudentId(), homeworkParam.getSubject(), clazzLevel, SafeConverter.toInt(homeworkParam.getData().get("regionCode"))));

        String finalDefaultBookId = defaultBookId;
        if (bookList.stream().noneMatch(book -> book.get("value").equals(finalDefaultBookId))) {
            NewBookProfile newBookProfile;
            if (StringUtils.isNotBlank(defaultBookId) && (newBookProfile = newContentLoaderClient.loadBook(defaultBookId)) != null) {
                bookList.add(0, MapUtils.m("value", newBookProfile.getId(), "label", newBookProfile.getName()));
            }
            defaultBookId = SafeConverter.toString(bookList.get(0).get("value"));
        }
        // 选择的教材
        data.put("selectedBookId", defaultBookId);
        // 教材列表
        data.put("bookList", bookList);
        hc.setData(data);
    }


    /**
     * 获取默认教材
     *
     * @param studentId  学生Id
     * @param subject    学科
     * @param clazzLevel 年级
     * @param regionCode 区域code
     * @return
     */
    private String loadDefaultBook(Long studentId, String subject, Integer clazzLevel, Integer regionCode) {
        StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(studentId);
        String bookId = null;
        if (studentDetail.getClazzId() != null) {
            List<Long> groupIds = raikouSDK.getClazzClient()
                    .getGroupStudentTupleServiceClient()
                    .findByStudentId(studentId)
                    .stream()
                    .filter(e -> e.getGroupId() != null)
                    .map(GroupStudentTuple::getGroupId)
                    .distinct()
                    .collect(Collectors.toList());
            Group group = ObjectUtils.get(() ->
                    groupLoaderClient.getGroupLoader()
                            .loadGroups(groupIds)
                            .getUninterruptibly()
                            .values()
                            .stream()
                            .filter(e -> !e.isDisabledTrue())
                            .filter(e -> e.getSubject() != null)
                            .filter(g -> Objects.equals(g.getSubject().name(), subject))
                            .findFirst()
                            .orElse(null));
            List<NewClazzBookRef> refs;
            if (group != null && !(refs = newClazzBookLoaderClient.loadGroupBookRefs(group.getId()).toList()).isEmpty()) {
                List<NewClazzBookRef> sortRef = refs.stream()
                        .sorted((o1, o2) -> Long.compare(o2.fetchUpdateTimestamp(), o1.fetchUpdateTimestamp()))
                        .collect(Collectors.toList());
                bookId = sortRef.get(0).getBookId();
            }
        }
        if (bookId == null) {
            Term term = SchoolYear.newInstance().currentTerm();
            NewBookProfile book = ObjectUtils.get(() -> newContentLoaderClient.
                    loadBooksByClassLevelWithSortByUpdateTime(Subject.safeParse(subject), ClazzLevel.parse(clazzLevel), regionCode).
                    stream().filter(e -> Objects.equals(e.getTermType(), term.getKey())).findFirst().get());
            bookId = ObjectUtils.get(() -> book.getId());
        }
        return bookId;
    }
}
