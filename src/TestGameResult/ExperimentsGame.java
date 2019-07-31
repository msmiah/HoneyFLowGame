package TestGameResult;

import game.Game;
import solver.ApproximationSolver;

public class ExperimentsGame {
   public static void main(String[] args) {
	   
	  // System.out.println("HoneyFlow");
	   Game g = new Game();
	   ApproximationSolver solver = new ApproximationSolver(g);
   }
}
