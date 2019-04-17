<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<div class="span9">
    <div style="padding: 0px 55px 0px 15px;">Report Status属性</div>

    <div style="background-color:#fff;">
    <div class="form-horizontal" id="addDepartmentForm" style="padding: 50px">
    <#list statusList as status>
        <div class="control-group">
            <label class="control-label status_name" for="focusedInput"  style="width: auto;">${status.summary_name!''}</label>
            <div class="controls">
                <input class="input-xxlarge focused" type="text" id="${status.summary_name!''}_value" value="${status.collection_name!''}" data-value="${status.collection_name!''}"/>
            </div>
        </div>
    </#list>
    <input type="button" class="update_status" value="修改">
</div>
<script type="application/javascript">
    $(".update_status").click(function () {
        var dataList = [];
        var edit="";
        $.each($(".status_name"), function (i, item) {
            var id = $(item).html();
            var value = $("#" + id + "_value").val();
            var oldValue = $("#" + id + "_value").attr("data-value");
            if(value!=oldValue){
                edit+=id+":"+value+";\r\n";
            }
            dataList.push({summary_name: id,collection_name: value});
        });
        console.info(dataList);
        if(edit && confirm("变更内容如下：\r\n"+edit+"是否确认")){
            $.ajax({url:"update_status.vpage",
                contentType: "application/json;charset=UTF-8",
                type:"POST",
                data:JSON.stringify({dataList:dataList}),
                success:function(res){
                    if (res.success) {
                        alert("修改成功");
                        window.location.reload();
                    } else {
                        alert(res.info);
                    }
                }});
        }
    });
</script>
</@layout_default.page>