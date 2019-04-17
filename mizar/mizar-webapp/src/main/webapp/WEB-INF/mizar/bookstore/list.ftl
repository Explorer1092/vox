<#import "../module.ftl" as module>
<@module.page
title="书店管理"
pageJsFile={"siteJs" : "public/script/bookstore/list"}
pageJs=["siteJs"]
leftMenu="书店管理"
>
    <@app.script href="/public/plugin/jquery/jquery-1.7.1.min.js"/>
<style>
    table thead th{ white-space: nowrap;}
</style>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="filter-form" action="/bookstore/manager/list.vpage" method="get">

        <div class="item">
            <p>门店名称</p>
            <input id="shopName"  value="${bookStoreName!}" maxLength="20" name="bookStoreName" class="v-select" placeholder="请输入门店名"/>
        </div>

        <div class="item">
            <p>门店ID</p>
            <input id="shopId" value="${bookStoreId!}" maxLength="10" name="bookStoreId" class="v-select" placeholder="请输入门店ID"/>
        </div>
        <div class="item">
            <p>手机号</p>
            <input id="phNum" value="${bookStorePhone!}" maxLength="11" name="bookStorePhone" class="v-select" placeholder="请输入手机号"/>
        </div>
        <div class="item">
            <p>联系人</p>
            <input id="linkman" value="${contactName!}" maxLength="10" name="contactName" class="v-select" placeholder="请输入联系人"/>
        </div>
        <div class="item" style="width:auto;margin-right:0;">
            <p style="color:transparent;">.</p>
            <input type="hidden" name="page" value="${page!0}" id="pageIndex">
            <a class="blue-btn" id="js-filter" style="float:left;" href="javascript:void(0)">搜索</a>
            <a class="blue-btn" id="reset" style="margin-left: 20px;" href="javascript:void(0)">重置</a>
        </div>
    </form>
    <div class="item" style="width:auto; float: right;">
        <p style="color:transparent;">.</p>
        <a class="blue-btn" onclick="check()"  href="javascript:void(0)">新建门店</a>
        <#--href="/bookstore/manager/insert.vpage"-->
    </div>

</div>
    <table class="data-table one-page displayed">
        <thead>
        <tr>
            <th>序号</th>
            <th>手机号</th>
            <th>门店ID</th>
            <th>门店名称</th>
            <th>订单量</th>
            <#if userRole?? && userRole ==21 >
            <th>订单明细</th>
            </#if>
            <th>省</th>
            <th>市</th>
            <th>联系人</th>
            <th>转介绍关系</th>
            <th>创建人</th>
            <th>源创建人</th>
            <th>创建时间</th>
            <th style="width:100px;">操作</th>
        </tr>
        </thead>
        <tbody>
    <#if bookStoreBeanPage.content?? &&  bookStoreBeanPage.content?size gt 0 >
        <#list bookStoreBeanPage.content as item>
            <tr>

                <td style="text-align: center">${item_index+1}</td>
                <td>
                    <a class="op-btn"  onclick="getMobile(this,'${item.mizarUserId}')">查看</a>
                </td>
                <td>${item.id!''}</td>
                <td>${item.bookStoreName!''}</td>
                <td>${item.orderNum!''}</td>
                 <#if userRole?? && userRole ==21 >
                <td>
                    <a class="op-btn" href="/bookstore/manager/bookStoreOrderInfo.vpage?bookStoreId=${item.id}&name=${item.bookStoreName}" style="margin-right:0;">查看</a>
                </td>
                 </#if>
                <td>${(item.storeAddressMap.provinceName)!''}</td>
                <td>${(item.storeAddressMap.cityName)!''}</td>
                <td>${item.contactName!''}</td>
                <td>
                    <#if item.createMobile?? && item.createMobile != ''>
                        ${item.createMobile}
                    <#else>
                    <a class="op-btn"  onclick="getMobile(this,'${item.createMizarUserId}')">查看</a>
                     </#if>
                </td>
                <td>${item.createUserName!''}</td>
                <td>${item.sourceMizarUserName!''}</td>
                <td>${item.createDateTime!''}</td>
                <td>
                    <a class="op-btn" href="/bookstore/manager/view.vpage?id=${item.id}" style="margin-right:0;">查看</a>
                    <#if userRole?? && userRole ==1 || userRole ==30 || userRole ==31 || userRole == 10>
                    <a class="op-btn" href="/bookstore/manager/update.vpage?id=${item.id}" style="margin-right:0;">编辑</a>
                    <a class="op-btn" id="modifyPhone" onclick="modifyPhone('${item.mizarUserId}')" style="margin-right:0;">修改手机号</a>
                    </#if>
                </td>
            </tr>
        </#list>
    <#else>
    <tr>
        <td colspan="14" style="<#if error??>color:#ff4d4d;</#if>text-align: center">${error!"该查询条件下没有数据"}</td>
    </tr>
    </#if>
    </tbody>
    </table>
    <#if bookStoreBeanPage.content?? &&  bookStoreBeanPage.content?size gt 0>

    <div id="paginator" pageIndex="${(page!0)}" class="paginator clearfix"
         totalPages="<#if totalPages??>${totalPages}<#else>1</#if>"></div>
    </#if>

