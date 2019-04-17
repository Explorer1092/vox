package com.voxlearning.utopia.service.campaign.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.campaign.api.WarmHeartPlanPointService;
import com.voxlearning.utopia.service.campaign.api.WarmHeartPlanService;
import com.voxlearning.utopia.service.campaign.api.entity.WarmHeartPlanActivity;
import com.voxlearning.utopia.service.campaign.impl.dao.WarmHeartPlanActivityDao;
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
@ExposeService(interfaceClass = WarmHeartPlanPointService.class)
public class WarmHeartPlanPointServiceImpl implements WarmHeartPlanPointService {

    @Inject
    private ParentLoaderClient parentLoaderClient;
    @Inject
    private WarmHeartPlanActivityDao warmHeartPlanActivityDao;

    @Override
    public WarmHeartPlanActivity load(Long userId) {
        return warmHeartPlanActivityDao.load(userId);
    }

    @Override
    public MapMessage delete(Long userId) {
        warmHeartPlanActivityDao.remove(userId);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage studentShow(Long userId) {
        try {
            WarmHeartPlanActivity ref = warmHeartPlanActivityDao.load(userId);
            boolean update = false;
            if (ref == null) {
                ref = new WarmHeartPlanActivity(userId);
            }
            if (ref.getStudentShowActivity() == null) {
                ref.setStudentShowActivity(new Date());
                update = true;
            }
            if (update) {
                warmHeartPlanActivityDao.upsert(ref);
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
            WarmHeartPlanActivity ref = warmHeartPlanActivityDao.load(userId);
            boolean update = false;
            if (ref == null) {
                ref = new WarmHeartPlanActivity(userId);
            }
            if (ref.getTeacherShowActivity() == null) {
                ref.setTeacherShowActivity(new Date());
                update = true;
            }
            if (update) {
                warmHeartPlanActivityDao.upsert(ref);
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
            WarmHeartPlanActivity ref = warmHeartPlanActivityDao.load(parentId);
            boolean update = false;
            if (ref == null) {
                ref = new WarmHeartPlanActivity(parentId);
            }
            if (ref.getParentShowActivity() == null) {
                ref.setParentShowActivity(new Date());
                update = true;
            }
            if (update) {
                warmHeartPlanActivityDao.upsert(ref);
            }

            if (studentId != null) {
                List<StudentParentRef> studentParentRefs = parentLoaderClient.loadParentStudentRefs(parentId);
                for (StudentParentRef studentParentRef : studentParentRefs) {
                    if (Objects.equals(studentParentRef.getStudentId(), studentId)) {
                        WarmHeartPlanActivity load = warmHeartPlanActivityDao.load(studentId);
                        boolean studentUpdate = false;
                        if (load == null) {
                            load = new WarmHeartPlanActivity(studentId);
                        }
                        if (load.getStudentParentShow() == null) {
                            load.setStudentParentShow(new Date());
                            studentUpdate = true;
                        }
                        if (studentUpdate) {
                            warmHeartPlanActivityDao.upsert(load);
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
            WarmHeartPlanActivity ref = warmHeartPlanActivityDao.load(userId);
            boolean update = false;
            if (ref == null) {
                ref = new WarmHeartPlanActivity(userId);
            }
            if (ref.getStudentClickGoAssignBtn() == null) {
                ref.setStudentClickGoAssignBtn(new Date());
                update = true;
            }
            if (update) {
                warmHeartPlanActivityDao.upsert(ref);
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
            WarmHeartPlanActivity ref = warmHeartPlanActivityDao.load(parentId);
            boolean update = false;
            if (ref == null) {
                ref = new WarmHeartPlanActivity(parentId);
            }
            if (ref.getParentClickGoAssign() == null) {
                ref.setParentClickGoAssign(new Date());
                update = true;
            }
            if (update) {
                warmHeartPlanActivityDao.upsert(ref);
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
            WarmHeartPlanActivity ref = warmHeartPlanActivityDao.load(parentId);
            boolean update = false;
            if (ref == null) {
                ref = new WarmHeartPlanActivity(parentId);
            }
            if (ref.getParentAssign() == null) {
                ref.setParentAssign(new Date());
                update = true;
            }
            if (update) {
                warmHeartPlanActivityDao.upsert(ref);
            }

            if (studentId != null) {
                WarmHeartPlanActivity load = warmHeartPlanActivityDao.load(studentId);
                if (load == null) {
                    load = new WarmHeartPlanActivity(studentId);
                }
                boolean studentUpdate = false;
                if (load.getStudentAssign() == null) {
                    load.setStudentAssign(new Date());
                    studentUpdate = true;
                }
                if (studentUpdate) {
                    warmHeartPlanActivityDao.upsert(load);
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
            WarmHeartPlanActivity ref = warmHeartPlanActivityDao.load(userId);
            boolean update = false;
            if (ref == null) {
                ref = new WarmHeartPlanActivity(userId);
            }
            if (ref.getTeacherClickAssignBtn() == null) {
                ref.setTeacherClickAssignBtn(new Date());
                update = true;
            }
            if (update) {
                warmHeartPlanActivityDao.upsert(ref);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
