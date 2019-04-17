package com.voxlearning.utopia.service.business.impl.service;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.buffer.ManagedNearBuffer;
import com.voxlearning.alps.api.buffer.NearBufferBuilder;
import com.voxlearning.alps.api.buffer.VersionedBufferData;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.cache.AtomicCallback;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.CannotAcquireLockException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.utopia.service.business.api.NewTeacherResourceService;
import com.voxlearning.utopia.service.business.api.entity.NewTeacherResource;
import com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask;
import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.entity.TeachingResourceCollect;
import com.voxlearning.utopia.service.business.api.mapper.NewTeacherResourceItemVo;
import com.voxlearning.utopia.service.business.api.mapper.NewTeacherResourceListVo;
import com.voxlearning.utopia.service.business.api.mapper.NewTeacherResourceWrapper;
import com.voxlearning.utopia.service.business.buffer.NewTeacherResourceWrapperBuffer;
import com.voxlearning.utopia.service.business.impl.dao.NewTeacherResourceDao;
import com.voxlearning.utopia.service.business.impl.dao.TeacherResourceTaskDao;
import com.voxlearning.utopia.service.business.impl.dao.TeachingResourceCollectDao;
import com.voxlearning.utopia.service.business.impl.dao.TeachingResourceDao;
import com.voxlearning.utopia.service.business.impl.dao.buffer.version.NewTeacherResourceWrapperVersion;
import com.voxlearning.utopia.service.business.impl.listener.BusinessMessagePublisher;
import com.voxlearning.utopia.service.business.impl.service.internal.InternalTeacherResourceRefService;
import com.voxlearning.utopia.service.business.impl.utils.NewTeacherResourceUtils;
import com.voxlearning.utopia.service.content.api.entity.NewBookProfile;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.TeachingResourceTask;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.text.Collator;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask.Status.EXPIRED;
import static com.voxlearning.utopia.service.business.api.entity.TeacherResourceTask.Status.FINISH;
import static java.util.stream.Collectors.toList;

@Named
@Slf4j
@ExposeService(interfaceClass = NewTeacherResourceService.class)
public class NewTeacherResourceServiceImpl implements NewTeacherResourceService, InitializingBean {

    @Inject
    private RaikouSDK raikouSDK;
    @Inject
    private TeachingResourceDao teachingResourceDao;
    @Inject
    private NewContentLoaderClient newContentLoaderClient;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private NewTeacherResourceWrapperVersion teacherResourceBufferVersion;
    @Inject
    private NewTeacherResourceDao newTeacherResourceDao;
    @Inject
    private TeachingResourceCollectDao teachingResourceCollectDao;
    @Inject
    private TeacherResourceTaskDao teacherResourceTaskDao;
    @Inject
    private BusinessMessagePublisher businessMessagePublisher;
    @Inject
    private InternalTeacherResourceRefService teacherResourceRefService;

    private ManagedNearBuffer<List<NewTeacherResourceWrapper>, NewTeacherResourceWrapperBuffer> teacherResourceBuffer;

    @Override
    public void afterPropertiesSet() throws Exception {
        NearBufferBuilder<List<NewTeacherResourceWrapper>, NewTeacherResourceWrapperBuffer> builder = NearBufferBuilder.newBuilder();
        builder.name("NewTeacherResourceWrapperBuffer");
        builder.category("SERVER");
        builder.nearBufferClass(NewTeacherResourceWrapperBuffer.class);
        builder.reloadNearBuffer(RuntimeMode.gt(Mode.STAGING) ? 10 : 1, TimeUnit.MINUTES);
        builder.eagerInitUnderProduction(true);
        builder.initializeNearBuffer(() -> {
            long version = teacherResourceBufferVersion.current();
            return new VersionedBufferData<>(version, loadTeacherCoursewareBufferData());
        });
        builder.reloadNearBuffer((oldVersion, attributes) -> {
            long currentVersion = teacherResourceBufferVersion.current();
            if (oldVersion < currentVersion) {
                return new VersionedBufferData<>(currentVersion, loadTeacherCoursewareBufferData());
            }
            return null;
        });
        teacherResourceBuffer = builder.build();
    }