</@module.page>

<script>
    // 修改手机号
    function modifyPhone (mizarUserId){
        $.ajax({
            type: 'POST',
            data: {
                userId:mizarUserId
            },
            url: '/bookstore/manager/searchMobile.vpage',
            success: function (res) {
                if(res) {
                    //res.mobile;
                    $.prompt("<div style='text-align:center;'>注意：修改手机号后，登陆账号会变为修改后的手机号，请认真填写!<br>" +
                            "<span>原手机号："+res.mobile+"</span></br><span>手机号：</span><input id='newMobile' class='audit-note'style='width:150px;height:30px' type='text'/></div> ", {
                        title: "修改手机号",
                        buttons: {"取消": false, "确定": true},
                        submit: function (e, v) {
                            if (v) {

                                $.ajax({
                                    type: 'POST',
                                    data: {
                                        mizarUserId:mizarUserId,
                                        mobile:$("#newMobile").val()
                                    },
                                    url: '/bookstore/manager/changeMobile.vpage',
                                    success: function (res) {
                                        if(res) {
                                            $.prompt("<div style='text-align:center;'>" + (res.info || "保存成功！") + "</div>", {
                                                title: "提示",
                                                buttons: {"确定": true},
                                                focus: 1,
                                                submit: function( e,v ){
                                                    if ( v ) {
                                                        window.location.href = '/bookstore/manager/list.vpage';
                                                    }
                                                },
                                                useiframe: true

                                            });
                                        } else {
                                            $.prompt("<div style='text-align:center;'>" + (res.info || "保存失败！") + "</div>", {
                                                title: "提示",
                                                buttons: {"确定": true},
                                                focus: 1,
                                                submit: function( e,v ){
                                                    if ( v ) {
                                                        window.location.href = '/bookstore/manager/list.vpage';
                                                    }
                                                },
                                                useiframe: true

                                            });
                                        }

                                    },
                                    error: function (res) {
                                        console.log(res.info);
                                        window.location.href = '/bookstore/manager/list.vpage';
                                    }
                                });
                            }
                        },
                        useiframe: true
                    });
                }

            }

        });

    }

    $('#reset').on('click',function () {
        $('#shopId').val('');
        $('#shopName').val('');
        $('#phNum').val('');
        $('#linkman').val('');
    })
    //点击查看手机号码
    function getMobile(ele,mizarUserId) {
        var $target = $(ele);
        $.ajax({
            type: 'POST',
            data: {
                userId:mizarUserId
            },
            url: '/bookstore/manager/searchMobile.vpage',
            success: function (res) {
                if(res) {
                    $target.parent().text(res.mobile);
                    $target.hide();
                }

            }

        });
    }
    //新建门店设置权限
    function check() {
        $.ajax({
            type: 'POST',
            url: '/bookstore/manager/checkAdd.vpage',
            success: function (res) {
                if(res.success) {
                   // window.location.replace(“/bookstore/manager/insert.vpage”)
                    window.location.href = '/bookstore/manager/insert.vpage';
                }else{
                    $.prompt("<div style='text-align:center;'>" + res.info + "</div>", {
                        title: "温馨提示",
                        buttons: {"我知道了": true},
                        focus: 1,
                        submit: function( e,v ){
                            // if ( v ) {
                            //     window.location.href = '/bookstore/manager/list.vpage';
                            // }
                        },
                        useiframe: true

                    });
                }

            }

        });

    }
</script>