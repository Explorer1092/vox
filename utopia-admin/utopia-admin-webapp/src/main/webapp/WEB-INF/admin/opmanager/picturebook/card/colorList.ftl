<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='绘本馆卡片管理' page_num=9>
<script src="${requestContext.webAppContextPath}/public/js/pagenation.js"></script>
<div id="main_container" class="span9">
    <legend>
        <strong>绘本馆彩蛋卡管理</strong>
    </legend>
    <form id="config-query" class="form-horizontal" method="get" action="list.vpage">
        <input type="hidden" id="pageNum" name="page" value="${currentPage!'1'}"/>
        <ul class="inline">
            <li>
                <label>状态&nbsp;
                    <select id="type" name="isOnLine">
                        <option value="">全部</option>
                        <option value="1" <#if isOnLine == 1>selected</#if>>已上线</option>
                        <option value="2"<#if isOnLine == 2>selected</#if>>未上线</option>
                    </select>
                </label>
            </li>
            <li>
                <label>
                    <input id="title" name="title" type="text" value="${title!''}" placeholder="名称"
                           style="width: 100px">
                </label>
            </li>
            <li>
                <button type="button" class="btn btn-primary" id="searchBtn">查 询</button>
            </li>
            <li>
                <a href="edit.vpage" class="btn btn-primary" id="addPictureBookCard">新建</a>
            </li>
        </ul>
    </form>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="220px">序号</th>
                        <th>彩蛋卡名称</th>
                        <th>开始时间</th>
                        <th>持续周数</th>
                        <th>创建人</th>
                        <th>状态</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                         <#if content?? && content?size gt 0>
                            <#list content as  config>
                            <tr>
                                <td>${config.id!''}</td>
                                <td>${config.name!''}</td>
                                <td>${config.startDate!''}</td>
                                <td>${config.continuedWeekNum!''}</td>
                                <td>${config.createUser!''}</td>
                                <td>
                                    <#if config.isOnLine == 1>
                                        已上线
                                    <#else>
                                        已下线
                                    </#if>
                                </td>
                                <td>
                                    <#if config.isOnLine == 1>
                                        <button class="btn btn-default" onclick="modify('${config.id!''}')">查看</button>
                                        <#--<button disabled="disabled" class="btn btn-default"
                                                onclick="changeStatus('${config.id!''}',1)">上线
                                        </button>
                                        <button class="btn btn-success" onclick="changeStatus('${config.id!''}',0)">下线
                                        </button>-->
                                    <#else>
                                        <button class="btn btn-success" onclick="modify('${config.id!''}')">编辑</button>
                                        <button class="btn btn-success" onclick="changeStatus('${config.id!''}',1)">上线
                                        </button>
                                    </#if>
                                </td>
                            </tr>
                            </#list>
                         <#else>
                        <tr>
                            <td colspan="7" style="text-align: center">暂无数据</td>
                        </tr>
                         </#if>
                    </tbody>
                </table>
                <ul class="pager">
                    <li><a href="#" onclick="pagePost(1)" title="Pre">首页</a></li>
                    <#if hasPrev>
                        <li><a href="#" onclick="pagePost(${currentPage-1})" title="Pre">&lt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&lt;</a></li>
                    </#if>
                    <li class="disabled"><a>第 ${currentPage!} 页</a></li>
                    <li class="disabled"><a>共 <#if totalPage==0>1<#else>${totalPage!}</#if> 页</a></li>
                    <#if hasNext>
                        <li><a href="#" onclick="pagePost(${currentPage+1})" title="Next">&gt;</a></li>
                    <#else>
                        <li class="disabled"><a href="#">&gt;</a></li>
                    </#if>
                </ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#commodity-query").submit();
    }

    $("#searchBtn").on('click', function () {
        $("#pageNum").val(1);
        $("#config-query").submit();
    });

    function modify(id) {
        if (id === '') {
            alert("参数错误");
        }
        window.location = "edit.vpage?card_id="+id;
    }
    function changeStatus(id, enable) {
        if (id === '' || enable === '') {
            alert("参数错误");
        }
        $.ajax({
            type: "post",
            url: "enabled.vpage",
            data: {
                card_id: id,
                enabled: enable
            },
            success: function (data) {
                if (data.success) {
                    window.location.reload();
                } else {
                    alert("操作失败");
                }
            }
        });
    }
</script>
</@layout_default.page>