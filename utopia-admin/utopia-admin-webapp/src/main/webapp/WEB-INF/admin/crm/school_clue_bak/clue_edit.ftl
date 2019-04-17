<style>
    table > td { width: 200px; }
    td > select { width: 170px; }
    td > input { width: 156px; }
    .input-middle { width: 419px; }
</style>
<div id="clue-edit" title="修改学校线索" style="font-size: small; display: none" clue-id="" updateTime="">
    <table width="100%">
        <tr>
            <td><strong>所属省份：</strong><select id="provinceCode" onchange="region.cities(this.value)"></select></td>
            <td><strong>所属城市：</strong><select id="cityCode" onchange="region.counties(this.value)"></select></td>
            <td><strong>所属区域：</strong><select id="countyCode" name="regionCode"></select></td>
        </tr>
        <tr>
            <td colspan="2"><strong>主干名称：</strong><input id="clue-edit-cmainName" type="text" class="input-middle"/></td>
            <td><strong>分校名称：</strong><input id="clue-edit-schoolDistrict" type="text"/></td>
        </tr>
        <tr>
            <td colspan="2"><strong>学校地址：</strong><input id="clue-edit-address" type="text" class="input-middle"/></td>
            <td>
                <strong>小/中/高：</strong>
                <select id="clue-edit-schoolPhase">
                    <option value="1">小学</option>
                    <option value="2">中学</option>
                    <option value="4">高学</option>
                </select>
            </td>
        </tr>
        <tr>
            <td>
                <strong>记录操作：审核修改学校</strong>
            </td>
            <td colspan="1"></td>
            <td style="text-align: center;">
                <input type="button" class="btn btn-primary" value="提  交" style="width:80px; margin-right: 20px;" onclick="saveClue()"/>
                <input type="button" class="btn" value="取  消" style="width:80px; margin-left: 20px;" onclick="closeDialog('clue-edit')"/>
            </td>
        </tr>
    </table>
</div>

<script type="text/javascript">
    function editClue(clueId) {
        if (blankString(clueId)) {
            alert("无效的线索ID");
            return false;
        }
        $("#clue-edit").attr("clue-id", clueId);
        $.ajax({
            url: "/crm/school_clue/base_clue.vpage",
            type: "POST",
            data: {
                "id": clueId
            },
            success: function (data) {
                if (!data) {
                    alert("操作失败！");
                } else {
                    iEdit(data);
                    $("#clue-edit").dialog({
                        height: "auto",
                        width: "800",
                        modal: true,
                        autoOpen: true
                    });
                }
            }
        });
    }

    function iEdit(clue) {
        region.provinces(clue.provinceCode, clue.cityCode, clue.countyCode);
        $("#clue-edit").attr("updateTime", clue.updateTime);
        $("#clue-edit-cmainName").val(clue.cmainName);
        $("#clue-edit-schoolDistrict").val(clue.schoolDistrict);
        $("#clue-edit-address").val(clue.address);
        $("#clue-edit-schoolPhase").val(clue.schoolPhase);
    }

    function saveClue() {
        var regionCode = $("#countyCode").val();
        if (!validNumber(regionCode)) {
            alert("无效的地区信息！");
            return false;
        }
        var cmainName = $("#clue-edit-cmainName").val();
        if (blankString(cmainName)) {
            alert("无效的学主干名称！");
            return false;
        }
        var schoolDistrict = $("#clue-edit-schoolDistrict").val();
        var schoolPhase = $("#clue-edit-schoolPhase").val();
        if (blankString(schoolPhase)) {
            alert("无效的学校阶段！");
            return false;
        }
        var address = $("#clue-edit-address").val();
        var clueId = $("#clue-edit").attr("clue-id");
        var updateTime = $("#clue-edit").attr("updateTime");
        $.ajax({
            url: "/crm/school_clue/save_base_clue.vpage",
            type: "POST",
            data: {
                "id": clueId,
                "regionCode": regionCode,
                "cmainName" : cmainName,
                "schoolDistrict" : schoolDistrict,
                "schoolPhase": schoolPhase,
                "address": address,
                "updateTime": updateTime
            },
            success: function (data) {
                if (data.success) {
                    if (confirm("操作成功，是否刷新页面查看最新记录状态？")) {
                        $("#iform").submit();
                    }
                } else {
                    alert(data.info);
                }
            }
        });
    }
</script>
