<#assign apptag=JspTaglibs["/WEB-INF/tld/app-tag.tld"]/>
<#macro checkAuth appName subSysPath>
    <#if requestContext.getCurrentUser().checkSysAuth(appName, subSysPath)>
        <#nested/>
    </#if>
</#macro>
<#macro page page_title = '首页' page_num=1>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- The styles -->
    <link id="bs-css" href="${requestContext.webAppContextPath}/public/css/bootstrap-cerulean.css" rel="stylesheet">
    <style type="text/css">
        body {
            padding-bottom: 40px;
            text-shadow: none;
        }

        .sidebar-nav {
            padding: 9px 0;
        }
    </style>
    <link href="${requestContext.webAppContextPath}/public/css/bootstrap-responsive.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/charisma-app.css" rel="stylesheet">
    <link href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.8.21.custom.css" rel="stylesheet">
    <link href='${requestContext.webAppContextPath}/public/css/fullcalendar.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/fullcalendar.print.css' rel='stylesheet' media='print'>
    <link href='${requestContext.webAppContextPath}/public/css/chosen.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/uniform.default.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/colorbox.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/jquery.cleditor.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/jquery.noty.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/noty_theme_default.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/elfinder.min.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/elfinder.theme.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/jquery.iphone.toggle.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/opa-icons.css' rel='stylesheet'>
    <link href='${requestContext.webAppContextPath}/public/css/uploadify.css' rel='stylesheet'>
    <!-- jQuery -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery-1.7.2.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/provincesChange.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery-utils/jquery-utils.js"></script>
    <!-- jQuery UI -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.8.21.custom.min.js"></script>
    <!-- transition / effect library -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-transition.js"></script>
    <!-- alert enhancer library -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-alert.js"></script>
    <!-- modal / dialog library -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-modal.js"></script>
    <!-- custom dropdown library -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-dropdown.js"></script>
    <!-- scrolspy library -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-scrollspy.js"></script>
    <!-- library for creating tabs -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-tab.js"></script>
    <!-- library for advanced tooltip -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-tooltip.js"></script>
    <!-- popover effect library -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-popover.js"></script>
    <!-- button enhancer library -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-button.js"></script>
    <!-- accordion library (optional, not used in demo) -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-collapse.js"></script>
    <!-- carousel slideshow library (optional, not used in demo) -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-carousel.js"></script>
    <!-- autocomplete library -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-typeahead.js"></script>
    <!-- tour library -->
    <script src="${requestContext.webAppContextPath}/public/js/bootstrap-tour.js"></script>
    <!-- library for cookie management -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.cookie.js"></script>
    <!-- calander plugin -->
    <script src='${requestContext.webAppContextPath}/public/js/fullcalendar.min.js'></script>
    <!-- data table plugin -->
    <script src='${requestContext.webAppContextPath}/public/js/jquery.dataTables.min.js'></script>
    <script src='${requestContext.webAppContextPath}/public/js/jqpaginator.min.js'></script>

    <!-- chart libraries start -->
    <script src="${requestContext.webAppContextPath}/public/js/excanvas.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery.flot.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery.flot.pie.min.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery.flot.stack.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/jquery.flot.resize.min.js"></script>
    <!-- chart libraries end -->

    <!-- select or dropdown enhancer -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.chosen.min.js"></script>
    <!-- checkbox, radio, and file input styler -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.uniform.min.js"></script>
    <!-- plugin for gallery image view -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.colorbox.min.js"></script>
    <!-- rich text editor library -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.cleditor.min.js"></script>
    <!-- notification plugin -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.noty.js"></script>
    <!-- file manager library -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.elfinder.min.js"></script>
    <!-- star rating plugin -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.raty.min.js"></script>
    <!-- for iOS style toggle switch -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.iphone.toggle.js"></script>
    <!-- autogrowing textarea plugin -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.autogrow-textarea.js"></script>
    <!-- multiple file upload plugin -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.uploadify-3.1.min.js"></script>
    <!-- history.js for cross-browser state change on ajax -->
    <script src="${requestContext.webAppContextPath}/public/js/jquery.history.js"></script>
    <#--html template-->
    <script src="${requestContext.webAppContextPath}/public/js/handerbars/handlebars-v4.0.5.js"></script>
    <!-- application script for Charisma demo -->
    <script src="${requestContext.webAppContextPath}/public/js/charisma.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/market-common.js"></script>
    <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>
    <!-- layer遮罩  -->
    <script src="${requestContext.webAppContextPath}/public/js/layer/layer.js"></script>
