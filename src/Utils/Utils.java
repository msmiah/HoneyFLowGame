package Utils;

public class Utils {
	public static final int defender = 1;
	public static final int attacker= 2;
	public static final int TOTAL_VULNERABILITY_TYPE = 2;
	public static final int TOTAL_HONEYFLOW_TYEP = 2;
	public static final int TOTAL_REAL_HOST_NO = 3;
	public static final int TOTAL_NON_EXISTING_HOST_OR_HONEYPOT_NO = 2;
	public static final int All_HOST = TOTAL_REAL_HOST_NO + TOTAL_NON_EXISTING_HOST_OR_HONEYPOT_NO;
	public static final int TOTAL_DEFENDER_ACTION_NO = All_HOST;
	public static final int TOTAL_ATTACKER_ACTION_NO = TOTAL_VULNERABILITY_TYPE + 1;
	public static final int NO_VULNERABILITY = 0;
	public static final int TYPE1_VULNERABILITY = 1;
	public static final int TYPE2_VULNERABILITY = 2;
	public static final int ATTACK_AS_NO_ATTACK = 0;
	public static final int ATTACK_AS_TYPE1_VULNERABILITY = 1;
	public static final int ATTACK_AS_TYPE2_VULNERABILITY = 2;
	public static final int NORMAL_FLOW = 0;
	public static final int HONEY_FLOW_VULNERABILITY_TYPE1 = 1;
	public static final int  HONEY_FLOW_VULNERABILITY_TYPE2 = 2;
	public static final int  MAX_LIMIT_OF_HONEY_FLOW = 10;

}
