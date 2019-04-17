<#import "../../nuwa/teachershellv3.ftl" as temp />
<#macro page index = "0" title="邀请">
<@temp.page show="invitation">
    <@sugar.capsule js=["ZeroClipboard"] css=["new_teacher.inviteupgrade"] />

    <#nested>
    <script type="text/javascript">
        $(function(){
            LeftMenu.focus("invite");
        });
    </script>
</@temp.page>
</#macro>
