package com.gmail.scyntrus.tmob;

import java.lang.reflect.Field;
import java.util.Map;

import net.minecraft.server.v1_7_R4.EntityTypes;
import net.minecraft.server.v1_7_R4.Navigation;
import net.minecraft.server.v1_7_R4.PathfinderGoalSelector;

public class ReflectionManager {
	public static Field navigationE = null;
	public static Field pathfinderGoalSelectorB = null;
	
	public static boolean goodNavigationE = false;
	public static boolean goodPathfinderGoalSelectorB = false;

	@SuppressWarnings("rawtypes")
	public static Map mapC;
	@SuppressWarnings("rawtypes")
	public static Map mapD;
	@SuppressWarnings("rawtypes")
	public static Map mapF;
	@SuppressWarnings("rawtypes")
	public static Map mapG;
	
	@SuppressWarnings("rawtypes")
	public static boolean init() {
		try {
	    	Field fieldC = EntityTypes.class.getDeclaredField("c"); //TODO: Update name on version change
	        fieldC.setAccessible(true);
	    	Field fieldD = EntityTypes.class.getDeclaredField("d");
	        fieldD.setAccessible(true);
	    	Field fieldF = EntityTypes.class.getDeclaredField("f");
	        fieldF.setAccessible(true);
	    	Field fieldG = EntityTypes.class.getDeclaredField("g");
	        fieldG.setAccessible(true);
	        mapC = (Map) fieldC.get(null);
	        mapD = (Map) fieldD.get(null);
	        mapF = (Map) fieldF.get(null);
	        mapG = (Map) fieldG.get(null);
		} catch (Exception e1) {
			try {
		    	Field fieldC = EntityTypes.class.getDeclaredField("field_75625_b");
		        fieldC.setAccessible(true);
		    	Field fieldD = EntityTypes.class.getDeclaredField("field_75626_c");
		        fieldD.setAccessible(true);
		    	Field fieldF = EntityTypes.class.getDeclaredField("field_75624_e");
		        fieldF.setAccessible(true);
		    	Field fieldG = EntityTypes.class.getDeclaredField("field_75622_f");
		        fieldG.setAccessible(true);
		        mapC = (Map) fieldC.get(null);
		        mapD = (Map) fieldD.get(null);
		        mapF = (Map) fieldF.get(null);
		        mapG = (Map) fieldG.get(null);
			} catch (Exception e2) {
	    	    e1.printStackTrace();
	    	    e2.printStackTrace();
				return false;
			}
		}
		try {
			navigationE = Navigation.class.getDeclaredField("e"); //TODO: Update name on version change
			navigationE.setAccessible(true);
			goodNavigationE = true;
		} catch (Exception e1) {
			try {
				navigationE = Navigation.class.getDeclaredField("field_75512_e");
				navigationE.setAccessible(true);
				goodNavigationE = true;
			} catch (Exception e2) {
				System.out.println("[TownyMobs] [Minor Error] Field not found: Navigation.e; Custom pathfinding distances cannot be set");
    	    	e1.printStackTrace();
    	    	e2.printStackTrace();
			}
		}
		try {
			pathfinderGoalSelectorB = PathfinderGoalSelector.class.getDeclaredField("b"); //TODO: Update name on version change
			pathfinderGoalSelectorB.setAccessible(true);
			goodPathfinderGoalSelectorB = true;
		} catch (Exception e1) {
			try {
				pathfinderGoalSelectorB = PathfinderGoalSelector.class.getDeclaredField("field_75782_a");
				pathfinderGoalSelectorB.setAccessible(true);
				goodPathfinderGoalSelectorB = true;
			} catch (Exception e2) {
				System.out.println("[TownyMobs] [Minor Error] Field not found: PathfinderGoalSelector.a; Unable to override mob goals");
    	    	e1.printStackTrace();
    	    	e2.printStackTrace();
			}
		}
		return true;
	}
}
