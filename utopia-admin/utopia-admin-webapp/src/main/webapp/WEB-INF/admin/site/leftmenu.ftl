<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<div class="span2">
    <div class="well sidebar-nav" style="background-color: #fff;">
        <ul class="nav nav-list">
            <li class="nav-header"><a href="#webManage" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>网站管理<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="webManage" class="nav nav-list collapse secondmenu" style="height: auto;">
                <li><a href="${requestContext.webAppContextPath}/site/pageblockcontent/list.vpage">页面内容</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/userpopup/userpopuplist.vpage">弹窗广告</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/message/messagelist.vpage">系统消息</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/dict/dicthomepage.vpage">字典管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/regionproduct/regionproducthomepage.vpage">产品开放区域管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/global/addglobaltags.vpage">产品开放特殊管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/batch/index.vpage">批量功能汇总</a></li>
            <#--<li><a href="${requestContext.webAppContextPath}/site/quiz/list.vpage">统一测试</a></li>-->
                <li><a href="${requestContext.webAppContextPath}/toolkit/toolkit.vpage">阿娟工具箱</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/message/clazzmessageindex.vpage">班级系统通知</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/advertisement/advertiserindex.vpage">广告中心</a></li>
                <#--<li><a href="${requestContext.webAppContextPath}/site/homework/homeworkreport.vpage">重新检查作业</a></li>-->
                <li><a href="${requestContext.webAppContextPath}/site/lottery/new/ratelist.vpage">抽奖概率调整</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/activity/rechargeteacher.vpage">充值话费导入老师</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/wechatmessage/messagehome.vpage">微信推送消息验证</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/task/taskhome.vpage">任务二次确认</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/recharge/rechargehome.vpage">批量充值话费</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/wechatfaq/list.vpage">微信FAQ</a></li>
                <#--<li><a href="${requestContext.webAppContextPath}/site/ambassador/auditambassador.vpage">校园大使审核</a></li>-->
                <li><a href="${requestContext.webAppContextPath}/site/clientappverion/list.vpage">客户端版本管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/commonconfig/configlist.vpage">通用配置管理</a></li>
                <#--<li><a href="${requestContext.webAppContextPath}/site/wechat/sendredpack.vpage">大使微信发红包</a></li>-->
                <li><a href="${requestContext.webAppContextPath}/site/cheat/index.vpage">作弊作业</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/wechat/menu.vpage?type=parent">家长微信菜单管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/wechat/menu.vpage?type=teacher">老师微信菜单管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/wechat/menu.vpage?type=chips">薯条英语微信菜单管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/wechat/menu.vpage?type=studytogether">17学微信菜单管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/wechat/index.vpage">17学微信菜单链接生成</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/vendormessage/appparentmessage.vpage">APP消息功能大集合</a></li>
                <li><a href="${requestContext.webAppContextPath}/abtest/index.vpage">abtest配置</a></li>
                <li><a href="${requestContext.webAppContextPath}/advisory/index.vpage">家校通资讯管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/jxt/getjxtexttablist.vpage">家校首页扩展项管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/wechatmessage/tm/index.vpage">微信模版消息</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/clientconfig/list.vpage?type=JSPATCH">CRM配置管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/surl/index.vpage">短地址生成</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/reportstatus/index.vpage">Report Status维护</a></li>
                <li><a href="${requestContext.webAppContextPath}/textbook/textbooklist.vpage">教材管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/pageblockcontent/simpleedit.vpage">简化页面内容管理</a></li>
                <li><a href="${requestContext.webAppContextPath}/crm/experiment/diagnosis/courseAnalysis/list.vpage">通用课程实验报告</a></li>
                <li>
                    <a href="${requestContext.webAppContextPath}/crm/courseware/contest/examine/workslist.vpage">课件大赛审核</a>
                </li>
                <li>
                    <a href="${requestContext.webAppContextPath}/site/activity/audit/list.vpage">趣味活动审批</a>
                </li>
                <li><a href="${requestContext.webAppContextPath}/site/order/index.vpage">批量查询购买增值产品</a></li>
                <li>
                    <a href="${requestContext.webAppContextPath}/site/wechat/qrcode/messages.vpage">运营二维码消息管理</a>
                </li>
                <li>
                    <a href="${requestContext.webAppContextPath}/site/achivement/index.vpage">学分体系管理</a>
                </li>
                <li>
                    <a href="${requestContext.webAppContextPath}/site/diamond/index.vpage">金刚位设置管理</a>
                </li>
            </ul>
            <li class="nav-header"><a href="#coin_tag" class="nav-header collapsed" data-toggle="collapse"><i class="glyphicon glyphicon-cog"></i>客服管理<span class="pull-right glyphicon glyphicon-chevron-toggle"></span></a></li>
            <ul id="coin_tag" class="nav nav-list collapse secondmenu" style="height: 0px;">
                <li><a href="${requestContext.webAppContextPath}/site/qiyukf/config/index.vpage">七鱼客服配置</a></li>
                <li><a href="${requestContext.webAppContextPath}/site/qiyukf/stats/query.vpage">七鱼客服统计</a></li>
            </ul>
        </ul>
    </div>
</div>
