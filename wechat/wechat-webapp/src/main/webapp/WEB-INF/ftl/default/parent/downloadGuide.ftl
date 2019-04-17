<#import './layout.ftl' as layout>
<@layout.page bodyClass='downloadGuide' title="下载最新家长通App"  specialHead='
   	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
	<meta name="format-detection" content="telephone=no" />
	<meta name="format-detection" content="email=no" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black" />
	<title>下载最新家长通App</title>
'>
    <@sugar.capsule css=['downloadGuide'] />
    <#escape x as x?html>
        <div class="banner">
            <div class="box">
                <div>家长通APP全新上线</div>
                <a href="javascript:void(0);" class="btn doClick" data-position="top">立即下载</a>
            </div>
        </div>
        <div class="wrap">
            <div class="text">1.不错过老师发布的最新作业和通知，查看班级完成进度和作业报告，还有语数外全科错题本</div>
            <div class="img"><div class="bg-1"></div></div>
            <div class="text">2.使用免费学习工具：英语点读机、英语随身听、语文课本朗读、随时随地与课本同步练习</div>
            <div class="img"><div class="bg-2"></div></div>
            <div class="text">3.真实班级群，安全高效与班内老师和家长沟通</div>
            <div class="img"><div class="bg-3"></div></div>
            <div class="text">4.更多精选教育资讯，为您提供免费学习辅导资源、名人教子经验、性格习惯培养等多方面文章指导</div>
            <a href="javascript:void(0);" class="btn doClick" data-position="bottom">立即下载最新家长通App</a>
        </div>
    </#escape>

    <script type="text/javascript">
        function pageLog(){
            require(['jquery','logger'], function($,logger) {
                //页面加载打点
                logger.log({
                    module: 'm_8d57XUXT',
                    op: 'o_vfjzejmY'
                });
                //下载按钮点击打点
                var downloadUrl="http://wx.17zuoye.com/download/17parentapp?cid=202007";

                $(document).on("click",".doClick",function(e){
                    var $target=$(e.target),
                            position=$target.data("position");

                    switch (position){
                        case "top":{
                            //顶部下载按钮点击
                            logger.log({
                                module: 'm_8d57XUXT',
                                op: 'o_7Gx8wv7x'
                            });
                            break;
                        }
                        case "bottom":{
                            //底部下载按钮点击
                            logger.log({
                                module: 'm_8d57XUXT',
                                op: 'o_q68wAaxR'
                            });

                            break;
                        }
                    }

                    setTimeout(function(){
                        location.href=downloadUrl;
                    },200);
                })
            })
        }
    </script>
</@layout.page>

