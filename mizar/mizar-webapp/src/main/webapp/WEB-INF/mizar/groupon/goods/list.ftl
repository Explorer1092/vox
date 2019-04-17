<#import "../../module.ftl" as module>
<@module.page
title="商品管理"
pageJsFile={"siteJs" : "public/script/basic/groupongoods", "commonJs" : "public/script/common/common"}
pageJs=["siteJs"]
leftMenu="商品管理"
>
<div class="op-wrapper orders-wrapper clearfix">
    <form id="filter-form" class="form-table" action="/groupon/goods/list.vpage" method="post">
        <table cellspacing="0" cellpadding="0">
            <tr>
                <td>商品ID:<input type="text" class="form-control" name="id" size="10"  <#if (paramBean.id)??>value="${paramBean.id!}"</#if> placeholder="请输入商品Id"></td>
                <td>商品名称:<input type="text" class="form-control col-lg-2" size="10" name="shortTitle"   placeholder="请输入名称"  value="<#if paramBean.shortTitle??>${paramBean.shortTitle!''}</#if>"    > </td>
                <td>
                    <label>商品品类：</label>
                    <select name="category">
                        <option value="">全部</option>
                        <#if categoryList?size gt 0>
                            <#list categoryList as category>
                                <option value="${category.categoryCode}" <#if (paramBean.category)?? && "${category.categoryCode}"=="${paramBean.category}" >selected</#if> >${category.categoryName}</option>
                            </#list>
                        </#if>
                    </select>
                </td>

            </tr>
            <tr>
                <td>
                    <label>是否包邮：</label>
                    <select name="postFree">
                        <option value="">全部</option>
                        <option value="1" <#if (paramBean.postFree)?? >${paramBean.postFree?string('selected','')}</#if> >是</option>
                        <option value="0" <#if (paramBean.postFree)?? >${paramBean.postFree?string('','selected')}</#if>>否</option>
                    </select>
                </td>
                <td>
                    <label>是否卖光：</label>
                    <select name="oos">
                        <option value="">全部</option>
                        <option value="1" <#if  (paramBean.oos)?? >${paramBean.oos?string('selected','')}</#if>>是</option>
                        <option value="0" <#if  (paramBean.oos)?? >${paramBean.oos?string('','selected')}</#if>>否</option>
                    </select>
                </td>
                <td>
                    <label>数据类源：</label>
                    <select name="dataSource">
                        <option value="">全部</option>
                        <#if dataSourceTypeList?size gt 0>
                            <#list dataSourceTypeList as type>
                                <option value="${type.code}" <#if (paramBean.dataSource)?? && type.code==paramBean.dataSource >selected</#if>>${type.getName()}</option>
                            </#list>
                        </#if>
                    </select>
                </td>
            </tr>
            <td>
                <label>商品状态：</label>
                <select name="status">
                    <option value="">全部</option>

                    <#if statusTypeList??&&statusTypeList?size gt 0>
                        <#list statusTypeList as status>
                            <option value="${status.code}" <#if  (paramBean.status)?? && status.code==paramBean.status >selected</#if>>${status.desc}</option>
                        </#list>
                    </#if>
                </select>
            </td>
            <td>
                开始时间：
                <input type="text" class="form-control col-lg-2" size="6" name="beginTime" id="beginTime" <#if  (paramBean.beginTime)??>value="${paramBean.beginTime}"</#if>  placeholder="请输入结束时间">
            </td>
            <td>
                结束时间：
                <input type="text" class="form-control col-lg-2" size="8" name="endTime" id="endTime" <#if (paramBean.endTime)??>value="${paramBean.endTime}"</#if>  placeholder="请输入结束时间">
            </td>
            </tr>
            <tr>
                <td colspan="3">
                    <a class="blue-btn" id="js-filter" style="float:left;" href="javascript:void(0)">搜索</a>
                    <a class="blue-btn" id="index-filter" style="float:left;margin-left:20px;padding-left: 20px;" href="/groupon/goods/detail.vpage">新增商品</a>
                </td>
            </tr>
        </table>
    </form>
</div>
<table class="data-table displayed">
    <thead>
    <tr>
        <th>标题</th>
        <th>类别</th>
        <th>价格</th>
        <th>是否包邮</th>
        <th>排序值</th>
        <th>状态</th>
        <th>商品来源</th>
        <th>缩略图</th>
        <th>发布时间</th>
        <th>操作</th>
    </tr>
    </thead>
    <tbody>
        <#if (pager.content)??&&pager.content?size gt 0>
            <#list pager.content as goods>
            <tr>
                <td>${goods.shortTitle!''}</td>
                <td><#if goods.categoryCode??> <#assign key="${goods.categoryCode}"> ${categoryMap[key]}</#if>  </td>
                <td>${goods.price!}</td>
                <td><#if (goods.postFree)??>${goods.postFree?string("是","否")}<#else>否</#if></td>
                <td><#if goods.orderIndex??><input type="text" goodsId="${goods.id}" name ="orderIndex" size="3" value="${goods.orderIndex}" style="border:none;"></#if></td>
                <td><#if goods.status??><#if goods.status=='OFFLINE'>下线<#elseif goods.status=='ONLINE'>上线<#else>审核中</#if></#if></td>
                <td><#if goods.goodsSource??><#if goods.goodsSource=='tian_mao'>天猫
                <#elseif goods.goodsSource=='tao_bao'>淘宝
                <#elseif goods.goodsSource=='jing_dong'>京东
                <#elseif goods.goodsSource=='dang_dang'>当当
                </#if>
                </#if>
                </td>
                <td><#if goods.image??><img src="${goods.image}" width="60px;height:60px"></#if> </td>
                <td><#if goods.deployTime??>${goods.deployTime?string("yyyy-MM-dd HH:mm:ss")}<#elseif goods.beginTime??>${goods.beginTime?string("yyyy-MM-dd HH:mm:ss")} </#if></td>
                <td>
                    <a class="op-btn" href="/groupon/goods/detail.vpage?id=${goods.id!''}" style="margin-right:0;">编辑</a>
                </td>
            </tr>
            </#list>
        </#if>
    </tbody>
</table>
<div id="paginator" pageIndex="${(pageIndex!0)+1}" title="后台从0开始,分页插件从第1页开始" class="paginator clearfix" totalPage="<#if (pager.totalPages)??&&pager.totalPages==0>1<#else>${pager.totalPages!1}</#if>"></div>
</@module.page>

