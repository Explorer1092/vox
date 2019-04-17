<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='绘本合集管理' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div id="main_container" class="span9">
    <legend>
        <strong>绘本合集管理</strong>
    </legend>
    <form id="config-query" class="form-horizontal" method="get" action="list.vpage">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <ul class="inline">
            <li>
                <label>推荐位置&nbsp;
                    <select id="type" name="query_type">
                        <option value="">全部</option>
                        <option value="1" <#if showType == 1>selected</#if>>家长端-绘本馆banner</option>
                        <option value="2"<#if showType == 2>selected</#if>>老师端</option>
                        <option value="3"<#if showType == 3>selected</#if>>家长端-学习资源绘本tab</option>
                    </select>
                </label>
            </li>
            <li>
                <label>标题：&nbsp;
                    <input id="title" name="query_title" type="text" value="${title!''}" placeholder="请输入标题"
                           style="width: 100px">
                </label>
            </li>
            <li>
                <button type="button" class="btn btn-primary" id="searchBtn">查 询</button>
            </li>
            <li>
                <button type="button" class="btn btn-success js-couponOption" data-type="add" id="add_accounts_btn">新建
                </button>
            </li>
        </ul>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="220px">ID</th>
                        <th>推荐位置</th>
                        <th>标题</th>
                        <th>最近一次操作时间</th>
                        <th>创建人</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                         <#if content?? && content?size gt 0>
                            <#list content as  config>
                            <tr>
                                <td>${config.id!''}</td>
                                <td>${config.showType!''}</td>
                                <td>${config.title!''}</td>
                                <td>${config.updateDate!''}</td>
                                <td>${config.creator!''}</td>
                                <td>
                                    <#if config.enabled>
                                        上线中
                                    <#else>
                                        已下线
                                    </#if>
                                </td>
                                <td>
                                    <#if config.enabled>
                                        <button disabled="disabled" class="btn btn-default" onclick="modify('${config.id!''}')">编辑</button>
                                        <button disabled="disabled" class="btn btn-default"
                                                onclick="changeStatus('${config.id!''}',1)">上线
                                        </button>
                                        <button class="btn btn-success" onclick="changeStatus('${config.id!''}',0)">下线
                                        </button>
                                    <#else>
                                        <button class="btn btn-success" onclick="modify('${config.id!''}')">编辑</button>
                                        <button class="btn btn-success" onclick="changeStatus('${config.id!''}',1)">上线
                                        </button>
                                        <button disabled="disabled" class="btn btn-default"
                                                onclick="changeStatus('${config.id!''}',0)">下线
                                        </button>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                         <#else>
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
                        </tr>
                         </#if>
                    </tbody>
                </table>
                <ul class="pager">
                    <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>
