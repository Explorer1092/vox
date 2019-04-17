<!DOCTYPE html>
<html>
<head>
    <#include "../nuwa/meta.ftl" />
    <title>一起作业，一起作业网，一起作业学生</title>
    <@sugar.capsule js=["jquery", "toolkit", "core", "alert"] css=["plugin.alert", "new_teacher.widget", "plugin.register"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
<#include "../layout/project.header.ftl"/>
<div class="register_box">
    <div id="signup_form_box">
        <h1 class="reg_title">
            绑定账号
        </h1>
    </div>
    <div class="reg_from">
        <div class="switch_con_box">
            <div class="sb_box">
                <span class="sc_img">
                    <i class="sc_icon gray">
                        <img src="${sourceLogo!0?html}" >
                    </i>
                </span>
                 <span class="sc_img" style="width: 70px;">
                    <i class="sc_icon sc_icon_st"></i>
                </span>
                 <span class="sc_img">
                    <i class="sc_icon sc_icon_zuoye"></i>
                </span>
                <p>${sourceName!'---'?html}</p>
            </div>
            <form id="loginForm" method="post" action="/j_spring_security_check">
                <#if dataKey??> <input type="hidden" name="dataKey" value="${dataKey?html}"> </#if>
                <ul id="signup_content_box" class="loginbox" style="width: 530px;">
                    <li class="inp"><b class="tit">一起账号 :</b>
                        <input type="text" value="" data-label="真实姓名" class="require"  id="index_login_username" name="j_username" value="" tabIndex="1">
                    </li>
                    <li class="inp"><b class="tit"> 密码 :</b>
                        <input type="password" value="" data-label="密码" class="require" id="index_login_password" name="j_password" tabIndex="2">
                    </li>
                    <#if error??><li class="inp pad" style="color: #ff0000">用户名或密码错误，请重新输入</li></#if>
                    <li class="inp pad "><a class="clrblue" href="/ucenter/resetnavigation.vpage" target="_blank">忘记账号或密码？</a>
                    </li>
                    <li class="inp pad mag">
                        <a style=" width: 134px;" class="reg_btn submitBtn" href="javascript:void(0);" id="_a_loginForm">绑定账号</a>
                    </li>
                    <li class="inp pad mag"><a style=" width: 214px; font-size: 14px; padding: 12px 0;" id="forback" class="reg_btn reg_btn_well reg_btn_green" href="<#if dataKey??>/signup/index.vpage?dataKey=${dataKey?html}<#else>/signup/index.vpage</#if>">没一起账号？完善个人信息</a></li>
                </ul>
            </form>
            <div class="clear"></div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function(){
        $("#_a_loginForm").on("click", function(){
            $("#loginForm").submit();
            return false
        });
    });
</script>
<#include "../layout/project.footer.ftl"/>
<@sugar.site_traffic_analyzer_end />
</body>
</html>