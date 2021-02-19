package agents;

import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.grid.Grid;

public class Mall {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	public Mall(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space = space;
		this.grid = grid;
	}
}
