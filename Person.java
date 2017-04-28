/* A person represents an individual in the Kingdom of Bangladesh. A person has certain characteristics that do not
 * change over time--their age and their marriage status. A person's location will change if they move after a flood,
 * and, if they do move, they will lose $500 in wealth, lose their property (if they are property-owning), and lose
 * their employment (if they are employed). 
 */

public class Person {
	public District loc;
	public int age;
	public double wealth;
	public boolean property;
	public boolean married;
	public boolean employed;
	
	public Person(District loc, int age, double wealth, boolean property, boolean married, boolean employed) {
	    this.loc = loc;
	    this.age = age;
	    this.wealth = wealth;
	    this.property = property;
	    this.married = married;
	    this.employed = employed;
	}
}
