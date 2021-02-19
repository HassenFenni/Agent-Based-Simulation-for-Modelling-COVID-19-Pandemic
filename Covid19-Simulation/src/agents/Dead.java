package agents;

import repast.simphony.engine.schedule.ScheduledMethod; 
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Dead {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	
	public Dead(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space=space;
		this.grid=grid;
	}
	
}
