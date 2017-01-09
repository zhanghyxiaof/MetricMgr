<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8" import="java.util.*,java.util.Map.*,com.vmware.metricprocessor.pojo.*"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
</head>
<body>
	<%
		Map<String, List<Metric>> map = (Map<String, List<Metric>>) request.getAttribute("metricMap");
		map.size();
	%>
	<p><%=map.size()%></p>
	<form id="form1" name="form1" method="post" action="download">
		<select name="mylist" size="5"></select>
		<% int order = 0;
		for (Entry<String, List<Metric>> entry : map.entrySet()) {%>
		</label> <label> <input type="checkbox" name="checkbox2"value="checkbox" /><%=entry.getKey() %></label>
			<%if (order%5 == 0) {%>
				<br />
			<%} order++;%>
		<%} %>
		<br />
		<input name="sub" type="submit" value="generate describe file">
	</form>
</body>
</html>