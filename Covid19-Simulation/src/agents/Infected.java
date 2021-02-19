package agents;
 
import java.util.ArrayList;    
import java.util.List;

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
//import repast.simphony.space.graph.Network;
import repast.simphony.space.grid.Grid;
import repast.simphony.space.grid.GridPoint;
import repast.simphony.util.ContextUtils;
import repast.simphony.util.SimUtilities;

public class Infected {
	private ContinuousSpace<Object> space;
	private Grid<Object> grid;
	private int age;
	
	private boolean symptomatic;
	public boolean hospitalized;
	public boolean works; 
	public boolean studies; 
	private boolean AllowedToGoOut;
	//private boolean compliesWithPolicies;
	//private boolean wantsToGoOut;
	private boolean wearsMask;
	private boolean atHospital;
	private boolean recovered;
	private boolean wantsToGoToMall;
	private double probability_of_death;
	
	public Hospital hospital;
	public School agent_school; 
	public Workplace agent_workplace;
	private Mall agent_Mall;
	
	Parameters params = RunEnvironment.getInstance().getParameters();
	private float prob_to_go_to_hospital = (Float)params.getValue("prob_to_go_to_hospital");
	private float chance_to_infect = (Float)params.getValue("chance_to_infect");
	private float prob_to_recover_hospital = (Float)params.getValue("prob_to_recover_hospital");
	private float prob_to_recover_naturally = (Float)params.getValue("prob_to_recover_naturally");
	//private float prob_to_die = (Float)params.getValue("prob_to_die");
	private float prob_to_wear_mask = (Float)params.getValue("prob_to_wear_mask");
	private String movement_scenario = (String)params.getValue("movement_scenario");
	private String limitation_strategy = (String)params.getValue("limitation_strategy");
	
	
	/*
	 * Constructor for context builder
	 */
	public Infected(ContinuousSpace<Object> space,Grid <Object> grid) {
		this.space = space;
		this.grid = grid;
		this.symptomatic = false;
		this.hospitalized = false; 
		this.hospital = null;
		this.agent_school = null;
		this.agent_workplace = null;
		this.agent_Mall = null; 
		this.works = false; 
		this.studies = false; 
        this.age= Randomizer.getRandomAge();
        this.probability_of_death = Randomizer.getDeathProbability(this.age);
        
        if (this.age >= 6 && this.age <= 25) {
        	this.studies = true; 
        }
        else if (this.age > 25 && this.age <=65){
        	this.works = true;
        }	
	}
	
	/*
	 * Constructor for Infected class, in order to keep the same attributes when we delete the Healthy agent and spawn an Infected one	
	 */
	public Infected(ContinuousSpace<Object> space, Grid<Object> grid, int age, boolean works, boolean studies,
			School agent_school, Workplace agent_workplace) {
		//attributes to keep
		this.age = age;
		this.works = works;
		this.studies = studies;
		this.agent_school = agent_school;
		this.agent_workplace = agent_workplace;
		//extra attributes
		this.space = space;
		this.grid = grid;
		this.symptomatic = false;
		this.hospitalized = false; 
		this.atHospital = false;
		this.hospital = null;

	}

