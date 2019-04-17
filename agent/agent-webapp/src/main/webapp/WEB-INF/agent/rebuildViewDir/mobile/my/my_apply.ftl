<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="申请记录"  footerIndex=4>
<@sugar.capsule css=['res']/>
<div class="crmList-box resources-box" style="background:#fff">
    <div class="res-top">
        <div class="return"><a href="/mobile/apply/application.vpage"><i class="return-icon"></i>返回</a></div>
        <span class="return-line"></span>
        <span class="res-title">我发起的申请</span>
    </div>
    <div class="c-main my-apply">
        <a href="/mobile/school_clue/user_clues.vpage">鉴定学校</a>
        <#--<a href="/mobile/teacher_fake/fake_teachers.vpage">老师判假</a>-->
        <#--暂时先不开放入口-->
        <#--<a href="/mobile/my/clazz_apply_record.vpage">老师包班</a>-->
        <#--<a href="/mobile/task/task_list.vpage">客服协助</a>-->
        <a href="/mobile/apply/list.vpage?applyType=AGENT_MODIFY_DICT_SCHOOL&status=1">字典表调整</a>
        <a href="/mobile/apply/list.vpage?applyType=AGENT_MATERIAL_APPLY&status=1">物料购买</a>
        <a href="/mobile/apply/list.vpage?applyType=AGENT_UNIFIED_EXAM_APPLY&status=1">统考测评</a>
        <a href="/mobile/apply/list.vpage?applyType=AGENT_DATA_REPORT_APPLY&status=1">大数据报告申请</a>
    </div>
</div>
</@layout.page>
