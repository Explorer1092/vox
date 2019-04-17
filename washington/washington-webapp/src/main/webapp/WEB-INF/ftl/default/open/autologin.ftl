<!DOCTYPE html>
<html>
    <head>
        <#include "../nuwa/meta.ftl" />
        <title>一起作业</title>
        <@sugar.capsule js=["jquery"] />
    </head>
    <body>
        <div style="display:none;">
            <form action="/j_spring_security_check" method="post">
                <input id="head_login_username" type="text" name="j_username" value="${(key?html)!}" class="inp" />
                <input id="head_login_password" type="password" name="j_password" class="inp" value="${(value?html)!}" />
                <input type="checkbox" name="_spring_security_remember_me" checked="checked" />
                <label id="remember">记住密码</label>
                <input name="login" type="submit" value="登录" class="dl" style="width:96px;cursor: pointer;"/>
            </form>
        </div>
        <div style="margin:0 auto;text-align: center;font-weight: bold;">
            请稍候，登录跳转中...
        </div>
        <script type="text/javascript">
            $(function(){
                $("input[name=login]").trigger("click");
            });
        </script>
    </body>
</html>