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

package com.voxlearning.washington.controller.open.wechat.parent;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MobileRule;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.alps.spi.exception.UtopiaRuntimeException;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.api.constant.UserServiceRecordOperationType;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserAuthentication;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.wechat.api.constants.SourceType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.client.WechatCodeServiceClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.washington.controller.open.AbstractOpenController;
import com.voxlearning.washington.data.OpenAuthContext;
import com.voxlearning.washington.support.PageBlockContentGenerator;
import com.voxlearning.washington.support.WechatServiceClientExtension;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author xin.xin
 * @since 2014-04-09
 */
@Controller
@RequestMapping(value = "/open/wechat")
@Slf4j
public class WechatController extends AbstractOpenController {

    @Inject private RaikouSystem raikouSystem;

    @Inject private WechatCodeServiceClient wechatCodeServiceClient;
    @Inject private WechatServiceClient wechatServiceClient;

    @Inject
    private WechatServiceClientExtension wechatServiceClientExtension;
    @ImportService(interfaceClass = CrmSummaryService.class) private CrmSummaryService crmSummaryService;

    //这个接口应该老师端还在调，先不干了　2016-09-02
    @RequestMapping(value = "getaccesstoken.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getAccessToken(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        int t = SafeConverter.toInt(openAuthContext.getParams().get("t"), 0);//默认0，家长端微信
        try {
            String accessToken = wechatCodeServiceClient.getWechatCodeService()
                    .generateAccessToken(WechatType.of(t))
                    .getUninterruptibly();
            if (StringUtils.isBlank(accessToken)) {
                openAuthContext.setCode("400");
                openAuthContext.setError("获取access_token失败");
            } else {
                openAuthContext.add("access_token", accessToken);
                openAuthContext.setCode("200");
            }
        } catch (UtopiaRuntimeException ex) {
            log.warn("load access_token failed,[wechatType:{},msg:{}]", t, ex.getMessage());
            openAuthContext.setCode("400");
            openAuthContext.setError("查询access_token失败 " + ex.getMessage());
        } catch (Exception ex) {
            log.error("load access_token failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询access_token失败");
        }
        return openAuthContext;
    }

    //这个接口可能有家长端以外的微信调用，先留着　2016-09-02
    @RequestMapping(value = "getdynamicqrcodescene.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getDynamicQRCodeScene(HttpServletRequest request) {
        // 获取动态码对应的scene
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        int t = SafeConverter.toInt(openAuthContext.getParams().get("t"), 0);//默认0，家长端微信
        int sceneId = SafeConverter.toInt(openAuthContext.getParams().get("sceneId"), 0);//默认0，家长端微信
        WechatType wechatType = WechatType.of(t);
        String key = wechatType.toString() + "-DYNAMIC-SCENE-ID-" + sceneId;
        Object cached = wechatServiceClient.getWechatService().getFromPersistenceCache(key).getUninterruptibly();
        if (cached != null && cached instanceof String) {
            String s = (String) cached;
            if (StringUtils.isNotBlank(s)) {
                openAuthContext.setCode("200");
                openAuthContext.add("scene", s);
            } else {
                openAuthContext.setCode("600");
            }
        } else {
            openAuthContext.setCode("600");
        }
        return openAuthContext;
    }

