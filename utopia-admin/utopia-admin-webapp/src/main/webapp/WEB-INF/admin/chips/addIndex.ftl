<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='新建' page_num=26>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        新建&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="info_frm" name="info_frm" action="save.vpage" method="post">
                <#--<input id="productId" name="id" value="${id!}" type="hidden" />-->
                    <div >
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置Key：</label>
                            <div class="controls">
                                <input type="text" id="name" name="name" class="form-control input_txt"/>
                            </div>
                            <label class="col-sm-2 control-label">描述：</label>
                            <div class="controls">
                                <input type="text" id="memo" name="memo" class="form-control input_txt"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">配置值：</label>
                            <#--<div class="controls">-->
                                <#--<input type="text" id="value" name="value" class="form-control input_txt"/>-->
                            <#--</div>-->
                            <#--<p>-->
                            <div class="controls">
                                <textarea id="value" name="value"  style="width: 99%; height: 600px;"></textarea>
                            </div>
                            <#--</p>-->
                        </div>
                        <#--<div class="control-group">-->

                        <#--</div>-->
                    </div>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
                        <i class="icon-pencil icon-white"></i> 确定(OK)
                    </a>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a title="返回" href="javascript:window.history.back();" class="btn">
                        <i class="icon-share-alt"></i> 取消(Cancel)
                    </a>
                </form>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $('#save_info').on('click', function () {
            // 做数据校验
            var name = $("#name").val();
            if (name == '') {
                alert("请输入配置项名称");
                return false;
            }
            var value = $("#value").val();
            if (value == '') {
                alert("请输入配置项值");
                return false;
            }
            // 保存商品信息
            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'add.vpage',
                success: function (data) {
                    if (data.success) {
                        alert("保存成功");
                        window.history.back();
                        window.location.reload();
                    } else {
                        alert("保存失败:" + data.info);
                    }
                },
                error: function (msg) {
                    alert("保存失败！");
                }
            });
        });
    });
</script>
</@layout_default.page>