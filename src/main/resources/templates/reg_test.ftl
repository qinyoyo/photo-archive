<!DOCTYPE html>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta name="viewport" content="width=device-width, initial-scale=1.0, minimum-scale=1.0, maximum-scale=1.0, user-scalable=no">
	<meta name="applicable-device" content="pc,mobile">
	<meta http-equiv="Cache-Control" content="no-transform">
	<meta name="apple-mobile-web-app-capable" content="yes">
	<meta name="apple-mobile-web-app-status-bar-style" content="black">
	<title>在线正则测试 正则表达式测试工具</title>
	<meta name="keywords" content="">
	<meta name="description" content="">
	<style>
		body {
			max-width: 800px;
			margin: auto;
		}

		*[onclick] {
			cursor: pointer;
		}
		.para {
			font-size: 14px;
			word-wrap: break-word;
			color: #333;
			margin-bottom: 15px;
			text-indent: 2em;
			line-height: 24px;
			zoom: 1;
		}

		table th, table td {
			padding: 2px 10px;
			font-size: 12px;
			line-height: 22px;
			height: 22px;
			border: 1px solid #e6e6e6;
		}
	</style>
	<script>
		function copy() {
			document.getElementById('textPattern').value = this.innerText
		}
		function test() {
			const string = document.getElementById('textSour').value
			const regExp = document.getElementById('textPattern').value
			const result = document.getElementById('textMatchResult')
			if (!string) {
				result.innerText = '请输入测试的字符串'
				return
			}
			if (!regExp) {
				result.innerText = '请输入正则表达式'
				return
			}
			const ignoreCase = document.getElementById('optionIgnoreCase').checked
			const global = document.getElementById('optionGlobal').checked
			const multiLines = document.getElementById('optionMultiLines').checked
			let op = ''
			if (global) op += 'g'
			if (ignoreCase) op += 'i'
			if (multiLines) op += 'm'
			const pat = (op ? new RegExp(regExp, op) : new RegExp(regExp))
			let r = ''
			let arr
			let index = 1
			while ((arr = pat.exec(string)) !== null) {
				let end = (global ? pat.lastIndex : arr.index + arr[0].length)
				r += (r ? '\n':'') + '第 '+ index +' 个匹配(' + arr.index + '-' + end + '): '+arr[0]
				for (let i=1;i<arr.length;i++) {
					if (arr[i]) r += '\nGroup '+i+': '+arr[i]
				}
				index++
				if (arr[0]==string) r += '\n完全匹配'
				if (!global) break
			}
			if (r) {
				result.innerText = r
			} else {
				result.innerText = '匹配失败'
			}
		}
	</script>
