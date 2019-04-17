<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="编辑学校简称/别名" pageJs="" footerIndex=4>
    <@sugar.capsule css=['school']/>
<div class="head fixed-head">
    <a class="return" href="javascript:window.history.back()"><i class="return-icon"></i>返回</a>
    <span class="return-line"></span>
    <span class="h-title">编辑学校简称/别名</span>
    <a href="javascript:void(0)" class="inner-right js-submit">提交</a>
</div>

<div class="flow">
    <div class="item undo">
        简称/别名
        <div class="inner-right">
            <input type="text" id="shortName" value="<#if shortName??>${shortName!''}</#if>" style="text-align: right;color: #50546d;font-size: 0.75rem;border: none;line-height: 1.3rem;" placeholder="请填写" maxlength="20">
        </div>
    </div>
    <div class="item">
        <div class="content">
            <p style="margin-top: .5rem">
                <span style="color: #ee5f5b;">注意: </span><br>
            </p>
            <p style="margin-top: .5rem">
                学校简称不能与同区域任何一所学校重复
            </p>
        </div>
    </div>
    <div class="item" id="shotError" style="display: none;">
        <div class="content">
            <p style="margin-top: 1rem;color: #ee5f5b;"> 提示：该简称学校已存在！</p>
        </div>
    </div>
</div>

<script type="text/javascript">
    var AT = new agentTool();
    $(function () {
        $(".js-submit").click(function () {
            var shortName = $("#shortName").val();
            var data = {
                nameType: "shortName",
                schoolName: shortName
            };
            console.log(data);
            $.post("save_name.vpage", data, function (res) {
                if (res.success) {
                    location.href = '/mobile/school_clue/add_school_info.vpage';
                } else {
                    var repeatSchool = res.repeatSchool;
                    if (repeatSchool) {
                        $("#shotError").show();
                    } else {
                        AT.alert(res.info);
                    }
                }
            });
        });
    });
</script>
</@layout.page>