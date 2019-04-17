<#import "/help/module.ftl" as com>
<#macro page t=0>
	<#compress>
		<@com.page nav=1>
		<div class="location"><a href="/help/index.vpage">帮助与支持</a> > <a href="/help/faq/index.vpage">常见问题</a>  > <#if t==1>页面白屏<#elseif t==2>麦克风问题<#elseif t==3>补做作业<#elseif t==4>创建班级<#elseif t==5>添加教材<#elseif t==6>修改密码<#elseif t==7>作业显示<#elseif t==9>如何做练习<#elseif t==10>作业加载<#elseif t==11>如何进行PK</#if></div>
		<div class="navs">
			<div class="menu">
				<a href="/help/faq/index.vpage">
					<h1 class="instructions">常见问题</h1>
				</a>
				<ul>
					<li <#if t==1>class="sel"</#if>><a href="/help/faq/index.vpage">页面白屏</a></li>
					<li <#if t==2>class="sel"</#if>><a href="/help/faq/p_1.vpage">麦克风问题</a></li>
					<li <#if t==3>class="sel"</#if>><a href="/help/faq/p_2.vpage">补做作业</a></li>
					<li <#if t==4>class="sel"</#if>><a href="/help/faq/p_3.vpage">创建班级</a></li>
					<li <#if t==5>class="sel"</#if>><a href="/help/faq/p_4.vpage">添加教材</a></li>
					<#--<li <#if t==6>class="sel"</#if>><a href="/help/faq/p_5.vpage">修改密码</a></li>-->
					<li <#if t==7>class="sel"</#if>><a href="/help/faq/p_6.vpage">作业显示</a></li>
					<li <#if t==9>class="sel"</#if>><a href="/help/faq/p_8.vpage">如何做练习</a></li>
					<li <#if t==10>class="sel"</#if>><a href="/help/faq/p_9.vpage">作业加载</a></li>
					<li <#if t==11>class="sel"</#if>><a href="/help/faq/p_10.vpage">如何进行PK</a></li>
                    <li <#if t==12>class="sel"</#if>><a href="/help/faq/p_11.vpage">更换班级</a></li>
                    <li <#if t==13>class="sel"</#if>><a href="/help/faq/p_12.vpage">大爆料问题</a></li>
				</ul>
			</div>
		</div>
		<div class="content separate">
			<div class="article problemBox">
				<#nested>
				<div class="warmInfo"><strong>没有在上面看到您的问题？未在上面找到答案？您可以 <a href="/help/problemcontact.vpage" class="blueClr">联系我们</a></strong></div>
			</div>
		</div>
		<div class="clear"></div>
		</@com.page>
	</#compress>
</#macro>
