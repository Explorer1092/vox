<#import "../layout/webview.layout.ftl" as layout/>
<@layout.page
bodyClass="bg-f4"
title="预约试听"
pageJs=["mizar"]
pageJsFile={"mizar" : "public/script/mobile/mizar/main"}
pageCssFile={"mizar" : ["public/skin/mobile/mizar/css/skin"]}
>
<div id="ShopSelectDetail">
    <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;text-align: center;">数据加载中...</div>
</div>
<div id="PopupContent"></div>
<script type="text/html" id="T:预约成功">
    <div class="bespokePop-box">
        <div class="popInner">
            <div class="close js-closeTemplate" style="cursor: pointer"></div>
            <h2>您已成功预约</h2>
            <%if(reserveFlag == '0'){%>
            <div class="bp-content">
                <p>请等待商家和您联系。</p>
            </div>
            <div class="bp-footer">
                <a href="javascript:void(0);" class="complete-btn js-closeTemplate" data-logs="{op:'shop_goods_order_success',s1: '${(goods.id)!}'}">完成</a>
            </div>
            <%}else{%>
            <div class="bp-content">
                <p>您在7天内已经预约过，请等待商家和您联系。</p>
            </div>
            <div class="bp-footer">
                <a href="javascript:void(0);" class="complete-btn js-closeTemplate" data-logs="{op:'shop_goods_order_denied',s1: '${(goods.id)!}'}">完成</a>
            </div>
            <%}%>
        </div>
    </div>
</script>
<script type="text/html" id="T:预约试听">
    <div class="bespokePop-box">
        <div class="popInner">
            <div class="close js-closeTemplate" style="cursor: pointer;"></div>
            <h1>预约试听</h1>
            <%if(success){%>
            <%}else{%>
            <div class="bp-content">
                <div class="bp-info" style="padding: 40px 0;"><%=info%></div>
            </div>
            <%}%>
        </div>
    </div>
</script>

<script type="text/html" id="T:预约试听Page">
    <div class="agencyHome-box">
        <div class="vote-top">
            <img src="<@app.link href="public/skin/mobile/mizar/images/banner-01.png"/>" width="100%">
        </div>
        <div class="ah-main borderNon">
            <div class="aeg-top borderLine">
                <div class="titleBar">请选择一家试听商户</div>
                <div class="aeg-top bi-module ah-content">
                    <a href="javascript:;" class="icon-arrow JS-shopSelect">
                        <dl>
                            <dd class="mod-content">
                                <div class="head">
                                    <%=data.shopName%>
                                    <#--<span class="booked-info">距离最近</span>-->
                                </div>
                                <div class="booked-address">
                                    <%=data.shopAddress%>
                                </div>
                            </dd>
                        </dl>
                    </a>
                </div>
            </div>
            <div class="aeg-top borderLine">
                <div class="titleBar">留下您的联系方式以便商户尽快联系您</div>
                <div class="booked-number js-clickSelectParent active" data-type="autoMobile" style="line-height: 100%;">
                    <input type="tel" value="" placeholder="请输入您的联系方式" style="border: none; outline: none; width: 100%; font-size: 0.7rem; padding: 0.5rem;" maxlength="11"/>
                </div>
            </div>
            <input type="text" class="js-errorInfo" style="color: #f00; text-align: center; border: none; outline: none; background: none; width: 90%;" readonly="readonly" value=""/>
        </div>

        <div class="footer noFixed">
            <div class="inner">
                <a href="javascript:void(0);" class="bespoke-btn js-freeAppointmentSubmit" style="width: 95%;">立即预约</a>
            </div>
        </div>
    </div>
</script>

<script type="text/html" id="T:ShopSelectDetail">
    <%if(mappers.length > 0){%>
    <%for(var i = 0; i < mappers.length; i++){%>
    <%var item = mappers[i]%>
    <#--<a href="/mizar/reserveselect.vpage?shopId=<%=item.id%>">-->
    <a href="javascript:;" class="JS-clickShopName" data-shopid="<%=item.id%>">
        <div class="business-list <%=(shopId == item.id ? 'active' : '')%>">
            <div class="b-left">
                <div class="name"><%=item.name%></div>
                <div class="address"><%=item.address%></div>
            </div>
            <div class="b-right">
                <%
                var fDistance = item.distance.toFixed(2) + 'km';
                if(item.distance < 1 && item.distance > 0){
                fDistance = (item.distance * 1000).toFixed(0) + 'm';
                }
                %>
                <span class="distance"><%=fDistance%></span>
                <span class="select-icon"></span>
            </div>
        </div>
    </a>
    <%}%>
    <%}else{%>
    <div class="w-ag-center w-gray" style="font-size: 14px; padding: 50px 0;text-align: center;">暂无更多商户</div>
    <%}%>
</script>
<script type="text/javascript">
    var baseData = {
        shopId: "${shopId!}",
        goodsId : "${brandId!}"
    };
    var initMode = "ShopSelectDetail";
    var brandId = "${brandId!0}";
</script>
</@layout.page>