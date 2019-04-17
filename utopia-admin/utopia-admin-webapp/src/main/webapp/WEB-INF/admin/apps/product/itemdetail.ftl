<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑" page_num=10>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script type="text/javascript" src="${requestContext.webAppContextPath}/public/js/form/jquery-form.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/mizar/mizar.css" rel="stylesheet">
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        添加/编辑子商品&nbsp;&nbsp;
        <a title="返回" href="javascript:window.history.back();" class="btn">
            <i class="icon-share-alt"></i> 返  回
        </a>
        <a title="保存" href="javascript:void(0);" class="btn btn-primary" id="save_info">
            <i class="icon-pencil icon-white"></i> 保  存
        </a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
        <div class="well">
        <form id="info_frm" name="info_frm" enctype="multipart/form-data" action="saveitem.vpage" method="post">
            <input id="id" name="id" value="${id!}" type="hidden" />
            <div class="form-horizontal">
                <div class="control-group">
                    <label class="col-sm-2 control-label">名称<span style="font-size: 12px;color: red;">*必填</span></label>
                    <div class="controls">
                        <input type="text" id="name" name="name" class="form-control input_txt" value="<#if item??>${item.name!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">描述</label>
                    <div class="controls">
                        <textarea id="desc" name="desc" class="intro_small"  placeholder="请填写描述"><#if item??>${item.desc!}</#if></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">产品类型</label>
                    <div class="controls">
                        <select name="productType">
                            <#list productTypes as s>
                                <option value="${s.name()!}" <#if s.name()==(item.productType)!>selected</#if>>${s.name()!}</option>
                            </#list>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">销售方式</label>
                    <div class="controls">
                        <select name="salesType">
                            <#list saleTypes as s>
                                <option value="${s.name()!}" <#if s.name()==(item.salesType)!>selected</#if>>${s.name()!}</option>
                            </#list>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">激活类型</label>
                    <div class="controls">
                        <select name="activeType">
                            <#list activeTypes as s>
                                <option value="${s.name()!}" <#if s.name()==(item.activeType)!>selected</#if>>${s.name()!}</option>
                            </#list>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">摊销类型</label>
                    <div class="controls">
                        <select name="amortizeType">
                            <#list amortizeTypes as s>
                                <option value="${s.name()!}" <#if s.name()==(item.amortizeType)!>selected</#if>>${s.name()!}</option>
                            </#list>
                        </select>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">有效期</label>
                    <div class="controls">
                        <input type="text" id="period" name="period" class="form-control input_txt" value="<#if item??>${item.period!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">价格</label>
                    <div class="controls">
                        <input type="text" id="originalPrice" name="originalPrice" class="form-control input_txt" value="<#if item??>${item.originalPrice!}</#if>" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">是否允许重复购买</label>
                    <div class="controls">
                        <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-bottom: 0;vertical-align: middle;cursor: pointer;"><input type="radio" name="repurchaseAllowed"  style="position: relative;top:-3px;" <#if item??><#if item.repurchaseAllowed>checked="checked"</#if><#else>checked="checked"</#if> /> 是</label>
                        <label class="radio-inline" style="position:relative;top:4px;display: inline-block;margin-left: 20px;margin-bottom: 0;vertical-align: middle;cursor: pointer;"><input type="radio" <#if (!item.repurchaseAllowed)!false>checked="checked"</#if> value="false" name="repurchaseAllowed" style="position: relative;top:-3px;" /> 否</label>
                    </div>
                </div>
                <div class="control-group">
                    <label class="col-sm-2 control-label">业务方ID</label>
                    <div class="controls">
                        <input type="text" id="appItemId" name="appItemId" class="form-control input_txt" value="<#if item??>${item.appItemId!}</#if>" />
                    </div>
                </div>
            </div>
        </form>
        </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {
        $('#info_frm').on('submit', function () {
            // 做数据校验
            var name = $("#name").val();
            if (name == '') {
                alert("请输入名称");
                return false;
            }
            var originalPrice = $("#originalPrice").val();
            if (originalPrice == '') {
                alert("请输入价格");
                return false;
            }
            var desc = $("#desc").val();
            if (desc == '') {
                alert("请输入描述");
                return false;
            }
            $('#info_frm').ajaxSubmit({
                type: 'post',
                url: 'saveitem.vpage',
                success: function (res) {
                    if (res.success) {
                        alert("保存成功");
                        window.location.href = "itemdetail.vpage?id=" + res.id;
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
    });
</script>
</@layout_default.page>