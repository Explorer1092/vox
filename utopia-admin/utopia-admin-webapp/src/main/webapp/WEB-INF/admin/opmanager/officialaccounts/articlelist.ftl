<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title="发布管理" page_num=16>
<div id="main_container" class="span9">
    <legend>
        <strong>${account.name!'公众号'}--发布管理</strong>
        <a href="articlesend.vpage?accountId=${accountId!0}" type="button" class="btn btn-info" style="float: right">发布新文章</a>
    </legend>
    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form class="form-inline" action="articleindex.vpage" method="get">
                    <div class="form-group" style="display:none">
                        <label for="exampleInputName2">id:</label>
                        <input type="text" class="form-control query-input date-input" name="accountId" value="${accountId!0}">
                    </div>
                    <div class="form-group">
                        <label for="exampleInputName2">发布时间:</label>
                        <input type="text" class="form-control query-input date-input" name="start" placeholder="起始时间" value="<#if start??>${start!}</#if>">
                    </div>
                    <div class="form-group">
                        <label for="exampleInputEmail2">--</label>
                        <input type="text" class="form-control query-input date-input" name="end" placeholder="结束时间" value="<#if end??>${end!}</#if>">
                    </div>
                    <div class="form-group">
                        <label for="exampleInputEmail2">状态:</label>
                        <select class="query-input" name="status">
                            <option value="">全部</option>
                            <option value="Online">未发布</option>
                            <option value="Published">已发布</option>
                            <option value="Offline">已撤回</option>
                        </select>
                    </div>
                    <div class="form-group">
                        <button type="submit" id="query-btn">查询</button>
                    </div>
                    <label style="font-size:17px;margin-left:10px;color:red">
                        今日还可发布：${publishLeftNumsD}次&nbsp;&nbsp;&nbsp;&nbsp;本月还可发布：${publishLeftNumsM}次
                    </label>
                </form>
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th width="120px">ID</th>
                        <th>文章预览</th>
                        <th width="90px">发布时间</th>
                        <th>发布人</th>
                        <th width="90px">状态</th>
                        <th width="120px">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                        <#if articlesList??>
                            <#if (articlesList?size>0)>
                                <#list articlesList as article >
                                <tr>
                                    <td style="font-size:8px;">${article.bundleId}</td>
                                    <td>
                                        <ol style="text-align:left;margin: 0 auto;display: inline-block;padding-left:20px;">
                                            <#list article.articles as item >
                                                <li class="preview-entry">
                                                    <div class="article-part"><img ${(item_index==0)?string('style="width:66px;height:36px;"','style="width:50px;height:36px;"')} name="articleImg" src="${item.imgUrl}"></img></div>
                                                    <div class="article-part">${item.articleTitle}</div>
                                                </li>
                                            </#list>
                                        </ol>
                                    </td>
                                    <td>${article.publishDatetime!''}</td>
                                    <td>${article.publishUser!''}</td>
                                    <td>
                                        <#switch article.status>
                                            <#case 'Published'>
                                                已发布
                                                <#break />
                                            <#case 'Online'>
                                                未发布
                                                <#break />
                                            <#case 'Offline'>
                                                已撤回
                                                <#break />
                                        </#switch>
                                    </td>
                                    <td>
                                        <#switch article.status>
                                            <#case 'Online'>
                                                <a name="publish-btn" class="btn" href="javascript:void(0);" data-buddleId="${article.bundleId}">发布</a>
                                                <#break />
                                            <#case 'Published'>
                                                <a name="revoke-btn" class="btn revoke" href="javascript:void(0);" data-buddleId="${article.bundleId}">撤回</a>
                                                <#break />
                                            <#case 'Offline'>
                                                <a class="btn" href="javascript:void(0);" data-buddleId="${article.bundleId}" disabled="true">发布</a>
                                                <#break />
                                            <#default>
                                                <#break />
                                        </#switch>
                                        <a id="edit-btn" class="btn" href="articlesend.vpage?bundleId=${article.bundleId}&accountId=${accountId!'0'}" data-buddleId="${article.bundleId}">编辑</a>
                                    </td>
                                </tr>
                                </#list>
                            <#else>
                                <tr>
                                    <td colspan="4">暂时还没有发布过文章哦~</td>
                                </tr>
                            </#if>
                        </#if>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<style>
    .table td , .table th{
        padding: 8px;
        line-height: 20px;
        text-align: center;
        vertical-align: middle;
        border-top: 1px solid #dddddd;
    }

    .form-group{
        display: inline-block;
    }

    .query-input{
        width:100px;
    }

    ul.content-list > li{
        float:left;
    }

    img.source-img{
        width:66px;
        height:36px;
    }

    .article-part{
        display:inline-block;
    }

    .preview-entry{
        margin-top:5px;
    }

</style>
<script type="text/javascript">
    function pagePost(pageNumber) {
        $("#pageNum").val(pageNumber);
        $("#activity-query").submit();
    }
    $(function(){

        // 状态上置最近一次的值
        $("select.query-input").val('${status!''}');

        $("a[name=revoke-btn]").on('click',function(){

            if(!confirm("发布过的文章撤回不会增加发布次数，确认撤回？")){
                return;
            }

            var $this=$(this),
                data={
                    bundleId  : $this.attr("data-buddleId"),
                    accountId : ${accountId!0}
                };
            $.post("articleoffline.vpage",data,function(res){
                if(res.success){location.reload();}
            });
        });

        $(".date-input").datepicker({
            dateFormat      : 'yy-mm-dd',  //日期格式，自己设置
            monthNames      : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            monthNamesShort : ['1月','2月','3月','4月','5月','6月','7月','8月','9月','10月','11月','12月'],
            dayNamesMin     : ["日", "一", "二", "三", "四", "五", "六"],
            defaultDate     : new Date(),
            numberOfMonths  : 1,
            changeMonth: false,
            changeYear: false,
            onSelect : function (selectedDate){}
        });

        var leftPublishNumsD = ${publishLeftNumsD};
        var leftPublishNumsM = ${publishLeftNumsM};

        $("a[name=publish-btn]").click(function(){

            if(leftPublishNumsD == 0 || leftPublishNumsM == 0){
                alert("当天/本月发布次数已达到最大值，无法再新建发布")
                return;
            }

            var afterPublishNumsD = Math.max(leftPublishNumsD - 1,0);
            var afterPublishNumsM = Math.max(leftPublishNumsM - 1,0);

            if(confirm(
                    "本次发布成功后今日还可发布"+ afterPublishNumsD +"次，本月还可发布" + afterPublishNumsM + "次\n" +
                    "是否确认发布？文章发布后将不能修改！")){
                var bundleId = $(this).data("buddleid");
                $.post("publisharticle.vpage",{"bundleId":bundleId},function(data){
                    if(data.success){
                        alert("发布成功!");
                        window.location.reload();
                    }else{
                        alert(data.info);
                    }
                });
            }
        });

        /*$("button#query-btn").click(function(){

        });*/

    });
</script>
</@layout_default.page>