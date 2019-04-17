package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.newhomework.api.OfflineHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomework;
import com.voxlearning.utopia.service.newhomework.api.entity.OfflineHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkBook;
import com.voxlearning.utopia.service.newhomework.impl.dao.OfflineHomeworkDao;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.vendor.api.entity.OfflineHomeworkSignRecord;
import com.voxlearning.utopia.service.vendor.consumer.JxtLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2016/9/8
 */
@Named
@Service(interfaceClass = OfflineHomeworkLoader.class)
@ExposeService(interfaceClass = OfflineHomeworkLoader.class)
public class OfflineHomeworkLoaderImpl implements OfflineHomeworkLoader {
    @Inject
    private OfflineHomeworkDao offlineHomeworkDao;
    @Inject
    private NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    private JxtLoaderClient jxtLoaderClient;
    @Inject
    private StudentLoaderClient studentLoaderClient;

    @Override
    public OfflineHomework loadOfflineHomework(String id) {
        return offlineHomeworkDao.load(id);
    }

    @Override
    public Map<String, OfflineHomework> loadOfflineHomeworks(Collection<String> ids) {
        return offlineHomeworkDao.loads(ids);
    }

    @Override
    public Map<String, OfflineHomework> loadByNewHomeworkIds(Collection<String> newHomeworkIds) {
        return offlineHomeworkDao.loadByNewHomeworkIds(newHomeworkIds);
    }

    @Override
    public Map<Long, List<OfflineHomework>> loadGroupOfflineHomeworks(Collection<Long> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return Collections.emptyMap();
        }
        return offlineHomeworkDao.loadGroupOfflineHomeworks(groupIds);
    }

    @Override
    public Page<OfflineHomework> loadGroupOfflineHomeworks(Collection<Long> groupIds, Date startDate, Date endDate, Pageable pageable) {
        if (CollectionUtils.isEmpty(groupIds)) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return offlineHomeworkDao.loadGroupOfflineHomeworks(groupIds, startDate, endDate, pageable);
    }

    @Override
    public MapMessage loadOfflineHomeworkDetail(String offlineHomeworkId) {
        OfflineHomework offlineHomework = offlineHomeworkDao.load(offlineHomeworkId);
        if (offlineHomework == null) {
            return MapMessage.errorMessage("错误的作业单id");
        }
        String newHomeworkId = offlineHomework.getNewHomeworkId();
        NewHomework newHomework = newHomeworkLoader.loadNewHomework(newHomeworkId);
        NewHomeworkBook newHomeworkBook = newHomeworkLoader.loadNewHomeworkBook(newHomeworkId);
        MapMessage mapMessage = buildOfflineHomeworkMapper(offlineHomework, newHomework, newHomeworkBook);
        // 签字列表
        if (SafeConverter.toBoolean(offlineHomework.getNeedSign())) {
            Map<String, List<OfflineHomeworkSignRecord>> offlineHomeworkSignMap = jxtLoaderClient.getSignRecordByOfflineHomeworkIds(Collections.singleton(offlineHomeworkId));
            List<OfflineHomeworkSignRecord> offlineHomeworkSignRecords = offlineHomeworkSignMap.containsKey(offlineHomeworkId) ? offlineHomeworkSignMap.get(offlineHomeworkId) : new ArrayList<>();
            Map<Long, String> studentVoiceMap = offlineHomeworkSignRecords.stream()
                    .sorted((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()))
                    .collect(Collectors.toMap(OfflineHomeworkSignRecord::getStudentId, OfflineHomeworkSignRecord::getVoiceUrl, (u, v) -> u, LinkedHashMap::new));
            Map<Long, String> groupStudents = studentLoaderClient.loadGroupStudents(offlineHomework.getClazzGroupId())
                    .stream()
                    .filter(u -> StringUtils.isNotBlank(u.fetchRealname()))
                    .collect(Collectors.toMap(User::getId, User::fetchRealname, (u, v) -> u, LinkedHashMap::new));
            List<Map<String, Object>> signedStudents = new ArrayList<>();
            List<Map<String, Object>> unsignedStudents = new ArrayList<>();
            for (Map.Entry<Long, String> student : groupStudents.entrySet()) {
                if (studentVoiceMap.containsKey(student.getKey())) {
                    signedStudents.add(MiscUtils.m("studentName", student.getValue(), "voiceUrl", studentVoiceMap.get(student.getKey())));
                } else {
                    unsignedStudents.add(MiscUtils.m("studentName", student.getValue(), "voiceUrl", ""));
                }
            }
            mapMessage.add("signedStudents", signedStudents);
            mapMessage.add("unsignedStudents", unsignedStudents);
        }
        return mapMessage;
    }

    private MapMessage buildOfflineHomeworkMapper(OfflineHomework offlineHomework, NewHomework newHomework, NewHomeworkBook newHomeworkBook) {
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.add("subject", offlineHomework.getSubject());
        mapMessage.add("subjectName", offlineHomework.getSubject().getValue());
        mapMessage.add("endTime", DateUtils.dateToString(offlineHomework.getEndTime()));
        if (newHomework != null && newHomeworkBook != null && newHomeworkBook.getPractices() != null) {
            Map<String, Object> newHomeworkContents = new HashMap<>();
            newHomeworkContents.put("duration", newHomework.getDuration());
            Set<String> unitNames = new LinkedHashSet<>();
            for (ObjectiveConfigType type : newHomeworkBook.getPractices().keySet()) {
                unitNames.addAll(newHomeworkBook.getPractices().get(type).stream().map(NewHomeworkBookInfo::getUnitName).collect(Collectors.toList()));
            }
            String units = StringUtils.join(unitNames, ",");
            newHomeworkContents.put("units", units);
            List<String> objectiveConfigList = newHomework.getPractices()
                    .stream()
                    .map(newHomeworkPracticeContent -> newHomeworkPracticeContent.getType().getValue())
                    .collect(Collectors.toList());
            newHomeworkContents.put("objectiveConfigs", objectiveConfigList);
            newHomeworkContents.put("newHomeworkId", newHomework.getId());
            mapMessage.add("newHomeworkContents", newHomeworkContents);
        }
        List<OfflineHomeworkPracticeContent> practiceContents = offlineHomework.getPractices();
        //兼容没有线下作业
        if (practiceContents == null){
            practiceContents = Collections.emptyList();
        }
        List<String> offlineHomeworkContentList = practiceContents.stream()
                .map(OfflineHomeworkPracticeContent::toString)
                .collect(Collectors.toList());
        mapMessage.add("offlineHomeworkContents", offlineHomeworkContentList);
        mapMessage.add("needSign", offlineHomework.getNeedSign());
        String teacherName = offlineHomework.getTeacherName();
        mapMessage.add("teacherFirstName", StringUtils.isBlank(teacherName) ? "" : teacherName.substring(0, 1));
        return mapMessage;
    }

}
