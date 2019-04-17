/*-----------课程管理相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform"],function($){

    /*------------------------新建课程------------------------------*/
    var requireInputs = $(".require");
    var gid = $("#goods-id").val();
    $("#categoryCode").blur(function(){
        var categoryCode =$('#categoryCode').val();
        if(''==categoryCode){
            alert('请填写分类标识')
            return false;
        }else{
            $.ajax({
                type: 'POST',
                url: '/groupon/category/checkcategorycode.vpage' ,
                data: {'categoryCode':categoryCode} ,
                dataType: 'json',
                success:function(data) {
                    if(data.success){
                        return true;
                    }else{
                        alert("分类标识已存在,请重新输入!");
                        $('#categoryCode').val('');
                        return false;
                    }
                },
                error : function() {
                    alert("异常！");
                    return false;
                }
            });
        }
    });
    function isEmptyInput(){
        var isTrue = false;
        requireInputs.each(function(){
            if($(this).val() == ''){
                $(this).addClass("error").val("请填写"+$(this).attr("data-title"));
                isTrue = true;
            }
        });
        return isTrue;
    }
    $("#save-btn").on("click",function(){
        if(isEmptyInput()){
            return false;
        }
        $("#detail-form").ajaxSubmit(function(res){
            if(res.success){
                location.href = "/group/category/list.vpage";
            }else{
                $.prompt("<div style='text-align:center;'>"+(res.info||"保存失败！")+"</div>", {
                    title: "提示",
                    buttons: { "确定": true },
                    focus : 1,
                    useiframe:true
                });
            }
        });
    });


    $(document).on("focus",".require.error",function(){
        $(this).removeClass('error').val('');
    });
});