<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='家校通首页扩展tab管理' page_num=4>
<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet"
      xmlns="http://www.w3.org/1999/html">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<div id="main_container" class="span9" xmlns="http://www.w3.org/1999/html" xmlns="http://www.w3.org/1999/html">
    <legend>家长通APP首页扩展tab项管理</legend>
    <a class="btn btn-primary" href="jxtexttabedit.vpage">新建tab</a>
    <div class="tab-content">
        <div class="tab-pane active" id="news">
            <table class="table table-hover table-striped  table-bordered">
                <tr>
                    <td>id</td>
                    <td>主标题</td>
                    <td>副标题</td>
                    <td>跳转地址</td>
                    <td>灰度一级名称</td>
                    <td>灰度二级名称</td>
                    <td>状态</td>
                    <td>优先级</td>
                    <td>生效时间</td>
                    <td>失效时间</td>
                    <td>操作</td>
                </tr>
                <#if extTabList?has_content>
                    <#list extTabList as extTab>
                        <tr>
                            <td>${extTab.id!''}</td>
                            <td>${extTab.name!''}</td>
                            <td>${extTab.desc!''}</td>
                            <td>${extTab.link!''}</td>
                            <td>${extTab.mainFunctionName!''}</td>
                            <td>${extTab.subFunctionName!''}</td>
                            <td>${extTab.online?string("上线","下线")}</td>
                            <td>${extTab.rank!0}</td>
                            <td><#if extTab.startDate??>${extTab.startDate?date}</#if></td>
                            <td><#if extTab.endDate??>${extTab.endDate?date}</#if></td>
                            <td>
                                <a class="btn btn-success" id="onLineBtn_${extTab.id!''}">上线</a>
                                <a class="btn btn-danger" id="offLineBtn_${extTab.id!''}">下线</a>
                                <a class="btn btn-primary" href="jxtexttabedit.vpage?id=${extTab.id!''}">编辑</a>

                            </td>
                        </tr>
                    </#list>
                </#if>
            </table>
        </div>
    </div>





<script type="text/javascript">

    $(function(){
        $('[id^="onLineBtn_"]').on("click",function(){
            var id=$(this).attr("id").substring("onLineBtn_".length);
            if(confirm("确定上线？")){
                $.post("onlinejxttab.vpage", {id: id}, function (data) {
                    if (data.success) {
                        //getArticleList(1);
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        })
    });
//    $(document).on('click', '#onLineBtn', function () {
//        var $this = $(this);
//        var id = $this.closest('td').data('id');
//        if(confirm("确定上线？")){
//            $.post("onlinejxttab.vpage", {id: id}, function (data) {
//                if (data.success) {
//                    //getArticleList(1);
//                } else {
//                    alert(data.info);
//                }
//            });
//        }
//    });


//    $(document).on('click', '#offLineBtn', function () {
//        var $this = $(this);
//        var id = $this.closest('td').data('id');
//        if(confirm("确定下线？")){
//            $.post("offlinejxttab.vpage", {id: id}, function (data) {
//                if (data.success) {
//                    //getArticleList(1);
//                } else {
//                    alert(data.info);
//                }
//            });
//        }
//    });



    $(function(){
        $('[id^="offLineBtn_"]').on("click",function(){
            var id=$(this).attr("id").substring("offLineBtn_".length);
            if(confirm("确定下线？")){
                $.post("offlinejxttab.vpage", {id: id}, function (data) {
                    if (data.success) {
                        //getArticleList(1);
                        window.location.reload();
                    } else {
                        alert(data.info);
                    }
                });
            }
        })
    });

</script>
</@layout_default.page>
