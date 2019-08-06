package solver;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import Utils.Utils;
import game.Game;
import game.Game.Action;
import game.Game.Node;
import gurobi.GRB;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class ApproximationSolver extends ZeroSumGameSolver {
	GRBModel model;
	GRBLinExpr objective;
	GRBLinExpr brConstraints;
	GRBLinExpr honeyflowConstraints;
	GRBLinExpr p2ActionConstraints;
	HashMap<String, GRBLinExpr> strategyVarsByAction;
	HashMap<String, GRBLinExpr> p2strategyVarsByAction;
	HashMap<String, GRBVar> p2strategy;
	HashMap<String, GRBLinExpr>[] zVarsByAction;
	private int zCnt;

	public ApproximationSolver(Game game) {
		super(game);
		try {
			initializeDataStructure();
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void initializeDataStructure() throws GRBException {
		try {
			GRBEnv env = new GRBEnv("ApproximationSolver.log");
			model = new GRBModel(env);
		} catch (GRBException e) {
			System.out.println("Error LPSolver(): Gurobi setup failed");
		}
		zCnt = 0;
		objective = new GRBLinExpr();
		brConstraints = new GRBLinExpr();
		honeyflowConstraints = new GRBLinExpr();
		p2ActionConstraints = new GRBLinExpr();
		this.zVarsByAction = (HashMap<String, GRBLinExpr>[]) new HashMap[Utils.TOTAL_ATTACKER_ACTION_NO + 1];
		for (int i = 0; i <= Utils.TOTAL_ATTACKER_ACTION_NO; i++) {
			this.zVarsByAction[i] = new HashMap<String, GRBLinExpr>();
		}
		this.strategyVarsByAction = new HashMap<String, GRBLinExpr>();
		this.p2strategyVarsByAction = new HashMap<String, GRBLinExpr>();
		this.p2strategy = new HashMap<String, GRBVar>();
		CreateVariablesAndExpressions(0, null, null, -1, null,false);
		setConstraints();
		SetObjective();
	}

	private GRBVar twoBinaryVarLinearization(GRBLinExpr var1, GRBVar var2) throws GRBException {

		GRBLinExpr lz1 = new GRBLinExpr();
		GRBLinExpr lz2 = new GRBLinExpr();
		GRBLinExpr lz3 = new GRBLinExpr();
		GRBVar z = model.addVar(0, Utils.MAX_LIMIT_OF_HONEY_FLOW, 0, GRB.INTEGER, "z" + zCnt);

		/* z-var1 <= 0 */
		lz1.addTerm(-1, z);
		lz1.add(var1);
		model.addConstr(lz1, GRB.GREATER_EQUAL, 0, "TLZ1" + zCnt);

		/* z-var2 <= 0 */
		lz2.addTerm(1, z);
		lz2.addTerm(-1, var2);
		model.addConstr(lz2, GRB.LESS_EQUAL, 0, "TLZ2" + zCnt);

		/* Z-Var1-Var2+1 <= 0 */
		lz3.addTerm(-1, z);
		lz3.add(var1);
		lz3.addTerm(1, var2);
		model.addConstr(lz3, GRB.GREATER_EQUAL, 1, "TLZ3" + zCnt);

		return z;

	}

	private void CreateVariablesAndExpressions(int currentNodeId, GRBLinExpr[] parentVariable, GRBVar childVariable,
			int childID, String childAction, boolean isReal) throws GRBException {
		Node node = game.getNodeById(currentNodeId);
		if (node.isLeaf()) {
			double p1value = node.getP1Value();
			double p2value = node.getP2Value();
			GRBLinExpr sumP2Action = new GRBLinExpr();
			if (isReal == false) {
				System.out.println("Size = " + parentVariable.length);
				GRBLinExpr sumZ = new GRBLinExpr();
				for (int i = 0; i < parentVariable.length; i++) {
					GRBVar z = twoBinaryVarLinearization(parentVariable[i], childVariable);
					sumZ.addTerm(1, z);
				}
				objective.multAdd(p1value, sumZ);
				brConstraints.multAdd(p2value, sumZ);
				sumP2Action.multAdd(p2value, sumZ);
				p2ActionConstraints.add(sumZ);
				p2strategyVarsByAction.put("node : " + childID + " action: " + childAction, sumZ);
			} else {
				GRBLinExpr tmp = new GRBLinExpr();
				tmp.addTerm(1,childVariable);
				objective.multAdd(p1value, tmp);
				brConstraints.multAdd(p2value, tmp);
				sumP2Action.multAdd(p2value, tmp);
				p2ActionConstraints.add(tmp);
				p2strategyVarsByAction.put("node : " + childID + " action: " + childAction, tmp);
				
			}
			if (childAction.equals("No-attack"))
				zVarsByAction[0].put(childID + childAction, sumP2Action);
			else if (childAction.equals("Attack-As-V1"))
				zVarsByAction[1].put(childID + childAction, sumP2Action);
			else
				zVarsByAction[2].put(childID + childAction, sumP2Action);
			// primalConstraints.put(node.getInformationSet(),
			 //System.out.println(childAction + " " + value);
			return;
		}

		for (Action action : node.getActions()) {
			if (node.getPlayer() == 1) {
				if (game.getNodeById(action.getChildId()).getIsReal() == false) {
					GRBLinExpr[] binaryVars = new GRBLinExpr[Utils.MAX_LIMIT_OF_HONEY_FLOW + 1];
					GRBLinExpr sum = new GRBLinExpr();
					for (int i = 0; i <= Utils.MAX_LIMIT_OF_HONEY_FLOW; i++) {
						GRBVar v = model.addVar(0, 1, 0, GRB.BINARY,
								"node:" + node.getNodeId() + "  action:" + action.getName());
						GRBLinExpr bVar = new GRBLinExpr();
						bVar.addTerm(i, v);
						binaryVars[i] = bVar;
						sum.add(bVar);

					}
					//System.out.println("Action : " + action.getName());
					honeyflowConstraints.add(sum);
					strategyVarsByAction.put("node : " + node.getNodeId() + "action: " + action.getName(), sum);
					CreateVariablesAndExpressions(action.getChildId(), binaryVars, childVariable, childID, childAction, node.getIsReal());
				}else {
					GRBLinExpr[] binaryVars = new GRBLinExpr[1];
					GRBLinExpr bVar = new GRBLinExpr();
					bVar.addConstant(1);
					binaryVars[0] = bVar;
			    	CreateVariablesAndExpressions(action.getChildId(), binaryVars, childVariable, childID, childAction, true);
				}

			} else if (node.getPlayer() == 2) {
				GRBVar v = model.addVar(0, 1, 0, GRB.BINARY,
						"node:" + node.getNodeId() + "  action:" + action.getName());
				//p2ActionConstraints.addTerm(1, v);
				p2strategy.put("node : " + node.getNodeId() + "action: " + action.getName(), v);
				CreateVariablesAndExpressions(action.getChildId(), parentVariable, v, node.getNodeId(),
						action.getName(), isReal);
			}
		}
	}

	private void setConstraints() throws GRBException {
		model.addConstr(honeyflowConstraints, GRB.LESS_EQUAL, Utils.MAX_LIMIT_OF_HONEY_FLOW,
				"P1-Honeyflow-Constraints");
		//System.out.println("Size " + p2ActionConstraints.size());
		model.addConstr(p2ActionConstraints, GRB.EQUAL, 1, "P2-Action-Constraints");
		for (int i = 0; i < Utils.TOTAL_ATTACKER_ACTION_NO; i++) {
			Set entrySet = zVarsByAction[i].entrySet();
			Iterator it = entrySet.iterator();
			GRBLinExpr lhs = new GRBLinExpr();
			while (it.hasNext()) {
				Map.Entry pair = (Map.Entry) it.next();
				// System.out.println("Key : " + pair.getValue());
				lhs.add((GRBLinExpr) pair.getValue());
				//it.remove();
			}
			if (null != lhs) {
				model.addConstr(lhs, GRB.GREATER_EQUAL, brConstraints, "P2-Action-Constraints-Br");
			}
		}

	}

	private void SetObjective() throws GRBException {

		model.setObjective(objective, GRB.MAXIMIZE);
		System.out
				.println("************************************************ Objective ***************************");
		System.out.println("Objective Function : " + objective );
	}

	@Override
	public void solveGame() {
		try {

			if (model.feasibility() != null) {
				model.optimize();
				valueOfGame = model.get(GRB.DoubleAttr.ObjVal);
				System.out.println("Defender's utility : " + valueOfGame);
			}

		} catch (GRBException e) {
			e.printStackTrace();
			System.out.println("Error SequenceFormLPSolver::solveGame: solve exception");
		}
	}

	@Override
	public void printStrategyVarsAndGameValue() {
		
		System.out.println(".........................P1 Strategy...................");
		Set entrySet = strategyVarsByAction.entrySet();
		Iterator it = entrySet.iterator();
		while (it.hasNext()) {
			Map.Entry pair = (Map.Entry) it.next();
			GRBLinExpr v = (GRBLinExpr) pair.getValue();
			try {
				System.out.println(pair.getKey() + ": \t" + v.getValue());
			} catch (GRBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println(".........................P2 Strategy...................");
		Set entrySetP2 = p2strategyVarsByAction.entrySet();		
		Iterator itt = entrySetP2.iterator();
		while (itt.hasNext()) {
			Map.Entry pair = (Map.Entry) itt.next();
			GRBLinExpr v = (GRBLinExpr) pair.getValue();
			try {
				System.out.println(pair.getKey() + ": \t" + v.getValue());
			} catch (GRBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public void printGameValue() {
		System.out.println(".........................P2 Strategy...................");
		Set entrySetP2 = p2strategy.entrySet();		
		Iterator itt = entrySetP2.iterator();
		while (itt.hasNext()) {
			Map.Entry pair = (Map.Entry) itt.next();
			GRBVar v = (GRBVar) pair.getValue();
			try {
				System.out.println(v.get(GRB.StringAttr.VarName) + ": \t" +v.get(GRB.DoubleAttr.X) + "\n");
			} catch (GRBException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	@Override
	public double[][][] getStrategyProfile() {
		// TODO Auto-generated method stub
		return null;
	}

}
