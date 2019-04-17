<#assign apptag=JspTaglibs["/WEB-INF/tld/app-tag.tld"]/>
<#macro page title="Tianji App" pageJs="" footerIndex=1 navBar='' footer="">
<!DOCTYPE html>
<html>
<head>
    <title>${title}</title>
    <meta charset="utf-8"/>

    <#--meta 属性可配置，TO—DO-->
    <meta name="description" content="17zuoye Mobile Marketing System">
    <meta name="viewport" content="width=device-width, initial-scale=1,maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta name="apple-mobile-web-app-status-bar-style" content="black">
    <meta name="format-detection" content="telephone=no">
    <meta http-equiv="Pragma" content="no-cache">
    <meta http-equiv="Cache-control" content="no-cache">
    <meta http-equiv="Cache" content="no-cache">

    <#--依赖cdntype=skip，一个是 CdnBaseTag/CdnResourceUrlGenerator ，一个是 PageBlockContentGenerator TODO-->
    <#--<@sugar.check_the_resources/>-->

    <link rel="stylesheet" href="/public/rebuildRes/css/mobile/home/base.css?v=123"/>

    <script type="application/javascript" src="/public/rebuildRes/js/common/agentTool.js"></script>
    <script src="https://cdn-cnc.17zuoye.cn/public/script/voxLogs.js?v=2016-06-02"></script>
    <script src="/public/js/jquery-1.9.1.min.js"></script>
    <script src="/public/js/resource/common.js"></script>
    <script src="/public/rebuildRes/js/common/common.js"></script>
    <script src="/public/js/template.js"></script>
    <script src="/public/js/storage.js"></script>
    <script src="/public/js/layer/layer.js"></script>
</head>
<body>
<div role="main">
    <div class="top-holder"></div>
    <#-- header start -->
    <#--<#include "../layoutTemplate/mobileHeaderTemp.ftl">-->
    <#--<div class="headerTempContainer"></div>-->
    <#-- header end -->

    <#nested />

     <#--footer start-->
    <#if navBar=='show'>
        <#include "../layoutTemplate/mobileFooterTemp.ftl">
    </#if>

    <div class="holder"></div>
    <#--<div class="footerTempContainer"></div>-->
    <#-- footer end -->

    <#include  "../../script.ftl" />
</div>
<#if footer == "none">

<#else>
<div class="more">
    <#--测试入口-->
    <#--<#if ProductDevelopment.isDevEnv()>
    <a href="/mobile/work_record/add_intoSchool_record_test.vpage" class="resourceTitle">
        <span class="resourceIco ico01"></span>
        Test swift
    </a>
    </#if>-->

    <#--<#if !requestContext.getCurrentUser().isBusinessDeveloper()>-->
    <@apptag.pageElement elementCode="e62b29bef968449b">
    <a onclick="openSecond('/mobile/school_clue/addnewschoolpage.vpage')" class="resourceTitle">
        <span class="resourceIco ico01"></span>
        新学校
    </a>
    </@apptag.pageElement>
    <#--<#else>
    <a href="javascript:void(0);" style="float:left;">
        <span style="background:url(/public/rebuildRes/image/mobile/home/icon_school_01.png) center center no-repeat #EEEEEE; background-size:2rem 2rem;"></span>
        新学校
    </a>
    </#if>-->
    <#if !requestContext.getCurrentUser().isCountryManager() || !requestContext.getCurrentUser().isRegionManager()>
        <a onclick="openSecond('/view/mobile/crm/workrecord/into_school.vpage')" class="resourceTitle">
            <span class="resourceIco ico02"></span>
            进校
        </a>
    <#else>
        <a href="javascript:void(0);" class="resourceTitle"> <#--灰度-->
            <span class="resourceIco ico02 iconGray"></span>
            进校
        </a>
    </#if>
    <a onclick="openSecond('/view/mobile/crm/workrecord/add_meeting.vpage')" class="resourceTitle">
        <span class="resourceIco ico04"></span>
        省市区组会
    </a>
    <#if !requestContext.getCurrentUser().isCityAgent() && !requestContext.getCurrentUser().isCityAgentLimited() > <#--代理-->
        <a onclick="openSecond('/view/mobile/crm/workrecord/visit_research.vpage')" class="resourceTitle">
            <span class="resourceIco ico06"></span>
            教研员拜访
        </a>
    <#else>
        <a href="javascript:void(0);" class="resourceTitle">
            <span class="resourceIco ico06 iconGray"></span>
            教研员拜访
        </a>
    </#if>
    <a onclick="openSecond('/view/mobile/crm/workrecord/accompany_record.vpage')" class="resourceTitle">
        <span class="resourceIco ico05"></span>
        陪同
    </a>
    <a class="js-close resourceTitle resourceClose" href="javascript:void(0)">
        <span class="resourceIco ico07"></span>
    </a>
