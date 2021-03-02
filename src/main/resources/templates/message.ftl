<!DOCTYPE html>
<html>
<head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="renderer" content="webkit">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes" />
    <title>Message</title>
    <script type="text/javascript" src="/static/js/ajax.js"></script>
</head>
<#if confirm?? && action??>
<script>
    window.onload=function() {
        if (confirm("${confirm}")) {
            let url = '/${action}?confirm=true'
            Ajax.get(url, function (responseText) {
                document.getElementById('app').innerHTML = responseText
            })
        }
    }
</script>
</#if>
<body>
<div id="app">
    <#if message??>${message?replace('\n','<br>')}<#else>Unknown Error</#if>
</div>
</body>
</html>



