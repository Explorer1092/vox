<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='添加小程序码' page_num=26>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加小程序码&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="info_frm" name="info_frm" action="save.vpage" method="post">
                <#--<input id="productId" name="id" value="${id!}" type="hidden" />-->
                    <div >
                        <div class="control-group">
                            <label class="col-sm-2 control-label">内容：</label>
                            <div class="controls">
                                <input type="text" id="content" name="content" class="form-control input_txt" placeholder="最多32个字符，支持英文数字和!#$&'()*+,/:;=?@-._~"/>
                            </div>
                            <label class="col-sm-2 control-label">小程序路径：</label>
                            <div class="controls">
                                <select class="form-control" id="path" name="path">
                                    <option value="pages/giftQR/home/index">礼盒卡片</option>
                                    <option value="pages/courseList/index">小程序首页</option>
                                </select>
                            </div>
                        </div>

                    <#--<div class="control-group">-->

                    <#--</div>-->
                    </div>
                    &nbsp;&nbsp;&nbsp;
                    <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
                        <i class="icon-pencil icon-white"></i> 确定(OK)
                    </a>
                    &nbsp;&nbsp;&nbsp;
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
            var content = $("#content").val();
            if (content == '') {
                alert("请输入小程序码内容");
                return false;
            }

            if (content.length > 32){
                alert("小程序码内容最多32个字符");
                return false;
            }

            var regex = /^(\w|\!|\#|\$|\&|\'|\(|\)|\*|\+|\,|\\|\/|\:|\;|\=|\?|\@|\-|\.|\_|\~)+$/;
            if (!regex.test(content)) {
                alert("小程序码内容不合法");
                return false;
            }
            debugger;

            // 保存商品信息
            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'save.vpage',
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