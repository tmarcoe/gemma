<h1>The Daily News Letter has been sent to ${mailCount} subscribers</h1>

<button type="button" onclick="followLink('/admin')">OK</button>

<script type="text/javascript">
	function followLink(link) {
		window.location.href = "${pageContext.request.contextPath}" + link;
	}
</script>