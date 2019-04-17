<#import './layout.ftl' as layout>
<@layout.page className='Fairyland' pageJs="fairyland" title="趣味学习商城" specialCss="skin2" specialHead='
   	<meta charset="utf-8">
	<meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
	<meta name="format-detection" content="telephone=no" />
	<meta name="format-detection" content="email=no" />
	<meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>趣味学习商城</title>
'>
    <@sugar.capsule js=['voxLogs']/>
    <#escape x as x?html>
        <#include "constants.ftl">

        <#assign topType = "topTitle">
        <#assign topTitle = "趣味学习商城">
        <#assign isUseNewTitle = true><#--使用新的UI2 title-->
        <script><#--改版的样式，不适用adapt-->
            window.notUseAdapt=true;
        </script>
        <#include "./top.ftl" >

        <#if result.success >
            <#if isGraduate!false><#--是否毕业判断-->
                <div class="null-box">
                    <div class="no-account"></div>
                    <div class="null-text">暂不支持小学毕业账号</div>
                </div>
            <#else>
                <#--顶部广告位-->
                <div id="headerBannerCrm"></div>
                <div class="mall-head">趣味学习商品由第三方提供 请自愿购买</div>
                <div class="parentApp-mall-tab" style="display: none;">
                    <a href="javascript:;" data-tag="ALL" class="do_clickTypeName">全部</a>
                    <a href="javascript:;" data-tag="APPS" class="do_clickTypeName active">课外乐园</a>
                    <a href="javascript:;" data-tag="BOOKS" class="do_clickTypeName">图书绘本</a>
                    <a href="javascript:;" data-tag="ELEC_PRODUCT" class="do_clickTypeName">电子产品</a>
                </div>

                <div id="fairylandList">
                    <div style="text-align: center; line-height:300px; color: #bbb;">数据加载中...</div>
                </div>
            </#if>
        <#else>
            <p class="hide doAutoTrack" data-track="interest|fail"></p>
            <#assign info = result.info errorCode = result.errorCode>
            <#include "errorTemple/errorBlock.ftl">
        </#if>
        <#--template module-->
        <script type="text/html" id="T:趣味学习LIST">
            <%
            var products = data.products;
            %>
            <ul class="mall-list">
                <%for(var i = 0, len = products.length; i < len; i++){%>
                <li>
                    <div class="info">
                        <div class="pic"><img src="<@app.avatar href='<%=products[i].productIcon%>'/>" alt=""></div>
                        <div class="head">
                            <span><%=products[i].productName%></span>
                            <%if(products[i].usePlatformDesc != ""){%><span class="tag"><%=products[i].usePlatformDesc%></span><%}%>
                        </div>
                        <div class="text"><%=products[i].productDesc%></div>
                    </div>
                    <div class="box clearfix">
                        <a href="shoppinginfo.vpage?productType=<%=products[i].appKey%><%=SID%>&" onclick='YQ.voxLogs({database: "parent", module: "m_84NR1ObF", op: "detail_btn_click", s0: "<%=products[i].appKey%>"});' class="btn">查看详情</a>
                        <%if(isApp && products[i].launchUrl){%>
                        <a href="javascript:;" class="btn JS-gotoGame"
                           onclick='YQ.voxLogs({database: "parent", module: "m_84NR1ObF", op: "study_btn_click", s0: "<%=products[i].appKey%>"});'
                           data-value="<%=products[i].appKey%>,<%=products[i].launchUrl%>,<%=products[i].browser%>,<%=products[i].orientation%>" style="margin-right: .5rem;">
                            <%=(products[i].status === 2 ? '进入学习': '点击试用')%>
                        </a>
                        <%}%>
                    </div>
                    <%if(products[i].operationMessage != "" || products[i].usingUserNum > 0){%>
                    <div class="foot clearfix">
                        <div class="txt"><%=products[i].operationMessage%></div>
                        <div class="num"><%if(products[i].usingUserNum > 0){%><%=products[i].usingUserNum%>个家长说好<%}%></div>
                    </div>
                    <%}%>
                </li>
                <%}%>
            </ul>
        </script>
        <script type="text/html" id="T:productSizeNull">
            <div class="null-box">
                <div class="stay-tuned"></div>
                <div class="null-text">敬请期待</div>
            </div>
        </script>
    </#escape>
</@layout.page>

