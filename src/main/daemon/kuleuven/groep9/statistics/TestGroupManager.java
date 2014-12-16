package kuleuven.groep9.statistics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The StatisticCollector that belongs to the FrequencyStatistic.
 * 
 * The class is responsible of managing the FailGroup objects.
 * FailGroup has 2 special instances, namely successGroup and 
 * unknownGroup, respectively containing tests that have not yet
 * been run and have run successfully. These 2 special groups are
 * kept in 2 attributes, while regular FailGroup are kept in a map
 * with the failMethod their members have in common as the key.
 */
public class TestGroupManager{

	private TestGroup successGroup;
	private TestGroup unknownGroup;
	private Map<String,TestGroup> map;
	
	public TestGroupManager(){
		setMap(new HashMap<String,TestGroup>());
	}
	
	/**
	 * Create a new FailGroupStatistic corresponding to the given test.
	 * 
	 * @param	test
	 * 			The test that the statistic should refer to.
	 */
	public TestGroup getUnknownGroup() {
		if(unknownGroup == null){
			unknownGroup = new TestGroup(this, "Unknown"){
				/**
				 * The unknownGroup has no representative.
				 */
				@Override
				protected void setRepresentative(TestGroupStatistic representative){}
			};
		}
		return unknownGroup;
	}
	
	public TestGroup getSuccessGroup(){
		if(successGroup == null){
			successGroup = new TestGroup(this, "Success"){
				/**
				 * The successGroup has no representative.
				 */
				@Override
				protected void setRepresentative(TestGroupStatistic representative){}
			};
		}
		return successGroup;
	}

	/**
	 * Delete the given FailGroup from the Map. This method will have
	 * no effect for successGroup or unknownGroup, as they are not
	 * meant to be deleted.
	 * 
	 * @param 	failGroup
	 * 			The group to be deleted.
	 */
	protected void removeGroup(TestGroup failGroup) {
		getMap().remove(failGroup.getName());
	}

	protected Map<String,TestGroup> getMap() {
		return map;
	}

	protected void setMap(Map<String,TestGroup> map) {
		this.map = map;
	}
	
	public Collection<TestGroup> getGroups(){
		return getMap().values();
	}
	
	/**
	 * Return the group that belongs to the given name (the name represents
	 * the failMethod). If the group doesn't exist yet, it first has to be
	 * created and added to the map. 
	 */
	public TestGroup getGroupByName(String name){
		if(getMap().containsKey(name)){
			return getMap().get(name);
		}else{
			TestGroup group = new TestGroup(this,name);
			getMap().put(name, group);
			return group;
		}
	}

}
