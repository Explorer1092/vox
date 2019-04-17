<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!-- 语法类数据-->
<#if lesson?exists>
<root>
    <gameTime>${gameTime!''}</gameTime>
    <timeXiShu>${timeXiShu}</timeXiShu>
    <dragGames>
        <#list sentences as sent>
            <game>
                <en>${sent.enText?html}</en>
                <cn>${sent.cnText?html}</cn>
            </game>
        </#list>
    </dragGames>
</root>
<#else>
<lessonId>null</lessonId>
</#if>