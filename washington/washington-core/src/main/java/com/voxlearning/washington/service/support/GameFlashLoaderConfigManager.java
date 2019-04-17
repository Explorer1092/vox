/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.service.support;

import com.voxlearning.utopia.core.cdn.CdnResourceUrlGenerator;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import lombok.Cleanup;
import org.apache.commons.io.IOUtils;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

@Named
public class GameFlashLoaderConfigManager {

    @Inject private CdnResourceUrlGenerator cdnResourceUrlGenerator;
    @Inject private PracticeLoaderClient practiceLoaderClient;

    protected File getWebRootFile(String fn) {
        WebApplicationContext webApplicationContext = ContextLoader.getCurrentWebApplicationContext();
        ServletContext servletContext = webApplicationContext.getServletContext();
        String path = servletContext.getRealPath(fn);
        return new File(path);
    }


    protected List<String> readWebRootFileLines(String fn) {
        try {
            @Cleanup FileInputStream fis = new FileInputStream(getWebRootFile(fn));
            return IOUtils.readLines(fis);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param m
     * @param request
     * @param file     页面加载flash文件，如果是框架类游戏先加载框架flash文件，否则直接加载游戏flash文件
     * @param gameName 游戏名称，供页面框架flash内部加载游戏flash文件
     */
    public void setupFlashUrl(Map m, HttpServletRequest request, String file, String gameName) {
        //FIXME: 目前这个路径先写死了，将来可能需要与 @app.flash 等统一到一套体系下。
        PracticeType englishPractice = practiceLoaderClient.loadNamedPractice(file);
        String flashPath = "/resources/apps/flash/" + file + ".swf";
        String appUrl = "/resources/apps/flash/" + gameName + ".swf";
        if (englishPractice != null && englishPractice.isIslandPractice()) {
            flashPath = "/resources/apps/flash/" + file + ".swf";
        }

        //standard
        m.put("flashURL", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashPath));
        //deprecated, should be remove when flash load are ready
        m.put("flashUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashPath));
        //框架类游戏内部加载flash文件的地址
        m.put("appUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, appUrl));
        setupFlashComponentUrl(m, request, gameName);
    }

    /**
     * @param m
     * @param request
     * @param file     页面加载flash文件，如果是框架类游戏先加载框架flash文件，否则直接加载游戏flash文件
     * @param gameName 游戏名称，供页面框架flash内部加载游戏flash文件
     * 英语迁移新体系兼容课外乐园走遍美国、通天塔、沃克大冒险（奇幻探险）flash的路径偶都改成flashv1
     */
    public void setupFlashV1Url(Map m, HttpServletRequest request, String file, String gameName) {
        //FIXME: 目前这个路径先写死了，将来可能需要与 @app.flash 等统一到一套体系下。
        PracticeType englishPractice = practiceLoaderClient.loadNamedPractice(file);
        String flashPath = "/resources/apps/flashv1/" + file + ".swf";
        String appUrl = "/resources/apps/flashv1/" + gameName + ".swf";
        if (englishPractice != null && englishPractice.isIslandPractice()) {
            flashPath = "/resources/apps/flashv1/" + file + ".swf";
        }

        //standard
        m.put("flashURL", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashPath));
        //deprecated, should be remove when flash load are ready
        m.put("flashUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashPath));
        //框架类游戏内部加载flash文件的地址
        m.put("appUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, appUrl));
        setupFlashV1ComponentUrl(m, request, gameName);
    }

    public String getFlashUrl(HttpServletRequest request, String url) {
        return cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, url);
    }

    public void setupFlashComponentUrl(Map m, HttpServletRequest request, String gameName) {
        String flashFutureGamePath = "/resources/apps/flash/future/game.swf";
        m.put("flashGameCoreUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashFutureGamePath));

        Map<String, String> cfg = cdnResourceUrlGenerator.getFlashIniConfig().get(gameName);
        if (cfg != null) {
            String logic = cfg.get("Logic");
            if (logic != null) {
                //FIXME: 目前这个路径先写死了，将来可能需要与 @app.flash 等统一到一套体系下
                String up = "/resources/apps/flash/future/logics/" + logic + ".swf";
                m.put("flashLogicUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, up));
            }
            String engine = cfg.get("Engine");
            if (engine != null) {
                //FIXME: 目前这个路径先写死了，将来可能需要与 @app.flash 等统一到一套体系下
                String up = "/resources/apps/flash/future/gameengines/" + engine + ".swf";
                m.put("flashEngineUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, up));
            }
        }
    }

    public void setupFlashV1ComponentUrl(Map m, HttpServletRequest request, String gameName) {
        String flashFutureGamePath = "/resources/apps/flashv1/future/game.swf";
        m.put("flashGameCoreUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, flashFutureGamePath));

        Map<String, String> cfg = cdnResourceUrlGenerator.getFlashIniConfig().get(gameName);
        if (cfg != null) {
            String logic = cfg.get("Logic");
            if (logic != null) {
                //FIXME: 目前这个路径先写死了，将来可能需要与 @app.flash 等统一到一套体系下
                String up = "/resources/apps/flashv1/future/logics/" + logic + ".swf";
                m.put("flashLogicUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, up));
            }
            String engine = cfg.get("Engine");
            if (engine != null) {
                //FIXME: 目前这个路径先写死了，将来可能需要与 @app.flash 等统一到一套体系下
                String up = "/resources/apps/flashv1/future/gameengines/" + engine + ".swf";
                m.put("flashEngineUrl", cdnResourceUrlGenerator.combineCdnUrl(request, CdnResourceUrlGenerator.CdnType_Auto, up));
            }
        }
    }

}
