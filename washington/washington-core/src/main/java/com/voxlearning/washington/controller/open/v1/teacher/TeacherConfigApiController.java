/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.teacher;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.AppAuditAccounts;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.school.client.SchoolExtServiceClient;
import com.voxlearning.utopia.service.user.api.entities.SchoolExtInfo;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLevelServiceClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import com.voxlearning.washington.controller.open.AbstractTeacherApiController;
import com.voxlearning.washington.controller.open.exception.IllegalVendorUserException;
import com.voxlearning.washington.controller.open.v1.util.InternalOffRewardService;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.washington.controller.open.ApiConstants.*;

/**
 * Created by jiangpeng on 16/4/11.
 * 配置相关api接口
 */

@Controller
@RequestMapping(value = "/v1/teacher/config")
@Slf4j
public class TeacherConfigApiController extends AbstractTeacherApiController {

    @Inject
    private TeacherLevelServiceClient teacherLevelServiceClient;
    @Inject
    private SchoolExtServiceClient schoolExtServiceClient;

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private InternalOffRewardService internalOffRewardService;

    public TeacherConfigApiController() {
        super();
        new shareClazzConfigProvider(RES_SHARE_CLAZZ_CONFIG);
        new centerFunctionConfigProvider(RES_CENTER_CONFIG);
        new offlineHomeworkSubjectConfigProvider(RES_OFFLINE_HOMEWORK_SUBJECTS);
        new MyTopConfigProvider(RES_MYTOP_CONFIG);
    }

    private static List<Subject> offlineHomeworkSubjectList = new ArrayList<>();

    static {
        offlineHomeworkSubjectList.add(Subject.ENGLISH);
        offlineHomeworkSubjectList.add(Subject.MATH);
        offlineHomeworkSubjectList.add(Subject.CHINESE);
    }

    private static Map<String, AbstractConfigProvider> commonConfigProviderMap = new HashMap<>();


    /**
     * 通用配置接口
     *
     * @return
     */
    @RequestMapping(value = "/common.vpage", method = {RequestMethod.POST})
    @ResponseBody
    public MapMessage commonConfig() {
        MapMessage resultMap = new MapMessage();
        try {
            validateRequired(REQ_CONFIG_KEYS, "配置key");
            validateRequest(REQ_CONFIG_KEYS);
        } catch (IllegalVendorUserException ue) {
            resultMap.add(RES_RESULT, ue.getCode());
            resultMap.add(RES_MESSAGE, ue.getMessage());
            return resultMap;
        } catch (IllegalArgumentException e) {
            resultMap.add(RES_RESULT, RES_RESULT_BAD_REQUEST_CODE);
            resultMap.add(RES_MESSAGE, e.getMessage());
            return resultMap;
        }
        String version = getRequestString(REQ_APP_NATIVE_VERSION);
        TeacherDetail teacher = getCurrentTeacher();
        TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(teacher.getId());
        resultMap.add(RES_RESULT, RES_RESULT_SUCCESS);
        List<String> keyList = JSON.parseArray(getRequestString(REQ_CONFIG_KEYS), String.class);
        if (CollectionUtils.isEmpty(keyList)) {
            return resultMap;
        }
        keyList.forEach(p -> {
            if (commonConfigProviderMap.containsKey(p)) {
                resultMap.add(p, commonConfigProviderMap.get(p).generateConfig(teacher, version, extAttribute));
            }
        });

        return resultMap;
    }


    abstract class AbstractConfigProvider {

        /**
         * 配置可能是jsonObject 也可能是jsonArray
         *
         * @param args
         * @return
         */
        abstract Object generateConfig(Object... args);
    }

