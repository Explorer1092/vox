<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 100px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">SPU管理</span></legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>SPU_ID&nbsp;
                        <input type="text" id="spuId" name="spuId" value="${spuId!''}"/>
                    </label>
                </li>
                <li>
                    <label>SPU名称&nbsp;
                        <input type="text" id="name" name="name" value="${name!''}" />
                    </label>
                </li>
                <li>
                    <label>系列ID&nbsp;
                        <input type="text" id="seriesId" name="seriesId" value="${seriesId!''}" />
                    </label>
                </li>
                <li>
                    <label>SPU类型&nbsp;
                        <select id="type" name="type">
                            <option value="-1">全部</option>
                            <option value="0" <#if type?? && type == 0>selected</#if>>普通</option>
                            <option value="1" <#if type?? && type == 1>selected</#if>>线下推广</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>含周复习课&nbsp;
                        <select id="hasReview" name="hasReview">
                            <option value="-1">全部</option>
                            <option value="1" <#if hasReview?? && hasReview == 1>selected</#if>>是</option>
                            <option value="2" <#if hasReview?? && hasReview == 2>selected</#if>>否</option>
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
                        <th>SPU_ID</th>
                        <th>SPU名称</th>
                        <th>系列ID</th>
                        <th>SPU类型</th>
                        <th>适合年级</th>
                        <th>含周复习</th>
                        <th>学习天数</th>
                        <th>配置环境</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as spu>
                            <tr>
                                <td>${spu.id!''}</td>
                                <td>${spu.name!''}</td>
                                <td>${spu.seriesId!''}</td>
                                <td><#if spu?? && spu.type?? && spu.type == 0>普通<#elseif spu?? && spu.type?? && spu.type == 1>线下推广</#if></td>
                                <td>
                                    <#assign index = 0>
                                    <#if spu.grades?? && spu.grades?size gt 0><#list spu.grades as grade><#if index != 0>,</#if>${grade}<#assign index = index + 1></#list></#if>
                                </td>
                                <td><#if spu?? && spu.hasReview?? && spu.hasReview == true>是<#elseif spu?? && spu.hasReview?? && spu.hasReview == false>否</#if></td>
                                <td>${spu.days!''}</td>
                                <td>
                                    <#if spu.envLevel?? && spu.envLevel == 10>单元测试
                                    <#elseif spu.envLevel?? && spu.envLevel == 20>开发环境
                                    <#elseif spu.envLevel?? && spu.envLevel == 30>测试环境
                                    <#elseif spu.envLevel?? && spu.envLevel == 40>预发布环境
                                    <#elseif spu.envLevel?? && spu.envLevel == 50>生产环境
                                    </#if>
                                </td>
                                <td>${spu.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${spu.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${spu.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${spu.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="10" style="text-align: center">暂无数据</td>
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
                        'edit': '?spuId=' + cid,
                        'info': '?spuId=' + cid,
                        'logs': '?spuId=' + cid
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