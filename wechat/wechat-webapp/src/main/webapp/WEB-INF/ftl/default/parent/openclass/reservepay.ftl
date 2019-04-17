<#import "../layout.ftl" as ucenter>
<@ucenter.page title='公开课支付' pageJs="trusteePay">
    <@sugar.capsule css=['trustee', 'jbox'] />
    <form action="/parent/trustee/order.vpage" method="post" id="orderPayForm">
        <div class="pro-detail">
            <div class="pd-main">
                <div class="banner">
                    <img src="<@app.link href="public/images/parent/openclass/banner.png"/>" />
                </div>
                <div class="detail-dif03">
                    <div class="intro pdm newintro">
                        <h4>产品介绍：</h4>
                        <p>${(type.description)!'---'}</p>
                    </div>
                    <div class="period pdm newperiod">
                        <h4>类型：</h4>
                        <div class="right">
                            <ul>
                                <li class="active"><span>${(type.description)!'---'}</span></li>
                            </ul>
                            <p class="price-mark">原价<span class="txt-yellow">${(type.price)!'--'}</span>元</p>
                        </div>
                    </div>
                </div>
            </div>
            <div class="pro-footer">
                <div class="pf-l">需支付金额：<span class="price_box">${(type.discountPrice)!'--'}元</span></div>
                <div id="buy_but" class="pf-r" style="cursor: pointer;"><a href="javascript:void(0);">确认并支付</a></div>
                <input type="hidden" value="0" name="sid" id="array-student"/>
                <input type="hidden" value="${(type.name())!'mm'}" name="trusteeType" id="array-product"/>
            </div>
        </div>
    </form>
</@ucenter.page>