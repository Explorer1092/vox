<#macro page title="一起教育科技_让学习成为美好体验" htmlClass="" bodyClass="">
<#assign mainSiteBaseUrl = (ProductConfig.getMainSiteBaseUrl())!''>
<!doctype html>
<!--<html><head><script type="text/javascript" src="/main.js"></script><style></style></head><body></body></html>-->
<html class="${htmlClass!}">
<head>
    <meta charset="utf-8">
    <title>${title!'一起教育科技_让学习成为美好体验'}</title>
    <meta name="keywords" content="一起作业,一起作业网,17作业网,一起作业网英语,一起作业学生端,一起学网校,一起作业教师端,家长通,在线教育平台,学生APP">
    <meta name="description" content="一起作业是一款免费学习工具，是一个学生、老师和家长三方互动的作业平台，老师轻松布置作业，学生快乐做作业，家长可以定期查看孩子的学习进度及报告，情景交融的学习模式，让孩子轻松搞定各科学习！一起作业，让学习成为美好体验。">
    <link rel="shortcut icon" href="/favicon.ico" type="image/x-icon" />
    <@sugar.capsule js=['jquery', 'core', 'template', 'flexslider', 'alert', 'DD_belatedPNG'] css=['plugin.alert', 'loginv5','indexv5'] />
    <@sugar.check_the_resources />
    <script type="text/javascript">
        (function(){
            if(getQueryString('ref') != "back"){
                var browser = {
                    versions: function(){
                        var u = navigator.userAgent;
                        var isMobile = {//移动终端浏览器版本信息
                            mobile : !!u.match(/AppleWebKit.*Mobile.*/), //是否为移动终端
                            android: u.indexOf('Android') > -1, //android终端或者uc浏览器
                            isIOS : u.indexOf('iPhone') > -1 //是否为iPhone或者iPad
                        };

                        //移动端访问页面
                        if(isMobile.android || (isMobile.isIOS && isMobile.mobile)){
                            location.href = "${mainSiteBaseUrl}/project/mobilegoin/index.vpage";
                        }
                    }()
                };
            }

            function getQueryString(name) {
                var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
                var r = window.location.search.substr(1).match(reg);
                if (r != null) return unescape(r[2]); return null;
            }
        }());
    </script>
    <@sugar.site_traffic_analyzer_begin />
</head>
<body class="${bodyClass!}">

<#--<div class="zy-header JS-indexPageBox">
    <div class="zy-inner">
        <div class="zy-nav">
            <a href="/" class="logo"></a>
            <div class="navList">
                <a href="/index.vpage" class="menu active" title="首页">首页</a>
                <a href="${mainSiteBaseUrl}/help/concept.vpage" class="menu" title="产品概念">产品概念</a>
                &lt;#&ndash;<a href="${mainSiteBaseUrl}/help/uservoice.vpage" class="menu" title="各界声音">各界声音</a>
                <a href="${mainSiteBaseUrl}/help/news/index.vpage" class="menu" title="新闻中心">新闻中心</a>&ndash;&gt;
                <a href="${mainSiteBaseUrl}/help/aboutus.vpage" class="menu" title="关于我们">关于我们</a>
                <a href="${mainSiteBaseUrl}/help/jobs.vpage" class="menu" title="加入我们">加入我们</a>
                <a href="https://www.17xueba.com/index.vpage" class="menu" title="一起学网校">一起学网校</a>
            </div>
        </div>
        <div class="rightIn">

        </div>
    </div>
</div>-->

