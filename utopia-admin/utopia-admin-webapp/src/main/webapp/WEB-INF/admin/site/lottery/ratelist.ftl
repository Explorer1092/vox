<#-- @ftlvariable name="productTypeList" type="java.util.List<java.lang.String>" -->
<#-- @ftlvariable name="requestContext" type="com.voxlearning.utopia.admin.interceptor.AdminHttpRequestContext" -->
<#import "../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=4>
<#--<link href="${requestContext.webAppContextPath}/public/css/datetimepicker.css" rel="stylesheet">
<script src="${requestContext.webAppContextPath}/public/js/bootstrap-datetimepicker.min.js"></script>-->
<style>
    span { font: "arial"; }
</style>
<div id="main_container" class="span9">
    <legend>
        <a href="/site/lottery/new/ratelist.vpage">活动奖品编辑</a>&nbsp;&nbsp;&nbsp;&nbsp;
        <strong>中奖率调整</strong>
        &nbsp;&nbsp;
        <small style="color: red">
            友情提示：各项中奖率之和要等于1000000，否则后果自负
        </small>
    </legend>
    <div>
        <form id="s_form" action="${requestContext.webAppContextPath}/site/lottery/ratelist.vpage" method="post" class="form-horizontal">
            <#--<fieldset>
                <legend>中奖率调整（分母为1000000）<span style="color: red">友情提示：各项中奖率之和要等于1000000，否则后果自负<span></legend>
            </fieldset>-->
            <ul class="inline">
                <li>
                    活动：<select name="campaignId" id="campaignId">
                <#--<option value="7" <#if campaignId == 7>selected="selected"</#if>>老师转盘抽奖</option>-->
                    <option value="57" <#if campaignId == 57>selected="selected"</#if>>开学大礼包抽奖</option>
                <#--<option value="46" <#if campaignId == 46>selected="selected"</#if>>中学老师抽奖</option>-->
                    <option value="49" <#if campaignId == 49>selected="selected"</#if>>APP大爆料宝箱抽奖</option>
                    <option value="51" <#if campaignId == 51>selected="selected"</#if>>17奖学金金奖池</option>
                    <option value="52" <#if campaignId == 52>selected="selected"</#if>>17奖学金银奖池</option>
                    <option value="53" <#if campaignId == 53>selected="selected"</#if>>17奖学金铜奖池</option>
                    <option value="54" <#if campaignId == 54>selected="selected"</#if>>六一点读机抽奖</option>
                    <option value="56" <#if campaignId == 56>selected="selected"</#if>>点读机打包购买抽奖</option>
                    <option value="58" <#if campaignId == 56>selected="selected"</#if>>阿分题英语21天学习活动抽奖</option>
                    <option value="59" <#if campaignId == 59>selected="selected"</#if>>直播双十二9.9抽奖活动</option>
                    <option value="60" <#if campaignId == 60>selected="selected"</#if>>2017寒假作业抽奖</option>
                    <option value="61" <#if campaignId == 61>selected="selected"</#if>>阿分题数学寒假练题大赛活动抽奖</option>
                    <option value="62" <#if campaignId == 62>selected="selected"</#if>>21天全能学霸养成计划活动抽奖</option>
                    <option value="63" <#if campaignId == 63>selected="selected"</#if>>学生APP奖品中心抽奖</option>
                    <option value="64" <#if campaignId == 64>selected="selected"</#if>>初中英语老师布置作业抽奖</option>
                    <option value="65" <#if campaignId == 65>selected="selected"</#if>>2018暑假作业抽奖</option>
                </select>
                </li>
                <li>
                    <button id="selectTable" type="submit" class="btn btn-primary">查 询</button>
                    <button id="editBut" type="button" class="btn btn-primary">提交</button>
                </li>
            </ul>
        </form>
    </div>
    <div id="data_table_journal">
        <table class="table table-striped table-bordered so_checkboxs" so_checkboxs_values="">
            <tr>
                <td>ID</td>
                <td>活动ID</td>
                <td>奖项</td>
                <td>奖品</td>
                <td>中奖率</td>
            </tr>
            <#if lotteries?? >
                <#list lotteries as lottery >
                    <tr>
                        <td>${lottery.id!}</td>
                        <td>${lottery.campaignId!}</td>
                        <td>${lottery.awardLevelName!}</td>
                        <td>${lottery.awardName!}</td>
                        <td><input name="rate_${lottery.id!}" data-lotteryid="${lottery.id!}" value="${lottery.awardRate!}" /></td>
                    </tr>
                </#list>
            </#if>
        </table>
    </div>
</div>

<script>
    $(function() {
        $('#editBut').on('click', function() {
            var $inputs = $("#data_table_journal").find("input[data-lotteryid]");
            var data = [];
            var keep = true;
            $inputs.each(function(index, value){
                var $self = $(value);
                var check = $self.val();
                if(check != "" && isFinite(check)){
                    data.push($self.attr("data-lotteryid") + "_" + check);
                }else{
                    keep = false;
                }
            });

            if(!keep){
                alert("请检查数据格式");
                return false;
            }
            $.ajax({
                type: 'post',
                url: 'editrate.vpage',
                data: {rates : data.toString()},
                success: function (data){
                    alert(data.info);
                }
            });
        });
    });

</script>
</@layout_default.page>