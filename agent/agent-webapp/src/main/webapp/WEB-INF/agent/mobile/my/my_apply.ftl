<#import "../../rebuildViewDir/mobile/layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="我的申请"  footerIndex=4>
<@sugar.capsule css=['res']/>
<div class="crmList-box resources-box">
    <div class="c-main my-apply">
        <a href="/mobile/school_clue/user_clues.vpage">学校线索</a>
        <a href="/mobile/teacher_fake/fake_teachers.vpage">老师判假</a>
        <#--暂时先不开放入口-->
        <#--<a href="/mobile/my/clazz_apply_record.vpage">包班申请</a>-->
    </div>
</div>
</@layout.page>