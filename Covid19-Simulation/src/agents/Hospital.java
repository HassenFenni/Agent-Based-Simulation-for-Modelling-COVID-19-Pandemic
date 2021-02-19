package agents;



import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Hospital {
	Parameters params = RunEnvironment.getInstance().getParameters();
	private int number_of_rooms = (Integer)params.getValue("number_of_rooms");
	
	public int current_capacity;
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public Hospital(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
		this.current_capacity = number_of_rooms;
	}
}
