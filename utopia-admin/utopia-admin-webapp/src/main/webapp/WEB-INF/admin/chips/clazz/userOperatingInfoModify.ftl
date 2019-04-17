<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='用户运营信息编辑' page_num=26>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        编辑用户&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="info_frm" name="info_frm" action="userInfoModifySave.vpage" method="post">
                <#--<input id="productId" name="id" value="${id!}" type="hidden" />-->
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">微信：</span></label>
                            <div class="controls">
                                <input type="hidden" id="clazzId" name="clazzId" class="form-control input_txt"
                                       value="${clazzId!}"/>
                                <input type="hidden" id="userId" name="userId" class="form-control input_txt"
                                       value="${userId!}"/>
                                <input type="text" id="wechatNumber" name="wechatNumber" class="form-control input_txt"
                                       value="${operatingPojo.wechatNumber!}"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">进群：</label>
                            <div class="controls">
                                <select id="joinedGroup" data-init='false' name="joinedGroup"
                                        class="multiple district_select">
                                    <option value="">----空----</option>
                                    <#if joinedGroupOptionList?size gt 0>
                                        <#list joinedGroupOptionList as e >
                                            <option value="${e.value?c}"
                                                    <#if e.selected>selected</#if>>${e.desc!}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学习年限：</label>
                            <div class="controls">
                                <input type="text" id="duration" name="duration" class="form-control input_txt"
                                       value="${operatingPojo.duration!}"/>
                            </div>
                        </div>
                    </div>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
                        <i class="icon-pencil icon-white"></i> 确 定
                    </a>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a title="返回" href="javascript:window.history.back();" class="btn">
                        <i class="icon-share-alt"></i> 取 消
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
//            var joinedGroup = $("#joinedGroup").val();
//            if (joinedGroup == '') {
//                alert("请选择是否进群");
//                return false;
//            }
//            var duration = $("#duration").val();
//            if (duration == '') {
//                alert("请输入学习年限");
//                return false;
//            }
            // 保存商品信息
            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'userInfoModifySave.vpage',
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
        })
    });
</script>

</@layout_default.page>