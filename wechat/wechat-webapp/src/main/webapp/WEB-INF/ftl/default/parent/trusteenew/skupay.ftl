<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='报名托管' pageJs="registtrustee">
    <@sugar.capsule css=['trusteetwo','jbox'] />
    <style>html, body{background-color: #fff;}</style>
    <div class="active-wrap">
        <form action="/parent/trustee/order.vpage" method="post" id="orderPayForm">
            <div class="pro-detail">
                <div class="pd-main">
                    <div class="banner">
                        <img src="<@app.link href="public/images/parent/trustee/detail-banner-02.jpg"/>" alt="" />
                    </div>
                    <div class="detail-dif01">
                        <div class="intro pdm">
                            <h4>产品介绍：</h4>
                            <p>托管班报名费用</p>
                            <p class="remark">托管班的开始和截止日期，以线下托管班实际报名为准。</p>
                        </div>
                        <div class="child pdm">
                            <h4>选择孩子：</h4>
                            <ul class="js-childList">
                                <#if students?has_content>
                                    <#list students as student>
                                        <li class="" data-cid="${student.id}">
                                            <img src="<@app.avatar href='${student.img!}'/>"/><p>${student.name!""}</p><i></i>
                                        </li>
                                    </#list>
                                </#if>
                            </ul>
                        </div>
                        <div class="period pdm">
                            <h4>种类：</h4>
                            <div class="right">
                                <ul class="js-sukList">
                                    <#if trusteeTypes?has_content>
                                        <#list trusteeTypes as type>
                                            <li typename="${type.name!""}" dataprice="${type.price!""}" datadiscountprice="${type.discountPrice!""}"><table cellpadding="0" cellspacing="0"><tr><td class="wtxt">${type.description!"--"}</td><td class="wprice">￥${type.discountPrice!"--"}</td></tr></table></li>
                                        </#list>
                                    </#if>
                                </ul>
                            </div>
                        </div>
                    </div>
                    <div class="empty"></div>
                </div>

                <!--有孩子未支付-->
                <div class="pro-footer js-canPayDiv">
                    <div class="pf-l"><del class="price js-oldPrice">原价0</del><span>需支付：<strong class="js-neededPrice">0元</strong></span></div>
                    <div class="pf-r"><a href="javascript:void(0)" class="js-confirmPayBtn">确认并支付</a></div>
                    <input type="hidden" value="" name="sid" id="array-student"/>
                    <input type="hidden" value="" name="trusteeType" id="array-product"/>
                </div>
                <!--所有孩子都已支付-->
                <div class="pro-footer pay-success js-cannotPayDiv" style="display: none;">
                    <a href="javascript:void(0)" class="pay-succsee">支付成功</a>
                </div>
            </div>
        </form>
    </div>
    <script>
        var trusteeTypesSize = "${trusteeTypes?size}";
        var shopid = "${shop.shopId!0}";
        ga('trusteeTracker.send', 'pageview');
    </script>
</@trusteeMain.page>