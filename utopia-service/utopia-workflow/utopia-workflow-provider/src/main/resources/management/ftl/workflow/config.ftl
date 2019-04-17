<#-- @ftlvariable name="advertisements" type="java.util.List<com.voxlearning.utopia.service.config.api.entity.Advertisement>" -->

<!DOCTYPE html>
<!--suppress HtmlUnknownTarget -->
<html>
<head>
</head>
<body>
<a class="btn btn-default" href="../../../" role="button">Back to Main</a>
<h3>Workflow Config</h3>


<h4> WorkFlowName: ${type!}</h4>
<h4> RuntimeMode : ${mode!}</h4>

<div class="container-fluid">
    <div class="row">
        <textarea style="width: 100%; height: 80%; resize: none;">${config!}</textarea>
    </div>
</body>
</html>

