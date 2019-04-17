package com.voxlearning.utopia.admin.controller.audit;

import com.voxlearning.utopia.admin.controller.crm.CrmAbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * AuditIndexController
 *
 * @author song.wang
 * @date 2017/1/13
 */
@Controller
@RequestMapping("/audit")
public class AuditIndexController extends CrmAbstractController {
    @RequestMapping(value = "auditindex.vpage", method = RequestMethod.GET)
    public String index() {
        return "audit/auditindex";
    }
}
