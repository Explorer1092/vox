<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
title='教育机构'
pageJs=["searchlist"]
pageJsFile={"searchlist" : "public/script/mobile/mizar/searchlist"}
pageCssFile={"searchlist" : ["public/skin/mobile/mizar/css/searchlist"]}
>
<#--APP 低于 1.6版本需要一个标题条-->
<style>
    .parentApp-topBar{position:relative;height:2.55rem;z-index:50}
    .parentApp-topBar .topBox{position:fixed;top:0;left:0;padding:1rem 0 0;width:100%;background-color:#5a70d6;z-index:20}
    .parentApp-topBarAndroid .topBox{padding:.5rem 0 0}
    .parentApp-topBar .topTab>a,.parentApp-topBar .topTab>div{padding:.125rem 0;color:#a4b4ff;font-size:.75rem;line-height:1.4rem;box-flex:1;-moz-box-flex:1;-webkit-box-flex:1;cursor:pointer}
    .parentApp-topBar .topTab>a{display:block}
    .parentApp-topBar .topHead{padding:.125rem 0 .225rem;text-align:center;color:#fff;font-size:.65rem;line-height:1.2rem}

    .ah-banner{overflow:hidden;background-color:#fff;padding:.5rem .25rem 0 .25rem}
    .ah-banner .ahB{float:left;margin:0 1.5% .5rem 1.5%;width:97%;height:5rem;background-color:#e3f4d7;border-radius:.2rem;overflow:hidden}
    .ah-banner .ahB img{display:block;width:100%;height:100%}
    .ah-banner .ahB.ahB-s{width:47%}
</style>
<div class="parentApp-topBar doTop" id="parentAppTopHeader" style="height: 51px;display: none;">
    <div class="topBox" style="padding: 20px 0 0;">
        <div class="topHead" style="padding: 2.5px 0 4.5px;font-size: 13px;line-height: 24px;">教育机构</div>
    </div>
</div>
<#-- end -->

<div class="search-box" style="display: none;" id="searchModal">
    <input type="text" class="txt" placeholder="输入搜索内容" maxlength="20" id="searchInput">
    <a href="javascript:void(0);" class="sure-btn js-searchBtn">确定</a>
</div>
<#--运营活动-->
<div id="ActivityBannerBox"></div>
<div id="headerBar">
    <div class="ageList-pop" style="display: none; position: relative;" data-index="0">
        <div class="inner-back"></div>
        <div class="inner-box">
            <div class="agl-inner">
                <ul>
                    <li data-order="smart" class="js-orderBtn active">智能排序</li>
                    <li data-order="distance" class="js-orderBtn">离我最近</li>
                    <li data-order="rating" class="js-orderBtn">评价最好</li>
                </ul>
            </div>
        </div>
    </div>
    <div class="ageList-pop" style="display: none;position: relative;" data-index="1">
        <div class="inner-back"></div>
        <div class="inner-box">
            <div class="agl-content">

                <div class="agl-left">
                    <ul>
                        <li class="js-firstCategory">全部</li>
                        <#if categoryList?? && categoryList?size gt 0>
                            <#list categoryList as cl>
                                <li class="js-firstCategory">${cl.firstCategory!''}</li>
                            </#list>
                        </#if>
                    </ul>
                </div>
                <div class="agl-right">
                    <ul id="secondCategoryList">
                        <li class="js-secondCategory active" data-code="" id="allSecondCate">全部</li>
                        <#if categoryList?? && categoryList?size gt 0>
                            <#list categoryList as cl>
                                <#if cl.secondCategoryList?? &&cl.secondCategoryList?size gt 0>
                                    <#list cl.secondCategoryList as sc>
                                        <li class="js-secondCategory" data-sid="${sc.id!0}" data-parent="${sc.firstCategory!''}" style="display: none;">${sc.secondCategory!''}</li>
                                    </#list>
                                </#if>
                            </#list>
                        </#if>
                    </ul>
                </div>
            </div>
        </div>
    </div>
    <div class="ageList-pop" style="display: none;position: relative;" data-index="2">
        <div class="inner-back"></div>
        <div class="inner-box">
            <div class="agl-content">
                <div class="agl-left">
                    <ul>
                        <#if areaList?? && areaList?size gt 0>
                            <#list areaList as al>
                                <li class="js-areaItem <#if al_index == 0>active</#if>" data-code="${al.regionCode!''}">${al.regionName!''}</li>
                            </#list>
                        </#if>
                    </ul>
                </div>
                <div class="agl-right">
                    <ul id="tradeItemList">
                        <li class="js-tradeItem active" data-code="0" id="allTradeItem">全部</li>
                        <#if areaList?? && areaList?size gt 0>
                            <#list areaList as al>
                                <#if al.tradeAreaList?? && al.tradeAreaList?size gt 0>
                                    <#list al.tradeAreaList as ta>
                                        <li class="js-tradeItem" data-parent="${ta.regionCode!''}" style="display: none;">${ta.tradeArea!''}</li>
                                    </#list>
                                </#if>
                            </#list>
                        </#if>
                    </ul>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="agencyHome-box">
    <div class="ah-main">
        <div class="ah-tab">
            <ul class="js-headerBar">
                <li>
                    <a href="javascript:void(0);">智能排序</a>
                </li>
                <li>
                    <a href="javascript:void(0);">全部分类</a>
                </li>
                <li>
                    <a href="javascript:void(0);">附近商家</a>
                </li>
                <li class="icon-search">
                    <a href="javascript:void(0);">搜索</a>
                </li>
            </ul>
        </div>
        <div class="ad-top" id="shopListCon">

        </div>
    </div>
    <div class="ah-btn">
        <a href="javascript:void(0);" class="refresh-btn js-refreshBtn" style="display: none;">查看更多</a>
    </div>
    <div class="ah-footer">
        <div class="inner" id="locationPlace">
            <a href="javascript:void(0);" class="place-btn placeFail" style="width: 75%; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;">获取定位中...</a><!--定位成功时，去掉placeFail类-->
        </div>
    </div>
</div>
<script type="text/html" id="shopItem">
    <%for(var i = 0; i < rows.length; i++) {%>
    <dl>
        <dt>
            <img src="<%=(pressImage(rows[i].photo))%>" />
            <%if(rows[i].vip && rows[i].sameSchoolFlag){%>
            <a class="btn" href="javascript:void(0);">有同学去过</a>
            <%}%>
        </dt>
        <dd data-id="<%=rows[i].id%>" class="ad-item js-shopDetail">
            <div class="head"><%=rows[i].name%></div>
            <div class="starBg">
                <% var starNum = rows[i].ratingStar %>
                <% var resultNum = 5 - starNum %>
                <%if(starNum != 0) { %>
                <%for(var index=0;index < starNum;index++) {%>
                <a href="javascript:void(0);" class="cliBg"></a>
                <% } %>
                <%}%>
                <%if(resultNum != 0) { %>
                <%for(var index=0;index < resultNum;index++) {%>
                <a href="javascript:void(0);"></a>
                <% } %>
                <%}%>
                <% if (rows[i].ratingCount != 0) {%>
                <span><%= rows[i].ratingCount || 0 %>条</span>
                <% }else { %>
                <span>0 条</span>
                <%}%>
            </div>

            <div class="distance"><%= rows[i].tradeArea%>  <%=rows[i].fDistance%></div>
            <div class="tip"><%= rows[i].fSecondCate %></div>
        </dd>
        <% var giftFlag = rows[i].welcomeGift %>
        <% if(giftFlag && giftFlag.length != 0){ %>
        <dd class="ad-side">
            <div class="prompt"><span class="label label-orange">预约</span><%= giftFlag %></div>
        </dd>
        <% } %>

    </dl>
    <%}%>
</script>
<script type="text/html" id="T:ActivityBannerBox">
    <%var items = result.data%>
    <div class="ah-banner">
        <%for(var i = 0; i < items.length; i++){%>
        <%if(items.length%2 == 1 && i == 0){%>
        <!--大图-->
        <div class="ahB">
            <a href="<%=result.goLink%>?aid=<%=items[i].id%>">
                <img src="<%=result.imgDoMain%>gridfs/<%=(items[i].gif ? items[i].gif : items[i].img)%>" width="100%" style="display: block;"/>
            </a>
        </div>
        <%}else{%>
        <!--小图-->
        <div class="ahB ahB-s">
            <a href="<%=result.goLink%>?aid=<%=items[i].id%>">
                <img src="<%=result.imgDoMain%>gridfs/<%=items[i].img%>" width="100%" style="display: block;"/>
            </a>
        </div>
        <%}%>
        <%}%>
    </div>
</script>
</@layout.page>