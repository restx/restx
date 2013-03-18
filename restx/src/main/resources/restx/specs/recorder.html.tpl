
<!DOCTYPE HTML>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
  <title>Restx Recorded Specs</title>
  <link rel="stylesheet" href="http://mleibman.github.com/SlickGrid/slick.grid.css" type="text/css"/>
  <link rel="stylesheet" href="http://mleibman.github.com/SlickGrid/examples/examples.css" type="text/css"/>
  <link rel="stylesheet" href="https://google-code-prettify.googlecode.com/svn/loader/skins/sunburst.css" type="text/css"/>
  <style type="text/css">
  * { font-size: 14px; font-family:Consolas,'Lucida Console','DejaVu Sans Mono',monospace; }
  </style>
</head>
<body style="background: #302E30; ">
  <div style="width:960px; margin-bottom: 5px; margin-left: auto; margin-right: auto; color: #F3F7E4;">Search: <input id="search"></div>
  <div id="myGrid" style="width:960px;height:400px; margin-left: auto; margin-right: auto; "></div>
  <pre class="prettyprint" style="width:900px;height:400px; margin-left: auto; margin-right: auto; overflow: auto;">
    <code class="language-yaml" id="details"></code>
  </pre>

<script src="http://mleibman.github.com/SlickGrid/lib/jquery-1.7.min.js"></script>
<script src="http://mleibman.github.com/SlickGrid/lib/jquery.event.drag-2.0.min.js"></script>

<script src="http://mleibman.github.com/SlickGrid/slick.core.js"></script>
<script src="http://mleibman.github.com/SlickGrid/slick.grid.js"></script>
<script src="http://mleibman.github.com/SlickGrid/slick.dataview.js"></script>

<script src="https://google-code-prettify.googlecode.com/svn/loader/prettify.js"></script>
<script src="https://google-code-prettify.googlecode.com/svn/loader/lang-yaml.js"></script>

<script>
  var grid, dataView;
  var columns = [
    {id: "id", name: "Id", field: "id", width: 40, sortable: true},
    {id: "method", name: "Method", field: "method", width: 60, sortable: true},
    {id: "path", name: "Path", field: "path", width: 280, sortable: true},
    {id: "recordTime", name: "Record Time", field: "recordTime", width: 260, sortable: true},
    {id: "duration", name: "Duration (ms)", field: "duration", width: 80, sortable: true},
    {id: "capturedItems", name: "Items", field: "capturedItems", width: 60, sortable: true},
    {id: "capturedRequestSize", name: "Req. body (b)", field: "capturedRequestSize", width: 60, sortable: true},
    {id: "capturedResponseSize", name: "Resp. body (b)", field: "capturedResponseSize", width: 60, sortable: true}
  ];

  var sortcol = "id";
  var sortdir = 1;
  var searchString = "";

  var options = {
    enableCellNavigation: true,
    enableColumnReorder: false
  };

  function comparer(a, b) {
    var x = a[sortcol], y = b[sortcol];
    return (x == y ? 0 : (x > y ? 1 : -1));
  }

  function myFilter(item, args) {
    if (args.searchString != ""
        && item["path"].indexOf(args.searchString) == -1
        && item["method"].indexOf(args.searchString) == -1
        && item["id"].indexOf(args.searchString) == -1
            ) {
      return false;
    }

    return true;
  }

  $(function () {
    var data = [
        // { id: "%03d", method: "%s", path: "%s", recordTime: "%s", duration: %d, capturedItems: %d, capturedRequestSize: %d, capturedResponseSize: %d },
        {data}
    ];

    dataView = new Slick.Data.DataView({ inlineFilters: true });
    grid = new Slick.Grid("#myGrid", dataView, columns, options);

    grid.onSort.subscribe(function (e, args) {
      sortdir = args.sortAsc ? 1 : -1;
      sortcol = args.sortCol.field;
      dataView.sort(comparer, args.sortAsc);
    });

    grid.onClick.subscribe(function(e, args) {
      var item = dataView.getItem(args.row);
      $.get('{baseUrl}/@/recorder/' + item.id, function(data) {
            $('#details').text(data);
            $('.prettyprint').removeClass('prettyprinted');
            prettyPrint();
            $('pre > span.pln').remove();
      }, 'text');
    });

      dataView.onRowCountChanged.subscribe(function (e, args) {
        grid.updateRowCount();
        grid.render();
      });

      dataView.onRowsChanged.subscribe(function (e, args) {
        grid.invalidateRows(args.rows);
        grid.render();
      });


      $("#search").keyup(function (e) {
          // clear on Esc
          if (e.which == 27) {
            this.value = "";
          }

          searchString = this.value;
          updateFilter();
        });

      function updateFilter() {
          dataView.setFilterArgs({
            searchString: searchString
          });
          dataView.refresh();
        }

      dataView.beginUpdate();
        dataView.setItems(data);
        dataView.setFilterArgs({
          searchString: searchString
        });
        dataView.setFilter(myFilter);
        dataView.endUpdate();
  })
</script>
</body>
</html>
