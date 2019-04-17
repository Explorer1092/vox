<#import "../../module.ftl" as module>
<@module.page
title="机构管理"
pageJsFile={"siteJs" : "public/script/basic/shop_detail"}
pageJs=["siteJs"]
leftMenu="机构管理"
>
<style>
    .input-control > label{line-height: 20px;width: 109px;}
    .input-control > input, .input-control > textarea{width: 715px; }
    .one-page .tec_center{text-align:center}
</style>
<div class="bread-nav">
    <a class="parent-dir" href="/basic/shop/index.vpage">机构列表</a>
    &gt;
    <a class="current-dir" href="javascript:void(0);" style="cursor: default">机构详情</a>
</div>
<h3 class="h3-title">
    机构详情
</h3>
<#--<script type="text/javascript" src="http://api.map.baidu.com/api?v=2.0&ak=stpdH3wKubAUFfjRZ8ELoN2A"></script>-->
<script type="text/javascript" src="https://webapi.amap.com/maps?v=1.3&key=ae2ed07e893eb41d259a47e7ba258c00&plugin=AMap.Geocoder"></script>
    <div style="margin-top: 10px;">
        <div class="input-control">
            <label>关联品牌：</label>
            <span>${(mizarBrand.brandName)!'--'}</span>
        </div>

        <div class="input-control">
            <label>机构全称：</label>
            <span>
                ${(mizarShop.fullName)!'--'}
            </span>
        </div>
        <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label>VIP：</label>
                <span>
                    <label for="vip"></label>
                    <input type="checkbox" name="vip" id="vip" readonly disabled <#if (mizarShop.vip)??><#if mizarShop.vip==true>checked="checked"</#if></#if>">
                </span>
            </div>
            <div class="input-control">
                <label>是否合作机构：</label>
                <span>
                    <label for="vip"></label>
                        <input type="checkbox" name="cooperator" id="cooperator" readonly disabled <#if (mizarShop.cooperator)??><#if mizarShop.cooperator==true>checked="checked"</#if></#if>">
                </span>
            </div>
        </#if>
        <div class="input-control">
            <label>负责BD：</label>
            <span>
                <#if userList??>
                    <#list  userList as user>
                    ${user.accountName!}-${user.mobile!}
                    </#list>
                </#if>
            </span>
        </div>
        <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label>机构简称：</label>
                <span>${(mizarShop.shortName)!'--'}</span>
            </div>
        </#if>
        <div class="input-control">
            <label>机构介绍：</label>
            <div style="font-size:15px; line-height: 28px;padding-left:110px;">${(mizarShop.introduction)!'--'}</div>
        </div>
        <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label>机构类型：</label>
                <span>${(mizarShop.shopType)!'--'}</span>
            </div>
        </#if>
        <div class="input-control">
            <label>地区编码：</label>
            <span>${regionName!'--'}</span>
        </div>
        <div class="input-control">
            <label>所属商圈：</label>
            <span>${(mizarShop.tradeArea)!'--'}</span>
        </div>
        <div class="input-control">
            <label>详细地址：</label>
            <span>${(mizarShop.address)!'--'}</span>
        </div>
    <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label>适配年级：</label>
                <span>${(mizarShop.matchGrade)!'--'}</span>
            </div>
            <div class="input-control">
                <label>合作等级分数：</label>
                <span>${(mizarShop.cooperationLevel)!'--'}</span>
            </div>
            <div class="input-control">
                <label>人工调整分数：</label>
                <span>${(mizarShop.adjustScore)!'--'}</span>
            </div>
        </#if>
        <div class="input-control">
            <label>联系电话：</label>
            <span>
                <#if (mizarShop.contactPhone)??>
                    <#list mizarShop.contactPhone as phone>
                        ${phone}
                        <#if phone_has_next>,</#if>
                    </#list>
                </#if>
            </span>
        </div>
        <#if !currentUser.isShopOwner() && !currentUser.isBD()>
            <div class="input-control">
                <label>是否线上机构：</label>
                <span>
                    <#if (mizarShop.type)== 1>线上</#if>
                    <#if (mizarShop.type) == 0>线下</#if>
                </span>
            </div>
        </#if>
        <div class="input-control">
            <label>状态：</label>
            <#switch (mizarShop.shopStatus)!>
                <#case 'ONLINE'> 在线 <#break>
                <#case 'OFFLINE'> 离线 <#break>
                <#case 'PENDING'> 待审核 <#break>
            </#switch>
            <#--${(mizarShop.shopStatus)!}-->
        </div>
        <div class="input-control">
            <label>GPS信息：</label>
            <span>
            经度: <input style="border: none;background: #ebeef2;" readonly id="longitude" name="longitude"  <#if mizarShop??>value="${mizarShop.longitude!}</#if>"/>&nbsp;
            纬度: <input style="border: none;background: #ebeef2;" readonly id="latitude" name="latitude" <#if mizarShop??>value="${mizarShop.latitude!}</#if>" />
            </span>
        </div>
        <div class="input-control">
            <label>高德地图：</label>
            <div>
                <div id="innerMap" data-disable="true" class="inner-map"></div>
            </div>
        </div>
        <div class="input-control">
            <label>一级分类：</label>
            <#--<label for="firstCategory"></label>-->
            <span>
                <#if (mizarShop.firstCategory)??>
                    <#list mizarShop.firstCategory as c>
                    ${c}<#if c_has_next>,</#if>
                    </#list>
                </#if>
            </span>
            <#--<select id="firstCategory" name="firstCategory" readonly="" disabled style="height: 30px;">-->
                <#--<option value="少儿教育" <#if mizarShop.firstCategory??><#if mizarShop.firstCategory?seq_contains("少儿教育")>selected</#if></#if>>少儿教育</option>-->
                <#--<option value="少儿外语" <#if mizarShop.firstCategory??><#if mizarShop.firstCategory?seq_contains("少儿外语")>selected</#if></#if>>少儿外语</option>-->
                <#--<option value="兴趣才艺" <#if mizarShop.firstCategory??><#if mizarShop.firstCategory?seq_contains("兴趣才艺")>selected</#if></#if>>兴趣才艺</option>-->
                <#--<option value="游学玩乐" <#if mizarShop.firstCategory??><#if mizarShop.firstCategory?seq_contains("游学玩乐")>selected</#if></#if>>游学玩乐</option>-->
            <#--</select>-->
        </div>
        <div class="input-control">
            <label>二级分类：</label>
            <span>
                <#if (mizarShop.secondCategory)??>
                    <#list mizarShop.secondCategory as c>
                        ${c}<#if c_has_next>,</#if>
                    </#list>
                </#if>
            </span>
        </div>
        <div class="input-control">
            <label>到店礼：</label>
            <span>${(mizarShop.welcomeGift)!'--'}</span>
        </div>

        <div class="input-control">
            <label>机构图片：</label>
            <div class="image-preview brandPhoto clearfix" style="padding-left:108px;">
                <#if mizarShop?? && mizarShop.photo?? >
                    <#list mizarShop.photo as p>
                        <div class="image">
                            <img src="${p!}" />
                        </div>
                    </#list>
                </#if>
            </div>
        </div>

        <div class="clearfix" style="clear:both;">
            <div class="input-control">
                <label>师资力量：</label>
            </div>
            <div class="image-preview clearfix" style="padding-left:108px">
                <div id="facultyBox">
                    <#if (mizarShop.faculty)??>
                        <table class="data-table one-page displayed">
                            <thead>
                            <tr>
                                <th class="tec_center">教师名称</th>
                                <th class="tec_center">教师教龄</th>
                                <th class="tec_center">教师科目</th>
                                <th class="tec_center">教师描述</th>
                                <th class="tec_center">教师图片</th>
                            </tr>
                            </thead>
                            <tbody>
                                <#list mizarShop.faculty as facultyMap>
                                <tr>
                                    <td class="tec_center">${facultyMap["name"]!''}</td>
                                    <td class="tec_center">${facultyMap["experience"]!''}</td>
                                    <td class="tec_center">${facultyMap["course"]!''}</td>
                                    <td class="tec_center">${facultyMap["description"]!''}</td>
                                    <td class="tec_center">  <div class="image" style="float: none; display: inline-block;"><img src='${facultyMap["photo"]!}' <#--style="width: 60px;height: 60px"-->></div></td>
                                </tr>
                                </#list>
                            </tbody>
                        </table>
                    </#if>
                </div>
            </div>
        </div>
        <div class=" submit-box">
            <a id="abandon-btn" class="submit-btn abandon-btn" href="/basic/shop/index.vpage">返回</a>
        </div>
    </div>
</@module.page>