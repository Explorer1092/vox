<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<script src="/public/js/jquery-1.9.1.min.js"></script>
<script src="/public/rebuildRes/lib/handlebar/handlebars-v4.0.5.js"></script>
<script src="/public/rebuildRes/js/mobile/home/homeIndex.js?v=201803301"></script>
<style>
    body{background-color: #f9f9fa;}
    .phe-content div{text-align:right;}
    .apply_box{overflow:hidden;background-color: #fff;width:100%}
    .apply_box .title{padding:.5rem 0.35rem;color:#3b73af;font-size:.65rem}
    .apply_box ul{overflow:hidden}
    .apply_box ul.list_two li{width:50%}
    .apply_box ul.list_forth li{width:25%}
    .apply_box li{margin:.25rem 0;float:left;text-align:center}
    .apply_box li .image{position: relative; margin:0 auto;width:1.2rem;height:1.2rem;}
    .apply_box li .image span.icon{border-radius:100%;text-align:center;color: white;width:.8rem;height:.8rem;display: inline-block;font-size:.45rem;line-height:.8rem;background:red;position:absolute;top: -.3rem;left: 60%;}
    .apply_box li .image span.icons{border-radius:100%;text-align:center;color: white;width:.5rem;height:.5rem;display: inline-block;font-size:.45rem;line-height:.8rem;background:red;position:absolute;top: -.2rem;left: 80%;}
    .apply_box li .info{padding:.2rem 0 0 0;color:#636880;font-size:.45rem}
    .resources-box .res-list{font-family: "Heiti SC","Rotobo",Arial;position: relative;padding:0 0.35rem;}
    .resources-box .res-list li{position:relative;float:left;width:33.3%;padding: 0.2rem 0;text-align:center;cursor:pointer}
    .resources-box .res-list li:after{position: absolute;content: "";width: 100%;height: 1px;overflow: hidden;bottom: 0;left: 0;background-color: #e9e9e9;}
    .resources-box .res-list li:first-child:after{left: 14%;}
    .resources-box .res-list li:last-child:after{right: 14%;left: auto;}
    .resources-box .res-list:last-child li:after{height: 0;}
    .resources-box .res-list li .sub{font-size:1rem;color:#ff7d5a;line-height:1.25rem}
    .resources-box .res-list li .volume{font-size:.75rem;line-height:1.25rem;color: #666}
</style>
<@layout.page title="我的业绩" pageJs="" footerIndex=1 navBar="show">
    <@sugar.capsule css=['new_home','swiper3']/>
<div class="home-box">
    <div class="c-head fixed-head">
        <span>首页</span>
    </div>
    <div class="h-content">
        <@apptag.pageElement elementCode="b30cef186ee84717">
            <div class="h-head">同步数据日期：${date?string("yyyy-MM-dd")!}</div>
        </@apptag.pageElement>
        <div class="swiper-container">
            <div class="performanceData" style="padding:.5rem 0;background:#fff;display: none;">
                <div class="apply_box1 resources-box"></div>
            </div>
            <#if !requestContext.getCurrentUser().isProductOperator()>
            <#if !requestContext.getCurrentUser().isCountryManager()>
                <div class="h-main" style="margin-top:.5rem">
                    <div class="h-side" style="border:none">
                        <#if requestContext.getCurrentUser().isRegionManager()>
                            <span style="margin-bottom:1rem;"><a href="javascript:void(0);" class="js-regionMsg">编辑</a></span>
                        </#if>
                    <@apptag.pageElement elementCode="23e594011f5a4bc7">
                        <p style="max-height: 2.4rem;overflow: hidden;"><i class="h-ico-2"></i>大区寄语：<span id="regionMsgDiv"></span></p>
                    </@apptag.pageElement>
                    </div>
                </div>
            </#if>
                <div class="apply_box" style="background-color: #f9f9fa">
                    <#if requestContext.getCurrentUser().isCityManager() || requestContext.getCurrentUser().isBusinessDeveloper()|| requestContext.getCurrentUser().isCountryManager()|| requestContext.getCurrentUser().isRegionManager()|| requestContext.getCurrentUser().isAreaManager()>
                        <div class="title" style="">任务</div>
                        <ul class="list_forth" style="background-color: #fff;padding:.5rem 0">
                        <@apptag.pageElement elementCode="8fe02f9c840f41b2">
                            <li><a href="javascript:;" onclick="openSecond('/mobile/performance/clazz_alter_statistics.vpage')"><div class="image changeClass"><#if teacherCount?? && teacherCount !=0><span class="icon">${teacherCount!0}</span></#if></div><div class="info">处理换班</div></a></li>
                        </@apptag.pageElement>
                            <#if showMaintainTeacher>
                            <li><a href="javascript:;" onclick="openSecond('/view/mobile/crm/task/maintain_teacher.vpage')"><div class="image icon_task_maintain"><#if unFinishedTaskNum?? && unFinishedTaskNum !=0><span class="icons"></span></#if></div><div class="info">维护老师</div></a></li>
                            </#if>
                            <li class="competior"></li>
                        </ul>
                    </#if>
                    <div class="title">百宝箱</div>
                    <ul class="list_forth" style="background-color: #fff;padding:.5rem 0">
                        <li>
                            <a class="teacherAuth" href="javascript:void(0)">
                                <div class="teacherauth image"><span style="position: relative;"><#if teacherAuthCount?? && teacherAuthCount !=0><div style="border-radius:100%;text-align:center;color: white;width:.8rem;height:.8rem;display: inline-block;font-size:.45rem;line-height:.8rem;background:red;position:absolute;top: -.3rem;left: 60%;">${teacherAuthCount!0}</div></#if></span></div><div class="info">新老师认证</div>
                            </a>
                        </li>
                        <@apptag.pageElement elementCode="c5dbbebfd90b4af0">
                            <li>
                                <a href="javascript:void(0)" onclick="into_school()">
                                    <div class="analysis image"></div>
                                    <div class="info">拜访统计</div>
                                </a>
                            </li>
                        </@apptag.pageElement>
                        <li>
                            <a href="javascript:void(0)" onclick="schoolAnalysis()">
                                <div class="analysis image"></div><div class="info">学校增长情况</div>
                            </a>
                        </li>
                        <@apptag.pageElement elementCode="ac4dbba208354cdd">
                        <li>
                            <a href="javascript:void(0)" onclick="ranking()">
                                <div class="ranking image"></div><div class="info">排行榜</div>
                            </a>
                        </li>
                        </@apptag.pageElement>
                        <li>
                            <#if requestContext.getCurrentUser().isBusinessDeveloper()>
                            <a href="javascript:;" onclick="openSecond('/mobile/analysis/top_school_rankings.vpage')">
                            <#else>
                            <a href="javascript:;" onclick="openSecond('/mobile/performance/choose_agent.vpage?breakUrl=top_school_rankings&needCityManage=1')">
                            </#if>
                                <div class="analysis image"></div><div class="info">TOP校榜</div>
                            </a>
                        </li>
                    </ul>
                    <div class="title">小工具</div>
                    <ul class="list_forth" style="background-color: #fff;padding:.5rem 0">
                        <@apptag.pageElement elementCode="e03c348e37f84438">
                        <li><a onclick="openSecond('/mobile/my/customer.vpage')"><div id="callCS" class="customer image"></div><div class="info">客服咨询</div></a></li>
                        </@apptag.pageElement>
                        <@apptag.pageElement elementCode="ee3e3c0475604c5e">
                        <li><a href="javascript:;" onclick="openSecond('/mobile/feedback/view/index.vpage')"><div class="shortNote image"></div><div class="info">产品反馈</div></a></li>
                        </@apptag.pageElement>
                        <li><a href="javascript:;" onclick="openSecond('/mobile/memorandum/user_memorandum_page.vpage')"><div class="shortNote image"></div><div class="info">备忘录</div></a></li>
                        <li><a href="javascript:;" onclick="openSecond('/mobile/apply/application.vpage')"><div class="applyProcess image"></div><div class="info">申请与审批</div></a></li>

                        <#if canGotoLargeExamManage?? && canGotoLargeExamManage>
                            <li><a href="javascript:;" onclick="openSecond('/view/mobile/crm/examnation/exam_list.vpage')"><div class="examManage image"></div><div class="info">大考管理</div></a></li>
                        </#if>

                    </ul>
                </div>
            </#if>
        </div>
    </div>
    <div class="mask"></div>
</div>
<script id="competior" type="text/html">
    <a href="javascript:;" onclick="openSecond('/view/mobile/crm/school/competing_info_collect.vpage?userId=${requestContext.getCurrentUser().getUserId()!0}&userRoleType=${requestContext.getCurrentUser().getRoleList()[0]!0}')">
        <div class="image changeClass">
            <%if(res && res.waitFeedBackNum){%>
            <span style="position: relative;">
                <div style="border-radius:100%;text-align:center;color: white;width:.8rem;height:.8rem;display: inline-block;font-size:.45rem;line-height:.8rem;background:red;position:absolute;top: -.3rem;left: 60%;"><%=res.waitFeedBackNum%></div>
            </span><%}%>
        </div>
        <div class="info">竞品信息收集</div>
    </a>
</script>
<script>
    template.helper('toQfw', function (val) {
        var re = /\d{1,3}(?=(\d{3})+$)/g;
        var valStr = val + '';
        return valStr.replace(/^(\d+)((\.\d+)?)$/, function(s, s1, s2){
            return s1.replace(re, '$&,') + s2;
        });
    });
</script>
<script id="cardItemTemp" type="text/html">
    <%if( res.junior17Data){%>
    <ul class="res-list list-dif clearfix">
        <%var dataList = res.junior17Data%>
        <li class="js-item" data-id="<%=dataList.id%>" data-idtype="<%=dataList.idType%>" data-level="<%=dataList.schoolLevel%>" data-mode="1">
            <div class="volume">小英新增</div>
            <div class="sub" style="color:#000"><%=toQfw(dataList.tmFinEngHwGte3IncAuStuCount)%></div>
            <div class="volume" style="color:#ff7d5a"><%if(dataList.pdFinEngHwGte3IncAuStuCount && dataList.pdFinEngHwGte3IncAuStuCount > 0){%>+<%}%><%=toQfw(dataList.pdFinEngHwGte3IncAuStuCount)%></div>
        </li>
        <li class="js-item" data-id="<%=dataList.id%>" data-idtype="<%=dataList.idType%>" data-level="<%=dataList.schoolLevel%>" data-mode="1">
            <div class="volume">小数新增</div>
            <div class="sub" style="color:#000"><%=toQfw(dataList.tmFinMathHwGte3IncAuStuCount)%></div>
            <div class="volume" style="color:#ff7d5a"><%if(dataList.pdFinMathHwGte3IncAuStuCount && dataList.pdFinMathHwGte3IncAuStuCount > 0){%>+<%}%><%=toQfw(dataList.pdFinMathHwGte3IncAuStuCount)%></div>
        </li>
        <li class="js-item" data-id="<%=dataList.id%>" data-idtype="<%=dataList.idType%>" data-level="<%=dataList.schoolLevel%>" data-mode="1">
            <div class="volume">小语新增</div>
            <div class="sub" style="color:#000"><%=toQfw(dataList.tmFinChnHwGte3IncAuStuCount)%></div>
            <div class="volume" style="color:#ff7d5a"><%if(dataList.pdFinChnHwGte3IncAuStuCount && dataList.pdFinChnHwGte3IncAuStuCount > 0){%>+<%}%><%=toQfw(dataList.pdFinChnHwGte3IncAuStuCount)%></div>
        </li>
    </ul>
    <%}%>
    <%if( res.middle17Data){%>
    <ul class="res-list list-dif clearfix">
        <%var dataList = res.middle17Data%>
        <%var klxdataList = res.middleKlxData%>
        <li class="js-item" data-id="<%=dataList.id%>" data-idtype="<%=dataList.idType%>" data-level="<%=dataList.schoolLevel%>" data-mode="1">
            <div class="volume">中英新增</div>
            <div class="sub" style="color:#000"><%=toQfw(dataList.tmFinEngHwGte3IncAuStuCount)%></div>
            <div class="volume" style="color:#ff7d5a"><%if(dataList.pdFinEngHwGte3IncAuStuCount && dataList.pdFinEngHwGte3IncAuStuCount > 0){%>+<%}%><%=toQfw(dataList.pdFinEngHwGte3IncAuStuCount)%></div>
        </li>
        <li class="js-item" data-id="<%=dataList.id%>" data-idtype="<%=dataList.idType%>" data-level="<%=dataList.schoolLevel%>" data-mode="1">
            <div class="volume">口语测评</div>
            <div class="sub" style="color:#000"><%=toQfw(dataList.tmFinEngOralTestGte1StuCount)%></div>
            <div class="volume" style="color:#ff7d5a"><%if(dataList.pdFinEngOralTestGte1StuCount && dataList.pdFinEngOralTestGte1StuCount > 0){%>+<%}%><%=toQfw(dataList.pdFinEngOralTestGte1StuCount)%></div>
        </li>
        <li class="js-item" data-id="<%=dataList.id%>" data-idtype="<%=dataList.idType%>" data-level="<%=dataList.schoolLevel%>" data-mode="2">
            <div class="volume">常规扫描</div>
            <div class="sub" style="color:#000"><%=toQfw(klxdataList.tmFinTpGte1WithExamStuCount)%></div>
            <div class="volume" style="color:#ff7d5a"><%if(klxdataList.pdFinTpGte1WithExamStuCount && klxdataList.pdFinTpGte1WithExamStuCount > 0){%>+<%}%><%=toQfw(klxdataList.pdFinTpGte1WithExamStuCount)%></div>
        </li>
    </ul>
    <%}%>
</script>
<script>
    var has_elements_roles = false,
            show_region = false;
    <@apptag.pageElement elementCode="23e594011f5a4bc7">
        show_region = true;
    </@apptag.pageElement>
    <@apptag.pageElement elementCode="b30cef186ee84717">
        has_elements_roles = true;
    </@apptag.pageElement>
    $(document).ready(function () {
        //隐藏顶部title
        try{
            var setTopBar = {
                show:false
            };
            setTopBarFn(setTopBar);
        }catch(e){

        }
    });

    $(document).on("click",'.teacherAuth',function(){
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_VjelQG49", //打点流程模块名
            op : "o_Avfhcr60" ,//打点事件名
            userId:${requestContext.getCurrentUser().getUserId()!0}
        });
        <#if requestContext.getCurrentUser().isBusinessDeveloper()>
            openSecond("/view/mobile/crm/teacher/new_auth_teacher.vpage?userId=${requestContext.getCurrentUser().getUserId()!0}");
        <#else>
            openSecond("/view/mobile/crm/search/search_bussiness.vpage");
        </#if>
    });
    function schoolAnalysis() {
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_YZt6AGcF", //打点流程模块名
            op : "o_pol9mQg8" ,//打点事件名
            userId:${requestContext.getCurrentUser().getUserId()!0}
        });
        setTimeout('openSecond("/mobile/resource/school/school_mau_increase_statistics.vpage")',500);
    }
    function ranking(){
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_tfbfMKwr", //打点流程模块名
            op : "o_AcJ6cJ0r" ,//打点事件名
            userId:${requestContext.getCurrentUser().getUserId()!0}
        });
        setTimeout('openSecond("/view/mobile/crm/ranking/ranking_survey.vpage")',500);
    }
    <@apptag.pageElement elementCode="6fd2a90637384fd4">
        $.get("/mobile/competitive_product/wait_feedback_num.vpage",function (res) {
            $('.competior').html(template("competior",{res:res}));
        });
    </@apptag.pageElement>
    var isCountryManager = 1;
    <#if requestContext.getCurrentUser().isCountryManager()>
        isCountryManager = 2;
    </#if>
    function into_school(){
        YQ.voxLogs({
            database : "marketing", //不设置database  , 默认库web_student_logs
            module : "m_NxXmVJvE", //打点流程模块名
            op : "o_kCEBv5Yh" ,//打点事件名
            userId:${requestContext.getCurrentUser().getUserId()!0}
        });
        setTimeout('openSecond("/view/mobile/crm/visit/visit_statistics.vpage")',500);
    }
</script>
</@layout.page>