</head>

<body>
<!-- topbar starts -->
<div class="navbar">
    <div class="navbar-inner">
        <div class="container-fluid">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".top-nav.nav-collapse,.sidebar-nav.nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <a class="brand" href="/" style="width: 240px"> <span>Marketing System</span></a>

            <!-- user dropdown starts -->
            <div class="btn-group pull-right">
                <a class="btn dropdown-toggle" data-toggle="dropdown" href="#">
                    <i class="icon-user"></i><span class="hidden-phone"> ${(requestContext.getCurrentUser().userName)!}</span>
                    <span class="caret"></span>
                </a>
                <ul class="dropdown-menu">
                    <li><a href="${requestContext.webAppContextPath}/resetPassword.vpage">重设密码</a></li>
                    <li><a href="${requestContext.webAppContextPath}/auth/logout.vpage">注销</a></li>
                </ul>
            </div>
            <!-- user dropdown ends -->
            <div class="top-nav nav-collapse">
                <ul class="nav">
                    <li><a href="http://www.17zuoye.com" target="_blank">访问一起教育科技官网</a></li>
                </ul>
                <ul class="nav">
                    <li><a href="http://www.firefox.com.cn/download/" target="_blank">本站点推荐使用火狐浏览器，点击下载</a></li>
                </ul>
            </div>
            <!--/.nav-collapse -->
        </div>
    </div>
