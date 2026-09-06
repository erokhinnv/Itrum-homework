package org.example;

public class App 
{
    public static void main( String[] args ) {
        MyStringBuilder myStringBuilder = new MyStringBuilder();
        myStringBuilder.append("New");
        myStringBuilder.append("York");
        myStringBuilder.append("This message is to be canceled");
        myStringBuilder.undo();
        System.out.println(myStringBuilder.toString());
    }
}
