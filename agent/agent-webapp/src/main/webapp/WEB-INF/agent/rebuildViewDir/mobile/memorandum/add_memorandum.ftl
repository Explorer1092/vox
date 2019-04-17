<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="添加备忘录" pageJs="memorandum" footerIndex=2 navBar="hidden">
    <@sugar.capsule css=['school']/>
<div id="list" class="flow">
        <textarea name="" id="context" cols="20" rows="10" placeholder="请输入" style="width:90%;padding:0;margin:5%;border:1px solid #eaeaea"></textarea>
</div>
<script>
    var schoolId = ${schoolId!0};
    var teacherId = ${teacherId!0};
</script>
</@layout.page>