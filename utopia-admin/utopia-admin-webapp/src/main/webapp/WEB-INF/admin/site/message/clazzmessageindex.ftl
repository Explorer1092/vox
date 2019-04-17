<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<link  href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>
<style>
    span {font: "arial";}
    .index {color: #0000ff;}
    .index, .item {font-size: 18px; font: "arial";}
    .warn {color: red;}
</style>
<div class="span9">
    <fieldset>
        <legend>班级系统通知</legend>
        <ul class="inline">
            <li>
                <label>班级ID：<textarea type="text" id="clazzId" placeholder="请输入班级号，每行一个"></textarea></label>
            </li>
            <li>
                <label>发送内容：<textarea  id="content" name="content" placeholder="请输入消息内容，每行一条" /></textarea></label>
            </li>
            <li>
                <button class="btn btn-primary" id="submit_send">发送</button>
            </li>
        </ul>
    </fieldset>
</div>

<script>
    $(function(){
        var submitSend = $('#submit_send');
        submitSend.on("click", function(){
            $.ajax({
                type: "post",
                url: "sendclazzmsg.vpage",
                data: {
                    clazzId : $("#clazzId").val(),
                    content : $("#content").val()
                },
                success: function (data){
                   alert(data.info);
                }
            });
        });
    });
</script>
</@layout_default.page>