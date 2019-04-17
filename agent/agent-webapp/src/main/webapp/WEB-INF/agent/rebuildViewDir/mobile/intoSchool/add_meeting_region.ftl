<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<#if isNewSchool?? && isNewSchool>
    <#assign header = "新学校所在地">
<#else>
    <#assign header = "选择地点">
</#if>
<@layout.page title="${header!}" pageJs="" footerIndex=4 navBar="hidden">
    <@sugar.capsule css=['researchers']/>
<#assign isNewSchool = (type?has_content&&type == "newSchool")!false/>
<div class="selInstructor-box">
    <div class="sit-tab">
        <ul class="js-Tab">
            <li data-index="1"><a href="javascript:void(0);">省级</a></li>
            <li data-index="2"><a href="javascript:void(0);">市级</a></li>
            <li data-index="3"><a href="javascript:void(0);">区级</a></li>
        </ul>
    </div>
    <div id="container"></div>

    <#include "../layoutTemplate/multipleTemp.ftl">
</div>
<script>
    var AT = new agentTool();

    $(document).on("ready",function(){
        //展示面板
        var displayPane = function () {
            $.post("load_region_detail.vpage", {regionCode:${regionCode!0}}, function (res) {
                if (res.success) {
                    var list = res.nodeList || [];
                    $("#container").html(template("thereLevelTemp", {array: list}));
                    disPlayActiveNode();
                    thereLevelEvent();
                } else {
                    AT.alert(res.info);
                }
            })
        };
        displayPane();
        var activeNode = function(node){
            $(node).addClass("active").siblings("li").removeClass("active");
        };

        //选中
        var selectSub = function(data){

            <#if isNewSchool>
                <#if schoolId?has_content && schoolId!=0>
                    var schoolId = ${schoolId!0};
                    $.post("/mobile/school_clue/choice_region.vpage", {regionCode: (data.region), schoolId: schoolId},
                            function (res) {
                                if (res.success) {
                                    location.href = "/mobile/school_clue/update_school_info.vpage?regionCode=" + (data.region) + "&schoolId=" + schoolId;
                                }
                            });
                <#else>
                    $.post("/mobile/school_clue/choice_region.vpage",{regionCode:(data.region)},function (res) {
                        if(res.success){
                            window.location.href = document.referrer;
                        }
                    });
                </#if>
            <#else>
                location.href = "/mobile/work_record/addGroupMeeting.vpage?region=" + (data.region);
            </#if>
        };

        var thereLevelEvent = function(){
            $(".js-firstLevel").on("click",function(e){
                activeNode(this);
                var pid = $(this).data("pid");
                $(".js-secondLevel[data-pid="+pid+"]").show();
                $(".js-secondLevel[data-pid!="+pid+"]").hide();

                var cid = $($(".js-secondLevel[data-pid="+pid+"]")[0]).data("cid");
                $(".js-thirdLevel[data-pid="+cid+"]").show();
                $(".js-thirdLevel[data-pid!="+cid+"]").hide();
            });
            $(".js-secondLevel").on("click",function(e){
                activeNode(this);
                var cid = $(this).data("cid");
                var pid = $(this).data("pid");

                $(".js-firstLevel[data-pid="+pid+"]").addClass("active");
                $(".js-thirdLevel[data-pid="+cid+"]").show();
                $(".js-thirdLevel[data-pid!="+cid+"]").hide();
            });

            $(".js-thirdLevel").on("click",function(e){
                activeNode(this);
                var rid = $(this).data("rid");
                var pid = $(this).data("pid");
                var secondNode = $(".js-secondLevel[data-cid="+pid+"]");
                secondNode.addClass("active");
                var firstNodeId = secondNode.data("pid");
                $(".js-firstLevel[data-pid="+firstNodeId+"]").addClass("active");
                var data = {region:rid};
                selectSub(data);
            });
        };

        //激活已选中节点
        var disPlayActiveNode = function() {
            var firstActiveNode = $(".js-firstLevel.active");
            if (firstActiveNode.length != 0) {
                var pid = $(firstActiveNode[0]).data("pid");
                $(".js-secondLevel[data-pid=" + pid + "]").show();
                $(".js-secondLevel[data-pid!="+pid+"]").hide();
                var secondActiveNode = $(".js-secondLevel.active");
                if(secondActiveNode.length !=0){
                    var cid = $(secondActiveNode[0]).data("cid");
                    $(".js-thirdLevel[data-pid="+cid+"]").show();
                    $(".js-thirdLevel[data-pid!="+cid+"]").hide();
                }
            }
        };
    });
</script>

</@layout.page>