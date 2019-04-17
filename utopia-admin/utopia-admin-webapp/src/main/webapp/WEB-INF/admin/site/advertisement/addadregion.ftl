<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑城市校区" page_num=4>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<link  href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" >
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div id="main_container" class="span9">
    <legend>
        添加/编辑城市校区:<#if ad??>${ad.name!}</#if>
    </legend>
    <div class="row-fluid" style="height: 500px">
        <div class="span6">
            <div class="well">
                <div class="form-horizontal">
                    <div style="height: 600px; overflow: visible; padding: 15px">
                        <div class="control-group">
                            <label class="col-sm-2 control-label"><strong>分区名称</strong></label>
                            <div class="controls">
                                <input type="text" id="name" width="25px" value="<#if region??>${region.districtName!}</#if>"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label"><strong>广告单价</strong></label>
                            <div class="controls">
                                <input type="text" id="price" width="25px" value="<#if region??>${region.price!}</#if>"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label"><strong>联系电话</strong></label>
                            <div class="controls">
                                <input type="text" id="contact_phone" width="25px" value="<#if region??>${region.contactPhone!}</#if>"/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label"><strong>市区</strong></label>
                            <div class="controls">
                                <input type="text" id="regionCode" width="25px" value="<#if region??>${region.regionCode!}</#if>" placeholder="区域Id英文逗号分隔"/>
                                <#--<div id="regiontree" class="sampletree" style="width:220px; height: 445px">-->
                                <#--</div>-->
                                <#--<input type="hidden" id="regionCode" value="<#if region??>${region.regionCode!}</#if>">-->
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <a type="button" class="btn btn-default" data-dismiss="modal" href="adregionindex.vpage?adId=<#if ad??>${ad.id!}</#if>">取消</a>
                        <button id="add_ad_region_btn" type="button" class="btn btn-primary">保存</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<input type="hidden" id="id" value="${id!}">
<input type="hidden" id="adId" value="<#if ad??>${ad.id!}</#if>">
<script type="text/javascript">

    function validateInput(name, price, contactPhone, regionCode) {
        if(name == '') {
            alert('分区名称不能为空！');
            return false;
        }
        if(price == '' || !$.isNumeric(price)) {
            alert('广告单价不能为空且必须为数字类型！');
            return false;
        }
        if(contactPhone == '') {
            alert('联系电话不能为空！');
            return false;
        }
        if(regionCode == '') {
            alert('市区不能为空！');
            return false;
        }
        return true;
    }

    function getSelectedRegionCode(){
        var regionTree = $("#regiontree").fancytree("getTree");
        var regionNodes = regionTree.getSelectedNodes();
        if(regionNodes == null || regionNodes == "undefined") return null;
        var regionNodes = $.map(regionNodes, function(node){
            return  node.key;
        });
        return regionNodes.join(",");
    }

    $(function() {
        var selectMode = 1;
        if($("#id").val() == '') {
            selectMode = 2;
        }
        $("#regiontree").fancytree({
            extensions: ["filter"],
            source: {
                url: "loadregion.vpage",
                cache:true
            },
            checkbox: true,
            selectMode: selectMode,

            init: function(event, data, flag) {
                var tree = $("#regiontree").fancytree("getTree");
                tree.visit(function(node){
                    if (node.key == $("#regionCode").val()) {
                        node.setSelected(true);
                        node.setActive();
                    }
                });
            }
        });

        $("#add_ad_region_btn").on("click",function(){
            var id = $("#id").val();
            var adId = $("#adId").val();
            var name = $("#name").val().trim();
            var price = $("#price").val().trim();
            var contactPhone = $("#contact_phone").val().trim();
//            var regionCode = getSelectedRegionCode();
            var regionCode = $("#regionCode").val().trim();
            if(!validateInput(name, price, contactPhone, regionCode)) {
                return false;
            }
            var param = {};
            param['adId'] = parseInt(adId);
            param['name'] = name;
            param['price'] = parseFloat(price);
            param['contactPhone'] = contactPhone;
            param['regionCode'] = regionCode;
            var postUrl = '';
            if(id != '') {
                postUrl = 'updateadregion.vpage';
                param['id'] = parseInt(id);
            } else {
                postUrl = 'saveadregion.vpage';
            }
            $.post(postUrl, param, function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    window.location.href = 'adregionindex.vpage?adId='+adId;
                }
            });
        });

    });

</script>
</@layout_default.page>