    private List<NewTeacherResourceWrapper> loadTeacherCoursewareBufferData() {
        List<NewTeacherResourceWrapper> list = new ArrayList<>();
        for (NewTeacherResource resource : newTeacherResourceDao.loadAll()) {
            NewTeacherResourceWrapper wrapper = new NewTeacherResourceWrapper();
            BeanUtils.copyProperties(resource, wrapper);
            list.add(wrapper);
        }
        return list;
    }

    @Override
    public MapMessage loadSubjectClazzLevel(Long teacherId) {
        TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
        List<Map<String, String>> subjectMsg = getSubjectMsg(teacherDetail);
        List<Map<String, String>> clazzLevelMsg = getClazzLevelMsg(teacherDetail);
        List<Map<String, String>> coursewareSource = getCoursewareSource();

        return MapMessage.successMessage()
                .add("subject_list", subjectMsg)
                .add("clazz_level_list", clazzLevelMsg)
                .add("courseware_source", coursewareSource);
    }

    @Override
    public MapMessage loadBookList(Long teacherId, Integer subjectId, Integer clazzLevelId, Integer levelTerm) {
        // 直接返回全部
        if (clazzLevelId == null && levelTerm == null) {
            List<Map<Object, Object>> bookListMsg = new ArrayList<>();
            bookListMsg.add(MapUtils.map(
                    "book_id", "",
                    "book_name", "全部",
                    "book_short_name", "全部",
                    "book_alias", "全部",
                    "book_publisher", "全部",
                    "book_short_publisher", "全部",
                    "clazz_level", 0,
                    "term_type", 0
            ));
            return MapMessage.successMessage().add("book_list", bookListMsg);
        }

        Collator chinaSort = Collator.getInstance(Locale.CHINA);

        Set<String> bookIdSet = teacherResourceBuffer.getNativeBuffer().dump().getData().stream()
                .filter(NewTeacherResourceWrapper::getOnline)
                .map(NewTeacherResourceWrapper::getBookId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<NewBookProfile> newBookProfileList = newContentLoaderClient.loadBooksBySubjectId(subjectId).stream()
                .filter(NewBookProfile::isOnline)
                .filter(e -> bookIdSet.contains(e.getId()))
                .filter(e -> {
                    if (clazzLevelId == null) {
                        return true;
                    } else {
                        return Objects.equals(e.getClazzLevel(), clazzLevelId);
                    }
                })
                .filter(e -> {
                    if (levelTerm == null) {
                        return true;
                    } else {
                        return Objects.equals(e.getTermType(), levelTerm);
                    }
                })
                .sorted((o1, o2) -> chinaSort.compare(o1.getName(), o2.getName()))
                .collect(toList());

        List<Map<Object, Object>> bookListMsg = newBookProfileList.stream().map(book -> MapUtils.map(
                "book_id", book.getId(),
                "book_name", book.getName(),
                "book_short_name", book.getShortName(),
                "book_alias", book.getAlias(),
                "book_publisher", book.getPublisher(),
                "book_short_publisher", book.getShortPublisher(),
                "clazz_level", book.getClazzLevel(),
                "term_type", book.getTermType()
        )).collect(toList());

        return MapMessage.successMessage().add("book_list", bookListMsg);
    }

    @Override
    public MapMessage loadResource(Long teacherId, Integer subjectId, String bookId, Integer source, Integer page, Integer pageSize) {
        List<NewTeacherResourceWrapper> teacherResourceWrappers = teacherResourceBuffer.getNativeBuffer().dump().getData();

        // 如果不限制,用老师的全部学科过滤
        Set<Subject> subjects = new HashSet<>();
        if (subjectId == null) {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            if (teacherDetail != null) {
                subjects = new HashSet<>(teacherDetail.getSubjects());
            }
        } else {
            subjects.add(Subject.fromSubjectId(subjectId));
        }

        Set<Subject> finalSubjects = subjects;
        teacherResourceWrappers = teacherResourceWrappers.stream()
                .filter(NewTeacherResourceWrapper::getOnline)
                .filter(i -> finalSubjects.contains(i.getSubject()))
                .filter(i -> {
                    if (StringUtils.isNotBlank(bookId)) {
                        return Objects.equals(i.getBookId(), bookId);
                    }
                    return true;
                }).filter(i -> {
                    if (source != null) {
                        return Objects.equals(i.getSource(), source);
                    }
                    return true;
                }).sorted(NewTeacherResourceUtils.comparator).collect(toList());

        // 分页
        PageRequest pageable = new PageRequest(page - 1, pageSize);
        Page<NewTeacherResourceWrapper> resources = PageableUtils.listToPage(teacherResourceWrappers, pageable);
        List<NewTeacherResourceWrapper> content = resources.getContent();

        // 转出成页面的 model 填充阅读量
        List<NewTeacherResourceListVo> listVos = content.stream()
                .map(convertListVoFunction)
                .filter(Objects::nonNull)
                .collect(toList());

        return MapMessage.successMessage().add("data", listVos)
                .add("hasNext", resources.hasNext())
                .add("totalPages", resources.getTotalPages())
                .add("totalElements", resources.getTotalElements());
    }

    @Override
    public VersionedBufferData<List<NewTeacherResourceWrapper>> loadNewTeacherResourceWrapperBufferData(long version) {
        NewTeacherResourceWrapperBuffer nativeBuffer = teacherResourceBuffer.getNativeBuffer();
        if (version < 0 || version < nativeBuffer.getVersion()) {
            return nativeBuffer.dump();
        }
        return null;
    }

    @Override
    public void resetNewTeacherResourceWrapperBuffer() {
        teacherResourceBuffer.reset();
    }

    @Override
    public NewTeacherResource loadDetailById(String id) {
        return newTeacherResourceDao.load(id);
    }

    @Override
    public MapMessage loadDetailMsgById(String id, Long teacherId) {
        NewTeacherResource newTeacherResource = loadDetailById(id);
        NewTeacherResourceItemVo data = convertItemVo(newTeacherResource);
        if (data == null) {
            return MapMessage.errorMessage("资源不存在");
        }

        MapMessage msg = MapMessage.successMessage().add("data", data);

        boolean collected = false;                                                  // 是否已收藏
        boolean authStatus = false;                                                 // 认证状态
        boolean shareParent = false;                                                // 是否已分享家长
        TeacherResourceTask.Status taskStatus = TeacherResourceTask.Status.INIT;    // 任务状态

        // 是否已收藏
        if (teacherId != null) {
            List<TeachingResourceCollect> collects = teachingResourceCollectDao.loadAllByUser(teacherId);
            collected = collects.stream()
                    .filter(i -> Objects.equals(i.getCategory(), newTeacherResource.getCategory()))
                    .anyMatch(i -> Objects.equals(i.getResourceId(), newTeacherResource.getId()));

            // 老师认证状态
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);
            authStatus = Objects.equals(teacherDetail.getAuthenticationState(), AuthenticationState.SUCCESS.getState());

            shareParent = teacherResourceRefService.getShareParentStatus(teacherId, id);

            TeacherResourceTask task = loadTeacherTaskUpdateExpired(teacherId).stream().filter(i -> Objects.equals(i.getResourceId(), id))
                    .findFirst().orElse(null);
            if (task != null) {
                taskStatus = task.convertViewStatus();
            }
        }
        msg.put("collected", collected);
        msg.put("auth_status", authStatus);
        msg.put("share_parent", shareParent);
        msg.put("task_status", taskStatus);
        return msg;
    }

