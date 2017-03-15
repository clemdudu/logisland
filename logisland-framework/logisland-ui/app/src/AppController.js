/**
 * App Controller for the LogIsland UI
 */

export default [ 'JobsDataService', 'TopicsDataService', 'ProcessorsDataService', 'ListService', 'AppSettings', '$mdSidenav', '$log', '$scope', AppController ];

function AppController(JobsDataService, TopicsDataService, ProcessorsDataService, ListService, AppSettings, $mdSidenav, $log, $scope) {
    var self = this;
    var vm = $scope;

    self.version              = AppSettings.version;
    self.appPath              = 'TITLE';

    self.jobs                 = JobsDataService.query(function() { (self.jobs.length>0) ? vm.selectedJob = self.jobs[0] : vm.selectedJob = null; });
    vm.selectedJob            = null;
    self.selectedProcessor    = null;
    self.addJob               = addJob;
    vm.expandJobs             = true;
    self.newJobTemplate = {name: "newJobTemplate", streams: [{"name": "[Stream name]", "component": "comp1", "config": [], "processors": []}]};

    self.topics               = TopicsDataService.query(function() {});

    self.menuItems            = [   {name: "Start", direction: "right", icon: "play"},
                                    {name: "Stop", direction: "right", icon: "stop"}];

    self.querySearch      = querySearch;
    self.selectedItemChange = selectedItemChange;
    self.searchTextChange = searchTextChange;

    self.toggleList       = ListService.toggle;
    self.closeList        = ListService.close;
    self.selectJob        = selectJob;

    function addJob() {
        $log.debug("add job not implemented");
    }

    function selectJob ( job ) {
        vm.selectedJob = angular.isNumber(job) ? self.jobs[job] : job;
        if(vm.selectedJob.streams.length > 0) {
            self.selectedStream = vm.selectedJob.streams[0];
        }
    }

    function querySearch (query) {
        var results = query
                ? self.jobs.filter( createFilterFor(query) )
                : self.jobs;
        return results;
    }

    function createFilterFor(query) {
        var lowercaseQuery = angular.lowercase(query);

        return function filterFn(job) {
            return (job.name.indexOf(lowercaseQuery) === 0);
        };
    }

    function searchTextChange(text) {
          $log.info('Text changed to ' + text);
    }

    function selectedItemChange(job) {
        selectJob(job);
    }
}