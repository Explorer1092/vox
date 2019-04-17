package com.voxlearning.luffy.controller.piclisten;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramBookService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramCheckService;
import com.voxlearning.utopia.service.piclisten.api.MiniProgramReadService;
import com.voxlearning.utopia.service.piclisten.client.MiniProgramBookServiceClient;
import com.voxlearning.utopia.service.piclisten.client.MiniProgramCheckServiceClient;
import com.voxlearning.utopia.service.piclisten.client.MiniProgramReadServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

/**
 * Not must login
 *
 * @author RA
 */
@Controller
@RequestMapping(value = "/xcx/piclisten")
public class ParentXcxPicListenOpenController extends AbstractXcxPicListenController {

    @Inject
    private MiniProgramBookServiceClient miniProgramBookServiceClient;
    @Inject
    private MiniProgramCheckServiceClient miniProgramCheckServiceClient;
    @Inject
    private MiniProgramReadServiceClient miniProgramReadServiceClient;


    @Override
    public boolean onBeforeControllerMethod() {
        return true;
    }


    @RequestMapping("/index.vpage")
    @ResponseBody
    public MapMessage index() {
        return wrapper((mm) -> {
            Long uid = uid();

            if (currentParent() != null) {
                mm.putAll(checkService().loadCheckData(uid));
                mm.putAll(readService().getTodayReadData(pid(), uid));
            }
            mm.put("today_checked", checkService().getTodayCheckCount());

        });

    }

    @RequestMapping(value = "/clazz_publisher.vpage")
    @ResponseBody
    public MapMessage clazzLevelTermList() {
        return wrapper(mm -> {
            mm.putAll(bookService().classLevelTerm(uid()));
        });

    }


    @RequestMapping(value = "/book/list.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bookList(HttpServletRequest request) {

        Long pid = pid();
        Long uid = uid();

        int clazzLevel = getRequestInt("clazz_level");
        String publishId = getRequestString("publisher_id");

        if (clazzLevel <= 0) {
            clazzLevel = 3; // Default
            if (uid != null && uid > 0) {
                StudentDetail studentDetail = studentLoaderClient.loadStudentDetail(uid);
                if (studentDetail != null) {
                    if (getClazzLevelcBook(studentDetail) > 0) {
                        clazzLevel = getClazzLevelcBook(studentDetail);
                    }
                }
            }
        }

        int clazz = clazzLevel;
        String sys = sys();

        return wrapper((mm) -> {
            mm.putAll(bookService().bookList(uid, pid, clazz, publishId, sys, getCdnBaseUrlStaticSharedWithSep(request)));
        });
    }

    @RequestMapping(value = "/book/detail.vpage", method = {RequestMethod.GET})
    @ResponseBody
    public MapMessage bookDetail(HttpServletRequest request) {
        Long pid = pid();
        Long uid = uid();
        String sys = sys();
        String bookId = getRequestString("book_id");

        return wrapper((mm) -> {
            mm.putAll(bookService().bookDetail(uid, pid, bookId, sys, getCdnBaseUrlStaticSharedWithSep(request)));
        });

    }


    private Integer getClazzLevelcBook(StudentDetail studentDetail) {
        if (studentDetail.isJuniorStudent()
                || (studentDetail.getClazz() != null && studentDetail.getClazz().isTerminalClazz()))
            return 7;
        if (studentDetail.isInfantStudent())
            return 1;
        if (studentDetail.getClazz() == null)
            return 3;
        return studentDetail.getClazzLevelAsInteger();
    }


    private MiniProgramBookService bookService() {
        return miniProgramBookServiceClient.getRemoteReference();
    }

    private MiniProgramCheckService checkService() {
        return miniProgramCheckServiceClient.getRemoteReference();
    }

    private MiniProgramReadService readService() {
        return miniProgramReadServiceClient.getRemoteReference();
    }

    private String getCdnBaseUrlStaticSharedWithSep(HttpServletRequest request) {
        return cdnResourceUrlGenerator.getCdnBaseUrlStaticSharedWithSep(request);
    }


    private String getCdnBaseUrlAvatarWithSep(HttpServletRequest request) {
        return cdnResourceUrlGenerator.getCdnBaseUrlAvatarWithSep(request);
    }


}
