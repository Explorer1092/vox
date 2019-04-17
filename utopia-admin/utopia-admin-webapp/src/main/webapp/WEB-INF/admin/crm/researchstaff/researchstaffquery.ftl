<#-- @ftlvariable name="province.name" type="java.lang.String" -->
<#-- @ftlvariable name="province.code" type="java.lang.Integer" -->
<#-- @ftlvariable name="provinces" type="java.util.List" -->
<#macro queryPage>
<style>

    label input{
        margin-top: 7px;
    }

    label select{
        margin-top: 7px;
    }

</style>
<div>
    <form action="researchstafflist.vpage" method="post">
        <fieldset>
            <legend>教研员查询</legend>
            <ul class="inline">
                <li>
                    <label>
                        教研员ID：<input name="researchStaffId" type="text"/>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li><label for="provCode">所属地区：
                    <select id="provCode" name="provCode" class="multiple district_select" next_level="cityCode" data-next_level="cityCode">
                        <option value="-1">全国</option>
                        <#if provinces??>
                            <#list provinces as p>
                                <option value="${p.code}"<#if conditionMap?? && p.code ==conditionMap["provCode"]>selected</#if>>${p.name}</option>
                            </#list>
                        </#if>
                    </select>
                </label>
                </li>
                <li>
                    <label for="cityCode">
                        所在市：
                        <select id="cityCode" data-init='false' name="cityCode" class="multiple district_select" next_level="countyCode" data-next_level="countyCode">
                            <option value="-1">全部</option>
                        </select>
                    </label>
                </li>
                <li>
                    <label for="countyCode">
                        所在区：
                        <select id="countyCode" data-init='false' name="countyCode" class="multiple district_select">
                            <option value="-1">全部</option>
                        </select>
                    </label>
                </li>
            </ul>
            <ul class="inline">
                <li>
                    <button id="query_info_btn" type="submit" class="btn btn-primary">提交</button>
                    <input type="hidden" id="prohvCodeCon" <#if conditionMap??>value="${conditionMap["provCode"]!}"</#if>>
                    <input type="hidden" id="cityCodeCon" <#if conditionMap??>value="${conditionMap["cityCode"]!}"</#if>>
                    <input type="hidden" id="countyCodeCon" <#if conditionMap??>value="${conditionMap["countyCode"]!}"</#if>>
                </li>
            </ul>
        </fieldset>
    </form>
</div>
<script>
    // 省,市,区
    $(function () {
        <#if researchStaffInfoList?has_content && researchStaffInfoList?size == 1>
            window.open("researchstaffhomepage.vpage?researchStaffId=${researchStaffInfoList[0].userId!''}", "_blank");
        </#if>
        $(document).keydown(function (evt) {
            if (evt.keyCode === 13) {
                $('#query_info_btn').click();
            }
        });

        function clearNextLevel(obj) {
            if (obj.attr("next_level")) {
                clearNextLevel($("#" + obj.attr("next_level")).html('<option value=""></option>'));
            }
        }

        var provCode = $("#provCodeCon").val();
        var cityCode = $("#cityCodeCon").val();
        var countyCode = $("#countyCodeCon").val();

        //地区部分
        $(".district_select").on("change", function () {
            var html = null;
            var $this = $(this);
            var next_level = $this.attr("next_level");
            var regionCode = $this.val();
            if (next_level) {
                next_level = $("#" + next_level);
                clearNextLevel($this);
                $.ajax({
                    type: "get",
                    url: "../user/regionlist.vpage",
                    data: {
                        regionCode: regionCode
                    },
                    success: function (data) {
                        html = '';
                        $.each(data.regionList,function(index,region){
                            html += '<option value="' + region["code"] + '"';
                            if (region["code"] == provCode || region["code"] == cityCode || region["code"] == countyCode) {
                                html += 'selected="selected"';
                            }
                            html += '>' + region["name"] + '</option>';
                        });

                        next_level.html(html);
                        next_level.trigger('change');
                    }
                });
            }
        });

        $("#provCode").trigger('change');

        <#if conditionMap?has_content>
            $('input[name="researchStaffName"]').val('${conditionMap.researchStaffName!}');
            $('input[name="researchStaffId"]').val('${conditionMap.researchStaffId!}');
            $('select[name="disabled"]').val('${conditionMap.disabled!}');
            $('input[name="phone"]').val('${conditionMap.phone!}');
            $('select[name="subject"]').val('${conditionMap.subject!}');
        </#if>

    });

</script>
</#macro>