<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#macro checkAuth systemName relativeUrl method='get'>
<#--fixme-->
<!--for example: systemName="crm" relativeUrl="student/studenthomepage[.vpage]" method="post"-->
    <#if method='get'>
        <#assign roleName='getAccessor'/>
    <#else>
        <#assign roleName='postAccessor'/>
    </#if>
    <#if requestContext.getCurrentAdminUser().checkAuthByRoleNames(systemName, relativeUrl, [roleName])>
        <#nested/>
    </#if>
</#macro>
<#macro page page_title = 'Admin' page_num=15 jqueryVersion="1.9.1" useDefaultHeaderSource=true>
<#--useDefaultHeaderSource 是否使用layout_default.ftl中的head标签中的资源。 default: true-->
<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <title>${page_title!'Admin'}</title>
    <link  href="${requestContext.webAppContextPath}/public/css/bootstrap.css" rel="stylesheet">
    <link  href="${requestContext.webAppContextPath}/public/css/admin.css" rel="stylesheet">
    <link href="https://17zuoye.com/favicon.ico" rel="shortcut icon">
    <#if useDefaultHeaderSource>
        <link  href="${requestContext.webAppContextPath}/public/css/jquery-ui-1.10.3.custom.min.css" rel="stylesheet">
        <link rel="stylesheet" href="${requestContext.webAppContextPath}/public/js/eleui/2.6.3/index.css">
        <script src="https://cdn-bsy.17zuoye.cn/s17/??lib/jquery/2.1.4/seed.min.js,lib/vue/2.4.4/seed.min.js"></script>
        <script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
        <script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
        <script src="${requestContext.webAppContextPath}/public/js/template.js"></script>

        <script src="${requestContext.webAppContextPath}/public/js/jquery.form.js"></script>
        <#--<script src="${requestContext.webAppContextPath}/public/legacy/region.js"></script>-->
        <script src="${requestContext.webAppContextPath}/public/legacy/common.js"></script>
        <script src="${requestContext.webAppContextPath}/public/js/ctrllog.js"></script>
        <script src="${requestContext.webAppContextPath}/public/js/aliyun/aliyun-oss-sdk.min.js"></script>
        <script src="${requestContext.webAppContextPath}/public/js/eleui/2.6.3/index.js"></script>
    </#if>
    <style type="text/css">
        body { padding-bottom: 40px; background-color: #f5f5f5; }
        a, input, button, select{ outline:none !important;}
        .form-signin { max-width: 300px; padding: 19px 29px 29px; margin: 0 auto 20px; background-color: #fff; border: 1px solid #e5e5e5; -webkit-border-radius: 5px; -moz-border-radius: 5px; border-radius: 5px; -webkit-box-shadow: 0 1px 2px rgba(0,0,0,.05); -moz-box-shadow: 0 1px 2px rgba(0,0,0,.05); box-shadow: 0 1px 2px rgba(0,0,0,.05); }
        .form-signin .form-signin-heading,  .form-signin .checkbox { margin-bottom: 10px; }
        .form-signin input[type="text"],  .form-signin input[type="password"] { font-size: 16px; height: auto; margin-bottom: 15px; padding: 7px 9px; vertical-align: inherit;}
        .font-zh{font-family: '微软雅黑', 'Microsoft YaHei', Arial; font-weight: normal;}
        .table th, .table td{word-break:break-all; word-wrap:break-word;}
        .date { width: 6em; }
        .ui-dialog-titlebar-close { display: none;}
        .navbar-fixed-top {position: static!important; margin-bottom: 20px!important;}
    </style>
</head>
<body>
    <#if page_num != 0 >
        <div class="navbar navbar-inverse navbar-fixed-top">
            <div class="navbar-inner">
                <div class="container-fluid">
                    <button type="button" class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse"> <span class="icon-bar"></span> <span class="icon-bar"></span> <span class="icon-bar"></span> </button>
                    <a href="${requestContext.webAppContextPath}/" target="_top" class="brand">17</a>
                    <div class="nav-collapse collapse">
                        <ul class="nav">
                            <#--<@checkAuth systemName='legacy' relativeUrl='index'>-->
                                <#--<li <#if page_num==1>class="active"</#if>><a href='${requestContext.webAppContextPath}/legacy/index.vpage' target="_blank">一起作业管理(旧版)</a></li>-->
                            <#--</@checkAuth>-->
                            <@checkAuth systemName='crm' relativeUrl='index'>
                                <#--<li <#if page_num==3>class="active"</#if>><a href='${requestContext.webAppContextPath}/crm/index.vpage'>CRM</a></li>-->

                                <li class="<#if page_num==3>active</#if> dropdown"><a href='javascript:void(0);' class="dropdown-toggle">CRM<b class="caret"></b></a>
                                    <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel" style="margin: 0;">
                                        <@checkAuth systemName='crm' relativeUrl='student/studentlist'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/student/studentlist.vpage">学生查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='teacher/teacherlist'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/teacher/teacherlist.vpage">老师查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='parent/parentlist'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/parent/parentlist.vpage">家长查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='faultOrder/faultorderlist'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/faultOrder/faultorderlist.vpage">用户追踪查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='researchstaff/researchstafflist'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/researchstaff/researchstafflist.vpage">教研员查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='school/schoollist'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/school/schoollist.vpage">学校查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='homework/homeworkhomepage'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/homework/newhomeworkhomepage.vpage">作业查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='legacy' relativeUrl='afenti/main'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/legacy/afenti/main.vpage">阿分题订单</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='legacy' relativeUrl='order/main'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/legacy/order/main.vpage">退款处理</a>
                                            </li>
                                        </@checkAuth>
                                        <#--<@checkAuth systemName='crm' relativeUrl='region/index'>-->
                                            <#--<li>-->
                                                <#--<a href="${requestContext.webAppContextPath}/crm/region/index.vpage">区域管理</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                        <@checkAuth systemName='crm' relativeUrl='feedback/feedbackindex'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/feedback/feedbackindex.vpage">用户反馈</a>
                                            </li>
                                        </@checkAuth>
                                        <#--<@checkAuth systemName='crm' relativeUrl='ambassador/ambassadorindex'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/ambassador/ambassadorindex.vpage">推荐认证</a>
                                            </li>
                                        </@checkAuth>-->
                                        <@checkAuth systemName='crm' relativeUrl='teachernew/index'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/teachernew/index.vpage">新版老师查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='task/task_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/task/task_list.vpage">任务查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='task/record_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/task/record_list.vpage">记录查询</a>
                                            </li>
                                        </@checkAuth>
                                        <#--<@checkAuth systemName="crm" relativeUrl='task/meeting_record_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/task/meeting_record_list.vpage">组会记录</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName="crm" relativeUrl='task/workload_summary'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/task/workload_summary.vpage">客服工作量</a>
                                            </li>
                                        </@checkAuth>-->
                                        <@checkAuth systemName="crm" relativeUrl='clazz/alteration/index'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/clazz/alteration/index.vpage">换班</a>
                                            </li>
                                        </@checkAuth>
                                        <#--<@checkAuth systemName='crm' relativeUrl='ambassador/ambassadorindex'>-->
                                            <#--<li>-->
                                                <#--<a href="${requestContext.webAppContextPath}/crm/activatemobile/unactivateuserlist.vpage">手机绑定</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                        <#-- 关闭老师批量功能  -->
                                        <#--<@checkAuth systemName='crm' relativeUrl='batchauthentication/batchauthenticationindex'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/batchauthentication/batchauthenticationindex.vpage">老师批量认证</a>
                                            </li>
                                        </@checkAuth>-->
                                        <#--<@checkAuth systemName="crm" relativeUrl='registerfeedback/index'>-->
                                            <#--<li>-->
                                                <#--<a href="${requestContext.webAppContextPath}/crm/registerfeedback/index.vpage">注册反馈</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                        <#--<@checkAuth systemName="crm" relativeUrl='realtimefeedback/index'>-->
                                            <#--<li>-->
                                                <#--<a href="${requestContext.webAppContextPath}/crm/realtimefeedback/index.vpage">实时反馈</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                        <@checkAuth systemName='crm' relativeUrl='clazzjournalphoto/index'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/clazz/photoManagment.vpage">班级动态图片管理</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='ambassador/report'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/ambassador/report.vpage">校园大使举报信息</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='wirelesscharging/wirelesslist'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/wirelesscharging/wirelesslist.vpage">充值查询</a>
                                            </li>
                                        </@checkAuth>
                                        <#--<@checkAuth systemName='crm' relativeUrl='coupon/list'>-->
                                            <#--<li>-->
                                                <#--<a href="${requestContext.webAppContextPath}/crm/coupon/list.vpage">体验券查询</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                        <@checkAuth systemName='crm' relativeUrl='user/findMobileMessagehomepage'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/user/findMobileMessagehomepage.vpage">查询短信</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='registerfeedback/ambassadorindex'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/registerfeedback/ambassadorindex.vpage">校园大使反馈</a>
                                            </li>
                                        </@checkAuth>
                                        <#--<@checkAuth systemName="crm" relativeUrl='user/xxtlist'>-->
                                            <#--<li>-->
                                                <#--<a href="${requestContext.webAppContextPath}/crm/user/xxtlist.vpage">校讯通消息</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                        <@checkAuth systemName='crm' relativeUrl='school_clue/clue_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/school_clue/clue_list.vpage">信息审核</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='ugc/schoolname_notonly'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/ugc/schoolname_notonly.vpage">UGC数据查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='school_review/review_manage'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/school_review/review_manage.vpage">学校抽审管理</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='school_review/review_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/school_review/review_list.vpage">学校抽审</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='summer_reporter/school_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/summer_reporter/school_list.vpage">暑期数据审核</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='main_sub_account/apply_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/main_sub_account/apply_list.vpage">包班制记录查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='clue/clue_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/clue/clue_list.vpage">线索查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='riskcontrol/abnormalclazz'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/riskcontrol/abnormalclazz.vpage">异常班级标记</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='productfeedback/feedback_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/productfeedback/feedback_list.vpage">全部反馈查询</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='cs_productfeedback/feedback_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/cs_productfeedback/productfeedbacklist.vpage">产品反馈(客服)</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='crm' relativeUrl='product_promotion/sms_promotion_list'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/crm/product_promotion/sms_promotion_list.vpage">产品推广管理</a>
                                            </li>
                                        </@checkAuth>
                                        </ul>
                                    </li>
                            </@checkAuth>

                            <@checkAuth systemName='site' relativeUrl='index'>
                                <li <#if page_num==4>class="active"</#if>><a href='${requestContext.webAppContextPath}/site/index.vpage'>网站管理</a></li>
                            </@checkAuth>
                            <@checkAuth systemName='advisory' relativeUrl='index'>
                                <li <#if page_num==5>class="active"</#if>><a href='${requestContext.webAppContextPath}/advisory/index.vpage'>资讯管理</a></li>
                            </@checkAuth>
                            <@checkAuth systemName='management' relativeUrl='management'>
                                <li <#if page_num==6>class="active"</#if>><a href='${requestContext.webAppContextPath}/management/index.vpage'>权限管理系统</a></li>
                            </@checkAuth>
<#--                            <@checkAuth systemName='bookmanager' relativeUrl='bookList'>
                                <li class="<#if page_num==7>active</#if> dropdown"><a href='javascript:void(0);'class="dropdown-toggle">教材管理<b class="caret"></b></a>
                                    <ul class="dropdown-menu" role="menu" aria-labelledby="dLabel" style="margin: 0;">
                                        <@checkAuth systemName='bookmanager' relativeUrl='bookList'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/bookmanager/bookList.vpage">英语教材管理</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='bookmanager' relativeUrl='mathbookList'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/bookmanager/mathbookList.vpage">数学教材管理</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='bookmanager' relativeUrl='chinesebookList'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/bookmanager/chinesebookList.vpage">语文教材管理</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='bookmanager' relativeUrl='physicsbookList'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/bookmanager/physicsbookList.vpage">物理教材管理</a>
                                            </li>
                                        </@checkAuth>
                                        <@checkAuth systemName='bookmanager' relativeUrl='chemistrybookList'>
                                            <li>
                                                <a href="${requestContext.webAppContextPath}/bookmanager/chemistrybookList.vpage">化学教材管理</a>
                                            </li>
                                        </@checkAuth>
                                    </ul>
                                </li>
                            </@checkAuth>-->
                            <@checkAuth systemName='knowledge' relativeUrl='knowledgeindex'>
                                <#--<li class="<#if page_num==8>active</#if> dropdown"><a href="javascript:void(0);" class="dropdown-toggle"> 知识点管理<b class="caret"></b></a>-->
                                    <#--<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel" style="margin: 0;">-->
                                        <#--<@checkAuth systemName='knowledge' relativeUrl='knowledgeindex'>-->
                                            <#--<li>-->
                                                <#--<a href='${requestContext.webAppContextPath}/knowledge/knowledgeindex.vpage'>应试知识点管理</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                        <#--<@checkAuth systemName='knowledge' relativeUrl='appknowledgeindex'>-->
                                            <#--<li>-->
                                                <#--<a href="${requestContext.webAppContextPath}/knowledge/appknowledgeindex.vpage">应用知识点管理</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                    <#--</ul>-->
                                <#--</li>-->
                                <#--<li <#if page_num==8>class="active"</#if>><a href='${requestContext.webAppContextPath}/knowledge/knowledgeindex.vpage'>知识点管理</a></li>-->
                            </@checkAuth>
                            <@checkAuth systemName='appmanager' relativeUrl='appindex'>
                                <li <#if page_num==10>class="active"</#if>><a href='${requestContext.webAppContextPath}/appmanager/appindex.vpage'>应用管理</a></li>
                            </@checkAuth>
                            <@checkAuth systemName='opmanager' relativeUrl='opindex'>
                                <li <#if page_num==9>class="active"</#if>><a href='${requestContext.webAppContextPath}/opmanager/opindex.vpage'>运营管理</a></li>
                            </@checkAuth>
                            <#--<@checkAuth systemName='customerservice' relativeUrl='customerserviceindex'>-->
                                <#--<li <#if page_num==9>class="active"</#if>><a href='${requestContext.webAppContextPath}/customerservice/customerserviceindex.vpage'>每日要闻</a></li>-->
                            <#--</@checkAuth>-->

                            <@checkAuth systemName='reward' relativeUrl='rewardindex'>
                                <li <#if page_num==12>class="active"</#if>><a href='${requestContext.webAppContextPath}/reward/rewardindex.vpage'>奖品中心</a></li>
                            </@checkAuth>
                            <#--<@checkAuth systemName='advertisement' relativeUrl='advertiserindex'>-->
                                <#--<li class="<#if page_num==13>active</#if> dropdown"><a href='javascript:void(0);'class="dropdown-toggle">广告后台<b class="caret"></b></a>-->
                                    <#--<ul class="dropdown-menu" role="menu" aria-labelledby="dLabel" style="margin: 0;">-->
                                        <#--<@checkAuth systemName='advertisement' relativeUrl='advertiserindex'>-->
                                            <#--<li>-->
                                                <#--<a href="${requestContext.webAppContextPath}/advertisement/advertiserindex.vpage">广告主管理</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                        <#--<@checkAuth systemName='advertisement' relativeUrl='advertisementindex'>-->
                                            <#--<li>-->
                                                <#--<a href="${requestContext.webAppContextPath}/advertisement/advertisementindex.vpage">广告管理</a>-->
                                            <#--</li>-->
                                        <#--</@checkAuth>-->
                                    <#--</ul>-->
                                <#--</li>-->
                            <#--</@checkAuth>-->
                            <#--<@checkAuth systemName='mizar' relativeUrl='mizarindex'>-->
                                <#--<li <#if page_num==17>class="active"</#if>><a href='${requestContext.webAppContextPath}/mizar/mizarindex.vpage'>机构导流管理</a></li>-->
                            <#--</@checkAuth>-->
                            <@checkAuth systemName='junior' relativeUrl='juniorindex'>
                                <li <#if page_num==17>class="active"</#if>><a href='${requestContext.webAppContextPath}/junior/juniorindex.vpage'>中学</a></li>
                            </@checkAuth>
                            <@checkAuth systemName='audit' relativeUrl='auditindex'>
                                <li <#if page_num==21>class="active"</#if>><a href='${requestContext.webAppContextPath}/audit/auditindex.vpage'>审核平台</a></li>
                            </@checkAuth>
                            <@checkAuth systemName='equator' relativeUrl='index'>
                                <li <#if page_num==24>class="active"</#if>><a href='${requestContext.webAppContextPath}/equator/index.vpage'>增值</a></li>
                            </@checkAuth>

                            <@checkAuth systemName='crm' relativeUrl='index'>
                                <li <#if page_num==25>class="active"</#if>><a href='${requestContext.webAppContextPath}/crm/experiment/index.vpage'>课程实验平台</a></li>
                            </@checkAuth>
                            <@checkAuth systemName='chips' relativeUrl='index'>
                                <li <#if page_num==26>class="active"</#if>><a href='${requestContext.webAppContextPath}/chips/index.vpage'>薯条英语</a></li>
                            </@checkAuth>
                        </ul>
                        <ul class="nav pull-right">
                            <li class="divider-vertical"></li>
                            <li class="dropdown"> <a class="dropdown-toggle" href="#" style="padding-left: 0px;padding-right: 0px;" id="admin_name_layout_page">${(requestContext.getCurrentAdminUser().adminUserName)!} <b class="caret"></b></a>
                                <ul class="dropdown-menu" style="margin: 0">
                                <#if requestContext.getCurrentAdminUser().isCsosUser()>
                                <li><a href="${requestContext.webAppContextPath}/auth/info.vpage">修改密码</a></li>
                                </#if>
                                <li><a href="${requestContext.webAppContextPath}/auth/logout.vpage">注销</a></li>
                            </ul>
                            </li>
                        </ul>
                    </div>
                    <!--/.nav-collapse -->
                </div>
            </div>
        </div>
    </#if>
    <#--<#if page_num==3>-->
        <#--<#include 'crm/headsearch.ftl' />-->
    <#--</#if>-->
    <div class="container-fluid">
        <#include 'widget_alert_messages.ftl' />
        <div class="row-fluid">
            <#switch page_num>
                <#case 3>
                    <#--<#include 'crm/leftmenu.ftl' />-->
                <#break/>
                <#case 4>
                    <#include 'site/leftmenu.ftl' />
                <#break/>
                <#--<#case 7>-->
                    <#--<#include 'bookmanager/leftmenu.ftl' />-->
                <#--<#break/>-->
                <#case 8>
                    <#include 'knowledge/leftmenu.ftl' />
                <#break/>
                <#case 9>
                    <#include 'opmanager/leftmenu.ftl' />
                    <#break/>
                <#case 16>
                    <#include 'opmanager/officialaccounts/leftmenu.ftl' />
                    <#break/>
                <#case 10>
                    <#include 'apps/leftmenu.ftl' />
                <#break/>
                <#case 11>
                    <#include 'payment/leftmenu.ftl' />
                <#break/>
                <#case 12>
                    <#include 'reward/leftmenu.ftl' />
                <#break/>
                <#case 14>
                    <#include 'site/wechatmessage/tm/leftmenu.ftl'/>
                    <#break/>
                <#case 13>
                    <#include 'advisory/leftmenu.ftl' />
                    <#break/>
                <#case 17>
                    <#include 'junior/leftmenu.ftl' />
                    <#break/>
                <#case 18>
                    <#include 'opmanager/growthmission/parent/leftmenu.ftl' />
                    <#break/>
                <#case 19>
                    <#include 'opmanager/growthmission/parent/missionleftmenu.ftl' />
                    <#break/>
                <#case 20>
                    <#include 'abtest/leftmenu.ftl' />
                    <#break/>
                <#case 21>
                    <#include 'audit/leftmenu.ftl' />
                    <#break/>
                <#case 23>
                    <#include 'opmanager/talk/leftmenu.ftl'/>
                    <#break />
                <#case 24>
                    <#include 'equator/leftmenu.ftl'/>
                    <#break />
                <#case 25>
                    <#include 'experiment/leftmenu.ftl'/>
                    <#break />
                <#case 26>
                    <#include 'chips/leftmenu.ftl'/>
                    <#break />
            </#switch>
            <#nested />
        </div>
    </div>
    <script type="text/javascript">
        var _currentUserName_ = "${(requestContext.getCurrentAdminUser().adminUserName)!}";
        $(function(){
            $(".dropdown").hover(function(){
                $(this).addClass("open");
            },function(){
                $(this).removeClass("open");
            });
        });
    </script>
    <#include "common/query_sensitive.ftl" />
</body>
</html>
</#macro>
