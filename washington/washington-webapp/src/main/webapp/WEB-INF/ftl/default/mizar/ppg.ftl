<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-f4"
title='品牌馆'
pageJs=["mizar"]
pageJsFile={"mizar" : "public/script/mobile/mizar/ppg"}
pageCssFile={"mizar" : ["public/skin/mobile/mizar/css/skin"]}
>
<#include "function.ftl"/>
<div class="agencyHome-box">
    <div class="vote-top" id="headerBannerDefault">
        <img src="<@app.link href="public/skin/mobile/mizar/images/bandBanner.jpg"/>" width="100%"/>
    </div>
    <div class="ah-main" id="PPGDetailList">
        <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;text-align: center;">
            数据加载中...
        </div>
    </div>
</div>

<script type="text/html" id="T:定位提示">
    <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;text-align: center;">
        <%=info%>
        <div style="padding-top: 40px;">
            <a href="javascript:void(0);" class="w-orderedBtn JS-GetTargeting" style="width: auto; padding: 10px 20px;">点击重新加载</a>
        </div>
    </div>
</script>
<script type="text/html" id="T:PPGDetailList">
<%for(var a = 0; a < listArray.length; a++){%>
    <%var aKey = listArray[a].key%>
    <div class="aeg-top borderLine">
        <div class="titleBar"><%=listArray[a].title%></div>
        <%if(dataMap[aKey].length > 0){%>
            <%for(var i = 0; i < dataMap[aKey].length; i++){%>
            <%var item = dataMap[aKey][i]%>
            <div class="JS-item" style="display: <%=(i > 4 ? 'none': '')%>;">
                <dl class="borLine-list JS-GoToPage" data-key="<%=aKey%>" data-url="/mizar/branddetail.vpage?brandId=<%=item.brandId%>" style="cursor: pointer">
                    <dt>
                        <img src="<%=item.brandLog%>">
                    </dt>
                    <dd>
                        <div class="head"><%=item.brandName%></div>
                        <div class="ordered-btn">
                            <span href="javascript:void(0);" class="w-orderedBtn">点击预约</span>
                        </div>
                        <%if(item.brandPoints){%>
                        <div class="tip">
                            <%for(var c = 0; c < item.brandPoints.length; c++){%>
                                <p>• <%=(item.brandPoints[c])%></p>
                            <%}%>
                        </div>
                        <%}%>
                    </dd>
                </dl>
                <div class="aeg-top bi-module ah-content JS-GoToPage" data-key="<%=aKey%>" data-url="/mizar/shopdetail.vpage?shopId=<%=item.shopId%>" style="cursor: pointer">
                    <dl>
                        <dd class="mod-content">
                            <div class="head"  style="width: 11rem;">
                                <%=item.shopName%>
                                <#--<span class="tag-booked"></span><span class="tag-group"></span>-->
                            </div>
                            <div class="starBg">

                                <a href="javascript:void(0);" class="<%=(item.ratingStar >= 1 ? 'cliBg': '')%>"></a>
                                <a href="javascript:void(0);" class="<%=(item.ratingStar >= 2 ? 'cliBg': '')%>"></a>
                                <a href="javascript:void(0);" class="<%=(item.ratingStar >= 3 ? 'cliBg': '')%>"></a>
                                <a href="javascript:void(0);" class="<%=(item.ratingStar >= 4 ? 'cliBg': '')%>"></a>
                                <a href="javascript:void(0);" class="<%=(item.ratingStar >= 5 ? 'cliBg': '')%>"></a>
                                <% if (item.ratingCount != "" && item.ratingCount != null) {%>
                                    <span class="num"><%=item.ratingCount%>条</span>
                                <% } %>
                                <%
                                var fDistance = item.distance.toFixed(2) + 'km';
                                if(item.distance < 1 && item.distance > 0){
                                    fDistance = (item.distance * 1000).toFixed(0) + 'm';
                                }
                                %>
                                <span class="distance"><%=fDistance%></span>
                            </div>
                        </dd>
                    </dl>
                    <span href="javascript:void(0);" class="icon-arrowRight">预约有礼</span>
                </div>
            </div>
            <%}%>
            <#--更多-->
            <%if(dataMap[aKey].length > 5){%>
                <div class="viewMore-btn JS-moreDetail" data-key="<%=aKey%>" style="cursor: pointer;">
                    <span class="view-btn">查看更多</span>
                </div>
            <%}%>
        <%}else{%>
            <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;text-align: center;">暂无<%=listArray[a].title%>机构</div>
        <%}%>
    </div>
<%}%>
</script>
<script type="text/javascript">
    var initMode = "PPGDetail";
</script>
</@layout.page>