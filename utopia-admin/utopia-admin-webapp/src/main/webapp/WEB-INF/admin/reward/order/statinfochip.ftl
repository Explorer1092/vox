<#-- @ftlvariable name="adminDictList" type="java.util.List<com.voxlearning.utopia.admin.persist.entity.AdminDict>" -->
<div>
    <table class="table table-hover table-striped table-bordered">
        <tr>
            <th style="width:30px;">ID</th>
            <th>奖品类型</th>
            <th>奖品名称</th>
            <th>单品名称</th>
            <th>总兑换数量</th>
            <th>老师兑换数量</th>
            <th>（园丁豆）总价</th>
            <th>学生兑换数量</th>
            <th>（学豆）总价</th>
            <th>中学老师兑换数量</th>
            <th>（学豆）总价</th>
            <th>总学豆</th>
        </tr>
    <#if results?has_content>
        <#list results as result>
            <tr>
                <th>${result.productId!}</th>
                <th><#if (result.productType!'') == 'JPZX_SHIWU'>实物<#elseif (result.productType!'') == 'JPZX_TIYAN'>虚拟<#elseif (result.productType!'') == 'JPZX_PRESENT'>爱心捐赠<#else>未知</#if></th>
                <th>${result.productName!}</th>
                <td>${result.skuName!}</td>
                <td>${result.totalCount!}</td>
                <td>${result.teacherQuantity!}</td>
                <td>${result.teacherTotalPrice!}</td>
                <td>${result.studentQuantity!}</td>
                <td>${result.studentTotalPrice!}</td>
                <td>${result.juniorTeacherQuantity!}</td>
                <td>${result.juniorTeacherTotalPrice!}</td>
                <td>${(result.teacherTotalPrice!"0")?number * 10 + (result.studentTotalPrice!"0")?number + (result.juniorTeacherTotalPrice!"0")?number}</td>
            </tr>
        </#list>
    </#if>
    </table>
</div>