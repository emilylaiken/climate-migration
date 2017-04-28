/* A district represents one of the 64 districts of Bangladesh. A district has a name, an elevation (which is 
 * the elevation of the police station in that district), a location based on latitude and longitude (again, based on 
 * the location of the police station), a population (based on data from XX), and three boolean variables representing
 * whether it is adjacent to the Jamuna, Ganges, or Meghna rivers. Only the population of the district is updated
 * throughout the simulation.
 */

public class District {
	public String name;
	public int elevation;
	public int population;
	public double lat;
	public double lon;
	public int jamuna;
	public int ganges;
	public int meghna;
	
	public District(String name, int elevation, int population, double lat, double lon, int jamuna, int ganges, int meghna) {
		this.name = name;
		this.elevation = elevation;
		this.population = population;
		this.lat = lat;
		this.lon = lon;
		this.jamuna = jamuna;
		this.ganges = ganges;
		this.meghna = meghna;
	}
	
	public boolean equals(District other){
		return (this.name == other.name);
	}
	
}
