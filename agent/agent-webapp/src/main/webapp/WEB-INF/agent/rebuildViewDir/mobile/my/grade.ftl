<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="教研员选择班级" pageJs="" footerIndex=4>
    <@sugar.capsule css=['researchers']/>
    <span class="sub_btn js-subBtn" style="display: none">确定</span>
<div class="sel-content">
    <div class="vir-title"><i class="titleIco ico03"></i>小学</div>
    <div class="sel-list">
        <ul id="juniorPane">
            <li class="js-item" data-index="1">
                <div>一年级</div>
            </li>
            <li class="js-item" data-index="2">
                <div>二年级</div>
            </li>
            <li class="js-item" data-index="3">
                <div>三年级</div>
            </li>
            <li class="js-item" data-index="4">
                <div>四年级</div>
            </li>
            <li class="js-item" data-index="5">
                <div>五年级</div>
            </li>
            <li class="js-item" data-index="6">
                <div>六年级</div>
            </li>
        </ul>
    </div>
</div>
<div class="sel-content">
    <div class="vir-title"><i class="titleIco ico04"></i>中学</div>
    <div class="sel-list">
        <ul id="middlePane">
            <li class="js-item" data-index="6">
                <div>六年级</div>
            </li>
            <li class="js-item" data-index="7">
                <div>七年级</div>
            </li>
            <li class="js-item" data-index="8">
                <div>八年级</div>
            </li>
            <li class="js-item" data-index="9">
                <div>九年级</div>
            </li>
        </ul>
    </div>
</div>

<div class="sel-content">
    <div class="vir-title"><i class="titleIco ico04"></i>高中</div>
    <div class="sel-list">
        <ul id="highPane">
            <li class="js-item" data-index="10">
                <div>高一</div>
            </li>
            <li class="js-item" data-index="11">
                <div>高二</div>
            </li>
            <li class="js-item" data-index="12">
                <div>高三</div>
            </li>
        </ul>
    </div>
</div>
<#--<div class="sel-info">小学和中学不支持同时选择</div>-->
<script>
    var AT = new agentTool();
    $(document).on("ready",function(){
        var setTopBar = {
            show: true,
            rightText:"确定" ,
            rightTextColor: "ff7d5a",
            needCallBack: true
        };
        var topBarCallBack =  function(){
            $(".js-subBtn").click();
        };
        setTopBarFn(setTopBar, topBarCallBack);
        <#if grade?? && grade?size gt 0>
            <#list grade as g>
                $("#juniorPane").find('[data-index="${g!0}"]').addClass("active");
                $("#middlePane").find('[data-index="${g!0}"]').addClass("active");
                $("#highPane").find('[data-index="${g!0}"]').addClass("active");
            </#list>
        </#if>

        $(document).on("click",".js-item",function(){
            /*var parent = $(this).parent("ul")[0].id;
              if(parent == "juniorPane"){
                  $("#middlePane>.js-item").removeClass("active");
              }else{
                  $("#juniorPane>.js-item").removeClass("active");
              }*/
            if($(this).hasClass("active")){
                $(this).removeClass("active")
            }else{
                $(this).addClass("active")
            }
        });

        $(document).on("click",".js-subBtn",function(){
            var node = $(".js-item.active");
            if(node.length!=0){
              /*  var parent = node.parent("ul")[0].id,
                    phase = "",*/
                 var  gradeList = [];
                /*if(parent == "juniorPane"){
                    phase = 1
                }else{
                    phase = 2
                }*/

                $.each(node,function(i,item){
                    gradeList.push($(item).data("index"))
                });
                var data = {
                   // phase:phase,
                    grade:gradeList.join(",")
                };
                $.post("save_phase_grade.vpage",data,function(res){
                    if(res.success){
                        location.href = "/view/mobile/crm/researcher/edit_researcher.vpage<#if id??>?id=${id!0}</#if>";
                    }else{
                        AT.alert(res.info)
                    }
                });
            }else{
                AT.alert("请选择班级")
            }
        });
    });
</script>
</@layout.page>