    private class shareClazzConfigProvider extends AbstractConfigProvider {
        @Override
        Object generateConfig(Object... args) {
            Teacher teacher = (Teacher) args[0];
            Map<String, Object> map = new LinkedHashMap<>();
            if (teacher.isJuniorTeacher() || teacher.isSeniorTeacher()) {
                map.put(RES_SHARE_TITLE, "同学们，来一起中学吧！");
            } else {
                map.put(RES_SHARE_TITLE, "同学们，来一起小学吧！");
            }

            map.put(RES_SHARE_CONTENT, "我推荐了网上练习，更轻松更有趣，快加入班级做练习吧。");

            String url = generateShareClazzUrl(teacher);
            String shortUrl = i7TinyUrl(url);
            map.put(RES_SHARE_URL, shortUrl);
            return map;
        }

        shareClazzConfigProvider(String key) {
            commonConfigProviderMap.put(key, this);
        }
    }


    /**
     * 线下作业单支持的学科
     */
    private class offlineHomeworkSubjectConfigProvider extends AbstractConfigProvider {
        @Override
        Object generateConfig(Object... args) {
            Teacher teacher = (Teacher) args[0];
            if (teacher == null || teacher.isInfantTeacher()) {
                return Collections.emptyList();
            }
            List<Subject> teacherSubjects = teacher.getSubjects();
            List<Map<String, Object>> subjectMapList = new LinkedList<>();
            offlineHomeworkSubjectList.forEach(subject -> {
                if (CollectionUtils.isNotEmpty(teacherSubjects) && teacherSubjects.contains(subject)) {
                    Map<String, Object> map = new LinkedHashMap<>();
                    map.put(RES_SUBJECT_NAME, subject.getValue());
                    map.put(RES_SUBJECT, subject.name());
                    subjectMapList.add(map);
                }
            });
            return subjectMapList;
        }

        offlineHomeworkSubjectConfigProvider(String key) {
            commonConfigProviderMap.put(key, this);
        }
    }

    /**
     * 我的页面顶部配置
     */
    private class MyTopConfigProvider extends AbstractConfigProvider {
        @Override
        Object generateConfig(Object... args) {
            TeacherDetail teacher = (TeacherDetail) args[0];
            String version = (String) args[1];
            TeacherExtAttribute extAttribute = (TeacherExtAttribute) args[2];

            List<Map<String, Object>> configList = new ArrayList<>();

            // 中学园丁豆 小学等级福利中心
            Map<String, Object> integral = new HashMap<>();
            if (teacher.isJuniorTeacher() || teacher.isSeniorTeacher()) {
                integral.put("title", "学豆");
                long usableIntegral = 0;
                if (teacher.getUserIntegral() != null) {
                    usableIntegral = teacher.getUserIntegral().getUsable();
                }
                integral.put("label", usableIntegral);
                integral.put("linkUrl", fetchMainsiteUrlByCurrentSchema() + integralUrl);
                integral.put("imgUrl", "");
            } else {
                integral.put("title", "等级福利中心");
                //Integer exp = teacherLevelServiceClient.loadExp(teacher.getId());
                Integer exp = 0;
                if (extAttribute != null && extAttribute.getExp() != null && extAttribute.getExp() > 0) {
                    exp = extAttribute.getExp();
                }
                integral.put("label", exp);
                integral.put("linkUrl", fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/activity2018/primary/task_system/index");
                integral.put("imgUrl", "");
            }

            configList.add(integral);

            return configList;
        }

        MyTopConfigProvider(String key) {
            commonConfigProviderMap.put(key, this);
        }
    }

    /**
     * 个人中心 各个入口配置
     * 邀请有礼
     * 福利活动
     * 奖品中心
     */
    private class centerFunctionConfigProvider extends AbstractConfigProvider {

