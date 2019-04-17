<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="处理包班申请" pageJs="clazzapply" footerIndex=2>
<@sugar.capsule css=['res']/>
<div class="crmList-box resources-box">
    <#--<div class="res-top fixed-head">-->
        <#--&lt;#&ndash;/mobile/resource/teacher/card.vpage?teacherId=${teacherId}&ndash;&gt;-->
        <#--<div class="return"><a href="javascript:window.history.back()"><i class="return-icon"></i>返回</a></div>-->
        <#--<span class="return-line"></span>-->
        <#--<span class="res-title">开通包班</span>-->
        <#--<a class="return orange-color js-confirm" href="javascript:void(0)" style="right:0;left:auto;padding-right:0.625rem;"><#if subjectClazz?? && subjectClazz?has_content> 开通</#if></a>-->
    <#--</div>-->
    <div id="clazz-apply" class="c-main">
        <#--渲染学科班级数据-->
    </div>
</div>
<div class="schoolParticular-pop" style="display: none;" id="repatePane">
    <div class="inner">
        <h1>开通包班</h1>
        <p class="info">是否确认</p>
        <div class="btn">
            <a href="javascript:void(0);" class="white_btn rejectBtn">否</a>
            <a href="javascript:void(0);" class="submitBtn">是</a>
        </div>
    </div>
</div>
<#--列表-->
<script type="text/html" id="T:学科和班级">
    <%if(tabHead.length){%>
    <div class="tips" style="padding-left: 2rem;">
        请选择老师所教的其他科目
    </div>
    <div class="f2 c-opts tab-head subject j-flex" style="margin-bottom:.5rem;border-top: .05rem solid #cdd3dc;">
        <%for(var i in tabHead){%>
            <span class="<%=tabHead[i][2]%>" data-subject="<%=tabHead[i][1]%>"><%=tabHead[i][0]%></span>
        <%}%>
    </div>
    <div class="tips" style="padding-bottom:0.5rem;line-height:1rem;">
        请选择老师在该科目所教的班级<span style="color:#fc6648;">（仅选择老师在该科目所教 的一个班级即可，其余班级可由老师自主操作）</span>
    </div>
    <div class="tab-main" style="background-color: #FFF;overflow: hidden">
        <%for(var i in tabMain){%>
            <div class="clazz-list tab-head clazz">
                <%for(var j in tabMain[i]){%>
                <a href="javascript:void(0);" data-cid="<%=tabMain[i][j].cid%>" data-gid="<%=tabMain[i][j].gid%>"><%=tabMain[i][j].cname%></a>
                <%}%>
            </div>
        <%}%>
    </div>
    <%}else{%>
    <div class="tips" style="text-align: center">暂时无可申请的班级</div>
    <%}%>
</script>

<#--确定弹窗-->
<script type="text/html" id="T:确认弹窗">
<div class="clazz-popup">
    <div class="text">已为老师开通<%=subjectName%>学科权限，老师可以在教师端使用啦～</div>
    <div class="popup-btn">
        <a href="javascript:window.history.back()" style="background:#ff7d5a;color:#fff;border-bottom-right-radius:0.2rem;width:100%" class="js-submit">我知道了</a>
    </div>
</div>
<div class="popup-mask js-remove"></div>
</script>
<script>
    var AT = new agentTool();
    var subjectClazz=${json_encode(subjectClazz)};
    var teacher=${json_encode(teacher)};
    var school=${json_encode(school)};
    var tabHead=[],tabMain=[];
    for(var key in subjectClazz){
        if(subjectClazz.hasOwnProperty(key)){
            switch(key){
                case "ENGLISH":
                    tabHead.push(["英语","ENGLISH","english"]);
                    break;
                case "MATH":
                    tabHead.push(["数学","MATH","math"]);
                    break;
                case "CHINESE":
                    tabHead.push(["语文","CHINESE","chinese"]);
                    break;
                default:
                    break;
            }
            tabMain.push(subjectClazz[key]);
        }
    }
    $("#clazz-apply").html(template("T:学科和班级",{tabHead:tabHead,tabMain:tabMain}));

    $(".subject").children().first().addClass("the");

    //申请提交按钮
    var data={},subject;
    var confirmBoolean = true;
    $(".js-confirm").on("click",function(){
        if($(".clazz").children(".the").length){
                var clazz=$(".clazz").children(".the").eq(0);
                subject=$(".subject").children(".the").eq(0);
                data={
                    teacherId : teacher.id,
                    subject   : subject.data().subject,
                    clazzId   : clazz.data().cid
                };
                if(confirmBoolean){
                    $("#repatePane").show();
                    confirmBoolean = false;
                }
        }else{
            AT.alert("请先选择班级!");
            confirmBoolean = true;
        }
    });
    $(document).on("click",".submitBtn",function(){
        $("#repatePane").hide();
        $.post("clazz_apply.vpage",data,function(res){
            if(res.success){
                $(document.body).append(template("T:确认弹窗",{
                    subjectName : subject.html()
                }));
            }else{
                confirmBoolean = true;
                AT.alert(res.info);
            }
        });
    });

    $(document).on("click",".rejectBtn",function(){
        confirmBoolean = true;
        $("#repatePane").hide();
    });
</script>
</@layout.page>
