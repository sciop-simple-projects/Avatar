package com.scio;

import java.util.Random;
import java.util.Scanner;

public class Main {

    // Quick and dirty CLI to showcase the class
    // The
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hello World!");
        System.out.println("Hi. Please tell me your name: ");
        String name = scanner.nextLine();
        System.out.println("Ok. Let me create an avatar for you...");
        boolean newPic = true;
        Avatar avatar = null;
        while(true) {
            if(newPic) {
                avatar = new Avatar(name);
                avatar.print();
                System.out.println("Do you like it? Do you want to save it as an svg? (Y/n)");
            }
            System.out.println("Hit enter for a new randomized avatar, anything else to make a custom one.");
            System.out.println("q exits the program.");
            name = scanner.nextLine();
            if (name == null || name.length() == 0)
            {
                name = "" + new Random().nextInt();
                newPic = true;

            }
            else if(newPic && name.equals("Y") || name.equals("y"))
            {
                if(!avatar.saveToFile(false)) System.out.println("ERROR SAVING TO FILE");
                newPic = false;
            }
            else if(newPic && (name.equals("n") || name.equals("N")))
            {
                newPic = false;
            }
            else if (name.equals("q")) return;
            else
            {
                newPic = true;
            }
        }
    }
}