        @Override
        Object generateConfig(Object... args) {
            Teacher teacher = (Teacher) args[0];
            String version = (String) args[1];
            List<ConfigModel> configList = new ArrayList<>();
            //ios端发了一个紧急版本,用来修复第一版的bug,但他么带上了第二版的代码,导致这个版本出现了第二版才有的功能
            if (VersionUtil.compareVersion(version, "1.6.1") >= 0) {
                // 我的班级
                ConfigModel myClassConfig = generateClassConfig(teacher);
                if (myClassConfig != null) {
                    myClassConfig.setOrder(2);
                    configList.add(myClassConfig);
                }

                // 鲜花
                ConfigModel flowerConfig = generateFlowerConfig(teacher);
                if (flowerConfig != null) {
                    flowerConfig.setOrder(2);
                    configList.add(flowerConfig);
                }

                // 奖品中心
                ConfigModel giftMall = generateGiftMallConfig(teacher);
                if (giftMall != null) {
                    giftMall.setOrder(3);
                    configList.add(giftMall);
                }

                // 福利活动
                ConfigModel welfare = generateWelfareConfig(teacher);
                if (welfare != null) {
                    welfare.setOrder(4);
                    configList.add(welfare);
                }

                // 护眼攻略
                ConfigModel eyestrategyConfig = generateEyestrategyConfig(teacher);
                if (eyestrategyConfig != null) {
                    eyestrategyConfig.setOrder(5);
                    configList.add(eyestrategyConfig);
                }

                // 课题申请
//                ConfigModel keti = generateKetiConfig(teacher, version);
//                if (keti != null) {
//                    keti.setOrder(7);
//                    configList.add(keti);
//                }

                // 邀请
//                ConfigModel invite = generateInviteConfig(teacher, version);
//                if (invite != null) {
//                    invite.setOrder(8);
//                    configList.add(invite);
//                }

                // 帮助与反馈
                ConfigModel help = generateHelpConfig(teacher, version);
                if (help != null) {
                    help.setOrder(8);
                    configList.add(help);
                }

                if (VersionUtil.compareVersion(version, "2.3.0") >= 0) {
                    // 阅卷
                    ConfigModel scanPaper = generateScanPaperConfig(teacher, version);
                    if (scanPaper != null) {
                        scanPaper.setOrder(10);
                        configList.add(scanPaper);
                    }
                }
                ConfigModel appraiseConfig = generateAppraiseConfig(teacher, version);
                if (appraiseConfig != null) {
                    help.setOrder(11);
                    configList.add(appraiseConfig);
                }

                ConfigModel reportConfig = generateInformConfig(teacher, version);
                if (reportConfig != null) {
                    help.setOrder(12);
                    configList.add(reportConfig);
                }

            } else if (VersionUtil.compareVersion(version, "1.5.0") >= 0) {
                ConfigModel flowerConfig = generateFlowerConfig(teacher);
                if (flowerConfig != null) {
                    flowerConfig.setOrder(2);
                    configList.add(flowerConfig);
                }

                ConfigModel help = generateHelpConfig(teacher, version);
                if (help != null) {
                    help.setOrder(3);
                    configList.add(help);
                }
            } else {
                ConfigModel welfare = generateWelfareConfig(teacher);
                if (welfare != null) {
                    welfare.setOrder(2);
                    configList.add(welfare);
                }

                ConfigModel giftMall = generateGiftMallConfig(teacher);
                if (giftMall != null) {
                    giftMall.setOrder(3);
                    configList.add(giftMall);
                }

                ConfigModel help = generateHelpConfig(teacher, version);
                if (help != null) {
                    help.setOrder(4);
                    configList.add(help);
                }

                ConfigModel flowerConfig = generateFlowerConfig(teacher);
                if (flowerConfig != null) {
                    flowerConfig.setOrder(5);
                    configList.add(flowerConfig);
                }
            }

            return configList;
        }

        // 我的班级只有中学非灰度地区显示
        private ConfigModel generateClassConfig(Teacher teacher) {
            if (teacher.isPrimarySchool() || teacher.isInfantTeacher()) {
                return null;
            }

            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            if (!grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(
                    teacherDetail, "Reward", "Index", true)) {
                ConfigModel model = new ConfigModel();
                model.setTitle("我的班级");
                model.setLable("");
                model.setLinkUrl("person_clazz_manage");
                model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/mobile/ketiimg/banji.png");
                return model;
            } else {
                return null;
            }
        }

