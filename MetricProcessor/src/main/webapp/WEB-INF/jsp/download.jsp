<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*,java.util.Map.*,com.vmware.metricprocessor.pojo.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
	<title>download page</title>
</head>
<body>
	<%
	String adapterKind =  (String) request.getAttribute("adapterKind");
	String adapterVersion = (String) request.getAttribute("adapterVersion");
	%>
	<h2 style="margin-left: 38%; margin-top:200px">process successfully</h2>
	<form style="margin-left: 41%; margin-top:50px" id="form1" name="form1" method="post" action="downloadDescribe;">
		<input class="btn btn-default" name="download" type="submit" value="download describe file">
		<input name="adapterKind" value=<%=adapterKind%> style="display:none">
		<input name="adapterVersion" value=<%=adapterVersion%> style="display:none">
	</form>
	
</body>
</html>