<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/datepicker/WdatePicker.js"></script>
<style>
    input {width: 100px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">SKU管理</span></legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>课程ID&nbsp;
                        <input type="text" id="skuId" name="skuId" value="${skuId!''}"/>
                    </label>
                </li>
                <li>
                    <label>SPU_ID&nbsp;
                        <input type="text" id="spuId" name="spuId" value="${spuId!''}" />
                    </label>
                </li>
                <li>
                    <label>期数&nbsp;
                        <input type="text" id="phase" name="phase" value="${phase!''}" />
                    </label>
                </li>
                <li>
                    <label>开始时间&nbsp;
                        <input type="text" id="openDate" name="openDate" placeholder="开始时间" value="${openDate!''}" onclick="WdatePicker()" autocomplete="OFF"/>
                    </label>
                </li>
                <li>
                    <label>结束时间&nbsp;
                        <input type="text" id="closeDate" name="closeDate" placeholder="结束时间" value="${closeDate!''}" onclick="WdatePicker()" autocomplete="OFF"/>
                    </label>
                </li>
                <li>
                    <label>组件化课程&nbsp;
                        <select id="isComponentSku" name="isComponentSku">
                            <option value="">全部</option>
                            <option value="true" <#if isComponentSku?? && isComponentSku == 'true'>selected</#if>>是</option>
                            <option value="false" <#if isComponentSku?? && isComponentSku == 'false'>selected</#if>>否</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>地图皮肤&nbsp;
                        <select id="templateType" name="templateType">
                            <option value="">全部</option>
                            <option value="ChineseFace_1" <#if templateType?? && templateType == 'ChineseFace_1'>selected</#if>>语文皮肤一</option>
                            <option value="ChineseFace_2" <#if templateType?? && templateType == 'ChineseFace_2'>selected</#if>>语文皮肤二</option>
                            <option value="English_1" <#if templateType?? && templateType == 'English_1'>selected</#if>>英语皮肤一</option>
                            <option value="Math_1" <#if templateType?? && templateType == 'Math_1'>selected</#if>>数学皮肤一</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>配置环境&nbsp;
                        <select id="envLevel" name="envLevel">
                            <option value="-1">全部</option>
                            <option value="10" <#if envLevel?? && envLevel == 10>selected</#if>>单元测试环境</option>
                            <option value="20" <#if envLevel?? && envLevel == 20>selected</#if>>开发环境</option>
                            <option value="30" <#if envLevel?? && envLevel == 30>selected</#if>>测试环境</option>
                            <option value="40" <#if envLevel?? && envLevel == 40>selected</#if>>预发布环境</option>
                            <option value="50" <#if envLevel?? && envLevel == 50>selected</#if>>生产环境</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>激活方式&nbsp;
                        <select id="activeType" name="activeType">
                            <option value="-1">全部</option>
                            <option value="2" <#if activeType?? && activeType == 2>selected</#if>>链接</option>
                            <option value="3" <#if activeType?? && activeType == 3>selected</#if>>公众号</option>
                            <option value="4" <#if activeType?? && activeType == 4>selected</#if>>直接激活</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>SKU类型&nbsp;
                        <select id="skuType" name="skuType">
                            <option value="-1">全部</option>
                            <option value="0" <#if skuType?? && skuType == 0>selected</#if>>训练营</option>
                            <option value="1" <#if skuType?? && skuType == 1>selected</#if>>伪轻课</option>
                            <option value="2" <#if skuType?? && skuType == 2>selected</#if>>其他</option>
                            <option value="3" <#if skuType?? && skuType == 3>selected</#if>>真轻课</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>创建人&nbsp;
                        <input type="text" id="createUser" name="createUser" value="${createUser!''}"/>
                    </label>
                </li>
                <li><button type="button" class="btn btn-primary" id="searchBtn">查询</button></li>
            </ul>
        </div>
    </form>

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增课程</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>课程ID</th>
                        <th>SPU_ID</th>
                        <th>期数</th>
                        <th>报名开始日期</th>
                        <th>报名结束日期</th>
                        <th>课程开始日期</th>
                        <th>课程结束日期</th>
                        <th>原价</th>
                        <th>优惠价</th>
                        <th>SKU类型</th>
                        <th>组件化课程</th>
                        <th>地图皮肤展示</th>
                        <th>配置环境</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as sku>
                            <tr>
                                <td>${sku.id!''}</td>
                                <td>${sku.spuId!''}</td>
                                <td>${sku.phase!''}</td>
                                <td>${sku.showDate!''}</td>
                                <td>${sku.sighUpEndDate!''}</td>
                                <td>${sku.openDate!''}</td>
                                <td>${sku.closeDate!''}</td>
                                <td>${sku.price!''}</td>
                                <td>${sku.discountPrice!''}</td>
                                <td><#if sku.skuType?? && sku.skuType == 0>训练营<#elseif sku.skuType?? && sku.skuType == 1>伪轻课<#elseif sku.skuType?? && sku.skuType == 2>其他<#elseif sku.skuType?? && sku.skuType == 3>真轻课</#if></td>
                                <td><#if sku ?? && (!sku.isComponentSku?? || sku.isComponentSku == false)>否<#elseif sku ?? && sku.isComponentSku?? && sku.isComponentSku == true>是</#if></td>
                                <td>
                                <#if sku.isComponentSku?? && sku.isComponentSku == true>
                                    <#if sku.templateType?? && sku.templateType == 'ChineseFace_1'>语文皮肤一
                                    <#elseif sku.templateType?? && sku.templateType == 'ChineseFace_2'>语文皮肤二
                                    <#elseif sku.templateType?? && sku.templateType == 'English_1'>英语皮肤一
                                    <#elseif sku.templateType?? && sku.templateType == 'Math_1'>数学皮肤一
                                    </#if>
                                </#if>
                                </td>
                                <td>
                                    <#if sku.envLevel?? && sku.envLevel == 10>单元测试
                                    <#elseif sku.envLevel?? && sku.envLevel == 20>开发环境
                                    <#elseif sku.envLevel?? && sku.envLevel == 30>测试环境
                                    <#elseif sku.envLevel?? && sku.envLevel == 40>预发布环境
                                    <#elseif sku.envLevel?? && sku.envLevel == 50>生产环境
                                    </#if>
                                </td>
                                <td>${sku.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${sku.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${sku.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${sku.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="11" style="text-align: center">暂无数据</td>
                        </tr>
                        </#if>
                    </tbody>
                </table>
                <ul class="message_page_list"></ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    $(function () {

        $(".message_page_list").page({
            total: ${totalPage!},
            current: ${currentPage!},
            autoBackToTop: false,
            maxNumber: 20,
            jumpCallBack: function (index) {
                $("#pageNum").val(index);
                $("#op-query").submit();
            }
        });

        $("#searchBtn").on('click', function () {
            $("#pageNum").val(1);
            $("#op-query").submit();
        });

        $(document).on('click',".js-couponOption",function () {
            var $this = $(this),
                    type = $this.data('type'),
                    cid = $this.data('cid'),
                    mapLink = {
                        'add' : '',
                        'edit': '?skuId=' + cid,
                        'info': '?skuId=' + cid,
                        'logs': '?skuId=' + cid
                    };
            var url = '';
            if(type === "info"){
                url = 'info.vpage'+ mapLink[type];
            } else if (type === "logs") {
                url = 'logs.vpage' + mapLink[type];
            } else if (type === "edit") {
                url = 'details.vpage' + mapLink[type];
            } else {
                url = 'details.vpage';
            }
            window.open(url, '_blank').location;
        });
    });
</script>
</@layout_default.page>