package agents;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class School {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	public School(ContinuousSpace<Object> space, Grid<Object> grid) {
		super();
		this.space = space;
		this.grid = grid;
	}
}
