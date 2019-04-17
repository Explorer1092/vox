<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="待我审核" pageJs="" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['new_home','notice']/>
<style>
    .red{border-radius:100%;color: white;width:1rem;height:1rem;display: inline-block;text-align: center;font-size:.60rem;vertical-align: middle;line-height:1rem;background:red}
</style>
<div class="res-top fixed-head">
    <a href="javascript:void(0)"><div class="return js-return"><i class="return-icon"></i>返回</div></a>
    <span class="return-line"></span>
    <span class="res-title">待我审核</span>
</div>
<div class="s-list js-noticeBox" style="display: none;">
    <div class="item js-list" data-type="1">
        字典表调整
        <div class="s-right">
            <span class="red">${modifyDictSchoolCount!0}</span>
            <i class="seeArrow"></i>
        </div>
    </div>
    <div class="item js-list" data-type="2">
        商品购买
        <div class="s-right">
            <span class="red">${orderCount!0}</span>
            <i class="seeArrow"></i>
        </div>
    </div>
    <div class="item js-list" data-type="7">
        大数据报告申请
        <div class="s-right">
            <span class="red">${dataReportCount!0}</span>
            <i class="seeArrow"></i>
        </div>
    </div>
</div>
<script>
    $('.red').each(function(){
        if($(this).html() >= 100){
            $(this).html("99+");
        }else if($(this).html() == 0){
            $(this).removeClass('red');
            $(this).html("");
        }
    });
    $(document).ready(function(){
        $('.js-noticeBox').show();
    });
//    $(document).on('click','.js-clazz',function(){
//        location.href = "/mobile/audit/clazz_apply_list.vpage"
//    });
    $(document).on('click','.js-return',function(){
        location.href = "/mobile/notice/index.vpage"
    });
    $(document).on('click','.js-list',function(){
        var workFlowType = $(this).data('type');
        location.href = "/mobile/audit/todo_list.vpage?workflowType="+workFlowType;
    });
</script>
</@layout.page>
