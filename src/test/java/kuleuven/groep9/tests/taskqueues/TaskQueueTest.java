package kuleuven.groep9.tests.taskqueues;

import java.util.Date;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

import kuleuven.groep9.taskqueues.Task;
import kuleuven.groep9.taskqueues.TaskQueue;
import kuleuven.groep9.taskqueues.Worker;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TaskQueueTest {
	private TaskQueue<IDCombinableTask> tq;
	private String fruit;
	
	@Before
	public void setUpQueue() {
		System.out.println("testthread is going to make taskqueue");
		tq = new TaskQueue<IDCombinableTask>();
		System.out.println("testthread made taskqueue");
		fruit = null;
		System.out.println("testthread is going to make worker");
		new Worker<IDCombinableTask>(tq) {

			@Override
			protected void work(IDCombinableTask task) {
				task.execute();
			}
		}.start();
		System.out.println("testthread made worker");
	}
	
	@Test
	public void testFirstElm() throws InterruptedException {
		System.out.println("testthread starting testfirstElm test");
		tq.add(new Pine(1, 100));
		System.out.println("testthread is going to sleep");
		Thread.sleep(200);
		System.out.println("testthread awakes");
		assertEquals("pine", this.fruit);
	}
	
	@Test
	public void testDelayedFirstElm() throws InterruptedException {
		System.out.println("testthread starting testdelayedfirstelm test");
		tq.add(new Pine(1, 100));
		Thread.sleep(10);
		assertEquals(null, this.fruit);
		Thread.sleep(100);
		assertEquals("pine", this.fruit);
	}
	
	@Test
	public void testCombinedElm() throws InterruptedException {
		System.out.println("testthread starting testcombinedelm test");
		tq.add(new Pine(1, 100));
		tq.add(new Apple(1, 50));
		Thread.sleep(150);
		assertEquals("pineapple", this.fruit);
	}
	
	protected abstract class IDCombinableTask extends Task<IDCombinableTask> {
		private int id;
		private final Date deadline;
		
		public IDCombinableTask(int id, long timeToCombineMillis) {
			this.id = id;
			this.deadline = new Date((new Date()).getTime() + timeToCombineMillis);
		}
		
		@Override
		public boolean canCombine(IDCombinableTask other) {
			if (other == null)
				return false;
			return (other.getId() == this.getId());
		}

		protected int getId() {
			return this.id;
		}
		
		@Override
		public long getDelay(TimeUnit unit) {
			long delay = unit.convert((getDeadline().getTime() -  (new Date()).getTime()), TimeUnit.MILLISECONDS);
			System.out.println("The delay for this elm is " + delay);
			return delay;
		}

		@Override
		public int compareTo(Delayed o) {
			return (int) (o.getDelay(TimeUnit.MILLISECONDS) - this.getDelay(TimeUnit.MILLISECONDS));
		}

		protected Date getDeadline() {
			return deadline;
		}
	}
	
	protected class Pine extends IDCombinableTask {

		public Pine(int id, long timeToCombineMillis) {
			super(id, timeToCombineMillis);
		}

		@Override
		public IDCombinableTask combine(IDCombinableTask other) {
			if (! canCombine(other))
				throw new IllegalArgumentException("The two given tasks cannot combine.");
			if (other instanceof Pine)
				return this;
			return other.combine(this);
		}

		@Override
		public void execute() {
			TaskQueueTest.this.fruit = "pine";
			System.out.println("setting fruit to pine");
		}
	}
	
	protected class Apple extends IDCombinableTask {

		public Apple(int id, long timeToCombineMillis) {
			super(id, timeToCombineMillis);
		}

		@Override
		public IDCombinableTask combine(IDCombinableTask other) {
			if (! canCombine(other))
				throw new IllegalArgumentException("The two given tasks cannot combine.");
			if (other instanceof Pine)
				return new Pineapple(this.getId(), this.getDelay(TimeUnit.MILLISECONDS));
			if (other instanceof Apple)
				return this;
			return other.combine(this);
		}

		@Override
		public void execute() {
			TaskQueueTest.this.fruit = "apple";
			System.out.println("setting fruit to apple");
		}
	}
	
	protected class Pineapple extends IDCombinableTask {

		public Pineapple(int id, long timeToCombineMillis) {
			super(id, timeToCombineMillis);
		}

		@Override
		public IDCombinableTask combine(IDCombinableTask other) {
			if (! canCombine(other))
				throw new IllegalArgumentException("The two given tasks cannot combine.");
			if (other instanceof Pine)
				return this;
			if (other instanceof Apple)
				return this;
			if (other instanceof Pineapple)
				return this;
			return other.combine(this);
		}

		@Override
		public void execute() {
			TaskQueueTest.this.fruit = "pineapple";
			System.out.println("setting fruit to pineapple");
		}
	}
}


