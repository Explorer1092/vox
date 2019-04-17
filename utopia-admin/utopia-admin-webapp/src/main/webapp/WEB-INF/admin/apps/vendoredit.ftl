<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用开发商管理" page_num=10>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>

<div id="main_container" class="span9">
    <form class="form-horizontal" action="updatevendor.vpage" method="post" id="vendor_edit_form">
        <input type="hidden" id="vendorId" name="vendorId" value="${appVendor.id}"/>
        <div class="control-group">
            <label class="col-sm-2 control-label">公司名(中文)&nbsp;</label>
            <div class="controls">
                <input type="text" id="vendorCname" name="vendorCname" class="form-control" value="${appVendor.cname!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">公司名(英文)&nbsp;</label>
            <div class="controls">
                <input type="text" id="vendorEname" name="vendorEname" class="form-control" value="${appVendor.ename!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">公司名(略称)&nbsp;</label>
            <div class="controls">
                <input type="text" id="vendorSname" name="vendorSname" class="form-control" value="${appVendor.shortName!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">公司地址&nbsp;</label>
            <div class="controls">
                <input type="text" id="vendorAddress" name="vendorAddress" class="form-control" value="${appVendor.address!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">网址&nbsp;</label>
            <div class="controls">
                <input type="text" id="webSite" name="webSite" class="form-control" value="${appVendor.webSite!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">LogoUrl&nbsp;</label>
            <div class="controls">
                <input type="text" id="logoUrl" name="logoUrl" class="form-control" value="${appVendor.logoUrl!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">第一联系人姓名&nbsp;</label>
            <div class="controls">
                <input type="text" id="contact1Name" name="contact1Name" class="form-control" value="${appVendor.contact1Name!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">第一联系人电话&nbsp;</label>
            <div class="controls">
                <input type="text" id="contact1Tel" name="contact1Tel" class="form-control" value="${appVendor.contact1Tel!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">第一联系人手机&nbsp;</label>
            <div class="controls">
                <input type="text" id="contact1Mob" name="contact1Mob" class="form-control" value="${appVendor.contact1Mob!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">第一联系人邮箱&nbsp;</label>
            <div class="controls">
                <input type="text" id="contact1Email" name="contact1Email" class="form-control" value="${appVendor.contact1Email!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">第二联系人姓名&nbsp;</label>
            <div class="controls">
                <input type="text" id="contact2Name" name="contact2Name" class="form-control" value="${appVendor.contact2Name!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">第二联系人电话&nbsp;</label>
            <div class="controls">
                <input type="text" id="contact2Tel" name="contact2Tel" class="form-control" value="${appVendor.contact2Tel!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">第二联系人手机&nbsp;</label>
            <div class="controls">
                <input type="text" id="contact2Mob" name="contact2Mob" class="form-control" value="${appVendor.contact2Mob!}"/>
            </div>
        </div>
        <div class="control-group">
            <label class="col-sm-2 control-label">第二联系人邮箱&nbsp;</label>
            <div class="controls">
                <input type="text" id="contact2Email" name="contact2Email" class="form-control" value="${appVendor.contact2Email!}"/>
            </div>
        </div>
        <div class="control-group">
            <div class="controls">
                <button type="button" id="btn_cancel" name="btn_cancel" class="btn">取消</button> &nbsp;&nbsp; <button type="submit" class="btn btn-primary">保存</button>
            </div>
        </div>
    </form>
</div>
<script type="text/javascript">

    $(function() {
        $('#btn_cancel').on('click', function () {
            $("#vendor_edit_form").attr("action", "vendorindex.vpage");
            $("#vendor_edit_form").submit();
        });
    });

</script>
</@layout_default.page>