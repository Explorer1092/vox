<#import "../../layout_default.ftl" as layout_default>
<@layout_default.page page_title='我的工作台' page_num=1>

<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>市场支持费用查询</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div class="span2">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th class="sorting" style="width: 100px;">结算日期-2015</th>
                        <th class="sorting" style="width: 100px;">结算日期-2016-上半年</th>
                        <th class="sorting" style="width: 100px;">结算日期-2016-下半年</th>
                        <th class="sorting" style="width: 100px;">结算日期-2017-上半年</th>
                    </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td style="text-align: center;"><a href="index.vpage?type=201506">2015年06月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201603">2016年03月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201609">2016年09月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201703">2017年03月</a></td>
                        </tr>
                        <tr>
                            <td style="text-align: center;"><a href="index.vpage?type=201509">2015年09月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201604">2016年04月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201610">2016年10月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201704">2017年04月</a></td>
                        </tr>
                        <tr>
                            <td style="text-align: center;"><a href="index.vpage?month=201510">2015年10月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201605">2016年05月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201611">2016年11月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201705">2017年05月</a></td>
                        </tr>
                        <tr>
                            <td style="text-align: center;"><a href="index.vpage?type=201511">2015年11月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201606">2016年06月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201612">2016年12月</a></td>
                            <td style="text-align: center;"><a href="/workspace/marketfee/marketfeeviewer.vpage?month=201706">2017年06月</a></td>
                        </tr>
                        <tr>
                            <td style="text-align: center;"><a href="index.vpage?type=201512">2015年12月</a></td>
                            <td style="text-align: center;">--</td>
                            <td style="text-align: center;">--</td>
                            <td style="text-align: center;">--</td>
                        </tr>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>市场支持费用辅助查询</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div class="span2">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="text-align: center;"><a href="index.vpage?type=manual">手动核算</a></th>
                        <th style="text-align: center;"><a href="index.vpage?type=overau">超额新增</a></th>
                        <th style="text-align: center;"><a href="index.vpage?type=overds">超额双科</a></th>
                    </tr>
                    </thead>
                </table>
            </div>
        </div>
    </div>
</div>
<div class="row-fluid sortable ui-sortable">
    <div class="box span12">
        <div class="box-header well" data-original-title="">
            <h2><i class="icon-info"></i>合作伙伴数据导出</h2>

            <div class="box-icon">
                <a href="#" class="btn btn-minimize btn-round"><i class="icon-chevron-up"></i></a>
                <a href="#" class="btn btn-close btn-round"><i class="icon-remove"></i></a>
            </div>
        </div>
        <div class="box-content">
            <div class="span2">
                <table class="table table-striped table-bordered">
                    <thead>
                    <tr>
                        <th style="text-align: center;">月份</th>
                        <th style="text-align: center;">操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <form id="partnerForm" action="/workspace/marketfee/partner.vpage" method="post">
                        <input type="hidden" id="month" name="month" value="201609"/>
                        <tr>
                        <td>201609</td>
                        <td>
                            <button type="button" class="btn btn-success" onclick="exportPartnerData('201609')">导出</button>
                        </td>
                        </tr>
                        <tr>
                            <td>201610</td>
                            <td>
                                <button type="button" class="btn btn-success" onclick="exportPartnerData('201610')">导出</button>
                            </td>
                        </tr>
                        <tr>
                            <td>201611</td>
                            <td>
                                <button type="button" class="btn btn-success" onclick="exportPartnerData('201611')">导出</button>
                            </td>
                        </tr>
                    </form>
                    </tbody>
                </table>
            </div>
        </div>
    </div>
</div>
<script type="text/javascript">
    function exportPartnerData(month){
        $("#month").val(month);
        $("#partnerForm").submit();
    }
</script>
</@layout_default.page>
