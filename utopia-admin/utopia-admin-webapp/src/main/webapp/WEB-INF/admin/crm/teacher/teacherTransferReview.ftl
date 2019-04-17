<div id="review-detail" title="审核" style="font-size: small; display: none" task-id="">
    <br>
    <table width="100%">
        <tr>
            <td><strong>其它联系人：</strong><input id="otherLinkMan" type="text"/></td>
            <td><strong>转校是否正确</strong><input id="affirmTransferSchool" type="checkbox" style="margin: 0 0 2px 7px;"/>
            </td>
            <td><strong>转班是否正确</strong><input id="affirmTransferClass" type="checkbox" style="margin: 0 0 2px 7px;"/>
            </td>
        </tr>
        <tr>
            <td><strong>转校原因： </strong><input id="transferSchoolReason" type="text" style="margin-left: 11px;"/></td>
        </tr>
        <tr>
            <td><strong>备注：</strong><input type="text" id="remark" style="margin-left: 41px;"/></td>
        </tr>
        <tr>

            <td style="text-align: center;">
                <input type="button" class="btn btn-primary" value="提  交" style="width:80px; margin-right: 20px;"
                       onclick="saveReview()"/>
                <input type="button" class="btn" value="取  消" style="width:80px; margin-left: 20px;"
                       onclick="closeDialog('review-detail')"/>
            </td>
        </tr>
    </table>
</div>
<script type="application/javascript">
    function saveReview() {
        var data = {
            id: $("#review-detail").attr("task-id"),
            otherLinkMan: $("#otherLinkMan").val(),
            affirmTransferSchool: $("#affirmTransferSchool").is(":checked"),
            affirmTransferClass: $("#affirmTransferClass").is(":checked"),
            transferSchoolReason: $("#transferSchoolReason").val(),
            remark: $("#remark").val()
        };
        $.post("update_review_info.vpage", data, function (res) {
            if (res.success) {
                alert("审核完毕");
                window.location.reload();
            } else {
                alert(res.info);
            }
        })
    }
</script>