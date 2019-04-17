<#macro page>
<!DOCTYPE html>
<html>
<head>
    <#include "../../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "core", "alert", "template"] css=["plugin.alert", "new_teacher.widget", "plugin.register"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
    <#include "../../layout/project.header.ftl"/>
    <div class="main">
        <div id="signup_form_box" class="loginbox">
            <#nested />
        </div>
    </div>
    <#include "../../layout/project.footer.ftl"/>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
</#macro>

<#macro feedbackButton buttonId="show-feedback">
<a href="javascript:void(0);" id='${buttonId}' style="margin-left: 20px; color: #39f;">遇到问题？我要反馈 &raquo;</a>
<script>
    $(function(){
        var $btn = $('#${buttonId}');
        $btn.click(function(){
            var url = '${ProductConfig.getMainSiteBaseUrl()}/ucenter/feedback-resetpwd.vpage';
            window.open (url,
                    'feedbackwindow',
                    'height=500,width=600,top=300,left=500,toolbar=no,menubar=no,scrollbars=no, resizable=no,location=no, status=no');
            $.prompt(html, { title: "问题反馈", position : { width:450 }, buttons: {} } );
            return false;
        });

        //忘记密码统计事件
        $(document).on("click", ".v-forgetStaticLog", function(){
            var $this = $(this);
            var $op = $this.attr("data-op");
            if( $op != ""){
                $17.voxLog({
                    app : "teacher",
                    module: "forgetStaticRefLog",
                    op: $op
                });
            }
        });
    });
</script>
</#macro>