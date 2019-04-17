<#import "../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="hide">
<style type="text/css">
    .t-homework-flower{ overflow: hidden; width: 100%; background: #fff; color: #4e5656; }
    .t-homework-flower .flower-left{ float: left; width: 635px; padding: 25px 0 25px 15px;}
    .t-homework-flower .flower-right{ float: right; width: 340px; height: 294px; border-left: 1px solid #d3d8df; background: url("<@app.link href="public/skin/teacherv3/images/flower/flower-bg.png"/>") no-repeat 35px 35px;}
    .t-homework-flower .fIntroBox{ width: 598px; height: 94px; padding: 10px; border: 1px solid #d3d8df;margin-top:25px;}
    .t-homework-flower .fIntroBox h3,.fIntroBox p{ font-size: 14px; color: #939393; font-weight: normal; }
    .t-homework-flower .fIntroBox p i{ display: inline-block; width: 5px; height: 5px; line-height: 0; overflow: hidden; vertical-align: middle; background: #dae6ee; margin: -2px 10px 0 10px; *margin: 2px 10px 0 10px;}
    .t-homework-flower .fl-section01{ margin-bottom: 25px;margin-top:5px; }
    .t-homework-flower .fl-section01 .fl-text{ float: left;}
    .t-homework-flower .fl-section01 .fr-text{ float: right; display: inline-block; line-height: 20px;}
    .t-homework-flower .fl-section02{ margin-bottom: 25px; overflow: hidden; *zoom: 1;}
    .t-homework-flower .fl-section02 .fl-text{ float: left; font-size: 14px;margin-top:5px;}
    .t-homework-flower .fl-section02 .fl-text i{ display: inline-block; width: 32px; height: 38px; background: url("<@app.link href="public/skin/teacherv3/images/flower/flower-icon-small.png"/>") no-repeat;}
    .t-homework-flower .fl-section02 .fl-text span{ font-size: 22px;}
    .t-homework-flower .fl-section02 .fr-btn{ float: right; margin-right: 15px; margin-bottom:20px;}
</style>
<div class="w-base" style="margin-top: 15px;">
    <div class="w-base-title">
        <#if !((currentTeacherDetail.subjects?size gt 1)!false)>
            <h3>家长点赞</h3>
        <#else>
            <h3>家长点赞-${curSubjectText!}</h3>
            <div class="w-base-ext">
                <#include "../block/switchsubjcet.ftl"/>
            </div>
        </#if>
    </div>
    <#if senderCnt?has_content && senderCnt != 0>
    <h3 style="font-size: 14px;font-weight: normal;padding: 10px 0 0 15px;color: #4e5656;">本月共有<span class="w-orange" style="font-size:22px">${senderCnt}</span>位家长点赞，与您一起关注孩子的学习！</h3>
    <#else>
        <h3 style="font-size: 14px;font-weight: normal;padding: 10px 0 0 15px;color: #4e5656;">本月暂时未收到点赞哦</h3>
    </#if>
    <div class="t-homework-flower">
        <div class="flower-left">
            <div class="fl-section01" style="float:left;">
                <div class="w-select v-clazz-select">
                    <div class="current"><span class="content">----</span><span class="w-icon w-icon-arrow"></span></div>
                    <ul>
                        <#list clazzList as item>
                            <li data-clazzid="${(item.id)!}" class="v-selectClazzLi">
                                <a href="javascript:void(0);">${(item.formalizeClazzName())!"${item.id}"} </a>
                            </li>
                        </#list>
                    </ul>
                </div>
                <script type="text/html" id="T:flowerContainer">
                    <div class="fl-section02">
                        <p class="fl-text" style="margin-left:15px;">该班级当前可兑换点赞<span><%=(dataJson.flowerCount ? dataJson.flowerCount : 0)%></span>个，可兑换<span><%=(dataJson.exchangeCount ? dataJson.exchangeCount : 0)%></span>次
                        </p>
                        <%if(dataJson.flowerCount && dataJson.flowerCount >= 10){%>
                        <a href="javascript:void(0)" data-clazzid="<%=clazzId%>" class="fr-btn w-btn w-btn-well w-btn-blue v-exchangeBtn">为该班兑换学豆</a>
                        <%}else{%>
                        <a href="javascript:void(0)" class="fr-btn w-btn w-btn-well w-btn-disabled">为该班兑换学豆</a>
                        <%}%>
                    </div>
                </script>
            </div>
            <div id="flowerContainer"><#--T:flowerContainer--></div>
            <div class="fIntroBox">
                <h3>说明:</h3>
                <p><i></i>学生完成作业后，家长在APP上查看作业情况时，可以给老师点赞；</p>
                <p><i></i>用 10个点赞 即可兑换一次班级学豆，数量随机哦；</p>
                <p><i></i>每月1号上个月点赞数清零。</p>
            </div>
        </div>
        <div class="flower-right"></div>
    </div>
</div>

<script>
   $(function(){
       var currentClazzId;

       //换班下拉
       $(".v-clazz-select").on({
           mouseenter: function(){
               $(this).find("ul").show();
           },
           mouseleave: function(){
               $(this).find("ul").hide();
           }
       });

       function exchangeinfoGet(id){
           $.post("exchangeinfo.vpage", {
               clazzId : id,
               subject : "${curSubject}"
           }, function(data){
               $("#flowerContainer").html( template("T:flowerContainer", {
                   dataJson : data,
                   clazzId : id
               }) );
           });
       }

       //select clazz
       $(".v-selectClazzLi").on("click", function(){
           var $this = $(this);
           var $thisParent = $(".v-clazz-select");

           currentClazzId = $this.data("clazzid");

           $this.addClass("active").siblings().removeClass("active");
           $thisParent.find(".content").html($this.text());

           $this.parent().hide();

           exchangeinfoGet(currentClazzId);
       });

       if($17.getQuery("clazzId")){
           var clazzIdBtn = $(".v-selectClazzLi[data-clazzid='"+ $17.getQuery("clazzId") +"']");
           if(clazzIdBtn.length > 0){
               clazzIdBtn.click();
           }else{
               $(".v-selectClazzLi:first").click();
           }
       }else{
           $(".v-selectClazzLi:first").click();
       }

       //兑换
       $(document).on("click", ".v-exchangeBtn", function(){
           var $this = $(this);

           $.post("flowerexchange.vpage", {
               clazzId : currentClazzId,
               subject : "${curSubject}"
           }, function(data){
               if(data.success){
                   $17.alert("恭喜！兑换获得" + data.integral + "个班级学豆！", function(){
                       exchangeinfoGet(currentClazzId);
                       $.prompt.close();
                       return false;
                   });
               }else{
                   $17.alert(data.info);
               }
           });
       });
   });
</script>
</@shell.page>