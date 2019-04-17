<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='合同详情' page_num=15>
<style type="text/css">
    .contract_item{padding-bottom: 50px;}
    .control-group{display: inline-block;margin-left: 10px;}
    input,select{width: 100%;}
    .upload_preview{padding-left: 100px;}
    .upload_preview img{width: 80px;height: 120px;}
    #fileImage{display: none;}
    .upload_preview .upload_append_list{display: inline-block;margin-right: 5px;text-align: center;}
    .form_title{padding-left: 100px;margin: 10px 0 15px;font-size: 16px;color: #000;}
    .payback_info{margin-bottom: 10px;}
    .payback_info span{margin-right: 15px;}
</style>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header btn-group" style="padding: 5px;height: auto;">
            <button type="button" class="btn btn-primary tab_btn" style="padding: 8px 30px;">合同信息</button>
            <button type="button" class="btn tab_btn" style="padding: 8px 30px;">回款信息</button>
            <button type="button" class="btn tab_btn" style="padding: 8px 30px;">操作日志</button>
        </div>
        <div class="contract_item contract_info_item">

        </div>
        <div class="contract_item payback_info_item hidden">
        </div>
        <div class="contract_item opetation_info_item hidden">
            操作日志
        </div>
    </div>
</div>
<#--查看大图-->
<div id="outerdiv" style="position:fixed;top:0;left:0;background:rgba(0,0,0,0.7);z-index:2;width:100%;height:100%;display: none;">
    <img id="bigimg" style="position: absolute;left: 50%;top: 50%;transform: translate(-50%,-50%);" src="" />
</div>
<#--合同信息模板-->
<script type="text/html" id="contract_info">
    <%var res1 = data.serviceRangeList%>
    <%var res = data.agentExamContract%>
    <form class="form-horizontal">
        <p class="form_title">基本信息</p>
        <div class="control-group" style="display: block;">
            <label class="control-label" for="focusedInput">合同编号</label>
            <div class="controls">
                <span style="margin-right: 20px;"><%=res.contractNumber%></span>
                <input type="button" class="btn btn-primary" id="editContract" value="编辑">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">学校ID</label>
            <div class="controls">
                <input name="schoolId" id="school_name" class="js-postData input-xlarge focused" type="text" value="<%=res.schoolId%>" data-einfo="请填写学校ID" readonly>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">学校名称</label>
            <div class="controls">
                <input id="real_name" name="schoolName" class="js-postData input-xlarge focused js-needed" type="text" value="<%=res.schoolName%>" data-einfo="请选择学校名称" readonly>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">等级：</label>
            <div class="controls" style="line-height: 26px;">
                <span style="display: inline-block;margin-right: 20px;" id="school_level"><%=res.schoolPopularityType%></span>专员：<span style="display: inline-block;" id="school_userName"><%=res.userName%></span>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">合同类型</label>
            <div class="controls">
                <select id="role_name" class="form-control js-needed" data-einfo="请选择合同类型" name="contractType" disabled style="width: 280px;">
                    <option value="">请选择</option>
                    <option value="PAY_EXAM" <%if(res.contractType == 'PAY_EXAM'){%>selected<%}%>>付费</option>
                    <option value="LARGE_EXAM" <%if(res.contractType == 'LARGE_EXAM'){%>selected<%}%>>大考</option>
                </select>
            </div>
        </div>

        <div class="control-group">
            <label class="control-label" for="focusedInput">金额</label>
            <div class="controls">
                <input id="phone_no" name="contractAmount" class="js-postData input-xlarge focused" type="text" maxlength="11"
                       value="<%=res.contractAmount%>"  data-einfo="请填写金额" readonly>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">硬件成本</label>
            <div class="controls">
                <input name="hardwareCost" class="js-postData input-xlarge focused canEdit" value="<%=res.hardwareCost%>" type="text" disabled>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">第三方产品成本</label>
            <div class="controls">
                <input name="thirdPartyProductCost" class="js-postData input-xlarge focused canEdit" value="<%=res.thirdPartyProductCost%>" type="text" disabled>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">服务开始日期</label>
            <div class="controls">
                <input id="contract_start_date" name="beginDate" class="js-postData input-xlarge focused js-needed canEdit" type="text" data-needInfo="合同开始日期不能为空"
                       value="<%=res.beginDate%>"  data-einfo="请填写服务开始日期" disabled>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">服务结束日期</label>
            <div class="controls">
                <input id="contract_end_date" name="endDate" class="js-postData input-xlarge focused canEdit" type="text"
                       value="<%=res.endDate%>"  data-einfo="请填写服务结束日期" disabled>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">签约日期</label>
            <div class="controls">
                <input id="contract_apply_date" name="contractDate" class="js-postData input-xlarge focused canEdit" type="text"
                       value="<%=res.contractDate%>" data-einfo="请填写签约日期" disabled>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">机器数量</label>
            <div class="controls">
                <input name="machinesNum" value="<%=res.machinesNum%>" class="js-postData input-xlarge focused canEdit" type="text" disabled>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">机器型号</label>
            <div class="controls">
                <input name="machinesType" value="<%=res.machinesType%>" class="js-postData input-xlarge focused canEdit" type="text" disabled>
            </div>
        </div>
        <div class="control-group serviceRange">
            <label class="control-label">服务范围</label>
            <div class="controls" style="height:18px;line-height:18px">
                <% for(var i = 0; i< res1.length;i++){ %>
                <% var list = res1[i] %>
                <input type="checkbox"  name ="serviceRange" class="serviceRangeInp canEdit" value="<%= list.sr_key %>" <%if(list.sr_show){%> checked <%}%> disabled><%= list.sr_value %> &nbsp;&nbsp;&nbsp;
                <% } %>
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">备注</label>
            <div class="controls">
                <textarea name="remark" class="canEdit" style="width: 725px;height: 30px;padding: 3px;resize: none;" disabled><%=res.remark%></textarea>
            </div>
        </div>
        <p class="form_title">分成设置 <a class="contractorAddBtn btn btn-primary canEditBtn hidden">添加签约人</a></p>
        <div class="contractorWrap">
            <% for(i in res.splitSettingList){ %>
            <% var list = res.splitSettingList[i] %>
            <div class="contractor_item" style="width: 100%;">
                <div class="control-group">
                    <label class="control-label" for="focusedInput">
                        <%if(list.contractorFlag == 1){%>主签约人<%}%>
                        <%if(list.contractorFlag == 0){%>合作签约人<%}%></label>
                    <div class="controls">
                        <input name="" class="js-postData input-xlarge focused contractorInput canEdit" value="<%= list.contractorName %>" type="text" disabled>
                        <input type="hidden" class="contractorId" value="<%= list.contractorId %>">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">分成比例</label>
                    <div class="controls">
                        <input name="" class="js-postData input-xlarge focused proportionInput canEdit" value="<%= list.splitProportion %>" type="text" disabled>%
                    </div>
                </div>
                <%if(list.contractorFlag == 0){%><a class="btn btn-warning contractorDelBtn canEditBtn hidden">删除</a><%}%>
            </div>
            <% } %>
        </div>
        <p class="form_title">合同文件 <input type="button" class="uploadImgBtn btn btn-primary canEditBtn hidden" value="上传照片"></p>
        <form id="uploadForm">
            <input id="fileImage" type="file" size="30" name="fileselect[]" multiple="" accept="image/jpeg,image/png,image/gif">
            <div id="preview" class="upload_preview">
                <% for(i in res.imageUrlList){ %>
                <div class="upload_append_list"><p>
                    <img src="<%= res.imageUrlList[i] %>"  class="upload_image" alt=""><br>
                    <a href="javascript:" class="normal_delete canEditBtn hidden" data-src="<%= res.imageUrlList[i] %>" title="删除">删除</a>
                </p></div>
                <% } %>
            </div>
            <div id="uploadInf" class="upload_inf"></div>
            <input type="button" id="fileSubmit" class="upload_submit_btn btn btn-primary hidden" value="确认上传图片">
        </form>
        <div class="form-actions hidden" style="padding: 5px 160px;margin: 0;">
            <button type="button" class="btn btn-primary submitBtn" data-info="0">取消</button>
            <button type="button" class="btn btn-primary submitBtn" data-info="save_data.vpage">保存</button>
        </div>
    </form>
</script>

<#--新建回款记录模态框-->
<div id="addpayback_dialog" class="modal fade hide">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">新建回款记录</h4>
            </div>
            <div class="modal-body">
                <div id="addpaybackDialog" class="form-horizontal">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="addpaybackSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>
<#--新建回款记录模板-->
<script id="addpaybackDialogTemp" type="text/x-handlebars-template">
    <div class="control-group">
        <label class="control-label" for="editDepName">回款日期：</label>
        <input type="text" value="" style="width: auto;" id="paybackDate">
        <input type="hidden" value="{{contractId}}" id="contractId">
        <input type="hidden" value="{{waitPaybackAmount}}" id="waitPaybackAmount">
    </div>
    <div class="control-group">
        <label class="control-label" for="focusedInput">回款金额：</label>
        <input id="paybackAmount" type="text"  value="" name="paybackAmount" style="width: auto;">
    </div>
</script>

<#--回款信息模板-->
<script type="text/html" id="payback_info">
    <p class="form_title">回款情况</p>
    <div class="payback_info" style="padding-left: 100px;">
        <span>应回款金额：<%= res.contractAmount%></span>
        <span>已回款金额：<%= res.havaPaybackAmount%></span>
        <span>待回款金额：<%= res.waitPaybackAmount%></span>
        <span>回款状态：<%= res.waitPaybackAmount==0?'已完成':'未完成'%></span>
    </div>
    <div class="payback_list clearfix" style="padding:0 100px;">
        <lable>回款明细：</lable>
        <input type="hidden" id="paybackContractId" value="">
        <input type="button" class="btn btn-primary addPayBackBtn" data-amount="<%= res.waitPaybackAmount%>" data-cid="<%= res.contractId%>" style="float: right" value="新建回款记录">
        <div class="areaDetailContent">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" rowspan="1">期数</th>
                        <th class="sorting" colspan="1">回款编号</th>
                        <th class="sorting" colspan="1">回款日期</th>
                        <th class="sorting" colspan="1">操作人</th>
                        <th class="sorting" colspan="1">回款金额</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <%if(res.contractPaybackList.length > 0){%>
                    <%for(var i = 0; i< res.contractPaybackList.length; i++){%>
                    <%var resDetail = res.contractPaybackList[i]%>
                    <tr>
                        <td class="center sorting_1">第<%=resDetail.period%>期</td>
                        <td class="center sorting_1"><%=resDetail.paybackNumber%></td>
                        <td class="center sorting_1"><%=resDetail.paybackDate%> </td>
                        <td class="center sorting_1"><%=resDetail.operatorName%></td>
                        <td class="center sorting_1"><%=resDetail.paybackAmount%></td>
                    </tr>
                    <%}%>
                    <%}else{%>
                    <tr>
                        <td colspan="5">暂无记录</td>
                    </tr>
                    <%}%>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>

<#--操作日志模板-->
<script type="text/html" id="opetation_info">
    <p class="form_title">操作日志</p>
    <div class="payback_list clearfix" style="padding:0 100px;">
        <div class="areaDetailContent">
            <div class="dataTables_wrapper">
                <table class="table table-bordered table-striped bootstrap-datatable datatable dataTable">
                    <thead>
                    <tr>
                        <th class="sorting" width="120px;">时间</th>
                        <th class="sorting" width="50px;">操作人</th>
                        <th class="sorting" width="50px;">类别</th>
                        <th class="sorting" width="300px;">修改内容</th>
                    </tr>
                    </thead>
                    <tbody role="alert" aria-live="polite" aria-relevant="all">
                    <%if(res.contractOperationRecordList.length > 0){%>
                    <%for(var i = 0; i< res.contractOperationRecordList.length; i++){%>
                    <%var resDetail = res.contractOperationRecordList[i]%>
                    <tr>
                        <td class="center sorting_1"><%=resDetail.createTime%></td>
                        <td class="center sorting_1"><%=resDetail.operatorName%></td>
                        <td class="center sorting_1"><%=resDetail.operationType%></td>
                        <td class="center sorting_1"><%=resDetail.operationContent%></td>
                    </tr>
                    <%}%>
                    <%}else{%>
                    <tr>
                        <td colspan="4">暂无记录</td>
                    </tr>
                    <%}%>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</script>

<script src="${requestContext.webAppContextPath}/public/js/uploadImg/uploadFile.js"></script>
<#--添加主签约人模板-->
<script id="contractorMainAdd" type="text/html">
    <div class="contractor_item" style="width: 100%;">
        <div class="control-group">
            <label class="control-label" for="focusedInput">主签约人</label>
            <div class="controls">
                <input name="" class="js-postData input-xlarge focused contractorInput" type="text">
                <input type="hidden" class="contractorId">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">分成比例</label>
            <div class="controls">
                <input name="" class="js-postData input-xlarge focused proportionInput" type="text">%
            </div>
        </div>
    </div>
</script>
<#--添加合作签约人模板-->
<script id="contractorAdd" type="text/html">
    <div class="contractor_item" style="width: 100%;">
        <div class="control-group">
            <label class="control-label" for="focusedInput">合作签约人</label>
            <div class="controls">
                <input name="" class="js-postData input-xlarge focused contractorInput" type="text">
                <input type="hidden" class="contractorId">
            </div>
        </div>
        <div class="control-group">
            <label class="control-label" for="focusedInput">分成比例</label>
            <div class="controls">
                <input name="" class="js-postData input-xlarge focused proportionInput" type="text">%
            </div>
        </div>
        <a class="btn btn-warning contractorDelBtn">删除</a>
    </div>
</script>
<script type="text/javascript">
$(function(){
    var id = getUrlParam('id');
    var imageUrlList = [];
    $.get("school_contract.vpage?id="+id,function (res){
        if(res){
            if(res.success){
                res.data.agentExamContract.beginDate = new Date(res.data.agentExamContract.beginDate).Format('yyyy-MM-dd');
                res.data.agentExamContract.endDate = new Date(res.data.agentExamContract.endDate).Format('yyyy-MM-dd');
                res.data.agentExamContract.contractDate = new Date(res.data.agentExamContract.contractDate).Format('yyyy-MM-dd');
            }
            imageUrlList = res.data.agentExamContract.imageUrlList ? res.data.agentExamContract.imageUrlList : [];
            $('.contract_info_item').html(template('contract_info',{data:res.data || ''}));

            //初始化上传图片控件
            setTimeout(function () {
                var params = {
                    fileInput: $("#fileImage").get(0),
                    upButton: $("#fileSubmit").get(0),
                    url:  "/file/multiple_file_upload.vpage",
                    onSelect: function(files) {
                        var html = '', i = 0;
                        var funAppendImage = function() {
                            file = files[i];
                            if (file) {
                                var reader = new FileReader();
                                reader.onload = function(e) {
                                    html = html + '<div id="uploadList_'+ i +'" class="upload_append_list upload_add_list"><p>'+
                                            '<img id="uploadImage_' + i + '" src="' + e.target.result + '" class="upload_image" /><br />' +
                                            '<a href="javascript:" class="upload_delete" title="删除" data-index="'+ i +'">删除</a></p>'+
                                            '</div>';

                                    i++;
                                    funAppendImage();
                                }
                                reader.readAsDataURL(file);
                            } else {
                                $("#preview .upload_add_list").remove();
                                $("#preview").append(html);
                                if (html) {
                                    //删除方法
                                    $(".upload_delete").click(function() {
                                        UPLOADFILE.funDeleteFile(files[parseInt($(this).attr("data-index"))]);
                                        return false;
                                    });
                                }
                            }
                            $('#fileImage').val('');
                        };
                        funAppendImage();
                    },
                    onDelete: function(file) {
                        $("#uploadList_" + file.index).fadeOut();
                    },
                    onSuccess: function(res) {
                        postData.imageUrlList = imageUrlList.concat(res.imageUrlList).toString();
                        upsert_contract('upsert_contract.vpage',postData,_loading);
                    }
                };
                UPLOADFILE = $.extend(UPLOADFILE, params);
                UPLOADFILE.init();
            },30);

            //初始化签约人联想
            autocomplete();

            //时间控件初始化
            $("#contract_start_date,#contract_end_date,#contract_apply_date").datepicker({
                dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
                closeText       : "确定",
                currentText     : "本月",
                monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
                monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
                dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
                defaultDate     : new Date(),
                numberOfMonths  : 1,
                changeMonth: true,
                changeYear: true,
                showButtonPanel: true,
                onSelect : function (selectedDate){}
            });
        }
    });

    //渲染模板
    var renderDepartment = function(tempSelector,data,container){
        var source   = $(tempSelector).html();
        var template = Handlebars.compile(source);

        $(container).html(template(data));
    };

    //回款信息
    function getPaybackInfo() {
        $.get("contract_payback_info.vpage?contractId="+id,function (res){
            if(res.success){
                res.data.contractPaybackList.map(function (item) {
                    item.paybackDate = new Date(item.paybackDate).Format('yyyy-MM-dd');
                    return item;
                });
                $('.payback_info_item').html(template('payback_info',{res:res.data || ''}));
            }else{
                layer.alert(res.info);
            }
        });
    }

    //回款信息新增
    $(document).on("click",".addPayBackBtn",function(){
        var contractId = $(this).data("cid");
        var waitPaybackAmount = $(this).data("amount");
        renderDepartment("#addpaybackDialogTemp",{
            contractId:contractId,
            waitPaybackAmount:waitPaybackAmount
        },"#addpaybackDialog");

        $("#addpayback_dialog").modal('show');
        //时间控件
        $("#paybackDate").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            closeText       : "确定",
            currentText     : "本月",
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: true,
            changeYear: true,
            showButtonPanel: true,
            onSelect : function (selectedDate){}
        });
    });

    //回款信息新增提交
    $(document).on("click","#addpaybackSubmitBtn",function(){
        var contractId = $("#contractId").val();
        var waitPaybackAmount = $("#waitPaybackAmount").val();
        var paybackDate = $("#paybackDate").val();
        var paybackAmount = $("#paybackAmount").val();
        var postData = {
            contractId: contractId,
            waitPaybackAmount: waitPaybackAmount,
            paybackDate: paybackDate,
            paybackAmount: paybackAmount
        };
        if(parseFloat(waitPaybackAmount) < parseFloat(paybackAmount)){
            layer.alert("回款金额需小于等于待回款金额("+ waitPaybackAmount +")");
            return;
        }
        if(!paybackDate){
            layer.alert("请填写回款日期");
            return;
        }
        $.post("add_contract_payback.vpage",postData,function(res){
            if(res.success){
                getPaybackInfo();
                $("#addpayback_dialog").modal('hide');
                layer.alert("新建成功");
            }else{
                layer.alert(res.info);
            }
        });

    });

    //操作日志
    function getOpetation() {
        $.get("contract_opetation_record.vpage?contractId="+id,function (res){
            if(res.success){
                $('.opetation_info_item').html(template('opetation_info',{res:res || ''}));
            }else{
                layer.alert(res.info);
            }
        });
    }


    //删除合同文件
    $(document).on('click','.normal_delete',function () {
        var _this = $(this);
        if(_this.data('src')){
            _this.parents('.upload_append_list').fadeOut();
            for (var i = 0; i < imageUrlList.length; i++) {
                if(imageUrlList[i] == _this.data('src')){
                    imageUrlList.splice(i, 1);
                }
            }
        }
    });

    //切换选项卡
    $(document).on('click','.tab_btn',function () {
        var $index = $(this).index();
        if($index == 1){
            getPaybackInfo()
        }else if($index == 2){
            getOpetation();
        }
        $(this).addClass('btn-primary').siblings().removeClass('btn-primary');
        $('.contract_item').eq($index).removeClass('hidden').siblings('.contract_item').addClass('hidden');
    });

    //点击编辑按钮切换可编辑状态
    $(document).on('click','#editContract',function () {
        $('.canEdit').removeAttr('disabled');
        $('.canEditBtn,.form-actions').removeClass('hidden');
    });


    //判断是否符合提交条件
    var reg = /^\d+$/;//大于等于0的整数
    var reg2 = /^([1-9]\d*(\.[0-9]*[1-9])?)|(0\.[0-9]*[1-9])|0$/;//大于等于0的数字
    var checkData = function (postData,_loading) {
        var flag = true;
        $.each($(".js-postData"),function(i,item){
            postData[item.name] = $(item).val();
            if($(item).hasClass('js-needed')){
                if(!($(item).val())){
                    layer.close(_loading);
                    layer.alert($(item).data("einfo"));
                    flag = false;
                    return false;
                }
            }
        });

        var serviceRangeStr = '';
        $.each($('.serviceRangeInp'),function (i,item) {
            layer.close(_loading);
            var _val = $(item).val();
            if($(item).prop('checked')){
                serviceRangeStr += _val + ','
            }
        });
        if(serviceRangeStr.length == 0){
            layer.alert('请选择服务范围');
            flag = false;
            return false;
        }else{
            postData.serviceRangeStr = serviceRangeStr;
        }

        //分成比例判断
        $.each($('.proportionInput'),function (i,item) {
            layer.close(_loading);
            var _val = Number($(item).val());
            if(_val!=0 && (_val<0 || _val>100 || !_val)){
                layer.alert('分成比例输入错误（0-100）');
                flag = false;
                return false;
            }
        });

        var contractAmount = $('input[name="contractAmount"]').val().trim(),
                hardwareCost = $('input[name="hardwareCost"]').val().trim(),
                machinesNum = $('input[name="machinesNum"]').val().trim(),
                thirdPartyProductCost = $('input[name="thirdPartyProductCost"]').val().trim();
        if(contractAmount && !reg.test(contractAmount)){//判断总金额大于等于0整数
            layer.close(_loading);
            layer.alert('总金额必须为整数');
            flag = false;
        }
        if(hardwareCost){//判断硬件成本大于等于0整数
            layer.close(_loading);
            if(!reg.test(hardwareCost)){
                layer.alert('硬件成本必须为整数');
                flag = false;
            }
            if(Number(hardwareCost) > Number(contractAmount)){
                layer.alert('硬件成本不能大于总金额');
                flag = false;
            }
        }
        if(thirdPartyProductCost){//判断第三方产品成本大于等于0整数
            layer.close(_loading);
            if(!reg.test(thirdPartyProductCost)){
                layer.alert('第三方产品成本必须为整数');
                flag = false;
            }
            if(Number(thirdPartyProductCost) > Number(contractAmount)){
                layer.alert('第三方产品成本不能大于总金额');
                flag = false;
            }
        }
        if(Number(hardwareCost) + Number(thirdPartyProductCost) > Number(contractAmount)){
            layer.alert('硬件成本与第三方产品成本总额不能大于总金额');
            flag = false;
        }
        if(machinesNum && !reg.test(machinesNum)){//判断机器数量大于等于0整数
            layer.close(_loading);
            layer.alert('机器数量必须为整数');
            flag = false;
        }

        if($("#role_name").val() == ""){
            layer.close(_loading);
            layer.alert($("#role_name").data('einfo'));
            flag = false;
        }else{
            postData['contractType'] = $("#role_name").val();
        }
        postData['remark'] = $("textarea").val();
        return flag;
    };

    var upsert_contract = function (_url,postData,_loading) {
        $.post(_url,postData,function (res) {
            if(res.success){
                layer.alert('修改成功');
                window.history.back();
            }else{
                layer.close(_loading);
                layer.alert(res.info);
            }
        });
    };
    var postData = {};
    var _loading;
    var contractorBol = true;
    function getContract(contractorFlag,item,splitSettingList,_loading) {
        if($(item).find('.contractorInput').val() != '' && $(item).find('.proportionInput').val()!= ''){
            if($(item).find('.contractorId').val() != ''){
                contractorBol = true;
                splitSettingList.push({
                    "contractorId":Number($(item).find('.contractorId').val()),
                    "splitProportion":Number($(item).find('.proportionInput').val()),
                    "contractorFlag":contractorFlag
                });
            }else{
                contractorBol = false;
                layer.close(_loading);
                layer.alert($(item).find('.contractorInput').val()+'在系统中不存在');
            }
        }else if($(item).find('.contractorInput').val() == '' && $(item).find('.proportionInput').val()== ''){
            contractorBol = true;
            layer.close(_loading);
        }else{
            contractorBol = false;
            layer.close(_loading);
            layer.alert('签约人信息不完整');
        }
    }

    //点击保存或者取消按钮操作
    $(document).on('click','.submitBtn',function () {
        var _this = $(this),info = _this.data('info');
        if(info == '0'){ //取消操作
            $('.canEdit').attr('disabled','disabled');
            $('.canEditBtn,.form-actions').addClass('hidden');
        }else{ //保存操作
            _loading = layer.load(1, {
                shade: [0.5,'#fff'] //0.1透明度的白色背景
            });

            // 获取签约人信息
            var splitSettingList = [],flag = true;
            var contractorItem = $('.contractorWrap .contractor_item');
            contractorItem.each(function (i,item) {
                if(i == 0){
                    getContract(1,item,splitSettingList,_loading);
                }else{
                    getContract(0,item,splitSettingList,_loading);
                }
                return contractorBol;
            });
            if(splitSettingList.length > 0 && splitSettingList[0].contractorFlag == 0){
                contractorBol = false;
                layer.close(_loading);
                layer.alert('主签约人信息不完整');
            }
            postData.splitSettingList = JSON.stringify(splitSettingList);
            postData.id = id;
            if(contractorBol && checkData(postData,_loading)){
                postData = postData;
                //上传图片
                $('#fileSubmit').trigger('click');
            }
        }
    });

    // 添加合作签约人
    $(document).on('click','.contractorAddBtn',function () {
        var contractorAdd = template('contractorAdd',{});
        var contractorMainAdd = template('contractorMainAdd',{});
        var contractorWrap = $('.contractorWrap');
        if(contractorWrap.find('.contractor_item').length > 0){
            contractorWrap.append(contractorAdd);
        }else{
            contractorWrap.append(contractorMainAdd);
        }

        //初始化签约人联想
        autocomplete();
    });

    // 签约人
    $(document).on('focus','.contractorInput',function () {
        var _this = $(this);
        $('.contractorInput').removeClass('contractorInp');
        $(this).addClass('contractorInp');
    });

    //自动补全签约人信息
    function autocomplete() {
        $('.contractorInput').autocomplete({
            delay :600,
            source:function(request,response){
                if(!request.term||request.term.trim()==''){
                    return;
                }
                $.get("search_user.vpage",{userKey: request.term},function(result){
                    response( $.map( result.dataList, function( item ) {
                        return {
                            value: item.realName,
                            id: item.id
                        }
                    }));
                });
            },
            select: function( event, ui ) {
                $('.contractorInp').next().val(ui.item.id);
            }
        });
    }

    $(document).on('keyup','.contractorInput',function () {
        $('.contractorInp').next().val('');
    });

    // 删除签约人
    $(document).on('click','.contractorDelBtn',function () {
        var _this = $(this);
        _this.parents('.contractor_item').remove();
    });

    // 上传图片按钮
    $(document).on('click','.uploadImgBtn',function () {
        $('#fileImage').trigger('click');
    });

    //查看大图
    $(document).on('click','.upload_image',function () {
       var _this = $(this),_src = _this.attr('src');
       $('#bigimg').attr('src',_src);
       $('#outerdiv').show();
    });
    $(document).on('click','#outerdiv',function () {
        $('#outerdiv').hide();
    });

});
</script>
</@layout_default.page>
