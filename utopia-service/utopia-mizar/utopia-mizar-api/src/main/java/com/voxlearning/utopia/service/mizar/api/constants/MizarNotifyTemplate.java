package com.voxlearning.utopia.service.mizar.api.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static com.voxlearning.utopia.service.mizar.api.constants.MizarNotifyType.AUDIT_DONE;
import static com.voxlearning.utopia.service.mizar.api.constants.MizarNotifyType.AUDIT_NOTICE;

/**
 * Mizar平台消息模板
 * <p>
 * Created by Yuechen.Wang on 2016/12/7.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum MizarNotifyTemplate {

    // 以下是品牌相关消息模板
    BRAND_CREATE(101, AUDIT_NOTICE, "新增品牌审核", "{#role} {#user} 于 {#time} 新增了一个品牌：{#name}<br/>点击前往审核", "/operate/audit/auditbrand.vpage?id={}"),
    BRAND_MODIFY(102, AUDIT_NOTICE, "品牌变更审核", "{#role} {#user} 于 {#itme} 对品牌 {#name}({#id}) 进行变更<br/>点击前往审核", "/operate/audit/auditbrand.vpage?id={}"),
    BRAND_APPROVE(103, AUDIT_DONE, "品牌申请批准", "{#role} {#user} 于 {#time} 批准了您的申请<br/>点击查看", "/basic/brand/view.vpage?id={}"),
    BRAND_REJECT(104, AUDIT_DONE, "品牌申请驳回", "{#role} {#user} 于 {#time} 驳回了您的申请<br/>驳回意见：{#reason}<br/>点击查看", "/basic/apply/changedetail.vpage?id={}"),
    // 以上是机构相关消息模板


    // 以下是机构相关消息模板
    SHOP_CREATE(201, AUDIT_NOTICE, "新增机构审核", "{#role} {#user} 于 {#time} 新增了一家机构：{#name}<br/>点击前往审核", "/operate/audit/auditshop.vpage?id={}"),
    SHOP_MODIFY(202, AUDIT_NOTICE, "机构变更审核", "{#role} {#user} 于 {#itme} 对机构 {#name}({#id}) 进行变更<br/>点击前往审核", "/operate/audit/auditshop.vpage?id={}"),
    SHOP_APPROVE(203, AUDIT_DONE, "机构申请批准", "{#role} {#user} 于 {#time} 批准了您的申请<br/>点击查看", "/basic/shop/edit.vpage?type=detail&id={}"),
    SHOP_REJECT(204, AUDIT_DONE, "机构申请驳回", "{#role} {#user} 于 {#time} 驳回了您的申请<br/>驳回意见：{#reason}<br/>点击查看", "/basic/apply/changedetail.vpage?id={}"),
    // 以上是机构相关消息模板

    // 以下是课程相关消息模板
    GOODS_CREATE(301, AUDIT_NOTICE, "新增课程审核", "{#role} {#user} 于 {#time} 新增了一家机构：{#name}<br/>点击前往审核", "/operate/audit/goodsinfo.vpage?rid={}"),
    GOODS_MODIFY(302, AUDIT_NOTICE, "课程变更审核", "{#role} {#user} 于 {#itme} 对机构 {#name}({#id}) 进行变更<br/>点击前往审核", "/operate/audit/goodsinfo.vpage?rid={}"),
    GOODS_APPROVE(303, AUDIT_DONE, "课程申请批准", "{#role} {#user} 于 {#time} 批准了您的申请<br/>点击查看", "/basic/goods/detail.vpage?gid={}"),
    GOODS_REJECT(304, AUDIT_DONE, "课程申请驳回", "{#role} {#user} 于 {#time} 驳回了您的申请<br/>驳回意见：{#reason}<br/>点击查看", "/basic/apply/changedetail.vpage?id={}"),
    // 以上是课程相关消息模板
    ;

    private final int id;          // 模板ID
    private final MizarNotifyType type;  // 消息类型
    private final String title;    // 消息标题
    private final String content;  // 消息内容
    private final String url;      // 跳转链接

}
