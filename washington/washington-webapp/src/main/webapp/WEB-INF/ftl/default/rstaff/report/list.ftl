<#import "../module.ftl" as com>
<@com.page t=2 s=7>
<ul class="breadcrumb_vox">
    <li><a href="javascript:void(0);">报 告</a> <span class="divider">/</span></li>
    <li class="active">区热点题型统计</li>
</ul>
<ul class="inline_vox">
    <li>
        <select id="provinces" next_level="citys" class="int_vox district_select">
            <#list provinces as p>
                <option value="${p.key}">${p.value}</option>
            </#list>
        </select>
    </li>
    <li>
        <select id="citys" defaultValue="${currentUser.region.cityCode}" next_level="countys" class="int_vox district_select" style="width: 140px">
            <option value="0">市</option>
        </select>
    </li>
    <li>
        <select id="countys" defaultValue="${currentUser.region.countyCode}" class="int_vox district_select" style="width: 140px">
            <option value="0">区</option>
        </select>
    </li>
    <li>
        <a id="report_query_but" href="javascript:void (0)" class="btn_vox btn_vox_warning"> 查看 </a>
        <a id="report_query_my_but" href="javascript:void (0)" class="btn_vox btn_vox_warning">我所在的区</a>
    </li>
</ul>
<table class="table_vox table_vox_bordered table_vox_striped">
    <thead>
    <tr>
        <td> 排 序 </td>
        <td> 题 型 </td>
        <td>布置次数 </td>
    </tr>
    </thead>
    <tbody id="report_content_box">
        <tr>
            <th colspan="3" class="text_gray_9">请选择您要查看的省市区</th>
        </tr>
        <!-- 内容显示 -->
    </tbody>
</table>
<script>
    $(function(){
        /** 查看 */
        $("#report_query_but").on("click", function(){
            var provincesCode   = $("#provinces option:selected").val();
            var citysCode       = $("#citys option:selected").val();
            var countysCode     = $("#countys option:selected").val();

            $.get("/rstaff/report/listchip.vpage?provinceCode=" + provincesCode + "&cityCode=" + citysCode + "&countryCode=" + countysCode, function(data){
                $("#report_content_box").html(data);
            });
        });

        $("#report_query_my_but").on("click", function(){
            var provincesCode   = ${currentUser.region.provinceCode};
            var citysCode       = ${currentUser.region.cityCode};
            var countysCode     = ${currentUser.region.countyCode};

            $.get("/rstaff/report/listchip.vpage?provinceCode=" + provincesCode + "&cityCode=" + citysCode + "&countryCode=" + countysCode, function(data){
                $("#report_content_box").html(data);
            });

        });


        $("#provinces").val(${currentUser.region.provinceCode});

        App.districtSelect.init($("#provinces"));
    });
</script>
</@com.page>