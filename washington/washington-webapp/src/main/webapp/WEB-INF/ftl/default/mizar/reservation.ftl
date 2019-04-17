<div class="footer">
    <div class="inner">
        <a href="javascript:void(0);" class="bespoke-btn js-reservationBtn" data-logs="{op:'shop_goods_order_click',s1: '${(goods.id)!}'}">立即预约</a>
        <#assign reservationPhone = phone![], pubilcWelcomeGift = shopWelcomeGift!/>
        <#if (shop.phone)??>
            <#assign reservationPhone = (shop.phone)![]/>
        </#if>
        <#if (goods.welcomeGift)?has_content>
            <#assign pubilcWelcomeGift = goods.welcomeGift!/>
        </#if>
        <#list reservationPhone as p>
            <#if p_index == 0>
                <a href="tel:${(p)!}" <#if !(p)?has_content>style="visibility: hidden;" </#if> data-logs="{op:'tel_dialed',s1: '${(goods.id)!}'}"><div class="phone"><i class="icon-phone"></i>电话</div></a>
            </#if>
        </#list>
        <#if showRemarkAndLike?? && showRemarkAndLike>
            <div class="phone" id="remarkBtn"><i class="icon-phone phone-1"></i>写评价</div>
            <#if hasLiked?? && hasLiked>
                <div class="phone">
                    <i class="icon-phone phone-2 praiseRed"></i>
                    <div class="praise-num">
                        <#if shop.likeCount?? && shop.likeCount gt 0>
                        ${(shop.likeCount)!}
                    </#if>
                    </div>
                    已点赞
                </div>
            <#else>
                <div class="phone" id="supportBtn">
                    <i class="icon-phone phone-2"></i>
                    <div class="praise-num" id="likeCount">
                        <#if (shop.likeCount)?? && shop.likeCount gt 0>
                        ${(shop.likeCount)!}
                    </#if>
                    </div>
                    <span>点赞</span>
                </div>
            </#if>
        <#else>
            <div class="mid">${pubilcWelcomeGift!'免费预约，了解课程详情'}</div>
        </#if>


        <#--<div class="mid">${pubilcWelcomeGift!'免费预约，了解课程详情'}</div>-->
    </div>
</div>
<div id="PopupContent"></div>
<script type="text/html" id="T:预约试听">
    <div class="bespokePop-box">
        <div class="popInner">
            <div class="close js-closeTemplate" style="cursor: pointer;"></div>
            <h1>预约试听</h1>
            <%if(success){%>
            <div class="bp-content">
                <div class="bp-title">
                    ${(goods.goodsName)!'免费预约，了解课程详情'}
                </div>
                <ul>
                    <%if(mobileMap.parentId){%>
                    <li class="js-clickSelectParent"
                        data-parentid="<%=mobileMap.parentId%>"
                        data-showname="<%=mobileMap.showName%>"
                        data-callname="<%=mobileMap.callName%>"
                        data-studentname="<%=mobileMap.studentName%>"
                        data-mobile="<%=mobileMap.mobile%>">
                        <span class="name"><%=mobileMap.showName%></span><span class="phone"><%=mobileMap.mobile%></span>
                    </li>
                    <%}%>
                    <li class="js-clickSelectParent" data-type="autoMobile">
                        <div class="bp-footer" style="width: 100%; float: none; padding: 0; text-align: right;">
                            <input type="tel" class="txt" placeholder="其他手机号，方便商家联系您" style="color: #333; text-align: center; width: 80%;" maxlength="11"/>
                        </div>
                    </li>
                </ul>
            </div>
            <div class="bp-footer">
                <input type="text" class="js-errorInfo" style="color: #f00; text-align: center; border: none; outline: none; width: 90%;" readonly="readonly" value=""/>
                <a href="javascript:void(0);" class="bespoke-btn js-freeAppointmentSubmit">免费预约</a>
            </div>
            <%}else{%>
                <div class="bp-content">
                    <div class="bp-info" style="padding: 40px 0;"><%=info%></div>
                </div>
            <%}%>
        </div>
    </div>
</script>

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
<script type="text/html" id="T:商家入驻">
    <div class="bespokePop-box">
        <div class="popInner">
            <div class="close js-closeTemplate" style="cursor: pointer;"></div>
            <h1>商家入驻</h1>
            <div class="bp-content" style="text-align: left;">
                <div class="bp-info" style="padding: 20px 20px;"><%=info%></div>
            </div>
            <div class="bp-footer">
                <a href="javascript:void(0);" class="bespoke-btn js-closeTemplate">知道了</a>
            </div>
        </div>
    </div>
</script>

<script id="generalizeBox_tem" type="text/html">
    <div class="titleBar">
        <div class="right titleGray">推广</div>
        其他小伙伴还看了
    </div>
    <div class="aeg-top">
        <%for(var i = 0; i < content.length; i++){%>
        <dl onclick="window.location.href='/mizar/shopdetail.vpage?shopId=<%=content[i].id%>'">
            <dt><img src="<%=content[i].photo%>" alt=""></dt>
            <dd>
                <div class="head"><%=content[i].name%></div>
                <div class="starBg">
                    <%for(var j = 0; j < 5; j++) { %>
                    <a href="javascript:void(0);" <%if(content[i].ratingStar > j){%> class="cliBg" <%}%> ></a>
                    <% } %>
                    <span><%=(content[i].ratingCount) || 0%>条</span>
                </div>
                <div class="distance"><%=content[i].tradeArea%> <%=(content[i].distance.toFixed(2)) || 0%>km</div>
                <div class="tip"><span><%=(content[i].secondCategory.join(',')) || ''%></span></div>
            </dd>
        </dl>
        <%}%>
    </div>
</script>

<script type="text/javascript">
    var baseData = {
        shopId: "${(goods.shopId)!}${(shop.shopId)!}",
        goodsId : "${(goods.id)!}",
        shopName: "${(shop.name)!}",
        activityId: "${activityId!}",
        isVip: ${((shop.isVip)!true)?string}
    };
</script>