<#--业务相关js 模块引入配置-->

<#--基本配置-->
<@sugar.capsule js=["fastclick",'requirejs'] />

<#assign singleJsConfigModule =  {
<#--工具-->
"common" : {'url' : "/public/rebuildRes/js/common/common"},
<#--插件-->
"echarts" : {'url' : "/public/rebuildRes/lib/echarts/echarts.min"},
"highcharts" : {'url' : "/public/rebuildRes/lib/echarts/highcharts"},
"swiper3" : {'url' : "/public/rebuildRes/lib/swiper3/js/swiper.min"},
"radialIndicator" : {'url' : "/public/rebuildRes/lib/radialIndicator/radialIndicator"},

<#--登录验证相关-->
"login" : {'url' : "/public/rebuildRes/js/mobile/auth/login"},
"dispatchEvent" : {'url' : "/public/rebuildRes/common/dispatchEvent"},
"loginByMobile" : {'url' : "/public/rebuildRes/js/mobile/auth/loginByMobile"},
"performanceIndex" : {'url' : "/public/rebuildRes/js/mobile/performance/performanceIndex"},
"searchIndex" : {'url' : "/public/rebuildRes/js/mobile/search/searchIndex"},
"taskIndex" : {'url' : "/public/rebuildRes/js/mobile/task/taskIndex"},

<#--新设计版本-->
<#--主页-->
"homeIndex" : {'url' : "/public/rebuildRes/js/mobile/home/homeIndex"},
"trendGraph" : {'url' : "/public/rebuildRes/js/mobile/home/trendGraph"},
"trendGraph_klx" : {'url' : "/public/rebuildRes/js/mobile/home/trendGraph_klx"},
"groupOverView" : {'url' : "/public/rebuildRes/js/mobile/home/groupOverView"},
"schoolOverView" : {'url' : "/public/rebuildRes/js/mobile/home/schoolOverView"},
"information" : {'url' : "/public/rebuildRes/js/mobile/home/information"},
"sortTable" : {'url' : "/public/rebuildRes/js/mobile/home/sortTable"},
"informationNew" : {'url' : "/public/rebuildRes/js/mobile/home/informationNew"},
"groupSchoolList" : {'url' : "/public/rebuildRes/js/mobile/home/groupSchoolList"},
"schoolPerformance" : {'url' : "/public/rebuildRes/js/mobile/home/schoolPerformance"},
"schoolPerformance_klx" : {'url' : "/public/rebuildRes/js/mobile/home/schoolPerformance_klx"},
"tianji_proclama" : {'url' : "/public/rebuildRes/js/mobile/home/tianji_proclama"},
"regionmsg" : {'url' : "/public/rebuildRes/js/mobile/home/regionmsg"},

<#--团队-->
<#--"teamAgent" : {'url' : "/public/rebuildRes/js/mobile/team/teamAgent"},-->
"groupmeeting" : {'url' : "/public/rebuildRes/js/mobile/team/groupmeeting"},
"teamCountry" : {'url' : "/public/rebuildRes/js/mobile/team/teamCountry"},

<#--进校-->
"addVisitPlan" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/addVisitPlan"},
"addMeeting" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/addMeeting"},
"joinMeeting" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/joinMeeting"},
"schoolCard" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/schoolCard"},
"schoolOffline" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/schoolOffline"},
"chooseSchool" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/chooseSchool"},
"userMemorandum" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/userMemorandum"},
"visitplan" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/visitplan"},
"add_group_meeting" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/add_group_meeting"},
"add_intoschool_record" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/add_intoschool_record"},
"add_intoschool" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/add_intoschool"},
"chooseTeacher" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/chooseTeacher"},
"edit_visit_record" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/edit_visit_record"},
"add_visit" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/add_visit"},
<#--产品反馈-->
"productTeacher" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/productTeacher"},
"feedbackList" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/feedbackList"},
"feedback" : {'url' : "/public/rebuildRes/js/mobile/intoSchool/feedback"},
<#--消息-->
"noticeIndex" : {'url' : "/public/rebuildRes/js/mobile/notice/noticeIndex"},
<#--工作记录-->
"work_statistic" : {'url' : "/public/rebuildRes/js/mobile/workRecord/work_statistic"},
<#--我的申请-->
"workflow" : {'url' : "/public/rebuildRes/js/mobile/workflow/workflow"},
"todo_done" : {'url' : "/public/rebuildRes/js/mobile/workflow/todo_done"},
"memorandum" : {'url' : "/public/rebuildRes/js/mobile/workflow/memorandum"},
"memorandumList" : {'url' : "/public/rebuildRes/js/mobile/workflow/memorandumList"},
"application" : {'url' : "/public/rebuildRes/js/mobile/workflow/application"},
"workflow_process" : {'url' : "/public/rebuildRes/js/mobile/workflow/workflow_process"},
<#--老师-->
"recordteacherjob" : {'url' : "/public/rebuildRes/js/mobile/school/recordteacherjob"},
"topschool" : {'url' : "/public/rebuildRes/js/mobile/school/topschool"},
"clazzapply" : {'url' : "/public/rebuildRes/js/mobile/school/clazzapply"},
"createdSchoolList" : {'url' : "/public/rebuildRes/js/mobile/school/createdSchoolList"},
"editSchool" : {'url' : "/public/rebuildRes/js/mobile/school/editSchool"},
"newschool" : {'url' : "/public/rebuildRes/js/mobile/school/newschool"},
"teacherCard" : {'url' : "/public/rebuildRes/js/mobile/school/teacherCard"},
"schoolAnalysis" : {'url' : "/public/rebuildRes/js/mobile/school/schoolAnalysis"},
"teacherauth" : {'url' : "/public/rebuildRes/js/mobile/school/teacherauth"},
"schoolDetail" : {'url' : "/public/rebuildRes/js/mobile/school/schoolDetail"},
"schoolConfirm" : {'url' : "/public/rebuildRes/js/mobile/school/schoolConfirm"},
"updateSchool" : {'url' : "/public/rebuildRes/js/mobile/school/updateSchool"},
"schoolChose" : {'url' : "/public/rebuildRes/js/mobile/school/schoolChose"},
"dictSchoolApply" : {'url' : "/public/rebuildRes/js/mobile/school/dictSchoolApply"},
"researchVisit" : {'url' : "/public/rebuildRes/js/mobile/school/researchVisit"},
"schoolappraisal" : {'url' : "/public/rebuildRes/js/mobile/school/schoolappraisal"},
"schoolPhoto" : {'url' : "/public/rebuildRes/js/mobile/school/schoolPhoto"},
<#--客服协助-->
"customer" : {'url' : "/public/rebuildRes/js/mobile/customer/customer"},
<#--我的-->
"researcher_res" : {'url' : "/public/rebuildRes/js/mobile/my/researcher_res"},
"newResearch" : {'url' : "/public/rebuildRes/js/mobile/my/newResearch"},
"cusList" : {'url' : "/public/rebuildRes/js/mobile/my/cusList"}
}/>

<#include "basescript.ftl">
