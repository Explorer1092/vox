<#import "../layout.ftl" as temp >
<@temp.page dpi="" title="学豆奖励">
    <@app.css href="public/skin/mobile/pc/css/beanreward.css" />

    <#if oldVersion!true>
        <div class="t-rewardDisable-box">
            <div class="disable-bg">
                <div class="d-title">星星奖励已自动发放</div>
            </div>
            <div class="lottery-ticket">
                <div class="l-left">通<br>知</div>
                <div class="l-right">星星榜奖励自3月起已暂停发放，2月及上学期排名奖励已自动发放</div>
            </div>
            <div class="disable-info">*奖励条件：老师2月或上学期奖励过星星的学生</div>
            <div class="disable-column">
                <div class="c-title">下载新版，奖励榜中可领更多学豆</div>
                <p class="c-paragraph">使用一起作业最新版，在【班级】->【奖励榜】中，可领取月奖励或学期奖励，比星星榜更给力哟~</p>
            </div>
            <div class="disable-btn">
                <a href="http://wx.17zuoye.com/download/17studentapp?cid=100158" class="receive-btn">下载最新版本</a>
            </div>
        </div>
    <#else>
        <div class="t-beanRewardCharts-box">
            <div class="charts-bg-1">
                <div class="charts-bg-2">
                    <div class="c-info">
                        <#assign notReceivedRewardCount =  notReceivedRewardCount!0>
                        <#if notReceivedRewardCount == 0 >
                            <span class="num text">暂无奖励</span>
                        <#else>
                            <i class="beans-icon"></i> <span class="num">+${notReceivedRewardCount}</span>
                        </#if>
                    </div>
                    <#--<#if myRank?has_content>
                        <p class="c-tips">月奖励排名第${myRank!""}名</p>
                    </#if>-->
                </div>
            </div>
            <div class="mount-info">安装一起作业家长通后，进入“班级群”里的“查看动态"，检查孩子的作业报告，获得成长值并领取已完成作业的额外学豆奖励，未领取的学豆奖励过期将被清零。 </div>
            <div class="pickUp-btn">
                <a href="javascript:;" data-module="beanReward" data-op="open_success" class="receive-btn doClickOpenParent">立即去领取</a>
            </div>
            <div class="charts-bg-3"></div>
        </div>
    </#if>
<script type="text/javascript">
    document.title = "学豆奖励";
</script>
</@temp.page>

