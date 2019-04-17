package com.voxlearning.utopia.admin.controller.chips;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.service.ai.api.ChipsStudyArticleService;
import com.voxlearning.utopia.service.ai.entity.ChipsStudyArticle;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Date;

/**
 * @author guangqing
 * @since 2019/3/19
 */
@Controller
@RequestMapping("/chips/studyInfo")
public class StudyInformationController extends AbstractAdminSystemController {

    @ImportService(interfaceClass = ChipsStudyArticleService.class)
    private ChipsStudyArticleService chipsStudyArticleService;

    /**
     * 图文素材列表入口
     */
    @RequestMapping(value = "list.vpage", method = RequestMethod.GET)
    public String list(Model model) {
        return "chips/studyInfo/index";
    }

    /**
     * 查询图文素材列表数据
     */
    @RequestMapping(value = "listData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage listData() {
        return chipsStudyArticleService.loadArticleListForCrm();
    }


    /**
     * 新增，编辑入口
     */
    @RequestMapping(value = "editIndex.vpage", method = RequestMethod.GET)
    public String editIndex(Model model) {
        return "chips/studyInfo/editIndex";
    }

    @RequestMapping(value = "editData.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage editData() {
        String  articleId = getRequestString("articleId");
        return chipsStudyArticleService.loadArticleForCrm(articleId);
    }
    /**
     * 保存
     */
    @RequestMapping(value = "save.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage editSave() {
        String id = getRequestString("id");
        String title = getRequestString("title");
        String shareIcon = getRequestString("shareIcon");
        String content = getRequestString("content");
        ChipsStudyArticle article = new ChipsStudyArticle();
        if (StringUtils.isNotBlank(id)) {
            article.setId(id);
        }
        article.setTitle(title);
        article.setShareIcon(shareIcon);
        article.setContent(content);
//        article.setCreateDate(new Date());
//        article.setUpdateDate(new Date());
        return chipsStudyArticleService.upsertStudyArticle(article);
    }
}
