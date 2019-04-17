<#import "/help/module.ftl" as com>
<#macro page t=0>
	<#compress>
		<@com.page  nav=1>
		<div class="location"><a href="/help/index.vpage">帮助与支持</a> > 家长</div>
		<div class="navs">
			<div class="menu">
				<a href="/help/parents/register.vpage">
					<h1 class="instructions">使用说明</h1>
				</a>
				<ul <#if t==0>style="display:none"</#if>>
					<li <#if t==1>class="sel"</#if>><a href="/help/parents/register.vpage">注册</a></li>
					<li <#if t==2>class="sel"</#if>><a href="/help/parents/child360.vpage">儿童上网管家</a></li>
					<li <#if t==3>class="sel"</#if>><a href="/help/parents/homeworkreport.vpage">作业成绩</a></li>
					<li <#if t==4>class="sel"</#if>><a href="/help/parents/message.vpage">消息中心</a></li>
				</ul>
			</div>
			<div class="menu"> <a href="/help/parents/question.vpage">
				<h1 class="problem">常见问题</h1>
				</a> </div>
		</div>
		<div class="content separate">
			<#if t==0>
			<div class="blueLine">常见问题  > </div>
			<#else>
			<div class="blueLine">使用说明  > <#if t==1>注册<#elseif t==2>儿童上网管家<#elseif t==3>作业成绩<#elseif t==4>消息中心</#if></div>
			</#if>
			<div id="QAlist" class="problemBox">
				<#nested>
				<div class="warmInfo"><strong>没有在上面看到您的问题？未在上面找到答案？您可以 <a href="/help/problemcontact.vpage" class="blueClr">联系我们</a></strong></div>
			</div>
		</div>
		<div class="clear"></div>
		</@com.page>
	</#compress>
</#macro>
