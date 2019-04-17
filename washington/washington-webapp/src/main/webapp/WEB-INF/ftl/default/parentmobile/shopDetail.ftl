<#import './layout.ftl' as layout>

<#--title 内容处理-->
<#assign topType = "topTitle">
<#if isOverTime!false>
    <#assign topTitle = "产品购买">
<#elseif result.success >
    <#if infos??>
        <#if infos?size gt 0>
            <#assign infoProduct = (infos[0].products)![]>
            <#list infoProduct?keys as key>
                <#list infoProduct[key] as product>
                    <#if product_index == 0>
                        <#assign topTitle = product.title!'产品购买'>
                    </#if>
                </#list>
            </#list>
        <#else>
            <#assign topTitle = '产品购买'>
        </#if>
    <#else>
        <#assign topTitle = '产品购买'>
    </#if>
</#if>

<@layout.page className='Ucenter' pageJs="second" title="课外乐园首页" specialCss="skin2" specialHead='
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, user-scalable=no" />
    <meta name="format-detection" content="telephone=no" />
    <meta name="format-detection" content="email=no" />
    <meta name="apple-mobile-web-app-status-bar-style" content="black" />
    <title>${topTitle!""}</title>
'>
    <script><#--改版的样式，不适用adapt-->
        if(window.navigator.userAgent.toLowerCase().indexOf("micromessenger") > -1){
            window.logApp = "wechat";
            window.logWhere = "wechat_logs";
        }
    </script>
    <#assign isUseNewTitle = true><#--使用新的UI2 title-->

    <#escape x as x?html>
        <em id="do-shopDetailPage" style="display:none;"></em>
        <#if isGraduate!false ><#--是否毕业判断-->
        <div class="null-box">
            <div class="no-account"></div>
            <div class="null-text">暂不支持小学毕业账号</div>
        </div>
        <#else>

            <#if isOverTime!false>
            <div class="parentApp-emptyProm">
                <div class="promIco"></div>
                <div class="promTxt">${message!""}</div>
            </div>
            <#elseif result.success >
                <#if infos??>
                    <#if infos?size gt 0>
                        <#assign  info = infos[0] >
                        <#include "./shopIcon.ftl">
                            <div class="purchase-box doTabBlock">
                                <#assign products = info.products![] productType = (productType!"")?trim >
                                <#assign isPicListenBook = productType?lower_case == "piclistenbook">
                                <#assign isStem101 = productType?lower_case == "stem101">
                                <#assign isWalkerElf = productType?lower_case == "walkerelf">
                                <#assign isFeeCourse = productType?lower_case == "feecourse">

                                <style> .grayLevel>div { background-color: #bbb; } </style>


                                <div class="purchase-banner">
                                    <#if bannerImage??>
                                        <img src="${bannerImage!''}" alt="">
                                    </#if>
                                </div>

                                <div class="pur-list">
                                    <#if !isPicListenBook!false>
                                        <dl class="pur-content">
                                            <dt></dt>
                                            <dd id="child_list_box" class="clearfix">
                                                <div class="pur-image active" data-sid="${info.uid!''}">
                                                    <div class="img"><img src="${imgUrl!''}"></div>
                                                    <div class="name">${info.name!""}</div>
                                                </div>
                                            </dd>
                                        </dl>
                                    </#if>

                                    <#if products?size == 0>
                                        <p style="text-align: center; font-size: 13px;margin:10px; "> 查找该产品出错 </p>
                                    <#else>
                                    <dl class="pur-content">
                                        <dt>周期</dt>
                                        <dd class="doCreateOrderPeriods doToggleActives">
                                            <div class="pur-cycle clearfix">
                                                    <#--趣味数学训练营-->
                                                    <#list products?keys as key>
                                                        <#assign shopDefaultPeriod = getShopDefaultPeriod(key)  shopDefaultPeriodIndex = 0>
                                                        <#list products[key] as product>
                                                            <#assign isBuyed = (info.buyIds!'')?index_of(product.productId) gt -1>
                                                            <a href="javascript:;" class="box <#if isStem101!false>half</#if>
                                                            <#if isBuyed&&!isStem101>
                                                            doBuyed grayLevel
                                                            <#else>
                                                            doCreateOrderPeriod doToggleActive
                                                            </#if>

                                                            <#if product_index == 0 >active</#if>"

                                                               data-price="${product.price!0}"
                                                               data-origin-price="${product.orignalPrice!0}"
                                                               data-product_id="${product.productId!''}"

                                                                <#if isWalkerElf>
                                                                   data-tip="${product.attributes!''}"
                                                                </#if>
                                                            >
                                                                <#if (specialProductType!'') == "AfentiSuit">
                                                                <#-- TODO 暂且这样判断吧 将来可以考虑弄成一个较为通用的模板 -->
                                                                    <#assign shopDefaultPeriodIndex = 6>
                                                                <#elseif product.period == shopDefaultPeriod && shopDefaultPeriodIndex == 0 >
                                                                    <#assign shopDefaultPeriodIndex = product_index>
                                                                </#if>

                                                                <#if productType?starts_with("Afenti") && [201, 202, 203, 204]?seq_index_of(product.appId!0) gt -1>
                                                                    <div>${product.name!''} ${product.period!0}天</div>
                                                                <#elseif isWalkerElf>
                                                                    <div>${product.productDesc!''}</div>
                                                                <#elseif isStem101>
                                                                        <input type="hidden" value="${product.productId!''}" class="doStem101LevelProduct" data-type = "${product.categoryName!''}" />
                                                                    <#if product.name?index_of("一段") gt -1>
                                                                        <div>${product.categoryName!''}</div>
                                                                    <#else>
                                                                        <div class="do_special_shop"></div>
                                                                    </#if>
                                                                <#elseif isFeeCourse>
                                                                    <div>${product.name!''}</div>
                                                                <#else>
                                                                    <div>${product.period!0}天</div>
                                                                </#if>
                                                            </a>
                                                            </#list>
                                                            <script>
                                                                window.shopDefaultPeriodIndex = ${shopDefaultPeriodIndex};
                                                            </script>
                                                        </#list>
                                                        </div>
                                                        <div class="do_tip pur-prom"></div>
                                                    </dd>

                                                    <#if isStem101 >
                                                        <script type="text/html" id="doLevelBoxTemp">
                                                            <div class="pur-cycle clearfix">
                                                                <%
                                                                var isBoughtIds = "${info.buyIds!''}".split(",");

                                                                productIds.forEach(function(id, index){
                                                                    var className = isBoughtIds.indexOf(id) === -1 ? "doLevel" : "grayLevel doBuyed";
                                                                %>


                                                                <a class="<%= className %> doToggleActive box" data-productId="<%= id %>" href="javascript:void(0);">
                                                                    <div><%= index + 1 %>段</div>
                                                                </a>
                                                                <%
                                                                });
                                                                %>
                                                            </div>
                                                    </script>
                                                    <dd class="doLevelBox doToggleActives"></dd>
                                                    </#if>
                                                </dl>
                                                </#if>
                                            </div>
                                            <div class="pur-list">
                                                <dl class="pur-content">
                                                    <dt class="title">详情</dt>
                                                    <dd class="imgbox">
                                                        <#if descImage??>
                                                            <img src="${descImage!''}" alt="">
                                                        </#if>
                                                    </dd>
                                                </dl>
                                            </div>
                                        </div>

                                        <div class="purfooter">
                                            <div class="inner clearfix">
                                                <div class="doCreateOrderForm">
                                                    <input type="hidden" name="sid" value="${sid}"/>
                                                    <input type="hidden" name="productId"/>
                                                    <a href="javascript:;" class="doCreateOrder  doTrack btn red"
                                                        <#if trackTypeObj[productType]?exists> data-track="interest|${trackTypeObj[productType]!""}_buy" </#if>
                                                        <#if promptMessage??> data-tip="${promptMessage!''}"</#if>
                                                        <#if applePay!false> data-is_use_applepay="true"</#if> >
                                                        <#if (info.status?string == "2")!false>
                                                            <#if isStem101||isWalkerElf!false>
                                                                立即开通
                                                            <#else>
                                                                立即续费
                                                            </#if>
                                                        <#else>
                                                            立即开通
                                                        </#if>
                                                    </a>
                                                    <#--http://project.17zuoye.net/redmine/issues/47604-->
                                                    <#if (info.launchUrl??&&info.launchUrl!=""&&info.status?string == "2")>
                                                        <a href="javascript:;" class="btn green JS-gotoGame"
                                                            <#if isPicListenBook!false>
                                                            data-sdk = "${sdk!""}"
                                                            data-sdk_book_id = "${sdk_book_id!""}"
                                                            <#else>
                                                           data-value="${productType!""},${info.launchUrl!""},${info.browser!""},${info.orientation!""}"
                                                            </#if>
                                                        >
                                                                进入学习
                                                        </a>
                                                    </#if>
                                                </div>


                                                <div class="money" style="padding: 0; line-height: normal;">
                                                    <span>金额：</span>
                                                    <span class="red"><em>￥</em><i id="js-price" class="doCreateOrderPrice"></i></span>
                                                    <del class="gray doCreateOrderOriginPrie"></del>
                                                </div>
                                            </div>
                                        </div>
                                        <script>
                                            window.productType = "${productType}";
                                        </script>

                                        <#else>
                                            <#assign info = "未找到产品信息" errorCode = 400>
                                            <#include "errorTemple/errorBlock.ftl">
                                        </#if>
                                    <#else>
                                        <#assign info = "未找到产品信息" errorCode = 400>
                                        <#include "errorTemple/errorBlock.ftl">
                                    </#if>
                                <#else>
                                    <#assign info = result.info errorCode = result.errorCode>
                                    <#include "errorTemple/errorBlock.ftl">
                                </#if>
                            </#if>
                        </#escape>
</@layout.page>
