package com.voxlearning.utopia.agent.service.memorandum;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.constants.AgentMemorandumGenre;
import com.voxlearning.utopia.agent.constants.MemorandumType;
import com.voxlearning.utopia.agent.dao.mongo.AgentMemorandumDao;
import com.voxlearning.utopia.agent.persist.entity.memorandum.AgentMemorandum;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.agent.service.taskmanage.AgentTaskManageService;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.client.AsyncTeacherServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by yaguang.wang
 * on 2017/5/10.
 */
@Named
public class AgentMemorandumService extends AbstractAgentService {
    @Inject private AgentMemorandumDao memorandumDao;
    @Inject private AsyncTeacherServiceClient asyncTeacherServiceClient;

    @Inject private AgentTaskManageService agentTaskManageService;
    public static final Integer PAGESIZE = 20;

    public List<AgentMemorandum> loadAll(Integer page, Integer pageSize) {
        return memorandumDao.findAll(page, pageSize);
    }

    public AgentMemorandum load(String memorandumId) {
        AgentMemorandum agentMemorandum = memorandumDao.load(memorandumId);
        if (agentMemorandum != null && !SafeConverter.toBoolean(agentMemorandum.getDisabled())) {
            return agentMemorandum;
        }
        return null;
    }

    public Map<String, AgentMemorandum> loads(Collection<String> memorandumIds) {
        return memorandumDao.loads(memorandumIds);
    }

    public List<AgentMemorandum> loadMemorandumByIntoSchoolRecordId(String intoSchoolRecordId) {
        return memorandumDao.findByIntoSchoolRecordId(intoSchoolRecordId);
    }
    public List<AgentMemorandum> loadMemorandumBySchoolId(Long schoolId) {
        return memorandumDao.findBySchoolId(schoolId);
    }

    public List<AgentMemorandum> loadMemorandumByTeacherId(Long teacherId) {
        return memorandumDao.findByTeacherId(teacherId);
    }

    public List<AgentMemorandum> loadMemorandumByUserId(Long userId) {
        return memorandumDao.findByCreateUserId(userId);
    }

    private MapMessage addMemorandum(AgentMemorandum memorandum) {
        memorandum.setWriteTime(new Date());
        memorandumDao.insert(memorandum);
        if(memorandum.getTeacherId() != null && memorandum.getTeacherId() > 0){
            AlpsThreadPool.getInstance().submit(() -> agentTaskManageService.setSubTaskFollowForListener(memorandum.getTeacherId()));
        }
        if (StringUtils.isNoneBlank(memorandum.getId())) {
            return MapMessage.successMessage().add("id", memorandum.getId());
        }
        return MapMessage.errorMessage("添加失败");
    }

    private MapMessage updateMemorandum(AgentMemorandum memorandum) {
        memorandum.setWriteTime(new Date());
        memorandum = memorandumDao.replace(memorandum);
        if (memorandum != null) {
            return MapMessage.successMessage().add("memorandum", memorandum);
        }
        return MapMessage.errorMessage("修改失败");
    }

    public MapMessage updateMemorandumNoWriteTime(AgentMemorandum memorandum) {
        memorandum = memorandumDao.replace(memorandum);
        if (memorandum != null) {
            return MapMessage.successMessage().add("memorandum", memorandum);
        }
        return MapMessage.errorMessage("修改失败");
    }

    private MapMessage deleteMemorandum(String id) {
        if (memorandumDao.deleteAgentMemorandum(id)) {
            return MapMessage.successMessage().add("id", id);
        }
        return MapMessage.errorMessage("删除失败");
    }

    public MapMessage addMemorandum(Long userId, Long schoolId, Long teacherId, String content, MemorandumType type,String url) {
        return addMemorandum(userId, schoolId, teacherId, content, type, null,url);
    }

