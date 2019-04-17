<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="设置班主任" pageJs="" footerIndex=3 navBar="hidden">
    <@sugar.capsule css=['new_home']/>
<style>
    .permissionSetting-list .set_info{padding:.75rem;font-size:.7rem;color:#636880}
    .permissionSetting-list .set_column{padding:.75rem 0;background-color:#fff;overflow:hidden}
    .permissionSetting-list .set_column .left{float:left}
    .permissionSetting-list .set_column .left li{padding:.75rem 0;width:3.75rem;text-align:center;font-size:.65rem;color:#636880;border-bottom:1px solid #ccc;cursor:pointer}
    .permissionSetting-list .set_column .left li.active{color:#ff7d5a}
    .permissionSetting-list .set_column .right{margin-left:3.75rem}
    .permissionSetting-list .set_column .right li{padding:.3rem 0;float:left;text-align:center;width:33%;cursor:pointer}
    .permissionSetting-list .set_column .right li span{display:inline-block;width:3.75rem;height:1.75rem;font-size:.65rem;color:#636880;line-height:1.75rem;border-radius:.25rem;border:1px solid #ccc}
    .permissionSetting-list .set_column .right li.active span{color:#fff;background-color:#ff7d5a;border:1px solid #ff7d5a}
    .permissionSetting-list .set_column.sel{padding:.5rem}
    .permissionSetting-list .set_column.sel li{float:left;width:25%}
    .permissionSetting-list .set_column.sel li span{margin:.25rem;padding:.25rem;display:inline-block;text-align:center;font-size:.65rem;color:#fff;background-color:#ff7d5a;border-radius:.25rem}
</style>
<div class="permissionSetting-list" id="parentDiv" style="background-color:#f1f2f5;">
    <div class="set_info">选择老师所管理的班级（可多选）：</div>
    <div class="set_column">
        <div class="left">
            <ul class="gradeUli">
            </ul>
        </div>
        <div class="right classDiv">
        </div>
    </div>
    <div class="set_info">已选班级</div>
    <div class="set_column sel">
        <ul class="addClass">

        </ul>
    </div>
</div>
<script type="text/html" id="addClassList">
    <%if(data.teacherClass.length>0){%>
    <%for(var i = 0;i< data.teacherClass.length;i++){%>
        <li class="<%=data.teacherClass[i].classId%>" data-info="<%=data.teacherClass[i].classId%>"><span><%=data.teacherClass[i].fullName%></span></li>
    <%}%>
    <%}%>
</script>
<script type="text/html" id="gradeList">
<%if(data){%>
    <%for(var i in data){%>
        <li><%=i%></li>
    <%}%>
<%}%>
</script>
<script type="text/html" id="classList">
    <%if(data){%>
        <%for(var i in data){%>
        <ul class="classUli_<%=i%> classUli" style="display: none;">
            <%var dataMap = data[i]%>
            <%for(var j=0;j< dataMap.length;j++){%>
                <li <%if(dataMap[j].selected){%> class="active"<%}%> data-info="<%=dataMap[j].classId%>" data-name="<%=dataMap[j].fullName%>"><span><%=dataMap[j].className%></span></li>
            <%}%>
        </ul>
        <%}%>
    <%}%>
</script>
<script>
    var addClassArr;
    $(document).ready(function () {
        var setTopBar = {
            show:true,
            rightText:"提交",
            rightTextColor:"ff7d5a",
            needCallBack:true
        } ;
        var callBackFn = function(){
            addClassArr = [];
            for(var i=0;i<$(".addClass li").length;i++){
                addClassArr.push($(".addClass li").eq(i).data("info"));
            }
            addClassArr = addClassArr.join(",");
            $.post("setclassmanagelist.vpage",{teacherId:${teacherId!0},schoolId:${schoolId!0},classIds:addClassArr},function(res){
                if(res.success){
                    AT.alert("提交成功");
                    setTimeout('disMissViewCallBack()',2000);
                }else{
                    AT.alert(res.info)
                }
            })
        };
        setTopBarFn(setTopBar,callBackFn);
    });
    var AT = new agentTool();
    var buttons ={};
    $.get("getclassmanagelist.vpage",{teacherId:${teacherId!0}},function(res){
        $.each(res.gradeList, function (i, item) {
            buttons[item.grade]=item.classList;
        });
//        console.log(buttons);
        $('.gradeUli').html(template("gradeList",{data:buttons}));
        $('.gradeUli li').eq(0).click();
        $('.classDiv').html(template("classList",{data:buttons}));
        $('.addClass').html(template("addClassList",{data:res}));
        $('.classUli_'+$('.gradeUli li').eq(0).html()).show();
        //        console.log(res);
    });
    $(document).on("click",'.gradeUli li',function () {
        $(this).addClass("active").siblings().removeClass("active");
        var liValue = $(this).html();
        $('.classUli_'+liValue).show().siblings().hide();
    });
    $(document).on("click",'.classUli li',function () {
        $(this).toggleClass("active");
        if($(this).hasClass("active")){
//            $('.addClass').remove($(this));
            $('.addClass').append("<"+ "li" + " class=" + $(this).data("info") + " data-info=" + $(this).data("info") +">" +'<span>' + $(this).data("name") +"</span></li>");
        }else{
            $('.'+ $(this).data("info")).remove();
        }
    });
</script>
</@layout.page>
