<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="sec"
	uri="http://www.springframework.org/security/tags"%>
<%@ taglib prefix="sf" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>

<sf:form method="post"
	action="${pageContext.request.contextPath}/updatecoupon"
	commandName="coupon">
	<table class="coupontable">
		<tr>
			<th>Coupon ID</th>
			<th>Coupon Name</th>
			<th>Rule Name</th>
			<th>Date Expired</th>
		</tr>
		<tr>
			<td><sf:input path="couponID" type="text" readonly="true" /></td>
			<td><sf:input path="name" type="text" /></td>
			<td><sf:input path="ruleName" type="date" /></td>
			<td><sf:input path="expires" type="date" />
		</tr>
		<tr>
			<td>Active&nbsp;<input type="hidden" value="on" name="_active" /> <sf:checkbox
					path="active" /></td>
			<td>Exclusive&nbsp;<input type="hidden" value="on" name="_active" /> <sf:checkbox
					path="exclusive" /></td>
		</tr>
		<tr><td>&nbsp;</td></tr>
	</table>
	<table>
		<tr>
			<th>Description</th>
		</tr>
		<tr>
			<td><sf:textarea path="description" rows="10" cols="50" /></td>
		</tr>
		<tr>
			<td><button type="submit" >Save</button></td>
		</tr>
	</table>


</sf:form>