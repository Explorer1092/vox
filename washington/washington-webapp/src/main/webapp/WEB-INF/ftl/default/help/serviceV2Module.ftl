<#macro page title="一起教育科技_让学习成为美好体验" pageJs=[] pageJsFile={} bodyClass="bg-f8" currentMenu="" keywords="" description="" >
<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
    fastClickFlag=false
    title=title
    pageJs=pageJs
    pageJsFile=pageJsFile
    pageCssFile={"init" : ["public/skin/default/v5/css/pc-home","public/skin/default/v5/css/indexv5", "public/skin/default/v5/css/paging"]}
    bodyClass=bodyClass
    keywords=keywords
    description=description
>
<script>
    var imgCdnHeader = "<@app.link href='/'/>" + 'public/skin/default/v5/images/';
</script>
<#--<div class="zy-header zy-header-fix JS-headMenuBox">
    <#if currentMenu != "APP下载">
    <div class="zy-back"></div>
    </#if>
    <div class="zy-inner">
        <#if currentMenu == "APP下载">
            <div class="rightIn">
                <a href="${ProductConfig.getUcenterUrl()!''}"><i class="returnHome" style="vertical-align: middle;"></i><span style="font-size: 14px; display: inline-block; vertical-align: middle;">返回首页</span></a>
            </div>
        <#else>
            <div class="zy-nav">
                <a href="${ProductConfig.getUcenterUrl()!''}" class="logo"></a>
                <div class="navList">
                    <a href="${ProductConfig.getUcenterUrl()!''}/index.vpage" class="menu" title="首页">首页</a>
                    <a href="/help/concept.vpage" class="menu ${((currentMenu == '产品概念')!false)?string('active', '')}" title="产品概念">产品概念</a>
                    &lt;#&ndash;<a href="/help/uservoice.vpage" class="menu ${((currentMenu == '各界声音')!false)?string('active', '')}" title="各界声音">各界声音</a>
                    <a href="/help/news/index.vpage" class="menu ${((currentMenu == '新闻中心')!false)?string('active', '')}" title="新闻中心">新闻中心</a>&ndash;&gt;
                    <a href="/help/aboutus.vpage" class="menu ${((currentMenu == '关于我们')!false)?string('active', '')}" title="关于我们">关于我们</a>
                    <a href="/help/jobs.vpage" class="menu ${((currentMenu == '加入我们')!false)?string('active', '')}" title="加入我们">加入我们</a>
                    <a href="https://www.17xueba.com/index.vpage" class="menu" title="一起学网校">一起学网校</a>
                </div>
            </div>
            <div class="rightIn">
                &lt;#&ndash;<a href="https://www.17xueba.com" class="load" target="_blank">兄弟品牌<i class="yqx-icon"></i></a>&ndash;&gt;
                <a href="/help/downloadApp.vpage?refrerer=pc" class="load" target="_blank"><i class="phone-icon"></i>APP下载</a>
                <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=login" class="inBtn active">登录</a>
                <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=register" class="inBtn">注册</a>
            </div>
        </#if>
    </div>
</div>-->

