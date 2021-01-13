<#assign at = '@' />
<!doctype html>
<html>
<head>
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <meta name="renderer" content="webkit">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="yes">
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="/static/font-awesome-4.7.0/css/font-awesome.min.css">
    <link rel="stylesheet" href="/static/css/transform_image.css">
    <script type="text/javascript" src="/static/js/ajax.js"></script>
    <script type="text/javascript" src="/static/js/alloy_finger.js"></script>
    <script type="text/javascript" src="/static/js/transform_image.js"></script>
    <title>${title}</title>
    <style>
        html {
            line-height: 1.5;
            -webkit-text-size-adjust: 100%;
        }
        body {
            margin: 0;
            font-size: 0.875em; /*五号*/
        }
        main {
            display: block;
        }
        blockquote {
            display: block;
            margin-block-start: 0.25em!important;
            margin-block-end: 0.25em!important;
            margin-inline-start: 0.875em!important;
            margin-inline-end: 0.875em!important;
        }
        h1 {
            font-size: 2em;  /* 小一 */
            margin: 0.67em 0;
        }
        h2 {
            font-size: 1.8em; /* 二号 */
            margin: 0.6em 0;
        }
        h3 {
            font-size: 1.6em; /* 小二 */
            margin: 0.53em 0;
        }
        h4 {
            font-size: 1.4em; /* 三号 */
            margin: 0.46em 0;
        }
        h5 {
            font-size: 1.2em; /* 四号 */
            margin: 0.4em 0;
        }
        h6 {
            font-size: 1em; /* 小四 */
            margin: 0.33em 0;
        }
        hr {
            box-sizing: content-box; /* 1 */
            height: 0; /* 1 */
            overflow: visible; /* 2 */
        }
        pre {
            font-family: monospace, monospace; /* 1 */
            font-size: 0.875em; /* 2 */
        }
        a {
            background-color: transparent;
        }
        abbr[title] {
            border-bottom: none; /* 1 */
            text-decoration: underline; /* 2 */
            text-decoration: underline dotted; /* 2 */
        }
        b,
        strong {
            font-weight: bolder;
        }
        code,
        kbd,
        samp {
            font-family: monospace, monospace; /* 1 */
            font-size: 0.875em; /* 2 */
        }
        small {
            font-size: 80%;
        }
        sub,
        sup {
            font-size: 75%;
            line-height: 0;
            position: relative;
            vertical-align: baseline;
        }

        sub {
            bottom: -0.25em;
        }

        sup {
            top: -0.5em;
        }
        img {
            border-style: none;
        }
        button,
        input,
        optgroup,
        select,
        textarea {
            font-family: inherit; /* 1 */
            font-size: 100%; /* 1 */
            line-height: 1.15; /* 1 */
            margin: 0; /* 2 */
        }
        button,
        input { /* 1 */
            overflow: visible;
        }

        button,
        select { /* 1 */
            text-transform: none;
        }
        button,
        [type="button"],
        [type="reset"],
        [type="submit"] {
            -webkit-appearance: button;
        }

        button::-moz-focus-inner,
        [type="button"]::-moz-focus-inner,
        [type="reset"]::-moz-focus-inner,
        [type="submit"]::-moz-focus-inner {
            border-style: none;
            padding: 0;
        }

        button:-moz-focusring,
        [type="button"]:-moz-focusring,
        [type="reset"]:-moz-focusring,
        [type="submit"]:-moz-focusring {
            outline: 1px dotted ButtonText;
        }

        fieldset {
            padding: 0.35em 0.75em 0.625em;
        }
        legend {
            box-sizing: border-box; /* 1 */
            color: inherit; /* 2 */
            display: table; /* 1 */
            max-width: 100%; /* 1 */
            padding: 0; /* 3 */
            white-space: normal; /* 1 */
        }
        progress {
            vertical-align: baseline;
        }
        textarea {
            overflow: auto;
        }

        [type="checkbox"],
        [type="radio"] {
            box-sizing: border-box; /* 1 */
            padding: 0; /* 2 */
        }
        [type="number"]::-webkit-inner-spin-button,
        [type="number"]::-webkit-outer-spin-button {
            height: auto;
        }
        [type="search"] {
            -webkit-appearance: textfield; /* 1 */
            outline-offset: -2px; /* 2 */
        }

        [type="search"]::-webkit-search-decoration {
            -webkit-appearance: none;
        }

        ::-webkit-file-upload-button {
            -webkit-appearance: button; /* 1 */
            font: inherit; /* 2 */
        }

        details {
            display: block;
        }
        summary {
            display: list-item;
        }

        template {
            display: none;
        }

        [hidden] {
            display: none;
        }
        ${at}charset "UTF-8";
        html {
            height: 100%;
        }
        body {
            overflow: scroll;
            display: table;
            table-layout: fixed;
            width: 100%;
            min-height:100%;
        }
        .center-block {
            display: block;
            text-align: center;
            vertical-align: middle;
            width:100%;
        }
        .center-block img,video,audio {
            max-width:100%;
            max-height:100%;
            margin: 0 auto;
        }
        body {
            max-width: 739px;
            padding: 10px;
            margin: auto;
        }
    </style>
</head>
<body onload="TransformImage('img')">
    <div>
        <h2 style="text-align: center;"> <u>${title}</u> </h2>
    </div>
</body>
</html>