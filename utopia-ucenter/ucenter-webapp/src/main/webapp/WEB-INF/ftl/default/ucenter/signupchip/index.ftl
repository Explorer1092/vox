<#import "module.ftl" as com>
<@com.page title="注册新账号" t=1>
    <h1 class="reg_title">
        <#if dataKey?? && dataKey?has_content>
            <span class="rt">已有一起教育账号？<a href="/ssologinbind.vpage<#if dataKey?? && dataKey?has_content>?dataKey=${dataKey}</#if>" class="clrblue">绑定账号</a></span>
        </#if>
        注册新账号
    </h1>
    <div class="reg_step">
        <p class="s_1"></p>
    </div>
    <!--reg_type-->
    <div class="reg_type">
        <#--<h4>请选择您要注册的学号类型</h4>-->
        <ul>
            <#assign suffix = "" />
            <#if dataKey?? && dataKey?has_content>
                <#assign suffix = "?dataKey=" + dataKey />
            </#if>
            <li class="teacher"><a href="/signup/htmlchip/teacher.vpage${suffix}" class="mytc" title="我是老师"><strong>我是<span>老师</span></strong>请选择这里</a></li>
            <li class="parents"><a href="/signup/htmlchip/parent.vpage${suffix}" class="myst" title="我是家长"><strong>我是<span>家长</span></strong>请选择这里</a></li>
            <li class="student"><a href="/signup/htmlchip/student.vpage${suffix}" class="mypt" title="我是学生"><strong>我是<span>学生</span></strong>请选择这里</a></li>
        </ul>
        <div class="clear"></div>
    </div>
    <div class="aliCenter clrgray" style="padding:20px 0 40px; border-top: 1px solid #ccc; clear: both;">如果您忘记学号，可以咨询在线客服：
        <span style="display: inline-block; margin-left: 20px;">已有账号，<a href="/" style="color: #39f; font-size: 14px;">立即登录</a></span>
    </div>
    <script type="text/javascript">
        $(function(){
            $17.tongji("注册事件点击量");
        });
    </script>
</@com.page>