<#import "../../../layout_default.ftl" as layout_default />
<@layout_default.page page_title='Web manage' page_num=9>
<div id="main_container" class="span9">
    <legend style="font-weight: 700;">
        连续奖励详情
        <a type="button" id="btn_cancel" href="index.vpage" name="btn_cancel" class="btn">返回</a> &nbsp;&nbsp;
        <input type="button" id="save_ad_btn" class="btn btn-primary" value="保存"/>
    </legend>

    <div class="row-fluid">
        <div class="span12">
            <div class="well">
                <form id="chapterForm" name="detail_form" enctype="multipart/form-data" action="" method="post">
                    <input id="cscrId" name="cscrId" value="${cscrId!}" type="hidden" class="js-postData">
                    <div class="form-horizontal">
                        <div class="control-group">
                            <label class="col-sm-2 control-label">连续奖励ID</label>
                            <div class="controls">
                                <input type="text" id="cscrId" name="cscrId" class="form-control" value="${content.id!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励周数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="weekCount" name="weekCount" placeholder="整数填写" class="form-control js-postData" value="${content.weekCount!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励天数 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="dayCount" name="dayCount" placeholder="整数填写" class="form-control js-postData" value="${content.dayCount!''}" style="width: 336px" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">学习币类型ID <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="coinTypeId" name="coinTypeId" class="form-control js-postData" value="${content.coinTypeId!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">家长奖励类型 <span style="color: red">*</span></label>
                            <div class="controls">
                                <input type="text" id="rewardType" name="rewardType" class="form-control js-postData" value="${content.rewardType!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">奖励描述 </label>
                            <div class="controls">
                                <input type="text" id="desc" name="desc" class="form-control js-postData" value="${content.desc!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">备注说明</label>
                            <div class="controls">
                                <input id="remark" name="remark" class="form-control js-postData" type="text" value="<#if content??>${content.remark!''}</#if>" style="width: 336px;" disabled/>
                            </div>
                        </div>
                        <div class="control-group">
                            <label class="col-sm-2 control-label">创建者</label>
                            <div class="controls">
                                <input id="createUser" name="createUser" class="form-control js-postData" type="text" value="${content.createUser!''}" style="width: 336px;" disabled/>
                            </div>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</div>
</@layout_default.page>

