<#import "../layout.ftl" as trusteeMain>
<@trusteeMain.page title='购买课程' pageJs="registtrustee">
<@sugar.capsule css=['openclass','jbox'] />
<style>html, body{background-color: #fff;}</style>
<div class="train-wrap">
    <form action="/parent/trustee/order.vpage" method="post" id="orderPayForm">
    <div class="train07-box">
        <div class="banner">
            <img src="<@app.link href="public/images/parent/openclass/banner-act05.png?v=1.0.1"/>" alt=""/>
        </div>
        <div class="main">
            <div class="intro pdm">
                <p>一起公开课-特色课程</p>
                <p class="remark">数学速算是一种不用算盘，不用手指算进行的数学运算方法。通过直接看算脑算，心算的训练方式，让儿童能快速轻松掌握计算方法。</p>
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
                <h4>课程选择：</h4>
                <div>

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
        <!--有孩子未支付-->
        <div class="sfooter js-canPayDiv">
            <div class="pf-l"><del class="price js-oldPrice">原价0</del><span>需支付：<strong class="js-neededPrice">0元</strong></span></div>
            <div class="pf-r"><a href="javascript:void(0)" class="js-confirmPayBtn">确认并支付</a></div>
            <input type="hidden" value="" name="sid" id="array-student"/>
            <input type="hidden" value="" name="trusteeType" id="array-product"/>
        </div>
        <!--所有孩子都已支付-->
        <div class="sfooter pay-success js-cannotPayDiv" style="display: none;">
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