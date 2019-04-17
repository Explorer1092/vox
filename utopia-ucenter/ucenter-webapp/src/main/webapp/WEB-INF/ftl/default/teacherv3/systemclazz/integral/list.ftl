<#import "../../../nuwa/teachershellv3.ftl" as shell />
<@shell.page show="main" showNav="hide">

<#--包班制支持-->
<#assign multiSubject = ((currentTeacherDetail.subjects?size gt 1)!false)/>
<#assign curSubject = curSubject!((currentTeacherDetail.subject)!'')/>
<#assign curSubjectText = curSubjectText!((currentTeacherDetail.subject.value)!'')/>

<style type="text/css">
    /*.t-classLearnBean-box .cl-title{text-align: center;font-size:18px;color: #4e5656;line-height: 45px;border-bottom: 1px solid #dae6ee;}*/
    .t-classLearnBean-box .cl-container{padding:0 115px;}
    .t-classLearnBean-box .cl-container .w-select{margin:20px 0;}
    .t-classLearnBean-box .cl-container .cl-info {padding:0 0 20px 0;}
    .t-classLearnBean-box .cl-container .cl-info .cl-number{line-height: 40px;color: #4e5656;float: left;padding:0 12px 0 0;}
    .t-classLearnBean-box { background-color: #fff; border-bottom: 1px solid #d3d8df; padding-bottom: 20px; margin-top: 15px;}
    .t-classLearnBean-box .infoBox{ padding: 20px; border-top: 1px solid #ddd;}
    .t-classLearnBean-box .infoBox h3{ padding: 0 0 10px 0; font-weight: normal; font-size: 14px; color: #333;}
    .t-classLearnBean-box .infoBox p{ font-size: 14px; color: #383a4c; line-height: 35px;}
    .t-classLearnBean-box .tableBox{border: 1px solid #d3d8df;border-radius: 4px;}
    .t-classLearnBean-box .tabBox{ width: 100%; height: 45px; background: #f9fcfe; border-bottom: 1px solid #68bcf7;}
    .t-classLearnBean-box .tabBox li{float: left; width: 190px ;height: 45px; line-height: 45px; text-align: center;cursor: pointer;}
    .t-classLearnBean-box .tabBox .active{ margin:-1px 0 0 -1px; *margin:0; background:#e1f0fb; border: 1px solid #68bcf7; border-bottom: 1px solid #e1f0fb;color: #0979ca;border-radius: 4px 4px 0 0;}
</style>
<div class="t-classLearnBean-box w-base">
    <div class="w-base-title">
        <#if !multiSubject>
            <h3>班级学豆</h3>
        <#else>
            <h3>${curSubjectText}班级学豆</h3>
            <div class="w-base-ext">
                <#include "../../block/switchsubjcet.ftl"/>
            </div>
        </#if>
    </div>
    <div class="cl-container">
        <div class="w-select v-clazz-select">
            <div class="current"><span class="content">----</span><span class="w-icon w-icon-arrow"></span></div>
            <ul>
                <#list clazzs as item>
                    <li data-groupid="${(item.groupId)!}" data-clazzid="${(item.clazzId)!}" class="v-selectClazzLi">
                        <a href="javascript:void(0);">${(item.clazzName)!"${(item.clazzId)!}"} </a>
                    </li>
                </#list>
            </ul>
        </div>
        <div id="integralContainer"></div>
    </div>
</div>
<script type="text/javascript">
    function createPageList(items) {
        var $integralContainer = $("#integralContainer");
        $integralContainer.html("<div style='padding: 100px 0; text-align: center; border: 1px solid #eee;'>加载第" + items.pageNumber + "页中...</div>");

        var url = '/teacher/systemclazz/integral/clazzintegralchip.vpage?'+ $.param(items);

        $.get(url, function (data) {
            if (data) {
                $integralContainer.html(data);
            }
        });
    }

    var currentClazzId;
    var currentGroupId;
    var currentGroupGe0 = true;

    $(function(){
        //换班下拉
        $(".v-clazz-select").on({
            mouseenter: function(){
                $(this).find("ul").show();
            },
            mouseleave: function(){
                $(this).find("ul").hide();
            }
        });

        //select clazz
        $(".v-selectClazzLi").on("click", function(){
            var $this = $(this);
            var $thisParent = $(".v-clazz-select");

            if($this.hasClass("active")){
                return false;
            }

            currentClazzId = $this.attr("data-clazzid");
            currentGroupId = $this.attr("data-groupid");

            $this.addClass("active").siblings().removeClass("active");
            $thisParent.find(".content").html($this.text());

            $this.parent().hide();

            createPageList({
                clazzId : currentClazzId,
                groupId : currentGroupId,
                pageNumber : 1,
                ge0 : currentGroupGe0,
                subject : "${curSubject!}"
            });
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
    });
</script>
</@shell.page>