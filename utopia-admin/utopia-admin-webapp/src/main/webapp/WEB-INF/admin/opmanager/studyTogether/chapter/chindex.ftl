<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<style>
    input {width: 100px;}
    select {width: 130px;}
</style>
<div class="span9">
    <fieldset>
        <legend>章节管理</legend>
    </fieldset>

    <form id="op-query" class="form-horizontal" method="get" action="">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <div>
            <ul class="inline">
                <li>
                    <label>章节ID&nbsp;
                        <input type="text" id="chapterId" name="chapterId" value="${chapterId!''}"/>
                    </label>
                </li>
                <li>
                    <label>章节名称&nbsp;
                        <input type="text" id="chapterName" name="chapterName" value="${chapterName!''}"/>
                    </label>
                </li>
                <li>
                    <label>课程ID&nbsp;
                        <input type="text" id="skuId" name="skuId" value="${skuId!''}" />
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

    <button type="button" class="btn btn-success js-couponOption" data-type="add">新增章节</button>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-hover table-striped table-bordered">
                    <thead>
                    <tr>
                        <th>章节ID</th>
                        <th>章节名称</th>
                        <th>课程ID</th>
                        <th>展示顺序</th>
                        <th>单周奖励</th>
                        <th>双周奖励</th>
                        <th>开课时间</th>
                        <th>配置环境</th>
                        <th>创建人</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if content?? && content?size gt 0>
                            <#list content as chapter>
                            <tr>
                                <td>${chapter.id!''}</td>
                                <td>${chapter.chapterName!''}</td>
                                <td>${chapter.skuId!''}</td>
                                <td>${chapter.seq!''}</td>
                                <td>
                                    <#assign index = 0>
                                    <#if chapter.singleRewardIds?? && chapter.singleRewardIds?size gt 0>
                                        <#list chapter.singleRewardIds as num>
                                            <#if index != 0>,</#if>
                                            ${num}
                                            <#assign index = index + 1>
                                        </#list>
                                    </#if>
                                </td>
                                <td>
                                    <#assign jndex = 0>
                                    <#if chapter.doubleRewardIds?? && chapter.doubleRewardIds?size gt 0>
                                        <#list chapter.doubleRewardIds!'' as num>
                                            <#if jndex != 0>,</#if>
                                            ${num}
                                            <#assign jndex = jndex + 1>
                                        </#list>
                                    </#if>
                                </td>
                                <td>${chapter.openDate!''}</td>
                                <td>
                                    <#if chapter.envLevel?? && chapter.envLevel == 10>单元测试
                                    <#elseif chapter.envLevel?? && chapter.envLevel == 20>开发环境
                                    <#elseif chapter.envLevel?? && chapter.envLevel == 30>测试环境
                                    <#elseif chapter.envLevel?? && chapter.envLevel == 40>预发布环境
                                    <#elseif chapter.envLevel?? && chapter.envLevel == 50>生产环境
                                    </#if>
                                </td>
                                <td>${chapter.createUser!''}</td>
                                <td>
                                    <a href="javascript:" class="btn btn-primary js-couponOption" data-type="info" data-cid="${chapter.id!''}">详情</a>
                                    <a href="javascript:" class="btn btn-success js-couponOption" data-type="edit" data-cid="${chapter.id!''}">编辑</a>
                                    <a href="javascript:" class="btn btn-warning js-couponOption" data-type="logs" data-cid="${chapter.id!''}">日志</a>
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
                        'edit': '?chapterId=' + cid,
                        'info': '?chapterId=' + cid,
                        'logs': '?chapterId=' + cid
                    };
            var url = '';
            if(type === "info"){
                url = 'chinfo.vpage'+ mapLink[type];
            } else if (type === "logs") {
                url = 'chlogs.vpage' + mapLink[type];
            } else {
                url = 'chdetails.vpage' + mapLink[type];
            }
            window.open(url, '_blank').location;
        });
    });
</script>
</@layout_default.page>