<script>
    $(function(){
        $(document).on('click', '[id^="query_user_phone_"]', function(){
            var item = $(this);
            var id = parseInt(item.attr("id").substr("query_user_phone_".length));
            if(!id) {
                alert('invalid id');
                return;
            }
            $.get("/crm/account/getuserphone.vpage",{
                userId:id
            },function(data){
                // 查看手机号时增加了次数限制
                if(data.success){
                    item.before(data.phone);
                    item.remove();
                }else if(data.popup){
                    $("#checkPhone_reason_dialog").modal("show");
                    $("#checkPhone_reason").val('');
                }else{
                    alert('访问次数过多');
                }
            });
        });
        $("#checkPhone_reason_btn").on("click", function () {
            var desc = $("#checkPhone_reason").val();
            if (desc.length < 10) {
                alert('备注内容至少输入10个字符哦');
                return;
            }
            $.get("/crm/account/getuserphone.vpage", {
                userId: ${studentId!0},
                desc: desc
            }, function (data) {
                if (!data.success) {
                    alert('访问次数过多');
                    return;
                }
                $("#checkPhone_reason_dialog").modal("hide");
                $('#query_user_phone_' + ${studentId!0}).before(data.phone).remove();
            });
        });

        $(document).on('click', '[id^="query_research_staff_phone_"]', function(){
            var item = $(this);
            var id = parseInt(item.attr("id").substr("query_research_staff_phone_".length));
            if(!id) {
                alert('invalid id');
                return;
            }
            $.get("/crm/account/getresearchstaffphone.vpage",{
                userId:id
            },function(data){
                item.before(data.phone);
                item.remove();
            });
        });

    });
</script>
