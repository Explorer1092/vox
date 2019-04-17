package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.TeacherNewTermPlanPointService;
import com.voxlearning.utopia.service.campaign.api.entity.NewTermPlanActivity;
import com.voxlearning.utopia.service.campaign.impl.dao.UserNewTermPlanActivityRefDao;
import com.voxlearning.utopia.service.user.api.entities.StudentParentRef;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Named
@Slf4j
@ExposeService(interfaceClass = TeacherNewTermPlanPointService.class)
public class TeacherNewTermPlanPointServiceImpl implements TeacherNewTermPlanPointService {

    @Inject
    private UserNewTermPlanActivityRefDao userNewTermPlanActivityRefDao;
    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Override
    public NewTermPlanActivity load(Long userId) {
        return userNewTermPlanActivityRefDao.load(userId);
    }

    @Override
    public MapMessage delete(Long userId) {
        userNewTermPlanActivityRefDao.remove(userId);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage studentShow(Long userId) {
        try {
            NewTermPlanActivity ref = userNewTermPlanActivityRefDao.load(userId);
            boolean update = false;
            if (ref == null) {
                ref = new NewTermPlanActivity(userId);
            }
            if (ref.getStudentShowActivity() == null) {
                ref.setStudentShowActivity(new Date());
                update = true;
            }
            if (update) {
                userNewTermPlanActivityRefDao.upsert(ref);
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage teacherShow(Long userId) {
        try {
            NewTermPlanActivity ref = userNewTermPlanActivityRefDao.load(userId);
            boolean update = false;
            if (ref == null) {
                ref = new NewTermPlanActivity(userId);
            }
            if (ref.getTeacherShowActivity() == null) {
                ref.setTeacherShowActivity(new Date());
                update = true;
            }
            if (update) {
                userNewTermPlanActivityRefDao.upsert(ref);
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage parentShow(Long parentId, Long studentId) {
        try {
            NewTermPlanActivity ref = userNewTermPlanActivityRefDao.load(parentId);
            boolean update = false;
            if (ref == null) {
                ref = new NewTermPlanActivity(parentId);
            }
            if (ref.getParentShowActivity() == null) {
                ref.setParentShowActivity(new Date());
                update = true;
            }
            if (update) {
                userNewTermPlanActivityRefDao.upsert(ref);
            }

            if (studentId != null) {
                List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
                for (StudentParentRef studentParentRef : studentParentRefs) {
                    if (Objects.equals(studentParentRef.getStudentId(), studentId)) {
                        NewTermPlanActivity load = userNewTermPlanActivityRefDao.load(studentId);
                        boolean studentUpdate = false;
                        if (load == null) {
                            load = new NewTermPlanActivity(studentId);
                        }
                        if (load.getStudentParentShow() == null) {
                            load.setStudentParentShow(new Date());
                            studentUpdate = true;
                        }
                        if (studentUpdate) {
                            userNewTermPlanActivityRefDao.upsert(load);
                        }
                    }
                }
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage studentClickGoAssignBtn(Long userId) {
        try {
            NewTermPlanActivity ref = userNewTermPlanActivityRefDao.load(userId);
            boolean update = false;
            if (ref == null) {
                ref = new NewTermPlanActivity(userId);
            }
            if (ref.getStudentClickGoAssignBtn() == null) {
                ref.setStudentClickGoAssignBtn(new Date());
                update = true;
            }
            if (update) {
                userNewTermPlanActivityRefDao.upsert(ref);
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage parentClickGoAssignBtn(Long parentId) {
        try {
            NewTermPlanActivity ref = userNewTermPlanActivityRefDao.load(parentId);
            boolean update = false;
            if (ref == null) {
                ref = new NewTermPlanActivity(parentId);
            }
            if (ref.getParentClickGoAssign() == null) {
                ref.setParentClickGoAssign(new Date());
                update = true;
            }
            if (update) {
                userNewTermPlanActivityRefDao.upsert(ref);
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    @Override
    public MapMessage parentAssign(Long parentId, Long studentId) {
        try {
            NewTermPlanActivity ref = userNewTermPlanActivityRefDao.load(parentId);
            boolean update = false;
            if (ref == null) {
                ref = new NewTermPlanActivity(parentId);
            }
            if (ref.getParentAssign() == null) {
                ref.setParentAssign(new Date());
                update = true;
            }
            if (update) {
                userNewTermPlanActivityRefDao.upsert(ref);
            }

            if (studentId != null) {
                NewTermPlanActivity load = userNewTermPlanActivityRefDao.load(studentId);
                if (load == null) {
                    load = new NewTermPlanActivity(studentId);
                }
                boolean studentUpdate = false;
                if (load.getStudentAssign() == null) {
                    load.setStudentAssign(new Date());
                    studentUpdate = true;
                }
                if (studentUpdate) {
                    userNewTermPlanActivityRefDao.upsert(load);
                }
            }
            return MapMessage.successMessage();
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return MapMessage.errorMessage();
        }
    }

    // 老师布置时调用
    void teacherAssign(Long userId) {
        try {
            NewTermPlanActivity ref = userNewTermPlanActivityRefDao.load(userId);
            boolean update = false;
            if (ref == null) {
                ref = new NewTermPlanActivity(userId);
            }
            if (ref.getTeacherClickAssignBtn() == null) {
                ref.setTeacherClickAssignBtn(new Date());
                update = true;
            }
            if (update) {
                userNewTermPlanActivityRefDao.upsert(ref);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
