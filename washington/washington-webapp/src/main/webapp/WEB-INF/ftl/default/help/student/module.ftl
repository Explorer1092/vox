<#import "/help/module.ftl" as com>
<#macro page t=0>
	<#compress>
		<@com.page  nav=1>
		<div class="location"><a href="/help/index.vpage">帮助与支持</a> > 学生</div>
		<div class="navs">
			<div class="menu">
				<a href="/help/student/homework.vpage">
					<h1 class="instructions">使用说明</h1>
				</a>
				<ul <#if t==0>style="display:none"</#if>>
					<li <#if t==1>class="sel"</#if>><a href="/help/student/homework.vpage">我的作业</a></li>
					<li <#if t==2>class="sel"</#if>><a href="/help/student/learning.vpage">学习中心</a></li>
                    <li <#if t==3>class="sel"</#if>><a href="/help/student/clazz.vpage">我的班级</a></li>
					<li <#if t==4>class="sel"</#if>><a href="/help/student/ucenter.vpage">个人设置</a></li>
					<li <#if t==5>class="sel"</#if>><a href="/help/student/message.vpage">消息盒子</a></li>
					<li <#if t==6>class="sel"</#if>><a href="/help/student/reward.vpage">奖品中心</a></li>
				</ul>
			</div>
			<div class="menu"> <a href="/help/student/question.vpage">
				<h1 class="problem">常见问题</h1>
				</a> </div>
		</div>
		<div class="content separate">
			<#if t==0>
			<div class="blueLine">常见问题  > </div>
			<#else>
			<div class="blueLine">使用说明  > <#if t==1>我的作业<#elseif t==2>学习中心<#elseif t==3>我的班级<#elseif t==4>个人设置<#elseif t==5>消息盒子<#elseif t = 6>奖品中心</#if></div>
			</#if>
			<div id="QAlist" class="problemBox">
				<#nested>
				<div class="warmInfo"><strong>没有在上面看到您的问题？未在上面找到答案？您可以 <a href="/help/problemcontact.vpage" class="blueClr">联系我们</a>   &nbsp;&nbsp;  <a href="http://www.google.com/intl/zh-CN/chrome/" target="_blank" class="blueClr">下载谷歌浏览器</a></strong></div>
			</div>
		</div>
		<div class="clear"></div>
		</@com.page>
	</#compress>
</#macro>
