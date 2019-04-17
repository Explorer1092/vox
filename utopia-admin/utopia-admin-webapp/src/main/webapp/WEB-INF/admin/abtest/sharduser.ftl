<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='添加方案' page_num=20>
<!--/span-->
<div class="span9">
    <div class="hero-unit">
        <h1>请为实验(${id!""})分配流量</h1>
    </div>
    <div>
        请选择入实验比例<input type="number" id="shard">%
        <br>
        <button id="setshard">确定</button>
    </div>
</div>
<script>
    var id=${id};
    $(function () {
        $("#setshard").on("click",function () {
            var shard=$("#shard").val();
            shard=parseInt(shard);
            if(shard>100){
                alert("请输入0-100的数字");
                return false;
            }
            $.post("setshard.vpage",{shard:shard,id:id},function (data) {
                console.info(data);
            })
        })
    });
</script>
<!--/row-->
</@layout_default.page>