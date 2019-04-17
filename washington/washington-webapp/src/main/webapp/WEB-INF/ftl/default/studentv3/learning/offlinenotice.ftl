<#--学生自学下线通知-->
<#import "module.ftl" as module>
<@module.learningCenter menuName="index">
<style type="text/css">
    .st-wrapper{
        position:relative;
        width:770px;
        height:630px;
        margin:33px auto;
        border-radius : 15px;
        background:url(<@app.link href="public/skin/studentv3/images/bg01_01.png?"/>) center center no-repeat #fff
    }
    .go-to{
        position:absolute;
        left:242px;
        top:230px;
        width:120px;
        height:41px;
        background:url(<@app.link href="public/skin/studentv3/images/btn.png"/>) no-repeat;
        background-size:100% 100%;
        border-radius:16px;color:#956322;
        line-height:41px;
        text-decoration:none;
        font-weight:700;
        text-indent:42px
    }
</style>
<div id="l_selfStudy_box" class="learn-con w-fl-right">
<div class="st-wrapper">
    <#--<a class="go-to" href="/student/babel/api/index.vpage" title="去通天塔">去通天塔</a>-->
</div>
</div>
</@module.learningCenter>