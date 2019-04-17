/*-----------经营罗盘相关-----------*/
define(["jquery","prompt","datetimepicker","paginator","jqform"],function($){
    /*门店概况*/
    $('#month').val($('#monthDate option:selected').val())
    $(document).on('change','#monthDate',function(){
        $('#month').val($('#monthDate option:selected').val())
    });
    $(".submit-search").on("click",function(){
        $("#filter-form").submit();
    });
    $('.close_btn').on('click',function(){
        $(this).closest('.income-arrow').hide();
    });
    $('.income-title i').on('click',function(){
        $('.income-arrow').show();
    });
});