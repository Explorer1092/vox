<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='添加合同' page_num=15>
<style type="text/css">
    .control-group{display: inline-block;margin-left: 10px;}
    input,select{width: 100%;}
    .upload_preview{padding-left: 100px;}
    .upload_preview img{width: 80px;height: 120px;}
    div.uploader{display: none;}
    .upload_preview .upload_append_list{display: inline-block;margin-right: 5px;text-align: center;}
    .form_title{padding-left: 100px;margin: 10px 0 15px;font-size: 16px;color: #000;}
</style>
<div class="row-fluid sortable ui-sortable box-content">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2>添加合同</h2>
            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
            <div class="pull-right">
                <a id="addContract" class="btn btn-success" href="javascript:;">
                    <i class="icon-plus icon-white"></i>添加
                </a>
                &nbsp;
            </div>
        </div>
        <div class="box-content school_manage">
            <form class="form-horizontal">
                <p class="form_title">基本信息</p>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">学校ID</label>
                    <div class="controls">
                        <input name="schoolId" id="school_name" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请填写学校ID">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">学校名称</label>
                    <div class="controls">
                        <input id="real_name" name="schoolName" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请选择学校名称" readonly>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">等级：</label>
                    <div class="controls" style="line-height: 26px;">
                        <span style="display: inline-block;margin-right: 20px;" id="school_level"></span>专员：<span style="display: inline-block;" id="school_userName"></span>
                    </div>
                </div>

                <div class="control-group">
                    <label class="control-label" for="focusedInput">合同类型</label>
                    <div class="controls">
                        <select id="role_name" class="form-control js-needed" data-einfo="请选择合同类型" name="contractType" style="width: 280px;">
                            <option value="">请选择</option>
                            <option value="PAY_EXAM">付费</option>
                            <option value="LARGE_EXAM">大考</option>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">总金额</label>
                    <div class="controls">
                        <input name="contractAmount" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请填写金额">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">硬件成本</label>
                    <div class="controls">
                        <input name="hardwareCost" class="js-postData input-xlarge focused" type="text" data-einfo="请填写硬件成本">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">第三方产品成本</label>
                    <div class="controls">
                        <input name="thirdPartyProductCost" class="js-postData input-xlarge focused" type="text" data-einfo="请填写第三方产品成本">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">服务开始日期</label>
                    <div class="controls">
                        <input id="contract_start_date" name="beginDate" class="js-postData input-xlarge focused js-needed" type="text" data-needInfo="合同开始日期不能为空"
                               data-einfo="请选择服务开始日期">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">服务结束日期</label>
                    <div class="controls">
                        <input id="contract_end_date" name="endDate" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请选择服务结束日期">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">签约日期</label>
                    <div class="controls">
                        <input id="contract_apply_date" name="contractDate" class="js-postData input-xlarge focused js-needed" type="text" data-einfo="请选择签约日期">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">机器数量</label>
                    <div class="controls">
                        <input name="machinesNum" class="js-postData input-xlarge focused" type="text">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">机器型号</label>
                    <div class="controls">
                        <input name="machinesType" class="js-postData input-xlarge focused" type="text">
                    </div>
                </div>
                <div class="control-group serviceRange">
                    <label class="control-label">服务范围</label>
                    <div class="controls" style="height:18px;line-height:18px">
                        <#if serviceRangeList?has_content>
                            <#list serviceRangeList as item>
                                <input type="checkbox"  name ="serviceRange" class="serviceRangeInp" value="${item.sr_key!''}" >${item.sr_value!''} &nbsp;&nbsp;&nbsp;
                            </#list>
                        </#if>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="focusedInput">备注</label>
                    <div class="controls">
                        <textarea name="remark" id="" style="width: 725px;height: 30px;padding: 3px;resize: none;"></textarea>
                    </div>
                </div>
                <p class="form_title">分成设置 <a class="contractorAddBtn btn btn-primary">添加签约人</a></p>
                <div class="contractorWrap">
                    <div class="contractor_item" style="width: 100%;">
                        <div class="control-group">
                            <label class="control-label" for="focusedInput">主签约人</label>
                            <div class="controls">
                                <input name="" class="js-postData input-xlarge focused contractorInput" type="text">
                                <input type="hidden" class="contractorId">
                                <input type="hidden" class="contractorFlag" value="1">
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="control-label" for="focusedInput">分成比例</label>
                            <div class="controls">
                                <input name="" class="js-postData input-xlarge focused proportionInput" type="text">%
                            </div>
                        </div>
                    </div>
                </div>
            </form>
            <p class="form_title">合同文件 <input type="button" class="uploadImgBtn btn btn-primary" value="上传照片"></p>
            <form id="uploadForm">
                <input id="fileImage" type="file" size="30" name="fileselect[]" multiple="" accept="image/jpeg,image/png,image/gif">
                <div id="preview" class="upload_preview"></div>
                <div id="uploadInf" class="upload_inf"></div>
                <input type="button" id="fileSubmit" class="upload_submit_btn btn btn-primary hidden" value="确认上传图片">
            </form>
        </div>
    </div>
</div>
<#--查看大图-->
<div id="outerdiv" style="position:fixed;top:0;left:0;background:rgba(0,0,0,0.7);z-index:2;width:100%;height:100%;display: none;">
    <img id="bigimg" style="position: absolute;left: 50%;top: 50%;transform: translate(-50%,-50%);" src="" />
</div>
<div id="editDepInfo_dialog" class="modal fade hide" style="width:800px">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal">&times;</button>
                <h4 class="modal-title">此学校已有一份合同信息，是否确认替换？</h4>
            </div>
            <div class="modal-body">
                <div id="editInfoDialog" class="form-horizontal">

                </div>
            </div>
            <div class="modal-footer">
                <div>
                    <button id="upsertSubmitBtn" type="button" class="btn btn-large btn-primary">确定</button>
                    <button type="button" class="btn btn-large btn-default" data-dismiss="modal">取消</button>
                </div>
            </div>
        </div>
    </div>
</div>

<script src="${requestContext.webAppContextPath}/public/js/uploadImg/uploadFile.js?v=1"></script>
<script id="old_examination" type="text/html">
    <div class="dataTables_wrapper" role="grid">
        <table class="table table-striped table-bordered bootstrap-datatable "
               id="datatable"
               aria-describedby="DataTables_Table_0_info">
            <thead>
            <tr>
                <th class="sorting" style="width: 60px;">合同编号</th>
                <th class="sorting" style="width: 60px;">合同类型</th>
                <th class="sorting" style="width: 60px;">金额</th>
                <th class="sorting" style="width: 60px;">服务开始时间</th>
                <th class="sorting" style="width: 60px;">服务结束时间</th>
                <th class="sorting" style="width: 100px;">签约人</th>
                <th class="sorting" style="width: 100px;">签约日期</th>
            </tr>
            </thead>
            <tbody>
            <%if(res){%>
                <tr>
                    <td><%=res.contractNumber%></td>
                    <td>
                        <%if(res.contractType == 'PAY_EXAM'){%>付费<%}%>
                        <%if(res.contractType == 'LARGE_EXAM'){%>大考<%}%>
                    </td>
                    <td><%=res.contractAmount%></td>
                    <td><%=res.beginDate%></td>
                    <td><%=res.endDate%></td>
                    <td><%=res.contractorName%></td>
                    <td><%=res.contractDate%></td>
                </tr>
            <%}%>
            </tbody>
        </table>
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
                <input type="hidden" class="contractorFlag" value="0">
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
        var imageUrlList = [];
        var layerAlert = null;//设置一个弹出层 用于查询学校信息 多次回车时切换弹出层
        //时间控件
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
        //查询学校信息
        $('#school_name').on('keypress',function (e) {
            if (e.keyCode == 13) {
                if($('.layui-layer-dialog').length>0){
                    layer.close(layerAlert);layerAlert = null;
                    return;
                }
                $.get("verify_school.vpage?schoolId=" + $('#school_name').val().trim(), function (res) {
                    if (res.success) {
                        $('#real_name').val(res.data.cmainName);
                        $('#school_level').html(res.data.schoolPopularityType);
                        $('#school_userName').html(res.data.userName ? res.data.userName : '暂无专员');
                    } else {
                        $('#real_name').val('');
                        $('#school_level,#school_userName').html('');
                        layerAlert = layer.alert(res.info);
                    }
                })
            }
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

        autocomplete();

        // 删除签约人
        $(document).on('click','.contractorDelBtn',function () {
            var _this = $(this);
            _this.parents('.contractor_item').remove();

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
                if($(item).parent().hasClass('checked')){
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
                machinesNum = $('input[name="machinesNum"]').val().trim();
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
                    layer.alert('添加成功');
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
        $(document).on('click',"#addContract",function () {
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
            if(contractorBol && checkData(postData,_loading)){
                postData = postData;
                //上传图片
                $('#fileSubmit').trigger('click');
            }

        });
        $(document).on('click','#upsertSubmitBtn',function () {
            upsert_contract('upsert_contract.vpage',postData,_loading);
        });

        // 添加合作签约人
        $(document).on('click','.contractorAddBtn',function () {
            var html = template('contractorAdd',{});
            $('.contractorWrap').append(html);

            autocomplete();
        });

        // 上传图片按钮
        $(document).on('click','.uploadImgBtn',function () {
            $('#fileImage').trigger('click');
        });
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
                                html = html + '<div id="uploadList_'+ i +'" class="upload_append_list"><p>'+
                                        '<img id="uploadImage_' + i + '" src="' + e.target.result + '" class="upload_image" /><br />' +
                                        '<a href="javascript:" class="upload_delete" title="删除" data-index="'+ i +'">删除</a></p>'+
                                        '</div>';

                                i++;
                                funAppendImage();
                            }
                            reader.readAsDataURL(file);
                        } else {
                            $("#preview").html(html);
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
                    postData.imageUrlList = res.imageUrlList.toString();
                    $.get('school_valid_contract.vpage?schoolId='+$('#school_name').val(),function (data) {
                        if(data.success){
                            if(data.agentExamContract){
                                layer.close(_loading);
                                data.agentExamContract.beginDate = new Date(data.agentExamContract.beginDate).Format('yyyy-MM-dd');
                                data.agentExamContract.endDate = new Date(data.agentExamContract.endDate).Format('yyyy-MM-dd');
                                data.agentExamContract.contractDate = new Date(data.agentExamContract.contractDate).Format('yyyy-MM-dd');
                                $('#editInfoDialog').html(template('old_examination',{res:data.agentExamContract}));
                                $('#editDepInfo_dialog').modal();
                            }else{
                                upsert_contract('upsert_contract.vpage',postData,_loading)
                            }
                        }
                    });
                }
            };
            UPLOADFILE = $.extend(UPLOADFILE, params);
            UPLOADFILE.init();
        },30);

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
