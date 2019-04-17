<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 100px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend>课节管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>课节ID&nbsp;
                        <input type="text" id="clazzFestivalId" name="clazzFestivalId" value="${clazzFestivalId!''}" />
                    </label>
                </li>
                <li>
                    <label>课节名称&nbsp;
                        <input type="text" id="clazzFestivalName" name="clazzFestivalName" value="${clazzFestivalName!''}"/>
                    </label>
                </li>
                <li>
                    <label>章节ID&nbsp;
                        <input type="text" id="chapterId" name="chapterId" value="${chapterId!''}"/>
                    </label>
                </li>
                <li>
                    <label>课程ID&nbsp;
                        <input type="text" id="skuId" name="skuId" value="${skuId!''}"/>
                    </label>
                </li>
                <li>
                    <label>课节类型&nbsp;
                        <select id="type" name="type">
                            <option value="-1">全部</option>
                            <option value="1" <#if type??&&type == 1>selected</#if>>普通</option>
                            <option value="2" <#if type??&&type == 2>selected</#if>>复习</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label>配置环境&nbsp;
                        <select id="envLevel" name="envLevel">
                            <option value="-1">全部</option>
                            <option value="10" <#if envLevel?? && envLevel == 10>selected</#if>>单元测试</option>
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

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增课节</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>课节ID</th>
                        <th>课节名称</th>
                        <th>课节类型</th>
                        <th>课程ID</th>
                        <th>章节ID</th>
                        <th>展示顺序</th>
                        <th>开始时间</th>
                        <th>内容模板ID</th>
                        <th>配置环境</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as clazzfestival>
                            <tr>
                                <td>${clazzfestival.id!''}</td>
                                <td>${clazzfestival.name!''}</td>
                                <td><#if clazzfestival.type?? && clazzfestival.type == 1>普通<#elseif clazzfestival.type?? && clazzfestival.type == 2>复习</#if></td>
                                <td>${clazzfestival.skuId!''}</td>
                                <td>${clazzfestival.chapterId!''}</td>
                                <td>${clazzfestival.seq!''}</td>
                                <td>${clazzfestival.openDate!''}</td>
                                <td>${clazzfestival.templateId!''}</td>
                                <td>
                                    <#if clazzfestival.envLevel?? && clazzfestival.envLevel == 10>单元测试
                                    <#elseif clazzfestival.envLevel?? && clazzfestival.envLevel == 20>开发环境
                                    <#elseif clazzfestival.envLevel?? && clazzfestival.envLevel == 30>测试环境
                                    <#elseif clazzfestival.envLevel?? && clazzfestival.envLevel == 40>预发布环境
                                    <#elseif clazzfestival.envLevel?? && clazzfestival.envLevel == 50>生产环境
                                    </#if>
                                </td>
                                <td>${clazzfestival.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${clazzfestival.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${clazzfestival.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${clazzfestival.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="9" style="text-align: center">暂无数据</td>
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
                        'edit': '?clazzFestivalId=' + cid,
                        'info': '?clazzFestivalId=' + cid,
                        'logs': '?clazzFestivalId=' + cid
                    };
            var url = '';
            if(type === "info"){
                url = 'info.vpage'+ mapLink[type];
            } else if (type === "logs") {
                url = 'logs.vpage' + mapLink[type];
            } else {
                url = 'details.vpage' + mapLink[type];
            }
            window.open(url, '_blank').location;
        });
    });
</script>
</@layout_default.page>