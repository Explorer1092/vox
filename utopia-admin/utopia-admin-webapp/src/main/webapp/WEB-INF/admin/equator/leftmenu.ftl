<div class="span2" id="equatorLeftMenu">
    <div class="well sidebar-nav nav" style="background-color: #fff;">
        <li data-toggle="collapse" data-target="#configuration" class="nav-header">配置管理</li>
        <li id="configuration">
            <ul class="nav nav-list">
                <li>
                    <a href="${requestContext.webAppContextPath}/equator/config/blacklist/index.vpage">黑名单管理</a>
                    <a href="${requestContext.webAppContextPath}/equator/config/generalconfig/manage/index.vpage">通用配置管理</a>
                    <a href="${requestContext.webAppContextPath}/equator/config/resourcetablemanage/index.vpage">资源表管理</a>
                </li>
            </ul>
        </li>

        <li data-toggle="collapse" data-target="#mission" class="nav-header">任务</li>
        <li id="mission">
            <ul class="nav nav-list">
                <li>
                    <a href="${requestContext.webAppContextPath}/equator/mission/task/index.vpage">Task</a>
                    <a href="${requestContext.webAppContextPath}/equator/mission/assignment/index.vpage">Assignment</a>
                    <a href="${requestContext.webAppContextPath}/equator/mission/event/index.vpage">Event</a>
                </li>
            </ul>
        </li>

        <li data-toggle="collapse" data-target="#userinfo" class="nav-header">用户数据查询</li>
        <li id="userinfo">
            <ul class="nav nav-list">
                <li>
                    <a href="${requestContext.webAppContextPath}/equator/newwonderland/material/list.vpage?studentId=${studentId!''}">用户中心</a>
                    <a href="${requestContext.webAppContextPath}/equator/newwonderland/common/rank/detail.vpage?studentId=${studentId!''}">排行榜详情</a>
                    <a href="${requestContext.webAppContextPath}/equator/student/history/index.vpage?studentId=${studentId!''}">学生操作记录</a>
                    <a href="${requestContext.webAppContextPath}/equator/student/history/material.vpage?studentId=${studentId!''}">学生道具变化记录</a>
                </li>
            </ul>
        </li>

        <li data-toggle="collapse" data-target="#tags" class="nav-header">标签管理</li>
        <li id="tags">
            <ul class="nav nav-list">
                <li>
                    <a href="${requestContext.webAppContextPath}/equator/newwonderland/tag/index.vpage">标签管理</a>
                </li>
            </ul>
        </li>

        <li data-toggle="collapse" data-target="#others" class="nav-header">其他</li>
        <li id="others">
            <ul class="nav nav-list">
                <li>
                    <a href="${requestContext.webAppContextPath}/equator/sapling/activity/present.vpage">青苗活动</a>
                    <a href="${requestContext.webAppContextPath}/equator/pmc/index.vpage">渠道管理</a>
                    <a href="${requestContext.webAppContextPath}/equator/babyeagle/studentlearninfo.vpage">小鹰(国)学堂</a>
                    <a href="${requestContext.webAppContextPath}/equator/mailservice/index.vpage">活动通知</a>
                    <a href="${requestContext.webAppContextPath}/equator/popup/index.vpage">通用弹窗</a>
                    <a href="${requestContext.webAppContextPath}/equator/newwonderland/tools/workbook.vpage">小工具</a>
                </li>
            </ul>
        </li>
    </div>
</div>

<script>
    $("#equatorLeftMenu a").each(function () {
        if (window.location.href.indexOf($(this).attr("href")) >= 0) {
            $(this).css("background-color", "#EEEEEE;");
        }
    });
</script>
