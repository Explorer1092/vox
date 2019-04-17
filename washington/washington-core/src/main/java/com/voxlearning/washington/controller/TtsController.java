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

package com.voxlearning.washington.controller;

import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.voxlearning.alps.annotation.meta.AuthenticationState;
import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.annotation.meta.RoleType;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.IOUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.http.client.execute.POST;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.storage.StorageClient;
import com.voxlearning.alps.spi.storage.StorageClientLocation;
import com.voxlearning.alps.spi.storage.StorageSystem;
import com.voxlearning.alps.webmvc.support.context.UtopiaHttpRequestContext;
import com.voxlearning.utopia.business.api.TtsListeningService;
import com.voxlearning.utopia.business.api.entity.TtsListeningPaper;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSentence;
import com.voxlearning.utopia.business.api.mapper.TtsListeningSubQuestion;
import com.voxlearning.utopia.service.user.api.entities.TeacherExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.washington.service.TtsListeningGenerator;
import com.voxlearning.washington.support.AbstractController;
import com.voxlearning.washington.support.FileDownloader;
import com.voxlearning.washington.support.TeacherTtsDownloader;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by tanguohong on 14-8-18.
 * Modified by zhangjunjie on 14-10-20
 * 注意：/tts/*只有老师和教研员才能访问，部分页面不在tts目录下。
 */
@Controller
@RequestMapping
public class TtsController extends AbstractController {

    @Inject private TeacherTtsDownloader teacherTtsDownloader;

    @StorageClientLocation(system = StorageSystem.GFS, storage = "fs-tts")
    private StorageClient fsTts;

    @RequestMapping(value = "/tts.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage getTts() {
        MapMessage mapMessage = new MapMessage();
        Map<String, Object> queryParams = JsonUtils.fromJson(getRequestParameter("param", null));
        if (queryParams == null) {
            queryParams = MiscUtils.<String, Object>map().add("g", "x").add("t", "参数错误").add("c", "l");
        }
        try {
            String url = commonConfiguration.getTts_url();
            POST post = HttpRequestExecutor.defaultInstance().post(url);
            queryParams.entrySet().forEach(e -> {
                String name = e.getKey();
                String value = SafeConverter.toString(e.getValue());
                post.addParameter(name, value);
            });
            String r = post.execute().getResponseString();
            Map resultMap = JsonUtils.fromJson(r);
            mapMessage.add("error", resultMap.get("error"));
            mapMessage.add("url", resultMap.get("url"));
        } catch (Exception ex) {
            mapMessage.setErrorCode("远程连接调用失败:" + ex.getMessage());
        }
        return mapMessage;
    }

    /**
     * 老师听力材料页面
     */
    @RequestMapping(value = "/tts/listening.vpage", method = RequestMethod.GET)
    public String listening(Model model) {
        int showDown = 0;

        boolean teacherAllowed = false;
        User user = currentUser();
        if (user.isTeacher()) {
            TeacherExtAttribute extAttribute = teacherLoaderClient.loadTeacherExtAttribute(user.getId());
            if (extAttribute != null) {
                teacherAllowed = SafeConverter.toInt(extAttribute.getLevel()) >= 1;
            }
        }

        if (teacherAllowed || currentUserRoleTypes().contains(RoleType.ROLE_RESEARCH_STAFF)) {
            showDown = 1;
        }
        // 教研员也在用这个controller，这里不能直接拿currentTeacher
        model.addAttribute("junior", currentUser().isTeacher() && currentTeacher() != null && currentTeacher().isJuniorTeacher());
        model.addAttribute("showDown", showDown);
        return "/tts/listening";
    }

