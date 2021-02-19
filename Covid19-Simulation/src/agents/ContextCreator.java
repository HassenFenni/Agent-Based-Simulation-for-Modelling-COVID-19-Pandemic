package agents;

import repast.simphony.context.Context; 
import repast.simphony.context.space.continuous.ContinuousSpaceFactory;
import repast.simphony.context.space.continuous.ContinuousSpaceFactoryFinder;
import repast.simphony.context.space.graph.NetworkBuilder;
import repast.simphony.context.space.grid.GridFactory;
import repast.simphony.context.space.grid.GridFactoryFinder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.continuous.RandomCartesianAdder;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridBuilderParameters;
import repast.simphony.space.grid.SimpleGridAdder;
import repast.simphony.space.grid.WrapAroundBorders;

public class ContextCreator implements repast.simphony.dataLoader.ContextBuilder<Object> {

	@Override
	public Context build(Context<Object> context) {
		//Initially we made this to show the infection network, we will not be using it or adding any edges because it clutters the screen 
		NetworkBuilder<Object> netBuilder = new NetworkBuilder<Object>(
				"infection network", context, true);
		netBuilder.buildNetwork();
		
		context.setId("Covid19-Simulation");
		Parameters params = RunEnvironment.getInstance().getParameters();
		int gridHeight = (Integer) params.getValue("gridHeight");
		int gridWidth = (Integer) params.getValue("gridWidth");
		
		//continuous space
		ContinuousSpaceFactory spaceFactory = ContinuousSpaceFactoryFinder
				.createContinuousSpaceFactory(null);
		ContinuousSpace<Object> space = spaceFactory.createContinuousSpace(
				"space", context, new RandomCartesianAdder<Object>(),
				new repast.simphony.space.continuous.WrapAroundBorders(), gridHeight, gridWidth);
		//grid
		GridFactory gridFactory = GridFactoryFinder.createGridFactory(null);
		Grid<Object> grid = gridFactory.createGrid("grid", context,
				new GridBuilderParameters<Object>(new WrapAroundBorders(),
						new SimpleGridAdder<Object>(), true, gridHeight, gridWidth));
		
		
		int infectedCount = (Integer)params.getValue("infected_count");
		
		for (int i=0; i<infectedCount; i++) {
			context.add(new Infected(space,grid));
		}
		// int humanCount = 100;
		int healthyCount = (Integer)params.getValue("healthy_count");
		
		for (int i=0; i<healthyCount; i++) {
		//	int energy = RandomHelper.nextIntFromTo(4,10);
			context.add(new Healthy(space,grid));
		}
		
		int hospitalCount = (Integer)params.getValue("hospital_count");
		
		for (int i=0; i<hospitalCount; i++) {
			context.add(new Hospital(space,grid));
		}
		
		int SchoolCount = (Integer)params.getValue("school_count"); 
		for (int i=0; i<SchoolCount; i++) {
			context.add(new School(space,grid));
		}
		int WorkplaceCount = (Integer)params.getValue("workplace_count"); 
		for (int i=0; i<WorkplaceCount; i++) {
			context.add(new Workplace(space,grid));
		}
		int MallCount = (Integer)params.getValue("mall_count"); 
		for (int i=0; i<MallCount; i++) {
			context.add(new Mall(space,grid));
		}
		
		
		for (Object obj : context) {
			NdPoint pt = space.getLocation(obj);
			grid.moveTo(obj, (int) pt.getX(), (int) pt.getY());
		}
		
		
		return context;
	}

}
