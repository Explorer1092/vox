<#import "../module.ftl" as com>
<#macro page t=0 title="">
	<#compress>
		<@com.page  nav=1>
		<div class="location"><a href="/help/index.vpage">帮助与支持</a> > 老师</div>
		<div class="navs">
			<div class="menu">
				<a href="/help/teacher/homework.vpage">
					<h1 class="instructions">使用说明</h1>
				</a>
				<ul <#if t==0>style="display:none"</#if>>
					<li <#if t==1>class="sel"</#if>><a href="/help/teacher/homework.vpage">作业管理</a></li>
					<#--<li <#if t==2>class="sel"</#if>><a href="/help/teacher/report.vpage">汇总报告</a></li>-->
					<li <#if t==3>class="sel"</#if>><a href="/help/teacher/clazz.vpage">班级管理</a></li>
					<li <#if t==4>class="sel"</#if>><a href="/help/teacher/resource.vpage">资源管理</a></li>
					<li <#if t==5>class="sel"</#if>><a href="/help/teacher/message.vpage">消息中心</a></li>
					<li <#if t==6>class="sel"</#if>><a href="/help/teacher/reward.vpage">教学用品中心</a></li>
					<li <#if t==7>class="sel"</#if>><a href="/help/teacher/ucenter.vpage">个人中心</a></li>
                    <li <#if t==8>class="sel"</#if>><a href="/help/teacher/smartclazz.vpage">智慧课堂</a></li>
                </ul>
			</div>
			<div class="menu"> <a href="/help/teacher/question.vpage">
				<h1 class="problem">常见问题</h1>
				</a> </div>
		</div>
		<div class="content separate">
			<#if t==0>
			<div class="blueLine">常见问题  > </div>
			<#else>
			<div class="blueLine">使用说明  > ${title!""}</div>
			</#if>
			<div id="QAlist" class="problemBox">
				<#nested>
				<div class="warmInfo"><strong>没有在上面看到您的问题？未在上面找到答案？您可以 <a href="/help/problemcontact.vpage" class="blueClr">联系我们</a></strong></div>
			</div>
		</div>
	<!-- InstanceEndEditable -->	
		<!--end-->
		<div class="clear"></div>
		</@com.page>
	</#compress>
</#macro>