        // 鲜花只有小学老师显示
        private ConfigModel generateFlowerConfig(Teacher teacher) {
            if (teacher.getKtwelve() == null || teacher.getKtwelve() != Ktwelve.PRIMARY_SCHOOL) {
                return null;
            }

            ConfigModel model = new ConfigModel();
            model.setTitle("鲜花");
            model.setLable("");
            model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + flowerUrl);
            model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/app/17teacher/res/flower.png");
            return model;
        }

        // 福利活动
        private ConfigModel generateWelfareConfig(Teacher teacher) {
            if (teacher != null && teacher.isPrimarySchool() && !AppAuditAccounts.isTeacherAuditAccount(teacher.getId())) {
                ConfigModel model = new ConfigModel();
                model.setTitle("教学活动");
                model.setLable("");
                model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/welfare");
                model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/mobile/ketiimg/fulihd.png");
                return model;
            } else {
                ConfigModel model = new ConfigModel();
                if (teacher != null && teacher.isJuniorTeacher()
                        && CollectionUtils.isNotEmpty(teacher.getSubjects()) && teacher.getSubjects().contains(Subject.ENGLISH)) {
                    model.setTitle("邀请新老师");
                    model.setLable("");
                    model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/activity2018/invite_teacher/index?source=junior_myinfo");
                    model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/mobile/ketiimg/teacherinvite.png");
                } else {
                    model.setTitle("教学活动");
                    model.setLable("");
                    model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/activity2018/junior/task_system/index");
                    model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/mobile/ketiimg/fulihd.png");
                }
                return model;
            }
        }

        // 护眼攻略， 中小学
        private ConfigModel generateEyestrategyConfig(Teacher teacher) {
            ConfigModel model = new ConfigModel();
            model.setTitle("护眼攻略");
            model.setLable("");
            model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/activity/eyestrategys");
            model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/mobile/ketiimg/eyecare_icon.png");
            return model;
        }

        // 奖品中心  小学 + 中学灰度地区
        private ConfigModel generateGiftMallConfig(Teacher teacher) {
            if (teacher == null || AppAuditAccounts.isTeacherAuditAccount(teacher.getId())) {
                return null;
            }

            TeacherDetail teacherDetail = teacherLoaderClient.loadTeacherDetail(teacher.getId());
            if (grayFunctionManagerClient.getTeacherGrayFunctionManager().isWebGrayFunctionAvailable(
                    teacherDetail, "Reward", "Index", true)) {

                if (internalOffRewardService.offline(teacherDetail)) {
                    return null;
                }

                ConfigModel model = new ConfigModel();
                model.setTitle("教学用品");
                model.setLable("");
                model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/common/reward");
                model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/mobile/ketiimg/giftmall.png");
                return model;
            } else {
                return null;
            }
        }

        public centerFunctionConfigProvider(String key) {
            commonConfigProviderMap.put(key, this);
        }

        // 帮助与反馈 中小学
        public ConfigModel generateHelpConfig(Teacher teacher, String version) {
            if (VersionUtil.compareVersion(version, "1.3.0.0") < 0) {
                return null;
            }

            ConfigModel model = new ConfigModel();
            model.setLable("");
            model.setTitle("帮助与反馈");
            model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/app/17teacher/res/feedback.png");
            if (teacher.isJuniorTeacher()) {
                model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/help/list?type=junior");
            } else if (teacher.isPrimarySchool()) {
                model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/help/list?type=primary");
            } else {
                return null;
            }
            return model;
        }

