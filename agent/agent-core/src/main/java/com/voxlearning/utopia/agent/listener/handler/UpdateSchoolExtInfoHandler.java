package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.mockexam.integration.StringUtil;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by yaguang.wang
 * on 2017/8/31.
 */
@Named
public class UpdateSchoolExtInfoHandler {
    @Inject private SchoolExtServiceClient schoolExtServiceClient;
    @Inject
    private SchoolLoaderClient schoolLoaderClient;

    public void handle(Long schoolId) {
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (schoolExtInfo == null || schoolExtInfo.getGradeStudentCount() == null) {
            return;
        }

        EduSystemType eduSystemType = schoolExtInfo.fetchEduSystem();
        if(eduSystemType == null){
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            if(school == null){
                return;
            }
            eduSystemType = EduSystemType.of(school.getDefaultEduSystemType());
        }

        String[] gradeArr =StringUtils.split(eduSystemType.getCandidateClazzLevel(), ",");

        Integer schoolSize = 0;
        for(String grade : gradeArr){
            ClazzLevel clazzLevel = ClazzLevel.parse(SafeConverter.toInt(grade));
            if(clazzLevel != null){
                schoolSize += SafeConverter.toInt(schoolExtInfo.fetchGradeStudentNum(clazzLevel, null));
            }
        }

        if(!Objects.equals(schoolExtInfo.getSchoolSize(), schoolSize)){
            schoolExtInfo.setSchoolSize(schoolSize);
            schoolExtServiceClient.getSchoolExtService().updateSchoolExtInfo(schoolExtInfo);
        }
    }

    public void handle2(Long schoolId) {
        SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(schoolId).getUninterruptibly();
        if (schoolExtInfo == null || (schoolExtInfo.getGradeStudentCount() != null && schoolExtInfo.getGradeClazzCount() != null)) {
            return;
        }

        EduSystemType eduSystemType = schoolExtInfo.fetchEduSystem();
        if(eduSystemType == null){
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            if(school == null){
                return;
            }
            eduSystemType = EduSystemType.of(school.getDefaultEduSystemType());
        }

        String[] gradeArr =StringUtils.split(eduSystemType.getCandidateClazzLevel(), ",");


        boolean needUpdGradeStuData = false;
        if(schoolExtInfo.getGradeStudentCount() == null){
            needUpdGradeStuData = true;
        }

        boolean needUpdGradeClazzData = false;
        if(schoolExtInfo.getGradeClazzCount() == null){
            needUpdGradeClazzData = true;
        }

        boolean updated = false;
        Integer schoolSize = 0;
        for(String grade : gradeArr){
            ClazzLevel clazzLevel = ClazzLevel.parse(SafeConverter.toInt(grade));
            if(clazzLevel != null){
                if(needUpdGradeStuData){
                    Integer studentCount = fetchGradeStuCount(schoolExtInfo, clazzLevel, eduSystemType);
                    if(studentCount != null){
                        schoolExtInfo.setGradeStudentNum(clazzLevel, studentCount);
                        schoolSize += studentCount;
                        updated = true;
                    }
                }

                if(needUpdGradeClazzData){
                    Integer clazzCount = fetchGradeClazzCount(schoolExtInfo, clazzLevel, eduSystemType);
                    if(clazzCount != null){
                        schoolExtInfo.setGradeClazzNum(clazzLevel, clazzCount);
                        updated = true;
                    }
                }
            }
        }

        if(updated){
            if(schoolSize > 0){
                schoolExtInfo.setSchoolSize(schoolSize);
            }
            schoolExtServiceClient.getSchoolExtService().updateSchoolExtInfo(schoolExtInfo);
        }
    }

