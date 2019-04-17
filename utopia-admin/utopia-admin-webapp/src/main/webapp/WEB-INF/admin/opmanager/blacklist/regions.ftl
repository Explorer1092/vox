<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="第三方应用管理" page_num=9>
<script src="//cdn.17zuoye.com/public/plugin/jquery/jquery-1.7.1.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/bootstrap.js"></script>
<link href="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.min.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/chosen.jquery.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/chosen_v1.10/prism.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>
<link href="${requestContext.webAppContextPath}/public/css/advertisement/advertisement.css" rel="stylesheet">
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>

<div id="main_container" class="span9">
    <div>
        <ul class="nav nav-tabs" role="tablist">
            <li role="presentation"><a href="/opmanager/blacklist/index.vpage">个人及学校黑名单</a></li>
            <li role="presentation" class="active"><a href="/opmanager/blacklist/regions.vpage">地区黑名单</a></li>
        </ul>
    </div>
    <div>
        <ul class="inline">
            <li>
                <select id="blacklistType" name="blacklistType" style="width:250px">
                    <option value="PaymentBlackListRegion">学生黑名单地区配置</option>
                    <option value="ParentPaymentBlackListRegion">家长黑名单地区配置</option>
                    <option value="PaymentGrayListRegion">灰名单地区配置</option>
                </select>
            </li>
    </div>
    <div id="stu_target_region_modal" class="span7" style="display: none;">
        <div id="stuRegionTree" class="controls"
             style="width:60%; height: 410px; float: left; display: inline;"></div>
    </div>
    <div id="parent_target_region_modal" class="span7" style="display: none;">
        <div id="parentRegionTree" class="controls"
             style="width:60%; height: 410px; float: left; display: inline;"></div>
    </div>
    <div id="gray_region_modal" class="span7" style="display: none;">
        <div id="grayRegionTree" class="controls"
             style="width:60%; height: 410px; float: left; display: inline;"></div>
    </div>
    <div class="control-group">
        <div class="controls">
            <button type="button" id="btn_save" name="btn_save" class="btn btn-primary">保存</button>
        </div>
    </div>
</div>
<script type="text/javascript">
    var parentRegionModel = $('#parent_target_region_modal');
    var stuRegionModel = $('#stu_target_region_modal');
    var grayRegionModel = $('#gray_region_modal');
    var stuData = ${studentBlacklistRegion!};
    var parentData = ${parentBlacklistRegion!};
    var grayData = ${grayRegion!};
    function initDateTree(node, data) {
        $(node).fancytree({
            source: data,
            checkbox: true,
            selectMode: 3
        });

    }
    $(function () {

        initDateTree($('#stuRegionTree'), stuData);
        stuRegionModel.show();

        $("#blacklistType").change(function () {
            if ($(this).val() == 'ParentPaymentBlackListRegion') {
                initDateTree($('#parentRegionTree'), parentData);
                parentRegionModel.show();
                stuRegionModel.hide();
                grayRegionModel.hide();
            } else if($(this).val() == 'PaymentBlackListRegion'){
                initDateTree($('#stuRegionTree'), stuData);
                stuRegionModel.show();
                parentRegionModel.hide();
                grayRegionModel.hide();
            }else if($(this).val() == 'PaymentGrayListRegion'){
                initDateTree($('#grayRegionTree'), grayData);
                grayRegionModel.show();
                stuRegionModel.hide();
                parentRegionModel.hide();
            }
        });


        $('#btn_save').live('click', function () {

            if (!confirm("确定要保存设置吗?")) {
                return false;
            }
            $("#btn_save").attr("disabled", true);

            var product = $('#blacklistType').find('option:selected').val();
            var regionList = new Array();

            var regionTree;
            if (product == "PaymentBlackListRegion") {
                regionTree = $("#stuRegionTree").fancytree("getTree");
            } else if (product == "ParentPaymentBlackListRegion") {
                regionTree = $("#parentRegionTree").fancytree("getTree");
            }else if(product == "PaymentGrayListRegion"){
                regionTree = $("#grayRegionTree").fancytree("getTree");
            }
            var regionNodes = regionTree.getSelectedNodes();
            $.map(regionNodes, function (node) {
                regionList.push(node.key);
            });

            $.post('saveregionconfig.vpage', {
                product: product,
                regionList: regionList.join(",")
            }, function (data) {
                if (data.success) {
                    alert("更新成功,区域配置五分钟之后生效,部分用户状态由于缓存会延迟生效(最长不超过一个小时)");
                } else {
                    alert(data.info);
                }
                $("#btn_save").attr("disabled", false);
            });
        });
    });

</script>
</@layout_default.page>