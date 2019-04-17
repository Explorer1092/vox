<#-- @ftlvariable name="productTypeList" type="java.util.List" -->
<#-- @ftlvariable name="exRegionList" type="java.util.List<com.voxlearning.utopia.service.region.api.entities.extension.ExRegion>" -->
<#-- @ftlvariable name="conditionMap" type="java.util.Map" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">

    <ul class="breadcrumb">
        <li><a href="createregionproducttag.vpage">产品类型管理</a><span class="divider">|</span></li>
        <li><a href="regionproducthomepage.vpage">增加产品区域</a><span class="divider">|</span></li>
        <li><span>查询产品区域</span><span class="divider">|</span></li>
    </ul>

    <fieldset><legend>查询产品区域</legend></fieldset>
    <form action="?" method="post">
        <ul class="inline">
            <li>
                <label>区域编码：<input name="regionCode" value="${(conditionMap.regionCode)!}"/></label>
            </li>
            <li>
                <label>产品类型：<select name="regionTag">
                    <option value="">全部</option>
                    <#if productTypeList?has_content>
                        <#list productTypeList as productType>
                            <option value="${productType!}" <#if (productType = conditionMap.regionTag)!false>selected="selected"</#if>>${productType!}</option>
                        </#list>
                    </#if>
                </select></label>
            </li>
        </ul>
        <ul class="inline">
            <li>
                <button class="btn btn-primary" id="submit" type="submit">提交</button>
            </li>
        </ul>
    </form>

    <div>
        <ul class="inline">
            <li>
                <button id="reload_all" class="btn btn-danger">重新加载产品区域</button>
            </li>
        </ul>
    </div>

    <table class="table table-hover table-striped table-bordered">
        <#if exRegionList?has_content>
            <tr>
                <th></th>
                <th style="width: 150px;">省</th>
                <th style="width: 150px;">市</th>
                <th style="width: 150px;">区</th>
                <th>产品类型</th>
            </tr>
            <#list exRegionList as exRegion>
                    <tr>
                        <td>${exRegion_index + 1}</td>
                        <td>${exRegion.provinceName!}(${exRegion.provinceCode!})</td>
                        <td>${exRegion.cityName!}(${exRegion.cityCode!})</td>
                        <td>${exRegion.countyName!}(${exRegion.countyCode!})</td>
                        <td>
                            <#if exRegion.tags?has_content>
                                <#list exRegion.tags as tag>
                                    <span>${tag!} <a id="delete_region_product_${exRegion.countyCode!}" data-region_code="${exRegion.countyCode!}" data-tag="${tag!}" href="javascript:void(0)">删除</a><#if tag_has_next && tag_index % 2 = 0>, </#if><#if tag_has_next && tag_index % 2 = 1><br/></#if></span>
                                </#list>
                            </#if>
                        </td>
                    </tr>
            </#list>
        </#if>
    </table>
</div>
<script>
    $(function() {

        $('a[id^="delete_region_product_"]').click(function() {
            var $this = $(this);
            var postData = {
                regionCode : $this.data('region_code'),
                regionTag  : $this.data('tag')
            };
            $.get("deleteregionproduct.vpage", postData, function(data) {
                //alert(data.info);
                if (data.success) {
                    $this.closest('span').remove();
                }
            });
        });

        $('#reload_all').click(function() {
            if(confirm('此操作会根据数据库中数据重新加载产品区域信息，确认继续？')) {
                $.post('reloadregionproduct.vpage', null, function(data) {
                    alert(data.info);
                    if (data.success) {
                        $('#submit').trigger('click');
                    }
                });
            }
        });

    });
</script>
</@layout_default.page>