<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑模板" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js" xmlns="http://www.w3.org/1999/html"></script>
<script src="https://cdn.bootcss.com/lodash.js/4.17.11/lodash.core.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/recorder/record.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/clipboard/clipboard.js"></script>
<style>
    [v-cloak] {
        display: none;
    }
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <h3>{{ commonTemplateId }} 主动服务模板添加/编辑</h3>
        <div class="well">
            <div class="control-group">
                <label class="control-label">大礼包介绍：</label>
                <div class="controls">
                    <textarea  id="id-giftPack" v-model="pojo.giftPackIntroduce" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                    <span class="btn btn-success btn-large copy-btn"  data-clipboard-target="#id-giftPack">复制</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">大礼包介绍图片：</label>
                <div class="controls">
                    <div>
                        <img v-if="pojo.giftPackImage" v-bind:src="pojo.giftPackImage" style="width: 300px;">
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">优惠券介绍：</label>
                <div class="controls">
                    <textarea id="id-coupon"  v-model="pojo.couponIntroduce" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                    <#--<span class="btn btn-success btn-large" @click="copyToClipBoard('coupon')">复制</span>-->
                    <span class="btn btn-success btn-large copy-btn"  data-clipboard-target="#id-coupon">复制</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">优惠券介绍图片：</label>
                <div class="controls">
                    <div>
                        <img v-if="pojo.couponImage" v-bind:src="pojo.couponImage" style="width: 300px;">
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">拼团介绍：</label>
                <div class="controls">
                    <textarea  id="id-group" v-model="pojo.groupIntroduce" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                    <span class="btn btn-success btn-large copy-btn"  data-clipboard-target="#id-group">复制</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">拼团介绍图片：</label>
                <div class="controls">
                    <div>
                        <img v-if="pojo.groupImage" v-bind:src="pojo.groupImage" style="width: 300px;">
                    </div>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">紧迫性介绍：</label>
                <div class="controls">
                    <textarea id="id-urgency" v-model="pojo.urgencyIntroduct" readonly="readonly" style="width: 600px;height:100px;"></textarea>
                    <span class="btn btn-success btn-large copy-btn"  data-clipboard-target="#id-urgency">复制</span>
                </div>
            </div>
            <div class="control-group">
                <label class="control-label">购买链接：</label>
                <div class="controls">
                    <div>
                        <img v-if="pojo.buyLink" v-bind:src="pojo.buyLink" style="width: 300px;">
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>

<div class="layer loading_layer" id="loading_layer"></div>
<div class="loading" id="loading"></div>
<script type="text/javascript">
    var vm = new Vue({
        el: '#box',
        data: {
            pojo:"",
            commonTemplateId:"",
        },
        methods: {

        },
        created: function () {
            var _this = this;
            $.get('${requestContext.webAppContextPath}/chips/ai/active/service/queryOtherServiceRenewUserTemplate.vpage', {
                userId: getUrlParam("userId"),
                clazzId:getUrlParam("clazzId"),
            }, function (res) {
                if(res.success) {
                    _this.pojo = res.pojo;
                    _this.commonTemplateId = res.commonTemplateId;
                }
            });
        }
    });

    function getUrlParam (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]); return null;
    }

    function copyToClipBoard(id) { //复制到剪切板
        console.log("copyToClipBoard:",id)
        const range = document.createRange();
        range.selectNode(document.getElementById('id-' + id));

        const selection = window.getSelection();
        if(selection.rangeCount > 0) selection.removeAllRanges();
        selection.addRange(range);
        document.execCommand("Copy");
    }
    var clipboard = new Clipboard('.copy-btn');
    //    clipboard.on('success', function (e) {
    //        alert("复制成功");
    //    });
    //    clipboard.on('error', function (e) {
    //        alert("复制失败，请手动复制");
    //    });

</script>

</@layout_default.page>