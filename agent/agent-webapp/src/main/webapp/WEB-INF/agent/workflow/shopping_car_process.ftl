<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的账户' page_num=11>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th"></i> 物料申请审核</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <div class="form-horizontal">
                 <#if applyData?has_content && applyData.apply?has_content>

                <#--购物车列表-->

                    <table class="table table-striped table-bordered bootstrap-datatable">
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
                            <label class="control-label">可用余额</label>
                            <div class="controls">
                                <label class="control-label" id="usableCashAmount" style="text-align: left;margin-left:90px;width:150px">
                                   <#if usableCashAmount??>${(usableCashAmount?string("###0.00"))!}<#else>0.00</#if>
                                </label>
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
                                <textarea class="input-xlarge" id="processNote" rows="5" style="width: 880px;"></textarea>
                            </div>
                        </div>
                        <div class="form-actions" style="background:#fff;border:none">
                            <#if processList?has_content>
                                <#list processList as item>
                                    <button type="button" class="btn btn-primary" data-dismiss="modal" style="margin-left:30px" onclick="processFunction(${item.type}, ${applyData.apply.workflowId!})">${item.desc}</button>
                                </#list>
                            </#if>
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
                                            <td><#if processResult.processDate?has_content>${processResult.processDate?string("yyyy-MM-dd")}</#if></td>
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
                    </fieldset>
                </#if>
            </div>
        </div>
    </div>
</div>

<script type="text/javascript">

    function processFunction(processResult, workflowId){
        var processNote = $('#processNote').val();
        if(processNote == ""){
            alert("请填写处理意见！");
            return;
        }
        if(confirm("确认" + (processResult == 1? "通过":"驳回") + "该请求吗？")){
            $.post('process.vpage',{
                processResult:processResult,
                workflowId:workflowId,
                processNote:processNote
            },function(data){
                if(data.success){
                    location.href = "list.vpage"
                }else{
                    alert(data.info)
                }
            });
        }
    }

</script>

</@layout_default.page>