    /**
     * 老师听力材料列表页
     */
    @RequestMapping(value = "/tts/listeningList.vpage", method = RequestMethod.GET)
    public String listeningList(Model model) {
        int currentPage = getRequestInt("pageNum", 1);
        String title = getRequestString("title");
        Pageable pageable = new PageRequest(currentPage - 1, 5);
        Long userId = currentUserId();
        Page<TtsListeningPaper> listeningPaperList = ttsListeningServiceClient.getRemoteReference().getListeningPaperPageByUserId(userId, pageable, title);
        model.addAttribute("dataList", listeningPaperList);
        return "/tts/listening/listeningList";
    }

    /**
     * 老师听力材料共享页
     */
    @RequestMapping(value = "/tts/sharingList.vpage", method = RequestMethod.GET)
    public String sharingList(Model model) {
        int currentPage = getRequestInt("pageNum", 1);
        long bookId = getRequestLong("bookId", 0);
        int classLevel = getRequestInt("classLevel", 0);
        Pageable pageable = new PageRequest(currentPage - 1, 5);
        Page<TtsListeningPaper> list = ttsListeningServiceClient.getRemoteReference().getSharedListeningPaperPage(pageable, bookId, classLevel, currentUser().isTeacher() && currentTeacher() != null && currentTeacher().isJuniorTeacher() ? Ktwelve.JUNIOR_SCHOOL : Ktwelve.PRIMARY_SCHOOL);
        model.addAttribute("shareList", list);
        model.addAttribute("bookId", bookId);
        model.addAttribute("classLevel", classLevel);
        return "/tts/listening/sharingList";
    }

    /**
     * 老师听力材料制作flash
     */
    @Deprecated
    @RequestMapping(value = "/tts/flash.vpage", method = RequestMethod.GET)
    public String flash(Model model) {
        User teacher = currentUser();
        boolean enableShare = false;
        if (teacher.fetchCertificationState() == AuthenticationState.SUCCESS
                || currentUserRoleTypes().contains(RoleType.ROLE_RESEARCH_STAFF))
            enableShare = true;
        model.addAttribute("defaultGrade", currentUser().isTeacher() && currentTeacher() != null && currentTeacher().isJuniorTeacher() ? 7 : 1);
        model.addAttribute("enableShare", enableShare);
        model.addAttribute("listeningUrl", getCdnBaseUrlStaticSharedWithSep() + "/fs-tts/");
        return "/tts/listening/flash";
    }

    /**
     * 更换教材
     */
    @RequestMapping(value = "/tts/changebook.vpage", method = RequestMethod.GET)
    public String changebook(Model model) {
        if (currentUser().isTeacher() && currentTeacher() != null && currentTeacher().isJuniorTeacher()) {
            return "/tts/listening/changebook_middleschool";
        }

        return "/tts/listening/changebook";
    }


    /**
     * 听力材料创建
     */
    @RequestMapping(value = "/tts/listening/index.vpage", method = RequestMethod.GET)
    public String listeningIndex(Model model) {
        User user = currentUser();
        boolean enableShare = false;
        if (user.fetchCertificationState() == AuthenticationState.SUCCESS
                || currentUserRoleTypes().contains(RoleType.ROLE_RESEARCH_STAFF))
            enableShare = true;
        model.addAttribute("enableShare", enableShare);
        model.addAttribute("share", 1);
        model.addAttribute("listeningUrl", getCdnBaseUrlStaticSharedWithSep() + "/fs-tts/");
        return "/tts/listening/index";
    }