</div>
<!-- topbar ends -->
<div class="container-fluid">
    <div class="row-fluid">

        <!-- left menu starts -->
        <div class="span2 main-menu-span">
            <div class="well nav-collapse sidebar-nav">
                <ul class="nav nav-tabs nav-stacked main-menu " id="leftMainMenu">
                    <@checkAuth appName='workspace' subSysPath='*'>
                        <li class="nav-header hidden-tablet">
                        <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 我的工作台</span></h4>
                        <ul style="display:<#if page_num == 1>block<#else>none</#if>;">
                            <@apptag.pageElement elementCode="ff54e810cfd9474c">
                                <li><a class="ajax-link" href="/workspace/kpiinfo/workrecord.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 工作记录下载</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="e7b66f12a99c427d">
                                <li><a class="ajax-link" href="/workspace/marketfee/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 市场支持费用审核</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="bb5be1c44bca416c">
                                <li><a class="ajax-link" href="/workspace/marketfee/history/query.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">市场支持费用历史</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="93b8e995067d45e1">
                                <li><a class="ajax-link" href="/workspace/notifysend/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 下发通知</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="a426182c640a4f3e">
                                <li><a class="ajax-link" href="/workspace/admin/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 管理员任务</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="222599cab2e44cf4">
                                <li><a class="ajax-link" href="/workspace/appupdate/data_packet_manage.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 资料包管理</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="5c081eda00fa4b79">
                                <li><a class="ajax-link" href="/workspace/appupdate/marketing_activity_manage.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 最新活动管理</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="a99bb52446e84ecc">
                                <li><a class="ajax-link" href="/workspace/appupdate/update_log_manage.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 平台更新日志管理</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="7fcb0ba2914846ff">
                                <li><a class="ajax-link" href="/workspace/appupdate/recommend_book_manage.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 推荐书籍管理</span></a></li>
                            </@apptag.pageElement>
                            <@checkAuth appName='workspace' subSysPath='kpiinfo'>
                                <#if requestContext.getCurrentUser().isCountryManager()>
                                    <li><a class="ajax-link" href="/workspace/kpiinfo/schoolClue.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 学校基础信息日报下载</span></a></li>
                                </#if>
                            </@checkAuth>
                            <@apptag.pageElement elementCode="5a95d34e434d4d4b">
                                <li><a class="ajax-link" href="/workspace/reportdownload/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">数据下载</a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="089f4a7057194dc0">
                                <li><a class="ajax-link" href="/workspace/everydayscan/detail_page.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">快乐学每日扫描量下载</a></li>
                            </@apptag.pageElement>

                            <@apptag.pageElement elementCode="79ee7cbeec0649a4">
                                <li><a class="ajax-link" href="/workspace/performance/my_data.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">业绩统计</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="0641b7c598774c18">
                                <li><a class="ajax-link" href="/workspace/import/import_klxstudents_view.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">批量导入快乐学学生账号</span></a></li>
                            </@apptag.pageElement>
                            <@apptag.pageElement elementCode="c7985004b7fd49c7">
                                <li><a class="ajax-link" href="/publish/data/publish_list_page.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 信息分发</span></a></li>
                            </@apptag.pageElement>
                        </ul>
                    </li>
                    </@checkAuth>

                    <@apptag.pageElement elementCode="9652cc3869aa4c56">

                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 活动管理</span></h4>
                            <ul style="display:<#if page_num == 19>block<#else>none</#if>;">
                                <@checkAuth appName='taskmanage' subSysPath='maintainteacher'>
                                    <li><a class="ajax-link" href="/activity_card/import/import_redeem.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">批量导入活动兑换码</span></a></li>
                                </@checkAuth>
                                <li><a class="ajax-link" href="/activity/manage/activity_list.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">活动列表</span></a></li>
                            </ul>
                        </li>
                    </@apptag.pageElement>

                    <@apptag.pageElement elementCode="bb106afe853547f4">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 任务中心</span></h4>
                            <ul style="display:<#if page_num == 16>block<#else>none</#if>;">
                                <@checkAuth appName='taskmanage' subSysPath='maintainteacher'>
                                    <li><a class="ajax-link" href="/taskmanage/maintainteacher/task_list_page.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">维护老师</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                    </@apptag.pageElement>
                    <@checkAuth appName='task' subSysPath='*'>
                        <@apptag.pageElement elementCode="ed1a1add4ba34ac0">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 我的任务</span></h4>
                            <ul style="display:<#if page_num == 2>block<#else>none</#if>;">
                                <@checkAuth appName='task' subSysPath='todolist'>
                                    <li><a class="ajax-link" href="/task/todolist/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 待处理任务</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='task' subSysPath='donelist'>
                                    <li><a class="ajax-link" href="/task/donelist/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 已处理任务</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='task' subSysPath='failorderlist'>
                                    <li><a class="ajax-link" href="/task/failorderlist/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 退款状态查询</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='task' subSysPath='manage'>
                                    <li><a class="ajax-link" href="/task/manage/creater_tasks.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 任务管理</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='task' subSysPath='loglistXXX'>
                                    <li><a class="ajax-link" href="grid.html"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 操作日志</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                        </@apptag.pageElement>
                    </@checkAuth>
                    <@checkAuth appName='apply' subSysPath='*'>
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 申请管理</span></h4>
                            <ul style="display:<#if page_num == 3>block<#else>none</#if>;">
                                <@checkAuth appName='apply' subSysPath='create'>
                                    <li><a class="ajax-link" href="/apply/create/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 创建申请</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='apply' subSysPath='view'>
                                    <li><a class="ajax-link" href="/apply/view/list.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 我的申请</span></a></li>
                                </@checkAuth>
                                <@apptag.pageElement elementCode="260015f592554587">
                                    <li><a class="ajax-link" href="/apply/manage/all_list.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 全部申请</span></a></li>
                                </@apptag.pageElement>
                            </ul>
                        </li>
                    </@checkAuth>
                    <@checkAuth appName='workflow' subSysPath='*'>
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 审核管理</span></h4>
                            <ul style="display:<#if page_num == 11>block<#else>none</#if>;">
                                <@checkAuth appName='workflow' subSysPath='todo'>
                                    <li><a class="ajax-link" href="/workflow/todo/list.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 待处理<#if dataList?has_content && dataList?size gt 0><span id="show_hide">(${dataList?size!0})</span></#if></span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='workflow' subSysPath='done'>
                                    <li><a class="ajax-link" href="/workflow/done/list.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 已处理</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                    </@checkAuth>
                    <@checkAuth appName='crm' subSysPath='*'>
                        <@apptag.pageElement elementCode="57b0106bae6c401e">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 用户信息</span></h4>
                            <ul style="display:<#if page_num == 3>block<#else>none</#if>;">
                                <@checkAuth appName='crm' subSysPath='area'>
                                </@checkAuth>
                            </ul>
                        </li>
                        </@apptag.pageElement>
                    </@checkAuth>
                    <@checkAuth appName='account' subSysPath='*'>
                        <@apptag.pageElement elementCode="52b38ca1fc2e42d6">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 我的账户</span></h4>
                            <ul style="display:<#if page_num == 4>block<#else>none</#if>;">
                                <@checkAuth appName='account' subSysPath='myconfigXXXX'>
                                    <li><a class="ajax-link" href="/account/myconfig/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 我的信息</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='account' subSysPath='myorder'>
                                    <li><a class="ajax-link" href="/account/myorder/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 我的订单</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='account' subSysPath='myaccount'>
                                    <li><a class="ajax-link" href="/account/myaccount/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 我的账户</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='account' subSysPath='memberincome'>
                                    <li><a class="ajax-link" href="/account/memberincome/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 下属收入</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                        </@apptag.pageElement>
                    </@checkAuth>
                    <@checkAuth appName='user' subSysPath='*'>
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 用户管理</span></h4>
                            <ul style="display:<#if page_num == 5>block<#else>none</#if>;">
                                <@apptag.pageElement elementCode="1cb22664b6594db6">
                                <li><a class="ajax-link" href="/user/orgconfig/department.vpage" target="_blank"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 部门管理</span></a></li>
                                </@apptag.pageElement>

                                <@apptag.pageElement elementCode="e3848f7164b742ee">
                                    <li><a class="ajax-link" href="/user/viewuserconfig/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 协作账户设置</span></a></li>
                                </@apptag.pageElement>
                            </ul>
                        </li>
                    </@checkAuth>
                    <@checkAuth appName='sysconfig' subSysPath='*'>
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 系统管理</span></h4>
                            <ul style="display:<#if page_num == 6>block<#else>none</#if>;">
                                <@apptag.pageElement elementCode="6e121a10bad748da">
                                    <li><a class="ajax-link" href="/sysconfig/citylevel/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 城市等级维护</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="c370e93976e64eb5">
                                    <li><a class="ajax-link" href="/system/permission/role_list.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 角色权限管理</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="09bcea09a95f4f92">
                                    <li><a class="ajax-link" href="/sysconfig/region/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 区域设置</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="3d922fc6dc264a5a">
                                    <li><a class="ajax-link" href="/sysconfig/performance_group/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 专员绩效分组</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="fc7c561985b742bc">
                                    <li><a class="ajax-link" href="/sysconfig/syspath/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 功能权限</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="053ebadee5884dd0">
                                    <li><a class="ajax-link" href="/sysconfig/password/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 修改密码及设备ID</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="bf628b413e194e45">
                                    <li><a class="ajax-link" href="/sysconfig/schooldic/schoolDictDetail.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 学校字典表维护</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="f1c2bca92bf04766">
                                    <li><a class="ajax-link" href="/sysconfig/payments/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 业绩结算数据维护</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="14ee179cd39846fe">
                                    <li><a class="ajax-link" href="/sysconfig/dateconfig/configpage.vpage" ><i class="icon-chevron-right"></i><span class="hidden-tablet"> 日期控制</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="df2b00d62fc743b9">
                                    <li><a class="ajax-link" href="/sysconfig/cache/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 缓存清除</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="8a815477061b4990">
                                    <li><a class="ajax-link" href="/sysconfig/headcount/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 部门HC维护</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="53f5f693501a4404">
                                    <li><a class="ajax-link" href="/sysconfig/config/list.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">总部接口人维护</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="6f6e0cffb9c64540">
                                    <li><a class="ajax-link" href="/sysconfig/tag/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">标签管理</span></a></li>
                                </@apptag.pageElement>
                            </ul>
                        </li>
                    </@checkAuth>

                    <@checkAuth appName='exam' subSysPath='*'>
                        <@apptag.pageElement elementCode="81a66b4d5c914c01">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 大考管理</span></h4>
                            <ul style="display:<#if page_num == 15>block<#else>none</#if>;">
                                <@checkAuth appName='exam' subSysPath='contractmanage'>
                                    <li><a class="ajax-link" href="/exam/contractmanage/manage.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 合同管理</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='exam' subSysPath='exammanage'>
                                    <li><a class="ajax-link" href="/exam/exammanage/statistics.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 大考统计</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='exam' subSysPath='exammanage'>
                                    <li><a class="ajax-link" href="/exam/exammanage/manage.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 提报查询</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                        </@apptag.pageElement>
                    </@checkAuth>
                    <@apptag.pageElement elementCode="1f3a945938fa4ae0">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 消息中心</span></h4>
                            <ul style="display:<#if page_num == 18>block<#else>none</#if>;">
                                <@apptag.pageElement elementCode="890fd4b4d7484741">
                                    <li><a class="ajax-link" href="/message/manage/createPage.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 新建消息</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="7414fb205d5b488f">
                                    <li><a class="ajax-link" href="/message/manage/message_list.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 消息列表</span></a></li>
                                </@apptag.pageElement>
                                <#--<@checkAuth appName='exam' subSysPath='exammanage'>-->
                                    <#--<li><a class="ajax-link" href="/exam/exammanage/statistics.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 大考统计</span></a></li>-->
                                <#--</@ch4eckAuth>-->
                                <#--<@checkAuth appName='exam' subSysPath='exammanage'>-->
                                    <#--<li><a class="ajax-link" href="/exam/exammanage/manage.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 提报查询</span></a></li>-->
                                <#--</@checkAuth>-->

                            </ul>
                        </li>
                    </@apptag.pageElement>
                    <@checkAuth appName='vendor' subSysPath='*'>
                        <@apptag.pageElement elementCode="d4ff65b929c641f6">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 应用信息</span></h4>
                            <ul style="display:<#if page_num == 7>block<#else>none</#if>;">
                                <@checkAuth appName='vendor' subSysPath='statistics'>
                                    <li><a class="ajax-link" href="/vendor/statistics/monthstatistics.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 应用月度统计</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='vendor' subSysPath='statistics'>
                                    <li><a class="ajax-link" href="/vendor/statistics/periodstatistics.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 应用周期统计</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='vendor' subSysPath='statistics'>
                                    <li><a class="ajax-link" href="/vendor/statistics/daystatistics.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 应用按天统计</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='vendor' subSysPath='statistics'>
                                    <li><a class="ajax-link" href="/vendor/statistics/sticky.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 应用留存粘性</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                        </@apptag.pageElement>
                    </@checkAuth>
                    <@checkAuth appName='oral' subSysPath='*'>
                        <@apptag.pageElement elementCode="cb244910b5f24b9d">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 统考管理</span></h4>
                            <ul style="display:<#if page_num == 8>block<#else>none</#if>;">
                                <@checkAuth appName='oral' subSysPath='manage'>
                                    <li><a class="ajax-link" href="/oral/manage/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 相关资料下载</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='oral' subSysPath='manage'>
                                    <li><a class="ajax-link" href="/oral/manage/list.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 统考测评管理</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                        </@apptag.pageElement>
                    </@checkAuth>

                    <@checkAuth appName='workspace' subSysPath='markettool'>
                        <@apptag.pageElement elementCode="b6a51dea2c4641d3">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 市场工具</span></h4>
                            <ul style="display:<#if page_num == 9>block<#else>none</#if>;">
                                <@checkAuth appName='workspace' subSysPath='markettool'>
                                    <li><a class="ajax-link" href="/workspace/markettool/bulkaccountindex.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 批量注册账号</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='workspace' subSysPath='markettool'>
                                    <li><a class="ajax-link" href="/workspace/markettool/addstudentindex.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 添加学生账号</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='workspace' subSysPath='markettool'>
                                    <li><a class="ajax-link" href="/workspace/markettool/clazzreform.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 班级重组</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='workspace' subSysPath='markettool'>
                                    <li><a class="ajax-link" href="/workspace/markettool/namelistindex.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 下载学生名单</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='workspace' subSysPath='markettool'>
                                    <li><a class="ajax-link" href="/workspace/markettool/school_search.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 学校ID查询</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                        </@apptag.pageElement>
                    </@checkAuth>
                    <@checkAuth appName='operationlog' subSysPath='*'>
                        <@apptag.pageElement elementCode="271d95cecdc546c2">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 操作记录</span></h4>
                            <ul style="display:<#if page_num == 7>block<#else>none</#if>;">
                                <@checkAuth appName='operationlog' subSysPath='schoolconfig'>
                                    <li><a class="ajax-link" href="/operationlog/schoolconfig/logpage.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 学校负责人调整</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                        </@apptag.pageElement>
                    </@checkAuth>
                    <@checkAuth appName='monitor' subSysPath='*'>
                        <@apptag.pageElement elementCode="a2fccdb3ee4642b8">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 监控工具</span></h4>
                            <ul style="display:<#if page_num == 10>block<#else>none</#if>;">
                                <@checkAuth appName='workspace' subSysPath='faketeacher'>
                                    <li><a class="ajax-link" href="/workspace/faketeacher/cancel_faketeacher.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">取消判假老师明细</span></a></li>
                                </@checkAuth>

                                <@checkAuth appName='monitor' subSysPath='class_alter'>
                                    <li><a class="ajax-link" href="/monitor/class_alter/view.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet">换班任务统计</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                        </@apptag.pageElement>
                    </@checkAuth>

                    <@checkAuth appName='materialbudget' subSysPath='*'>
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 物料管理</span></h4>
                            <ul style="display:<#if page_num == 14>block<#else>none</#if>;">
                                <@apptag.pageElement elementCode="4d0690bcef184d41">
                                    <li><a class="ajax-link" href="/sysconfig/product/index.vpage"><i class="icon-chevron-right "></i><span class="hidden-tablet"> 商品管理</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="3c00ba515b4940bb">
                                    <li><a class="ajax-link" href="/materialbudget/budget/budget.vpage"><i class="icon-chevron-right "></i><span class="hidden-tablet"> 费用管理</span></a></li>
                                </@apptag.pageElement>

                                <@apptag.pageElement elementCode="efecc1c1cb164fe3">
                                    <li><a class="ajax-link" href="/materialbudget/order/order.vpage"><i class="icon-chevron-right "></i><span class="hidden-tablet"> 订单管理</span></a></li>
                                </@apptag.pageElement>

                                <@apptag.pageElement elementCode="16540d2624984ab7">
                                    <li><a class="ajax-link" href="/workspace/invoice/list.vpage"><i class="icon-chevron-right "></i><span class="hidden-tablet"> 发货管理</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="09a8b9a8fbc9491f">
                                    <li><a class="ajax-link" href="/workspace/purchase/index.vpage"><i class="icon-chevron-right "></i><span class="hidden-tablet"> 商品购买</span></a></li>
                                </@apptag.pageElement>
                            </ul>
                        </li>
                    </@checkAuth>
                    <@apptag.pageElement elementCode="0b2bde6ed5f14bd5">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 测评</span></h4>
                            <ul style="display:<#if page_num == 3>block<#else>none</#if>;">
                                <@apptag.pageElement elementCode="eea319942b0a4024">
                                <li><a class="ajax-link" href="/mockexam/plan/forquery.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 小学测评管理</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="67b9c58ba23445b4">
                                <li><a class="ajax-link" href="/mockexam/paper/forquery.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 小学试卷管理</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="909267d5e550469b">
                                    <li><a class="ajax-link" href="/middleschool/mockexam/paper/forquery.vpage"><i class="icon-eye-open"></i><span class="hidden-tablet"> 中学试卷管理</span></a></li>
                                </@apptag.pageElement>
                            </ul>
                        </li>
                    </@apptag.pageElement>
                    <@apptag.pageElement elementCode="bb240d371f3e4cc0">
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 培训中心</span></h4>
                            <ul style="display:<#if page_num == 17>block<#else>none</#if>;">
                                <@apptag.pageElement elementCode="ef22296a8c8b45f2">
                                    <li><a class="ajax-link" href="/trainingcenter/column/columnList.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 栏目管理</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="ae2cc1458bce4582">
                                    <li><a class="ajax-link" href="/trainingcenter/article/index.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 文章管理</span></a></li>
                                </@apptag.pageElement>
                                <@apptag.pageElement elementCode="bbca5fafc07b4ae7">
                                    <li><a class="ajax-link" href="/trainingcenter/material/materialList.vpage"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 素材管理</span></a></li>
                                </@apptag.pageElement>
                            </ul>
                        </li>
                    </@apptag.pageElement>
                   <#-- <@checkAuth appName='datareport' subSysPath='*'>
                        <li class="nav-header hidden-tablet">
                            <h4><i class="icon-tasks"></i><span class="hidden-tablet"> 数据报表</span></h4>
                            <ul style="display:<#if page_num == 10>block<#else>none</#if>;">
                                <@checkAuth appName='datareport' subSysPath='report'>
                                    <li><a class="ajax-link" href="/datareport/report/index.vpage?type=city&init=1"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 城市表</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='datareport' subSysPath='report'>
                                    <li><a class="ajax-link" href="/datareport/report/index.vpage?type=school&init=1"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 学校表</span></a></li>
                                </@checkAuth>
                                <@checkAuth appName='datareport' subSysPath='report'>
                                    <li><a class="ajax-link" href="/datareport/report/index.vpage?type=teacher&init=1"><i class="icon-chevron-right"></i><span class="hidden-tablet"> 老师表</span></a></li>
                                </@checkAuth>
                            </ul>
                        </li>
                    </@checkAuth>-->
                </ul>
            </div>
        </div>
        <noscript>
            <div class="alert alert-block span10">
                <h4 class="alert-heading">警告!</h4>

                <p>您必须拥有 <a href="http://en.wikipedia.org/wiki/JavaScript" target="_blank">JavaScript</a> 才能使用本系统.</p>
            </div>
        </noscript>

        <div id="content" class="span10">
            <#nested />
        </div>
    </div>
    <!--/.fluid-container-->

    <!-- external javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script type="text/javascript">
        $(function () {
            var leftMainMenu = $("#leftMainMenu");
            leftMainMenu.find(".nav-header").live("click", function () {
                var $this = $(this);
                if ($this.find("ul").length > 0) {
                    $this.find("ul").show();
                    $this.siblings().removeClass('active').find("ul").hide();
                } else {
                    $this.addClass("active");
                    $this.siblings().find("ul").hide();
                }
            });
        });
    </script>
</body>
</html>

</#macro>
