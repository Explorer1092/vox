<#list map_top_list as map>
    <#if map_index == 0>
    <h4 style=" margin: 0; font-size:16px; font-family:'宋体';text-align:center;">${clazzName!''}学生名单</h4>
    <h4 style=" margin: 0; font-size:14px; font-family:'宋体';">方法一：请打印学生使用指南发给学生</h4>
    <table style="border-collapse: collapse; font-size: 12px; font-family:'宋体'; margin-left: 25px;">
        <tr>
            <td style="border: 1px solid #000; width: 65px;height: 50px;">打印名单</td>
            <td>——></td>
            <td style="border: 1px solid #000;  width: 65px;height: 50px;">剪裁发给学生</td>
            <td >——></td>
            <td style="border: 1px solid #000;  width: 65px;height: 50px;">学生加入班级完成作业</td>
            <td>——></td>
            <td style="border: 1px solid #000;  width: 65px;height: 50px;">老师检查</td>
        </tr>
    </table>
    <ol style=" font-size: 12px; font-family:'宋体';">
        <li>为避免其他班学生加入您的班级，建议老师不要公开张贴老师手机号！</li>
        <li>老师可使用电脑登录www.17zuoye.com，点击“我的班级”，查看已加入的学生。</li>
    </ol>
    <h4 style=" margin: 0; font-size:14px; font-family:'宋体';">方法二：和学生或家长讲解使用指南</h4>
    <ol style=" font-size: 12px; font-family:'宋体';">
        <li>把老师手机号或老师账号写在黑板或转发到家长QQ群、微信群；</li>
        <img width="295" height="110" src="http://cdn.17zuoye.com/static/project/app/clazz-work-blackboard.png" />
        <li>告诉学生或家长打开www.17zuoye.com输入老师号或老师手机号，注册、领取学生帐号</li>
        <img width="295" height="160" src="http://cdn.17zuoye.com/static/project/app/clazz-work-register.png" />
        <li>担心学生不会？打印《一起作业注册指南》发给学生！每人一个：</li>
    </ol>
    </#if>

<table width="100%" border="1" cellpadding="1" cellspacing="0" bordercolor="#CCCCCC" bordercolorlight="#FFFFFF" bordercolordark="#CCCCCC" style="font-size:12px;font-family:'宋体';">
    <tr>
        <td colspan="2" align="center" valign="middle"><strong>《一起作业注册指南》请${map.name!''}<#if map.name?has_content>的</#if>家长妥善保管</strong></td>
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
            <#--学生密码：${map.pwd!'xxx'}<br/>-->
        </td>
        <#if studentAppDownload!false>
            <td>
                1.请家长用手机扫描右侧二维码下载软件，在手机完成作业；<br /> 或使用电脑访问一起作业网：www.17zuoye.com，在电脑完成作业<br />
                2.请家长添加微信公众号：一起作业家长通，<br /> 可免费接收作业报告、老师通知，建议父母都加一下<br />
                3.有任何疑问，请在微信“解答专区”或在网站底部咨询客服
            </td>
        <#else>
            <td>
                1. 下载APP：用手机扫描右侧二维码，下载“一起作业学生端”<br />
                2. 注册账号：下载安装后选择“注册账号”，输入老师手机号<br />
                3. 加入班级：确认学校及老师信息无误后，点击“选择所在的班级及年级”，点击“确认加入”<br />
                4. 填写学生的真实姓名后，再填写家长手机号和密码（密码自己设定，便于记忆为主）<br />
                5. 没有智能手机的情况下，请在电脑端输入：www.17zuoye.com，注册步骤和手机端一致
            </td>
        </#if>
    </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="1" style="font-size:12px; color:#aaa;font-family:'宋体';">
    <#if (map_top_list?size != (map_index + 1)) || (map_top_list?size == (map_index + 1) && studentNameList?size != 0)>
        <tr>
            <td align="center" style="color: #000000;padding: 3px;"> - - - - - &#9986; - - - - - - -- - - - - - - -剪裁线 - - - - - - - - - - - - - - - - -</td>
        </tr>
    </#if>

</table>
<#-- 第一页2个 其它页5个 -->
    <#if ((map_index == 1) && (map_top_list?size gt 3))
        || (map_index > 2 && (map_index-2 + 1)%5 == 0)
        || ((map_index + 1) == map_top_list?size && (map_top_list?size - 2) % 5 == 0 && studentNameList?size > 0)>
    <br style="page-break-before: always">
    </#if>
