<h4 style=" margin: 0; font-size:16px; font-family:'宋体';text-align:center;">学生名单</h4>
<h4 style=" margin: 0; font-size:14px; font-family:'宋体';">一、学生名单</h4>
<#if map_top_list?size gt 0>
<table width="100%" border="1" cellpadding="1" cellspacing="0" bordercolor="#CCCCCC" bordercolorlight="#FFFFFF" bordercolordark="#CCCCCC" style="font-size:12px;font-family:'宋体';">
    <tr>
        <td align="center" valign="middle"><strong>学生姓名</strong></td>
        <td align="center" valign="middle"><strong>一起作业ID</strong></td>
        <td align="center" valign="middle"><strong>手机号</strong></td>
        <td align="center" valign="middle"><strong>学生密码</strong></td>
    </tr>
    <#list map_top_list as map>
        <tr>
            <td align="center" valign="middle">${map.name!'某某'}</td>
            <td align="center" valign="middle">${map.userId!'xxx'}</td>
            <td align="center" valign="middle"><#if (map.mobile)?has_content>${map.mobile!'未绑定'}<#else>未绑定</#if></td>
            <td align="center" valign="middle">学生密码已自行修改</td>
        </tr>
    </#list>
</table>
<#else>
<span style=" font-size: 12px; font-family:'宋体';">
&nbsp;&nbsp;暂无学生加入班级
</span>
</#if>
<br><br>
<h4 style=" margin: 0; font-size:14px; font-family:'宋体';">二、让更多学生加入班级</h4>
<ol style=" font-size: 12px; font-family:'宋体';">
    <li>把老师手机号或老师账号发给学生或者学生家长；</li>
    <li>告诉学生或家长下载「一起作业学生端」，下载地址：17zyw.cn/AviMVb；</li>
    <li>学生输入老师手机号或老师号，根据提示加入班级。</li>
    <p style="padding-left: 22px;"><img src="http://cdn-cnc.17zuoye.cn/ucenter/public/skin/teacherv3/images/collect/sign-intro-2.png"/></p>
</ol>