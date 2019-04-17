<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title="添加/编辑模板" page_num=26>
<script src="${requestContext.webAppContextPath}/public/js/vue2.4.2.min.js" xmlns="http://www.w3.org/1999/html"></script>
<script src="https://cdn.bootcss.com/lodash.js/4.17.11/lodash.core.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/recorder/record.js"></script>
<style>
    [v-cloak] {
        display: none;
    }
</style>

<div id="box" v-cloak class="span9">
    <div class="form-horizontal">
        <h3>{{ name }} 主动服务模板添加/编辑</h3>
        <div class="well">
            <template v-for="(item, i) in textList">
                <div class="control-group">
                    <label class="control-label">文案：</label>
                    <div class="controls">
                        <textarea  v-model="textList[i]" title="使用{userName}替换{学生姓名},{finishedDaysCount}替换{已完课天数}" style="width: 600px;height:100px;"></textarea>
                    </div>
                </div>
            </template>
            <div style="margin-left: 600px">
                <span class="btn btn-success btn-large" @click="save">保存</span>
                <span class="btn btn-info btn-large" @click="cancel">返回</span>
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
            name:"",
            textList:[],
            serviceType: "",
            id:"",
        },
        methods: {
            save: function () {
                var _this = this;
                console.log(_this.textList)
                console.log("json: " + JSON.stringify(_this.textList));
                $.post('${requestContext.webAppContextPath}/chips/ai/active/service/otherServiceTypeSave.vpage', {
                    json: JSON.stringify(_this.textList),
                    id: _this.id,
                    name :_this.name,
                    serviceType : _this.serviceType,
                }, function (res) {
                    if (res.success) {
                        alert("保存成功");
                    } else {
                        alert(res.info);
                    }
                });
            },
            cancel:function () {
                var method = getUrlParam("method")
                console.log("cancel method: " + method)
                var hostName = window.location.host;
                console.log("http://" + hostName +"/chips/ai/active/service/activeServiceIndex.vpage?method=" + method
                        + "&id=" + getUrlParam('id') + "&serviceType=" + getUrlParam('serviceType'));
                window.location.href = "http://" + hostName +"/chips/ai/active/service/activeServiceIndex.vpage?method=" + method
                        + "&id=" + getUrlParam('id') + "&serviceType=" + getUrlParam('serviceType');
            },

        },
        created: function () {
            var _this = this;
            var name = getUrlParam("name");
            _this.name = name;
            console.log(name)
            _this.serviceType = getUrlParam("serviceType");
            var id = getUrlParam("id");
            _this.id = id;
            $.get('${requestContext.webAppContextPath}/chips/ai/active/service/queryOtherServiceType.vpage', {
                id: id,
                serviceType: getUrlParam("serviceType"),
                name:getUrlParam("name"),
            }, function (res) {
                if(res.success) {
                    _this.textList = res.textList;
                    _this.name = name;
                }
            });
        }
    });

    function getUrlParam (name) {
        var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
        var r = window.location.search.substr(1).match(reg);
        if (r != null) return decodeURIComponent(r[2]); return null;
    }

</script>

</@layout_default.page>