<div id="add_dialog" class="modal fade hide" aria-hidden="true" role="dialog" style="width: 800px">
    <div class="modal-dialog">
        <div class="modal-content">
            <div class="modal-header">
                <h3 class="modal-title">配置绘本合集</h3>
            </div>
            <div class="modal-body" style="overflow: auto;max-height: 500px;">
                <form id="add-account-frm" action="save.vpage" method="post" role="form">
                    <input type="hidden" id="singleItemBox" name="singleItemBox" value="0"/>
                    <input type="hidden" id="config_id" name="id" value=""/>
                    <div class="form-group">
                        <span class="control-label" style="font-size: 14px"><span style="color: red">*</span>推荐位置</span>
                        <div class="controls" style="display: inline-block">
                            <label class="control-label" style="display: inline-block;margin: 0 33px 0 83px">
                                <input type="radio" class="showType" name="showType" id="radio_1" value="1" style="margin: 0"/>家长端-绘本馆banner
                            </label>
                            <label class="control-label" style="display: inline-block;margin: 0 33px 0 0">
                                <input type="radio" class="showType" name="showType" id="radio_2" value="2" style="margin: 0"/>老师端
                            </label>
                            <label class="control-label" style="display: inline-block">
                                <input type="radio" class="showType" name="showType" id="radio_3" value="3" style="margin: 0"/>家长端-学习资源绘本tab
                            </label>
                        </div>
                    </div>
                    <div class="form-group" id="adImg">
                        <span class="inline" style="font-size: 14px"><span style="color: red">*</span>广告图</span>
                        <input type="hidden" id="adUrl" name="adUrl" value="" required/>
                        <input type="file" class="fileUpBtn" id="adUpload" accept="image/gif, image/jpeg, image/png, image/jpg" style="margin-left: 93px">
                        <br/><img src="" id="adImgView" width="50%"/>
                    </div>
                    <div class="form-group has-feedback">
                        <span style="font-size: 14px;"><span style="color: red">*</span>合集标题</span>
                        <input class="form-control" type="text" id="pb_title" name="from_title"
                               style="margin:10px 0 0 78px"
                               maxlength="20" data-error="请填写合集标题" required>
                        <span class="glyphicon form-control-feedback" aria-hidden="true"></span>
                        <div class="help-block with-errors"></div>
                    </div>
                    <div class="form-group">
                        <span class="inline" style="font-size: 14px"><span style="color: red">*</span>页面主图</span>
                        <input type="hidden" id="imageUrl" name="imageUrl" value="" required/>
                        <input type="file" class="fileUpBtn" id="imageUpload" accept="image/gif, image/jpeg, image/png, image/jpg" style="margin-left: 79px">
                        <br/><img src="" id="imgView" width="50%"/>
                    </div>
                    <div class="form-group" style="margin-top: 20px">
                        <span style="font-size: 14px"><span style="color: red">*</span>介绍(200字以内)</span>
                        <textarea class="form-control" id="instruction" name="instruction" rows="5" cols="10"
                                  maxlength="200" required style="margin-left: 30px;vertical-align: top"></textarea>
                        <div class="help-block with-errors"></div>
                    </div>
                <#-- 课古诗内容 -->
                    <div class="form-group" id="singleItemMain">
                        <span class="control-label" style="font-size: 14px"><span
                                style="color: red;font-size: 20px;">*</span>绘本:</span>
                        <div id="newAddSingleItem"></div>
                        <div class="controls singleItemBox" style="margin-top: 5px;">
                            <input type="button" value="添加" class="btn btn-primary" id="addRowSingle">
                        </div>
                    </div>
                </form>
            </div>
            <div class="modal-footer">
                <button id="cancel_button" type="button" class="btn btn-default">取消</button>
                <button id="save-account-submit" type="submit" class="btn btn-primary">提交</button>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $(".message_page_list").page({
            total: ${totalPage!},
            current: ${currentPage!},
            autoBackToTop: false,
            maxNumber: 10,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#config-query").submit();
            }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#config-query").submit();
        });

    })

    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#config-query").submit();
    }

    function modify(id) {
        if (id === '') {
            alert("参数错误");
        }
        $.ajax({
            type: "get",
            url: "modify.vpage",
            data: {
                id: id
            },
            success: function (data) {
                if (data.success) {
                    hideAdInput();
                    $("#add_dialog #singleItemBox").val(data.singleItemBox);
                    $("#add_dialog #config_id").val(data.config.id);
                    $("#add_dialog #radio_"+data.config.showTypeCode).prop("checked",true);
                    $("#add_dialog #pb_title").val(data.config.title);
                    $("#add_dialog #imgView").prop("src",data.config.imgUrl);
                    $("#add_dialog #instruction").text(data.config.introduction);
                    $("#add_dialog #imageUrl").val(data.config.imgUrl);
                    console.log(data.config.showTypeCode);
                    if(data.config.showTypeCode!==2){
                        $("#add_dialog #adUrl").val(data.config.adImgUrl);
                        $("#add_dialog #adImgView").prop("src",data.config.adImgUrl);
                        showAdInput();
                    }

                    var p_list = data.config.infoVOS;
                    for(var i=0;i<p_list.length;i++){

                        var info_id = p_list[i].id;
                        var pb_id = p_list[i].pictureBookId;
                        var pb_ci = p_list[i].configWords===null?"":p_list[i].configWords;
                        var cl_id = p_list[i].configListId;
                        var recommendWords = p_list[i].recommendWords===null?"":p_list[i].recommendWords;
                        var recommendWordsSecond = p_list[i].recommendWordsSecond===null?"":p_list[i].recommendWordsSecond;

                        var itemCount = $("#newAddSingleItem").find(".pb_id").length+1;
                        var newAddList = $("#newAddSingleItem");
                        var _html = '<div class="form-group controls singleItemBox pictureBookInfo" style="margin-top: 5px;">' +
                                '<input type="hidden" name="info_id_' + itemCount + '" value="' + info_id + '"/>' +
                                '<input type="hidden" name="cl_id_' + itemCount + '" value="' + cl_id + '"/>' +
                                '<span style="font-size: 14px; font-weight: normal;"><span style="color: red">*</span>绘本ID:</span>' +
                                '<input type="text" name="pb_id_' + itemCount + '" value="' + pb_id + '" class="input pb_id" style="width: 150px;margin:0 5px;" required>' +
                                '<span style="font-size: 14px; font-weight: normal;">配置信息:</span>' +
                                '<input type="text" name="pb_ci_' + itemCount + '" value="' + pb_ci + '" class="input pb_ci" style="width: 150px;margin:0 5px;">' +
                                '<span style="font-size: 14px; font-weight: normal;">(名称下方可配置一条信息)</span><br/>' +
                                '<span style="font-size: 14px; font-weight: normal;">推荐语1</span>' +
                                '<input type="text" name="pb_rewords1_' + itemCount + '" value="' + recommendWords + '" class="input orderCtn" style="width: 150px;margin:5px 7px;">' +
                                '<span style="font-size: 14px; font-weight: normal;">推荐语2</span>' +
                                '<input type="text" name="pb_rewords2_' + itemCount + '" value="' + recommendWordsSecond + '" class="input orderCtn" style="width: 150px;margin:0 11px;">' +
                                '<input type="button" value="删除" class="btn thisDelete" data-val="' + (itemCount++) + '">' +
                                '</div>';
                        newAddList.append(_html);
                        $('#singleItemBox').val($("#newAddSingleItem").find(".pb_id").length);
                    }
                    $("#add_dialog").modal("show");
                } else {
                    alert("操作失败");
                }
            }
        });
    }

    function changeStatus(id, enable) {
        if (id === '' || enable === '') {
            alert("参数错误");
        }
        $.ajax({
            type: "post",
            url: "enable.vpage",
            data: {
                id: id,
                enable: enable
            },
            success: function (data) {
                if (data.success) {
                    var type = $("#type").val();
                    var title = $("#title").val();
                    var pageNum = ${currentPage!'1'};
                    window.location.href = 'list.vpage?page='+pageNum+'&query_type='+type+'&query_title='+title;
                } else {
                    alert("操作失败");
                }
            }
        });
    }

    $("button#add_accounts_btn").click(function () {
        $("#add_dialog").modal("show");
    });

    function mapForm(func) {
        var frm = $("form#add-account-frm");
        $.each($("input,textarea", frm), function (index, field) {
            var _f = $(field);
            func(_f, _f.attr("type") == "checkbox");
        });
    }

    var skuCContent = [];
    //添加绘本
    var singleItemBox = $("#newAddSingleItem").find(".pb_id").length;
    $(document).on("click", "#addRowSingle", function () {
        var count = $("#newAddSingleItem").find(".pb_id").length;
        singleItemBox = count + 1;
        var newAddList = $("#newAddSingleItem");
        var _html = '<div class="form-group controls singleItemBox pictureBookInfo" style="margin-top: 5px;">' +
                '<span style="font-size: 14px; font-weight: normal;"><span style="color: red">*</span>绘本ID:</span>' +
                '<input type="text" name="pb_id_' + singleItemBox + '" value="" class="input pb_id" style="width: 150px;margin:0 5px;" required>' +
                '<span style="font-size: 14px; font-weight: normal;">配置信息:</span>' +
                '<input type="text" name="pb_ci_' + singleItemBox + '" value="" class="input pb_ci" style="width: 150px;margin:0 5px;">' +
                '<span style="font-size: 14px; font-weight: normal;">(名称下方可配置一条信息)</span><br/>' +
                '<span style="font-size: 14px; font-weight: normal;">推荐语1</span>' +
                '<input type="text" name="pb_rewords1_' + singleItemBox + '" value="" class="input orderCtn" style="width: 150px;margin:5px 7px;">' +
                '<span style="font-size: 14px; font-weight: normal;">推荐语2</span>' +
                '<input type="text" name="pb_rewords2_' + singleItemBox + '" value="" class="input orderCtn" style="width: 150px;margin:0 11px;">' +
                '<input type="button" value="删除" class="btn thisDelete" data-val="' + (singleItemBox++) + '">' +
                '</div>';
        newAddList.append(_html);
        $('#singleItemBox').val($("#newAddSingleItem").find(".pb_id").length);
    });
    //删除绘本
    $(document).on("click", "#newAddSingleItem .thisDelete", function () {
        var $this = $(this);
        singleItemBox--;
        var count = $("#newAddSingleItem").find(".pb_id").length;
        if (count === $this.data("val")) {
            skuCContent.splice($this.data("val"), 1);
            $this.closest(".singleItemBox").remove();
            $('#singleItemBox').val($("#newAddSingleItem").find(".pb_id").length);
        } else {
            alert("从尾行开始删除")
        }
        ;
    });

    $("button#save-account-submit").click(function () {
            var frm = $("form#add-account-frm");
            var postData = {};
            var flag = true;
            var radioChecked = '0';
            $.each($("input:radio", frm), function (index, field) {
                var _f = $(field);
                if(_f.prop('checked')){
                    postData[_f.attr("name")] = _f.val();
                    radioChecked = _f.val();
                }
            });
            $.each($("input:text,input:hidden,textarea", frm), function (index, field) {
                var _f = $(field);
                if(_f.prop("required")){
                    if(_f.prop('id') === 'adUrl'&&radioChecked === '2'){
                        return true;
                    }
                    if(_f.val()===''){
                        alert("请填写全部必填项");
                        flag = false;
                        return false;
                    }
                }
                postData[_f.attr("name")] = _f.val();
            });

            if($('#singleItemBox').val()==='0'){
                alert("请填写全部必填项");
                flag = false;
            }
            if(!flag){
                return false;
            }


        $.post("save.vpage", postData,
                function (data) {
                    if (data.success) {
                        $("#add_dialog").modal("hide");
                        $('#singleItemBox').val(0);
                        $(".pictureBookInfo").remove();
                        $("form#add-account-frm")[0].reset();
                        alert("保存成功");
                        window.location.reload();
                    }
                    else
                        alert(data.info);
                }
        );
    });

    $("button#cancel_button").click(function () {
        $("#add_dialog").modal("hide");
        $("#singleItemBox").val(0);
        $(".pictureBookInfo").remove();
        $("form#add-account-frm")[0].reset();
        $("#add_dialog #imgView").prop("src","");
        $("#add_dialog #adImgView").prop("src","");
        $("#add_dialog #adUrl").val("");
        $("#add_dialog #imageUrl").val("");
        $("#add_dialog #instruction").text("");
    });

    $("#add_dialog .fileUpBtn").change(function () {
        var container = $(this);
        var fileId = container.prop("id");
        var ext = container.val().split('.').pop().toLowerCase();
        if (container != '') {
            if ($.inArray(ext, ['gif', 'png', 'jpg', 'jpeg']) == -1) {
                alert("仅支持以下格式的图片【'gif','png','jpg','jpeg'】");
                return false;
            }
            var formData = new FormData();
            formData.append('inputFile', container[0].files[0]);
            var fileSize = (container[0].files[0].size / 1024 / 1012).toFixed(4); //MB
            console.info(fileSize);
            if (fileSize >= 2) {
                alert("图片过大，重新选择。");
                return false;
            }
            $.ajax({
                url: 'upload.vpage',
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (data) {
                    if (data.success) {
                        if(fileId==="adUpload"){
                            $("#add_dialog #adImgView").attr('src', data.imgUrl);
                            $("#add_dialog #adImgView").data("file_name", data.imgName);
                            $("#add_dialog #adUrl").val(data.imgUrl);
                        }else{
                            $("#add_dialog #imgView").attr('src', data.imgUrl);
                            $("#add_dialog #imgView").data("file_name", data.imgName);
                            $("#add_dialog #imageUrl").val(data.imgUrl);
                        }
                    } else {
                        alert("上传失败");
                    }
                }
            });
        }
    });
    $("#add_dialog .showType").click(function () {
        var radio = $(this);
        if(radio.prop('checked')&&radio.val()==='2'){
            hideAdInput();
        }else{
            showAdInput();
        }
    });
    function hideAdInput() {
        $("#add_dialog #adImg").hide();
    }
    function showAdInput() {
        $("#add_dialog #adImg").show();
    }
    
</script>
</@layout_default.page>