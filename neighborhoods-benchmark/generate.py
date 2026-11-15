#!/usr/bin/env python3
"""Generate the explicit move configuration of every quickstart in neighborhoods-benchmark/<quickstart>/.

For each quickstart it writes selectors-solverConfig.xml, neighborhoods-solverConfig.xml and
BenchmarkNeighborhoodProvider.java, which list the same moves, in the same order, with equal weights.
Edit SPEC below when a quickstart changes its planning entities or variables, then run:

  python3 neighborhoods-benchmark/generate.py
"""
import pathlib

OUT = pathlib.Path(__file__).resolve().parent

# quickstart -> (domain package, solution class, [(entity class, [(basic var, allowsUnassigned)], (list var, allowsUnassignedValues) | None)])
SPEC = {
    "bed-allocation": ("org.acme.bedallocation.domain", "BedPlan", [("Stay", [("bed", True)], None)]),
    "conference-scheduling": ("org.acme.conferencescheduling.domain", "ConferenceSchedule",
                              [("Talk", [("timeslot", False), ("room", False)], None)]),
    "employee-scheduling": ("org.acme.employeescheduling.domain", "EmployeeSchedule", [("Shift", [("employee", False)], None)]),
    "facility-location": ("org.acme.facilitylocation.domain", "FacilityPlan", [("Consumer", [("facility", False)], None)]),
    "flight-crew-scheduling": ("org.acme.flightcrewscheduling.domain", "FlightCrewSchedule",
                               [("FlightAssignment", [("employee", False)], None)]),
    "food-packaging": ("org.acme.foodpackaging.domain", "PackagingSchedule", [("Line", [("operator", False)], ("jobs", True))]),
    "maintenance-scheduling": ("org.acme.maintenancescheduling.domain", "MaintenanceSchedule",
                               [("Job", [("crew", False), ("startDate", False)], None)]),
    "meeting-scheduling": ("org.acme.meetingschedule.domain", "MeetingSchedule",
                           [("MeetingAssignment", [("startingTimeGrain", False), ("room", False)], None)]),
    "order-picking": ("org.acme.orderpicking.domain", "PickingPlan", [("PickingCart", [], ("pickTasks", False))]),
    "project-job-scheduling": ("org.acme.projectjobschedule.domain", "ProjectJobSchedule",
                               [("Allocation", [("executionMode", False), ("delay", False)], None)]),
    "school-timetabling": ("org.acme.schooltimetabling.domain", "Timetable",
                           [("Lesson", [("timeslot", False), ("room", False)], None)]),
    "sports-league-scheduling": ("org.acme.sportsleagueschedule.domain", "LeagueSchedule", [("Match", [("round", False)], None)]),
    "task-assigning": ("org.acme.taskassigning.domain", "TaskAssigningSolution", [("Employee", [], ("tasks", True))]),
    "tournament-scheduling": ("org.acme.tournamentschedule.domain", "TournamentSchedule",
                              [("TeamAssignment", [("team", False)], None)]),
}

PROVIDER = "BenchmarkNeighborhoodProvider"


def construction_xml(pkg, entities):
    """A bare construction heuristic cannot handle an entity with both basic and list variables;
    give such a model the same two phases as the default phase list: basic variables first, then the list variable."""
    mixed = [(e, b, l) for e, b, l in entities if b and l]
    if not mixed:
        return "  <constructionHeuristic/>"
    out = []
    for entity, basics, (lvar, _) in mixed:
        fq = f"{pkg}.{entity}"
        changes = "\n".join(f"""      <changeMoveSelector>
        <entitySelector mimicSelectorRef="placerEntity{entity}"/>
        <valueSelector variableName="{var}"/>
      </changeMoveSelector>""" for var, _ in basics)
        out.append(f"""  <constructionHeuristic>
    <queuedEntityPlacer>
      <entitySelector id="placerEntity{entity}">
        <entityClass>{fq}</entityClass>
      </entitySelector>
{changes}
    </queuedEntityPlacer>
  </constructionHeuristic>
  <constructionHeuristic>
    <queuedValuePlacer>
      <valueSelector id="placerValue{entity}" variableName="{lvar}"/>
      <changeMoveSelector>
        <valueSelector mimicSelectorRef="placerValue{entity}" variableName="{lvar}"/>
      </changeMoveSelector>
    </queuedValuePlacer>
  </constructionHeuristic>""")
    return "\n".join(out)