    public MapMessage addMemorandum(Long userId, Long schoolId, Long teacherId, String content, MemorandumType type, String intoSchoolRecordId,String url) {
        if (userId == null) {
            return MapMessage.errorMessage("用户已退出请重新登录");
        }

        if (type == null) {
            return MapMessage.errorMessage("类别保存错误");
        }
        if (StringUtils.isBlank(content) && type == MemorandumType.PICTURE) {
            return MapMessage.errorMessage("图片上传失败");
        }
        if (StringUtils.isBlank(content) && type == MemorandumType.TEXT) {
            return MapMessage.errorMessage("备注信息不能为空");
        }
        if (StringUtils.isBlank(content) && StringUtils.isBlank(url) && type == MemorandumType.IMAGE_TEXT) {
            return MapMessage.errorMessage("备注图片不能全为空");
        }
        if (teacherId != 0L) {
            School school = asyncTeacherServiceClient.getAsyncTeacherService()
                    .loadTeacherSchool(teacherId)
                    .getUninterruptibly();
            if (school != null) {
                schoolId = school.getId();
            }
        }
        if (schoolId == 0L) {
            return MapMessage.errorMessage("学校备注信息的学校Id未找到");
        }
        AgentMemorandum memorandum = new AgentMemorandum();
        memorandum.setCreateUserId(userId);
        memorandum.setSchoolId(schoolId);
        if (teacherId != 0L) {
            memorandum.setTeacherId(teacherId);
            memorandum.setGenre(AgentMemorandumGenre.TEACHER);
        } else {
            memorandum.setGenre(AgentMemorandumGenre.SCHOOL);
        }
        memorandum.setContent(content);
        memorandum.setType(type);
        memorandum.setDisabled(false);
        memorandum.setUrl(url);
        memorandum.setIntoSchoolRecordId(intoSchoolRecordId);
        return addMemorandum(memorandum);
    }

    public MapMessage updateMemorandum(Long userId, String memorandumId, String content) {
        if (userId == null) {
            return MapMessage.errorMessage("用户已退出请重新登录");
        }
        AgentMemorandum memorandum = load(memorandumId);
        if (memorandum == null || SafeConverter.toBoolean(memorandum.getDisabled())) {
            return MapMessage.errorMessage("未找到需要修改的信息");
        }
        if (!Objects.equals(memorandum.getCreateUserId(), userId)) {
            return MapMessage.errorMessage("这份信息不属于你，你无法修改");
        }
        if (memorandum.getType() == MemorandumType.PICTURE && StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("照片上传失败");
        }
        if (memorandum.getType() == MemorandumType.TEXT && StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("备注内容不能为空");
        }
        memorandum.setContent(content);
        return updateMemorandum(memorandum);
    }

    public MapMessage deleteMemorandum(Long userId, String memorandumId) {
        if (userId == null) {
            return MapMessage.errorMessage("用户已退出请重新登录");
        }
        AgentMemorandum memorandum = load(memorandumId);
        if (memorandum == null || SafeConverter.toBoolean(memorandum.getDisabled())) {
            return MapMessage.errorMessage("未找到需要修改的信息");
        }
        if (!Objects.equals(memorandum.getCreateUserId(), userId)) {
            return MapMessage.errorMessage("这份信息不属于你，你无法删除");
        }
        return deleteMemorandum(memorandumId);
    }

    public Long countSchoolIdMemorandum(Long schoolId, MemorandumType type) {
        return loadMemorandumBySchoolId(schoolId).stream().filter(p -> Objects.equals(type, p.getType())).count();
    }

    public Long countTeacherIdMemorandum(Long teacherId,MemorandumType type){
        return loadMemorandumByTeacherId(teacherId).stream().filter(p -> Objects.equals(type, p.getType())).count();
    }

    public AgentMemorandum loadMemorandumByTeacherIdFirstOne(Long teacherId, MemorandumType type) {
        return memorandumDao.findByTeacherIdLimitPage(teacherId, 1, 1, type).stream().findFirst().orElse(null);
    }

    public List<AgentMemorandum> loadMemorandumBySchoolIdPage(Long schoolId, Integer page, MemorandumType type) {
        return memorandumDao.findBySchoolIdLimitPage(schoolId, page, PAGESIZE, type);
    }

    public List<AgentMemorandum> loadMemorandumByUserIdPage(Long userId, Integer page, MemorandumType type, Date startTime, Date endTime) {
        return memorandumDao.findByCreateUserIdLimitPage(userId, page, PAGESIZE, type, startTime, endTime);
    }

    public List<AgentMemorandum> loadMemorandumByTeacherIdPage(Long teacherId, Integer page, MemorandumType type) {
        return memorandumDao.findByTeacherIdLimitPage(teacherId, page, PAGESIZE, type);
    }

    public List<AgentMemorandum> loadMemorandumByTeacherId(Long teacherId,MemorandumType type, Date startTime, Date endTime) {
        List<AgentMemorandum> memorandums = memorandumDao.findByTeacherId(teacherId);
        return memorandums.stream()
                .filter(p -> type == null ||p.getType() == type)
                .filter(p -> startTime == null || startTime.before(p.getWriteTime()))
                .filter(p -> endTime == null || endTime.after(p.getWriteTime()))
                .collect(Collectors.toList());
    }

    /**
     * 查询第一个
     * @param schoolId
     * @param type
     * @return
     */
    public AgentMemorandum loadFirstMemorandumBySchoolId(Long schoolId, MemorandumType type){
        return memorandumDao.findBySchoolId(schoolId).stream().filter(p -> p.getType() == type).findFirst().orElse(null);
    }
}
