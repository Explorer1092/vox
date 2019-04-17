<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 130px;}
    select {width: 120px;}
</style>
<div class="span9">
    <fieldset>
        <legend><span style="color: #00a0e9">主题管理</span></legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>主题ID&nbsp;
                        <input type="text" id="subjectId" name="subjectId" value="${subjectId!''}"/>
                    </label>
                </li>
                <li>
                    <label>主题名称&nbsp;
                        <input type="text" id="name" name="name" value="${name!''}"/>
                    </label>
                </li>
                <li>
                    <label>主题顺序&nbsp;
                        <input type="text" id="seq" name="seq" value="${seq!''}"/>
                    </label>
                </li>
                <li>
                    <label>系列ID&nbsp;
                        <input type="text" id="seriesId" name="seriesId" value="${seriesId!''}"/>
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

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增主题</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>主题ID</th>
                        <th>主题名称</th>
                        <th>系列ID</th>
                        <th>主题顺序</th>
                        <th>配置环境</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as subject>
                            <tr>
                                <td>${subject.id!''}</td>
                                <td>${subject.name!''}</td>
                                <td>${subject.seriesId!''}</td>
                                <td>${subject.seq!''}</td>
                                <td>
                                    <#if subject.envLevel?? && subject.envLevel == 10>单元测试
                                    <#elseif subject.envLevel?? && subject.envLevel == 20>开发环境
                                    <#elseif subject.envLevel?? && subject.envLevel == 30>测试环境
                                    <#elseif subject.envLevel?? && subject.envLevel == 40>预发布环境
                                    <#elseif subject.envLevel?? && subject.envLevel == 50>生产环境
                                    </#if>
                                </td>
                                <td>${subject.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${subject.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${subject.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${subject.id!''}">日志</a>
                                </td>
                            </tr>
                            </#list>
                        <#else >
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
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
                        'edit': '?subjectId=' + cid,
                        'info': '?subjectId=' + cid,
                        'logs': '?subjectId=' + cid
                    };
            if(type === "info"){
                location.href = 'info.vpage'+ mapLink[type];
            } else if (type === "logs") {
                location.href = 'logs.vpage' + mapLink[type];
            } else if (type === "edit") {
                location.href = 'details.vpage' + mapLink[type];
            } else {
                location.href = 'details.vpage';
            }
        });
    });
</script>
</@layout_default.page>