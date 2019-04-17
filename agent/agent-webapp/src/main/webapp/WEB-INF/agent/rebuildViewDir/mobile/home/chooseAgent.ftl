<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="选择专员" pageJs="" footerIndex=1>
<@sugar.capsule css=['researchers']/>
<div class="crmList-box">
    <#--<div class="res-top fixed-head">
        <a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>
        <span class="return-line"></span>
        <span class="res-title">选择人员</span>
    </div>-->
</div>

<div class="selInstructor-box">
    <div class="sit-tab">
        <ul class="js-Tab"></ul>
    </div>
    <div id="container"></div>

    <#include "regionTemp.ftl"> <#--区域选择-->
</div>
<script>
    var AT = new agentTool();

    $(document).on("ready",function(){
        try{
        var setTopBar = {
            show:true,
            rightText:"",
            rightTextColor:"ff7d5a",
            needCallBack:true
        };
        setTopBarFn(setTopBar);
        }catch(e){
            console.log(e)
        }
        var activeNode = function(node){
            $(node).addClass("active").siblings("li").removeClass("active");
        };

        //选中
        var selectSub = function(bdId){
            var breakUrl = '${breakUrl!0}';
            console.log(breakUrl)
            if (breakUrl == 'visit_school_result'){
                location.href = "/mobile/performance/visit_school_result_page.vpage?bdId="+bdId;
            }else if(breakUrl == 'school_mau_increase_statistics'){
                location.href = "/mobile/resource/school/school_mau_increase_statistics.vpage?userId="+bdId;
            }else if(breakUrl == 'teacherauth'){
                location.href = "/mobile/resource/teacherauth/index.vpage?userId="+bdId;
            }else if(breakUrl == 'feedback'){
                location.href = "/mobile/feedback/view/index.vpage?userId="+bdId;
            }else if(breakUrl == 'top_school_rankings'){
                location.href = "/mobile/analysis/top_school_rankings.vpage?userId="+bdId;
            }
        };

        var oneLevelEvent = function(){
            $(".js-firstLevel").on("click",function(e){
                activeNode(this);
                var pid = $(this).data("pid");
                selectSub(pid);
            });
        };

        var twoLevelEvent = function(){

            $(".js-firstLevel").on("click",function(e){
                activeNode(this);
                var pid = $(this).data("pid");
                $(".js-secondLevel[data-pid="+pid+"]").show();
                $(".js-secondLevel[data-pid!="+pid+"]").hide();
            });

            $(".js-secondLevel").on("click",function(e){
                activeNode(this);
                var cid = $(this).data("cid");
                var pid = $(this).data("pid");

                $(".js-firstLevel[data-pid="+pid+"]").addClass("active");
                selectSub(cid);
            });

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

                selectSub(rid);
            });
        };

        //分发事件
        var disPatcherEvent = function(index) {
            switch(index){
                case 1:
                    oneLevelEvent();
                    break;
                case 2:
                    twoLevelEvent();
                    break;
                case 3:
                    thereLevelEvent();
                    break;
            }
        };

        //激活已选中节点
        var disPlayActiveNode = function(index) {
            var firstActiveNode = $(".js-firstLevel.active");
            if(index == 2){
                if(firstActiveNode.length != 0){
                    var pid = $(firstActiveNode[0]).data("pid");
                    $(".js-secondLevel[data-pid="+pid+"]").show();
                    $(".js-secondLevel[data-pid!="+pid+"]").hide();
                }
            }else if(index == 3){
                if(firstActiveNode.length != 0){
                    var pid = $(firstActiveNode[0]).data("pid");
                    $(".js-secondLevel[data-pid="+pid+"]").show();
                    $(".js-secondLevel[data-pid!="+pid+"]").hide();

                    var secondActiveNode = $(".js-secondLevel.active");
                    if(secondActiveNode.length !=0){
                        var cid = $(secondActiveNode[0]).data("cid");
                        $(".js-thirdLevel[data-pid="+cid+"]").show();
                        $(".js-thirdLevel[data-pid!="+cid+"]").hide();
                    }
                }
            }
        };

        var getDate = function(){
            var tempArray = ["oneLevelTemp","twoLevelTemp","thereLevelTemp"];

            $.post("choose_business_developer.vpage",
                    {
                        selectedUser:${selectedUser!0},
                        needCityManage:${needCityManage!0}
                    },
                    function(res){
                if(res.success){
                    if(res.nodeList && res.nodeList.length!= 0){
                        var list = res.nodeList || [];
                        var index = parseInt(res.tier);
                        if(index > 0 && index <4){
                            var tempId = tempArray[index-1];
                            $("#container").html(template(tempId,{array:list}));
                            $(".js-Tab").html(template("headerTab",{index:index}));

                            disPlayActiveNode(index); //激活已选中节点

                            disPatcherEvent(parseInt(index));
                        }else{
                            AT.alert("数据层级超出范围");
                        }
                    }else{
                        AT.alert(res.info);
                    }
                }else{
                    AT.alert(res.info);
                }
            })
        };

        getDate();

    });
</script>
</@layout.page>