	/*
	* SCHEDULED METHOD ( 4 scheduled methods in total: step(), die(), go_to_hospital(), recover_naturally() )
	* Here we manage recurring events such as the different movement scenarios and the action of infecting people 
	* As well as implementing the different limitation strategies
	*/
	@ScheduledMethod(start = 1, interval = 1,priority = 2)
	public void step() {	
		go_to_hospital();
		if (this.atHospital)
			return;
		if (this.recovered)
			return;
		
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
			infect();
		}
	}
	
	/*
	 * Initialize AllowedToGoOut according to limitation strategy
	 * Update wears mask every 24hours
	 */
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
		if (limitation_strategy.equals("isolateInfectious")) {
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
	
	/*
	* Random movement function 
	*/
	public void randomMove() {
		//get the grid location of this human 
		GridPoint pt = grid.getLocation(this);
		//use the GridCellNgh class to create GridCells for 
		//the surrounding neighbourhood
		GridCellNgh<Object> nghCreator = new GridCellNgh<Object>(grid,pt,
				Object.class,1,1);
		List<GridCell<Object>> gridCells = nghCreator.getNeighborhood(true);
		SimUtilities.shuffle(gridCells,RandomHelper.getUniform());	
		GridCell<Object> cell = gridCells.get(0);
		GridPoint point_to_move = cell.getPoint();
		moveTowards(point_to_move);
	}
	
	/*
	* Agents will move randomly in the viscinity of their respective central attractor agent (mall, workplace or school) 
	*/
	public void hoverAroundAttractors(Object attractorAgent) {
		int radius_x =2; 
		int radius_y =2; 
		//get the grid location of this human 
		GridPoint pt = grid.getLocation(this);
		GridPoint pt_att = grid.getLocation(attractorAgent);
		//use GridCellNgh to find the neighbourhood of attractor agent (we will use a radius of 1 in each direction)
		GridCellNgh<Object> nghCreator_att = new GridCellNgh<Object>(grid,pt_att,
				Object.class,radius_x,radius_y);
		List<GridCell<Object>> gridCells_att = nghCreator_att.getNeighborhood(true);
		
		//use the GridCellNgh class to create GridCells for 
		//this human's surrounding neighbourhood
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
	
	
	/*
	* Move towards a point in steps of 2 
	*/
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
	
	/*
	* Infect all the neighbouring healthy agents based on a probability
	*/
	public void infect() {
		GridPoint pt = grid.getLocation(this);
		List<Object> healthy = new ArrayList<Object>();
		//Get list of all healthy people at the new location
		
		for (Object obj : grid.getObjectsAt(pt.getX(),pt.getY())) {
			if (obj instanceof Healthy) {
				healthy.add(obj);
			}
		}
		
		//infect the Healthy non socially isolated people in the list based on a probability
		if (healthy.size() > 0) {
			for(Object obj : healthy) {
				double random = Math.random();
				//impact of mask 
				if (this.wearsMask) {
					double maskFactor = Randomizer.getRandomMaskFactor();
					chance_to_infect = (float) (chance_to_infect*(1-maskFactor)); 
				}
				if (((Healthy)obj).isWearsMask()){
					double maskFactor = Randomizer.getRandomMaskFactor();
					chance_to_infect = (float) (chance_to_infect*(1-maskFactor));
				}
				//infect based on a probability
				if (random <= chance_to_infect && !((Healthy) obj).social_isolate) {
					NdPoint spacePt = space.getLocation(obj);
					Context <Object> context = ContextUtils.getContext(obj);
					//remove the healthy person and spawn an infected person instead
					context.remove(obj);
					Infected infected = new Infected(space, grid,((Healthy)obj).getAge(),((Healthy)obj).isWorks(),((Healthy)obj).isStudies(),
							((Healthy)obj).getAgent_school(),((Healthy)obj).getAgent_workplace());
					context.add(infected);
					space.moveTo(infected, spacePt.getX(), spacePt.getY());
					grid.moveTo(infected, pt.getX(), pt.getY());
					
					/*Network<Object> net = (Network<Object>) context.getProjection("infection network");
					net.addEdge(this, infected);*/
				}
			}
		}
	}
		
	/* 
	 * Go through all hospitals in the context,find the closest one,update it's capacity
	 *  then return it 	
	 */
	public Hospital getNearestHospital() { 
		double minDistSq = Double.POSITIVE_INFINITY;
		Hospital minAgent = null; 
		NdPoint myLocation;
		Context context = ContextUtils.getContext(this);
			
		for(Object agent : context) {
			if (agent instanceof Hospital) {
				Hospital thishospital = (Hospital) agent;
				if (thishospital.current_capacity > 0) {
					NdPoint currloc = space.getLocation(this);
					NdPoint loc = space.getLocation(agent);
					//Euclidian distance
					double distSq = currloc.getX()- loc.getX() * currloc.getX()- loc.getX() + currloc.getY() - loc.getY() * currloc.getY() -loc.getY();
					if (distSq < minDistSq) {
						minDistSq = distSq; 
						minAgent = (Hospital) agent;
					}
				}
			}
		}
		if(minAgent != null) minAgent.current_capacity-- ;
		return minAgent;
	}
	/*
	 * SCHEDULED METHOD ( 4 scheduled methods in total: step(), die(), go_to_hospital(), recover_naturally() )
	 * Takes infected agent to the hospital based on a probabilty that he actually goes to the hospital "prob_to_go_to_hospital"
	 * He could recover based on a probability "prob_to_recover", we then spawn a Recovered agent and finally delete the Infected agent	
	 */
	//@ScheduledMethod(start = 1, interval = 1,priority = 1)
	private void go_to_hospital() {

		if (Math.random() < prob_to_go_to_hospital){
			//Get nearest hospital
			//Send agent there
			Hospital nearest_hospital = getNearestHospital();
			if(nearest_hospital == null) return; 
			NdPoint target_location = space.getLocation(nearest_hospital);
			space.moveTo(this, (double)target_location.getX(), (double) target_location.getY());
			grid.moveTo(this, (int)target_location.getX(), (int) target_location.getY());
			this.hospital = nearest_hospital;
			this.hospitalized = true; 
			this.atHospital = true;	

			//remove the infectious person and spawn a recovered person instead
			if(Math.random() < prob_to_recover_hospital) {
				GridPoint pt = grid.getLocation(this);
				NdPoint spacePt = space.getLocation(this);
				Context <Object> context = ContextUtils.getContext(this);
				Recovered recovered = new Recovered(space, grid,this.studies,this.agent_school,this.works,this.agent_workplace,this.age);
				context.add(recovered);
				space.moveTo(recovered, spacePt.getX(), spacePt.getY());
				grid.moveTo(recovered, pt.getX(), pt.getY());
				
				if (context.size() > 1) {
					this.recovered = true;
					context.remove(this);
				}
				else {
					RunEnvironment.getInstance().endRun();
				}
					
			}
		}
	}
	
	/*
	 * SCHEDULED METHOD ( 4 scheduled methods in total: step(), die(), go_to_hospital(), recover_naturally() )
	 * Agent might recover naturally, based on a probability "prob_to_recover_naturally", an agent of type "Recovered" is spawned in his place
	 */
	@ScheduledMethod(start = 1, interval = 1,priority = 1)
	public void recover_naturally() {
		if(Math.random() < prob_to_recover_naturally) {
			GridPoint pt = grid.getLocation(this);
			NdPoint spacePt = space.getLocation(this);
			Context <Object> context = ContextUtils.getContext(this);
			Recovered recovered = new Recovered(space, grid,this.studies,this.agent_school,this.works,this.agent_workplace,this.age);
			context.add(recovered);
			space.moveTo(recovered, spacePt.getX(), spacePt.getY());
			grid.moveTo(recovered, pt.getX(), pt.getY());
			if (context.size() > 1)
				context.remove(this);
			else
				RunEnvironment.getInstance().endRun();
		}
	}
	
	/*
	 * SCHEDULED METHOD ( 4 scheduled methods in total: step(), die(), go_to_hospital(), recover_naturally() )
	 * Die function, scheduled at every step, the agent dies based on a probability "prob_to_die", an agent of type "Dead" is spawned in his place
	 */
	@ScheduledMethod(start = 1, interval = 1,priority = 1)
	public void die() {
		if(Math.random() < probability_of_death) {
			GridPoint pt = grid.getLocation(this);
			NdPoint spacePt = space.getLocation(this);
			Context <Object> context = ContextUtils.getContext(this);
			Dead dead = new Dead(space, grid);
			context.add(dead);
			space.moveTo(dead, spacePt.getX(), spacePt.getY());
			grid.moveTo(dead, pt.getX(), pt.getY());
			if (context.size() > 1)
				context.remove(this);
			else
				RunEnvironment.getInstance().endRun();
		}
	}
	
	/*
	 * Gets the nearest mall
	 */
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
	 * Gets the nearest school 
	 */
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
	
	/*
	 * Gets the nearest workplace
	 */
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
	
	
}
