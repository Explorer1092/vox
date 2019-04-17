<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑品牌" page_num=17>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑品牌&nbsp;&nbsp;
        <a title="返回" href="javascript:window.history.back();" class="btn">
            <i class="icon-share-alt"></i> 返  回
        </a>
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
            <i class="icon-pencil icon-white"></i> 保  存
        </a>
        <#if brand??>
        <div style="float: right;">
            <a title="添加" href="${requestContext.webAppContextPath}/mizar/shop/info.vpage?bid=${bid!}" class="btn btn-success">
                <i class="icon-plus icon-white"></i> 添加机构
            </a>
            <a title="查看关联机构" href="${requestContext.webAppContextPath}/mizar/shop/index.vpage?bid=${bid!}" class="btn btn-warning">
                <i class="icon-leaf icon-white"></i> 查看关联机构
            </a>
        </div>
        </#if>
    </legend>
    <div class="row-fluid"><div class="span12"><div class="well">
        <form id="info_frm" name="info_frm" enctype="multipart/form-data" action="savebrand.vpage" method="post">
            <input id="bid" name="bid" value="${bid}" type="hidden">
            <div class="form-horizontal">
                <div class="control-group">
                    <label class="col-sm-2 control-label">品牌名称</label>
                    <div class="controls">
                        <input type="text" id="bname" name="bname" class="form-control input_txt" value="<#if brand??>${brand.brandName!}</#if>"/>
                    </div>
                </div>
                <#if brand??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">Logo</label>
                    <div class="controls">
                        <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_logo" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                        <table class="table img_table">
                            <#if brand?? && brand.brandLogo?has_content>
                            <tr>
                                <td>
                                <div class="img_box">
                                    <span class="img_x_alt" id="del_logo" data-file="${brand.brandLogo!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                    <img <#if brand??>src="${brand.brandLogo!}" </#if> />
                                </div>
                                </td>
                            </tr>
                            </#if>
                        </table>
                    </div>
                </div>
                </#if>
                <div class="control-group">
                    <label class="col-sm-2 control-label">品牌介绍</label>
                    <div class="controls">
                        <textarea id="intro" name="intro" class="intro" placeholder="请填写品牌介绍"><#if brand??>${brand.introduction}</#if></textarea>
                    </div>
                </div>
                <#if brand??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">中心图片</label>
                    <div class="controls">
                        <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_photo" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                        <table class="table img_table">
                            <#if brand?? && brand.brandPhoto?? && brand.brandPhoto?has_content>
                                <#list brand.brandPhoto as p>
                                    <#if p_index%3== 0> <tr></#if>
                                    <td class="img_td">
                                        <div class="img_box">
                                            <span class="img_x_alt" id="del_photo" data-file="${p!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                            <img src="${p!}" />
                                        </div>
                                    </td>
                                    <#if p_index%3==2 || !p_has_next>
                                        <#if brand.brandPhoto?size % 3 == 0><td></td><td></td></#if>
                                        <#if brand.brandPhoto?size % 3 == 1><td></td></#if>
                                    </tr>
                                    </#if>
                                </#list>
                            </#if>
                        </table>
                    </div>
                </div>
                </#if>
                <div class="control-group">
                    <label class="col-sm-2 control-label">创立时间</label>
                    <div class="controls">
                        <input type="text" id="establish" name="establish" class="form-control input_txt" value="<#if brand??>${brand.establishment!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">品牌规模</label>
                    <div class="controls">
                        <input type="text" id="scale" name="scale" class="form-control input_txt" value="<#if brand??>${brand.shopScale!}</#if>" />
                    </div>
                </div>
                <#if brand??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">师资力量</label>
                    <div class="controls">
                        <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_faculty" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                        <table class="table img_table">
                        <#if brand?? && brand.faculty?? && brand.faculty?has_content>
                            <#list brand.faculty as f>
                                <#if f_index%3== 0> <tr></#if>
                                <td class="img_td">
                                    <div class="img_box">
                                        <span class="img_x_alt" id="del_faculty" data-file="${f.photo!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                        <img src="${f.photo!}" />
                                    </div>
                                    <div>
                                        ${f.name!'-'} &nbsp; ${f.experience!0}年教龄 &nbsp; ${f.description!'-'}
                                        <a href="javascript:void(0);" class="edit-faculty" data-img="${f.photo!}" data-index="${f_index}" data-name="${f.name!''}"  data-exp="${f.experience!''}"  data-desc="${f.description!''}">编辑</a>
                                    </div>
                                </td>
                                <#if f_index%3==2 || !f_has_next>
                                    <#if brand.faculty?size % 3 == 0><td></td><td></td></#if>
                                    <#if brand.faculty?size % 3 == 1><td></td></#if>
                                </tr>
                                </#if>
                            </#list>
                        </#if>
                        </table>
                    </div>
                </div>
                </#if>
                <#if brand??>
                <div class="control-group">
                    <label class="col-sm-2 control-label">获奖证书</label>
                    <div class="controls">
                        <a class="btn btn-info" href="javascript:void(0);" id="upload_btn_certification" style="margin-bottom: 5px;"> <i class="icon-picture icon-white"></i> 上  传</a>
                        <table class="table img_table">
                            <#if brand?? && brand.certificationPhotos?? && brand.certificationPhotos?has_content>
                                <#list brand.certificationPhotos as cer>
                                    <#if cer_index%3== 0> <tr></#if>
                                    <td class="img_td">
                                        <div class="img_box">
                                            <span class="img_x_alt" id="del_certification" data-file="${cer!}"><img src="${requestContext.webAppContextPath}/public/img/x_alt.png"/></span>
                                            <img src="${cer!}" />
                                        </div>
                                    </td>
                                    <#if cer_index%3==2 || !cer_has_next>
                                        <#if brand.certificationPhotos?size % 3 == 0><td></td><td></td></#if>
                                        <#if brand.certificationPhotos?size % 3 == 1><td></td></#if></tr>
                                    </#if>
                                </#list>
                            </#if>
                        </table>
                    </div>
                </div>
                </#if>
                <div class="control-group">
                    <label class="col-sm-2 control-label">获奖证书描述</label>
                    <div class="controls">
                        <textarea id="cerName" name="cerName" class="intro"  placeholder="请填写获奖证书描述"><#if brand??>${brand.certificationName!}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">品牌特点</label>
                    <div class="controls">
                        <textarea id="points" name="points" class="intro_small"
                                  placeholder="请填写品牌特点,以逗号分隔"><#if brand?? && brand.points?has_content><#list brand.points as s><#if s_index!=0>,</#if>${s}</#list></#if></textarea>
                    </div>
                </div>
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
            <div style="width:200px;" id="uploadHint">
                姓名：&nbsp;<input placeholder="如：李安" id="facultyName" type="text" class="input-small" /><br/>
                教龄：&nbsp;<input placeholder="如：5" id="facultyExp" type="text" class="input-small"/><br/>
                描述：&nbsp;<input placeholder="如：英语讲师" id="facultyDesc" type="text" class="input-small"/><br/>
            </div>
            <div style="display: none;" id="uploadDesc">
                <textarea placeholder="请填写描述" id="uploadDesc" style="resize: none;" rows="3"></textarea>
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
<div id="editDialog" class="modal fade hide" style="width:550px; height: 300px;">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
        <h3>师资力量编辑</h3>
    </div>
    <input id="index" type="hidden">
    <div class="modal-body">
        <div style="float: left; width: 280px;">
            <div style="height: 200px; width: 280px;">
                <img id="e-img" src="" alt="预览" style="height: 200px; width: 280px;"/>
            </div>
        </div>
        <div style="float: right">
            <div style="width:200px;" id="uploadHint">
                姓名：&nbsp;<input placeholder="如：李安" id="e-name" type="text" class="input-small" /><br/>
                教龄：&nbsp;<input placeholder="如：5" id="e-exp" type="text" class="input-small"/><br/>
                描述：&nbsp;<input placeholder="如：英语讲师" id="e-desc" type="text" class="input-small"/><br/>
            </div>
        </div>
    </div>
    <div class="modal-footer">
        <button title="确认修改" class="uploader" id="edit_confirm">
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
                url: 'savebrand.vpage',
                success: function (res) {
                    if (res.success) {
                        alert("保存成功");
                        window.location.href = 'info.vpage?bid=' + res.bid;
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

        $("[id^='del_']").on('click', function() {
            if (!confirm("是否确认删除？")) {
                return false;
            }
            var $this = $(this);
            var field = $this.attr("id").substring("del_".length);
            var file = $(this).data("file");
            var bid = $('#bid').val();
            $.post('deletephoto.vpage', {bid : bid, field: field, file: file}, function(res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
                }
            });
        });

        $("[id^='upload_btn_']").on('click', function() {
            var field = $(this).attr("id").substring("upload_btn_".length);
            if (field == 'faculty') {
                $('#uploadHint').show();
                $('#uploadDesc').hide();
            } else {
                $('#uploadHint').hide();
                $('#uploadDesc').show();
            }
            $('#uploadFile').val("");
            $('#uploadField').val(field);
            $('#imgSrc').attr("src", "");
            $('#uploadDesc').val("");
            $('#facultyName').val("");
            $('#facultyExp').val("");
            $('#facultyDesc').val("");
            $('#uploaderDialog').modal("show");
        });

        $('#upload_confirm').on('click', function() {
            // 获取参数
            var field = $('#uploadField').val();
            var desc = $('#uploadDesc').val();
            var fname = $('#facultyName').val();
            var fexp = $('#facultyExp').val();
            var fdesc = $('#facultyDesc').val();
            // 拼formData
            var formData = new FormData();
            var file = $('#uploadFile')[0].files[0];
            formData.append('file', file);
            formData.append('file_size', file.size);
            formData.append('file_type', file.type);
            formData.append('field', field);
            formData.append('desc', desc);
            formData.append('fname', fname);
            formData.append('fexp', fexp);
            formData.append('fdesc', fdesc);
            formData.append('bid', $('#bid').val());
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

        $('.edit-faculty').on('click', function() {
            var $this = $(this);
            var index = $this.data().index;
            var name = $this.data().name;
            var exp = $this.data().exp;
            var desc = $this.data().desc;
            var img =$this.data().img;
            $('#e-img').attr("src", img);
            $('#e-name').val(name);
            $('#e-exp').val(exp);
            $('#e-desc').val(desc);
            $('#index').val(index);
            $('#editDialog').modal('show');
        });

        $('#edit_confirm').on('click', function() {
            var $this = $(this);
            var index = $this.data().index;
            var name = $this.data().name;
            var exp = $this.data().exp;
            var desc = $this.data().desc;
            var img =$this.data().img;
            $('#e-img').attr("src", img);
            var data = {
                bid: $('#bid').val(),
                fname: $('#e-name').val(),
                fexp: $('#e-exp').val(),
                fdesc: $('#e-desc').val(),
                index: $('#index').val()
            };
            var bid = $('#bid').val();
            $.post('editfaculty.vpage', data, function(res) {
                if (res.success) {
                    window.location.reload();
                } else {
                    alert(res.info);
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