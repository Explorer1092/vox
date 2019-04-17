<#import "../layout_new_no_group.ftl" as layout>
<#assign groupName="work_record">
<@layout.page title ="填写学校名称" ><!--学校简称-->
<div class="mobileCRM-V2-header">
    <div class="inner">
        <div class="box">
            <a href="javascript:window.history.back();" class="headerBack">&lt;&nbsp;返回</a>
            <a href="javascript:void(0);" class="headerBtn">保存</a>
            <#if nameMode=="fullName">
                <div class="headerText">填写学校名称</div>
            <#else >
                <div class="headerText">填写学校简称</div>
            </#if>
        </div>
    </div>
</div>
<div class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
<#--<form action="save_school_name.vpage" method="POST" id="save-school-name" enctype="multipart/form-data">-->
    <ul class="mobileCRM-V2-list">
        <li>
            <div class="box">
                <input type="text"
                    <#if nameMode=="fullName">
                       placeholder="请填写学校名称"
                    <#else >
                       placeholder="请填写学校简称"
                    </#if>
                       class="side-fr side-time" name="schoolName" id="schoolName" style="width:100%;text-align: left;"
                       value="${schoolName!''}">
                <#if nameMode=="fullName">
                    <input placeholder="分校信息（选填）" class="side-fr side-time" name="schoolDistrict" id="schoolDistrict"
                           style="width:100%;text-align: left;" value="${schoolDistrict!''}">
                </#if>
                <input type="hidden" name="nameMode" id="nameMode" value="${nameMode!''}">
            </div>
        </li>
    </ul>
<#--</form>-->
</div>
    <#if nameMode=="fullName">
    <div class="mobileCRM-V2-info mobileCRM-V2-mt">
        注意: <br>
        1.名称需要与校牌照片一致<br>
        2.校区或学部信息请用括号括起来写在名称后<br>
        3.若是高中低学部，直接填写高或中或低<br>
        4.若为国际学部，括号中填写‘国际’<br>
        5.若为九年一贯制学校，括号中填写‘小学’或‘初中’<br>
    </div>
    <#else >
    <div class="mobileCRM-V2-info mobileCRM-V2-mt">
        注意: <br>
        学校简称不能与同区域任何一所学校重复
    </div>
    </#if>
<!-- 如果存在重复学校增加提示 -->

<div id="errorMessage" hidden class="mobileCRM-V2-box mobileCRM-V2-info mobileCRM-V2-mt">
    <div hidden id="shotError">
        <div style="color: red;padding-top: 12px;"> 提示：该简称学校已存在！</div>
    </div>
    <div hidden id="fullError">
        <div style="color: red;padding: 12px 0 12px 0;"> 提示！请确保要新建学校与一下疑似重复学校不重合</div>
        <ul id="promptList" class="mobileCRM-V2-list">

        </ul>

    </div>
</div>
    <#if nameMode=="fullName">
    <div hidden class="schoolCluePhotoFooter">
        <div class="takePhoto" id="add_record">依然使用</div>
    </div>
    <script>
        $("#add_record").on("click", function () {
            schoolName.schoolName = $("#schoolName").val();
            $.post('still_used_school_name.vpage', schoolName, function (data) {
                if (data.success) {
                    window.location.href = 'javascript:window.history.back();';
                } else {
                    alert(data.info);
                }
            })
        })
    </script>
    </#if>
<script>
    var schoolName = {
        "schoolName": $("#schoolName").val(),
        "schoolDistrict": $("#schoolDistrict").val(),
        "nameMode": $("#nameMode").val()
    };

    $(".headerBtn").click(function () {
        $("#errorMessage").hide();
        $("#shotError").hide();
        $("#fullError").hide();
        schoolName.schoolName = $("#schoolName").val();
        schoolName.schoolDistrict = $("#schoolDistrict").val();
        if (schoolName.schoolName.trim() == "") {
            alert("学校名称不能为空");
            return;
        }
        $.post('save_school_name.vpage', schoolName, function (data) {
            if (data.success) {
                if (data.info) {
                    alert(data.info);
                }
                window.location.href = 'javascript:window.history.back();';
            } else {
                if (data.nameMode) {
                    $("#errorMessage").show();
                    if (data.nameMode == "fullName") {
                        $("#fullError").show();
                        var promptList = data.promptList;
                        var schoolList = "";
                        console.info(promptList);
                        for (var i = 0; i < promptList.length; i++) {
                            schoolList += '<li><div>' + promptList[i].schoolName + '</div><div>学校ID:' + promptList[i].schoolId + '(' + promptList[i].regionName + ')</div></li>';
                        }
                        $("#promptList").html(schoolList);
                        $(".schoolCluePhotoFooter").show();
                    } else {
                        $("#shotError").show();
                    }
                }
            }
        })
    });
</script>
</@layout.page>