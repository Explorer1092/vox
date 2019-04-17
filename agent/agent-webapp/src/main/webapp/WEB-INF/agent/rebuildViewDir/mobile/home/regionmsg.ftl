<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="大区寄语" pageJs="regionmsg">
<@sugar.capsule css=['base','new_home']/>
<div class="homeText-box">
    <div class="ht-text">
        <textarea id="regMsg" placeholder="在此处编辑大区寄语吧（限35字以内）" maxlength="35"></textarea>
        <p class="ht-info">保存后将对本大区内所有市经理和市场专员可见</p>
    </div>
</div>
<script>
    var groupid = "${groupId!0}";
</script>
</@layout.page>