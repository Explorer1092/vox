<#import '../layout.ftl' as layout>

<@layout.page className='HomeworkAgreement' title="产品购买协议" pageJs="second">

<#assign topType = "topTitle">
<#assign topTitle = "产品购买协议">
<#include "../top.ftl" >

<#escape x as x?html>
    <#include "../../help/shopagreement_text.ftl">
</#escape>

</@layout.page>
