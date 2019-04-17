<!DOCTYPE html>
<html>
    <head>
        <#include "../nuwa/meta.ftl" />
        <title>一起作业</title>
        <@sugar.capsule js=["jquery"] />
    </head>
    <body>
        <div style="display:none;">
            <form action="${targetUrl}" method="post" id="frm">
                <input id="token" type="text" name="token" value="${token}" />
                <input id="uid" type="text" name="uid" value="${uid}" />
                <input id="url_input" type="text" name="url_input" value="${urlInput!''}"/>
            </form>
        </div>
        <div style="margin:0 auto;text-align: center;font-weight: bold;">
            请稍候，登录跳转中...
        </div>
        <script type="text/javascript">
            $(function(){
                $("#frm").submit();
            });
        </script>
    </body>
</html>