package kuleuven.groep9.statistics;

import kuleuven.groep9.runner.RecurringTest;

import java.util.ArrayList;
import java.util.List;

import org.junit.runner.Description;

/**
 * Represents a group of tests that fail because of the same method.
 * There are 2 exceptions, i.e. successGroup contains the succeeded
 * tests and unknownGroup contains the tests that have never been run.
 * A group is characterized by a representative and a name, the 
 * 2 exception groups have no representative. Groups are managed by a
 * FailGroupManager.
 */
public class TestGroup {
	
	/**
	 * Name is usually the method that causes the failure.
	 * In case of success, it will be "success".
	 * In case the tests have not yet been run, it will be "unknown".
	 */
	private final String name;
	private List<TestGroupStatistic> members;
	private TestGroupStatistic representative;
	private final TestGroupManager manager;
	
	public TestGroup(TestGroupManager manager, String name){
		this.manager = manager;
		this.name = name;
		this.members = new ArrayList<TestGroupStatistic>();
		setRepresentative(null);
	}
	
	public TestGroupStatistic getRepresentative() {
		return representative;
	}

	protected void setRepresentative(TestGroupStatistic representative) {
		this.representative = representative;
	}
	
	public String getName() {
		return name;
	}

	public TestGroupManager getManager() {
		return manager;
	}

	/**
	 * Checks whether the given test is the representative of this group.
	 * 
	 * @param 	member
	 * 			The given test
	 */
	public boolean isRepresentative(TestGroupStatistic member){
		return member == getRepresentative();
	}

	/**
	 * Select the first test in the list as the new representative 
	 * for this group.
	 */
	public void reelect() {
		setRepresentative(getMembers().get(0));
	}

	/**
	 * Checks whether the group contains members.
	 */
	public boolean isEmpty() {
		return getMembers().isEmpty();
	}

	/**
	 * Add the given test to this group.
	 * 
	 * @param 	test
	 * 			The test to be added.
	 */
	protected void add(TestGroupStatistic test) {
		members.add(test);
		reelect();
	}

	/**
	 * Remove the given test from this group.
	 * 
	 * @param 	member
	 * 			The test to be removed.
	 */
	protected void removeMember(TestGroupStatistic member){
		getMembers().remove(member);
		member.clearGroup();
		
		if(isEmpty()){
			getManager().removeGroup(this);
			return;
		}
		
		if(isRepresentative(member)){
			reelect();
		}
	}
	
	public List<TestGroupStatistic> getMembers() {
		return members;
	}

	protected void setMembers(List<TestGroupStatistic> members) {
		this.members = members;
	}

}