def selectors_xml(pkg, entities):
    sel = []
    for entity, basics, lst in entities:
        fq = f"{pkg}.{entity}"
        for var, _ in basics:
            sel.append(f"""        <changeMoveSelector>
          <entitySelector>
            <entityClass>{fq}</entityClass>
          </entitySelector>
          <valueSelector variableName="{var}"/>
        </changeMoveSelector>""")
        if basics:
            sel.append(f"""        <swapMoveSelector>
          <entitySelector>
            <entityClass>{fq}</entityClass>
          </entitySelector>
        </swapMoveSelector>""")
        if lst:
            var = lst[0]
            for kind in ("listChangeMoveSelector", "listSwapMoveSelector"):
                sel.append(f"""        <{kind}>
          <valueSelector variableName="{var}"/>
        </{kind}>""")
            sel.append(f"""        <kOptListMoveSelector>
          <minimumK>2</minimumK>
          <maximumK>2</maximumK>
          <valueSelector variableName="{var}"/>
        </kOptListMoveSelector>""")
    body = "\n".join(line[2:] for line in "\n".join(sel).splitlines())
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<!-- Same moves, same order and same weights as {PROVIDER}.java. -->
<solver xmlns="https://timefold.ai/xsd/solver">
{construction_xml(pkg, entities)}
  <localSearch>
    <unionMoveSelector>
{body}
    </unionMoveSelector>
  </localSearch>
</solver>
"""


def neighborhoods_xml(pkg, entities):
    return f"""<?xml version="1.0" encoding="UTF-8"?>
<solver xmlns="https://timefold.ai/xsd/solver">
  <enablePreviewFeature>NEIGHBORHOODS</enablePreviewFeature>
  <neighborhoods>
    <neighborhoodProviderClass>{pkg}.{PROVIDER}</neighborhoodProviderClass>
  </neighborhoods>
{construction_xml(pkg, entities)}
  <localSearch/>
</solver>
"""


def provider_java(pkg, solution, entities):
    lines, imports = [], set()
    for entity, basics, lst in entities:
        v = entity[0].lower() + entity[1:]
        lines.append(f"        var {v} = solution.genuineEntity({entity}.class);")
        for var, nullable in basics:
            imports.add("PlanningVariableMetaModel")
            imports.add("ChangeMoveProvider")
            lines.append(f"        builder.add(new ChangeMoveProvider<>(\n"
                         f"                (PlanningVariableMetaModel<{solution}, {entity}, Object>) {v}.variable(\"{var}\"), {str(nullable).lower()}));")
        if basics:
            imports.add("SwapMoveProvider")
            lines.append(f"        builder.add(new SwapMoveProvider<>({v}));")
        if lst:
            var, nullable = lst
            imports.update(("PlanningListVariableMetaModel", "ListChangeMoveProvider", "ListSwapMoveProvider",
                            "TwoOptListMoveProvider"))
            lv = f"{v}{var[0].upper()}{var[1:]}"
            lines.append(f"        var {lv} = (PlanningListVariableMetaModel<{solution}, {entity}, Object>) {v}.variable(\"{var}\");")
            lines.append(f"        builder.add(new ListChangeMoveProvider<>({lv}, {str(nullable).lower()}));")
            lines.append(f"        builder.add(new ListSwapMoveProvider<>({lv}));")
            lines.append(f"        builder.add(new TwoOptListMoveProvider<>({lv}, true));")
    imp = sorted(
        [f"ai.timefold.solver.core.preview.api.domain.metamodel.{i}" for i in imports if i.endswith("MetaModel")]
        + [f"ai.timefold.solver.core.preview.api.move.builtin.{i}" for i in imports if i.endswith("Provider")]
        + [f"ai.timefold.solver.core.preview.api.neighborhood.{i}" for i in ("Neighborhood", "NeighborhoodBuilder",
                                                                              "NeighborhoodProvider")])
    imp_block = "\n".join(f"import {i};" for i in imp)
    body = "\n".join(lines)
    return f"""package {pkg};

{imp_block}

/**
 * Same moves, same order and same weights as selectors-solverConfig.xml.
 * crossingNull matches what the selectors include for the variable.
 */
@SuppressWarnings("unchecked")
public class {PROVIDER} implements NeighborhoodProvider<{solution}> {{

    @Override
    public Neighborhood defineNeighborhood(NeighborhoodBuilder<{solution}> builder) {{
        var solution = builder.getSolutionMetaModel();
{body}
        return builder.build();
    }}
}}
"""


for qs, (pkg, solution, entities) in SPEC.items():
    d = OUT / qs
    d.mkdir(parents=True, exist_ok=True)
    (d / "selectors-solverConfig.xml").write_text(selectors_xml(pkg, entities))
    (d / "neighborhoods-solverConfig.xml").write_text(neighborhoods_xml(pkg, entities))
    (d / f"{PROVIDER}.java").write_text(provider_java(pkg, solution, entities))
print(f"Generated {len(SPEC)} quickstarts in {OUT}.")
