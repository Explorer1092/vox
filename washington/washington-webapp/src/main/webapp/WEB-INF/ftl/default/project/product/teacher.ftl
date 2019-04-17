<!doctype html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>
-->
<html>
<head>
<#include "../../nuwa/meta.ftl" />
<@sugar.capsule js=["jquery"] css=["project.teacher"] />
</head>
<body style="overflow-y: scroll;">
<div class="header">
	<a href="/" class="logo"></a>
	<div class="link">
		<a href="http://user.qzone.qq.com/2484705684" target="_blank">QQ空间</a>
		<a href="http://weibo.com/yiqizuoye" target="_blank">新浪微博</a>
		<a href="http://1.t.qq.com/zone_17zuoye" target="_blank">QQ微博</a>
	</div>
</div>
<div class="main">
	<!--page1-->
	<div id="page1" <#if p == 1 >style="display:none;"</#if>>
		<div class="content">
			<div class="inline">
				<div class="videoBox">
					<#--<embed src="http://player.youku.com/player.php/sid/XNDQzNzA4NjY4/v.swf" allowFullScreen="true" quality="high" width="400" height="300" align="middle" allowScriptAccess="always" type="application/x-shockwave-flash"></embed>
					<object classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=7,0,0,0" width="400" height="300" id="cc_B13A54A6CA73101F"><param name="movie" value="http://union.bokecc.com/flash/single/CA54DC8006D3C38A_B13A54A6CA73101F_true_BC80316BC90B3146_1/player.swf" /><param name="allowFullScreen" value="true" /><param name="allowScriptAccess" value="always" /><embed src="http://union.bokecc.com/flash/single/CA54DC8006D3C38A_B13A54A6CA73101F_true_BC80316BC90B3146_1/player.swf" width="400" height="300" name="cc_B13A54A6CA73101F" allowFullScreen="true" allowScriptAccess="always" pluginspage="http://www.macromedia.com/go/getflashplayer" type="application/x-shockwave-flash"/></object>
					<embed type="application/x-shockwave-flash" src="http://player.youku.com/player.php/sid/XNDQzNzA4NjY4/v.swf" id="movie_player" name="movie_player" bgcolor="#FFFFFF" quality="high" wmode="transparent" allowfullscreen="true"
flashvars="isShowRelatedVideo=false&showAd=0&show_pre=1&show_next=1&isAutoPlay=false&isDebug=false&UserID=&winType=interior&playMovie=true&MM
Control=false&MMout=false&RecordCode=1001,1002,1003,1004,1005,1006,2001,3001,3002,3003,3004,3005,3007,3008,9999"
pluginspage="http://www.macromedia.com/go/getflashplayer" width="400" height="327"></embed>-->
					<#--<embed src="http://static.video.qq.com/TPout.swf?auto=1&vid=m1020do1t3v" quality="high" width="400" height="300" align="middle" allowScriptAccess="sameDomain" allowFullscreen="true" type="application/x-shockwave-flash"></embed>-->
					<embed src="http://static.video.qq.com/TPout.swf?auto=1&vid=k102500vwc8" quality="high" width="400" height="300" align="middle" allowScriptAccess="sameDomain" allowFullscreen="true" type="application/x-shockwave-flash"></embed>
				</div>
				<div class="stepLearn">
					<p class="s1"></p>
					<p class="s2"></p>
					<p class="s3"></p>
					<p class="loginRegBtn"><a href="/login.vpage" class="login">登录</a><a href="/signup/index.vpage" class="reg">免费注册</a></p>
				</div>
				<div class="clear"></div>
			</div>
		</div>
		<div class="learnmore"></div>
	</div>
	<!--page2-->
	<div id="page2" <#if p != 1 >style="display:none;"</#if>>
		<div class="content">
			<div class="inline">
				<div class="titleFea"><a href="javascript:void(0);" title="返回" class="btn learnback"></a>教育部最新颁布的新课程标准对小学英语教学的要求</div>
				<ul class="features">
					<li class="u1"></li>
					<li class="u2"></li>
					<li class="u3"></li>
					<li class="u4"></li>
					<li class="u5"></li>
					<li class="u6"></li>
				</ul>
				<div class="loginRegBtn"><a href="/login.vpage" class="login">登录</a><a href="/signup/index.vpage" class="reg">免费注册</a></div>
			</div>
		</div>
		<div class="learnback"></div>
	</div>
	<!---->
</div>
<div class="footer">
	<div class="navs">
		<a href="/help/aboutus.vpage">关于我们</a><span>•</span>
		<a href="/help/jobs.vpage">诚聘英才</a><span>•</span>
		<a href="/help/contactus.vpage">联系我们</a><span>•</span>
		<a href="/help/parentsguidelines.vpage">家长须知</a><span>•</span>
		<a href="/help/childrenhealthonline.vpage">儿童安全上网</a><span>•</span>
		<a href="javascript:void(0);">意见反馈</a>
		<span class="f_tel">教师QQ群：<b>235401380</b></span>
		<span class="f_tel">客服电话：<b><@ftlmacro.hotline/></b></span>
	</div>
	<div class="copyright">
		${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
	</div>
</div>
<script type="text/javascript">
	$(function(){
		$(".learnmore").on('click',function(){
			$("#page2").fadeIn();
			$("#page1").slideUp();
		});
		
		$(".learnback").on('click',function(){
			$("#page1").slideDown();
			$("#page2").fadeOut();
		});
	});
</script>
</body>
</html>
<!--
<html>
<head>
<script type="text/javascript" src="/main.js">
</script>
<style>
</style>
</head>
<body>
</body>
</html>