<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="资料包" pageJs="teamCountry" footerIndex=4>
    <@sugar.capsule css=['team']/>
<div class="crmList-box">
    <div class="tips">
        <#list datumType as item>
            <a href="javascript:void(0)" class="item dt  <#if  item.id == type>dashed</#if>">${item.desc!}</a>
            <div class="dd" <#if  item.id == type>style="display:block"</#if>>
                <#if dataPacket[item.id?string]??>
                    <#list dataPacket[item.id?string] as tip>
                        <a  href="javascript:;" onclick="openSecond('${tip.fileUrl!}')" class="item">${tip.fileName!}</a>
                    </#list>
                <#else>
                    <a  href="javascript:void(0)" class="item" style="text-align: center;background:none;text-indent: 0;">暂无~</a>
                </#if>
            </div>
        </#list>
    </div>
</div>
<script>
    $(document).on("click",".dt",function(){
        var $this=$(this);
        $this.next(".dd").slideToggle(function(){
            $this.toggleClass("dashed");
        });
    });
</script>
</@layout.page>