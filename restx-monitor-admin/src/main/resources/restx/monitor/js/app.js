adminApp = angular.module('admin', ['ngResource']);

adminApp.config(function($routeProvider) {
    $routeProvider.
        when('/', {
            controller: 'MetricsController',
            templateUrl: 'views/metrics.html'
        }).
        when('/sessions', {
            controller: 'SessionsController',
            templateUrl: 'views/sessions.html'
        });
});

adminApp.factory('Metrics', function($resource) {
    return $resource('../../metrics');
});

adminApp.factory('SessionStats', function($resource) {
    return $resource('../../sessionStats');
});

adminApp.controller('SessionsController', function($rootScope, $scope, SessionStats) {
    $scope.searchQuery = '';
    $rootScope.$on('search', function() {
        $scope.searchQuery = $rootScope.searchQuery;
    })

    $scope.init = function() {
        $scope.sessionStats = SessionStats.query();
    }
});

adminApp.controller('MetricsController', function($rootScope, $scope, Metrics) {
    $scope.init = function() {
        $scope.metrics = Metrics.get();
    }

    initGrid();
});

function initGrid() {
    var grid, dataView;
    var columns = [
        {id: "label", name: "Label", field: "label", width: 220, sortable: true},
        {id: "hits", name: "Hits", field: "hits", width: 48, sortable: true},
        {id: "avg", name: "Avg", field: "avg", width: 78, sortable: true},
        {id: "lastVal", name: "Last Value", width: 78, field: "lastVal", sortable: true},
        {id: "min", name: "Min", field: "min", width: 78, sortable: true},
        {id: "max", name: "Max", field: "max", width: 78, sortable: true},
        {id: "active", name: "Active", field: "active", width: 58, sortable: true},
        {id: "avgActive", name: "Avg Active", field: "avgActive", width: 70, sortable: true}
    ];

    var sortcol = "label";
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
        if (args.searchString != "" && item["label"].indexOf(args.searchString) == -1) {
            return false;
        }

        return true;
    }

    $(function () {
        dataView = new Slick.Data.DataView({ inlineFilters: true });
        grid = new Slick.Grid("#myGrid", dataView, columns, options);

        grid.onSort.subscribe(function (e, args) {
            sortAsc = args.sortAsc;
            sortcol = args.sortCol.field;
            dataView.sort(comparer, sortAsc);
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

        $.getJSON('../../monitor', setData);

        $('#refresh').click(function() {
            $.getJSON('../../monitor', setData);
        })
    })
}