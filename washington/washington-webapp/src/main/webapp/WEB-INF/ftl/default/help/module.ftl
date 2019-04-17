<#macro page title="一起教育科技" nav=0>
<!doctype html>
<html>
<head>
    <#include "../nuwa/meta.ftl" />
    <title>一起教育科技</title>
    <@sugar.capsule js=["jquery"] css=["plugin.help"] />
    <@sugar.site_traffic_analyzer_begin />
</head>
<body>
	<div class="header" >
        <div class="navigation">
            <#--<a href="/help/index.vpage" style="float:right; margin:30px 0 0;"><<帮助首页</a>-->
            <span class="logo"><a href="/" style="text-indent: -9999px; display: block;width:117px;height:45px;border:none;">一起教育科技</a></span>
        </div>
	</div>
	<div class="main" style="height: 74%;">
		<#nested>
	</div>
	<div class="footer" >
		<p class="navs"><span class="contacttel">客服电话：<b><@ftlmacro.hotline/></b></span><#--<a href="/help/aboutus.vpage">关于我们</a><i>•</i> <a href="/help/jobs.vpage">诚聘英才</a><i>•</i> <a href="/help/contactus.vpage">联系我们</a><i>•</i> <a href="/help/parentsguidelines.vpage">家长须知</a><i>•</i> <a href="/help/childrenhealthonline.vpage">儿童健康上网</a><i>•</i> <a href="/help/index.vpage">帮助</a>--></p>
        ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
	</div>
    <script type="text/javascript">
        $(function(){
            var listLi = $('#QAlist li');
            if( listLi.parent().height() > 350 ){
                listLi.parent().css({'height':''});
            }else{
                listLi.parent().css({'height':'400'});
            }
            listLi.on('click', function(){
                $(this).find('div').slideDown(150).closest(listLi).siblings().find("div").slideUp(150);
            });
            $('html').css('height', '100%');
			$('body').css('height', '100%');
        });
    </script>
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
</#macro>