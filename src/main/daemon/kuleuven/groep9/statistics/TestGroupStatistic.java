package kuleuven.groep9.statistics;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;

/**
 * Class representing a Statistic that contains information
 * about a failure. The method that causes the failure
 * needs to be saved, therefore this class makes use of 
 * the OSSrewriter. A FailGroupStatistic is a member of a
 * FailGroup and has a reference to the test it belongs to.
 */
public class TestGroupStatistic extends Statistic{
	
//	private static TestGroupManager testGroupManager;
	private final TestGroupManager testGroupManager;
	private TestGroup group;

//	public TestGroupStatistic(Description descr){
	public TestGroupStatistic(Description descr, TestGroupManager manager){
		super(descr);
		this.testGroupManager = manager;
		clearGroup();
	}

	public TestGroup getGroup() {
		return group;
	}

	protected void setGroup(TestGroup group) {
		this.group = group;
	}
	
	/**
	 * In case of a failure, the monitor can stop catching
	 * the methods. The method is removed from the unknownGroup
	 * and added to the correct FailGroup.
	 */
	@Override
	public void testFailure(Failure failure){
		StackTraceElement trace = failure.getException().getStackTrace()[0];
		TestGroup group = getTestGroupManager().getGroupByName(trace.toString());
		addToGroup(group);
	}
	
	/**
	 * When a test is finished and has succeeded, it can
	 * be put into the successGroup.
	 */
	@Override
	public void testFinished() {
		if(getGroup().equals(getTestGroupManager().getUnknownGroup())){
			TestGroup group = getTestGroupManager().getSuccessGroup();
			addToGroup(group);
		}
	}

	/**
	 * Checks whether the corresponding test is the
	 * representative test of this group.
	 * 
	 * @return	representative
	 */
	public boolean isRepresentative() {
		return getGroup().isRepresentative(this);
	}

	/**
	 * This method removes the test from its current group
	 * and puts it into the unknownGroup.
	 */
	protected void clearGroup() {
		setGroup(getTestGroupManager().getUnknownGroup());
		getTestGroupManager().getUnknownGroup().add(this);
	}
	
	/**
	 * This method adds a method to a new, given group.
	 * It also removes the method from its old group.
	 * 
	 * @param 	group
	 * 			The new group for this object.
	 */
	protected void addToGroup(TestGroup group){
		TestGroup oldGroup = getGroup();
		oldGroup.removeMember(this);
		group.add(this);
		setGroup(group);
	}
	
	public TestGroupManager getTestGroupManager(){
		return testGroupManager;
	}
	
	@Override
	public Statistic clone(Description descr){
		return new TestGroupStatistic(descr,getTestGroupManager());
	}
	
	@Override
	public int compareTo(Statistic o) {
		TestGroupStatistic other = (TestGroupStatistic)o;
		
		boolean f1 = this.isRepresentative();
		boolean f2 = other.isRepresentative();
		return -Boolean.compare(f1, f2);
	}
}