<div class="header-wrap">
    <div class="header">
        <div class="logo"><a href="/"></a></div>
        <ul class="nav-list">
            <a class="link active" href="/"><i>首页</i></a>
            <a class="link" href="${mainSiteBaseUrl}/help/education.vpage" ><i>关于一起</i></a>
            <div class="link JS-productServer ${((currentMenu == '一起小学' || currentMenu == '一起中学' || currentMenu == '一起学' || currentMenu == '一起公益')!false)?string('active', '')}">
                <i>产品服务</i>
                <div class="product-wrap JS-productWrap">
                    <div class="product-inner">
                        <span class="icon"></span>
                        <div class="product-box">
                            <a class="${((currentMenu == '一起小学')!false)?string('active', '')}" href="${mainSiteBaseUrl}/help/primaryschool.vpage">一起小学</a>
                            <a class="${((currentMenu == '一起中学')!false)?string('active', '')}" href="${mainSiteBaseUrl}/help/middleschool.vpage">一起中学</a>
                            <a class="${((currentMenu == '一起学')!false)?string('active', '')}" href="${mainSiteBaseUrl}/help/togetherlearn.vpage">一起学</a>
                            <a class="${((currentMenu == '一起公益')!false)?string('active', '')}" href="${mainSiteBaseUrl}/help/publicwelfare.vpage">一起公益</a>
                        </div>
                    </div>
                </div>
            </div>
            <a class="link" href="${mainSiteBaseUrl}/help/latestnews.vpage"><i>最新动态</i></a>
            <a class="link" href="https://app.mokahr.com/apply/17zuoye/524" target="_blank"><i>加入我们</i></a>
            <a class="link" href="https://www.17xueba.com/index.vpage" title="一起学网校" target="_blank"><i>一起学网校</i></a>
            <a class="link" href="${(ProductConfig.getMainSiteBaseUrl())!}/help/downloadApp.vpage?refrerer=pc"><i style="font-weight: bold">APP下载</i></a>

        </ul>
    </div>
</div>

    <#nested />

    <#--<div class="zy-homeFooter">
        <div class="innerBox">
            <div class="left">
                &lt;#&ndash;<div class="il-ppxd">
                    <span class="il-md">兄弟品牌：</span>
                    <a href="http://www.ustalk.com" target="_blank"><i class="zy-icon icon-ustalk"></i></a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!}/help/downloadApp.vpage?refrerer=pc&count=2" target="_blank"><i class="zy-icon icon-parent"></i><span>家长通</span></a>
                    &lt;#&ndash;<a href="http://kuailexue.com" target="_blank"><i class="zy-icon icon-kuailexue"></i><span>快乐学</span></a>&ndash;&gt;
                </div>&ndash;&gt;
                <div class="text">
                    ${(pageBlockContentGenerator.getPageBlockContentHtml('PlatformCopyright', 'webCopyright'))!''}
                </div>
            </div>
            <div class="right">
                <div class="cell">
                    <a href="javascript:;" class="tag" style="cursor: default;">使用须知</a>
                    <a href="${mainSiteBaseUrl}/help/serviceagreement.vpage?agreement=0" target="_blank"><i class="zy-icon icon-agree1"></i><span>用户协议</span></a>
                    <a href="${mainSiteBaseUrl}/help/privacyprotection.vpage" target="_blank"><i class="zy-icon icon-agree2"></i><span>隐私保护</span></a>
                </div>
                <div class="cell" style="width:120px;">
                    <a href="javascript:;" class="tag" style="cursor: default;">联系我们</a>
                    <a href="${mainSiteBaseUrl}/help/kf/index.vpage" target="_blank"><i class="zy-icon icon-bzfk"></i><span>帮助反馈</span></a>
                    <a href="javascript:;"><i class="zy-icon icon-tel"></i><span>400-160-1717</span></a>
                </div>
                <div class="cell">
                    <a href="javascript:;" class="tag" style="cursor: default;">关注我们</a>
                    <a href="http://weibo.com/yiqizuoye" target="_blank"><i class="zy-icon icon-xlwb"></i><span>新浪微博</span></a>
                    <a href="javascript:;">
                        <i class="zy-icon icon-gfwx"></i><span>官方微信</span>
                        <div class="codeImg"></div>
                    </a>
                </div>

                &lt;#&ndash;<div class="cell">
                    <a href="javascript:void(0)" class="tag">兄弟品牌</a>
                    <a href="http://www.ustalk.com" target="_blank"><i class="zy-icon icon-ustalk"></i></a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!}/help/downloadApp.vpage?refrerer=pc&count=2" target="_blank"><i class="zy-icon icon-parent"></i><span>家长通</span></a>
                </div>&ndash;&gt;
            </div>
        </div>
    </div>-->