</head>
<body>
<div class="mod-panel">
	<div class="hd"><h1>正则表达式测试</h1></div>
	<div class="bd">
		<label>待匹配的文本</label>
		<textarea id="textSour" style="margin: 0px; height: 45px; width: 100%;" onchange="test()"></textarea>
		<label>正则表达式 <input id="textPattern" style="margin: 0px; width: 100%;" type="text" onchange="test()">
		</label>
		<label class="checkbox">
			<input type="checkbox" value="global" checked="checked" id="optionGlobal" name="optionGlobl">全局搜索
		</label>
		<label class="checkbox">
			<input type="checkbox" value="ignoreCase" id="optionIgnoreCase" name="optionIgnoreCase">忽略大小写
		</label>
		<label class="checkbox">
			<input type="checkbox" value="multiLines" id="optionMultiLines" name="optionMultiLines">多行匹配
		</label>
		<p>
			<button onclick="test()" style="width:160px">计算</button>
		</p>
		<div style="color:red" id="textMatchResult">
		</div>
	</div>

	<table>
		<caption>Javascript函数</caption>
		<tbody>
		<tr>
			<td>test</td>
			<td>regExp.test(string) 用来查看正则表达式与指定的字符串是否匹配。返回 true 或 false, RegExp.$1 ... $9 获取分组</td>
		</tr>
		<tr>
			<td>exec</td>
			<td>arr = regExp.exec(string) 返回数组，表示各个分组(0表示匹配内容）全局匹配模式下 arr.index, regExp.lastIndex 表示位置，可循环匹配</td>
		</tr>
		<tr>
			<td>match</td>
			<td>string.match(regExp) 全局模式返回所有匹配的串的数组，非全局模式与exec返回相同(仅第一个匹配值)</td>
		</tr>
		<tr>
			<td>replace</td>
			<td>string.replace(regExp，replacement) 全局模式替换所有的匹配，非全局模式仅替换第一个匹配值.replacement中表达式:$1..$99(分组),$&(匹配串),$`(匹配串左侧文本),$'(匹配串右侧文本),$$($)</td>
		</tr>
		<tr>
			<td>search</td>
			<td>string.search) 返回第一个与 regexp 相匹配的子串的起始位置,与indexOf类似</td>
		</tr>
		</tbody>
	</table>

	<table>
		<caption>常用正则表达式规则</caption>
		<tbody>
		<tr>
			<td onclick="copy.call(this)">中文字符</td>
			<td onclick="copy.call(this)">[\u4e00-\u9fa5]</td>
		</tr>
		<tr>
			<td>双字节字符</td>
			<td onclick="copy.call(this)">[^\x00-\xff]</td>
		</tr>
		<tr>
			<td>空白行</td>
			<td onclick="copy.call(this)">\n\s*\r</td>
		</tr>
		<tr>
			<td>国内手机</td>
			<td onclick="copy.call(this)">((\+86-)?1([358][0-9]|4[579]|66|7[0135678]|9[89])[0-9]{8}|\+(1|1\d{3}|[2-7,9]\d{0,3}|8[0-5,7-9]\d{0,2})-\d{6,12})</td>
		</tr>
		<tr>
			<td>身份证号</td>
			<td onclick="copy.call(this)">[1-9]\d{5}(18|19|([23]\d))\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\d{3}[0-9Xx]</td>
		</tr>
		</tbody>
	</table>
	<table log-set-param="table_view" data-sort="sortDisabled">
		<tbody>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">元字符</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">描述</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">将下一个字符标记符、或一个向后引用、或一个八进制转义符。例如，“\\n”匹配\n。“\n”匹配换行符。序列“\\”匹配“\”而“\(”则匹配“(”。即相当于多种编程语言中都有的“转义字符”的概念。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">^</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配输入字行首。如果设置了RegExp对象的Multiline属性，^也匹配“\n”或“\r”之后的位置。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">$</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配输入行尾。如果设置了RegExp对象的Multiline属性，$也匹配“\n”或“\r”之前的位置。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">*</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配前面的子表达式任意次。例如，zo*能匹配“z”，也能匹配“zo”以及“zoo”。*等价于{0,}。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">+</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配前面的子表达式一次或多次(大于等于1次）。例如，“zo+”能匹配“zo”以及“zoo”，但不能匹配“z”。+等价于{1,}。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">?</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配前面的子表达式零次或一次。例如，“do(es)?”可以匹配“do”或“does”。?等价于{0,1}。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">{<i>n</i>}</div>
			</td>
			<td align="left">
				<div class="para" label-module="para"><i>n</i>是一个非负整数。匹配确定的<i>n</i>次。例如，“o{2}”不能匹配“Bob”中的“o”，但是能匹配“food”中的两个o。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">{<i>n</i>,}</div>
			</td>
			<td align="left">
				<div class="para" label-module="para"><i>n</i>是一个非负整数。至少匹配<i>n</i>次。例如，“o{2,}”不能匹配“Bob”中的“o”，但能匹配“foooood”中的所有o。“o{1,}”等价于“o+”。“o{0,}”则等价于“o*”。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">{<i>n</i>,<i>m</i>}</div>
			</td>
			<td align="left">
				<div class="para" label-module="para"><i>m</i>和<i>n</i>均为非负整数，其中<i>n</i>&lt;=<i>m</i>。最少匹配<i>n</i>次且最多匹配<i>m</i>次。例如，“o{1,3}”将匹配“fooooood”中的前三个o为一组，后三个o为一组。“o{0,1}”等价于“o?”。请注意在逗号和两个数之间不能有空格。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">?</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">当该字符紧跟在任何一个其他限制符（*,+,?，{<i>n</i>}，{<i>n</i>,}，{<i>n</i>,<i>m</i>}）后面时，匹配模式是非贪婪的。非贪婪模式尽可能少地匹配所搜索的字符串，而默认的贪婪模式则尽可能多地匹配所搜索的字符串。例如，对于字符串“oooo”，“o+”将尽可能多地匹配“o”，得到结果[“oooo”]，而“o+?”将尽可能少地匹配“o”，得到结果 ['o', 'o', 'o', 'o']</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">.点</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配除“\n”和"\r"之外的任何单个字符。要匹配包括“\n”和"\r"在内的任何字符，请使用像“[\s\S]”的模式。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">(pattern)</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配pattern并获取这一匹配。所获取的匹配可以从产生的Matches集合得到，在VBScript中使用SubMatches集合，在JScript中则使用$0…$9属性。要匹配圆括号字符，请使用“\(”或“\)”。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">(?:pattern)</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">非获取匹配，匹配pattern但不获取匹配结果，不进行存储供以后使用。这在使用或字符“(|)”来组合一个模式的各个部分时很有用。例如“industr(?:y|ies)”就是一个比“industry|industries”更简略的表达式。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">(?=pattern)</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">非获取匹配，正向肯定预查，在任何匹配pattern的字符串开始处匹配查找字符串，该匹配不需要获取供以后使用。例如，“Windows(?=95|98|NT|2000)”能匹配“Windows2000”中的“Windows”，但不能匹配“Windows3.1”中的“Windows”。预查不消耗字符，也就是说，在一个匹配发生后，在最后一次匹配之后立即开始下一次匹配的搜索，而不是从包含预查的字符之后开始。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">(?!pattern)</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">非获取匹配，正向否定预查，在任何不匹配pattern的字符串开始处匹配查找字符串，该匹配不需要获取供以后使用。例如“Windows(?!95|98|NT|2000)”能匹配“Windows3.1”中的“Windows”，但不能匹配“Windows2000”中的“Windows”。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">(?&lt;=pattern)</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">非获取匹配，反向肯定预查，与正向肯定预查类似，只是方向相反。例如，“(?&lt;=95|98|NT|2000)Windows”能匹配“2000Windows”中的“Windows”，但不能匹配“3.1Windows”中的“Windows”。</div>
				<div class="para" label-module="para">*python的正则表达式没有完全按照正则表达式规范实现，所以一些高级特性建议使用其他语言如java、scala等</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">(?&lt;!pattern)</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">非获取匹配，反向否定预查，与正向否定预查类似，只是方向相反。例如“(?&lt;!95|98|NT|2000)Windows”能匹配“3.1Windows”中的“Windows”，但不能匹配“2000Windows”中的“Windows”。</div>
				<div class="para" label-module="para">*python的正则表达式没有完全按照正则表达式规范实现，所以一些高级特性建议使用其他语言如java、scala等</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">x|y</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配x或y。例如，“z|food”能匹配“z”或“food”(此处请谨慎)。“[z|f]ood”则匹配“zood”或“food”。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">[xyz]</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">字符集合。匹配所包含的任意一个字符。例如，“[abc]”可以匹配“plain”中的“a”。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">[^xyz]</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">负值字符集合。匹配未包含的任意字符。例如，“[^abc]”可以匹配“plain”中的“plin”任一字符。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">[a-z]</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">字符范围。匹配指定范围内的任意字符。例如，“[a-z]”可以匹配“a”到“z”范围内的任意小写字母字符。</div>
				<div class="para" label-module="para">注意:只有连字符在字符组内部时,并且出现在两个字符之间时,才能表示字符的范围; 如果出字符组的开头,则只能表示连字符本身.</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">[^a-z]</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">负值字符范围。匹配任何不在指定范围内的任意字符。例如，“[^a-z]”可以匹配任何不在“a”到“z”范围内的任意字符。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\b</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配一个单词的边界，也就是指单词和空格间的位置（即正则表达式的“匹配”有两种概念，一种是匹配字符，一种是匹配位置，这里的\b就是匹配位置的）。例如，“er\b”可以匹配“never”中的“er”，但不能匹配“verb”中的“er”；“\b1_”可以匹配“1_23”中的“1_”，但不能匹配“21_3”中的“1_”。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\B</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配非单词边界。“er\B”能匹配“verb”中的“er”，但不能匹配“never”中的“er”。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\cx</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配由x指明的控制字符。例如，\cM匹配一个Control-M或回车符。x的值必须为A-Z或a-z之一。否则，将c视为一个原义的“c”字符。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\d</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配一个数字字符。等价于[0-9]。grep 要加上-P，perl正则支持</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\D</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配一个非数字字符。等价于[^0-9]。grep要加上-P，perl正则支持</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\f</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配一个换页符。等价于\x0c和\cL。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\n</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配一个换行符。等价于\x0a和\cJ。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\r</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配一个回车符。等价于\x0d和\cM。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\s</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配任何不可见字符，包括空格、制表符、换页符等等。等价于[ \f\n\r\t\v]。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\S</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配任何可见字符。等价于[^ \f\n\r\t\v]。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\t</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配一个制表符。等价于\x09和\cI。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\v</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配一个垂直制表符。等价于\x0b和\cK。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\w</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配包括下划线的任何单词字符。类似但不等价于“[A-Za-z0-9_]”，这里的"单词"字符使用Unicode字符集。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\W</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配任何非单词字符。等价于“[^A-Za-z0-9_]”。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\x<i>n</i></div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配<i>n</i>，其中<i>n</i>为十六进制转义值。十六进制转义值必须为确定的两个数字长。例如，“\x41”匹配“A”。“\x041”则等价于“\x04&amp;1”。正则表达式中可以使用ASCII编码。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\<i>num</i></div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配<i>num</i>，其中<i>num</i>是一个正整数。对所获取的匹配的引用。例如，“(.)\1”匹配两个连续的相同字符。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\<i>n</i></div>
			</td>
			<td align="left">
				<div class="para" label-module="para">标识一个八进制转义值或一个向后引用。如果\<i>n</i>之前至少<i>n</i>个获取的子表达式，则<i>n</i>为向后引用。否则，如果<i>n</i>为八进制数字（0-7），则<i>n</i>为一个八进制转义值。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\<i>nm</i></div>
			</td>
			<td align="left">
				<div class="para" label-module="para">标识一个八进制转义值或一个向后引用。如果\<i>nm</i>之前至少有<i>nm</i>个获得子表达式，则<i>nm</i>为向后引用。如果\<i>nm</i>之前至少有<i>n</i>个获取，则<i>n</i>为一个后跟文字<i>m</i>的向后引用。如果前面的条件都不满足，若<i>n</i>和<i>m</i>均为八进制数字（0-7），则\<i>nm</i>将匹配八进制转义值<i>nm</i>。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\<i>nml</i></div>
			</td>
			<td align="left">
				<div class="para" label-module="para">如果<i>n</i>为八进制数字（0-7），且<i>m</i>和<i>l</i>均为八进制数字（0-7），则匹配八进制转义值<i>nml</i>。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\u<i>n</i></div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配<i>n</i>，其中<i>n</i>是一个用四个十六进制数字表示的Unicode字符。例如，\u00A9匹配版权符号（&amp;copy;）。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\p{P}</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">小写 p 是 property 的意思，表示 Unicode 属性，用于 Unicode 正表达式的前缀。中括号内的“P”表示Unicode 字符集七个字符属性之一：标点字符。</div>
				<div class="para" label-module="para">其他六个属性：</div>
				<div class="para" label-module="para">L：字母；</div>
				<div class="para" label-module="para">M：标记符号（一般不会单独出现）；</div>
				<div class="para" label-module="para">Z：分隔符（比如空格、换行等）；</div>
				<div class="para" label-module="para">S：符号（比如数学符号、货币符号等）；</div>
				<div class="para" label-module="para">N：数字（比如阿拉伯数字、罗马数字等）；</div>
				<div class="para" label-module="para">C：其他字符。</div>
				<div class="para" label-module="para"><i>*注：此语法部分语言不支持，例：javascript。</i></div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">\&lt;</div>
				<div class="para" label-module="para">\&gt;</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">匹配词（word）的开始（\&lt;）和结束（\&gt;）。例如正则表达式\&lt;the\&gt;能够匹配字符串"for the wise"中的"the"，但是不能匹配字符串"otherwise"中的"the"。注意：这个元字符不是所有的软件都支持的。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">( )</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">将( 和 ) 之间的表达式定义为“组”（group），并且将匹配这个表达式的字符保存到一个临时区域（一个正则表达式中最多可以保存9个），它们可以用 \1 到\9 的符号来引用。</div>
			</td>
		</tr>
		<tr>
			<td align="left" width="96">
				<div class="para" label-module="para">|</div>
			</td>
			<td align="left">
				<div class="para" label-module="para">将两个匹配条件进行逻辑“或”（or）运算。例如正则表达式(him|her) 匹配"it belongs to him"和"it belongs to her"，但是不能匹配"it belongs to them."。注意：这个元字符不是所有的软件都支持的。</div>
			</td>
		</tr>
		</tbody>
	</table>

</div>
</body>
</html>