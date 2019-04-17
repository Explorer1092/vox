<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="标签管理" page_num=24>
<style>

    span {
        margin-left: 5px;
    }

</style>

<div id="main_container" class="span9" style="font-size: 14px">

    <h3>用户标签列表</h3>

    <#-- todo by lei.liu 修改的需求量不大 -->
    <#--<div style="margin: 10px 0">-->
        <#--<button class="btn btn-success">追加标签</button>-->
    <#--</div>-->

    <#if targetTagConfigList ?? && targetTagConfigList?size gt 0 >
        <table class="table table-striped table-bordered">
            <thead>
                <tr>
                    <th style="text-align: center">标签名称</th>
                    <th style="text-align: center">应用层面</th>
                    <th style="text-align: center">平台层面</th>
                    <th style="text-align: center">产品层面</th>
                <#-- todo by lei.liu 修改的需求量不大 -->
                <#--<th style="text-align: center">修改</th>-->
                </tr>
            </thead>
            <tbody id="tbody">
                <#list targetTagConfigList as targetTagConfig>
                <tr>
                    <td>
                        <input type="hidden" name="targetIds" value="${targetTagConfig.id}">
                        ${targetTagConfig.name}
                    </td>
                    <td>
                        <#if targetTagConfig.appStrategies?has_content>
                            <#list targetTagConfig.appStrategies as strategie>
                                <#list strategie.tags as tag>
                                    <span>${STRATEGY_NAME[strategie.strategy]}</span>
                                    <span>${tag.desc}</span>
                                    <#if tag_has_next>
                                        <br>
                                    </#if>
                                </#list>
                            </#list>
                        </#if>
                    </td>
                    <td>
                        <#if targetTagConfig.platformStrategies?has_content>
                            <#list targetTagConfig.platformStrategies as strategie>
                                <#list strategie.tags as tag>
                                    <span>${STRATEGY_NAME[strategie.strategy]}</span>
                                    <span>${tag.desc}</span>
                                    <#if tag_has_next>
                                        <br>
                                    </#if>
                                </#list>
                            </#list>
                        </#if>
                    </td>
                    <td>
                        <#if targetTagConfig.productStrategy?has_content>
                            <#list targetTagConfig.productStrategy?keys as productstrategy>
                                <#list targetIdProductStrategyProduct[targetTagConfig.id][productstrategy] as product>
                                    <span style="display:inline-block;min-width: 160px;text-align: center">${PRODUCT_NAME[product]}</span>
                                    <span>${STRATEGY_NAME[productstrategy]}</span>
                                    （
                                    <#list targetTagConfig.products[product] as strategie>
                                        <#list strategie.tags as tag>
                                            <span>${STRATEGY_NAME[strategie.strategy]}</span>
                                            <span>${tag.desc}</span>
                                        </#list>
                                    </#list>
                                    ）
                                </#list>
                            </#list>
                        </#if>
                    </td>

                    <#-- todo by lei.liu 修改的需求量不大 -->
                    <#--<td>-->
                        <#--<button class="btn btn-default" data-targetid="competitionIsland0023">编辑</button>-->
                        <#--<button class="btn btn-danger" data-targetid="competitionIsland0023">删除</button>-->
                    <#--</td>-->
                </tr>
                </#list>
            </tbody>
        </table>
    </#if>

</div>
<script>
    $(function () {
    });
</script>
</@layout_default.page>