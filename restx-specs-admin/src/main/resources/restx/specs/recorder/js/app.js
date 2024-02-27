angular.module('admin', ['ngResource']);

angular.config(function ($routeProvider, $locationProvider) {
    // undo the default ('!') to avoid breaking change from angularjs 1.6
    $locationProvider.hashPrefix('');
});

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
var sortAsc = true;
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
  alertify.set({ delay: 3000 });

  dataView = new Slick.Data.DataView({ inlineFilters: true });
  grid = new Slick.Grid("#myGrid", dataView, columns, options);

  grid.setSelectionModel(new Slick.RowSelectionModel());

  function withSelection(f) {
     var selectedRows = grid.getSelectedRows();
     if (selectedRows.length) {
         f.call(grid, selectedRows[0], dataView.getItem(selectedRows[0]));
     }
  }

  var save = function() {
      withSelection(function(selIndex, selItem) {
         $.post('../../recorders/storage/' + selItem.id + '?path=' + $('#saveToFolder').val(), function(data) {
             console.log('saved', selItem, data);
             alertify.success("Saved to " + data);
         });
      });
  };

  var selectNext = function() {
     var selectedRows = grid.getSelectedRows();
     if (selectedRows.length) {
        grid.setSelectedRows([selectedRows[0] + 1]);
     } else {
        grid.setSelectedRows([0]);
     }
  };

  $('#save').click(save);

  $('body').keyup(function(e) {
      if (e.which == 83   // s
          && $(e.target).closest('#myGrid').length > 0 // focus is in the grid
          ) {
          save();
          selectNext();
      }
  })

  grid.onSort.subscribe(function (e, args) {
    sortAsc = args.sortAsc;
    sortcol = args.sortCol.field;
    dataView.sort(comparer, sortAsc);
  });

  grid.onSelectedRowsChanged.subscribe(function(e, args) {
      withSelection(function(selIndex, selItem) {
          $.get('../../recorders/' + selItem.id, function(data) {
                $('#details').text(data);
                $('.prettyprint').removeClass('prettyprinted');
                prettyPrint();
                $('pre > span.pln').remove();
          }, 'text');
      });
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

    function setData(data) {
        dataView.beginUpdate();
        dataView.setItems(data);
        dataView.setFilterArgs({
          searchString: searchString
        });
        dataView.setFilter(myFilter);
        dataView.sort(comparer, sortAsc);
        dataView.endUpdate();
        grid.invalidateAllRows();
        grid.render();
    }

    $.getJSON('../../recorders', setData);

    $('#refresh').click(function() {
      $.getJSON('../../recorders', setData);
    })
})
