<#import "../layout_default.ftl" as layout_default>
<@layout_default.page page_title="通天塔活力消耗记录查询">
<div class="span9">

    用户ID:<input type="text" class="userId"/><input type="button" class="btn btn-primary query" value="查询"/>
    <div><h3 class="rs"></h3></div>
    <!-- student login -->

<script>
    $(function() {
        $(document).on("click",".query",function(){
            var reg = /^[0-9]*[1-9][0-9]*$/
            if ($(".userId").val().match(reg)) {
                $(".rs").html("正在查询中...");
                var queryUrl = "queryVitalityConsumeHistory.vpage";
                $.post(queryUrl, {userId:$(".userId").val()}, function (data) {
                            if(data.success){
                                if(data.history.length < 1){
                                    $(".rs").html("没有查到此人相关记录");
                                }
                                var html = '';
                                for(var i = 0 ; i < data.history.length; i++){
                                    var h = data.history[i];
                                    html += h.time+","+"消耗"+ h.count + "点,"+"消耗前剩余"+ h.before+"点,消耗后剩余"+ h.after+"点<br>";
                                }
                                $(".rs").html(html);
                            }else{
                                $(".rs").html("查询失败");
                            }
                        }
                        ,'json');
            }
        });
    });
</script>
</@layout_default.page>