    /**
     * 听力材料查看
     * 可以分享给其他人，不需要登录就可以访问
     */
    @RequestMapping(value = "/tts_view.vpage", method = RequestMethod.GET)
    public String listeningView(Model model) {
        String id = getRequestString("id");
        TtsListeningService service = ttsListeningServiceClient.getRemoteReference();
        TtsListeningPaper paper = service.getListeningPaperById(id);
        if (paper == null)
            return "redirect:/tts/listening.vpage";
        List<TtsListeningSentence> list = TtsListeningGenerator.getPlayList(paper);
        model.addAttribute("playList", JsonUtils.toJson(list));
        model.addAttribute("listeningUrl", getCdnBaseUrlStaticSharedWithSep() + "/fs-tts/");
        model.addAttribute("paper", paper);
        if (SafeConverter.toInt(paper.getFormat()) == 1) {
            String text = paper.getRichTextNewFormat();
            if (text != null) {
                text = text.replaceAll("</?TextFlow[^>]*>", "");
                text = text.replace("<img id=", "<i class=").replace("/>", "></i>");
                text = text.replaceAll("<i class=\"SOUND\\d+", "<i class=\"SOUND");
                text = text.replaceAll("<i class=\"VOICE\\d+", "<i class=\"VOICE");
                text = text.replaceAll("<i class=\"MUSIC\\d+", "<i class=\"MUSIC");
                text = text.replaceAll("<i class=\"VOWELS(\\d+)[FM]?", "<i class=\"PHONE PHONE$1");
                text = text.replaceAll("<i class=\"SPEED", "<i class=\"SPEED SPEED");
                text = text.replaceAll("<i class=\"LOOP", "<i class=\"LOOP LOOP");
                text = text.replaceAll("<i class=\"PAUSE", "<i class=\"PAUSE PAUSE");
                text = text.replaceAll("<i class=\"VOLUME", "<i class=\"VOLUME VOLUME");
                text = text.replaceAll("<i class=\"ROLE", "<i class=\"ROLE ROLE");
            }
            model.addAttribute("text", text);
            return "tts/listening/flashView";
        }
        return "/tts/listening/view";
    }

    /**
     * 听力材料修改
     */
    @RequestMapping(value = "/tts/listening/edit.vpage", method = RequestMethod.GET)
    public String listeningEdit(Model model) {
        Long userId = currentUserId();
        String id = getRequestString("id");
        TtsListeningService service = ttsListeningServiceClient.getRemoteReference();
        TtsListeningPaper paper = service.getListeningPaperById(id);
        if (paper == null || !paper.getAuthor().equals(userId))
            return "redirect:/tts/listening.vpage";


        boolean enableShare = false;
        if (currentUser().fetchCertificationState() == AuthenticationState.SUCCESS
                || currentUserRoleTypes().contains(RoleType.ROLE_RESEARCH_STAFF))
            enableShare = true;
        model.addAttribute("enableShare", enableShare);

        if (SafeConverter.toInt(paper.getShare()) == 1) {
            model.addAttribute("share", 1);
        } else {
            model.addAttribute("share", 0);
        }
        model.addAttribute("id", id);
        model.addAttribute("listeningUrl", getCdnBaseUrlStaticSharedWithSep() + "/fs-tts/");
        if (SafeConverter.toInt(paper.getFormat()) == 1) {
            if (paper.getRichText() != null)
                paper.setRichText(paper.getRichTextNewFormat().replace("'", "\\'"));
            if (paper.getTitle() != null)
                paper.setTitle(paper.getTitle().replace("'", "\\'"));
            model.addAttribute("paper", paper);
            model.addAttribute("defaultGrade", currentUser().isTeacher() && currentTeacher() != null && currentTeacher().isJuniorTeacher() ? 7 : 1);
            return "tts/listening/flash";
        }
        model.addAttribute("paper", JsonUtils.toJson(paper));
        return "/tts/listening/index";
    }

    /**
     * 分页功能
     */
    @RequestMapping(value = "/tts/listeningPage.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage listeningPage() {
        MapMessage mapMessage = new MapMessage();
        int currentPage = getRequestInt("pageNum");
        String title = getRequestString("title");
        Pageable pageable = new PageRequest(currentPage, 10);
        Long userId = currentUserId();
        Page<TtsListeningPaper> listeningPaperList = ttsListeningServiceClient.getRemoteReference().getListeningPaperPageByUserId(userId, pageable, title);
        mapMessage.setSuccess(true);
        mapMessage.set("value", listeningPaperList);
        return mapMessage;
    }

