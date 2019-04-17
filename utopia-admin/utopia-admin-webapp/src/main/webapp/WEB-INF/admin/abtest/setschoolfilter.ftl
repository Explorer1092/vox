<#import "../layout_default.ftl" as layout_default />
<@layout_default.page page_title='选择学校' page_num=20>
<script src="${requestContext.webAppContextPath}/public/js/knockout/dist/knockout.js"></script>
<script src="${requestContext.webAppContextPath}/public/js/knockout.mapping/knockout.mapping.js"></script>
<!--/span-->
<div class="span9">
    <div class="hero-unit">
        <h1>请为实验(${id!""})选择学校</h1>
    </div>
    <div class="span7">
        <div class="sampletree" style="width:60%; height: 410px; float: left; display: inline;"></div>
        <div style="width:40%; height: 500px; float:right; display: inline;">
            <textarea id="schoolIds" class="form-control" rows="20"
                      style="width:80%; resize: none;height: 400px; float: left; display: inline;"
                      data-bind="value:schoolIds"
                      placeholder="一行输入一条数据，建议从EXCEL编辑导入，如果超过3000行建议使用其他策略投放">
            </textarea>
            <button class="btn btn-warning" data-bind="click:saveSchoolIds">保存</button>
        </div>
    </div>
</div>
<div id="loadingDiv"
     style="display:none ;position: absolute;top: 0;left: 0;width: 100%;height: 100%; background-color: black;opacity: 0.6;z-index: 10;color: white;font-size: 38px;">
    <p style="text-align: center;top: 30%;position: relative;">正在查询，请等待……</p>
</div>
<!--/row-->
<script type="application/javascript">
    // init label tree
    var id = "${id}";
//    var choosedSchoolIds = "";
    var choosedSchoolIds = "${choosedSchoolIds!''}";
    function ViewModel() {
        this.experimentId = ko.observable(id);
        this.schoolIds = ko.observable(choosedSchoolIds.replace(/,/g,'\n'));
        this.saveSchoolIds = function () {
            console.info("saveSchoolIds");
            var schoolIds = this.schoolIds();
            console.info(schoolIds);
            // format将/r/n替换成逗号，因为后端使用逗号来维护的,并去掉空格
            schoolIds=schoolIds.replace(/[\r\n\s]+/g, ',').replace(/,+/g,',');
            $.post("saveschoolids.vpage", {schoolIds: schoolIds, id: this.experimentId()}, function (data) {
                console.info(data);
                if (data.success) {
                    alert("保存成功");
                    window.location.reload();
                } else {
                    alert(data.info);
                }
            })
        }.bind(this);
    }
    var viewModel = new ViewModel();
    ko.applyBindings(viewModel);
</script>
</@layout_default.page>