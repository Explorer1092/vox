<#import './layout.ftl' as layout>
    <@layout.page className='Index' pageJs='index' title="家长端首页">
    <#escape x as x?html>
        <#include "constants.ftl">
        <div class="parentApp-homeFixedBanner" id="doTopGradualBar"></div>
        <div id="headerBannerCrm" class="flexslider"></div>
        <div id="headerDefaultBanner" style="display: none;"><img src="${buildStaticFilePath("banner.png", "img")}" alt="banner" width="100%"/></div>
        <style>
            #headerBannerCrm {border: none; margin: 0; }
            #headerBannerCrm .flex-viewport{  width: 640px; margin: 0 auto;}
            @media only screen and (max-width: 720px) {
                #headerBannerCrm .flex-viewport{ width: 720px; }
            }
            #headerBannerCrm .flex-control-nav{ bottom: -10px;}
        </style>
        <#--顶部广告位-->
        <#if isGraduate!false ><#--是否毕业判断-->
            <div class="parentApp-messageNull">暂时不支持小学毕业账号</div>
        <#else>

                <#assign loadhomeTeml = "" >
                <#if isBindClazz >
                <div class="parentApp-homeMsg">
                    <div class="parentApp-homeMsg">
                        <#assign loadhomeTeml >
                            <a href="javascript:;" data-type="openHomework" >
                                <div class="linkHd">英语</div>
                                <div class="linkFt">__english__</div>
                            </a>
                            <a  href="javascript:;" data-type="openHomework" >
                                <div class="linkHd">数学</div>
                                <div class="linkFt">__math__</div>
                            </a>
                        </#assign>

                        <div id="newHomeWork" class="msgLink">
                            <#noescape> ${loadhomeTeml?replace("__english__", "暂无")?replace("__math__", "暂无")} </#noescape>
                        </div>
                    </div>

                    <div class="msgNew"><a href="javascript:;" data-type="openHomework"><span>查看作业动态</span></a></div>
                </div>
                <#else>
                <div class="parentApp-workBox parentApp-workBox-none">还没绑定班级，快让孩子找老师绑定班级吧</div>
                </#if>

                <#--<#include "./activity/holiday/entryModule.ftl">-->


            <section class="parentApp-wrap">

                <#assign homeLinkList = [
                {
                "link" :  '/parentMobile/home/studyTrack.vpage?sid=${sid}&referrer=index',
                "name" : '学业报告',
                "className" : 'acm',
                "trackInfo" : "report|reportv2_open",
                "memo" : '查看学习概况、错题本、学校表现'
                },
                {
                "link" : '/parentMobile/rank/classes.vpage?sid=${sid}',
                "name" : '班级榜单',
                "className" : 'rak',
                "trackInfo" : "star|open",
                "memo" : '班级同学和家长最新动态榜'
                },

                {
                "link" : '/parentMobile/parentreward/getmissions.vpage?sid=${sid}&cp=1',
                "name" : '家长奖励',
                "className" : 'par',
                "trackInfo" : "parent|open",
                "memo" : '目标激励，积极成长'
                },
                {
                "link" : '/parentMobile/ucenter/shoppinginfolist.vpage?sid=${sid}',
                "name" : '趣味学习',
                "className" : 'aft',
                "trackInfo" : "interest|open",
                "memo" : '趣味应用，轻松学习，成绩冲刺',
                "gray": (hasClazz!false)
                }
                ]
                >

                <ul class="parentApp-homeLinkList">
                    <#list homeLinkList as linkInfo>
                        <#if linkInfo.gray!true>
                            <li>
                                <a href="${linkInfo.link}" class="do_not_add_client_params doTrack" data-track = "${linkInfo.trackInfo!""}">
                                    <span class="ico-1 ico-${linkInfo.className}"></span>
                                    <span class="ico-2"></span>
                                    <div class="hd">
                                    ${linkInfo.name}
                                    <#--只有【学生档案袋】显示NEW标签-->
                                        <#if  linkInfo.className == 'fb' && knewtonNewFlag!false>
                                            <span class="ico-fb-tag"></span>
                                        </#if>
                                    </div>
                                    <p class="ft">${linkInfo.memo}</p>
                                </a>
                            </li>
                        </#if>
                    </#list>
                </ul>

            </section>

            <script id="bannerTemp" type="text/html">
                <%
                if(advertisements.length === 0){
                advertisements = [ {} ];
                }

                var dots = '',
                defaultImgSrc = '';
                %>
                <div class="doSlide parentApp-topFocus-box">
                    <ul>
                        <%
                        advertisements.forEach(function(banner){
                        dots += '<em class="doDot"></em>';

                        var url = banner.resourceUrl || '';

                        if(
                    ${isNotSupportTrust?string} &&
                        /\/\/wechat.*\/parent\/activity\/globalmath.vpage/.test(url)
                        ){
                        url = "/parentMobile/ucenter/upgrade.vpage";
                        }

                        if(url != ''){
                        url = url + (url.indexOf('?') === -1 ? '?' : '&') + 'sid=${sid}';
                        }
                        %>
                        <li data-track="banner|<%= banner.id %>" class="doTrack">
                            <a href="<%= url %>">
                                <span class="layer-1"></span>
                                <span class="layer-2"></span>
                                <%if(banner.img){%>
                                <img src="<@app.avatar href="<%=banner.img%>"/>"/>
                                <%}else{%>
                                <img src="${buildStaticFilePath("banner.png", "img")}" alt="banner"/>
                                <%}%>
                            </a>
                        </li>
                        <% }); %>
                    </ul>

                    <div class="doSlideDots focusDot">
                        <%== dots %>
                    </div>
            </script>

            <script id="newHomeWorkTemp" type="text/html">
                <#noescape> ${loadhomeTeml?replace("__english__", "<%= englishState %>")?replace("__math__", "<%= mathState %>")} </#noescape>
            </script>
        </#if>
    </#escape>
</@layout.page>

