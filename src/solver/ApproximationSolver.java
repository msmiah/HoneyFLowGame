package solver;

import game.Game;
import game.Game.Action;
import game.Game.Node;

public class ApproximationSolver extends ZeroSumGameSolver {

	public ApproximationSolver(Game game) {
		super(game);
		CreateSequenceFormVariablesAndConstraints(0);

	}

	private void CreateSequenceFormVariablesAndConstraints(int currentNodeId) {
		Node node = game.getNodeById(currentNodeId);
		if (null == node || node.isLeaf()) {
			return;
		}

		for (Action action : node.getActions()) {
			if (node.getPlayer() == 1) {
				CreateSequenceFormVariablesAndConstraints(action.getChildId());

			} else if (node.getPlayer() == 2) {
				CreateSequenceFormVariablesAndConstraints(action.getChildId());
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
