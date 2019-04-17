package com.voxlearning.wechat.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishClazzService;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishUserExtSplit;
import com.voxlearning.wechat.anotation.CorsHeader;
import com.voxlearning.wechat.constants.AuthType;
import com.voxlearning.wechat.controller.AbstractChipsController;
import com.voxlearning.wechat.support.utils.OAuthUrlGenerator;
import com.voxlearning.wechat.support.utils.StringExtUntil;
import lombok.Cleanup;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;

@Controller
@RequestMapping(value = "/chips/open")
@CorsHeader
public class ChipsOpenController extends AbstractChipsController {

    @ImportService(interfaceClass = ChipsEnglishClazzService.class)
    private ChipsEnglishClazzService chipsEnglishClazzService;

    @RequestMapping(value = "index.vpage", method = RequestMethod.GET)
    public String index(Model model) {
        String page = getRequestString("page");
        AuthType authType = AuthType.CHIPS_CENTER;
        if (StringUtils.isNotBlank(page)) {
            switch (page) {
                case "s6" :
                   return redirectWithMsg("活动已经下线", model);
                case "s7" :
                    authType = AuthType.CHIPS_OPEN_AD_7_GRADE;
                    break;
                case "s8" :
                    authType = AuthType.CHIPS_OPEN_ADDRESS_CHECK;
                    break;
                default:
                    authType = AuthType.CHIPS_CENTER;
                    break;
            }
        }

        String key = "";
        String queryString = getRequestContext().getRequest().getQueryString();
        if (StringUtils.isNotBlank(queryString)) {
            key = StringExtUntil.md5(queryString);
            persistenceCache(key, queryString);
        }
        return "redirect:" + OAuthUrlGenerator.generatorUserInfoScopeForChipsLogin(authType, key);
    }

    // 提交问卷
    @RequestMapping(value = "ugc/submit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage ugcSubmit() {
        Long userId = getRequestContext().getUserId();
        if (userId == null) {
            return MapMessage.errorMessage("请重新登录");
        }

        String grade = getRequestString("grade");
        String studyDuration = getRequestString("studyDuration");
        String expect = getRequestString("expect");
        String weekPoints = getRequestString("weekPoints");
        String otherExtraRegistration = getRequestString("otherExtraRegistration");
        String recentlyScore = getRequestString("recentlyScore");
        Integer serviceScore = getRequestInt("serviceScore");
        String recipientName = getRequestString("recipientName");
        String recipientTel = getRequestString("recipientTel");
        String recipientAddr = getRequestString("recipientAddr");
        String courseLevel = getRequestString("courseLevel");

        return wrapper(mm -> {
            MapMessage re = AtomicCallbackBuilderFactory.getInstance()
                    .<MapMessage>newBuilder()
                    .keyPrefix("loadAndRecordTaskTalk")
                    .keys(userId)
                    .callback(() -> {
                        ChipsEnglishUserExtSplit extSplit = chipsEnglishClazzService.selectChipsEnglishUserExtSplitByUserId(userId);
                        if (extSplit == null) {
                            extSplit = new ChipsEnglishUserExtSplit();
                            extSplit.setCreateTime(new Date());
                            extSplit.setId(userId);
                        }

                        extSplit.setUpdateTime(new Date());
                        if (StringUtils.isNotBlank(grade)) {
                            extSplit.setGrade(grade);
                        }

                        if (StringUtils.isNotBlank(studyDuration)) {
                            extSplit.setStudyDuration(studyDuration);
                        }

                        if (StringUtils.isNotBlank(weekPoints)) {
                            extSplit.setWeekPoints(weekPoints);
                        }

                        if (StringUtils.isNotBlank(expect)) {
                            extSplit.setExpect(expect);
                        }

                        if (StringUtils.isNotBlank(otherExtraRegistration)) {
                            extSplit.setOtherExtraRegistration(otherExtraRegistration);
                        }

                        if (serviceScore != null) {
                            extSplit.setServiceScore(serviceScore);
                        }

                        if (StringUtils.isNotBlank(recentlyScore)) {
                            extSplit.setRecentlyScore(recentlyScore);
                        }

                        if (StringUtils.isNotBlank(recipientName)) {
                            extSplit.setRecipientName(recipientName);
                        }

                        if (StringUtils.isNotBlank(recipientTel)) {
                            extSplit.setRecipientTel(recipientTel);
                        }

                        if (StringUtils.isNotBlank(recipientAddr)) {
                            extSplit.setRecipientAddr(recipientAddr);
                        }

                        if (StringUtils.isNotBlank(courseLevel)) {
                            extSplit.setCourseLevel(courseLevel);
                        }
                        return chipsEnglishClazzService.upsertChipsEnglishUserExtSplit(extSplit);
                    })
                    .build()
                    .execute();
            mm.putAll(re);
        });
    }


    @RequestMapping(value = "image.vpage", method = RequestMethod.GET)
    public void image(HttpServletResponse resp) {
        String url = getRequestString("url");
        if (StringUtils.isBlank(url)) {
            return;
        }
        try (OutputStream out = resp.getOutputStream();InputStream inputStream = getRemoteStream(url)) {
            byte[] buffer = new byte[1024 * 4];
            int n = 0;
            while ((n = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, n);
            }
            out.flush();
        } catch (Exception e) {

        }
    }

    private InputStream getRemoteStream(String fileUrl) throws IOException {
        URL url = new URL(fileUrl);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(300 * 1000);
        return conn.getInputStream();
    }

}
