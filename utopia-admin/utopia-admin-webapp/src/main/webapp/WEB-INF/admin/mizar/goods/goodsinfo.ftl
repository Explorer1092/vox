<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑品牌" page_num=17>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑课程&nbsp;&nbsp;
        <#if goods??> (<#if goods.status??>${goods.status.getDesc()!''}<#else>离线</#if>)</#if>
        <a title="返回" href="javascript:window.history.back();" class="btn">
            <i class="icon-share-alt"></i> 返  回
        </a>
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
            <i class="icon-pencil icon-white"></i> 保  存
        </a>
        <#if goods?? && goods.status?? && (goods.status == 'PENDING' || goods.status == 'OFFLINE' )>
            <a title="审核通过" href="javascript:void(0);" class="btn btn-success" id="approve_info">
                <i class="icon-ok icon-white"></i> 审核通过
            </a>
        </#if>
        <#if goods??>
        <div  style="float: right;">
            <a title="查看关联机构" href="${requestContext.webAppContextPath}/mizar/shop/info.vpage?sid=${goods.shopId}" class="btn btn-info">
                <i class="icon-th icon-white"></i> 查看关联机构
            </a>
        </div>
        </#if>
    </legend>
    <div class="row-fluid"><div class="span12"><div class="well">
        <form id="info_frm" name="info_frm" enctype="multipart/form-data" action="savegoods.vpage" method="post">
            <input id="gid" name="gid" value="${gid!}" type="hidden">
            <div class="form-horizontal">
                <#if goods??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程ID</label>
                    <div class="controls">
                        <input type="text"  class="form-control input_txt"  value="${goods.id!}" readonly/>
                    </div>
                </div>
                </#if>
                <div class="control-group">
                    <label class="col-sm-2 control-label">机构ID</label>
                    <div class="controls">
                        <input type="text" id="sid" name="sid" class="form-control input_txt"
                            <#if new?? && !new> <#if goods??>value="${goods.shopId!}"</#if> <#else> <#if sid??>value="${sid!}"</#if> </#if>/>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程名称</label>
                    <div class="controls">
                        <input type="text" id="goodsName" name="goodsName" class="form-control input_txt" value="<#if goods??>${goods.goodsName!}</#if>" />
                        <input type="checkbox" id="recommended" name="recommended" <#if goods?? && goods.recommended?? && goods.recommended> checked </#if>>&nbsp;&nbsp;推荐到首页
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程标题</label>
                    <div class="controls">
                        <textarea id="title" name="title" class="intro_small"  placeholder="请填写课程标题"><#if goods??>${goods.title!}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程简介</label>
                    <div class="controls">
                        <textarea id="desc" name="desc" class="intro"  placeholder="请填写课程简介"><#if goods??>${goods.desc}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课时</label>
                    <div class="controls">
                        <input type="text" id="goodsHours" name="goodsHours" class="form-control input_txt" value="<#if goods??>${goods.goodsHours!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">时长</label>
                    <div class="controls">
                        <input type="text" id="duration" name="duration" class="form-control input_txt" value="<#if goods??>${goods.duration!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">上课时间</label>
                    <div class="controls">
                        <input type="text" id="goodsTime" name="goodsTime" class="form-control input_txt" value="<#if goods??>${goods.goodsTime!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">年龄段</label>
                    <div class="controls">
                        <input type="text" id="target" name="target" class="form-control input_txt" value="<#if goods??>${goods.target!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程分类</label>
                    <div class="controls">
                        <input type="text" id="category" name="category" class="form-control input_txt" value="<#if goods??>${goods.category!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">试听</label>
                    <div class="controls">
                        <input type="text" id="audition" name="audition" class="form-control input_txt" value="<#if goods??>${goods.audition!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">跳转链接</label>
                    <div class="controls">
                        <input type="text" id="redirectUrl" name="redirectUrl" class="form-control input_txt" value="<#if goods??>${goods.redirectUrl!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程价格</label>
                    <div class="controls">
                        原价: <input type="text" id="originalPrice" name="originalPrice" class="form-control input_txt_small"
                                   value="<#if goods??>${goods.originalPrice!}</#if>" />&nbsp;
                        现价: <input type="text" id="price" name="price" class="form-control input_txt_small"
                                   value="<#if goods??>${goods.price!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">预约礼</label>
                    <div class="controls">
                        <textarea id="appointGift" name="appointGift" class="intro_small" placeholder="请填写预约礼"><#if goods??>${goods.appointGift!}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">到店礼</label>
                    <div class="controls">
                         <textarea id="welcomeGift" name="welcomeGift" class="intro_small" placeholder="请填写到店礼"><#if goods??>${goods.welcomeGift!}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程标签</label>
                    <div class="controls">
                        <textarea id="tags" name="tags" class="intro_small"
                                  placeholder="请填写课程标签,以逗号分隔"><#if goods?? && goods.tags?has_content><#list goods.tags as tag><#if tag_index!=0>,</#if>${tag}</#list></#if></textarea>
                    </div>
                </div>
                <#if goods??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">banner图片</label>
                    <div class="controls">
                        <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_banner" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                        <table class="table img_table">
                            <#if goods?? && goods.bannerPhoto?? && goods.bannerPhoto?has_content>
                                <#list goods.bannerPhoto as banner>
                                    <#if banner_index%3== 0> <tr></#if>
                                    <td class="img_td">
                                        <div class="img_box">
                                            <span class="img_x_alt" id="del_banner" data-file="${banner!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                            <img src="${banner!}" />
                                        </div>
                                    </td>
                                    <#if banner_index%3==2 || !banner_has_next>
                                        <#if goods.bannerPhoto?size % 3 == 0><td></td><td></td></#if>
                                        <#if goods.bannerPhoto?size % 3 == 1><td></td></#if>
                                    </tr>
                                    </#if>
                                </#list>
                            </#if>
                        </table>
                    </div>
                </div>
                </#if>
                <#if goods??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">课程详情图片</label>
                    <div class="controls">
                        <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_detail" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                        <table class="table img_table">
                            <#if goods?? && goods.detail?? && goods.detail?has_content>
                                <#list goods.detail as d>
                                    <#if d_index%3== 0> <tr></#if>
                                    <td class="img_td">
                                        <div class="img_box">
                                            <span class="img_x_alt" id="del_detail" data-file="${d!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                            <img src="${d!}" />
                                        </div>
                                    </td>
                                    <#if d_index%3==2 || !d_has_next>
                                        <#if goods.detail?size % 3 == 0><td></td><td></td></#if>
                                        <#if goods.detail?size % 3 == 1><td></td></#if>
                                    </tr>
                                    </#if>
                                </#list>
                            </#if>
                        </table>
                    </div>
                </div>
                </#if>
            </div>
        </form>
    </div></div></div>
