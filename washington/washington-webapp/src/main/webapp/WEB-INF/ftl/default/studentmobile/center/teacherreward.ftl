<#import "../layout.ftl" as temp >
<@temp.page>
<div class="wr">
    <#if (currentStudentDetail.clazz.classLevel)??>
        <#if rewardlist?? && rewardlist?size gt 0>
            <div class="info-state3">
                <div class="bg">本学期获得${integralCount!''}学豆</div>
            </div>
            <#list rewardlist as rewardlist>
                <div class="info-title"><span class="step1">${rewardlist.date!''}</span></div>
                <div class="info-text">${rewardlist.rewardContent!''}</div>
            </#list>
        <#else>
            <div class="no-record">老师还没发奖励哦~</div>
        </#if>

    <#else>
        <div class="no-record">加入班级后才可查看哦~</div>
    </#if>
</div>
<script>
    //设置title
    document.title = '老师奖励';
    $(function(){
        //log
        $M.appLog('homework',{
            app: "17homework_my",
            type: "log_normal",
            module: "user",
            operation: "page_teacher_reward"
        });
    });
</script>
</@temp.page>