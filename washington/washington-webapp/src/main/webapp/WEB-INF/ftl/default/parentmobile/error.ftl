<#import './layout.ftl' as layout>
<@layout.page className='Error' pageJs='error' title="错误详情页">
    <#escape x as x?html>
        <#include "errorTemple/errorBlock.ftl">
    </#escape>
</@layout.page>