</div>
<div id="uploaderDialog" class="modal fade hide" style="width:550px; height: 300px;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>上传图片</h3>
    </div>
    <div class="modal-body">
        <div style="float: left; width: 280px;">
            <div style="height: 200px; width: 280px;">
                <img id="imgSrc" src="" alt="预览" style="height: 200px; width: 280px;"/>
            </div>
        </div>
        <div style="float: right">
            <div style="display: block;">
                <textarea placeholder="请填写描述" id="uploadDesc" style="resize: none;"></textarea>
            </div>
            <div style="display: block;">
                <a href="javascript:void(0);" class="uploader">
                    <input type="file" name="file" id="uploadFile" accept="image/*" onchange="previewImg(this)">选择素材
                </a>
            </div>
        </div>
        <input type="hidden" id="uploadField" value="photo">
    </div>
    <div class="modal-footer">
        <button title="确认上传" class="uploader" id="upload_confirm">
            <i class="icon-ok"></i>
        </button>
        <button class="uploader" data-dismiss="modal" aria-hidden="true"><i class="icon-trash"></i></button>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $('#info_frm').on('submit', function () {
            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'savegoods.vpage',
                success: function (res) {
                    if (res.success) {
                        alert("保存成功");
                        window.location.href = "info.vpage?gid=" + res.gid;
                    } else {
                        alert("保存失败:" + res.info);
                    }
                },
                error: function (msg) {
                    alert("保存失败！");
                }
            });
            return false;
        });

        $('#save_info').on('click', function () {
            if (confirm("是否确认保存？")) {
                $('#info_frm').submit();
            }
        });

        $('#approve_info').on('click', function () {
            if (confirm("是否确认通过审核并上线？")) {
                var gid = $('#gid').val();
                $.post("approvegoods.vpage", {gid:gid},function(res) {
                   if (res.success) {
                       alert("审核通过");
                       window.location.reload();
                   } else {
                       alert(res.info);
                   }
                });
            }
        });

        $("[id^='del_']").on('click', function() {
            if (!confirm("是否确认删除？")) {
                return false;
            }
            var $this = $(this);
            var field = $this.attr("id").substring("del_".length);
            var file = $(this).data("file");
            var gid = $('#gid').val();
            $.post('deletephoto.vpage', {gid : gid, field: field, file: file}, function(res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        $("[id^='upload_btn_']").on('click', function() {
            var filed = $(this).attr("id").substring("upload_btn_".length);
            $('#uploadFile').val("");
            $('#uploadField').val(filed);
            $('#imgSrc').attr("src", "");
            $('#uploadDesc').val("");
            $('#uploaderDialog').modal("show");
        });

        $('#upload_confirm').on('click', function() {
            // 获取参数
            var field = $('#uploadField').val();
            var desc = $('#uploadDesc').val();
            // 拼formData
            var formData = new FormData();
            var file = $('#uploadFile')[0].files[0];
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('field', field);
            formData.append('desc', desc);
            formData.append('gid', $('#gid').val());
            // 发起请求
            $.ajax({
                url: 'uploadphoto.vpage' ,
                type: 'POST',
                data: formData,
                processData: false,
                contentType: false,
                success: function (res) {
                    if (res.success) {
                        alert("上传成功");
                        window.location.reload();
                    } else {
                        alert(res.info);
                    }
                }
            });
        });
    });

    function previewImg(file) {
        var prevDiv = $('#imgSrc');
        if (file.files && file.files[0]) {
            var reader = new FileReader();
            reader.onload = function (evt) {
                prevDiv.attr("src", evt.target.result);
            };
            reader.readAsDataURL(file.files[0]);
        }
        else {
            prevDiv.html('<img class="img" style="filter:progid:DXImageTransform.Microsoft.AlphaImageLoader(sizingMethod=scale,src=\'' + file.value + '\'">');
        }
    }
</script>
</@layout_default.page>