package solver;

import game.Game;

public abstract class ZeroSumGameSolver {
	

	int player1 = 1;
	int player2 = 2;
	int terminal = -2;
	double valueOfGame;
	double [] strategyVars;
	Game game;
	
	public ZeroSumGameSolver(Game game) {
		this.game = game;
	}
	
	public abstract void solveGame();
	
	public abstract void printStrategyVarsAndGameValue();
	public abstract void printGameValue();



	public double getValueOfGame() {
		return valueOfGame;
	}


	public double[] getStrategyVars() {
		return strategyVars;
	}

	public abstract double[][][] getStrategyProfile();


}