    private Integer fetchGradeStuCount(SchoolExtInfo extInfo, ClazzLevel clazzLevel, EduSystemType eduSystem){
        Integer studentCount = null;
//        if (eduSystem == EduSystemType.P5 || eduSystem == EduSystemType.P6) {
//            if (clazzLevel == ClazzLevel.SECOND_GRADE) {
//                studentCount = extInfo.getGrade1StudentCount();
//            } else if (clazzLevel == ClazzLevel.THIRD_GRADE) {
//                studentCount = extInfo.getGrade2StudentCount();
//            } else if (clazzLevel == ClazzLevel.FOURTH_GRADE) {
//                studentCount = extInfo.getGrade3StudentCount();
//            } else if (clazzLevel == ClazzLevel.FIFTH_GRADE) {
//                studentCount = extInfo.getGrade4StudentCount();
//            } else if (clazzLevel == ClazzLevel.SIXTH_GRADE && eduSystem == EduSystemType.P6) {
//                studentCount = extInfo.getGrade5StudentCount();
//            }
//        }else if(eduSystem == EduSystemType.J4){
//            if (clazzLevel == ClazzLevel.SEVENTH_GRADE) {
//                studentCount = extInfo.getGrade6StudentCount();
//            } else if (clazzLevel == ClazzLevel.EIGHTH_GRADE) {
//                studentCount = extInfo.getGrade7StudentCount();
//            } else if (clazzLevel == ClazzLevel.NINTH_GRADE) {
//                studentCount = extInfo.getGrade8StudentCount();
//            }
//        }else if(eduSystem == EduSystemType.J3){
//            if (clazzLevel == ClazzLevel.EIGHTH_GRADE) {
//                studentCount = extInfo.getGrade7StudentCount();
//            } else if (clazzLevel == ClazzLevel.NINTH_GRADE) {
//                studentCount = extInfo.getGrade8StudentCount();
//            }
//        }else if(eduSystem == EduSystemType.S3){
//            if (clazzLevel == ClazzLevel.SENIOR_TWO) {
//                studentCount = extInfo.getGrade11StudentCount();
//            } else if (clazzLevel == ClazzLevel.SENIOR_THREE) {
//                studentCount = extInfo.getGrade12StudentCount();
//            }
//        }else if(eduSystem == EduSystemType.S4){
//            if (clazzLevel == ClazzLevel.SENIOR_ONE) {
//                studentCount = extInfo.getGrade9StudentCount();
//            } else if (clazzLevel == ClazzLevel.SENIOR_TWO) {
//                studentCount = extInfo.getGrade11StudentCount();
//            } else if (clazzLevel == ClazzLevel.SENIOR_THREE) {
//                studentCount = extInfo.getGrade12StudentCount();
//            }
//        }
        return studentCount;
    }

    private Integer fetchGradeClazzCount(SchoolExtInfo extInfo, ClazzLevel clazzLevel, EduSystemType eduSystem){
        Integer clazzCount = null;
//        if(eduSystem == EduSystemType.P5 || eduSystem == EduSystemType.P6){
//            if(clazzLevel == ClazzLevel.SECOND_GRADE){
//                clazzCount = extInfo.getNewGrade1ClassCount();
//            }else if (clazzLevel == ClazzLevel.THIRD_GRADE) {
//                clazzCount = extInfo.getNewGrade2ClassCount();
//            } else if (clazzLevel == ClazzLevel.FOURTH_GRADE) {
//                clazzCount = extInfo.getNewGrade3ClassCount();
//            } else if (clazzLevel == ClazzLevel.FIFTH_GRADE) {
//                clazzCount = extInfo.getNewGrade4ClassCount();
//            } else if (clazzLevel == ClazzLevel.SIXTH_GRADE && eduSystem == EduSystemType.P6) {
//                clazzCount = extInfo.getNewGrade5ClassCount();
//            }
//        }else if (eduSystem == EduSystemType.J4) {
//            if (clazzLevel == ClazzLevel.SEVENTH_GRADE) {
//                clazzCount = extInfo.getNewGrade6ClassCount();
//            } else if (clazzLevel == ClazzLevel.EIGHTH_GRADE) {
//                clazzCount = extInfo.getNewGrade7ClassCount();
//            } else if (clazzLevel == ClazzLevel.NINTH_GRADE) {
//                clazzCount = extInfo.getNewGrade8ClassCount();
//            }
//        }else if (eduSystem == EduSystemType.J3) {
//            if (clazzLevel == ClazzLevel.EIGHTH_GRADE) {
//                clazzCount = extInfo.getNewGrade7ClassCount();
//            } else if (clazzLevel == ClazzLevel.NINTH_GRADE) {
//                clazzCount = extInfo.getNewGrade8ClassCount();
//            }
//        }else if(eduSystem == EduSystemType.S3){
//            if (clazzLevel == ClazzLevel.SENIOR_TWO) {
//                clazzCount = extInfo.getNewGrade11ClassCount();
//            } else if (clazzLevel == ClazzLevel.SENIOR_THREE) {
//                clazzCount = extInfo.getNewGrade12ClassCount();
//            }
//        }else if(eduSystem == EduSystemType.S4){
//            if (clazzLevel == ClazzLevel.SENIOR_ONE) {
//                clazzCount = extInfo.getNewGrade9ClassCount();
//            } else if (clazzLevel == ClazzLevel.SENIOR_TWO) {
//                clazzCount = extInfo.getNewGrade11ClassCount();
//            } else if (clazzLevel == ClazzLevel.SENIOR_THREE) {
//                clazzCount = extInfo.getNewGrade12ClassCount();
//            }
//        }

        return clazzCount;
    }
}
