package solver;

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
		objective =  new GRBLinExpr();
		brConstraints =  new GRBLinExpr();
		honeyflowConstraints = new GRBLinExpr();
		CreateSequenceFormVariablesAndConstraints(0,null,null);
	}
	private GRBVar twoBinaryVarTinearization(GRBLinExpr var1, GRBVar var2) throws GRBException {

		GRBLinExpr lz1 =  new GRBLinExpr();
		GRBLinExpr lz2 =  new GRBLinExpr();
		GRBLinExpr lz3 =  new GRBLinExpr();
		GRBVar z = model.addVar(0, Utils.MAX_LIMIT_OF_HONEY_FLOW, 0, GRB.CONTINUOUS,   "z" + zCnt);

		/* z-var1 <= 0 */
		lz1.addTerm(-1, z);
		lz1.add(var1);
		model.addConstr(lz1, GRB.GREATER_EQUAL, 0, "TLZ1" + zCnt);
		

		/* z-var2 <= 0 */
		lz1.addTerm(1, z);
		lz1.addTerm(-1, var2);
		model.addConstr(lz1, GRB.LESS_EQUAL, 0, "TLZ2" + zCnt);
		

		/*Z-Var1-Var2+1 <= 0*/
		lz3.addTerm(-1, z);
		lz3.add(var1);
		lz3.addTerm(1, var2);
		model.addConstr(lz3, GRB.GREATER_EQUAL, 1, "TLZ3" +zCnt);
		
		return z;
		
		
	}
	private void CreateSequenceFormVariablesAndConstraints(int currentNodeId, GRBLinExpr[] parentVariable,
			GRBVar childVariable) throws GRBException {
		Node node = game.getNodeById(currentNodeId);
		if (node.isLeaf()) {
			double value = node.getValue();

			
			// primalConstraints.put(node.getInformationSet(),
			System.out.println(value+ ": par :" +parentVariable + " : child" + childVariable);
			return;
		}

		for (Action action : node.getActions()) {
			if (node.getPlayer() == 1) {
				GRBLinExpr[] binaryVars = new GRBLinExpr[Utils.MAX_LIMIT_OF_HONEY_FLOW];
				GRBLinExpr sum = new GRBLinExpr();
				for (int i = 0; i <= Utils.MAX_LIMIT_OF_HONEY_FLOW; i++) {
					GRBVar v = model.addVar(0, 1, 0, GRB.BINARY,
							"node:" + node.getNodeId() + "  action:" + action.getName());
					GRBLinExpr bVar = new GRBLinExpr();
					bVar.addTerm(i, v);
					binaryVars[i] = bVar;
					sum.addTerm(1,v);

				}
				
				model.addConstr(sum, GRB.EQUAL, 1, "P1" + node.getNodeId());
				CreateSequenceFormVariablesAndConstraints(action.getChildId(), binaryVars, childVariable);

			} else if (node.getPlayer() == 2) {
				GRBVar v = model.addVar(0, 1, 0, GRB.BINARY,
						"node:" + node.getNodeId() + "  action:" + action.getName());
				CreateSequenceFormVariablesAndConstraints(action.getChildId(), parentVariable, v);
			}
		}
	}

	@Override
	public void solveGame() {
		// TODO Auto-generated method stub

	}

	@Override
	public void printStrategyVarsAndGameValue() {
		// TODO Auto-generated method stub

	}

	@Override
	public void printGameValue() {
		// TODO Auto-generated method stub

	}

	@Override
	public double[][][] getStrategyProfile() {
		// TODO Auto-generated method stub
		return null;
	}

}
