<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="我发起的申请"  footerIndex=4>
    <@sugar.capsule css=['res']/>
<style>
    .s-right{float:right}
    .red{    border-radius: 100%;
        color: white;
        width: 1rem;
        height: 1rem;
        display: inline-block;
        text-align: center;
        font-size: .60rem;
        vertical-align: middle;
        line-height: 1rem;
        background: red;}
    .s-right .seeArrow{margin: 0 0 0 .25rem;
        display: inline-block;
        vertical-align: middle;
        width: .45rem;
        height: .85rem;
        background: url(/public/rebuildRes/image/mobile/home/icon_arrow_right.png?v=c73404835e) no-repeat;
        background-size: 100% 100%;}
</style>
<div class="crmList-box resources-box" style="background:#fff">
    <div class="c-main my-apply">
        <a onclick='openSecond("/mobile/school_clue/user_clues.vpage")'>鉴定学校<div class="s-right">
    <#if pendingMap?? && pendingMap["AGENT_SCHOOL_AUTH"]?? && pendingMap["AGENT_SCHOOL_AUTH"] != 0><span class="red">${pendingMap["AGENT_SCHOOL_AUTH"]!0}</span>
            </#if><i class="seeArrow"></i>
        </div></a>
    <#--<a href="/mobile/teacher_fake/fake_teachers.vpage">老师判假</a>-->
    <#--暂时先不开放入口-->
    <#--<a href="/mobile/my/clazz_apply_record.vpage">老师包班</a>-->
    <#--<a href="/mobile/task/task_list.vpage">客服协助</a>-->
        <a onclick='openSecond("/mobile/apply/list.vpage?applyType=AGENT_MODIFY_DICT_SCHOOL&status=1")'>字典表调整
            <div class="s-right">
                <#if pendingMap?? && pendingMap["AGENT_MODIFY_DICT_SCHOOL"]?? && pendingMap["AGENT_MODIFY_DICT_SCHOOL"] != 0><span class="red">${pendingMap["AGENT_MODIFY_DICT_SCHOOL"]!0}</span>
                </#if><i class="seeArrow"></i>
            </div>
        </a>
        <a onclick="openSecond('/mobile/apply/list.vpage?applyType=AGENT_MATERIAL_APPLY&status=1')">物料购买
            <div class="s-right">
    <#if pendingMap?? && pendingMap["AGENT_MATERIAL_APPLY"]?? && pendingMap["AGENT_MATERIAL_APPLY"] != 0><span class="red">${pendingMap["AGENT_MATERIAL_APPLY"]!0}</span>
    </#if><i class="seeArrow"></i>
            </div>
        </a>
        <a onclick='openSecond("/mobile/apply/list.vpage?applyType=AGENT_UNIFIED_EXAM_APPLY&status=1")'>统考测评
            <div class="s-right">
        <#if pendingMap?? && pendingMap["AGENT_UNIFIED_EXAM_APPLY"]?? && pendingMap["AGENT_UNIFIED_EXAM_APPLY"] != 0><span class="red">${pendingMap["AGENT_UNIFIED_EXAM_APPLY"]!0}</span>
        </#if> <i class="seeArrow"></i>
            </div>
        </a>
        <a onclick='openSecond("/mobile/apply/list.vpage?applyType=AGENT_DATA_REPORT_APPLY&status=1")'>大数据报告申请
            <div class="s-right">
            <#if pendingMap?? && pendingMap["AGENT_DATA_REPORT_APPLY"]?? && pendingMap["AGENT_DATA_REPORT_APPLY"] != 0><span class="red">${pendingMap["AGENT_DATA_REPORT_APPLY"]!0}</span>
            </#if> <i class="seeArrow"></i>
            </div>
        </a>
    </div>
</div>
</@layout.page>
