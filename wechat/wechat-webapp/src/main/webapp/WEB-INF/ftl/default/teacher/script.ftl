<@sugar.capsule js=["jquery","fastClick",'requirejs'] />
<#--基本配置-->
<#assign singleJsConfigModule =  {
    <#--登录-->
    "teacherLogin" : {'url' : "public/js/teacher/teacherLogin",'useJsKid': true},

    "weuijs" : {'url' : "public/js/weui/weui",'useJsKid': true},


    <#--日期相关-->
    "picker" : {'url' : "public/lib/datetimepicker/picker",'useJsKid': true},
    "pickerDate" : {'url' : "public/lib/datetimepicker/picker.date",'useJsKid': true},
    "pickerTime" : {'url' : "public/lib/datetimepicker/picker.time",'useJsKid': true},


    <#--作业相关-->
    "homeworkIndex" : {'url' : "public/js/teacher/homework/homeworkIndex",'useJsKid': true},
    "package" : {'url' : "public/js/teacher/homework/package",'useJsKid': true},
    "history" : {'url' : "public/js/teacher/homework/report/history",'useJsKid': true},
    "reportDetail" : {'url' : "public/js/teacher/homework/report/reportDetail",'useJsKid': true},
    "basicAppDetail" : {'url' : "public/js/teacher/homework/report/basicAppDetail",'useJsKid': true},
    "crd" : {'url' : "public/js/teacher/homework/report/crd",'useJsKid': true},
    "quickremarks" : {'url' : "public/js/teacher/homework/report/quickremarks",'useJsKid': true},
    "quickaward" : {'url' : "public/js/teacher/homework/report/quickaward",'useJsKid': true},
    "wx" : {'url' : "public/lib/weixin/jweixin-1.0.0",'useJsKid': false},
    "scorerule" : {'url' : "public/js/teacher/homework/report/scorerule",'useJsKid': true},

    "englishExam" : {'url' : "public/js/teacher/homework/englishexam",'useJsKid': true},
    "basicapp" : {'url' : "public/js/teacher/homework/basicapp",'useJsKid': true},
    "reading" : {'url' : "public/js/teacher/homework/reading",'useJsKid': true},
    "oralpractice":{'url' : "public/js/teacher/homework/oralpractice",'useJsKid': true},
    "voiceRecommend" : {'url' : "public/js/teacher/homework/report/voiceRecommend",'useJsKid': true},


    <#--音频播放-->
    "jp" : {'url' : "public/lib/jplayer/jquery.jplayer",'useJsKid': true},

    "radialIndicator":{'url' : "public/lib/radialIndicator/radialIndicator.min",'useJsKid': false},

    <#--推荐使用swiper3-->
    "swiper3" : {'url' : "public/lib/swiper/js/swiper.min","useJsKid" : false},

    <#--会场历程活动-->
    "dgmeeting" : {'url' : "public/js/teacher/activity/dgmeeting",'useJsKid': true},
    "flexslider" : {'url' : "public/lib/jquery.flexslider/jquery.flexslider.min","useJsKid" : false},
    "swiper" : {'url' : "public/lib/swiper/swiper2.min","useJsKid" : false},
    "swiperAni" : {'url' : "public/lib/swiper/swiper.animate1.0.2.min","useJsKid" : false},
    <#-- 假期老师预选教材 -->
    "recommendbook" : {'url' : "public/js/teacher/activity/recommendbook",'useJsKid': true},
    <#-- 假期老师预选教材 微信专版-->
    "recommendbookwechat" : {'url' : "public/js/teacher/activity/recommendbookwechat",'useJsKid': true},


    <#--班级管理-->
    "clazzManage":{'url' : "public/js/teacher/clazzmanage/clazzManage",'useJsKid': true},
    "editClazz":{'url' : "public/js/teacher/clazzmanage/editClazz",'useJsKid': true},
    "editStudent":{'url' : "public/js/teacher/clazzmanage/editStudent",'useJsKid': true},
    "resetPwd":{'url' : "public/js/teacher/clazzmanage/resetPwd",'useJsKid': true},
    "createClazz":{'url' : "public/js/teacher/clazzmanage/createClazz",'useJsKid': true},
    "transferclazz":{'url' : "public/js/teacher/clazzmanage/transferclazz",'useJsKid': true},
    "addTeacher":{'url' : "public/js/teacher/clazzmanage/addTeacher",'useJsKid': true},
    "teacherList":{'url' : "public/js/teacher/clazzmanage/teacherList",'useJsKid': true},

    <#--活动-->

    "teachersDay2016" : {'url' : "public/js/teacher/activity/teachersDay2016",'useJsKid': true},
    "teachersDayShare" : {'url' : "public/js/teacher/activity/teachersDayShare",'useJsKid': true},


    <#--题库相关-->
    "examCore_new" : {'url' : "",'useJsKid': true},

    <#--作业单-->
    "offlineHomeworkIndex" :{'url':"public/js/teacher/homework/offlinehomework/offlineHomeworkIndex",'useJsKid':true},
    "offlineHomeworkShare" :{'url':"public/js/teacher/homework/offlinehomework/offlineHomeworkShare",'useJsKid':true},
    "offlineHomeworkDetail" :{'url':"public/js/teacher/homework/offlinehomework/offlineHomeworkDetail",'useJsKid':true}
}/>

<#include "../basescript.ftl">