        // 阅卷
        public ConfigModel generateScanPaperConfig(Teacher teacher, String version) {
            TeacherDetail teacherDetail = (TeacherDetail) teacher;
            SchoolExtInfo schoolExtInfo = schoolExtServiceClient.getSchoolExtService().loadSchoolExtInfo(teacherDetail.getTeacherSchoolId()).getUninterruptibly();
            if (schoolExtInfo == null || !schoolExtInfo.isScanMachineFlag()) {
                return null;
            }
            // 020阅卷的老师
            ConfigModel model = new ConfigModel();
            model.setLable("");
            model.setTitle("阅卷");
            model.setImgUrl("https://cdn-cnc.17zuoye.cn/resources/app/17middle_teacher/res/middle_teacher_o2o_icon.png");
            model.setLinkUrl(ProductConfig.getKuailexueUrl() + "/m/exam/tasks");
            return model;
        }

        // 我要评价 中学
        public ConfigModel generateAppraiseConfig(Teacher teacher, String version) {
            TeacherDetail teacherDetail = (TeacherDetail) teacher;
            if (teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher()) {
                ConfigModel model = new ConfigModel();
                model.setLable("");
                model.setTitle("我要评价");
                if (teacherDetail.isMathTeacher()) {
                    model.setImgUrl("https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/junior_math_comment.png");
                } else {
                    model.setImgUrl("https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/junior_en_comment.png");
                }
                model.setLinkUrl(ProductConfig.getMainSiteBaseUrl() + "/redirector/appeval.vpage?app_key=17JuniorTea");
                return model;
            }
            return null;
        }
        // 我要举报 中学
        public ConfigModel generateInformConfig(Teacher teacher, String version) {
            TeacherDetail teacherDetail = (TeacherDetail) teacher;
            if (teacherDetail.isJuniorTeacher() || teacherDetail.isSeniorTeacher()) {
                ConfigModel model = new ConfigModel();
                model.setLable("");
                model.setTitle("我要举报");
                if (teacherDetail.isMathTeacher()) {
                    model.setImgUrl("https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/junior_math_inform.png");
                } else {
                    model.setImgUrl("https://cdn-cnc.17zuoye.cn/resources/mobile/teacher/images/junior_en_inform.png");
                }
                model.setLinkUrl(ProductConfig.getMainSiteBaseUrl() + "/redirector/appreport.vpage?app_key=17JuniorTea");
                return model;
            }
            return null;
        }

        // 课题申请 小学
        public ConfigModel generateKetiConfig(Teacher teacher, String version) {
            if (teacher != null && teacher.isPrimarySchool()) {
                ConfigModel model = new ConfigModel();
                model.setTitle("课题申请");
                model.setLable("");
                model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/common/topic/list");
                model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/mobile/ketiimg/keti.png");
                return model;
            } else {
                return null;
            }
        }

        // 邀请 小学
        public ConfigModel generateInviteConfig(Teacher teacher, String version) {
            if (teacher != null) {
                ConfigModel model = new ConfigModel();
                model.setTitle("邀请新老师");
                model.setLable("");
                model.setLinkUrl(fetchMainsiteUrlByCurrentSchema() + "/view/mobile/teacher/activity2018/invite_teacher/index?refer=myinfo");
                model.setImgUrl("http://cdn-cnc.17zuoye.cn/resources/mobile/ketiimg/teacherinvite.png");
                return model;
            } else {
                return null;
            }
        }

        class ConfigModel {
            @Getter
            @Setter
            private String lable;
            @Getter
            @Setter
            private String title;
            @Getter
            @Setter
            private String linkUrl;
            @Getter
            @Setter
            private Integer order;
            @Getter
            @Setter
            private String imgUrl = "";
        }

    }

    public static String i7TinyUrl(String longUrl) {
        String responseStr = HttpRequestExecutor.defaultInstance()
                .post("http://www.17zyw.cn/crt")
                .addParameter("url", longUrl)
                .execute()
                .getResponseString();
        if (StringUtils.isNotBlank(responseStr)) {
            return "http://www.17zyw.cn/" + responseStr;
        } else {
            return longUrl;
        }
    }

}
