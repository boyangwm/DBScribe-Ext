<html><head><meta http-equiv="Content-Type" content="text/html; charset=UTF-8"><title>DBSchemaScribe Report</title>
<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css">
<script src="https://ajax.googleapis.com/ajax/libs/jquery/1.11.3/jquery.min.js"></script>
 <script src="http://maxcdn.bootstrapcdn.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
   <script src="https://google-code-prettify.googlecode.com/svn/loader/run_prettify.js?lang=css&skin=desert"> </script>
 <style>
.modal.modal-wide .modal-dialog {
  width: 90%;
}
.modal-wide .modal-body {
  overflow-y: auto;
  overflow-x: auto;
}
p {word-wrap: break-word; }
</style>
<script>
\$(".modal-wide").on("show.bs.modal", function() {
  var height = \$(window).height() - 200;
  \$(this).find(".modal-body").css("max-height", height);
});
</script>
<style>
div.tooltip-inner {
    max-width: 1500px;
}
</style>
<script>
\$(document).ready(function () {
  \$("a").tooltip({
    'selector': '',
    'placement': 'bottom',
    'container':'body'
  });
});
</script>
 </head>
 <body>
 $
 Function:
 {fc|
 $fc$
 }
 $
<div class="container">
	<div class="row">
		<div class="col-xs-3" style="position:fixed;float:left;left:0px"><h2> DBScribeSchema Report</h2> 
			<div><p>Summary:
			$
			ProjectSum:
			{ps|
			$ps$
			}
			$
			</p></div>
			<div class="panel panel-default">
			<div class="panel-heading">DataBase Information</div><div class="panel-body" style="max-height:550px;overflow-x: scroll;overflow-y: scroll">
			$ 
			Index:
			{ix| 
			$ix$
			}
			$
			</div>
			</div>
		</div>
		<div class="col-xs-9" style="float:right;right:0px">
		
			$IDNum,
			Title, 
			Content:
			{id, ms ,mbd| 
			<div class="panel panel-primary"><a id="$ms$"></a>
				<div class="panel-heading"><b>$id$. &nbsp;$ms$</b></div>
				<div class="panel-body">
									<p align="right">
										<div id="$id$" class="modal modal-wide fade">
										  <div class="modal-dialog">
											<div class="modal-content">
											  <div class="modal-header">
												<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
												<h4 class="modal-title">$ms$</h4>
											  </div>
											  <div class="modal-footer">
												<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
											  </div>
											</div><!-- /.modal-content -->
										  </div><!-- /.modal-dialog -->
										</div><!-- /.modal -->
										</p>
				<p>
				<span class="glyphicon glyphicon-comment"></span> <b> Attributes: </b>
				$mbd$
				</p>
				</div>
			</div>
			}
			$
		</div>
	</div>
</div>
<script>
\$(document).ready(function(){
    \$('[data-toggle="tooltip"]').tooltip();   
});
</script>
</body>
</html>