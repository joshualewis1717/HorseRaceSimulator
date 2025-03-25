
/**
 * Write a description of class Horse here.
 * 
 * @author Joshua Lewis
 * @version 25/03/2025
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



    //Other methods of class Horse
    public void fall()
    {
        hasFallen = true;
    }

    public double getConfidence()
    {
        return confidence;
    }

    public int getDistanceTravelled()
    {
        return distanceTraveled;
    }

    public String getName()
    {
        return name;
    }

    public char getSymbol()
    {
        return symbol;
    }

    public void goBackToStart()
    {
        distanceTraveled = 0;
        hasFallen = false;
        hasWon = false;
    }

    public boolean hasFallen()
    {
        return hasFallen;
    }

    public boolean hasWon()
    {
        return hasWon;
    }

    public void moveForward()
    {
        distanceTraveled++;
    }

    public void setConfidence(double newConfidence)
    {
        if (newConfidence < 0) {newConfidence = 0;}
        if (newConfidence > 1) {newConfidence = 1;}
        this.confidence = newConfidence;
    }

    public void setSymbol(char newSymbol)
    {
        this.symbol = newSymbol;
    }

    public void setAsWinner() {
        hasWon = true;
    }

}