</#list>
<#-- 导入名单列表 -->
<#assign mapSize = (map_top_list?size)>
<#list studentNameList as studentName>
    <#if studentName_index == 0 && map_top_list?size == 0 >
    <h4 style=" margin: 0; font-size:16px; font-family:'宋体';text-align:center;">${clazzName!''}学生名单</h4>
    <h4 style=" margin: 0; font-size:14px; font-family:'宋体';">方法一：请打印学生使用指南发给学生</h4>
    <table style="border-collapse: collapse; font-size: 12px; font-family:'宋体'; margin-left: 25px;">
        <tr>
            <td style="border: 1px solid #000; width: 65px;height: 50px;">打印名单</td>
            <td>——></td>
            <td style="border: 1px solid #000;  width: 65px;height: 50px;">剪裁发给学生</td>
            <td >——></td>
            <td style="border: 1px solid #000;  width: 65px;height: 50px;">学生加入班级完成作业</td>
            <td>——></td>
            <td style="border: 1px solid #000;  width: 65px;height: 50px;">老师检查</td>
        </tr>
    </table>
    <ol style=" font-size: 12px; font-family:'宋体';">
        <li>为避免其他班学生加入您的班级，建议老师不要公开张贴老师手机号！</li>
        <li>老师可使用电脑登录www.17zuoye.com，点击“我的班级”，查看已加入的学生。</li>
    </ol>
    <h4 style=" margin: 0; font-size:14px; font-family:'宋体';">方法二：和学生或家长讲解使用指南</h4>
    <ol style=" font-size: 12px; font-family:'宋体';">
        <li>把老师手机号或老师账号写在黑板或转发到家长QQ群、微信群；</li>
        <img width="295" height="110" src="http://cdn.17zuoye.com/static/project/app/clazz-work-blackboard.png" />
        <li>告诉学生或家长打开www.17zuoye.com输入老师号或老师手机号，注册、领取学生帐号</li>
        <img width="295" height="160" src="http://cdn.17zuoye.com/static/project/app/clazz-work-register.png" />
        <li>担心学生不会？打印《一起作业注册指南》发给学生！每人一个：</li>
    </ol>
    </#if>

<table width="100%" border="1" cellpadding="1" cellspacing="0" bordercolor="#CCCCCC" bordercolorlight="#FFFFFF" bordercolordark="#CCCCCC" style="font-size:12px;font-family:'宋体';">
    <tr>
        <td colspan="2" align="center" valign="middle"><strong>《一起作业注册指南》请${studentName}家长妥善保管</strong></td>
        <td rowspan="2" align="center" valign="middle">
            <img width="93" height="93" src="http://cdn.17zuoye.com/static/project/app/app-102007-code.png" />
        </td>
    </tr>
    <tr>
        <td nowrap="nowrap" style="width: 130px;font-family:'宋体';text-align: center;">
            <#if method=='ACCOUNT'>
                老师账号<br/><strong style="font-size: 16px;">${teacherId!'xxx'}</strong>
            <#else>
                老师手机号<br/><strong style="font-size: 16px;">${mobile!'xxx'}</strong>
            </#if>
        </td>
        <td>
            1. 下载APP：用手机扫描右侧二维码，下载“一起作业学生端”<br />
            2. 注册账号：下载安装后选择“注册账号”，输入老师手机号<br />
            3. 加入班级：确认学校及老师信息无误后，点击“选择所在的班级及年级”，点击“确认加入”<br />
            4. 填写学生的真实姓名后，再填写家长手机号和密码（密码自己设定，便于记忆为主）<br />
            5. 没有智能手机的情况下，请在电脑端输入：www.17zuoye.com，注册步骤和手机端一致
        </td>
    </tr>
</table>
<table width="100%" border="0" cellspacing="0" cellpadding="1" style="font-size:12px; color:#aaa;font-family:'宋体';">
    <#if studentNameList?size != (studentName_index+1) >
        <tr>
            <td align="center" style="color: #000000;padding: 3px;"> - - - - - &#9986; - - - - - - -- - - - - - - -剪裁线 - - - - - - - - - - - - - - - - -</td>
        </tr>
    </#if>
</table>
<#-- 第一页2个 其它页5个 -->
    <#if (mapSize == 0 && studentName_index == 1 && (studentNameList?size gt 3))
        || (mapSize == 1 && studentName_index == 0 && (studentNameList?size gt 2))
        || (mapSize > 2 && ((mapSize - 2) % 5 + studentName_index + 1) % 5 == 0)>
        <br style="page-break-before: always">
    </#if>
</#list>
<#if studentAppDownload!false>
    <img src='http://log.17zuoye.cn/log?_c=vox_logs:web_teacher_logs&_l=3&_log={"userId":"${teacherId!0}","app":"teacher","module":"studentAppDownload","op":"downloadletterforstudentapp","target":""}' alt=""/>
</#if>