    /**
     * 听力材料下载
     */
    @RequestMapping(value = "/tts_download.vpage", method = RequestMethod.GET)
    @ResponseBody
    public void downloadListeningResource(@RequestParam("paperId") String paperId) {
        try {
            TtsListeningService service = ttsListeningServiceClient.getRemoteReference();
            TtsListeningPaper paper = service.getListeningPaperById(paperId);
            if (paper == null)
                return;
            List<TtsListeningSentence> playList = TtsListeningGenerator.getPlayList(paper);
            if (playList == null)
                return;
            String fileName = String.format("tts_%s.zip", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
            ZipArchiveOutputStream zos = getZipOutputStreamForDownloading(fileName);
            teacherTtsDownloader.downloadTtsResource(paper, playList, zos);
            zos.flush();
            zos.close();
        } catch (Exception ex) {
            logger.error("Error occurs when downloading tts '{}'", paperId, ex);
        }
    }

    /**
     * 听力材料删除
     */
    @RequestMapping(value = "/tts/listening/delete.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteListeningPaper() {
        Long userId = currentUserId();
        String id = getRequestString("id");
        MapMessage mapMessage = new MapMessage();
        try {
            mapMessage.setSuccess(ttsListeningServiceClient.getRemoteReference().deleteListeningPaper(id, userId));
        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            logger.error("听力试卷删除失败", ex);
        }
        return mapMessage;
    }

    /**
     * 听力句子音频生成
     */
    @RequestMapping(value = "/tts/listening/generateSentence.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateListeningSentence(@RequestBody String json) {
        MapMessage mapMessage = new MapMessage();
        try {
            TtsListeningSentence sentence = TtsListeningGenerator.generateSentenceVoice(commonConfiguration.getTts_url(), json);
            if (sentence != null && StringUtils.isNotEmpty(sentence.getVoice()) && sentence.getDuration() != null && sentence.getDuration() > 0) {
                mapMessage.set("value", sentence.getDuration());
                mapMessage.setInfo(sentence.getVoice());
                mapMessage.setSuccess(true);
            } else {
                mapMessage.setSuccess(false);
            }

        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            logger.error("听力句子音频生成失败", ex);
        }

        return mapMessage;
    }

    /**
     * 听力小题音频生成
     */
    @RequestMapping(value = "/tts/listening/generateSubQuestion.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateListeningSubQuestion(@RequestBody String json) {
        MapMessage mapMessage = new MapMessage();
        try {
            TtsListeningSubQuestion subQuestion = TtsListeningGenerator.generateSubQuestionVoice(commonConfiguration.getTts_url(), json);
            List<String> ids = new ArrayList<>();
            if (subQuestion != null && subQuestion.getSentences() != null) {
                for (TtsListeningSentence sentence : subQuestion.getSentences()) {
                    ids.add(sentence.getVoice());
                }
            }
            mapMessage.set("value", ids);
            mapMessage.setSuccess(true);
        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            logger.error("听力小题音频生成失败", ex);
        }

        return mapMessage;
    }

    /**
     * FLASH听力试卷音频生成
     */
    @RequestMapping(value = "/tts/listening/generatePaperFromFlash.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateListeningPaperFromFlash(@RequestBody String json) {
        MapMessage mapMessage;
        try {
            Map<String, String> inputMap = JsonUtils.fromJsonToMap(json, String.class, String.class);
            if (inputMap == null)
                return MapMessage.errorMessage();
            /**
             * 过滤XSS攻击
             */
            inputMap.put("title", StringUtils.cleanXSS(inputMap.get("title")));
            User user = currentUser();
            Long userId = user.getId();
            Integer userType = 1;
            if (currentUserRoleTypes().contains(RoleType.ROLE_RESEARCH_STAFF)) {
                userType = 2;
            }
            TtsListeningService service = ttsListeningServiceClient.getRemoteReference();
            String authorName = "";
            if (user.getProfile() != null && user.getProfile().getRealname() != null) {
                authorName = user.getProfile().getRealname();
            }
            TtsListeningPaper paper = new TtsListeningPaper();
            paper.setAuthor(userId);
            paper.setAuthorName(authorName);
            paper.setUserType(userType);
            paper.setTitle(inputMap.get("title"));
            if (inputMap.get("id") != null) {
                paper.setId(inputMap.get("id"));
            }
            paper.setBookId(conversionService.convert(inputMap.get("bookId"), Long.class));
            paper.setBookName(inputMap.get("bookName"));
            paper.setClassLevel(conversionService.convert(inputMap.get("classLevel"), Integer.class));
            paper.setCreateDatetime(new Date());
            paper.setRichText(inputMap.get("richText"));
            paper.setShare(conversionService.convert(inputMap.get("share"), Integer.class));
            mapMessage = TtsListeningGenerator.generatePaperVoiceByPaper(commonConfiguration.getTts_url(), paper);
            if (mapMessage.isSuccess()) {
                if (paper.getId() != null) {
                    TtsListeningPaper old = service.getListeningPaperById(paper.getId());
                    if (old != null)
                        paper.setCreateDatetime(old.getCreateDatetime());
                }
                String id = service.saveListeningPaper(paper);
                mapMessage.set("value", id);
            }
        } catch (Exception ex) {
            mapMessage = MapMessage.errorMessage("生成音频失败，请稍候再试。");
            logger.error("FLASH音频生成失败", ex);
        }

        return mapMessage;
    }

    /**
     * FLASH文本音频试听
     */
    @RequestMapping(value = "/tts/listening/generateVoiceFromFlash.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateVoiceFromFlash(@RequestBody String xml) {
        MapMessage mapMessage;
        try {
            mapMessage = TtsListeningGenerator.generateVoice(commonConfiguration.getTts_url(), xml);
        } catch (Exception ex) {
            mapMessage = MapMessage.errorMessage("生成音频失败，请稍候再试。");
            logger.error("FLASH音频生成失败", ex);
        }
        return mapMessage;
    }


    /**
     * HTML听力试卷音频生成
     */
    @RequestMapping(value = "/tts/listening/generatePaper.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage generateListeningPaper(@RequestBody String json) {
        MapMessage mapMessage = new MapMessage();
        try {
            User user = currentUser();
            Long userId = user.getId();
            Integer userType = 1;
            if (currentUserRoleTypes().contains(RoleType.ROLE_RESEARCH_STAFF)) {
                userType = 2;
            }
            TtsListeningService service = ttsListeningServiceClient.getRemoteReference();
            String authorName = "";
            if (user.getProfile() != null && user.getProfile().getRealname() != null) {
                authorName = user.getProfile().getRealname();
            }
            TtsListeningPaper paper = TtsListeningGenerator.generatePaperVoice(commonConfiguration.getTts_url(), json, userId, userType, authorName);
            if (paper != null) {
                if (paper.getId() != null) {
                    TtsListeningPaper old = service.getListeningPaperById(paper.getId());
                    if (old != null)
                        paper.setCreateDatetime(old.getCreateDatetime());
                }
                String id = service.saveListeningPaper(paper);
                mapMessage.setSuccess(true);
                mapMessage.set("value", id);
            } else {
                mapMessage.setSuccess(false);
            }

        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            logger.error("听力试卷音频生成失败", ex);
        }

        return mapMessage;
    }

    /**
     * 获取下载完整MP3地址
     */
    @RequestMapping(value = "/tts/listening/getCompleteVoice.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage getCompleteVoice() {
        MapMessage mapMessage = new MapMessage();
        try {
            TtsListeningService service = ttsListeningServiceClient.getRemoteReference();
            String paperId = getRequestString("paperId");
            TtsListeningPaper paper = service.getListeningPaperById(paperId);
            if (paper == null) {
                mapMessage.setSuccess(false);
                logger.error("找不到听力试卷，id=" + paperId);
                return mapMessage;
            }
            String url = commonConfiguration.getTts_url().replace("tts.php", "concatenate.php");
            String id = TtsListeningGenerator.getCompleteVoice(url, paper);
            mapMessage.setSuccess(true);
            mapMessage.set("value", "/tts_downloadMP3.vpage?id=" + id);
        } catch (Exception ex) {
            mapMessage.setSuccess(false);
            logger.error("听力试卷音频生成失败", ex);
        }

        return mapMessage;
    }

    /**
     * Mp3下载
     */
    @RequestMapping(value = "/tts_downloadMP3.vpage", method = RequestMethod.GET)
    @ResponseBody
    public void downloadListeningMP3(@RequestParam("id") String id) {
        try {
            String fileName = String.format("tts_%s.mp3", DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE));
            OutputStream outputStream = getOutputStreamForDownloading(fileName, "audio/mpeg");
            teacherTtsDownloader.downloadTtsMp3(id, outputStream);
            outputStream.flush();
            outputStream.close();
        } catch (Exception ex) {
            logger.error("Error occurs when downloading tts mp3 '{}'", id, ex);
        }
    }

