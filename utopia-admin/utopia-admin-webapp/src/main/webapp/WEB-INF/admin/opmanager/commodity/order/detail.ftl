<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js" xmlns="http://www.w3.org/1999/html"></script>
<div class="span9">
    <fieldset>
        <legend><font color="#00bfff">学习币商城</font>/订单详情</legend>
    </fieldset>

    <div id="main_container" class="span9">
        <div class="row-fluid">
            <div class="span12">
                <form class="well form-horizontal" style="background-color: #fff;">
                    <legend class="field-title">兑换单信息</legend>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName">订单ID：</label>
                            <div class="controls">
                                <input type="text" value="${order.orderId!''}" readonly="readonly">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">订单状态：</label>
                            <div class="controls">
                                <input type="text" value="${order.orderStatus!''}" readonly="readonly">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">订单生成时间：</label>
                            <div class="controls">
                                <input type="text" value="${order.createDate!''}" readonly="readonly">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">订单完成时间：</label>
                            <div class="controls">
                                <input type="text" value="${order.finishDate!''}" readonly="readonly">
                            </div>
                        </div>
                    </fieldset>
                    <legend class="field-title">商品信息</legend>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label" for="productName">商品ID：</label>
                            <div class="controls">
                                <input type="text" value="${order.commodityId!''}" readonly="readonly">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">商品名称：</label>
                            <div class="controls">
                                <input type="text" value="${order.commodityName!''}" readonly="readonly">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">商品分类：</label>
                            <div class="controls">
                                <input type="text" value="${order.commodityCategory!''}" readonly="readonly">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">采购价：</label>
                            <div class="controls">
                                <input type="text" value="${order.purchase!''}" readonly="readonly">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">配送费：</label>
                            <div class="controls">
                                <input type="text" value="${order.dispatchPrice!''}" readonly="readonly">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="productName">学习币数量：</label>
                            <div class="controls">
                                <input type="text" value="${order.coin!''}" readonly="readonly">
                            </div>
                        </div>
                    </fieldset>
                    <#if order.categoryLevel??>
                        <#if order.categoryLevel = 1>
                            <legend class="field-title">收件信息</legend>
                            <fieldset>
                                <div class="control-group">
                                    <label class="control-label" for="productName">收件人姓名：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.userName!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">联系方式：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.phone!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">地址：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.address!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">寄送状态：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.sendStatus!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">寄送方式：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.sendWay!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">运单号：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.logisticsCode!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">备注：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.remark!''}" readonly="readonly">
                                    </div>
                                </div>
                            </fieldset>
                        <#elseif order.categoryLevel = 2>
                            <legend class="field-title">优惠券适用信息</legend>
                            <fieldset>
                                <div class="control-group">
                                    <label class="control-label" for="productName">孩子姓名：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.userName!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">家长电话：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.phone!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">年龄：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.age!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">年级：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.clazzLevel!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">寄送状态：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.sendStatus!''}" style="color: red;" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">寄送方式：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.sendWay!'无需寄送'}" style="color: red;" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">信息备注：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.remark!''}" readonly="readonly">
                                    </div>
                                </div>
                            </fieldset>
                        <#else >
                            <legend class="field-title">优惠券适用信息</legend>
                            <fieldset>
                                <div class="control-group">
                                    <label class="control-label" for="productName">家长电话：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.phone!''}" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">寄送状态：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.sendStatus!''}" style="color: red;" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">寄送方式：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.sendWay!'无需寄送'}" style="color: red;" readonly="readonly">
                                    </div>
                                </div>
                                <div class="control-group">
                                    <label class="control-label" for="productName">信息备注：</label>
                                    <div class="controls">
                                        <input type="text" value="${order.remark!''}" readonly="readonly">
                                    </div>
                                </div>
                            </fieldset>
                        </#if>
                    </#if>
                </form>
            </div>
        </div>
    </div>

</div>
</@layout_default.page>