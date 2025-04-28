
/**
 * A class to represent each racer in the horse race
 * encapsulating all relevant data and methods for a horse
 * 
 * @author Joshua Lewis
 * @version 28/04/2025
 */

public class Horse
{
    //Fields of class Horse
    private String name;
    private char symbol;
    private int distanceTraveled;
    private boolean hasFallen;
    private double confidence;
    private boolean hasWon;
      
    //Constructor of class Horse
    /**
     * Constructor for objects of class Horse
     */
    public Horse(char horseSymbol, String horseName, double horseConfidence)
    {
        this.name = horseName;
        setSymbol(horseSymbol);
        setConfidence(horseConfidence);
        this.distanceTraveled = 0;
        this.hasFallen = false;
        this.hasWon = false;
    }



    /***
     * Sets the horse as fallen and debuffs its confidence
     */
    public void fall()
    {
        hasFallen = true;
        confidence -= 0.1;
    }

    /***
     * returns the horse's confidence
     */
    public double getConfidence()
    {
        return confidence;
    }

    /***
     * returns the distance travelled
     */
    public int getDistanceTravelled()
    {
        return distanceTraveled;
    }
    /***
     * returns the horse's name
     */
    public String getName()
    {
        return name;
    }
    /***
     * returns the horse's symbol
     */
    public char getSymbol()
    {
        return symbol;
    }

    /***
     * resets the horse's distance, fallen flag and winner flag
     * so the race can be restarted
     */
    public void goBackToStart()
    {
        distanceTraveled = 0;
        hasFallen = false;
        hasWon = false;
    }
    /***
     * marks the horse as fallen
     */
    public boolean hasFallen()
    {
        return hasFallen;
    }

    /***
     * increments the distance travelled
     */
    public void moveForward()
    {
        distanceTraveled++;
    }

    /***
     * sets the horse's confidence and ensures
     * it is within the range of 0-1
     */
    public void setConfidence(double newConfidence)
    {
        if (newConfidence < 0) {newConfidence = 0;}
        else if (newConfidence > 1) {newConfidence = 1;}
        confidence = newConfidence;
    }

    /***
     * sets the horse's charSymbol
     */
    public void setSymbol(char newSymbol)
    {
        this.symbol = newSymbol;
    }

    /***
     * returns if this horse was the winner
     */
    public boolean hasWon()
    {
        return hasWon;
    }

    /***
     * sets a flag to track if this horse won
     * also buffs confidence as a result
     */
    public void setAsWinner() {
        confidence += 0.1;
        hasWon = true;
    }

}