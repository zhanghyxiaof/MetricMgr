<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*,java.util.Map.*,com.vmware.metricprocessor.pojo.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<title>Administrator Page</title>
</head>
<body onload="initial_page()">
	<%
	List<Adapter> adapterList = (List<Adapter>) request.getAttribute("adapterList");
	%>
	<!-- 用于文件上传的表单元素 --> 
	<form style="margin-left:30%; margin-top:50px;" name="demoForm" id="demoForm" method="post" enctype="multipart/form-data" 
	action="javascript: uploadAndSubmit();"> 
		<div style="margin-left: 0%; margin-top:100px; width:100%">
			please select the Adapter Kind: 
			<select id="adapterKind" name="adapterKind" style="margin-left: 10px; width: 20%" onchange="display_version_list(this.value)">
			<%for (Adapter adapter : adapterList){%>
				<option value=<%=adapter.getAdapterKind()%>> <%=adapter.getAdapterKind()%> </option>
			<%}%>			
			</select>
		</div>		
		
		<div style="margin-left: 0%; margin-top:30px; width:100%">
			please select the Adapter Version:
			<%for (Adapter adapter : adapterList){%>
				<select id=<%=adapter.getAdapterKind()%> name="adapterVersion" style="margin-left: 10px; width: 20%; display:none">
				<%for (String version : adapter.getVersionList()) {%>
					<option value=<%=version%>> <%=version%> </option>
				<%} %>
				</select>
			<%}%>
		</div>		
	
		<p style="margin-top:30px;">Upload File:
			<div>
				<label class="btn btn-default" id="uploadHint" style="margin-top:10px;" for="uploadInput"> Upload File </label>
				<input style="opacity: 0; position: absolute; z-index: -1;" type="file" name="file" id="uploadInput" 
				onchange="document.getElementById('uploadHint').innerHTML = this.value;"/>
			</div>		
		</p>
		
<!--  		<div style="margin-top:20px;">
			Progessing: <span id="bytesRead"> 0 </span>bytes / <span id="bytesTotal">0 </span>bytes
		</div>   -->
		
		<div id="upload_status" style="margin-top:20px"></div>
		
		<div id="current_status" style="margin-top:20px"></div>
		
		<p><input style="margin-top:20px;" class="btn btn-default" type="submit" value="upload" /></p> 
		
	</form> 
	 
	<script>
	function initial_page()
	{
		document.getElementById(document.getElementById("adapterKind").value).style.display ='inline';
	}
	
	function display_version_list(val)
	{
		var all_version_selects = document.getElementsByName("adapterVersion");
		for (var i=0; i<all_version_selects.length; i++){
			console.log(all_version_selects[i]);
			all_version_selects[i].style.display ='none';
		}
		document.getElementById(val).style.display ='inline';
	}
	
	function uploadAndSubmit() { 
		var form = document.forms["demoForm"]; 
		    
		if (form["file"].files.length > 0) { 
		// 寻找表单域中的 <input type="file" ... /> 标签
			var file = form["file"].files[0]; 
			// try sending 
			var reader = new FileReader(); 
	
/*  			reader.onloadstart = function() { 
			// 这个事件在读取开始时触发
				console.log("onloadstart"); 
				document.getElementById("bytesTotal").textContent = file.size; 
			} 
			reader.onprogress = function(p) { 
			// 这个事件在读取进行中定时触发
				console.log("onprogress"); 
				document.getElementById("bytesRead").textContent = p.loaded; 
			}   */
	
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
					
					/* document.getElementById("bytesRead").textContent = file.size; */ 
					
					// 构造 XMLHttpRequest 对象，发送文件 Binary 数据
					var xhr = new XMLHttpRequest(); 
					xhr.open(/* method */ "POST", 
					/* target url */ "uploadDescribe?fileName=" + file.name 
							+"&adapterKind=" +  document.getElementById("adapterKind").value
							+"&adapterVersion=" +  document.getElementById(document.getElementById("adapterKind").value).value
					/*, async, default to true */); 
					xhr.overrideMimeType("application/octet-stream"); 
					xhr.sendAsBinary(reader.result); 
					xhr.onreadystatechange = function() { 
						if (xhr.readyState == 4) { 
							if (xhr.status == 200) { 
								document.getElementById("upload_status").innerText = "upload success";
								document.getElementById("current_status").innerText = xhr.getResponseHeader("current_status");
			 				}
							else{
								document.getElementById("upload_status").innerText = "upload failed";
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