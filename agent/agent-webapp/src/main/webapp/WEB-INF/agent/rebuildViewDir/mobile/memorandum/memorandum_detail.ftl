<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#assign shortIconTail = "?x-oss-process=image/resize,w_720,h_720/auto-orient,1">
<#if memorandum.type == 'TEXT'>
    <#assign header = "备忘录详情">
<#elseif memorandum.type == 'PICTURE'>
    <#assign header = "照片库">
</#if>
<@layout.page title="${header!}" pageJs="" footerIndex=2 navBar="hidden">
    <@sugar.capsule css=['school']/>
<style>
    .schoolParticular-pop{position:fixed;top:0;left:0;width:100%;height:100%;background-color:rgba(0,0,0,.6)}
    .schoolParticular-pop .inner{position:absolute;top:50%;left:50%;transform:translate(-50%,-50%);-webkit-transform:translate(-50%,-50%);-moz-transform:translate(-50%,-50%);-o-transform:translate(-50%,-50%);-ms-transform:translate(-50%,-50%);width:15.5rem;border-radius:.2rem;background-color:#fff;overflow:hidden}
    .schoolParticular-pop h1{padding:1.5rem 0 0 0;text-align:center;font-size:.9rem;color:#ff7d5a;font-weight:400}
    .schoolParticular-pop ul{margin:1.125rem 1.75rem;height:5rem;overflow-y:auto}
    .schoolParticular-pop ul li{margin-bottom:.625rem;font-size:.75rem;color:#636880}
    .schoolParticular-pop .info{padding:1.125rem 0 1.75rem 0;text-align:center;font-size:.75rem;color:#636880;line-height:1.15rem}
    .schoolParticular-pop .item{padding:.25rem 0 0 0;text-align:center;font-size:.55rem;color:#898c91}
    .schoolParticular-pop .btn{display:-webkit-box;display:-moz-box}
    .schoolParticular-pop .btn a{-webkit-box-flex:1;-moz-box-flex:1;width:100%;display:block;height:2.25rem;text-align:center;font-size:.9rem;color:#fff;line-height:2.25rem;background-color:#ff7d5a}
    .schoolParticular-pop .btn a.white_btn{color:#636880;height:2.2rem;background-color:#fff;border-top:.05rem solid #cdd3dc}
    .memorandum-footer{height:2.25rem}
    .memorandum-footer .inner{position:fixed;left:0;bottom:0;width:100%;height:2.25rem;text-align:center;background-color:#ff7d5a}
    .memorandum-footer .inner .del_btn{display:block;line-height:2.25rem;font-size:.9rem;color:#fff}
</style>
<#if memorandum.type == 'TEXT'>
<div id="list" class="flow">
<#--<input type="text" id="context"/>-->
    <textarea name="" id="content" cols="20" rows="10" placeholder="请输入" style="width:90%;padding:0;margin:5%;border:1px solid #eaeaea">${memorandum.content!''}</textarea>
    <input hidden id="id" value="${memorandum.id!''}"/>
</div>
<#elseif memorandum.type == 'PICTURE'>
<img src="${memorandum.content!''}${shortIconTail}" alt="" style="width:100%">
<input hidden id="id" value="${memorandum.id!''}"/>
</#if>
<div class="memorandum-footer">
    <div class="inner">
        <a href="javascript:void(0);" class="del_btn">删除</a>
    </div>
</div>
<div class="schoolParticular-pop" style="display:none;z-index: 111;">
    <div class="inner">
        <h1>提示信息</h1>
        <p class="info">确定要删除这条记录吗？</p>
        <div class="btn">
            <a href="javascript:void(0);" class="white_btn">取消</a>
            <a href="javascript:void(0);" onclick="deleteMemorandum()">确定</a>
        </div>
    </div>
</div>

<script>
    $(function () {
        var setTopBar = {
            show: true,
            rightText:"" ,
            rightTextColor: "ff7d5a",
            needCallBack: false
        };
        var topBarCallBack =  function(){};
        setTopBarFn(setTopBar, topBarCallBack);
    });

    $(document).on('click','.del_btn',function(){
        $('.schoolParticular-pop').show();
    });
    $(document).on('click','.white_btn',function(){
        $('.schoolParticular-pop').hide();
    });
    function deleteMemorandum() {
        $('.schoolParticular-pop').hide();
        $.post("delete_memorandum.vpage", {id: $("#id").val()}, function (res) {
            if (res.success) {
                AT.alert("删除成功");
                disMissViewCallBack();
            } else {
                AT.alert(res.info);
            }
        });
    }
</script>
</@layout.page>