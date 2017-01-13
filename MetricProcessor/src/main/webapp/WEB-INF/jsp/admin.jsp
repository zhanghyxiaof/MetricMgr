<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*,java.util.Map.*,com.vmware.metricprocessor.pojo.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<title>Administrator Page</title>
</head>
<body>
	<!-- 用于文件上传的表单元素 --> 
	<form style="margin-left:30%; margin-top:50px;" name="demoForm" id="demoForm" method="post" enctype="multipart/form-data" 
	action="javascript: uploadAndSubmit();"> 
	<p>Upload File: <input style="margin-top:10px;" type="file" name="file" /></p> 
	<div style="margin-top:20px;">
		Progessing: <span id="bytesRead"> 0 </span>bytes / <span id="bytesTotal">0 </span>bytes
	</div> 
	<p><input style="margin-top:20px;" class="btn btn-default" type="submit" value="upload" /></p> 
	</form> 
	 
	<script>
	function uploadAndSubmit() { 
		var form = document.forms["demoForm"]; 
		    
		if (form["file"].files.length > 0) { 
		// 寻找表单域中的 <input type="file" ... /> 标签
			var file = form["file"].files[0]; 
			// try sending 
			var reader = new FileReader(); 
	
			reader.onloadstart = function() { 
			// 这个事件在读取开始时触发
				console.log("onloadstart"); 
				document.getElementById("bytesTotal").textContent = file.size; 
			} 
			reader.onprogress = function(p) { 
			// 这个事件在读取进行中定时触发
				console.log("onprogress"); 
				document.getElementById("bytesRead").textContent = p.loaded; 
			} 
	
			reader.onload = function() { 
			   // 这个事件在读取成功结束后触发
				console.log("load complete"); 
			} 

			reader.onloadend = function() { 
			   // 这个事件在读取结束后，无论成功或者失败都会触发
				if (reader.error) { 
			 		console.log(reader.error); 
			 	} 
				else {
					if (!XMLHttpRequest.prototype.sendAsBinary) {
						XMLHttpRequest.prototype.sendAsBinary = function (sData) {
							var nBytes = sData.length, ui8Data = new Uint8Array(nBytes);
						    for (var nIdx = 0; nIdx < nBytes; nIdx++) {
						      ui8Data[nIdx] = sData.charCodeAt(nIdx) & 0xff;
						    }
						    /* send as ArrayBufferView...: */
						    this.send(ui8Data);
						    /* ...or as ArrayBuffer (legacy)...: this.send(ui8Data.buffer); */
						};
					}
					
					document.getElementById("bytesRead").textContent = file.size; 
					// 构造 XMLHttpRequest 对象，发送文件 Binary 数据
					var xhr = new XMLHttpRequest(); 
					xhr.open(/* method */ "POST", 
					/* target url */ "uploadDescribe?fileName=" + file.name 
					/*, async, default to true */); 
					xhr.overrideMimeType("application/octet-stream"); 
					xhr.sendAsBinary(reader.result); 
					xhr.onreadystatechange = function() { 
						if (xhr.readyState == 4) { 
							if (xhr.status == 200) { 
								alert("upload complete"); 
			 				} 
			 			} 
			 		} 
			 	} 
			} 	
			reader.readAsBinaryString(file); 
		} 
		else { 
			alert ("Please choose a file."); 
		} 
	}
	 </script>
</body>
</html>