<div class="footer-page-wrap">
    <div class="footer-box">
        <ul class="link-page">
            <li class="item">
                <div class="main-txt" href="javascript:;">关于一起</div>
                <a href="${ProductConfig.getMainSiteBaseUrl()!''}/help/primaryschool.vpage">一起小学</a>
                <a href="${ProductConfig.getMainSiteBaseUrl()!''}/help/middleschool.vpage">一起中学</a>
                <a href="${ProductConfig.getMainSiteBaseUrl()!''}/help/togetherlearn.vpage">一起学</a>
                <a href="${ProductConfig.getMainSiteBaseUrl()!''}/help/publicwelfare.vpage">一起公益</a>
            </li>
            <li class="item">
                <div class="main-txt" href="javascript:;">最新动态</div>
                <a href="${ProductConfig.getMainSiteBaseUrl()!''}/help/latestnews.vpage">新闻列表</a>
            </li>
            <li class="item">
                <div class="main-txt" href="https://app.mokahr.com/apply/17zuoye">加入我们</div>
                <a href="https://app.mokahr.com/apply/17zuoye" target="_blank">加入一起</a>
            </li>
            <li class="item">
                <div class="main-txt" href="javascript:;">登录/注册</div>
                <#if !(userinfo??)>
                <a href="javascript:;" class="JS-register-main">注册</a>
                </#if>
                <a href="javascript:;" class="active JS-login-main">登录</a>
            </li>
            <li class="item">
                <div class="main-txt" href="javascript:;">关注我们</div>
                <a class="weibo" href="https://www.weibo.com/yiqizuoye"><img src="<@app.link href='public/skin/default/v5/images/weibo.png'/>" alt=""></a>
                <a class="weixin" href="javascript:;"><img src="<@app.link href='public/skin/default/v5/images/weixin.png'/>" alt=""><span class="weixin_code"></span></a>
            </li>
            <li class="item nobar">
                <div class="main-txt" href="javascript:;">客服电话</div>
                <a class="pnumber" href="javascript:;">400-160-1717</a>
            </li>
        </ul>
        <div class="download-txt">客户端下载</div>
        <ul class="download-code">
            <li>
                <img src="<@app.link href='public/skin/default/v5/images/student-x.jpg'/>" alt="">
                <p>一起小学学生</p>
            </li>
            <li>
                <img src="<@app.link href='public/skin/default/v5/images/teacher-x.jpg'/>" alt="">
                <p>一起小学老师</p>
            </li>
            <li>
                <img src="<@app.link href='public/skin/default/v5/images/yqx.jpg'/>" alt="">
                <p>一起学</p>
            </li>
            <li>
                <img src="<@app.link href='public/skin/default/v5/images/student-z.jpg'/>" alt="">
                <p>一起中学学生</p>
            </li>
            <li>
                <img src="<@app.link href='public/skin/default/v5/images/teacher-z.jpg'/>" alt="">
                <p>一起中学老师</p>
            </li>
            <li class="no-marg">
                <img src="<@app.link href='public/skin/default/v5/images/yqx-wx.png'/>"  alt="">
                <p>一起学网校</p>
            </li>
        </ul>
        <div class="note-1">Copyright © 2011-2019 Shanghai Hexu Ltd. All Rights Reserved.</div>
        <div class="note-2">ICP证沪B2-20150026 <a href="http://www.miitbeian.gov.cn" style="color: inherit; text-decoration: none">沪ICP备13031855号-2</a> 京公网安备 11010502032354号</div>
    </div>
</div>

    <@sugar.capsule js=['loginv5'] />
    <@sugar.site_traffic_analyzer_end />
</body>
</html>
<!--<html><head><script type="text/javascript" src="/main.js"></script><style></style></head><body></body></html>-->
</#macro>