    @Override
    public void incrReadCount(String id, Long incr) {
        newTeacherResourceDao.incrReadCount(id, incr);
    }

    @Override
    public void incrCollectCount(String id, Long incr) {
        newTeacherResourceDao.incrCollectCount(id, incr);
    }

    @Override
    public void incrParticipateNum(String id, Long incr) {
        newTeacherResourceDao.incrParticipateNum(id, incr);
    }

    @Override
    public void incrFinishNum(String id, Long incr) {
        newTeacherResourceDao.incrFinishNum(id, incr);
    }

    @Override
    public MapMessage collect(String id, Long teacherId) {
        NewTeacherResourceWrapper wrapper = teacherResourceBuffer.getNativeBuffer().loadById(id);
        if (wrapper == null) {
            return MapMessage.errorMessage("资源不存在");
        }
        List<TeachingResourceCollect> collects = teachingResourceCollectDao.loadAllByUser(teacherId);
        TeachingResourceCollect saveModle = new TeachingResourceCollect();
        saveModle.setUserId(teacherId);
        saveModle.setCategory(wrapper.getCategory());
        saveModle.setResourceId(id);
        saveModle.setDisabled(false);
        if (collects.indexOf(saveModle) >= 0) {
            return MapMessage.errorMessage("不可重复收藏");
        }
        teachingResourceCollectDao.upsert(saveModle);
        newTeacherResourceDao.incrCollectCount(id, 1L);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage disableCollect(String id, Long teacherId) {
        NewTeacherResourceWrapper wrapper = teacherResourceBuffer.getNativeBuffer().loadById(id);
        if (wrapper == null) {
            return MapMessage.errorMessage("资源不存在");
        }

        TeachingResourceCollect collect = teachingResourceCollectDao.loadAllByUser(teacherId).stream()
                .filter(i -> Objects.equals(i.getCategory(), wrapper.getCategory())
                        && Objects.equals(i.getResourceId(), wrapper.getId()))
                .findFirst().orElse(null);

        if (collect != null) {
            collect.setDisabled(true);
            teachingResourceCollectDao.upsert(collect);
            incrCollectCount(id, -1L);
        }

        return MapMessage.successMessage();
    }

    private List<TeacherResourceTask> loadTeacherTaskUpdateExpired(Long teacherId) {
        final Date now = new Date();
        return teacherResourceTaskDao.loadTeacherTasks(teacherId)
                .stream()
                .map(task -> {
                    if (task.getTask() == null
                            || Objects.equals(task.getTask(), TeachingResourceTask.FREE.name())
                            || Objects.equals(task.getTask(), TeachingResourceTask.NONE.name())
                            || Objects.equals(task.getStatus(), FINISH.name())
                            || Objects.equals(task.getStatus(), EXPIRED.name())
                            || task.getExpiryDate() == null
                            || task.getExpiryDate().after(now)) {
                        return task;
                    } else {
                        teacherResourceTaskDao.updateTaskStatus(task.getId(), EXPIRED.name());
                        task.setStatus(EXPIRED.name());
                        return task;
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    public MapMessage receiveResource(String id, Long teacherId) {
        try {
            AtomicCallback<MapMessage> callback = () -> {
                NewTeacherResourceWrapper wrapper = teacherResourceBuffer.getNativeBuffer().loadById(id);
                if (wrapper == null) {
                    return MapMessage.errorMessage("资源不存在");
                }

                TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacherId);

                if (wrapper.getReceiveLimit() && !Objects.equals(teacherDetail.getAuthenticationState(), AuthenticationState.SUCCESS.getState())) {
                    return MapMessage.errorMessage("认证后才可领取");
                }

                List<TeacherResourceTask> teacherTaskList = loadTeacherTaskUpdateExpired(teacherId);

                TeacherResourceTask teacherTask = teacherTaskList.stream()
                        .filter(i -> Objects.equals(i.getResourceId(), id))
                        .findFirst().orElse(null);

                if (teacherTask == null) {
                    teacherTask = new TeacherResourceTask();
                    teacherTask.setUserId(teacherId);
                    teacherTask.setResourceId(id);
                    teacherTask.setTask(TeachingResourceTask.FREE.name());
                    teacherTask.setStatus(TeacherResourceTask.Status.FINISH.name());  // 尚未接入任务,全部默认成功
                    teacherResourceTaskDao.upsert(teacherTask);
                    return MapMessage.successMessage("领取成功").add("debug", "首次领取");
                } else {
                    if (Objects.equals(teacherTask.getStatus(), TeacherResourceTask.Status.FINISH.name())) {
                        return MapMessage.errorMessage("不可重复领取");
                    }

                    if (Objects.equals(teacherTask.getStatus(), TeacherResourceTask.Status.ONGOING.name())) {
                        return MapMessage.errorMessage("任务进行中...");
                    }

                    // 过期后重新开始
                    if (Objects.equals(teacherTask.getStatus(), TeacherResourceTask.Status.EXPIRED.name())) {
                        Integer validityPeriod = SafeConverter.toInt(wrapper.getValidityPeriod(), 15);
                        Date expiryDate = DateUtils.addDays(DateUtils.getDayEnd(new Date()), validityPeriod);
                        teacherTask.setStatus(TeacherResourceTask.Status.ONGOING.name());
                        teacherTask.setExpiryDate(expiryDate);
                        teacherResourceTaskDao.upsert(teacherTask);
                        return MapMessage.successMessage("领取成功").add("debug", "过期后重新开始");
                    }
                }
                return MapMessage.errorMessage();
            };
            return AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("NewTeacherResourceService:receiveResource")
                    .keys(teacherId)
                    .callback(callback)
                    .build()
                    .execute();
        } catch (CannotAcquireLockException e) {
            return MapMessage.errorMessage("请重试...");
        } catch (Exception e) {
            log.error("老师领取资源异常", e);
        }
        return MapMessage.errorMessage();
    }

    @Override
    public MapMessage shareParent(Long teacherId, String resourceId) {
        TeachingResource load = teachingResourceDao.load(resourceId);
        if (load == null) {
            return MapMessage.errorMessage("资源不存在");
        }

        boolean parentStatus = teacherResourceRefService.getShareParentStatus(teacherId, resourceId);
        if (parentStatus) {
            return MapMessage.errorMessage("不可重复分享");
        }
        teacherResourceRefService.shareParent(teacherId, resourceId);

        sendShareParentMsg(teacherId, resourceId);

        return MapMessage.successMessage();
    }

    @Override
    public Boolean getShareParent(Long teacherId, String resourceId) {
        return teacherResourceRefService.getShareParentStatus(teacherId, resourceId);
    }

    private void sendShareParentMsg(Long teacherId, String resourceId) {
        Map<String, Object> msgBody = new HashMap<>();
        msgBody.put("messageType", "shareParent");
        msgBody.put("teacherId", teacherId);
        msgBody.put("resourceId", resourceId);

        String jsonMsg = JsonUtils.toJson(msgBody);
        businessMessagePublisher.getTeacherResourcePublisher().publish(Message.newMessage().withPlainTextBody(jsonMsg));
    }

    private List<Map<String, String>> getSubjectMsg(TeacherDetail currentTeacher) {
        List<Map<String, String>> list = new ArrayList<>();

        Subject mainSubject = currentTeacher.getSubject();
        list.add(MapUtils.map("subject_id", mainSubject.getId(), "subject_name", mainSubject.getValue()));

        List<Subject> subjects = Arrays.asList(Subject.CHINESE, Subject.MATH, Subject.ENGLISH);
        for (Subject subject : subjects) {
            if (subject == mainSubject) continue;
            list.add(MapUtils.map("subject_id", subject.getId(), "subject_name", subject.getValue()));
        }

        return list;
    }

    private List<Map<String, String>> getClazzLevelMsg(TeacherDetail currentTeacher) {
        List<Map<String, String>> list = new ArrayList<>();

        list.add(MapUtils.map(
                "level_id", 0,
                "level_name", "全部",
                "level_term", 0)
        );

        List<ClazzLevel> clazzLevelList = Arrays.asList(
                ClazzLevel.FIRST_GRADE,
                ClazzLevel.SECOND_GRADE,
                ClazzLevel.THIRD_GRADE,
                ClazzLevel.FOURTH_GRADE,
                ClazzLevel.FIFTH_GRADE,
                ClazzLevel.SIXTH_GRADE
        );

        if (CollectionUtils.isNotEmpty(clazzLevelList)) {
            for (ClazzLevel integer : clazzLevelList) {
                list.add(MapUtils.map(
                        "level_id", integer.getLevel(),
                        "level_name", integer.getDescription() + "上册",
                        "level_term", 1)
                );
                list.add(MapUtils.map(
                        "level_id", integer.getLevel(),
                        "level_name", integer.getDescription() + "下册",
                        "level_term", 2)
                );
            }
        }

        return list;
    }

    private List<Map<String, String>> getCoursewareSource() {
        List<Map<String, String>> list = new ArrayList<>();
        list.add(MapUtils.map(
                "source_id", 0,
                "source_name", "课件大赛"
        ));
        list.add(MapUtils.map(
                "source_id", 1,
                "source_name", "一起作业"
        ));
        return list;
    }

    private List<Clazz> getTeacherClazz(Long mainTeacherId) {
        List<Long> teacherIds = teacherLoaderClient.loadSubTeacherIds(mainTeacherId);
        HashSet<Long> teacherIdSet = new HashSet<>(teacherIds);
        teacherIdSet.add(mainTeacherId);

        return teacherLoaderClient.loadTeachersClazzIds(teacherIdSet)
                .values()
                .stream()
                .flatMap(Collection::stream)
                .map(cId -> raikouSDK.getClazzClient().getClazzLoaderClient().loadClazz(cId))
                .filter(Objects::nonNull)
                .filter(Clazz::isPublicClazz)
                .filter(clazz -> !clazz.isTerminalClazz())
                .distinct()
                .sorted(Comparator.comparingInt(o -> o.getClazzLevel().getLevel()))
                .collect(toList());
    }

    private Function<NewTeacherResourceWrapper, NewTeacherResourceListVo> convertListVoFunction = wrapper -> {
        if (wrapper == null) {
            return null;
        }

        NewTeacherResource newTeacherResource = loadDetailById(wrapper.getId());
        if (newTeacherResource != null) {
            NewTeacherResourceListVo listVo = new NewTeacherResourceListVo();
            BeanUtils.copyProperties(wrapper, listVo);
            listVo.setReadCount(newTeacherResource.getReadCount());
            listVo.setCollectCount(newTeacherResource.getCollectCount());
            listVo.setParticipateNum(newTeacherResource.getParticipateNum());
            listVo.setFinishNum(newTeacherResource.getFinishNum());
            return listVo;
        }
        return null;
    };

    private NewTeacherResourceItemVo convertItemVo(NewTeacherResource newTeacherResource) {
        if (newTeacherResource == null) {
            return null;
        }

        NewTeacherResourceItemVo itemVo = new NewTeacherResourceItemVo();
        BeanUtils.copyProperties(newTeacherResource, itemVo);

        if (itemVo.getAuthorId() == null) {
            itemVo.setAuthorName("小柒老师");
            itemVo.setAuthorSchoolName("一起作业");
        } else {
            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(itemVo.getAuthorId());
            if (teacherDetail != null) {
                itemVo.setAuthorName(teacherDetail.fetchRealname());
                itemVo.setAuthorSchoolId(teacherDetail.getTeacherSchoolId());
                itemVo.setAuthorSchoolName(teacherDetail.getTeacherSchoolName());
            }
        }
        return itemVo;
    }
}
