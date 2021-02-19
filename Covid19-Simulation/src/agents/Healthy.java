package agents;

import java.util.List;
import java.util.Random;

import repast.simphony.context.Context;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.parameter.Parameters;
import repast.simphony.query.space.grid.GridCell;
import repast.simphony.query.space.grid.GridCellNgh;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.SpatialMath;
import repast.simphony.space.continuous.ContinuousSpace;
import repast.simphony.space.continuous.NdPoint;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Healthy {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	public boolean social_isolate;
	
	Parameters params = RunEnvironment.getInstance().getParameters();
	//private float prob_to_social_isolate = (Float)params.getValue("prob_to_social_isolate");
	private String movement_scenario = (String)params.getValue("movement_scenario");
	private String limitation_strategy = (String)params.getValue("limitation_strategy");
	private boolean works;
	private boolean studies;
	private School agent_school;
	private Workplace agent_workplace;
	private int age;
	private boolean AllowedToGoOut;
	private boolean wearsMask;
	private boolean wantsToGoToMall;
	private double prob_to_wear_mask;
	private Object agent_Mall;
	
	/*
	 * Constructor for context builder
	 */
	public Healthy(ContinuousSpace<Object> space, Grid<Object> grid) {
		this.space=space;
		this.grid=grid;
		this.works = false; 
		this.studies = false; 
		agent_school = null; 
		agent_workplace = null;
		this.AllowedToGoOut = true;
		this.age= Randomizer.getRandomAge();
        
		
        if (this.age >= 6 && this.age <= 25) {
        	this.studies = true; 
        }
        else if (this.age >= 25 && this.age <=65){
        	this.works = true;
        }
		
	}
	
	@ScheduledMethod(start = 1, interval = 1,priority =1)
	public void step() {
		Init(); 
		if ( (this.AllowedToGoOut) ){
			//movement scenario
			if (movement_scenario.equals("moveRandomly")) {
				randomMove();
			}
			else if (movement_scenario.equals("centralAttractors")){
				if (this.studies) {
					agent_school = getNearestSchool();
					hoverAroundAttractors(agent_school);
				}
				else if (this.works) {
					agent_workplace = getNearestWorkplace();
					hoverAroundAttractors(agent_workplace);
				}
				else {
					if (this.wantsToGoToMall) {
						agent_Mall = getNearestMall();
						hoverAroundAttractors(agent_Mall);
					}
					else {
						randomMove();
					}
				}	
			}
		}
	}
	
	public void Init() {
		
		double ticks = Math.max(RepastEssentials.GetTickCount(), 0);
		//limitation strategy
		if (limitation_strategy.equals("curfew")){
			if (ticks%24 >= 20 || ticks%24 <=6){
				this.AllowedToGoOut = false;
			}
			else {
				this.AllowedToGoOut = true;
			}
		}
		if (limitation_strategy.equals("lockdown")) {
			this.AllowedToGoOut = false;
		}
	
		if (limitation_strategy.equals("none")) {
			this.AllowedToGoOut = true; 
		}
		
		//We update wants to go to mall every 6hours, for the people who don't work or study 
		if (ticks % 24 ==0 || ticks ==1 ) {	
			if (!this.studies && !this.works ) {
				if(Math.random() < 0.25) {
					this.wantsToGoToMall = true;
				}
				else {
					this.wantsToGoToMall = false;
				}
			}
		}
		
		// We update the fact if the agent wears a mask or not every day 
		if (ticks % 24 ==0 || ticks ==1 ) {	
			if (Math.random()<prob_to_wear_mask) {
				this.wearsMask = true;
			}
			else {
				this.wearsMask = false;
			}
		}
		
	}
	
	private void randomMove() {
		//get the grid location of this human 
		GridPoint pt = grid.getLocation(this);
		//use the GridCellNgh class to create GridCells for 
		//the surrunding neighbourhood
		GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid,pt,
				Object.class,1,1);
		List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells,RandomHelper.getUniform());
		GridCell<Object> cell = gridCells.get(0);
		GridPoint point_to_move = cell.getPoint();
		moveTowards(point_to_move);
	}
	
	public void moveTowards(GridPoint pt) {
		//only move if we are not already in this grid location
		if(!pt.equals(grid.getLocation(this))) {
			NdPoint myPoint = space.getLocation(this);
			NdPoint otherPoint = new NdPoint(pt.getX(),pt.getY());
			double angle = SpatialMath.calcAngleFor2DMovement(space,myPoint,otherPoint);
			space.moveByVector(this, 2,angle,0);
			myPoint = space.getLocation(this);
			grid.moveTo(this, (int)myPoint.getX(), (int)myPoint.getY());
		}
	}
	
	public void hoverAroundAttractors(Object attractorAgent) {
		int radius_x =1; 
		int radius_y =1; 
		//get the grid location of this human 
		GridPoint pt = grid.getLocation(this);
		GridPoint pt_att = grid.getLocation(attractorAgent);
		//use GridCellNgh to find the neighbourhood of attractor agent (we will use a radius of 50cells)
		GridCellNgh<Object> nghCreator_att = new GridCellNgh<Object>(grid,pt_att,
				Object.class,radius_x,radius_y);
		List<GridCell<Object>> gridCells_att = nghCreator_att.getNeighborhood(true);
		
		//use the GridCellNgh class to create GridCells for 
		//the surrounding neighbourhood
		GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid,pt,
				Object.class,1,1);
		List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells,RandomHelper.getUniform());	
		GridCell<Object> cell = gridCells.get(0);
		GridPoint point_to_move = cell.getPoint();
		
		//flag = are we within attractor boudaries ?
		boolean flag = false;
		for (GridCell<Object> x:gridCells_att) {
			if (x.getPoint().equals(pt)) {
				flag=true;
			}
		}
		if (flag) {
			moveTowards(point_to_move);
		}
		else {
			//moveToAttractor(attractorAgent);
			moveTowards(pt_att);
		}
	}
	
	public void moveToAttractor(Object attractorAgent) {
		GridPoint point = grid.getLocation(this);
		int x = point.getX();
		int y = point.getY();
		point = grid.getLocation(attractorAgent);
		double x2 = point.getX();
		double y2 = point.getY();
		if(x2>x)
		x++;
		if(x2<x)
		x--;
		if(y2>y)
		y++;
		if(y2<y)
		y--;
		grid.moveTo(this, x, y);
	}
	
	public School getNearestSchool() { 
		double minDistSq = Double.POSITIVE_INFINITY;
		School minAgent = null; 
		NdPoint myLocation;
		Context context = ContextUtils.getContext(this);
		
		for(Object agent : context) {
			if (agent instanceof School) {
				NdPoint currloc = space.getLocation(this);
				NdPoint loc = space.getLocation(agent);
				//Euclidian distance
				double distSq = currloc.getX()- loc.getX() * currloc.getX()- loc.getX() + currloc.getY() - loc.getY() * currloc.getY() -loc.getY();
				if (distSq < minDistSq) {
					minDistSq = distSq; 
					minAgent = (School) agent;
				}
			}
		}
		return minAgent;
	}
	
	public Workplace getNearestWorkplace() { 
		double minDistSq = Double.POSITIVE_INFINITY;
		Workplace minAgent = null; 
		NdPoint myLocation;
		Context context = ContextUtils.getContext(this);
		
		for(Object agent : context) {
			if (agent instanceof Workplace) {
				NdPoint currloc = space.getLocation(this);
				NdPoint loc = space.getLocation(agent);
				//Euclidian distance
				double distSq = currloc.getX()- loc.getX() * currloc.getX()- loc.getX() + currloc.getY() - loc.getY() * currloc.getY() -loc.getY();
				if (distSq < minDistSq) {
					minDistSq = distSq; 
					minAgent = (Workplace) agent;
				}
			}
		}
		return minAgent;
	}
	
	public Mall getNearestMall() { 
		double minDistSq = Double.POSITIVE_INFINITY;
		Mall minAgent = null; 
		NdPoint myLocation;
		Context context = ContextUtils.getContext(this);
		
		for(Object agent : context) {
			if (agent instanceof Mall) {
				NdPoint currloc = space.getLocation(this);
				NdPoint loc = space.getLocation(agent);
				//Euclidian distance
				double distSq = currloc.getX()- loc.getX() * currloc.getX()- loc.getX() + currloc.getY() - loc.getY() * currloc.getY() -loc.getY();
				if (distSq < minDistSq) {
					minDistSq = distSq; 
					minAgent = (Mall) agent;
				}
			}
		}
		return minAgent;
	}

	/*
	 *Getters and setters 
	 */
	
	public boolean isWorks() {
		return works;
	}

	public void setWorks(boolean works) {
		this.works = works;
	}

	public boolean isStudies() {
		return studies;
	}

	public void setStudies(boolean studies) {
		this.studies = studies;
	}

	public School getAgent_school() {
		return agent_school;
	}

	public void setAgent_school(School agent_school) {
		this.agent_school = agent_school;
	}

	public Workplace getAgent_workplace() {
		return agent_workplace;
	}

	public void setAgent_workplace(Workplace agent_workplace) {
		this.agent_workplace = agent_workplace;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public boolean isWearsMask() {
		return wearsMask;
	}

	public void setWearsMask(boolean wearsMask) {
		this.wearsMask = wearsMask;
	}
}
