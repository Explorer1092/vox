<#import "../layoutTemplate/mobileLayoutBase.ftl" as layout>
<@layout.page title="教研员资源" pageJs="" footerIndex=4>
<@sugar.capsule css=['researchers']/>
<div class="crmList-box">
    <div class="res-top fixed-head">
        <a href="javascript:window.history.back();"><div class="return"><i class="return-icon"></i>返回</div></a>
        <span class="return-line"></span>
        <span class="res-title">选择教研员</span>
    </div>
</div>
<div class="selInstructor-box">
    <div class="sit-tab">
        <ul>
            <li class="active"><a href="javascript:void(0);">省级</a></li>
            <li><a href="javascript:void(0);">市级</a></li>
            <li><a href="javascript:void(0);">区级</a></li>
        </ul>
    </div>
    <#--一级联动-->
    <div class="sit-list oneLevel">
        <div class="sit-listLeft">
            <ul>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
            </ul>
        </div>
    </div>
    <#--三级联动-->
    <div class="sit-list threeLevel" style="display: none">
        <div class="sit-listLeft">
            <ul>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
            </ul>
        </div>
        <div class="sit-listBot"><#--去掉sit-listBot类是二级联动-->
            <ul>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
            </ul>
        </div>
        <div class="sit-listRight">
            <ul>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
                <li class="active">江苏省</li>
                <li>山东省</li>
                <li>安徽省</li>
                <li>甘肃省</li>
            </ul>
        </div>
    </div>
</div>


</@layout.page>