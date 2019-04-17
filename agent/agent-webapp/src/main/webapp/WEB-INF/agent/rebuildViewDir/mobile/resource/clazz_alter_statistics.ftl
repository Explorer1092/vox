<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="处理换班" pageJs="">
    <@sugar.capsule css=['home']/>
<style></style>
<div id="my_table">
    <#if clazzAlterStatistics??>
        <div class="view-box schoolRecord-box">
            <div class="srd-module">
                <div class="mTable bgTable" style="display: block">
                    <table  cellpadding="0" cellspacing="0">
                        <thead>
                        <tr>
                            <td>部门/人员</td>
                            <td class="sortable">待处理换班数量</td>
                        </tr>
                        </thead>
                        <tbody>
                            <#list clazzAlterStatistics as userRole>
                            <tr class="js-item" data-url="${userRole.nextUrl!""}">
                                <td>${userRole.name!""}</td>
                                <td class="group_itemCount">${userRole.count!0}</td>
                            </tr>
                            </#list>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </#if>
</div>
<script type="text/javascript">
    function getParam(name) {
        return location.href.match(new RegExp('[?#&]' + name + '=([^?#&]+)', 'i')) ? RegExp.$1 : '';
    }
    var schoolId = getParam('schoolId');
    $(document).on("click",".js-item",function () {
        openSecond("/mobile/performance/" + $(this).data("url") + "&schoolId=" + schoolId);
    });
    var countNum = function(countName){
        var count = 0;
        $('.mTable').find('.'+ countName).each(function(){
            if($(this).html() != ''){
                count += parseInt($(this).html());
            }
        });
        $('.mTable').find('.' + countName + "Num").html(count);
    };
    countNum("group_itemCount");

</script>
</@layout.page>
