<#list map_top_list as map>
    <#if map_index == 0>
    <h4 style=" margin: 0; font-size:16px; font-family:'宋体';text-align:center;">${clazzName!''}学生名单</h4>
    <h4 style=" margin: 0; font-size:14px; font-family:'宋体';">${teacherName!''}老师您好：</h4>
    <h4 style=" margin: 0; font-size:14px; font-family:'宋体';">请打印学生使用指南发给学生</h4>
    <table style="border-collapse: collapse; font-size: 12px; font-family:'宋体'; margin-left: 25px;">
        <tr>
            <td style="border: 1px solid #000; width: 65px;height: 50px;">打印名单</td>
            <td>——></td>
            <td style="border: 1px solid #000;  width: 65px;height: 50px;">剪裁发给学生</td>
            <td >——></td>
            <td style="border: 1px solid #000;  width: 65px;height: 50px;">学生输入账号密码登录系统完成练习</td>
            <td>——></td>
            <td style="border: 1px solid #000;  width: 65px;height: 50px;">老师检查</td>
        </tr>
    </table>
    <ol style=" font-size: 12px; font-family:'宋体';">
        <li>请打印此名单发给学生，并提醒学生妥善保存，以防忘记账号密码。学生使用自己的账号密码登录系统即可完成老师推荐的练习。为避免账号被盗，建议老师不要公开张贴账号密码！</li>
        <li>老师可使用电脑登录www.17zuoye.com，点击“班级管理－学生详情”，查看或修改学生的密码。</li>
    </ol>
    </#if>

<table width="100%" border="1" cellpadding="1" cellspacing="0" bordercolor="#CCCCCC" bordercolorlight="#FFFFFF" bordercolordark="#CCCCCC" style="font-size:12px;font-family:'宋体';">
    <tr>
        <td colspan="2" align="center" valign="middle"><strong>《一起小学注册指南》请${map.name!''}<#if map.name?has_content>的</#if>家长妥善保管</strong></td>
        <td rowspan="2" align="center" valign="middle">
            <img width="93" height="93" src="http://cdn.17zuoye.com/static/project/app/app-102007-code.png" />
        </td>
    </tr>
    <tr>
        <td nowrap="nowrap" style="width: 130px;font-family:'宋体';">
        ${map.name!'某某'}<br/>
            学生帐号：${map.userId!'xxx'}<br/>
            <#if map.pwd?? && map.pwd?length gt 0>
                学生密码：${map.pwd!'xxx'}
            <#else>
                学生密码已自行修改
            </#if>
        </td>
        <td>
            1. 下载APP：用手机扫描右侧二维码，下载“一起小学学生端”<br />
            2. 注册账号：下载安装后选择“注册账号”，输入老师手机号<br />
            3. 加入班级：确认学校及老师信息无误后，点击“选择所在的班级及年级”，点击“确认加入”<br />
            4. 填写学生的真实姓名后，再填写家长手机号和密码（密码自己设定，便于记忆为主）<br />
            5. 没有智能手机的情况下，请在电脑端输入：www.17zuoye.com，注册步骤和手机端一致
        </td>
    </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="1" style="font-size:12px; color:#aaa;font-family:'宋体';">
    <#if (map_top_list?size != (map_index + 1)) || (map_top_list?size == (map_index + 1) && studentNameList?size != 0)>
        <tr>
            <td align="center" style="color: #000000;padding: 3px;"> - - - - - &#9986; - - - - - - -- - - - - - - -剪裁线 - - - - - - - - - - - - - - - - -</td>
        </tr>
    </#if>
</table>
<#-- 第一页4个 其它页5个 -->
    <#if ((map_index == 3) && (map_top_list?size gt 4))
    || (map_index > 4 && (map_index-4 + 1)%5 == 0)
    || ((map_index + 1) == map_top_list?size && (map_top_list?size - 4) % 5 == 0 && studentNameList?size > 0)>
    <br style="page-break-before: always">
    </#if>
</#list>
