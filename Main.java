/* The Main class runs the simulation, first loading data for districts (by reading in from an Excel file), then 
 * generating semi-randomized data for individuals. It then allows users to produce floods of specific magnitudes in
 * any of the three rivers, simulates the decision of whether or not to move for each individual, and returns 
 * updated population data.
 */

import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class Main {
	
	static ArrayList<District> districts = new ArrayList<District>(); //Will contain all 64 districts
	static ArrayList<Person> people = new ArrayList<Person>(); //Will contain all 144,043,697 individuals
	static int num_districts = 0;
	static int total_pop = 0;
	static double max_flood_depth = 500;
	static double average_dist = 0;
	
	// Returns a random integer within specified range (including start and end points) 
	public static int randomWithRange(int min, int max)
	{
	   int range = (max - min) + 1;     
	   return (int)(Math.random() * range) + min;
	}
	
	// Returns Pythagorean distance between two districts
	public static double distBetween(District t1, District t2) {
		return Math.sqrt((Math.pow(Math.abs(t1.lat - t2.lat), 2) + Math.pow(Math.abs(t1.lon - t2.lon), 2)));
	}
	
	// Returns the average distance between all the districts in the districts list
	public static double avgDist() {
		double sum_distances = 0;
		int num_distances = 0;
		for (int i = 0; i < num_districts-1; i++) {
			for (int j = i+1; j < num_districts; j++) {
				sum_distances += distBetween(districts.get(i), districts.get(j));
				num_distances++;
			}
		}
		return sum_distances / num_distances;
	}
	
	/* Prints out population of each district */
	public static void printpop() {
		System.out.println("Population Data: ");
		for (int i = 0; i < num_districts; i++) {
			System.out.println(districts.get(i).name + " has population " + districts.get(i).population);
		}
	}
	
	/* Generates starter data for districts and individuals. Data for districts are read in from an excel file. Data
	 * for individuals are then produced based on the population in each district. Characteristics for individuals--
	 * age, marital status, wealth, property-owning status, and employment status--are produced semi-randomly based on
	 * Bangladesh census data.
	 */
	public static void getdata() {
		//Get districts data by reading in from CSV file
		System.out.println("Loading geographic data...");
		String csvFile = "/Users/emily/desktop/district_data.csv";
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";
        try {
            br = new BufferedReader(new FileReader(csvFile));
            while ((line = br.readLine()) != null) {
                String[] district = line.split(cvsSplitBy);
                String name = district[0];
                int pop = Integer.parseInt(district[1]);
                double lat = Double.parseDouble(district[2]);
                double lon = Double.parseDouble(district[3]);
                int altitude = Integer.parseInt(district[4]);
                int jamuna = Integer.parseInt(district[5]);
                int ganges = Integer.parseInt(district[6]);
                int meghna = Integer.parseInt(district[7]);
                District new_district = new District(name, altitude, pop, lat, lon, jamuna, ganges, meghna);
                districts.add(new_district);
            }
        } 
        catch (FileNotFoundException e) {
            e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        } 
        finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        num_districts = districts.size();
        average_dist = avgDist();
        
        //Calculate the total population for global variable
        for (int i = 0; i < num_districts; i++) {
        	total_pop += districts.get(i).population;
        }
       
        //Get people data by semi-random production
        System.out.println("Loading population data...");
        //Variables that will determine the distribution of characteristics
        int min_age = 0;
        int max_age = 60;
        int min_age_employment = 10;
        int min_age_marriage = 10;
        int min_age_property = 10;
        double prob_employed = 0.5;
        double prob_married = 0.5;
        double prob_property = 0.5;
        //Calculate 5 income "buckets"; 1/5 of the people will be allocated to each of the buckets
        double gdp_per_cap = 957.82; // Data from World Bank
        double[] income_inequality = {.0522, .0910, .1333, .2056, .5179}; //Data from XX
        double[] incomes_by_quintile = new double[5];
        for(int i = 0; i < 5; i++) {
        	incomes_by_quintile[i] = (gdp_per_cap*total_pop*income_inequality[i]) / (total_pop/5); 
        }
        int wealth_variance = 100; // The amount wealth is allowed to vary within each quintile--should be no more than 200
        for (int j = 0; j < num_districts; j++) {
        	for (int k=0; k < districts.get(j).population; k++) {
        		int age = randomWithRange(min_age, max_age); //Age is uniformly distributed between min and max
        		int wealth_quintile = randomWithRange(0, 4);
        		double wealth = incomes_by_quintile[wealth_quintile] + randomWithRange(-wealth_variance, wealth_variance);
        		boolean property = false;
        		boolean married = false;
        		boolean employed = false;
        		//Property-owning status is based on probability of owning property (unless under min age)
        		double property_indicator = randomWithRange(0, 100);
            	if (age < min_age_property) {
            		property = false;
            	}
            	else if (property_indicator <= prob_property * 100.0) {
            		property = true;
            	}
            	else {
            		property = false;
            	}
            	//Marriage status is calculated based on probability of being married (unless under min age)
            	double married_indicator = randomWithRange(0, 100);
            	if (age < min_age_marriage) {
            		property = false;
            	}
            	else if (married_indicator <= prob_married * 100.0) {
            		married = true;
            	}
            	else {
            		married = false;
            	}
            	//Employment status is calculated based on probability of being employed (unless under min age)
            	double employed_indicator = randomWithRange(0, 100);
            	if (age < min_age_employment) {
            		employed = false;
            	}
            	if (employed_indicator <= prob_employed * 100.0) {
            		employed = true;
            	}
            	else {
            		employed = false;
            	}
            	people.add(new Person(districts.get(j), age, wealth, property, married, employed));
        	}
        	System.out.println("        Loaded data for " + districts.get(j).name);
        }
        System.out.println("Data loaded");
	}
	
	/* The flood function accepts a river, which can be any of the three major rivers in Bangladesh, and a severity
	 * in cm, which must be less than the max severity specified in the global variables. It then calculates,
	 * for each individual, whether or not they will move and where they will move to. It then updates population 
	 * and individual information accordingly.
	 */
	
	public static void flood(String river, double severity) {
		if (river != "jamuna" && river != "ganges" && river != "meghna") {
			System.out.println("invalid river");
			return;
		}
		else if (severity > max_flood_depth) {
			System.out.println("flood depth too high, not possible");
			return;
		}
		System.out.println("FLOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOD");
		ArrayList<District> affected_districts = new ArrayList<District>();
		ArrayList<District> secondarily_affected = new ArrayList<District>();
		ArrayList<Person> will_move = new ArrayList<Person>();
		ArrayList<Integer> moving_indexes = new ArrayList<Integer>();
		
		// Go through list of towns, finding which towns are directly affected by flood (that is, adjacent to the river)
		if (river == "jamuna") {
			for (int i = 0; i < num_districts; i++) {
				if (districts.get(i).jamuna == 1) {
					affected_districts.add(districts.get(i));
					System.out.println(districts.get(i).name + " is directly affected");
				}
			}
		}
		else if (river == "ganges") {
			for (int i = 0; i < num_districts; i++) {
				if (districts.get(i).jamuna == 1) {
					affected_districts.add(districts.get(i));
					System.out.println(districts.get(i).name + " is directly affected");
				}
			}
		}
		else if (river == "meghna") {
			for (int i = 0; i < num_districts; i++) {
				if (districts.get(i).jamuna == 1) {
					affected_districts.add(districts.get(i));
					System.out.println(districts.get(i).name + " is directly affected");
				}
			}
		}

		//run through districts, find all districts close enough to also be affected
		int num_directly_affected = affected_districts.size();
		for (int i = 0; i < num_districts; i++) {
			if (!affected_districts.contains(districts.get(i))) {
				for (int j = 0; j < num_directly_affected; j++) {
					if (distBetween(districts.get(i), districts.get(j)) < avgDist()/6 && districts.get(i).elevation < 33) {
						affected_districts.add(districts.get(i));
						secondarily_affected.add(districts.get(i));
						System.out.println(districts.get(i).name + " is secondarily affected");
						break;
					}
				}
			}
		}
		
		/* run through people, find all people who are in affected districts. Calculate the probability they will move
		using a point system. Create a list of all those who are moving. */
		System.out.println("Finding people who are moving...");
		for (int j = 0; j < total_pop; j++) {
			Person affected_person = people.get(j);
			if (affected_districts.contains(affected_person.loc)) {
				int prob_move = 10;
				//Being married makes people less likely to move than if they are single
				if (affected_person.married) {
					prob_move = prob_move - 3;
				}
				else {
					prob_move = prob_move + 1;
				}
				//Employed people are less likely to move than unemployed ones, because moving will mean losing their job
				if (affected_person.employed) {
					prob_move = prob_move - 6; 
				}
				else {
					prob_move = prob_move + 2;
				}
				//Owning property makes a person less likely to move, because moving will mean losing their property
				if (affected_person.property) {
					prob_move = prob_move - 6;
				}
				else {
					prob_move = prob_move + 2;
				}
				//If a person is between 40 and 50, they are slightly less likely to move because they have family, etc.
				if (affected_person.age > 40 && affected_person.age < 50) {
					prob_move = prob_move - 3;
				}
				//If a person is between 50 and 60, they are much less likely to move
				else if (affected_person.age > 50 || affected_person.age < 10) {
					prob_move = prob_move - 5;
				}
				else {
					prob_move = prob_move + 2;
				}
				//Wealthy people are more able to move than poor ones. However, extremely wealthy people are less 
				//likely to move because they can just fix everything and go on with their lives.
				if (affected_person.wealth < 200) {
					prob_move = prob_move - 10;
				}
				else if (affected_person.wealth < 400) {
					prob_move = prob_move - 5;
				}
				else if (affected_person.wealth < 600) {
					prob_move = prob_move - 1;
				}
				else if (affected_person.wealth > 2000) {
					prob_move = prob_move - 2;
				}
				else if (affected_person.wealth < 1000) {
					prob_move = prob_move + 5;
				}
				else {
					prob_move = prob_move + 1;
				}
				//People in secondarily affected regions are less likely to move than those in directly affected regions
				if ((affected_person.loc.ganges == 0 && river == "ganges") || (affected_person.loc.meghna == 0 && river == "meghna") || (affected_person.loc.jamuna == 0 && river == "jamuna")) {
					prob_move = prob_move - 5;
				} 
				//If the flood isn't too severe, people are less likely to move
				if (severity < 300) {
					prob_move = prob_move - 5;
				}
				//Add some randomness to represent other factors
				prob_move = prob_move + randomWithRange(-20, 10);
				//If the probability is now greater than 19, the person will move
				if (prob_move >= 19) {
					//When the person moves, they lose $500 (or all their money if they have less than that in total)
					affected_person.wealth -= 500;
					people.get(j).wealth -= 500;
					if (affected_person.wealth < 0) {
						affected_person.wealth = 0;
						people.get(j).wealth = 0;
					}
					//When a person moves, they lose their employment
					affected_person.employed = false;
					//When a person moves, they lose their property
					affected_person.property = false;
					will_move.add(affected_person);
					moving_indexes.add(j);
				}
			}
		}
		int num_moving = will_move.size();
		
		//Calculate where each person will move to
		System.out.println("Calculating where they will move to...");
		int[] movements = new int[num_districts]; //Keeps track of how many people are moving to each place
		for (int d = 0; d < num_districts; d++) {
			movements[d] = 0;
		}
		for (int k = 0; k < num_moving; k++) {
			Person moving_person = will_move.get(k);
			//For each person, score each district based on altitude, distance from original district, and population.
			//Pick the highest scoring district to move to. 
			int[] scores = new int[num_districts];
			for (int a = 0; a < scores.length; a++) {
				scores[a] = 0;
			}
			for (int b = 0; b < num_districts; b++) {
				//If the district is affected by a flood, it is less likely to be chosen
				if (affected_districts.contains(districts.get(b)) && !(secondarily_affected.contains(districts.get(b)))) {
					scores[b] = -18;
				}
				//If the district is at high elevation, it is more likely to be chosen
				if (districts.get(b).elevation > 20) {
					scores[b] += 5;
				}
				//If the district is has a large population, it is more likely to be chosen (urbanization)
				if (districts.get(b).population > 1000000) {
					scores[b] += 5;
				}
				//If the district has an even larger population, it is more likely to be chosen (urbanization)
				if (districts.get(b).population > 10000000) {
					scores[b] += 10;
				}
				//Districts that are close by are more likely to be chosen
				if (distBetween(districts.get(b), moving_person.loc) < average_dist / 4) {
					scores[b] += 15;
				}
				else if (distBetween(districts.get(b), moving_person.loc) < average_dist / 2) {
					scores[b] += 7;
				}  
				//Add in some randomness to represent variance
				scores[b] += randomWithRange(-15, 15); 		
			}
			
			int max_score = -18;
			int max_index = -1;
			for (int c = 0; c < scores.length; c++) {
				if (scores[c] >= max_score) {
					max_score = scores[c];
					max_index = c;
				}
			}
			movements[max_index]++;
			
			//remove 1 person from current locations population
			for (int f = 0; f < num_districts; f++) {
				if (districts.get(f).equals(will_move.get(k).loc)) {
					districts.get(f).population--;
				}
			}
			//update person's location
			people.get(moving_indexes.get(k)).loc = districts.get(max_index);
			//add 1 person to their new locations population
			for (int f = 0; f < num_districts; f++) {
				if (districts.get(f).equals(will_move.get(k).loc)) {
					districts.get(f).population++;
				}
			}
		}
		
		//print out movements
		for (int e = 0; e < movements.length; e++) {
			System.out.println(movements[e] + " moved to " + districts.get(e).name);
		}
		
		//Print new population data
		printpop();
	}
	
	//Runs the simulation, first getting the data, then running a flood
	public static void main(String [] args) {
		getdata();
		//printpop();
		//Induce flood
		flood("ganges", 400);
	}
}
