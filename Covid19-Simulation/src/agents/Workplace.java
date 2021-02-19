package agents;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Workplace {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	public Workplace(ContinuousSpace<Object> space, Grid<Object> grid) {
		super();
		this.space = space;
		this.grid = grid;
	}
}
