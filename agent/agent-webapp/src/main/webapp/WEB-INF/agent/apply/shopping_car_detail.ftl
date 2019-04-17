<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=page_num>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 物料申请</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div class="form-horizontal">

                <#if applyData?has_content && applyData.apply?has_content>
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label">订单编号</label>
                            <div class="controls">
                                <label class="control-label" style="text-align: left;margin-left:90px;width:150px">${applyData.apply.id}</label>
                            </div>
                        </div>
                    </fieldset>
                <#--购物车列表-->

                    <table class="table table-striped table-bordered bootstrap-datatable" style="width: 1000px;margin-left: 50px;">
                        <thead>
                        <tr>
                            <th class="sorting" style="width: 100px;">商品名称</th>
                            <th class="sorting" style="width: 100px;">商品单价</th>
                            <th class="sorting" style="width: 100px;">购买数量</th>
                            <th class="sorting" style="width: 100px;">金额</th>
                        </tr>
                        </thead>
                        <tbody>
                            <#if applyData.apply.orderProductList?has_content && applyData.apply.orderProductList??>
                                <#assign totalAmount = 0 />
                                <#assign cardTotalAmount = 0 />
                                <#list applyData.apply.orderProductList as orderProduct>
                                <tr class="odd">
                                    <td class="center  sorting_1">
                                        ${orderProduct.productName!}
                                    </td>
                                    <td class="center  sorting_1">
                                        ${(orderProduct.price?string("###0.00"))!}
                                    </td>
                                    <td class="center  sorting_1">
                                        ${orderProduct.productQuantity!}
                                    </td>
                                    <td class="center  sorting_1">
                                        <#assign cardTotalAmount = (orderProduct.price!0) * (orderProduct.productQuantity!0) />
                                        ${cardTotalAmount?string("###0.00")}
                                    </td>
                                </tr>
                                </#list>
                            </#if>
                        </tbody>
                    </table>

                        <#--订单详情-->
                    <fieldset>
                        <div class="control-group">
                            <label class="control-label">合计</label>
                            <div class="controls">
                                <label class="control-label" id="modifyType" style="text-align: left;margin-left:90px;width:150px">${(applyData.apply.orderAmount?string("###0.00"))!}</label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">支付方式</label>
                            <div class="controls">
                                <label class="control-label" id="schoolIdLabel" style="text-align: left;margin-left:90px;width:600px"><#if applyData.apply.paymentMode?? && applyData.apply.paymentMode == 1>物料费用</#if><#if applyData.apply.paymentMode?? && applyData.apply.paymentMode == 2>城市支持费用(${applyData.apply.costMonthStr!})</#if><#if applyData.apply.paymentMode?? && applyData.apply.paymentMode == 3>自付</#if></label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">上传凭证</label>
                            <div class="controls">
                                <div id="payment_voucher_photo" style="margin-left:50px">
                                    <#if applyData.apply?has_content && applyData.apply.paymentVoucher?has_content>
                                        <img src='${applyData.apply.paymentVoucher!''}?x-oss-process=image/resize,w_100,h_100/auto-orient,1'/>
                                        <a href="${applyData.apply.paymentVoucher!''}" target="_blank">查看大图</a>
                                    </#if>
                                </div>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">注意事项</label>
                            <div class="controls">
                                <label class="control-label" id="regionName" style="text-align: left;margin-left:90px;width:150px">${applyData.apply.orderNotes!}</label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">收货人</label>
                            <div class="controls">
                                <label class="control-label" id="schoolLevel" style="text-align: left;margin-left:90px;width:150px">${applyData.apply.consignee!}</label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">联系电话</label>
                            <div class="controls">
                                <label class="control-label" id="schoolLevel" style="text-align: left;margin-left:90px;width:150px">${applyData.apply.mobile!}</label>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label">收货地址</label>
                            <div class="controls">
                                <label class="control-label" id="schoolLevel" style="text-align: left;margin-left:90px;width:150px">${applyData.apply.province!""}${applyData.apply.city!""}${applyData.apply.county!""}${applyData.apply.address!}</label>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label">审核意见</label>
                            <div class="controls">
                                <label class="control-label" id="schoolLevel" style="text-align: left;margin-left:90px;width:150px"><#list applyData.processResultList as list>${list.processNotes!}</#list></label>
                            </div>
                        </div>

                        <div class="control-group">
                            <label class="control-label">处理意见区</label>
                        </div>

                        <div class="dataTables_wrapper" role="grid">
                            <table class="table table-striped table-bordered bootstrap-datatable" id="historyApplyTable" style="width: 1000px;margin-left: 50px;">
                                <thead>
                                <tr>
                                    <th class="sorting" style="width: 60px;">日期</th>
                                    <th class="sorting" style="width: 60px;">提交人</th>
                                    <th class="sorting" style="width: 60px;">审核日期</th>
                                    <th class="sorting" style="width: 60px;">审核人</th>
                                    <th class="sorting" style="width: 60px;">处理结果</th>
                                    <th class="sorting" style="width: 60px;">处理意见</th>
                                    <th class="sorting" style="width: 60px;">备注</th>
                                </tr>
                                </thead>
                                <tbody>
                                    <#if applyData.processResultList?has_content>
                                        <#list applyData.processResultList as processResult>
                                        <tr>
                                            <td><#if applyData.apply.orderTime?? >${applyData.apply.orderTime?string("yyyy-MM-dd")!}<#else>${applyData.apply.createDatetime?string("yyyy-MM-dd")!}</#if></td>
                                            <td>${applyData.apply.creatorName!}</td>
                                            <td><#if processResult.processDate??>${processResult.processDate?string("yyyy-MM-dd")!}</#if></td>
                                            <td>${processResult.accountName!}</td>
                                            <td>${processResult.result!}</td>
                                            <td>${processResult.processNotes!}</td>
                                            <td></td>
                                        </tr>
                                        </#list>
                                    </#if>
                                </tbody>
                            </table>
                        </div>
                        <#if agentInvoice?? >
                            <div class="control-group">
                                <label class="control-label">物流信息</label>
                            </div>

                            <div class="control-group">
                                <label class="control-label">发货日期</label>
                                <div class="controls">
                                    <label class="control-label" style="text-align: left;margin-left:90px;width:150px"><#if agentInvoice.deliveryDate??>${agentInvoice.deliveryDate?string("yyyy-MM-dd")!}</#if></label>
                                </div>
                            </div>

                            <div class="control-group">
                                <label class="control-label">物流状态</label>
                                <div class="controls">
                                    <label class="control-label" style="text-align: left;margin-left:90px;width:150px"><#if agentInvoice.logisticsStatus??>${agentInvoice.logisticsStatus.value!}</label>
                                </div>
                            </div>

                            <div class="control-group">
                                <label class="control-label">物流单号</label>
                                <div class="controls">
                                    <label class="control-label" style="text-align: left;margin-left:90px;width:150px">${agentInvoice.logisticsId!}</label>
                                </div>
                            </div>

                            <div class="control-group">
                                <label class="control-label">物流公司</label>
                                <div class="controls">
                                    <label class="control-label" style="text-align: left;margin-left:90px;width:150px">${agentInvoice.logisticsCompany!}</label>
                                </div>
                            </div>

                            <div class="control-group">
                                <label class="control-label">物流费用</label>
                                <div class="controls">
                                    <label class="control-label" style="text-align: left;margin-left:90px;width:150px">${agentInvoice.logisticsPrice!}元</label>
                                </div>
                            </div>
                        </#if>
                    </#if>


                    </fieldset>
                </#if>
            </div>
        </div>
    </div>
</div>

</@layout_default.page>
