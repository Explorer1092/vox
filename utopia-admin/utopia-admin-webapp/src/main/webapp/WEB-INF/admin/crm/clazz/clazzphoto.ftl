<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='班级动态图片管理' page_num=3>
<div id="main_container" class="span9">
    <legend>班级动态图片管理</legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="frm" class="form-horizontal" method="post" action="${requestContext.webAppContextPath}/crm/clazz/photoManagment.vpage" >
                    <input type="hidden" id="pageNumber" name="pageNumber" value="1">
                    学生ID<input name="studentId" id="studentId" type="text"/>
                    <button id="selectTable" type="submit" class="btn btn-primary">查 询</button>
                    <input type="button" class="btn btn-warning" value="删除" id="removeButton"/>
                </form>
                <ul class="pager">
                    <#if (journalPage.hasPrevious())>
                        <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">上一页</a></li>
                    </#if>
                    <#if (journalPage.hasNext())>
                        <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">下一页</a></li>
                    </#if>
                    <li>当前第 ${pageNumber!} 页 |</li>
                    <li>共 ${journalPage.totalPages!} 页</li>
                </ul>
                <div id="data_table_journal">
                    <table class="table table-striped table-bordered">
                        <tr>
                            <td></td>
                            <td>ID</td>
                            <td>用户ID</td>
                            <td>创建时间</td>
                            <td style="width: 70%">图片</td>
                        </tr>
                        <#if journalPage.content?? >
                            <#list journalPage.content as journal >
                                <tr>
                                    <td><input name="journalId" type="checkbox" value="${journal.journalId!}"></td>
                                    <td>${journal.journalId!}</td>
                                    <td>${journal.relevantUserId!}</td>
                                    <td>${journal.date!}</td>
                                    <td>
                                        <#if (journal.param.photos)?? >
                                            <#list journal.param.photos as photo >
                                                <img src="${prePath}/gridfs/${photo!}" width="120" style="height: 120px"/>
                                            </#list>
                                        </#if>
                                    </td>
                                </tr>
                            </#list>
                        </#if>
                    </table>
                </div>
                <ul class="pager">
                    <#if (journalPage.hasPrevious())>
                        <li><a href="#" onclick="pagePost(${pageNumber-1})" title="Pre">上一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">上一页</a></li>
                    </#if>
                    <#if (journalPage.hasNext())>
                        <li><a href="#" onclick="pagePost(${pageNumber+1})" title="Next">下一页</a></li>
                    <#else>
                        <li class="disabled"><a href="#">下一页</a></li>
                    </#if>
                    <li>当前第 ${pageNumber!} 页 |</li>
                    <li>共 ${journalPage.totalPages!} 页</li>
                </ul>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function pagePost(pageNumber){
        $("#pageNumber").val(pageNumber);
        $("#frm").submit();
    }

    $(function() {
        $('#removeButton').on('click', function() {
            var journalIds = [];
            $("#data_table_journal input[name='journalId']:checked").each(function(){
                journalIds.push($(this).val());
            });
            if(journalIds.length == 0){
                alert("请至少选择一条数据");
                return false;
            }
            var postData = {
                journalIds : journalIds.join(",")
            };

            $.post("/crm/clazz/cleanjournalphoto.vpage", postData, function(data){
                if(data.success){
                    $("#frm").submit();
                }else{
                    alert(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>