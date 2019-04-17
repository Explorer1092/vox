<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='合并班级' page_num=26>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        合并班级&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="info_frm" name="info_frm" action="mergeSave.vpage" method="post">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">待合并班级(Self)：</span></label>
                            <div class="controls">
                                <input type="hidden" id="clazzId" name="clazzId" class="form-control input_txt"
                                       value="${clazzId!}"/>
                                <input type="text" id="clazzName" name="clazzName" class="form-control input_txt"
                                       disabled value="${clazzName!}"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">目标班级(Aim)：</label>
                            <div class="controls">
                                <select id="aimClazzId" data-init='false' name="aimClazzId"
                                        class="multiple district_select">
                                    <option value="">----请选择----</option>
                                    <#if aimClazzList?size gt 0>
                                        <#list aimClazzList as e >
                                            <option value="${e.value!}"
                                                    <#if e.selected>selected</#if>>${e.desc!}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                    </div>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
                        <i class="icon-pencil icon-white"></i> 确定(OK)
                    </a>
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
                    <a title="返回" href="javascript:window.history.back();" class="btn ">
                        <i class="icon-share-alt "></i> 取消(Cancel)
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
            var aimClazzId = $("#aimClazzId").val();
            if (aimClazzId == '') {
                alert("请选择目标班级");
                return false;
            }
            // 保存商品信息
            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'mergeSave.vpage',
                success: function (data) {
                    if (data.success) {
                        alert("保存成功");
                        window.history.back();
                        window.location.reload();
                    } else {
                        alert("保存失败！" + data.info);
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