package game;

import Utils.Utils;

public class Game {

	int player1 = Utils.defender;
	int player2 = Utils.attacker;
	public int[][] informationSets;
	public int nodeCount;
	public int[] valueOfHosts;
	public int[] defenderActions = { Utils.NORMAL_FLOW, Utils.NORMAL_FLOW, Utils.NORMAL_FLOW,
			Utils.HONEY_FLOW_VULNERABILITY_TYPE1, Utils.HONEY_FLOW_VULNERABILITY_TYPE2 };
	public boolean[] isRealHost = { true, true, true, false, false };
	public String[] defenderActionName = { "Normal-Flow", "Normal-Flow", "Normal-Flow", "Honey-Flow-T1",
			"Honey-Flow-T2" };
	public int[] attackerActions = { Utils.ATTACK_AS_NO_ATTACK, Utils.ATTACK_AS_TYPE1_VULNERABILITY,
			Utils.ATTACK_AS_TYPE2_VULNERABILITY };
	public String[] attackerActionName = { "No-attack", "Attack-As-V1", "Attack-as-V2" };
	public int[] vulerabilityOfHost = { Utils.NO_VULNERABILITY, Utils.TYPE1_VULNERABILITY,
			Utils.ATTACK_AS_TYPE2_VULNERABILITY, Utils.NO_VULNERABILITY, Utils.NO_VULNERABILITY };
	private Node[] nodes;

	public class Action {
		private String name;
		private int childId;

		public String getName() {
			return name;
		}

		public int getChildId() {
			return childId;
		}

		public boolean equals(Action action) {
			if (name.equals(action.name))
				return true;
			else
				return false;
		}

	}

	public class Node {
		private int nodeId;
		private String name;
		private int player;
		private int informationSet;
		private Action[] actions;
		private double value = 0;
		private boolean isRealAssest = false;

		public Node(int nodeId, String name, int player) {
			this.nodeId = nodeId;
			this.name = name;
			this.player = player;

		}

		public int getNodeId() {
			return nodeId;
		}

		public String getName() {
			return name;
		}

		public int getPlayer() {
			return player;
		}

		public int getInformationSet() {
			return informationSet;
		}

		public Action[] getActions() {
			return actions;
		}

		public double getValue() {
			return value;
		}

		public boolean getIsReal() {
			return isRealAssest;
		}

		public boolean isLeaf() {
			return player == -2;
		}
	}

	public Game() {
		nodes = new Node[100];
		nodeCount = 0;
		valueOfHosts = new int[Utils.All_HOST];
		informationSets = new int[2][];
		init();

	}

	private void init() {
		valueOfHosts[0] = 100; // no-vulnerability (H1)
		valueOfHosts[1] = 50; // host with vulnerability type V1
		valueOfHosts[2] = 30; // host with vulnerability type v2
		valueOfHosts[3] = 10; // non-existing host sending honey flow with V1 vulnerability
		valueOfHosts[4] = 5; // non-existing host sending honey flow with V2 vulnerability
		createDefenderNode();

	}

	private void createDefenderNode() {

		Node dNode = new Node(nodeCount, "D", player1);
		nodes[nodeCount] = dNode;
		dNode.nodeId = nodeCount;
		dNode.actions = new Action[Utils.TOTAL_DEFENDER_ACTION_NO];
		for (int i = 0; i < dNode.actions.length; i++) {
			Action action = new Action();
			dNode.isRealAssest = isRealHost[i];
			action.name = defenderActionName[i];
			createAttackerNode(action, i);
			dNode.actions[i] = action;
			//System.out.println("child id :" + action.getChildId());
		}
	}

	private void createAttackerNode(Action parentAction, int utilityIndex) {

		Node aNode = new Node(++nodeCount, "A", player2);
		nodes[nodeCount] = aNode;
		parentAction.childId = nodeCount;
		aNode.actions = new Action[Utils.TOTAL_ATTACKER_ACTION_NO];
		for (int i = 0; i < aNode.actions.length; i++) {
			Action action = new Action();
			action.name = attackerActionName[i];
			createLeafNode(action, attackerActions[i], utilityIndex);
			aNode.actions[i] = action;
		}

	}

	private void createLeafNode(Action parentAction, int actionType, int index) {

		Node leafNode = new Node(++nodeCount, "T", -2);
		nodes[nodeCount] = leafNode;
		parentAction.childId = nodeCount;
		leafNode.nodeId = nodeCount;
		leafNode.actions = null;
		if (actionType == Utils.ATTACK_AS_NO_ATTACK)
			leafNode.value = 0;
		else if ((actionType != Utils.ATTACK_AS_NO_ATTACK && isRealHost[index] == false)
				|| (actionType != vulerabilityOfHost[index]))
			leafNode.value = valueOfHosts[index]; 
		else
			leafNode.value = -valueOfHosts[index];

	}

	public Node getNodeById(int currentNodeId) {
		return nodes[currentNodeId];
	}
}
