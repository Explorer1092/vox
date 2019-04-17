<script type="text/javascript">
    $(function() {
        $("input.so_checkbox_all").on("click", function(){
            if(!$("input.so_checkbox_all").is(':checked')){
                $("input.so_checkbox_all").prop("checked", false);
                $("input.so_checkbox").prop("checked", false);
                $("table.so_checkboxs").attr("so_checkboxs_values", "");
            }else{
                $("input.so_checkbox").prop("checked", true);
                var so_checkboxs_values = [];
                $("input.so_checkbox:checked").each(function(){
                    so_checkboxs_values.push($(this).val());
                });
                $("table.so_checkboxs").attr("so_checkboxs_values", so_checkboxs_values.toString());
            }
        });

        $("input.so_checkbox").on("click", function(){
            if($("input.so_checkbox").size() == $("input.so_checkbox:checked").size()){
                $("input.so_checkbox_all").prop("checked", true);
            }else{
                $("input.so_checkbox_all").prop("checked", false);
            }
            var so_checkboxs_values = [];
            $("input.so_checkbox:checked").each(function(){
                so_checkboxs_values.push($(this).val());
            });
            $("table.so_checkboxs").attr("so_checkboxs_values", so_checkboxs_values.toString());
        });

        $('#removeButton').on('click', function() {
            var commentIds = $("table.so_checkboxs").attr("so_checkboxs_values").split(",");
            if(commentIds.length == 0){
                alert("请至少选择一条数据");
                return false;
            }
            var postData = {
                commentIds : commentIds.toString()
            };

            $.post("${requestContext.webAppContextPath}/site/audit/deletecomment.vpage", postData, function (data) {
                if(data.success){
                    $('#submit').trigger('click');
                }else{
                    alert(data.info);
                }
            });
        });

        $('#approveButton').on('click', function() {
            var commentIds = $("table.so_checkboxs").attr("so_checkboxs_values").split(",");
            if(commentIds.length == 0){
                alert("请至少选择一条数据");
                return false;
            }
            var postData = {
                commentIds : commentIds.toString()
            };

            $.post("${requestContext.webAppContextPath}/site/audit/approvecomment.vpage", postData, function (data) {
                if(data.success){
                    $('#submit').trigger('click');
                }else{
                    alert(data.info);
                }
            });
        });

        $('#pre_page').on('click', function () {
            var $pageNum = $('#pageNumber');
            var pageNum = parseInt($pageNum.val());
            $pageNum.val(pageNum - 1);
            $('#submit').trigger('click');
        });

        $('#next_page').on('click', function () {
            var $pageNum = $('#pageNumber');
            var pageNum = parseInt($pageNum.val());
            $pageNum.val(pageNum + 1);
            $('#submit').trigger('click');

        });

        //当审核状态发生变化时，重置页码数
        $(".approved").change(function(){
            $('#pageNumber').val(1);
        });

    });
</script>