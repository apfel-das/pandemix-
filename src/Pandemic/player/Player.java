package Pandemic.player;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;



import Pandemic.Gameboard.GameBoard;
import Pandemic.Gameboard.SimulatePandemic;
import Pandemic.cities.City;
import Pandemic.variables.Disease;
import Pandemic.variables.Piece;
import Pandemic.variables.Variables;
import Pandemic.actions.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class Player implements Cloneable{

  String playerName;
  int tactic;
  int AItries = 0;
  String playerRole;
  GameBoard pandemicBoard;
  Piece playerPiece;
  int playerAction,evalNum; //number of action per turn for each player
  boolean activePlayer;
  String colorDisc;
  float k;
  int decRound=0;
  ArrayList<City> cardsDiscarded;
  boolean myTurn = false; 
  //a hash-map to remember-track down movement of removal
  Map<City , Integer> cubesRemoved = new HashMap<City , Integer>();
   
  
  
  ArrayList<City>  hand ; //hand_cards maybe from action
  String[] possibleColour = {"Red","Blue","Yellow","Black"};
  ArrayList<Action> suggestions = new ArrayList<Action>();
  ArrayList<Float> MyUtilities = new ArrayList<Float>();
  ArrayList<Float> sugUtilities = new ArrayList<Float>();
  Float[] meanUt = new Float[3];
  
  
  /*
   * Constructor for objects of class Player
   */
  public Player(String pName,String pRole,int ev)
  {
      playerName = pName;
      hand = new ArrayList<City>();
      cardsDiscarded = new ArrayList<City>();
   
      playerAction = 4;
      tactic = 50;
      playerRole = pRole;
      this.evalNum = ev;
      this.k = 0;
     
  }

  public void setGameBoard(GameBoard currentBoard) { this.pandemicBoard = currentBoard;  }

  public void setPlayerPiece(Piece newPiece) { playerPiece = newPiece; }
  
  public ArrayList<Action> getSuggestions() { return suggestions; }

  public void setSuggestions(ArrayList<Action> suggestions) {this.suggestions = suggestions; }

  /*
  *draw @param numberOfCards  from playerPile and check if it is epidemic
  *and call @resolveEpidemic() or draw to  @hand()
  */
  public void drawCard(int numberOfCards, boolean test1)
  {
      // draws a card for the player from the board
      for (int i = 0; i<numberOfCards; i++)
      {
    	  if (test1==true)
    	  {
    		  // create instance of Random class 
    	      Random rand = new Random(); 
    	      int rand_int1 = rand.nextInt(pandemicBoard.playerPile.size()); 
    		  while(pandemicBoard.playerPile.get(rand_int1).equals(Variables.isEpidemic)) {
    			  rand_int1 = rand.nextInt(pandemicBoard.playerPile.size()); 
    			  Collections.shuffle(pandemicBoard.playerPile);
    		  }
    		//adds a new card to the players hand.
         	 hand.add((City) pandemicBoard.playerPile.get(rand_int1));
         	 pandemicBoard.playerPile.remove(rand_int1);//remove the card from PlayerDeck
         	 System.out.println(this.getPlayerName() + " draws a card");
    	  }
    	  else
    	  {
    		 if (pandemicBoard.playerPile.size()!= 0)
    		 {
	        	 //first element from array list PlayerPile
	              if (pandemicBoard.playerPile.get(0).equals(Variables.isEpidemic)) {            	  
	            	  System.out.println("-----EPIDEMIC DRAWN!-----");
	                  pandemicBoard.resolveEpidemic();//follow the steps for epidemic event
	                  pandemicBoard.playerPile.remove(0);
	                  break;
	              }
	              else 
	              {
	            	 //adds a new card to the players hand.
	            	 hand.add((City) pandemicBoard.playerPile.get(0));
	              }
	              pandemicBoard.playerPile.remove(0);//remove the card from PlayerDeck
	              System.out.println(this.getPlayerName() + " draws a card");
	          }
	          else
	          {
	              System.out.println("no more cards left");
	          }
    	  }
      }
  }


  /*
   * Count how many cards with a specific colour 
   * player has in his hands 
   * @param colour
   */
  public int getCountXCards(String colour)
  {
      int toReturn = 0;
      for (int i = 0 ; i < hand.size(); i++)
      {
          if (hand.get(i).getColour().equals(colour))
          {
              toReturn ++;
          }
      }
      return toReturn;
  }
  
  /*
   * remove a card from hand ,
   * then calls methods to put it in the discard pile()
   * and remove the card from the hand.
   */
  public void discardCard(String cardName)
  {
    int toDiscard =0;
	for (int i = 0; i < getHand().size(); i++)
	{
	   if (cardName.equals(getHand().get(i).getName())){
	    //System.out.println("found matching card in hand");
		toDiscard=i;   
	   }
	}
	if(myTurn == true) 
	{
		
		//just store the card, to revive later on
		cardsDiscarded.add(hand.get(toDiscard));
	}
	
	pandemicBoard.addPlayerCardDiscard(hand.get(toDiscard));//remove from playerDeck to put in PlayerDiscardDeck
    hand.remove(toDiscard);
    
  }
  
  
  // the exact opposite operation of discardCard
  public void reviveCard(String cardName) 
  {
	  int toGive =0;
	  City c = null;
	  
	  for (int i = 0; i<pandemicBoard.playerDiscardPile.size(); i++) 
	  {
		   c = (City) pandemicBoard.getPlayerDiscardPile().get(i);
		  if(cardName.equals(c.getName())) {
			  toGive = i;
		  }
	  }
	  
	  pandemicBoard.getPlayerDiscardPile().remove(toGive);
	  hand.add(c);
	 
	 
	  
	 
  }
  
  //discard all the cards needed for cure
  public void discardCureCards(String colour,int numberToDiscard)
  {
      for (int i = 0 ; i < numberToDiscard ; i ++)
      {
          for (int x = 0 ; x < hand.size(); x++ )
          {
              if (hand.get(x).getColour().equals(colour))
              {
                  discardCard(hand.get(x).getName());
                  break;
              }
          }
      }
  }
 
  //give back the cure cards you confiscated cards
  
  public void reviveCureCards(ArrayList<City> missing, int numberToRevive) 
  {
	  for(int i =0; i<numberToRevive; i++) 
	  {
		  reviveCard(missing.get(i).getName());
	  }
	  
  }
  
  // get PlayerAction 
  public int getPlayerAction(){ return playerAction; }
  
  // decrease the playerAction (Max=4)
  public void decreasePlayerAction(){ playerAction--;}

  // sets a players action back to 4
  public void resetPlayerAction() { playerAction=4;  }

  // return the name of player
  public String getPlayerName() { return playerName; }

  // Returns an array with the players cards in hand
  public ArrayList<City> getHand() { return hand;    }
  
  public String getPlayerRole() {return playerRole;	  }

  public void setPlayerRole(String playerRole) {this.playerRole = playerRole; }

  public Piece getPlayerPiece() { return playerPiece;	}    
  
  /**********************************************************************************
  *******These are the main (7+4) methods used to control the players actions********
  **********************************************************************************/
  
//Build research station as OPERATIONS_EXPERT

  
  //Build research station
  public boolean buildResearchStation ()
  {
	 if (playerAction>0) { 
	  buildResearchStation tmp = new buildResearchStation(playerPiece.getLocation(),getHand());
	  if (playerRole.equals("OPERATIONS_EXPERT") && !Variables.CITY_WITH_RESEARCH_STATION.contains(playerPiece.getLocation())) 
	  {
		  Variables.ADD_CITY_WITH_RESEARCH_STATION(playerPiece.getLocation());
          decreasePlayerAction();
          System.out.println("building a research station in " + playerPiece.getLocation().getName());
          suggestions.add(tmp);
          return true;
	  }
	  else
	  {
	      if (tmp.isaLegalMove())
	      {
	          discardCard(playerPiece.getLocation().getName());
	          Variables.ADD_CITY_WITH_RESEARCH_STATION(playerPiece.getLocation());
	          decreasePlayerAction();
	          //System.out.println("building a research station in " + playerPiece.getLocation().getName());
	          suggestions.add(tmp);
	          return true;
	      }
	  }
	 }
	 
      return false;
  }
  
  //Undo the Build of a research station
  public void undoBuildResearchStation(City c) 
  {
	  Variables.GET_CITIES_WITH_RESEARCH_STATION().remove(c);
  }
  
//Treat disease action
 public boolean treatDisease (City location, String colour)
 {
	if (playerAction>0) {  
	
		
	
		
	 treatDisease tmp = new treatDisease(location,colour);
	 
	 if (playerRole.equals("MEDIC")) {
	     if(tmp.isaLegalMove()==true && location == playerPiece.getLocation())
	     {
	    	//case we're playing, count how many will be removed to revive later on
	    	 if(myTurn == true ) 
             {
	    		 System.out.println("Attempting to treat, gotta remember this..");
            	 cubesRemoved.put(location, location.getCubeColour(colour));
             }
	    	 System.out.println("Removing all " + colour + " cube from " + location.getName());
	    	
	    	 // removing more than 1 cubes
	    	 while(location.getCubeColour(colour)!=0) {
	        	 location.removeCube(colour);
	             pandemicBoard.addCube(colour);//add in pool of cube
	             
	             
	             
	        }
	         decreasePlayerAction();
	         suggestions.add(tmp);
	         return true;
	     }
	 }
	 else {
		 if(tmp.isaLegalMove()==true && location == playerPiece.getLocation())
	     {
	         System.out.println("Removing a " + colour + " cube from " + location.getName());
	         
	         if(myTurn == true) 
	         {
	        	 cubesRemoved.put(location,1);
	         }
	         
	         location.removeCube(colour);
	         pandemicBoard.addCube(colour); //add in pool of cube
	         decreasePlayerAction();
	         suggestions.add(tmp);
	         return true;
	     }
	 }
	}
     return false;
 }
  //undo the treat disease action
  public void undoTreatDisease(City loc, String colour) 
  {
	  int cb =(Integer)cubesRemoved.get(loc);
	
  
	  if(playerRole.equals("MEDIC")) 
	  {
		  while(cb != 0) 
		  {
			  loc.addCube(colour); //re-add the cube in its respective location
			  pandemicBoard.removeCube(colour); //remove from pool pile 
			  cb--;
		  }
	  }
	  
	  else 
	  {
		  
		  loc.addCube(colour); //re-add the cube in its respective location
		  pandemicBoard.removeCube(colour); //remove from pool pile 
		  
	  }
	  
  }
  
  // Drive action,
  public boolean driveCity (City location, City destination)
  {
	if (playerAction>0) { 
	  System.out.println(getPlayerName() + " current location is in " + location);
	  System.out.println("and he wants to go in " + destination);
	  // System.out.println(location.getMaxCube());
	  // System.out.println(destination.getMaxCube());
      // System.out.println("attempting to move " + getPlayerName() + " to " + destination.getName() + " from "+ location.getName());
	  driveCity tmp = new driveCity(location,destination);
      if (tmp.isaLegalMove())
      {    	  
          System.out.println(getPlayerName() + " drives from " + location.getName() + " to " + destination.getName() + ".");
          playerPiece.setLocation(destination);
          decreasePlayerAction();
          suggestions.add( (driveCity) tmp);
          
          
          
          
          return true;
      }
      else 
      {
          System.out.println("the location isn't connected");
      }
	}
      return false;
  }
  public void  undoDriveCity (City prev)
  {
	
	  
         	  
         
          playerPiece.setLocation(prev);
          
         
          
         
  
  
  }
  
  // Charter Flight action
  public boolean charterFlight(City location, City destination)
  {
	if (playerAction>0) { 
	  // System.out.println(getPlayerName() + " wants to flying from " + 
	  // location.getName() + " to "+ destination.getName() + 
	  // " on a charter flight");
	  charterFlight tmp = new charterFlight(location,getHand(),destination);
      if (tmp.isaLegalMove() && playerPiece.getLocation().equals(location))
      {
          System.out.println(getPlayerName() + " takes a charter flight to " + 
          destination.getName() + " from " + location.getName() );
          discardCard(location.getName());
          playerPiece.setLocation(destination);
          decreasePlayerAction();
          suggestions.add(tmp);
          return true;
      }
	}
      return false;
  }
  
  //undo the action of charterFlight
  
  public void undoCharterFlight(City loc)
  {
	  
	  String  cityName = null;
	  
	  if(cardsDiscarded.contains(loc)) 
	  {
		  cityName = loc.getName();
		  playerPiece.setLocation(loc);
		  reviveCard(cityName);
	  }
	  
	 
  }
  
  //Direct flight
  public boolean directFlight(City location, City destination)
  {
	 if (playerAction>0) { 
	// System.out.println(getPlayerName() + " wants to flying from " + 
	// location.getName() + " to "+ destination.getName() + 
	// " on a direct flight");
	  directFlight tmp = new directFlight(destination,getHand());
	  if (tmp.isaLegalMove())
	  {
		  System.out.println(getPlayerName() + " takes a direct flight to " + 
		  destination.getName() + " from " + location.getName() );
		  discardCard(destination.getName());
		  playerPiece.setLocation(destination);
		  decreasePlayerAction();
		  suggestions.add(tmp);
          return true;
	  }
	 }
	  return false;
  }
  
  //undo direct flight action
  public void undoDirectFlight(City loc) 
  {
	  undoCharterFlight(loc);
  }
  
  //ShuttleFlight
  public boolean shuttleFlight(City location, City destination)
  {
	 if (playerAction>0) { 
	// System.out.println(getPlayerName() + " wants to flying from " + 
	// location.getName() + " to "+ destination.getName() + 
	// " on a shuttle flight");
	 shuttleFlight tmp = new shuttleFlight(location,destination);
	 if(tmp.isaLegalMove())
	 {
		 System.out.println(getPlayerName() + " takes a shuttle flight to " + 
		 destination.getName() + " from " + location.getName() );
		 playerPiece.setLocation(destination);
		 decreasePlayerAction();
		 suggestions.add(tmp);
         return true;
	 }
	}
	 return false;
  }
  
  //undo the shuttle flight action
  public void undoShuttleFlight(City loc) 
  {
	  playerPiece.setLocation(loc);
  }
  
  // Discover Cure action
  public boolean discoverCure(City location, String colour)
  { 
	if (playerAction>0) { 
	 discoverCure tmp = new discoverCure(location,getHand(),colour);
	 if (playerRole.equals("SCIENTIST")) {
	    if (tmp.isaLegalMove(1))
		{
		   System.out.println("Its possible to discover a cure");
		   discardCureCards(colour,(pandemicBoard.getNeededForCure()-1));
	       pandemicBoard.cureDisease(colour);
	       
	     
	       
	       decreasePlayerAction();
	       suggestions.add(tmp);
	       return true;
		}
	 }
	 else
	 {
		if (tmp.isaLegalMove(0))
		{
		   System.out.println("Its possible to discover a cure");
		   discardCureCards(colour,pandemicBoard.getNeededForCure());
	       pandemicBoard.cureDisease(colour);
	       decreasePlayerAction();
	       suggestions.add(tmp);
	       return true;
		}
	 }
	}
	 return false;
  }
  //Undo the discover Cure Action, on the board
  
  public boolean undoDiscoverCure(City location, String colour) 
  {
	  if(playerRole.equals("SCIENTIST"))
		{
		  //give back the cards
		  reviveCureCards(cardsDiscarded,(pandemicBoard.getNeededForCure()-1));
		  //notify the board, undo the "cure" action for this Disease
		  Disease toRevive = pandemicBoard.getDisease(colour);
		  toRevive.setCured(false);
		 
		}
	  else
	  {
		  //give back the cards
		  reviveCureCards(cardsDiscarded,(pandemicBoard.getNeededForCure()));
		  //notify the board, undo the "cure" action for this Disease
		  Disease toRevive = pandemicBoard.getDisease(colour);
		  toRevive.setCured(false);
		  
	  }
	  
	  
	return true;
	  
  }
  
  
  /*
   *Below are implemented
   *1) --> OPERATIONS_EXPERT special ability (build research station without to discard a card) is implemented as if in the @BuildResearchStaion method
   *2) --> MEDIC special ability  (treat all the cubes of a specific colour) implemented in @treatDisease as if statement
   *3) --> SCIENTIST special ability is implemented as if in the classic cure disease
   *4) --> QUARANTINE_SPECIALIST special ability is implemented in @infectCityPhase in @GameBoard class 
   */  
  
  //---------------------------------------------------------------------------------
  /**********************************************************************************
  ***************** These methods are used for AI controlled players.****************
  **********************************************************************************/
  //---------------------------------------------------------------------------------
  
  
  public void makeDecision(ArrayList<City>  friend_hand,String Role, City friend_location)
  {
	 
	 //take Variables.Suggestions and build model for others player
      System.out.print(this.getPlayerName() + " is thinking..... ");
      boolean checkCure = checkCureWorthIt();
      
      
      if(friend_hand == null) 
      	 {
    	  
      		 this.myTurn = true;
      		
      		 System.out.println("I am : "+this.getPlayerName() +" -> "+getPlayerRole());
      		 
      		 
      		 this.AItries++;
      		 k += greedyAI(Role);
        	  
        	
        	 
        	 if(suggestions.size() == 4)
        	 {
        		MyUtilities.add(k);
        		k = 0;
        		 
        		 
        		 for(int i = 0; i<MyUtilities.size(); i++) 
        		 {
        			 System.out.println("AI_Utillity:  " +MyUtilities.get(i)+"for action: "+i);
        		 }
        		 
        		 undoActivity(Role);
        		 
        		 
        		 
        		 sugEvaluate(Role);
        		 
        		 System.out.println("After Evaluating All players suggestions I concluded:");
          		 
          		 for(int l=0; l<sugUtilities.size(); l++) 
          		 {
          			 System.out.println("Utillity of Suggestion = "+l+" -> "+sugUtilities.get(l)+" round: "+(l/3+1));
          		 }
          		 
            	  
        		 System.out.println("==================================");
        		 System.out.println("Now I have what I need to decide:");
        		 doStuff(analyseStats(decRound));
        		 decRound++;
        		 
        		 
        		 
        	 	
        	 
        	 }
        	
        	  
      	 }
      else
      	 {
      		  if (checkCure)
      		  {
		          System.out.println("might be worth trying to find a cure.");
		          checkTryCure();
		          
		      }
		     
		      if (!checkCure && (getDistanceResearch() > 3) && (tactic > 0) )
		      {
		    	  
		    	  
		          tactic--;
		          System.out.print("They are far enough from a research station to consider building one.");
		          if (!buildResearchStation())
		          {
		              System.out.println(" Can't find the required card.");
		              rollDice();
		          }
		      }
		      else if (!checkCure)
		      {
		    	  
		    	  rollDice();
		    	  rollDice();
		    	  rollDice();
		    	  rollDice();
		          tactic--;
		      }
		      if (tactic < -500 )
		      {
		          System.out.println("out of ideas, will drive randomly");
		          driveRandom();
		      }
	       
      	 }
    
     
      
      	tactic--;
      
  }
  
  
//Frequential Analysis on Utilities, a tactic similar to fictitious play algorithm
private int analyseStats(int round) {
	
	System.out.println("Attempting to analyse..!!!" +round);
	
	
	
		int j =0;
		int meanMe =0;
		int sub = round+1;
	
		for(int i=round*3; i<sugUtilities.size(); i++) 
		{
				float s;
				if( sub > 1)
					s= ((sub-1)*meanUt[j] + sugUtilities.get(i))/sub;
				else
					s=sugUtilities.get(i)/sub;
				
				
				
				
				meanUt[j]= s;
				j++;
				
				
			
		}
		
		for(int i=0; i<MyUtilities.size(); i++) 
		{
			meanMe += MyUtilities.get(i);
		}
		
		System.out.println("=====================================================");
		
		
		Float[] finalMax=new Float[4];
		int winner = 0;
		for(int k =0; k<meanUt.length; k++) 
		{
			finalMax[k] = meanUt[k];
			
			
			System.out.println("MEAN -> "+meanUt[k] +"Mine: "+meanMe/sub);
			
			
			
			
		}
		finalMax[3] = (float) (meanMe/sub);
		float chicken=finalMax[0];
		for (int i=0; i<finalMax.length; i++)
		{
			if(finalMax[i].compareTo(chicken) > 0)
			{
				chicken = finalMax[i];
				winner = i;
			}
		}
		
		
		
		System.out.println("Final Max Utillity is: "+chicken+"ff: "+winner);
	// TODO Auto-generated method stub
return winner;
}

//Player will either treat disease or go to a city with 3 cubes.
  public void rollDice()
  {
      System.out.print("Wants to treat disease... ");
      if (!tryTreat(3)) {
    	  if (!go3CubeCities()) {
    		  if(!tryTreat(2))	 	 {
    			  if (!go2CubeCities()) 	{
    				  if(!tryTreat(1)) 			{
    					  if (!go1CubeCities()) 	{
    						  System.out.println("Going to drive randomly as can't think of anything.");
    						  driveRandom();
    					  }
    				  }
    			  }
    		  }
    	  }
      }                
  }

  //Check to see if the disease can be treat according to the @param threshold of cubes.
  public boolean tryTreat(int threshold)
  {
      boolean toReturn = false;
      City locationCity = playerPiece.getLocation();
      if (locationCity.getMaxCube() >= threshold)
      {
          System.out.println("As there are " + threshold + " cubes in " + locationCity.getName() + " " + this.getPlayerName() + " will try and treat disease.");
          String locationColour = locationCity.getColour();
          treatDisease(locationCity,locationColour);
          toReturn = true;
      }
      else 
      {
          System.out.println("Doesn't think it's worth trying to treat here.");
      }
      return toReturn;
      
  }
  
  public boolean checkTryCure()
  {
      if (checkCureWorthIt()) {
          if (discoverCure(playerPiece.getLocation(),tryCureCardColour())) {
              System.out.println(this.getPlayerName() + " has discovered a cure!");
              System.out.println("Yeah!!");
              System.out.println("  Yeah!!");
              System.out.println("    Yeah!!");    
              return true;
          }
          else{
              System.out.println("They need to go to a researh station.");
              tryDriveResearchStation();
          }
      }
      else{
         System.out.println("no point in trying to find a cure.");
      }
      return false;
  }
  
  //evaluate the chance for cure , (if is it worth)
  public boolean checkCureWorthIt()
  {
      String toCure = tryCureCardColour();
      if (toCure != null)
      {
          for (int i = 0 ; i < pandemicBoard.diseases.size() ; i ++){
        	  
              Disease disease = pandemicBoard.diseases.get(i);
              if (toCure == disease.getColour() && !disease.getCured()){
                  return true;
              }
          }          
      }
      return false;      
  }
  
  //an attempt to drive to nearest research station
  public void tryDriveResearchStation()
  {
      System.out.println("Searching cities with research stations as destinations.");
      getDistances(Variables.GET_CITIES_WITH_RESEARCH_STATION());
      // System.out.println("Calculating destination");
      City toDriveTo = calculateDestination();
      //System.out.println("I'll try to drive to " + toDriveTo.getName());
      driveCity(playerPiece.getLocation(),toDriveTo); 
  }
  
  
	
  /** 
   * Check to see if the count of cards in any color equal the number required
   * for a cure
  **/
  public String tryCureCardColour()
  {
      for (int i = 0; i < pandemicBoard.getNumberColours(); i ++)
      {
    	 
          if (getCountXCards(possibleColour[i]) >= pandemicBoard.getNeededForCure())
          {
        	 
              return possibleColour[i];
          }           
      }
      return null;
  }
  
  //get distance 
  public int getDistanceResearch()
  {		
	  ArrayList<City> destinations = new ArrayList<City>();
	  for (City c : pandemicBoard.cities) {
		  for (City dest: Variables.GET_CITIES_WITH_RESEARCH_STATION()) {
			  if (c.getName().equals(dest.getName())) {
				  destinations.add(c);
			  }
		  }
	  }
	  
      getDistances(destinations);
      return playerPiece.getLocation().getDistance();
  }
  
  //get distances of the cities which a look to travel
  public void getDistances(ArrayList<City> destinations){
      pandemicBoard.resetDistances();
      setDestination(destinations);
      int distance = 1;
      int loc=-1;
      for(int i = 0 ; i < pandemicBoard.cities.size() ; i ++){
        if (pandemicBoard.cities.get(i).getName().equals( playerPiece.getLocation().getName() )){
            loc = i;
            break;
          }
      }
      while (pandemicBoard.cities.get(loc).getDistance() == 9999){
          //System.out.println("Looking for places distance of " + distance);
          for (int i = 0 ; i < pandemicBoard.cities.size() ; i ++){
        	  for (int x = 0 ; x < pandemicBoard.cities.get(i).getNeighbors().size() ; x ++){        		  
        		  //System.out.println(pandemicBoard.cities.get(i).getNeighbors().get(x).getDistance());
                  if ( pandemicBoard.cities.get(i).getDistance() == (distance-1) && pandemicBoard.cities.get(i).getNeighbors().get(x).getDistance() > distance ){
                      pandemicBoard.cities.get(i).getNeighbors().get(x).setDistance(distance);
                  }
              }
          }
          distance ++;
          
      }
  }
  
  public void setDestination(ArrayList<City> destinations)
  {
      for (int i = 0 ; i < destinations.size() ; i ++)
      {
          destinations.get(i).setDistance(0);
      }        
  }
  
  // Try to random charter flight
  public void charterRandom(){
      Random rand = new Random();
      int n = rand.nextInt(pandemicBoard.cities.size());
      charterFlight(playerPiece.getLocation(),pandemicBoard.cities.get(n));
  }
  
  //try to drive to random city until a possible city is chosen.
  public void driveRandom(){
	  Random rand1 = new Random();
	  City temp = playerPiece.getLocation();
      int n = rand1.nextInt(temp.getNeighbors().size());
      driveCity(playerPiece.getLocation(),temp.getNeighbors().get(n));
  }
  
  public City calculateDestination()
  {
      int closestDestination = 9999;
      City toReturn = new City(0,0,0,0,0);
      for (int i = 0 ; i < playerPiece.getLocation().getNeighbors().size(); i++)
      {	
          if (playerPiece.getLocation().getNeighbors().get(i).getDistance() < closestDestination)
          {
              //System.out.println("Will probably go to " + playerPiece.getLocationConnections()[i].getName());
              toReturn = playerPiece.getLocation().getNeighbors().get(i);
              closestDestination = playerPiece.getLocation().getNeighbors().get(i).getDistance();
          }
          
      }
      return toReturn;
  }
  
  //find all the cities with 3 cubes and measure distances to make a decision 
  //in which city to drive
  public boolean go3CubeCities()
  {        
	  ArrayList<City> CitiesWith3Cubes =  pandemicBoard.get3CubeCities();
      if (CitiesWith3Cubes.size() > 0)
      {
          //System.out.print("Setting 3 cube cities ");
//          for (int i = 0 ; i < CitiesWith3Cubes.size() ; i ++)
//          {
//              System.out.print("#:"+(i+1) + " " + CitiesWith3Cubes.get(i).getName());
//          }
         
          getDistances(CitiesWith3Cubes);
          //System.out.println(" as destinations.");
          City toDriveTo = calculateDestination();
          //System.out.println(this.getPlayerName() + " will go to " + toDriveTo.getName());
          driveCity(playerPiece.getLocation(),toDriveTo);
          return true;
      }
      else
      {
          System.out.println("No 3 cube cities.");
          return false;
      }
  }
  
  //find all the cities with 2 cubes 
  //measure distance and drive to best case (city)
  public boolean go2CubeCities()
  {        
	  ArrayList<City> CitiesWith2Cubes = pandemicBoard.get2CubeCities();
      if (CitiesWith2Cubes.size() > 0)
      {
          //System.out.print("Setting 2 cube cities ");
//          for (int i = 0 ; i < CitiesWith2Cubes.size() ; i ++)
//          {
//             System.out.print("#:"+(i+1) + " " + CitiesWith2Cubes.get(i).getName());
//          }
          getDistances(CitiesWith2Cubes);
          //System.out.println(" as destinations.");
          City toDriveTo = calculateDestination();
          //System.out.println(this.getPlayerName() + " will go to " + toDriveTo.getName());
          driveCity(playerPiece.getLocation(),toDriveTo);
          return true;
      }
      else
      {
          //System.out.println("No 2 cube cities.");
          return false;
      }
  } 
  
  //find all the cities with 1 cubes 
  //measure distance and drive to best case (city)
  public boolean go1CubeCities()
  {        
	  ArrayList<City> CitiesWith1Cubes = pandemicBoard.get1CubeCities();
      if (CitiesWith1Cubes.size() > 0)
      {
          //System.out.print("Setting 1 cube cities ");
//          for (int i = 0 ; i < CitiesWith1Cubes.size() ; i ++)
//          {
//              System.out.print("#:"+(i+1) + " " + CitiesWith1Cubes.get(i).getName());
//          }
          getDistances(CitiesWith1Cubes);
          //System.out.println(" as destinations.");
          City toDriveTo = calculateDestination();
          //System.out.println(this.getPlayerName() + " will go to " + toDriveTo.getName());
          driveCity(playerPiece.getLocation(),toDriveTo);
          return true;
      }
      else
      {
          //System.out.println("No 1 cube cities.");
          return false;
      }
  }
  
  

	public Object clone(GameBoard gb, Piece pc) throws CloneNotSupportedException {
		Player cloned = (Player) super.clone();
		cloned.playerName = String.valueOf(this.playerName);
		cloned.playerRole = String.valueOf(this.playerRole);
		cloned.pandemicBoard = gb;
		cloned.playerPiece = pc;
		ArrayList<City> clonedhands = new ArrayList<City>();
		for (int i=0;i<this.hand.size();i++) {
			clonedhands.add((City)this.hand.get(i).clone());
		}
		cloned.hand = clonedhands;
		cloned.suggestions = this.suggestions; //shallow copy
		return cloned;
	}
	/*
	   * Get a City, return the number of cubes in it
	   * 
	   * 
	   * */
	  private int cityCubes(City c) 
	  {
		  ArrayList<City> CitiesWith1Cubes = pandemicBoard.get1CubeCities();
		  ArrayList<City> CitiesWith2Cubes = pandemicBoard.get2CubeCities();
		  ArrayList<City> CitiesWith3Cubes = pandemicBoard.get3CubeCities();
		  
		  
		  for(int i =0; i<CitiesWith1Cubes.size(); i++) 
		  {
			  if(c == CitiesWith1Cubes.get(i)) 
			  {
				  return 1;
			  }
			 
		  }
		  for(int i =0; i<CitiesWith2Cubes.size(); i++) 
		  {
			  if(c == CitiesWith2Cubes.get(i)) 
			  {
				  return 2;
			  }
			  
		  }
		  for(int i =0; i<CitiesWith3Cubes.size(); i++) 
		  {
			  if(c == CitiesWith3Cubes.get(i)) 
			  {
				  return 3;
			  }
			  
		  }
		  
		  
		  return 0;
		  
		  
		  
		 
		  
		  
	  }
	  
	  

	 // suggestions evaluation 
	  
	  public void sugEvaluate(String role) {
		    
		  String tst = "";
		  switch(role) 
		  {
		  		case "QUARANTINE_SPECIALIST":	
		  			System.out.println("------------------------O xristos/QUARANTINE SPECIALIST--------------------------------------");
		  			int i=0;
		  			int j = 0;
		  			while(i<3) {
		  				while(j<4) {
		  					System.out.println(Variables.Suggestions[i].get(j).getClass().getName());
		  					j++;
		  				}

			  			i++;	
		  			}
		  		
		  			EvalProp(role);
		  			
		  			  
		  			
		  			break;
		  		case "OPERATIONS_EXPERT":
		  			
		  			System.out.println("---------------------------O iakwbos/OPERATIONS EXPERT-------------------------------");
		  			i=0;
		  			j = 0;
		  			while(i<3) {
		  				while(j<4) {
		  					System.out.println(Variables.Suggestions[i].get(j).getClass().getName());
			  				j++;

		  				}
		  				i++;
		  			}
		  		
		  			
		  			EvalProp(role);
		
		  			break;
		  		case "SCIENTIST":
		  			
		  			System.out.println("-----------------------------O bombardistis/SCIENTIST----------------------------");
		  			i=0;
		  			j = 0;
		  			try {
		  			while(i< Variables.Suggestions.length){
		  				while(j<suggestions.size()) {
		  					System.out.println(Variables.Suggestions[i].get(j).getClass().getName());
		  					j++;
		  				}
		  				i++;
		  			}
		  			
		  			
		  			EvalProp(role);
		  			}
		  			catch(Exception e)
		  			{
		  				
		  				System.out.println("Problem happened" +e);
		  				
		  			}
		  			break;
		  		case "MEDIC":
		  			
		  			System.out.println("------------------------------O nekri pisina/MEDIC------------------------------");
		  			i=0;
		  			j = 0;
		  			while(i<3) {
		  				while(j<4) {
		  					System.out.println(Variables.Suggestions[i].get(j).getClass().getName());
		  					j++;
		  				}
		  				i++;
		  			}
		  			
		  			
		  			EvalProp(role);

		  			break;
		  		default:
		  			System.out.println("--------------------------o tipotas-----------------------------");
		  			break;
		  }
		  
	  
		  
	  }
	  
	  
	  // Evaluation depending on each Role (because of dynamic utilities depending on role)//
	  
	  
	 
	  
	public void EvalProp(String role) {
		
		
		  float util = 0;
		  int diff = 0;
		  
		  System.out.println("========================================================================");
		  System.out.println(this.getPlayerName() + "attempt to evaluate suggestions, what he sees is:");
		  for(int k =0; k<Variables.Suggestions.length-1; k++) {
			  ArrayList<Action> s  = Variables.Suggestions[k];
			 
				  System.out.println(s.toString()+ "index: "+k);
			  
		  }
		  
		  System.out.println("========================================================================");
		  //.getClass() requires " == " 
		  
		  for(int i=0; i<3; i++) {
			  ArrayList<Action> dbg = Variables.Suggestions[i];
			  System.out.println("Snack Pot has: "+dbg);
			 for(int j=0; j<4; j++) {
				 
				 System.out.println("|Constraints| => (i) , (j) =" +i+" "+j);
				 System.out.println("Evaluating Action: "+Variables.Suggestions[i].get(j));
				 System.out.println("========================================================================");
				 
				 
				 if(Variables.Suggestions[i].get(j).getClass() == driveCity.class) {
					 
					 
					
				
					 driveCity dC = (Pandemic.actions.driveCity) Variables.Suggestions[i].get(j);
					 
					 diff =cityCubes(dC.getMoveTo()) - cityCubes(dC.getMoveFrom()); 
					
					 
					//utilising based on distance
					 if(diff == 0) 
					 {
						 //neutral
						 util += 1.0;
					 } 
					 else if(diff > 0) 
					 {
						 //high gain
						 util = util + diff*2;
					 }
					 else if(diff < 0) 
					 {
						 //small penalty
						 util += 0.0; 
					 }
			 
	 				
					 
					 
					 

				 }else if(Variables.Suggestions[i].get(j).getClass() == treatDisease.class){
					 
					 
					
					 treatDisease tD = 	(Pandemic.actions.treatDisease) Variables.Suggestions[i].get(j);
					
					 tD.getColour();
					 Disease dis = pandemicBoard.getDisease(tD.getColour());
					 
					 
					 
					 // I'm a MEDIC and there is no CURE yet Discovered				 
					 if(role == "MEDIC") 
					 {
						 //scale up medic utility based on city's cubes
						 
						
						 // the number of cubes int the city attempting to treat some Disease
						 int cubNum = cityCubes(tD.getLocation());
						 switch(cubNum) 
						 {
						 	case 3:
							 	util += 10.0;
							 	break;
						 	case 2:
						 		util += 7.0;
							 	break;
							
						 	case 1:
						 		util += 3.0;
							 	break;
						 	
							 
						 	default:
						 		System.out.println("Why treating here:"+cubNum+"city: "+tD.getLocation());
						 
						 
						 }
						 
					 }			
					 else
					 {
						 //Im not a medic and the disease is cured ==> EVERYBODY IS A MEDIC NOW
						if(dis.getCured())
						{
							System.out.println("SOMETHING IS CURED: "+dis.getColour()+" "+pandemicBoard.getDisease(tD.getColour()));
							//scale up medic utility based on city's cubes
							 
							
							 // the number of cubes int the city attempting to treat some Disease
							 int cubNum = cityCubes(tD.getLocation());
							 switch(cubNum) 
							 {
							 	case 3:
								 	util += 10.0;
								 	break;
							 	case 2:
							 		util += 6.0;
								 	break;
								
							 	case 1:
							 		util += 3.0;
								 	break;
								 
							 	default:
							 		System.out.println("Why treating here(not a MEDIC):"+cubNum+"city: "+tD.getLocation());
							 }	
						}
						else 
						{
							// Everybody acts with respect to his identity - cures 1 cube per row
							
							util += 5.0;
							System.out.println("Why treating here(not a MEDIC):"+cityCubes(tD.getLocation())+"city: "+tD.getLocation());
						}
					 }
						
					
			  
				 }else if(Variables.Suggestions[i].get(j).getClass() == discoverCure.class){
					
					//mark the event of cure discovery 
					
					discoverCure dC= (Pandemic.actions.discoverCure) Variables.Suggestions[i].get(j);
					colorDisc = dC.getColorOfDisease();
					
					
					
					 //why not treating, go ahead son
					 if(role == "SCIENTIST")
						 util += 14.0; 
					 else
						 util += 12.0;
						 
						 
				 }else if(Variables.Suggestions[i].get(j).getClass() == directFlight.class) {
					 String dCol;
					 
					 
					 if(role == "SCIENTIST") {
						 
						
						 if(getHand().size() > 3)
						 {
						 //if full color, the next possible action after building could be a cure
							 if(getCountXCards("black") == 3 || getCountXCards("yellow") == 3 || getCountXCards("blue") == 3 || getCountXCards("red") == 3) 
							 {
								 
								 directFlight dF = (Pandemic.actions.directFlight)Variables.Suggestions[i].get(j);
								 //if im able to discard this card cause of its color
								 dCol =  dF.getMoveTo().getColour();
								 
								 //is there a research station there?
								 ArrayList<City> rC = Variables.GET_CITIES_WITH_RESEARCH_STATION();	
								
								 
								 if(getCountXCards(dCol) != 3 &&  rC.contains(dF.getMoveTo())) 
								 {
									 //good move do it
									util +=  9.0; 
								 }
								 else 
								 {
									 //why ruin everything?
									 util -= 8.0;
									 
								 }
							 }
						 }
					 }
					 else {
						 
						 if(getHand().size() > 4)
						 {
						 //if full color, the next possible action after building could be a cure
							 if(getCountXCards("black") == 4 || getCountXCards("yellow") == 4 || getCountXCards("blue") == 4 || getCountXCards("red") == 4) 
							 {
								 
								 directFlight dF = (Pandemic.actions.directFlight)Variables.Suggestions[i].get(j);
								 //if im able to discard this card cause of its color
								 dCol =  dF.getMoveTo().getColour();
								 
								 //is there a research station there?
								 ArrayList<City> rC = Variables.GET_CITIES_WITH_RESEARCH_STATION();	
								
								 
								 if(getCountXCards(dCol) != 4 &&  rC.contains(dF.getMoveTo())) 
								 {
									 //good move do it
									util +=  8.0; 
								 }
								 else 
								 {
									 //why ruin everything?
									 util -= 8.0;
									 
								 }
							 }
						 }
						 
					 }
					 
					 
					 
					 
					 
				 }else if(Variables.Suggestions[i].get(j).getClass() == shuttleFlight.class) {
					 //TODO: SHUTTLE DIFF FIND
					shuttleFlight sH = (Pandemic.actions.shuttleFlight) Variables.Suggestions[i].get(j);
					diff =cityCubes(sH.getMoveTo()) - cityCubes(sH.getMoveFrom()); 
					//utilising based on cubes
					 if(diff == 0) 
					 {
						 //neutral
						 util += 1.0;
					 } 
					 else if(diff > 0) 
					 {
						 //high gain
						 util = util + diff*2;
					 }
					 else if(diff < 0) 
					 {
						 //small penalty
						 util += 0.0; 
					 }
			 
					 
					 
				 }else if(Variables.Suggestions[i].get(j).getClass() == buildResearchStation.class) {
					 
					 String dCol;
					if(role == "OPERATIONS EXPERT") { 
						
						util+=11;
						
					}else if(getHand().size() > 4 && role != "SCIENTIST"){
					 //if full color, the next possible action after building could be a cure
						 if(getCountXCards("black") == 4 || getCountXCards("yellow") == 4 || getCountXCards("blue") == 4 || getCountXCards("red") == 4) 
						 {
							 
							 buildResearchStation bs = (Pandemic.actions.buildResearchStation)Variables.Suggestions[i].get(j);
							 //if im able to discard this card cause of its color
							 dCol =  bs.getCityToResearchStation().getColour();
							 if(getCountXCards(dCol) != 4 ) 
							 {
								 //good move do it
								util +=  8.5; 
							 }
							 else 
							 {
								 //why ruin everything?
								 util -= 8.0;
								 
							 }
						
							 
						 
						 }
						 else 
						 {
							 util -= 8.0;
						 }
					 }else if(role == "SCIENTIST") {
						 if(getHand().size() > 3){
							 //if full color, the next possible action after building could be a cure
								 if(getCountXCards("black") == 3 || getCountXCards("yellow") == 3 || getCountXCards("blue") == 3 || getCountXCards("red") == 3) 
								 {
									 
									 buildResearchStation bs = (Pandemic.actions.buildResearchStation)Variables.Suggestions[i].get(j);
									 //if im able to discard this card cause of its color
									 dCol =  bs.getCityToResearchStation().getColour();
									 if(getCountXCards(dCol) != 3 ) 
									 {
										 //good move do it
										util +=  9.5; 
									 }
									 else 
									 {
										 //why ruin everything?
										 util -= 8.0;
										 
									 }
								
									 
								 
								 }
						 
					 }
					 else 
					 {
						 util -= 8.0;
						 
					 }
					}
					 
				 }else if(Variables.Suggestions[i].get(j).getClass() == charterFlight.class)
				 {
					 
					 
					 
					 String dCol = "";
					 if(role != "SCIENTIST") {
						 if(getHand().size() > 4)
						 {
						 //if full color, the next possible action after building could be a cure
							 if(getCountXCards("black") == 4 || getCountXCards("yellow") == 4 || getCountXCards("blue") == 4 || getCountXCards("red") == 4) 
							 {
								 
								 
								 charterFlight cF = (Pandemic.actions.charterFlight)Variables.Suggestions[i].get(j);
								 //if im able to discard this card cause of its color
								 dCol =  cF.getMoveTo().getColour();
								 
								 //is there a research station there?
								 ArrayList<City> rC = Variables.GET_CITIES_WITH_RESEARCH_STATION();	
								
								 
								 if(getCountXCards(dCol) != 4 &&  rC.contains(cF.getMoveTo())) 
								 {
									 //good move do it
									util +=  9.0; 
								 }
								 else 
								 {
									 //why ruin everything?
									 util -= 8.0;
									 
								 }
							 }
						 }
					 }else 
					 {
						 //case we got a scientist
						 if(getHand().size() > 3)
						 {
						 //if full color, the next possible action after building could be a cure
							 if(getCountXCards("black") == 3 || getCountXCards("yellow") == 3 || getCountXCards("blue") == 3 || getCountXCards("red") == 3) 
							 {
								 
								 charterFlight cF = (Pandemic.actions.charterFlight)Variables.Suggestions[i].get(j);
								 //if im able to discard this card cause of its color
								 dCol =  cF.getMoveTo().getColour();
								 
								 //is there a research station there?
								 ArrayList<City> rC = Variables.GET_CITIES_WITH_RESEARCH_STATION();	
								
								 
								 if(getCountXCards(dCol) != 3 &&  rC.contains(cF.getMoveTo())) 
								 {
									 //good move do it
									util +=  10.0; 
								 }
								 else 
								 {
									 //why ruin everything?
									 util -= 8.0;
									 
								 }
							 }
						 }
					 }
				 }
				 else 
				 {
					 System.out.println("Kalimeraaaa ti kaaneis? Na sai paaanta kalaaaaaaa!!!!!");
					 
				 }
			
			 }
			 
			 
			
			 System.out.println("Calculated utility = "+util);
			 
			 //append on end of utillity lists
			 sugUtilities.add(util);
			 
			 //recalibrate
			 util = 0;
			
			
			 
		  
				
		  }
		
		
		 
			  
	}
	  
	public void undoActivity(String role) {
		
	
	
	
		
		
		
		boolean f = suggestions.isEmpty();
		if(f!=true) 
		{
			for(int j=3; j>=0; j--) 
			{
				
				if(suggestions.get(j).getClass() == driveCity.class) 
				{
					//undoing the driveCity Action
					driveCity dc = (Pandemic.actions.driveCity)suggestions.get(j);
					undoDriveCity(dc.getMoveFrom());
					System.out.print(true);
				}
				else if(suggestions.get(j).getClass() == treatDisease.class) 
				{
					//undoing the treatDisease Action
					
					treatDisease tD = (Pandemic.actions.treatDisease)suggestions.get(j);
					undoTreatDisease(tD.getLocation(),tD.getColour());
					
					
				}
				else if(suggestions.get(j).getClass() == discoverCure.class) 
				{
					discoverCure discCure = (Pandemic.actions.discoverCure)suggestions.get(j);
					undoDiscoverCure(discCure.getCurrent_city(),discCure.getColorOfDisease());
				}
				else if(suggestions.get(j).getClass() == charterFlight.class) 
				{
					charterFlight cF = (Pandemic.actions.charterFlight)suggestions.get(j);
					undoCharterFlight(cF.getMoveTo());
					
				}
				else if(suggestions.get(j).getClass() == shuttleFlight.class) 
				{
					shuttleFlight sF = (Pandemic.actions.shuttleFlight)suggestions.get(j);
					undoShuttleFlight(sF.getMoveTo());
				}
				else if(suggestions.get(j).getClass() == directFlight.class) 
				{
					directFlight dF = (Pandemic.actions.directFlight)suggestions.get(j);
					undoDirectFlight(dF.getMoveTo());
					
				}
				else if(suggestions.get(j).getClass() == buildResearchStation.class) 
				{
					buildResearchStation bs = (Pandemic.actions.buildResearchStation)suggestions.get(j);
					undoBuildResearchStation(bs.getCityToResearchStation());
				}
			}
		}
		
		
		
		
	
	
		
		
	}
	  
	
		 
	
	public float greedyAI(String role) 
	{
		
		float myUtl = 0;
		boolean hasCommited = false;
		boolean tryCure = checkCureWorthIt();
		Disease dis = (Pandemic.variables.Disease)pandemicBoard.getDisease(playerPiece.getLocation().getColour());
	
		
		//get some data to work with
		
		ArrayList<City> neighbors = playerPiece.getLocationConnections();
		ArrayList<City> CitiesWithNoCubes = new ArrayList<City>();
		ArrayList<City> CitiesWith1Cubes = pandemicBoard.get1CubeCities();
		ArrayList<City> CitiesWith2Cubes = pandemicBoard.get2CubeCities();
		ArrayList<City> CitiesWith3Cubes = pandemicBoard.get3CubeCities();
      
		ArrayList<City> ThreeCubeNeighbors = new ArrayList<City>();
		ArrayList<City> TwoCubeNeighbors = new ArrayList<City>();
		ArrayList<City> OneCubeNeighbors = new ArrayList<City>();
		
		System.out.println("Attemting to play on my own..");
		System.out.println("===============================");
		
		for (int j=0; j<neighbors.size(); j++) 
		{
			if(CitiesWith1Cubes.contains(neighbors.get(j)))
				OneCubeNeighbors.add(neighbors.get(j));
			else if(CitiesWith2Cubes.contains(neighbors.get(j)))
				TwoCubeNeighbors.add(neighbors.get(j));
			else if(CitiesWith3Cubes.contains(neighbors.get(j)))
				ThreeCubeNeighbors.add(neighbors.get(j));
			else
				CitiesWithNoCubes.add(neighbors.get(j));
			
		}
		
		
		//if its worth curing
		
		if(tryCure &&  (hasCommited ==false)) 
		{
			
			
			if(role == "SCIENTIST"){
				myUtl += 14;
			}
			else 
			{
				myUtl += 12;
			}
			discoverCure(playerPiece.getLocation(),tryCureCardColour());
			hasCommited = true;
			
		}
		else if( !Variables.CITY_WITH_RESEARCH_STATION.contains(playerPiece.getLocation()) && role.equals("OPERATIONS EXPERT") && (hasCommited == false))
		{
			buildResearchStation();
			hasCommited = true;
			myUtl += 11;
			
		}
		else if((dis.getCured() || role.equals("MEDIC")) && cityCubes(playerPiece.getLocation()) == 3)
		{
			
			hasCommited = true;
			if(treatDisease(playerPiece.getLocation(),dis.getColour())) 
			{
				myUtl+= 10;
			}
			
		}
		else if(role.equals("SCIENTIST")  && (getCountXCards("black") > 3 || getCountXCards("blue") > 3 || getCountXCards("red") > 3 || getCountXCards("yellow") > 3 )) 
		{
			hasCommited = true;
			//search a city with research station in hand of player
			City c= null;
			boolean found = false;
			//for each card
			for(int j =0; j < getHand().size(); j++) 
			{
				c = getHand().get(j);
				if(Variables.CITY_WITH_RESEARCH_STATION.contains(c) && found == false) 
				{
					found = true;
					
				}
				
			}
			
			
			if(directFlight(playerPiece.getLocation(),c)) 
			{
				myUtl += 9;
			}
		}
		else if( (role.equals("OPERATIONS EXPERT") == false) && buildResearchStation() && (hasCommited == false)) 
		{
			
			hasCommited = true;
		    myUtl += 8.5;
		}
		else if(!role.equals("SCIENTIST")  && (getCountXCards("black") > 4 || getCountXCards("blue") > 4 || getCountXCards("red") > 4 || getCountXCards("yellow") > 4 )) 
		{
			hasCommited = true;
			//search a city with research station in hand of player
			City c= null;
			boolean found = false;
			//for each card
			for(int j =0; j < getHand().size(); j++) 
			{
				c = getHand().get(j);
				if(Variables.CITY_WITH_RESEARCH_STATION.contains(c) && found == false) 
				{
					found = true;
					
				}
				
			}
			
			
			if(directFlight(playerPiece.getLocation(),c)) 
			{
				myUtl += 8;
			}
			
		}
		else if((dis.getCured() || role.equals("MEDIC")) && cityCubes(playerPiece.getLocation()) == 2)
		{
			if(treatDisease(playerPiece.getLocation(),dis.getColour())) 
			{
				myUtl+= 7;
			}
			
		}
		//try finding a nice location with 3 cubes to drive to
		else if(ThreeCubeNeighbors.size() > 0 && (hasCommited == false)) 
		{
			hasCommited = true;
			driveCity(playerPiece.getLocation(),ThreeCubeNeighbors.get(0));
			
			int diff = 3 - cityCubes(playerPiece.getLocation());
			
			if(diff == 0)
				myUtl += 1;

			
			else if(diff < 0)
				myUtl += 0;
			else
				myUtl += 2*diff;
		}
		else if(treatDisease(playerPiece.getLocation(),dis.getColour())) 
		{
			hasCommited = true;
			myUtl += 5;
		}
	  else if(TwoCubeNeighbors.size() > 0 && (hasCommited == false)) 
		{
		  hasCommited = true;
		  driveCity(playerPiece.getLocation(),TwoCubeNeighbors.get(0));
		    
			int diff = 2 -cityCubes(playerPiece.getLocation());
			
			if(diff == 0)
				myUtl += 1;
			else if(diff < 0)
				myUtl += 0;
			else
				myUtl += 2*diff;
		}
		else if((dis.getCured() || role.equals("MEDIC")) && cityCubes(playerPiece.getLocation()) == 1)
		{
			hasCommited = true;
			if(treatDisease(playerPiece.getLocation(),dis.getColour())) 
			{
				myUtl+= 3;
			}
		}
		
		else if(OneCubeNeighbors.size() > 0 && (hasCommited == false)) 
		{
			hasCommited = true;
			driveCity(playerPiece.getLocation(),OneCubeNeighbors.get(0));
			int diff = 1 -cityCubes(playerPiece.getLocation());
			
			if(diff == 0)
				myUtl += 1;
			else if(diff < 0)
				myUtl += 0;
			else
				myUtl += 2*diff;
		}
		else if(CitiesWithNoCubes.size() > 0 && hasCommited == false) 
		{
			hasCommited = true;
			driveCity(playerPiece.getLocation(),CitiesWithNoCubes.get(0));
			int diff = cityCubes(playerPiece.getLocation());
			
			if(diff == 0)
				myUtl += 1;
			else if(diff < 0)
				myUtl += 0;
			else
				myUtl += 2*diff;
		}
		else 
		{
			rollDice();
			rollDice();
			rollDice();
			rollDice();
			myUtl -= 2;
		}
		if(role.equals("OPERATIONS EXPERT")) 
		{
			System.out.print("Check this out" + hasCommited);
		}
		
		System.out.println("Inner utility (from AI): " +myUtl+" -> "+AItries);
		if(this.AItries == 4) 
		{
			this.AItries = 0;
		}
	
		return myUtl;
		  
		
	}		
		  
	public void doStuff(int actor) 
	{
		ArrayList<Action> toPerform =new ArrayList<Action>();
		if(actor == 3) 
		{
			toPerform = getSuggestions();
		}
		else 
		{
			toPerform = Variables.Suggestions[actor];
		}
		
		System.out.println("I will perform");
		
		//just printing
		for(int i =0; i<toPerform.size(); i++) 
		{
			System.out.println("*** "+toPerform.get(i)+" ***");
		}
		
		
		
		//now we perform those
		boolean f = toPerform.isEmpty();
		if(f!=true) 
		{
			for(int j=0; j<toPerform.size(); j++) 
			{
				
				if(toPerform.get(j).getClass() == driveCity.class) 
				{
					//undoing the driveCity Action
					driveCity dc = (Pandemic.actions.driveCity)toPerform.get(j);
					driveCity(dc.getMoveFrom(),dc.getMoveTo());
					System.out.print(true);
				}
				else if(toPerform.get(j).getClass() == treatDisease.class) 
				{
					//undoing the treatDisease Action
					
					treatDisease tD = (Pandemic.actions.treatDisease)toPerform.get(j);
					treatDisease(tD.getLocation(),tD.getColour());
					
					
				}
				else if(toPerform.get(j).getClass() == discoverCure.class) 
				{
					discoverCure discCure = (Pandemic.actions.discoverCure)toPerform.get(j);
					discoverCure(discCure.getCurrent_city(),discCure.getColorOfDisease());
				}
				else if(toPerform.get(j).getClass() == charterFlight.class) 
				{
					charterFlight cF = (Pandemic.actions.charterFlight)toPerform.get(j);
					charterFlight(cF.getMoveFrom(),cF.getMoveTo());
					
				}
				else if(toPerform.get(j).getClass() == shuttleFlight.class) 
				{
					shuttleFlight sF = (Pandemic.actions.shuttleFlight)toPerform.get(j);
					shuttleFlight(sF.getMoveFrom(),sF.getMoveTo());
					
					
					
				}
				else if(toPerform.get(j).getClass() == directFlight.class) 
				{
					directFlight dF = (Pandemic.actions.directFlight)toPerform.get(j);
					directFlight(playerPiece.getLocation(),dF.getMoveTo());
					
				}
				else if(toPerform.get(j).getClass() == buildResearchStation.class) 
				{
					buildResearchStation bs = (Pandemic.actions.buildResearchStation)toPerform.get(j);
					buildResearchStation();
				}
			}
		}
		
		
		
		
		
	}  
}



