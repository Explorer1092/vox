<#import "../layout.ftl" as layout>
<@layout.page group="workbench" title="我的工作台">
<div class="ui-grid-a">
    <div class="ui-block-a" style="padding-bottom: 20px; padding-right: 5px">
        <div class="ui-shadow ui-btn ui-corner-all"
             style="height:80px; text-align: center; font-size: small; background-color: #545454;">
            今日完成工作量<br>

            <div style="font-size: 1.8em; font-weight: bold; color: red">
            ${(((workload.dayDone)!0)/100)?string("#")}
            </div>
            <div style="text-align: center; font-size: small;">
                还需完成 ${(((workload.dayRemain)!0)/100)?string("#")}
            </div>
        </div>
    </div>
    <div class="ui-block-b" style="padding-bottom: 20px; padding-right: 5px">
        <div class="ui-shadow ui-btn ui-corner-all"
             style="height:80px; text-align: center; font-size: small; background-color: #545454;">
            当月完成工作量<br>

            <div style="font-size: 1.8em; font-weight: bold; color: red">
            ${(((workload.monthDone)!0)/100)?string("#")}
            </div>
            <div style="text-align: center; font-size: small;">
                完成度 ${(workload.monthRate)!0}%
            </div>
        </div>
    </div>
</div>

<div class="ui-grid-a">
    <div class="ui-block-a">
        <a href="visit_list.vpage" data-ajax="false" class="ui-shadow ui-btn ui-corner-all" style="padding: 1.5em 0;">拜访计划</a>
    </div>
    <div class="ui-block-b">
        <a href="workload.vpage" data-ajax="false" class="ui-shadow ui-btn ui-corner-all" style="padding: 1.5em 0;">有效工作量</a>
    </div>
</div>
<div class="ui-grid-a">
    <div class="ui-block-a">
        <a href="record_list.vpage" data-ajax="false" class="ui-shadow ui-btn ui-corner-all" style="padding: 1.5em 0;">工作记录</a>
    </div>
    <div class="ui-block-b">
        <a href="flow_task.vpage" data-ajax="false" class="ui-shadow ui-btn ui-corner-all" style="padding: 1.5em 0;">流转任务</a>
    </div>
</div>
<div class="ui-grid-a">
    <div class="ui-block-a">
        <a href="/mobile/workbench/clue/clue_index.vpage" data-ajax="false" class="ui-shadow ui-btn ui-corner-all"
           style="padding: 1.5em 0;">学校线索</a>
    </div>
    <div class="ui-block-b">
        <a href="feedback_list.vpage" data-ajax="false" class="ui-shadow ui-btn ui-corner-all"
           style="padding: 1.5em 0;">意见反馈</a>
    </div>
</div>
<div class="ui-grid-a">
    <div class="ui-block-a">
        <a href="/resetPassword.vpage?client=h5" data-ajax="false" class="ui-shadow ui-btn ui-corner-all"
           style="padding: 1.5em 0;">修改密码</a>
    </div>
</div>

<script type="text/javascript">
    $(function () {
        $("#header-logout").show();
    });
</script>
</@layout.page>