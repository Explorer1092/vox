<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="班级详情" pageJs="common" footerIndex=2 navBar="hidden">
<@sugar.capsule css=['res',"home"]/>
<style>
    .js_color{
        color: #ff7d5a;
    }
</style>
<div class="resources-box">
    <div class="res-content clazz-detail">
        <div class="dashed data t1" style="border:none">学生人数：<#if clazzInfo??>${clazzInfo.totalStuCnt!0}</#if>人<span style="float:right;">班级ID：<#if clazzInfo??>${clazzInfo.cid!"--"}</#if></span></div>
        <div class="h-head" style="background-color:rgb(241,242,245);font-size:.65rem;color:#7f86ad;line-height:1.175rem;padding:.35rem 0 .35rem .5rem ;border:none">学生名单</div>
        <table class="sideTable">
            <thead>
            <tr><td>姓名</td><td>一起ID</td></tr>
            </thead>
            <#if studentList??>
                <tbody>
                <#list studentList as student>
                    <tr style="line-height:1.5rem;height:1.5rem"><td>${student["studentName"]!""}</td><td>${student["studentId"]!""}</td></tr>
                </#list>
                </tbody>
            </#if>
        </table>
    </div>
</div>
<script>
    $(".dt").on("click",function(){
        var $this=$(this);
        $this.next(".student-list").slideToggle(function(){
            $this.toggleClass("dashed");
        });
    });
    $(document).on('click','.JS-tab',function(){
        var _$this = $(this);
        $('.student').eq(_$this.index()).show().siblings().hide();
        _$this.addClass('js_color').siblings().removeClass('js_color');
    })
</script>
</@layout.page>
