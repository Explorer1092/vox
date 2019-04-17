<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title='信息分发' page_num=page_num>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<style type="text/css">
    .publish_warn{padding: 10px 0;font-size: 14px;}
    .publish_warn ul li{list-style: none;line-height: 2;color: #333;}
</style>
<div class="row-fluid sortable ui-sortable">

    <div class="box span12 box-content" style="padding: 0;">
        <form id="form" class="form-horizontal" method="post" enctype="multipart/form-data"
              action="/publish/data/upload_data.vpage" data-ajax="false">
            <input type="hidden" name="publishId" <#if control??>value="${control.publishId!''}"</#if>>
            <div class="box-header well">
                <h2><i class="icon-th"></i> 信息分发添加数据</h2>
            </div>
            <div class="publish_warn">
                <ul>
                    <li>1、请上传含有标准行和列的表格数据 （有合并单元格的数据请处理后再上传，否则可能出现表头识别有误）</li>
                    <li>2、多个sheet请分开上传，默认上传第一个sheet</li>
                    <li>3、前2-3列固定为部门、姓名、组别，发布成功后对应部门/人员将能够看到本部门/人员所在行的数据，同组别内可查看同组内成员所有数据</li>
                </ul>
            </div>
            <div class="control-group">
                <label class="control-label"></label>
                <div class="controls">
                    <label class="control-label" style="width: 220px">
                        <input type="radio" name="isGrouped" value="false" <#if control?? && control.isGrouped?has_content>disabled</#if> <#if control?? && !control.isGrouped>checked</#if>>数据不分组(前2列为“部门、姓名”)
                    </label>
                    <label class="control-label" style="width: 450px;">
                        <input type="radio" name="isGrouped" value="true" <#if control?? && control.isGrouped?has_content>disabled</#if> <#if control?? && control.isGrouped>checked</#if>>数据分组(前3列为“部门、姓名、组别”,同组别部门/人员可相互查看数据)
                    </label>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label" for="focusedInput">上传excel</label>
                <div class="controls">
                    <div class="uploader" id="uniform-sourceFile">
                        <input id="sourceFile" name="sourceExcelFile" type="file" disabled />
                    </div>
                    <a href="javascript:;" class="btn btn-primary savaBtn">上传</a>
                </div>
            </div>
            <div class="dataWrap" style="margin-left: 160px;margin-right: 10px;max-height: 500px; overflow: scroll;">
                <div class="errorInfo" style="color: #f00;">

                </div>
                <div class="dataInfo"></div>
                <table class="table table-striped table-bordered bootstrap-datatable">
                    <thead>
                    </thead>
                    <tbody>
                    </tbody>
                </table>
            </div>
            <#include 'info_distribution.ftl'>
        </form>
    </div>
</div>
<div id="loadingDiv" style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在上传，请等待……</p>
</div>
<script type="application/javascript">
    $(function () {
        var publishId = '${publishId!''}';
        var groupIds = "";
        var groupIdList = [];
        <#if control?? && control.groupIdList?? && control.groupIdList?size gt 0>
            <#list control.groupIdList as list>
                groupIdList.push(${list!0});
            </#list>
        </#if>
        groupIds = groupIdList.toString();
        //数据分组 使上传文件按钮可以点击
        $('input[name="isGrouped"]').on('click',function () {
            $("#sourceFile").attr('disabled',false).parent().removeClass('disabled');
        });
        $("#sourceFile").on('change',function () {
            var _val =  $(this).val();
            var index = _val.lastIndexOf("\\");
            var index2 = _val.lastIndexOf(".");

            var sourceFile = $("#sourceFile").val();
            if (blankString(sourceFile)) {
                alert("请上传excel！");
                return;
            }
            var fileParts = sourceFile.split(".");
            var fileExt = fileParts.length < 2 ? null : fileParts[fileParts.length - 1].toLowerCase();
            if (fileExt !== "xls" && fileExt !== "xlsx" && fileExt !== "csv") {
                alert("请上传正确格式(xls、xlsx、csv)的excel！");
                $('#title').val('');
                return;
            }

            $('#title').val(_val.substring(index + 1, index2));
            $('input[name="isGrouped"]').attr('disabled',false).parents('.radio').removeClass('disabled');
        });

        // 上传操作
        var savaBtn = $('.savaBtn'),//上传按钮
            thead = $('.dataWrap table thead'),
            tbody = $('.dataWrap table tbody');
        savaBtn.on('click',function () {
            var formElement = $('#form')[0];
            var postData = new FormData(formElement);
            $("#loadingDiv").show();//显示loading
            thead.html('');
            tbody.html('');//重新上传之后删除之前上传的内容
            $('.dataInfo').text('');//数据显示部分情况
            $('.errorInfo').text('');//错误信息清空
            $.ajax({
                url: "upload_data.vpage",
                type: "POST",
                data: postData,
                dataType: "json",
                processData: false,  // 告诉jQuery不要去处理发送的数据
                contentType : false,
                success: function (res) {
                    $("#loadingDiv").hide();//隐藏loading
                    if(res.success){
                        publishId = res.publishId;
                        $('input[name="publishId"]').val(res.publishId);
                        viewData();
                    }else{
                        if(res.info){
                            alert(res.info);
                            return ;
                        }
                        if(res.errorList){
                            $('.errorInfo').text(res.errorList);
                        }
                    }
                }
            });
        });

        function viewData() {
            $.get("publish_data_list.vpage",{'publishId':publishId},function (res) {
                if(res.success){
                    var data = res.data;
                    $('.dataInfo').text('共'+ data.dataList.length +'行，共'+ data.dataTitleList.length +'列');
                    thead.html('');
                    tbody.html('');
                    var headerString = '';
                    data.dataTitleList.forEach(function (val) {
                        headerString += '<th style="min-width: 100px;">'+ val +'</th>';
                    });
                    thead.append("<tr><th style='width: 50px;min-width: auto;'>序号</th>"+ headerString +"</tr>");
                    data.dataList.forEach(function (arr,index) {
                        var bodyString = '';
                        arr.forEach(function (val) {
                            bodyString += '<td>'+ val +'</td>';
                        });
                        index = index + 1;
                        tbody.append("<tr><td>"+ index +"</td>"+ bodyString +"</tr>");
                    });
                }else{
                    alert(res.info);
                }
            });
        }
        //进入编辑页面显示数据表格
        if(publishId !== ''){
            viewData();
            //如果是编辑页面 就可以点击上传文件
            $("#sourceFile").attr('disabled',false).parent().removeClass('disabled');
        }


        $("#useUpdateDep_con_dialog").fancytree({
            source: {
                url: "/user/orgconfig/getNewDepartmentTree.vpage?groupIds="+groupIds,
                cache:true
            },
            checkbox: true,
            autoCollapse:true,
            selectMode: 3
        });
        var checkData = function () {
            var checkBoolean = true;
            if($('#title').val() == '' || $('input[name="subordinate"]:checked').length == 0 || $('input[name="_download"]:checked').length == 0){
                checkBoolean = false ;
                alert('请检查是否填写完整');
            }
            if(publishId === ''){
                checkBoolean = false ;
                alert('请检查是否上传文件');
            }
            return checkBoolean;
        };
        var post_data = function(_url,postData,_href){
            $.get(_url,postData)
                    .done(function (res) {
                        if(res.success){
                            _href;
                        }else{
                            alert(res.info);
                        }
                    })
        };
        $(document).on("click",".submitBtn",function(){
            var _info = $(this).data('info');
            if(_info == 0){
                window.history.back();
            }else if(_info != 0 && checkData()){
                var tree = $("#useUpdateDep_con_dialog").fancytree("getTree");
                var area_code = [];
                if($("#useUpdateDep_con_dialog").fancytree("getTree").getSelectedNodes()){
                    var node = $("#useUpdateDep_con_dialog").fancytree("getTree").getSelectedNodes();
                    for(var i = 0; i< node.length; i++){
                        area_code.push(node[i].key);
                    }
                }
                var dataObj = {
                    publishId : publishId,
                    title : $('#title').val(),
                    allowViewSubordinateData:$('input[name="subordinate"]:checked').val(),
                    allowDownload:$('input[name="_download"]:checked').val(),
                    comment:$("#comment").val(),
                    groupIds:area_code.toString()
                };
                var roleTypes = [];
                $('input[name="roleType"]:checked').each(function () {
                    var roleType = $(this).val();
                    roleTypes.push(roleType);
                });
                dataObj.roleIds = roleTypes.toString();
                if(_info == 'save_data.vpage'){
                    var next_action = function () {
                        alert('提交成功');
                        window.history.back();
                    };
                    post_data(_info,dataObj,next_action());
                }else{
                    var next_action = function () {
                        $.get(_info,{
                            id:publishId,
                            status:1
                        })
                        .done(function (res) {
                            if(res.success){
                                alert('提交成功');
                                window.history.back();
                            }
                        })
                    };
                    post_data('save_data.vpage',dataObj,next_action());
                }

            }

        });
    });
</script>
</@layout_default.page>

