$(".js-completed").on("click",function(){
    window.location.href="/mobile/audit/done_list.vpage";
});
$(".js-itemBtn1").on("click",function(){
    openSecond("/mobile/audit/process_page.vpage?workflowId=" + $(this).data().sid + "&applyType=" + $(this).data().type) ;
});
$(".js-pending").on("click",function(){
    window.location.href = "/mobile/audit/todo_list.vpage";
});
$(".js-itemBtn2").on("click",function(){
    openSecond("/mobile/apply/apply_detail.vpage?workflowId="+$(this).data().sid + "&applyType="+$(this).data().type);
});