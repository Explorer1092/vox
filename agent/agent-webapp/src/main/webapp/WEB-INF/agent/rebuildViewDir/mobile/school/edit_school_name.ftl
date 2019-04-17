<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="编辑学校名称" pageJs="editSchool" footerIndex=4>
    <@sugar.capsule css=['school']/>
<style>
    .holder{background:#f1f2f5}
</style>
<#--<div class="head fixed-head">-->
    <#--<a class="return" href="javascript:window.history.back()"><i class="return-icon"></i>返回</a>-->
    <#--<span class="return-line"></span>-->
    <#--<span class="h-title">编辑学校名称</span>-->
    <a style="display:none;" href="javascript:void(0)" class="inner-right js-submit">提交</a>
<#--</div>-->
<div style="width:100%;background:#f1f2f5;padding-top:.5rem">
        <div class="schoolParticular-list">
            <ul>
                <li><input id="mainName" value="<#if mainName??>${mainName!''}</#if>" type="text" placeholder="请输入学校主干名称"></li>
                <li><input id="schoolDistrict" type="text"  value="<#if schoolDistrict??>${schoolDistrict!''}</#if>" placeholder="请输入分部名称(有分校必填)"></li>
            </ul>
        </div>
        <div class="particular-subtitle">示例: 北京市东城区第二实验小学 - 朝阳分校</div>
        <div class="schoolParticular-list">
            <ul>
                <li><input type="text" readonly="readonly" placeholder="北京市东城区第二实验小学"></li>
                <li><input type="text" readonly="readonly" placeholder="朝阳分校"></li>
            </ul>
        </div>
        <div class="particular-subtitle">注意</div>
        <div class="schoolParticular-info">
            <p>1.校名按照校牌填写</p>
            <p>2.学校有分校，将该分校的信息单独填写在分部信息中，无分校则只填主干名称</p>
            <p>3.九年一贯制学校无需填写小学部和中学部</p>
        </div>
</div>
    <#--</div>-->
    <#--<div class="item" id="repatePane" style="display: none;">-->
        <#--<div class="content" id="school_list">-->


        <#--</div>-->
    <#--</div>-->
    <div id="repatePane" class="schoolParticular-pop" style="display:none">

    </div>


<script type="text/html" id="repetition_school">
    <div class="inner">
        <h1>名称重复提示</h1>
        <p class="item">请确保新建学校于系统中已经存在的学校不重复</p>
        <ul>
            <%for (var i=0;i< repeatSchool.length;i++){%>
                <li>名称：<%=repeatSchool[i].schoolName%>ID:<%=repeatSchool[i].schoolId%> (<%=repeatSchool[i].regionName%>)</li>
            <%}%>
        </ul>
        <div class="btn">
            <a href="javascript:void(0);" class="white_btn js-mobileIndex">取消创建</a>
            <a href="javascript:void(0);" class="stillSubmit">依然创建</a>
        </div>
    </div>
</script>

<script type="text/html" id="repetition_all_same_school">
    <div class="inner">
        <h1>提交失败</h1>
        <p class="info">提交失败,系统中已存在名称完全相同的学校
            <%for (var i=0;i< repeatSchool.length;i++){%>
                <br>学校ID:<%=repeatSchool[i].schoolId%>
            <%}%>
        </p>
        <div class="btn">
            <a href="javascript:void(0);" class="js-mobileIndex">返回首页</a>
        </div>
    </div>
</script>

<script type="text/javascript">
var AT = agentTool();
$(document).on("ready",function(){
    var schoolId = ${schoolId!0};
    $(document).on("click",".js-submit",function () {
        var mainName = $("#mainName").val();
        var schoolDistrict = $("#schoolDistrict").val();
        var data = {
            nameType: "name",
            schoolName: mainName,
            schoolDistrict: schoolDistrict,
            schoolId:schoolId
        };
        $.post("save_name.vpage", data, function (res) {
            if (res.success) {
                //if(schoolId == 0){
                disMissViewCallBack();
                //}else{
                //    location.href = '/mobile/school_clue/update_school_info.vpage?schoolId='+schoolId;
                //}

            } else {
                var repeatSchool = res.repeatSchool;
                if (repeatSchool) {
                    if(res.allSame){
                        $("#repatePane").html(template("repetition_all_same_school", {repeatSchool: repeatSchool}));
                        $("#repatePane").show();
                    }else{
                        $("#repatePane").html(template("repetition_school", {repeatSchool: repeatSchool}));
                        $("#repatePane").show();

                    }
                } else {
                    AT.alert(res.info);
                }
            }
        });
    });

    $(document).on("click",".stillSubmit",function () {
        var data = {
            name: $("#mainName").val(),
            schoolDistrict: $("#schoolDistrict").val()
        };

        $.post('continue_use_school_name.vpage', data, function (res) {
            if (res.success) {
                disMissViewCallBack();
            } else {
                AT.alert(res.info);
            }
        })
    });

    $(document).on('click','.js-mobileIndex',function(){
        //暂时解决取消添加新学校跳转页面
        $("#repatePane").hide();
        // window.location.href = "/mobile/performance/index.vpage" ;
    });

});
</script>
</@layout.page>