    //这个接口有家长外的微信端调用，留着先　2016-09-02
    // 获取微信页面jsapi调用的accesstoken
    @RequestMapping(value = "getjsapiticket.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getJsapiTicket(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        int t = SafeConverter.toInt(openAuthContext.getParams().get("t"), 0);//默认0，家长端微信
        try {
            openAuthContext.add("jsapi_ticket", wechatCodeServiceClient.getWechatCodeService()
                    .generateJsApiTicket(WechatType.of(t))
                    .getUninterruptibly());
            openAuthContext.setCode("200");
        } catch (UtopiaRuntimeException ex) {
            log.warn("load jsapi_ticket failed,[wechatType:{},msg:{}]", t, ex.getMessage());
            openAuthContext.setCode("400");
            openAuthContext.setError("查询jsapi_ticket失败 " + ex.getMessage());
        } catch (Exception ex) {
            log.error("load access_token failed.", ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询jsapi_ticket失败");
        }
        return openAuthContext;
    }

    //这个接口还有微信端在调，留着先　　2016-09-02
    //微信端绑定验证，支持userId和手机号
    @RequestMapping(value = "loginbyid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loginById(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String token = SafeConverter.toString(openAuthContext.getParams().get("uid"));
        String pwd = SafeConverter.toString(openAuthContext.getParams().get("pwd"));
        //type=0 or null(兼容app。。) 家长通  type=1 老师微信端  reference to WechatType
        int type = SafeConverter.toInt(openAuthContext.getParams().get("type"), 0);

        if (StringUtils.isEmpty(token) || StringUtils.isEmpty(pwd)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("参数不全，登录失败");
            return openAuthContext;
        }
        WechatType wechatType = WechatType.of(type);
        if (wechatType == null) {
            openAuthContext.setCode("400");
            openAuthContext.setError("不存在此应用");
            return openAuthContext;
        }

        try {
            List<User> users = userLoaderClient.loadUsers(token, null);

            Map<Integer, User> userRoleMap = new HashMap<>();
            for (User user : users) {
                userRoleMap.put(user.getUserType(), user);
            }
            User user = null;
            if (WechatType.PARENT == wechatType) {    //家长通登录，允许两种账号（学生和家长）登录
                user = userRoleMap.get(UserType.PARENT.getType());
                if (user == null) {
                    user = userRoleMap.get(UserType.STUDENT.getType());
                }
                if (user == null && userRoleMap.size() > 0) {
                    throw new UtopiaRuntimeException("请使用学生或者家长账号登录");
                }
            } else if (WechatType.TEACHER == wechatType) { //微信老师端
                user = userRoleMap.get(UserType.TEACHER.getType());
                if (user == null && userRoleMap.size() > 0) {
                    throw new UtopiaRuntimeException("请使用老师账号登录");
                }
                if (user != null) {// 禁止中学老师使用微信老师端
                    Teacher teacher = (user instanceof Teacher) ? (Teacher) user
                            : teacherLoaderClient.loadTeacher(user.getId());
                    if (teacher != null && teacher.isJuniorTeacher()) {
                        throw new UtopiaRuntimeException("微信端暂时只支持小学老师使用");
                    }
                }
            } else if (WechatType.AMBASSADOR == wechatType) {//校园大使微信号
                user = userRoleMap.get(UserType.TEACHER.getType());
                if (user == null && userRoleMap.size() > 0) {
                    throw new UtopiaRuntimeException("校园大使才能登录");
                }
                if (user != null) {
                    //肯定就是个老师了
                    TeacherDetail teacher = teacherLoaderClient.loadTeacherDetail(user.getId());
                    if (!teacher.isSchoolAmbassador()) {
                        //普通的老师号也不让登录了
                        throw new UtopiaRuntimeException("校园大使才能登录");
                    }
                }
            }
            if (user == null) {
                throw new UtopiaRuntimeException("用户不存在");
            } else {
                // 临时密码校验 xuesong.zhang 2015-11-19
                if (StringUtils.isBlank(pwd) || !StringUtils.equalsIgnoreCase(userLoaderClient.loadUserTempPassword(user.getId()), pwd)) {
                    UserAuthentication ua = userLoaderClient.loadUserAuthentication(user.getId());
                    if (!ua.fetchUserPassword().match(pwd)) {
                        throw new UtopiaRuntimeException("用户密码错误");
                    }
                }
            }
            if (WechatType.TEACHER == wechatType) {
                Teacher teacher = teacherLoaderClient.loadTeacher(user.getId());
                if (teacher.getSubject() != null) {
                    openAuthContext.add("subject", teacher.getSubject().getKey());
                }
                //如果是短信邀请进来的，默认绑定手机号
                miscServiceClient.bindInvitedTeacherMobile(user.getId());
            }
            openAuthContext.setCode("200");
            openAuthContext.add("uid", user.getId());
            if (MobileRule.isMobile(token)) {
                openAuthContext.add("mobile", token);
            }
            openAuthContext.add("type", StringUtils.lowerCase(UserType.of(user.getUserType()).name()));
        } catch (UtopiaRuntimeException ex) {
            openAuthContext.setCode("400");
            openAuthContext.setError(ex.getMessage());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("登录失败");
        }
        return openAuthContext;
    }

    //发送手机注册家长号验证码
    @RequestMapping(value = "/sendregisterparentverifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendVerifyCode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //绑家长手机发送验证码
    @RequestMapping(value = "sendbindmobileverifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendBindMobileVerifyCode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //微信端手机号＋验证码登录，需要的验证码由此发出
    @RequestMapping(value = "sendloginverifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sendLoginVerifyCode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //此接口还有微信端调用，先留着 2016-09-02
    //生成contextId,防止刷短信
    @RequestMapping(value = "generatecid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext generateContextId(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);

        String contextId = RandomUtils.randomString(10);
        Boolean ret = washingtonCacheSystem.CBS.unflushable.set("VrfCtxWx_" + contextId, 10 * 60, getWebRequestContext().getRealRemoteAddr());
        if (Boolean.TRUE.equals(ret)) {
            openAuthContext.setCode("200");
            openAuthContext.add("cid", contextId);
        } else {
            openAuthContext.setCode("400");
            openAuthContext.setError("生成contextId出错");
        }
        return openAuthContext;
    }

    @RequestMapping(value = "/verifycode.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext verifyCode(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    @RequestMapping(value = "studentlist.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext studentList(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    /**
     * 获取学生信息
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "studentinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext userInfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //这个接口不确定有没有微信端在调，先留着　2016-09-02
    @RequestMapping(value = "getuserinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getUserInfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long userId;
        try {
            userId = Long.parseLong(String.valueOf(openAuthContext.getParams().get("uid")));
        } catch (Exception ex) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的用户ID");
            return openAuthContext;
        }

        User user = raikouSystem.loadUser(userId);
        if (null != user) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", user.getId());
            info.put("userType", user.getUserType());
            openAuthContext.add("userinfo", info);
            openAuthContext.setCode("200");
        } else {
            openAuthContext.setCode("400");
            openAuthContext.setError("invalid userId");
        }
        return openAuthContext;
    }

    //这个接口不确定有没有微信端调，先留着　2016-09-02
    @RequestMapping(value = "getbindmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getBindMobile(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long userId;
        try {
            userId = Long.parseLong(String.valueOf(openAuthContext.getParams().get("uid")));
        } catch (Exception ex) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的用户ID");
            return openAuthContext;
        }

        String am = sensitiveUserDataServiceClient.showUserMobile(userId, "wechat/getbindmobile", SafeConverter.toString(userId));
        if (am != null) {
            openAuthContext.setCode("200");
            openAuthContext.add("mobile", am);
        } else {
            openAuthContext.setCode("400");
            openAuthContext.setError("用户未绑手机");
        }
        return openAuthContext;
    }

    /**
     * ************微信帐号绑定***********
     */
    //根据openId查询绑定信息
    @RequestMapping(value = "/getbinduserbyopenid.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getBindUserByOpenId(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String openId = SafeConverter.toString(openAuthContext.getParams().get("openId"));
        if (null == openId) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的openId");
            return openAuthContext;
        }
        try {
            User user = wechatLoaderClient.loadWechatUser(openId);
            if (null != user) {
                openAuthContext.setCode("200");
                Map<String, Object> info = new HashMap<>();
                info.put("id", user.getId());
                openAuthContext.add("userinfo", info);
            } else {
                openAuthContext.setCode("200");
            }
        } catch (Exception ex) {
            log.error("load user by openid failed. openId:{}", openId, ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询绑定用户失败");
        }
        return openAuthContext;
    }

    //这个接口还有端在调，留着20160902
    //根据userId获取绑定的微信openIds，目前仅支持查询单个用户
    @RequestMapping(value = "getbindedopenids.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getbindedopenids(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        Long userId = SafeConverter.toLong(context.getParams().get("uid"), Long.MIN_VALUE);
        Integer t = SafeConverter.toInt(context.getParams().get("t"));
        WechatType wechatType = WechatType.of(t);
        if (userId == Long.MIN_VALUE || wechatType == null) {
            context.setCode("400");
            context.setError("invalid datas");
            return context;
        }
        try {
            //去库中或者缓存中load出
            Map<Long, List<UserWechatRef>> bindsMap = wechatLoaderClient.loadUserWechatRefs(Collections.singleton(userId), wechatType);
            List<UserWechatRef> activeBinds = bindsMap.get(userId);
            if (CollectionUtils.isNotEmpty(activeBinds)) {
                activeBinds = activeBinds.stream().filter(e -> !e.isDisabledTrue()).collect(Collectors.toList());
            } else {
                activeBinds = Collections.emptyList();
            }
            context.setCode("200");
            context.add("activeBinds", activeBinds);
        } catch (Exception ex) {
            log.error("load user binded openids faild:{}", userId, ex);
            context.setError("400");
            context.setError("查询微信绑定信息失败");
        }
        return context;
    }

    //这个接口还有端在调，先留着20160902
    //根据openId、userId查询绑定信息
    @RequestMapping(value = "/getbindinfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getBindInfo(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        long userId;
        try {
            userId = Long.parseLong(String.valueOf(openAuthContext.getParams().get("userId")));
        } catch (Exception ex) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的userId");
            return openAuthContext;
        }
        String openId = SafeConverter.toString(openAuthContext.getParams().get("openId"));
        if (null == openId) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的openId");
            return openAuthContext;
        }
        try {
            User user = wechatLoaderClient.loadWechatUser(openId, userId);
            if (null != user) {
                openAuthContext.setCode("200");
                Map<String, Object> info = new HashMap<>();
                info.put("id", user.getId());
                openAuthContext.add("userinfo", info);
            } else {
                openAuthContext.setCode("200");
            }
        } catch (Exception ex) {
            log.error("load bind info failed. userId:{},openId:{}", userId, openId, ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("查询绑定信息失败");
        }
        return openAuthContext;
    }

    //这个接口还有端在调，先留着20160902
    //解除用户与微信绑定关系
    @RequestMapping(value = "unbinduser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext unBindUser(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String openId = SafeConverter.toString(openAuthContext.getParams().get("openid"));
        if (StringUtils.isEmpty(openId)) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的参数");
            return openAuthContext;
        }

        try {
            wechatServiceClientExtension.unbindUserAndWechat(openId);
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            log.error("unbind user failed.openId:{}", openId, ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("解绑失败");
        }
        return openAuthContext;
    }

    //**************解答专区****************
    @RequestMapping(value = "searchquestion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext searchQuestion(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //查询某一自助答疑问题
    @RequestMapping(value = "question.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext question(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //获取所有自助答疑分类
    @RequestMapping(value = "faqcatalog.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext faqCatalog(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //获取自助答疑某一分类的问题
    @RequestMapping(value = "loadquestionbycatalog.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext loadQuestionByCatalog(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //获取自助答疑分类
    @RequestMapping(value = "catalog.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext catalog(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //这个接口python还在调，先留着20160902
    //保存用户提交的问题
    @RequestMapping(value = "submitquestion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext submitQuestion(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        String openId = SafeConverter.toString(openAuthContext.getParams().get("openid"));
        String question = SafeConverter.toString(openAuthContext.getParams().get("q"));
        if (null == openId || null == question) {
            openAuthContext.setCode("400");
            openAuthContext.setError("无效的参数");
            return openAuthContext;
        }

        try {
            wechatServiceClient.submitQuestion(openId, question, SourceType.WECHAT);
            openAuthContext.setCode("200");
        } catch (Exception ex) {
            log.error("提交问题失败，[openid:{},question:{},msg:{}]", openId, question, ex.getMessage(), ex);
            openAuthContext.setCode("400");
            openAuthContext.setError("提交问题失败");
        }
        return openAuthContext;
    }

    //*********扫二维码相关***********
    //如果微信端已有家长号绑定，则直接关联学生与该家长号
    //如果学生没有keyparent，微信端将注册一个新家长号，并置为keyparent(无手机号)
    //如果学生有keyparent,将和微信端绑定

    //根据studentId查询其关键家长
    @RequestMapping(value = "getkeyparent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getKeyParent(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //根据家长id查询其关联的孩子
    @RequestMapping(value = "getchildren.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getChildren(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    @RequestMapping(value = "getparents.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getParents(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;

    }

    //关联学生与家长号(无需学生密码)
    @RequestMapping(value = "bindstudentparent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext bindStudentParent(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //家长重置学生密码
    @RequestMapping(value = "resetchildpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext resetStudentPwd(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //注册家长号（无手机号）
    @RequestMapping(value = "registerparentwithoutmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    //FIXME:这个方法应该删掉了,但是线上还有零星的调用,可能是很早以前的家长APP
    public OpenAuthContext registerParentWithoutMobile(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //根据手机号获取家长号（手机号已绑定）
    @RequestMapping(value = "getparentbymobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getParentByMobile(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //将孩子号与家长号关联（需要验证孩子密码）
    @RequestMapping(value = "bindstudentparentwithpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext bindStudentParentWithPwd(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;
    }

    //家长号绑定手机
    @RequestMapping(value = "bindparentmobile.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext bindParentMobile(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //家长端APP注册家长号（无手机号）
    @RequestMapping(value = "registerparentwithoutmobile_app.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext registerParentWithoutMobile_app(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    /**
     * 家长端APP注册家长号的接口
     */
    @RequestMapping(value = "registerparent_app.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext registerParent_app(HttpServletRequest request) {
        OpenAuthContext openAuthContext = getOpenAuthContext(request);
        openAuthContext.setCode("400");
        openAuthContext.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return openAuthContext;
    }

    //这个接口不确定还有没有调，先留着20160902
    @RequestMapping(value = "resetpwd.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext resetPwd(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        long userId = SafeConverter.toLong(context.getParams().get("uid"), Long.MIN_VALUE);
        String pwd = SafeConverter.toString(context.getParams().get("pwd"));
        if (Long.MIN_VALUE == userId || null == pwd) {
            context.setCode("400");
            context.setError("无效的参数");
            return context;
        }

        try {
            User user = raikouSystem.loadUser(userId);
            MapMessage message = userServiceClient.setPassword(user, pwd);
            if (message.isSuccess()) {
                context.setCode("200");
                UserServiceRecord userServiceRecord = new UserServiceRecord();
                userServiceRecord.setUserId(user.getId());
                userServiceRecord.setOperatorId(user.getId().toString());
                userServiceRecord.setOperationType(UserServiceRecordOperationType.用户信息变更.name());
                userServiceRecord.setOperationContent("修改密码");
                userServiceRecord.setComments("用户重置密码");
                userServiceRecord.setAdditions("refer:WechatController.resetpwd");
                userServiceClient.saveUserServiceRecord(userServiceRecord);
            } else {
                logger.warn("修改密码失败,[userId:{},,msg:{}]", userId, message.getInfo());
                context.setCode("400");
                context.setError("修改密码失败");
            }
        } catch (Exception ex) {
            logger.error("reset password failed. userId:{}", userId, ex);
            context.setCode("400");
            context.setError("修改密码失败");
        }
        return context;
    }

    @RequestMapping(value = "getsigninfo.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getSignInfo(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;
    }

    @RequestMapping(value = "sign.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext sign(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;
    }

    @RequestMapping(value = "follow.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext follow(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;
    }

    @RequestMapping(value = "getstudentyeartrack.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getStudentYearTrack(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;
    }

    @RequestMapping(value = "getstudentafentiekcorrectreport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getStudentAfentiEkCorrectReport(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;
    }

    @RequestMapping(value = "getstudentafentitendaysreport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getStudentAfentiTenDaysReport(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;
    }

    @RequestMapping(value = "getstudentwalkerreport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getStudentWalkerReport(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;
    }

    @RequestMapping(value = "getstudentpsrwrongexamreport.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getStudentPsrWrongExamReport(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        context.setCode("400");
        context.setError("请升级最新版家长APP：https://wx.17zuoye.com/download/17parentapp?cid=202010&_from=wechatmenu");
        return context;
    }


    @RequestMapping(value = "getpageblockcontent.vpage", method = RequestMethod.POST)
    @ResponseBody
    public OpenAuthContext getPageBlockContent(HttpServletRequest request) {
        OpenAuthContext context = getOpenAuthContext(request);
        String pageName = SafeConverter.toString(context.getParams().get("pageName"));
        String blockName = SafeConverter.toString(context.getParams().get("blockName"));
        if (StringUtils.isEmpty(pageName) || StringUtils.isEmpty(blockName)) {
            context.setCode("400");
            context.setError("无效的参数");
            return context;
        }
        try {
            PageBlockContentGenerator generator = getPageBlockContentGenerator();
            String content = generator.getPageBlockContentHtml(pageName, blockName);
            if (StringUtils.isNotEmpty(content)) {
                context.setCode("200");
                context.add("content", content);
            } else {
                context.setCode("400");
                context.setError("未找到page block content");
            }
        } catch (Exception ex) {
            logger.error("get page block content failed. pageName:{},blockName:{}", pageName, blockName, ex);
            context.setCode("400");
            context.setError("获取页面内容失败");
        }
        return context;
    }
}
