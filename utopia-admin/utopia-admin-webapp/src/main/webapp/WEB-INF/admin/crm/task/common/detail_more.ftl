<div id="detail-more" title="详细" style="font-size: small; display: none"></div>

<script type="text/javascript">
    function moreDetail(node) {
        $("#detail-more").html($(node).attr("detail"));
        $("#detail-more").dialog({
            height: "auto",
            width: "500",
            autoOpen: true,
            buttons: {
                "OK": function () {
                    $(this).dialog("close");
                }
            }
        });
    }
</script>