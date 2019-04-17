<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='编辑班级' page_num=26>

<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        编辑班级&nbsp;&nbsp;
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="info_frm" name="info_frm" action="save.vpage" method="post">
                <#--<input id="productId" name="id" value="${id!}" type="hidden" />-->
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">班名(Class)：</span></label>
                            <div class="controls">
                                <input type="hidden" id="clazzId" name="clazzId" value="${clazz.clazzId!}">
                                <input type="text" id="clazzName" name="clazzName" class="form-control input_txt"
                                       value="${clazz.clazzName!}"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">班主任(Teacher)：</label>
                            <div class="controls">
                                <select id="clazzTeacherName" data-init='false' name="clazzTeacherName"
                                        class="multiple district_select">
                                    <option value="">----请选择----</option>
                                    <#if teacherOptionList?size gt 0>
                                        <#list teacherOptionList as e >
                                            <option value="${e.value!}"
                                                    <#if e.selected>selected</#if>>${e.desc!}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">产品(Product)：</label>
                            <div class="controls">
                                <input id="productId" type="hidden" name="productId" value="${clazz.productId!}">
                                <select data-init='false'
                                        class="multiple district_select" disabled="disabled">
                                    <option value="">----请选择----</option>
                                    <#if productOptionList?size gt 0>
                                        <#list productOptionList as e >
                                            <option value="${e.value!}"
                                                    <#if e.selected>selected</#if>>${e.desc!}</option>
                                        </#list>
                                    </#if>
                                </select>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">用户上限(Limitation)：</label>
                            <div class="controls">
                                <input type="text" id="userLimitation" name="userLimitation"
                                       class="form-control input_txt" value="${clazz.userLimitation!}"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">班级类型：</label>
                            <div class="controls">
                                <select id="clazzType" data-init='false' name="clazzType"
                                        class="multiple district_select">
                                    <#if clazzTypeOptionList?size gt 0>
                                        <#list clazzTypeOptionList as e >
                                            <option value="${e.name()!}"
                                                    <#if clazz?? && clazz.type ?? && e.name() == clazz.type>selected</#if>>${e.description!}</option>
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
            var clazzId = $("#clazzId").val();
            if (clazzId == '') {
                alert("没有班级id");
                return false;
            }
            // 做数据校验
            var clazzName = $("#clazzName").val();
            if (clazzName == '') {
                alert("请输入班级");
                return false;
            }
            var clazzTeacherName = $("#clazzTeacherName").val();
            if (clazzTeacherName == '') {
                alert("请选择班主任");
                return false;
            }
            var productId = $("#productId").val();
            console.log(productId)
            if (productId == '') {
                alert("请选择产品");
                return false;
            }

            var userLimitation = $("#userLimitation").val();
            if (userLimitation == '') {
                alert("请输入班级");
                return false;
            }

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
        })
    });
</script>

</@layout_default.page>