<#if currentMenu != "APP下载">
    <div class="header-wrap">
        <div class="header">
            <div class="logo"><a href="/"></a></div>
            <ul class="nav-list">
                <a class="link " href="${ProductConfig.getUcenterUrl()!''}/index.vpage"><i>首页</i></a>
                <a class="link ${((currentMenu == '关于一起')!false)?string('active', '')}" href="/help/education.vpage" title="关于一起"><i>关于一起</i></a>
                <div class="link JS-productServer ${((currentMenu == '一起小学' || currentMenu == '一起中学' || currentMenu == '一起学' || currentMenu == '一起公益')!false)?string('active', '')}">
                    <i>产品服务</i>
                    <div class="product-wrap JS-productWrap">
                        <div class="product-inner ">
                            <span class="icon"></span>
                            <div class="product-box">
                                <a class="${((currentMenu == '一起小学')!false)?string('active', '')}" href="/help/primaryschool.vpage">一起小学</a>
                                <a class="${((currentMenu == '一起中学')!false)?string('active', '')}" href="/help/middleschool.vpage">一起中学</a>
                                <a class="${((currentMenu == '一起学')!false)?string('active', '')}" href="/help/togetherlearn.vpage">一起学</a>
                                <a class="${((currentMenu == '一起公益')!false)?string('active', '')}" href="/help/publicwelfare.vpage">一起公益</a>
                            </div>
                        </div>
                    </div>
                </div>
                <a class="link ${((currentMenu == '最新动态')!false)?string('active', '')}" href="/help/latestnews.vpage" title="最新动态"><i>最新动态</i></a>
                <a class="link" href="http://app.mokahr.com/apply/17zuoye/524" title="加入我们" target="_blank"><i>加入我们</i></a>
                <a class="link" href="https://www.17xueba.com/index.vpage" title="一起学网校" target="_blank"><i>一起学网校</i></a>
                <a class="link" href="/help/downloadApp.vpage?refrerer=pc"><i style="font-weight: bold">APP下载</i></a>
                <div class="loginBox">
                    <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=login">注册</a>/
                    <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=register">登录</a>
                </div>
            </ul>
        </div>
    </div>
<#else>
    <div class="zy-header zy-header-fix JS-headMenuBox">
        <div class="zy-inner">
            <div class="rightIn">
                <a href="${ProductConfig.getUcenterUrl()!''}"><i class="returnHome" style="vertical-align: middle;"></i><span style="font-size: 14px; display: inline-block; vertical-align: middle;">返回首页</span></a>
            </div>
        </div>
    </div>
</#if>

        <#nested />
    <#--<div class="zy-homeFooter JS-indexPageBox">
        <div class="innerBox">
            <div class="left">
                &lt;#&ndash;<div class="il-ppxd">
                    <span class="il-md">兄弟品牌：</span>
                    <a href="http://www.ustalk.com" target="_blank"><i class="zy-icon icon-ustalk"></i></a>
                    <a href="${(ProductConfig.getMainSiteBaseUrl())!}/help/downloadApp.vpage?refrerer=pc&count=2" target="_blank"><i class="zy-icon icon-parent"></i><span>家长通</span></a>
                    &lt;#&ndash;<a href="http://kuailexue.com" target="_blank"><i class="zy-icon icon-kuailexue"></i><span>快乐学</span></a>&ndash;&gt;
                </div>&ndash;&gt;
                ${(layout.getWebInfo("copyright"))!}
            </div>
            <div class="right">
                <div class="cell">
                    <a href="javascript:;" style="cursor: default;" class="tag">联系我们</a>
                    <a href="/help/kf/index.vpage" target="_blank"><i
                            class="zy-icon icon-bzfk"></i><span>帮助反馈</span></a>
                    <a href="javascript:;"><i class="zy-icon icon-tel"></i><span>${(layout.getWebInfo("tel"))!}</span></a>
                </div>
                <div class="cell">
                    <a href="javascript:;" style="cursor: default;" class="tag">关注我们</a>
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
                <div class="main-txt" >关于一起</div>
                <a href="/help/primaryschool.vpage">一起小学</a>
                <a href="/help/middleschool.vpage">一起中学</a>
                <a href="/help/togetherlearn.vpage">一起学</a>
                <a href="/help/publicwelfare.vpage">一起公益</a>
            </li>
            <li class="item">
                <div class="main-txt" href="javascript:;">最新动态</div>
                <a href="latestnews.vpage">新闻列表</a>
            </li>
            <li class="item">
                <div class="main-txt" href="https://app.mokahr.com/apply/17zuoye">加入我们</div>
                <a href="https://app.mokahr.com/apply/17zuoye" target="_blank">加入一起</a>
            </li>
            <li class="item">
                <div class="main-txt" href="javascript:;">登录/注册</div>
                <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=login">注册</a>
                <a href="${ProductConfig.getUcenterUrl()!''}/login.vpage?ref=register">登录</a>
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


    </@layout.page>
</#macro>
