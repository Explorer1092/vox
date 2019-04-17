<#import "../../layout/project.module.student.ftl" as temp />
<@temp.page title="一起作业4岁啦">
	<@app.css href="public/skin/project/fouryearcelebrate/css/skin.css" />
	<div class="stu_4year_logo">
        <a href="/" title="返回一起作业首页"></a>
	</div>
<#if (currentUser.userType == 3)!false>
	<div class="stu_4year_container">
		<div class="stu_4year_main">
			<div class="stu_4year_main01">
				<p>17作业四岁啦！在这四年中作业君陪伴大家的成长，与同学们学习玩耍，深深感到小伙伴是最最重要的，而你是否注意到身边的小伙伴了呢？一起加入小组，跟小伙伴们一起进步吧。</p>
			</div>
			<div class="stu_4year_main02">
				<div>
					<span>活动时间：</span>
					<p>10月10日—10月23日</p>
				</div>
				<div style="clear: both;">
					<span>活动说明：</span>
					<p>活动期间，在小组内的学生完成作业，学生即可获得<i>×2</i>倍学豆奖励</p>
				</div>
			</div>
			<div class="stu_4year_main03">
				<p>小组长邀请小组成员帮助促进小组成员完成作业~</p>
                <#--<a href="http://help.17zuoye.com/?page_id=1095" target="_blank" class="btn_4year btn_4year_link" style="margin-left: 100px; margin-top: 30px;">如何加入小组</a>-->
                <a href="//cdn.17zuoye.com/static/project/grouphelpa/index.html" target="_blank" class="btn_4year btn_4year_link" style="margin-left: 100px; margin-top: 30px;">如何加入小组</a>
			</div>
		</div>
	</div>
<#else>
	<!--tea_4year_container——start-->
	<div class="stu_4year_container">
		<div class="stu_4year_main">
			<div class="stu_4year_main01">
				<p>17作业四岁啦！在这四年中老师们见证了作业君的成长，作业君也不断努力为老师们服务。现在新的小组功能，希望能协助老师们更加方便的管理班级，以感谢老师们的支持与信任。</p>
			</div>
			<div class="stu_4year_main02">
				<div>
					<span>活动时间：</span>
					<p>10月10日—10月23日</p>
				</div>
				<div style="clear: both;">
					<span>活动说明：</span>
					<p>活动期间，在小组内的学生完成作业，老师即可获得<i>×2</i>倍园丁豆奖励</p>
				</div>
			</div>
			<div class="stu_4year_main03">
				<p>老师设置小组长，指导小组长管理小组</p>
                <a href="${(ProductConfig.getUcenterUrl())!''}/teacher/systemclazz/clazzindex.vpage?type=celebrate" target="_blank" class="btn_4year btn_4year_link" style="margin-left: 100px; margin-top: 30px;">小组管理</a>
			</div>
		</div>
	</div>
</#if>
</@temp.page>