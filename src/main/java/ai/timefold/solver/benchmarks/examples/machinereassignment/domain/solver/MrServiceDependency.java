package ai.timefold.solver.benchmarks.examples.machinereassignment.domain.solver;

import ai.timefold.solver.benchmarks.examples.machinereassignment.domain.MrService;

public class MrServiceDependency {

    private MrService fromService;
    private MrService toService;

    public MrServiceDependency() {
    }

    public MrServiceDependency(MrService fromService, MrService toService) {
        this.fromService = fromService;
        this.toService = toService;
    }

    public MrService getFromService() {
        return fromService;
    }

    public void setFromService(MrService fromService) {
        this.fromService = fromService;
    }

    public MrService getToService() {
        return toService;
    }

    public void setToService(MrService toService) {
        this.toService = toService;
    }

}