</div>
</#if>

<script>
    var AT = new agentTool();
    $(".js-more").on("click",function(e){
        $(this).addClass("the");
        e.stopPropagation();
        // $(".more").fadeIn(500);
        location.href = "/view/mobile/crm/work/index.vpage";
    });
    $('.js-operator').on('click',function(){
        AT.alert('暂无此权限')
    });
    $(document).on('click','.js-message',function(){
        window.location.href="/mobile/notice/index.vpage";
    });
    $(document).on('click','.js-noTeam',function(){
        AT.alert('暂无此权限')
    });
    $(".js-close").on("click",function(){
        $(".js-more").removeClass("the");
        $(".more").fadeOut(100);
    });
    /*--tab切换--*/

    $(".tab-head").children("a,span").on("click",function(){
        var $this=$(this);
        $this.addClass("the").siblings().removeClass("the");
        $this.parent().siblings(".tab-main").eq(0).children().eq($this.index()).show().siblings().hide();
    });

    $(".j-flex").each(function(){
        var $this = $(this);
        var sum = 0;
        $this.children().each(function(){
            sum+=$(this).outerWidth();
        });
        $this.children().each(function(){
            $(this).width($(this).outerWidth()/sum*100+"%");
        });
    });
    //    尝试固定底部导航栏
    $(document).on("focus","input,textarea",function(){
        $(".nav").hide();
    });
    $(document).on("blur","input,textarea",function(){
        setTimeout(function(){
            $(".nav").show();
        },500);
    });

    //平台、设备和操作系统
    var system ={
        win : false,
        mac : false,
        xll : false
    };

    //检测平台
    var p = navigator.platform;
    system.win = p.indexOf("Win") == 0;
    system.mac = p.indexOf("Mac") == 0;
    system.x11 = (p == "X11") || (p.indexOf("Linux") == 0);

    if(!system.win && !system.mac && !system.xll) {
        $('#foot_tab').hide();
    }

    //检测是否支持wk_webview
    var support_wk_webview = function support_wk_webview() {
        try {
            window.webkit.messageHandlers['_17m_external'].postMessage;
            return true;
        } catch (e) {
            return false;
        }
    };

    var do_external = function (method, param) {
        if(support_wk_webview()){
            window.webkit.messageHandlers['_17m_external'].postMessage({"method":method , "param":param})
        }else{
            if(param){
                window.external[method](param);
            }else{
                window.external[method]();
            }
        }
    };

    var openSecond = function (url){
        //跳转语句
        if(system.win||system.mac||system.xll){
            window.location.href = url;
        }else{
            var data = {
                url:window.location.protocol+ "//" + window.location.host + url,
                back_close:true,
                title:""
            };
            do_external('openSecondWebview',JSON.stringify(data));
        }
    };

    var setTopBarFn = function(setTopBar,resFn){
        if(!system.win && !system.mac && !system.xll) {
            var u = navigator.userAgent;
            var isAndroid = u.indexOf('Android') > -1 || u.indexOf('Adr') > -1; //android终端
            var isiOS = !!u.match(/\(i[^;]+;( U;)? CPU.+Mac OS X/);
            var vox = window.vox = window.vox || {};
            vox.task = vox.task || {};
            if (support_wk_webview()) {
                do_external('setTopBarInfo',JSON.stringify(setTopBar));
                vox.task.setTopBarInfoCallBack = function () {
                    resFn();
                };
            }else {
                if(isAndroid){
                    window.external.setTopBarInfo(JSON.stringify(setTopBar));
                    vox.task.setTopBarInfoCallBack = function () {
                        resFn();
                    };
                }else{
                    var getInit = setInterval(function(){
                        try{
                            window.external.getInitParams();
                            clearInterval(getInit);
                            window.external.setTopBarInfo(JSON.stringify(setTopBar));
                            vox.task.setTopBarInfoCallBack = function () {
                                resFn();
                            };
                        }catch(e){
                            console.error('方法未找到');
                        }
                    },1000);
                }
            }
        }
    };
    var reloadCallBack = function(){
        var vox = window.vox = window.vox || {};
        vox.task = vox.task || {};
        vox.task.refreshData = function(){
            window.location.reload();
        };
    };
    var disMissViewCallBack = function(){
        do_external('disMissView');
    };
    //固定头部
    $(".top-holder").height($(".fixed-head").outerHeight());
</script>
</body>
</html>
</#macro>
