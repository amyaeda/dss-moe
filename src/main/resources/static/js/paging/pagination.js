$(function() {
	onPageSizeChange();
});



function onPageSizeChange() {
	$('#pageSizeSelect').change(function(event) {
		//window.location.replace("/?pageSize=" + this.value + "&page=1&search=" + $('#search').val());
		window.location.replace("/dss-moe/adminActivityReport?pageSize=" + this.value + "&page=1&startDate="+$('#startDate').val()+"&endDate="+$('#endDate').val()+"&userId="+$('#userURL').val()+"&status="+$('#status1').val() );
	});
	
	$('#pageSizeSelectAdminList').change(function(event) {
		//window.location.replace("/?pageSize=" + this.value + "&page=1&search=" + $('#search').val());
		window.location.replace("/dss-moe/adminList?pageSize=" + this.value + "&page=1&nameSearch="+$('#nameSearch').val() );
	});
	
	$('#pageSizeSelectApprovalList').change(function(event) {
		//window.location.replace("/?pageSize=" + this.value + "&page=1&search=" + $('#search').val()); ,nameSearch=${nameSearch}
		window.location.replace("/dss-moe/approvalList?pageSize=" + this.value + "&page=1&nameSearch="+$('#nameSearch').val() );
	});
	
	$('#pageSizeSelectFileList').change(function(event) {
		//window.location.replace("/?pageSize=" + this.value + "&page=1&search=" + $('#search').val());
		window.location.replace("/dss-moe/fileList?pageSize=" + this.value + "&page=1&iptsName="+$('#select2-iptsName-container').val()+"&searchItem="+$('#searchItem').val()+"&startDate="+$('#startDate').val()+"&endDate="+$('#endDate').val()+"&userId="+$('#userId').val()+"&modulCode="+$('#modulCode').val()+"&jenisIpts="+$('#jenisIpts').val() ); 
	});
	
	$('#pageSizeSelectReportList').change(function(event) {
		
		//window.location.replace("/?pageSize=" + this.value + "&page=1&search=" + $('#search').val());
		window.location.replace("/dss-moe/documentReport?pageSize=" + this.value + "&page=1&module="+$('#module').val()+"&iptsName="+$('#select2-iptsName-container').val()+"&searchItem="+$('#searchItem').val()+"&startDate="+$('#startDate').val()+"&endDate="+$('#endDate').val()+"&userId="+$('#userURL').val()+"&status="+$('#status').val()+"&modulCode="+$('#modulCode').val()+"&jenisIpts="+$('#jenisIpts').val());
	});
	
	$('#pageSizeSelectTokenList').change(function(event) {
		//window.location.replace("/?pageSize=" + this.value + "&page=1&search=" + $('#search').val());
		window.location.replace("/dss-moe/revokeToken?pageSize=" + this.value + "&page=1" );
	});
}
