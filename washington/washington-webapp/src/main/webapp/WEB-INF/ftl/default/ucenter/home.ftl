<ul>
	<#if userId?? >
    <li><a href="/index.vpage">${userId}网站首页</a></li>
    <li><a href="/ucenter/logout.vpage?_=1" title="退出">退出</a></li>
	<#else>
	<li><a href="/ucenter/signup.vpage" title="立即注册成为一起作业用户">注册</a></li>
    <li><a href="/ucenter/login.vpage" title="登录一起作业更精彩">登录</a></li>
    </#if>
</ul>