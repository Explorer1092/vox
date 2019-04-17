package com.voxlearning.utopia.admin.controller.site;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 *
 * Created by Shuai Huan on 2014/10/30.
 */
@Controller
@RequestMapping("/site/thirdparty")
public class SiteThirdPartyManagement extends SiteAbstractController{

    @RequestMapping(value = "travelAmerica.vpage", method = RequestMethod.GET)
    String travelAmerica() {
        return "site/batch/batchamerican";
    }
}