    @RequestMapping(value = "/tts_downloadHelpDoc.vpage", method = RequestMethod.GET)
    @ResponseBody
    public void downloadHelpDoc(HttpServletRequest request, HttpServletResponse response) {
        try {
            FileDownloader.downloadSpecificFile(request, response, "/public/downloadtemplate/tts_o2o_helper.doc", "使用说明书.doc");
        } catch (IOException ex) {
//            log.error("downloadletter Error", ex.getMessage());
        }

    }

    /**
     * 获取ZipOutputStream，浏览器下载压缩文件
     * 直接拿到ZipOutputStream，减少拷贝次数
     */
    public ZipArchiveOutputStream getZipOutputStreamForDownloading(String filename) throws IOException {
        getWebRequestContext().getResponse().reset();
        filename = UtopiaHttpRequestContext.attachmentFilenameEncoding(filename, getWebRequestContext().getRequest());
        getWebRequestContext().getResponse().addHeader("Content-Disposition", "attachment;filename=" + filename);
        getWebRequestContext().getResponse().setContentType("application/x-zip-compressed");

        ZipArchiveOutputStream zos = new ZipArchiveOutputStream(getWebRequestContext().getResponse().getOutputStream());
        zos.setEncoding("GBK");
        return zos;
    }

    /**
     * 获取OutputStream，浏览器下载文件
     */
    public OutputStream getOutputStreamForDownloading(String filename,
                                                      String contentType) throws IOException {
        getWebRequestContext().getResponse().reset();
        filename = UtopiaHttpRequestContext.attachmentFilenameEncoding(filename, getWebRequestContext().getRequest());
        getWebRequestContext().getResponse().addHeader("Content-Disposition", "attachment;filename=" + filename);
        getWebRequestContext().getResponse().setContentType(contentType);
        return getWebRequestContext().getResponse().getOutputStream();
    }

    /**
     * 从url中获取id
     * 这里因为之前有没有前缀的数据，所以做了兼容性处理
     *
     * @param url
     * @return
     * @author changyuan.liu
     */
    private String getTtsOfflinePaperGfsFileId(String url) {
        if (url == null) return null;
        int ind;
        if (url.contains(TtsListeningService.TTS_O2O_FILE_PREFIX)) {
            ind = url.indexOf(TtsListeningService.TTS_O2O_FILE_PREFIX)
                    + TtsListeningService.TTS_O2O_FILE_PREFIX.length();
        } else {
            ind = url.lastIndexOf("/") + 1;
        }
        return url.substring(ind);
    }
}
