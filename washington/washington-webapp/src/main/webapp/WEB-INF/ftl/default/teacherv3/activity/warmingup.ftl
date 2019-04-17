<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main">
    <@app.css href="public/skin/project/warmingup/css/skin.css" />
<div class="examPreheat">
    <div class="ep-banner"><img src="<@app.link href="public/skin/project/warmingup/images/ep-banner.png" />"></div>
    <!--小学数学-->
    <#if (currentTeacherDetail.subject == "MATH")!false>
    <div class="ep-detail">
        <div class="title">内容特色</div>
        <ul class="flavor">
            <li><i class="f-icon f-icon01"></i><span class="text">当地高频易错题集锦</span></li>
            <li><i class="f-icon f-icon02"></i><span class="text">重点难点知识梳理</span></li>
            <li><i class="f-icon f-icon03"></i><span class="text">本地期末统考真题集锦</span></li>
        </ul>
    </div>
    </#if>
    <!--小学英语-->
    <#if (currentTeacherDetail.subject == "ENGLISH")!false>
    <div class="ep-detail">
        <div class="title">内容特色</div>
        <ul class="flavor">
            <li><i class="f-icon f-icon01"></i><span class="text">高频易错单词集锦</span></li>
            <li><i class="f-icon f-icon02"></i><span class="text">重点难点知识梳理</span></li>
        </ul>
    </div>
    </#if>
    <div class="ep-detail">
        <div class="title">活动详情</div>
        <ul class="detail">
            <li>
                <p class="iconBox"><i class="d-icon d-icon01"></i></p>
                <div class="info">
                    <p class="name">布置期末复习作业</p>
                    <p class="intro">每布置一份期末复习作业，会获得额外的园丁豆奖励</p>
                    <i class="arrow-icon"></i>
                </div>
            </li>
            <li>
                <p class="iconBox"><i class="d-icon d-icon02"></i></p>
                <div class="info">
                    <p class="name">奖励学生、写评语</p>
                    <p class="intro">可在作业报告中检查期末复习作业，查看学生完成情况</p>
                    <p class="intro">可对学生进行评语及奖励学豆</p>
                    <i class="arrow-icon"></i>
                </div>
            </li>
            <li>
                <p class="iconBox"><i class="d-icon d-icon03"></i></p>
                <div class="info">
                    <p class="name">额外园丁豆奖励</p>
                    <p class="intro">检查作业后，根据学生的完成情况，可获得额外的园丁豆奖励.</p>
                </div>
            </li>
        </ul>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        LeftMenu.focus("warmingup");
    });
</script>
</@shell.page>