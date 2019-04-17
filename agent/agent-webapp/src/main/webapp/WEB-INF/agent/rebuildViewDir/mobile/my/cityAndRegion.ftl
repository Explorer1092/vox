<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="教研员资源" pageJs="" footerIndex=4>
    <@sugar.capsule css=['researchers']/>
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
        var setTopBar = {
            show:true,
            rightText:"",
            rightTextColor:"ff7d5a",
            needCallBack:true
        };
        var topBarCallBack =  function(){};
        setTopBarFn(setTopBar, topBarCallBack);
        var level = 0;

        var activeNode = function(node){
            $(node).addClass("active").siblings("li").removeClass("active");
        };

        //选中
        var selectSub = function(data){
            $.post("save_city_region.vpage",data,function(res){
                if(res.success){
                    location.href = "/view/mobile/crm/researcher/edit_researcher.vpage<#if id??>?id=${id!0}</#if>";
                }else{
                    AT.alert(res.info);
                }
            });
        };

        var oneLevelEvent = function(index){
            $(".js-firstLevel").on("click",function(e){
                activeNode(this);
                var pid = $(this).data("pid");
                var data = {region:pid,level:index};
                selectSub(data);
            });
        };

        var twoLevelEvent = function(index){

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

                var data = {region:cid,level:index};
                selectSub(data);
            });
        };

        var thereLevelEvent = function(index){

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

                var data = {region:rid,level:index};
                selectSub(data);
            });
        };

        //分发事件
        var disPatcherEvent = function(index) {
            switch(index){
                case 1:
                    oneLevelEvent(index);
                    break;
                case 2:
                    twoLevelEvent(index);
                    break;
                case 3:
                    thereLevelEvent(index);
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

        //展示面板
        var displayPane = function (index) {

            var tempArray = ["oneLevelTemp","twoLevelTemp","thereLevelTemp"];

            var tempId = tempArray[index-1];
            $.post("load_city_region.vpage",{level:index},function(res){
                if(res.success){
                    if(res.regionInfo && res.regionInfo.success){
                        var list = res.regionInfo.nodeList || [];
                        $("#container").html(template(tempId,{array:list}));

                        disPlayActiveNode(index);

                        disPatcherEvent(index);
                    }else{
                        AT.alert(res.info);
                    }
                }else{
                    AT.alert(res.info);
                }
            })

        };

        $(document).on("click",".js-Tab>li",function(){
            level =$(this).data("index");
            $(this).addClass("active").siblings("li").removeClass("active");

            displayPane(level);
        });

        <#if level?has_content>
            level = ${level};
            $(".js-Tab>li[data-index='"+level+"']").addClass("active");
            displayPane(level);
        </#if>

    });
</script>

</@layout.page>