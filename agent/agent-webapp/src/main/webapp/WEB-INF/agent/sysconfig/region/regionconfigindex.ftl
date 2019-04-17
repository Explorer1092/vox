<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='区域设置' page_num=6>
<link  href="${requestContext.webAppContextPath}/public/js/fancytree/ui.fancytree.min.css" rel="stylesheet" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree-all.min.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/fancytree/jquery.fancytree.filter.js"></script>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well">
            <h2><i class="icon-th-list"></i> 区域设置 </h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>

        <div class="box-content">
            <form class="form-horizontal">
                <fieldset>
                    <div class="control-group">
                        <label class="control-label" for="focusedInput">类型</label>
                        <div class="controls">
                            <select id="product" style="width: 280px">
                                <#list productList as productCard>
                                    <option value="${productCard.key}"> ${productCard.value} </option>
                                </#list>
                            </select>
                            <a id="btnRegionConfigSearch" class="btn btn-info" href="#">
                                <i class="icon-search icon-white"></i>
                                查询
                            </a>
                        </div>
                    </div>
                    <div class="control-group">
                        <label class="control-label" for="selectError1">开通地区</label>
                        <div id="cardregiontree" class="controls" style="width: 280px;height: 400px">
                        </div>
                    </div>
                    <div class="form-actions">
                        <button id="btnRegionConfigSave" type="button" class="btn btn-primary">保存</button>
                    </div>
                </fieldset>
            </form>
        </div>
    </div>

</div>

<script type="text/javascript">
    $(function(){
        $('#btnRegionConfigSearch').live('click',function(){
            var product = $('#product').find('option:selected').val();

            $("#cardregiontree").fancytree({
                source: {
                    url: "loadregionconfig.vpage?product=" + product ,
                    cache:false
                },
                checkbox: true,
                selectMode: 3
            });
        });

        $('#btnRegionConfigSave').live('click',function(){

            if(!confirm("确定要保存设置吗?")){
                return false;
            }

            var product = $('#product').find('option:selected').val();
            var regionList = new Array();

            var regionTree = $("#cardregiontree").fancytree("getTree");
            var regionNodes = regionTree.getSelectedNodes();
            $.map(regionNodes, function(node){
                regionList.push(node.key);
            });

            $.post('saveregionconfig.vpage',{
                product:product,
                regionList:regionList.join(",")
            },function(data){
                if(!data.success){
                    alert(data.info);
                }else{
                    alert(data.info);
                    $('#btnRegionConfigSearch').click();
                }
            });
        });
    });
</script>
</@layout_default.page>
