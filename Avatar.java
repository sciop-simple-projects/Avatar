package com.scio;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Avatar {
    // Class variables
    static String[] pre = new String[] { "<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\">\r\n" };
    static int[] imgSize = new int[]{8,5};
    static int[] margins = new int[]{2,0};
    static int pixel = 64;
    static String[] post = new String[]
            {
                    "<rect x=\"16\" y=\"16\" rx=\"64\" ry=\"64\" width=\"" + (pixel*imgSize[1]+32 + (imgSize[1]-1)*margins[1]) + "\" height=\"" +
                            (pixel*imgSize[0]+32 + (imgSize[0]-1)*margins[0]) + "\" style=\"fill:none;stroke:black;stroke-width:32\" />",
                    "</svg>"
            };

    // Object vars
    boolean[][] filled;
    byte[] color;
    String name;

    public Avatar(String name) {

        this.name = name;

        // Data is derived deterministically from the name, hashing it and hashing the hash if we need more bytes.
        filled = new boolean[imgSize[0]][imgSize[1]];
        color = new byte[]{0,0,0,0};

        // We need four bytes for the color and imgsize[0]*(imgsize[1]/2) bits for the shape.
        int bytesNeeded = imgSize[1]/2 + imgSize[1]%2;  // Half of the number of cols since it is symmetrical.If odd...
        bytesNeeded *= imgSize[0];                      // Multiply by number of rows
        bytesNeeded = (int)Math.ceil(bytesNeeded/8.0);    // bits -> bytes
        bytesNeeded += 4;                               // Four more bytes for the colors

        // We want a randomized, deterministically derived array of bytes.
        // We can not use new Random().nextByte() or w/e because apparently it is NOT DETERMINISTIC. Depends on the machine.
        // Like it is now we can transfer the name and the avatar carries over automatically and we just need to generate it.
        byte[] raw = new byte[bytesNeeded];
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return;
        }
        int k = 0;
        byte[] randomBytes = md.digest(this.name.getBytes());
        while(k < raw.length) {
            for (byte b : randomBytes) {
                if(k >= raw.length) break;
                raw[k] = b;
                k++;
            }
            if(k < raw.length - 1) randomBytes = md.digest(randomBytes);
        }

        //First the color
        int indexOfRaw = 0;
        for(int i = 0; i < color.length; i++)
        {
            color[i] = raw[indexOfRaw];
            indexOfRaw++;
        }

        // Then the shape
        int bitMaskThingy = 0;
        for(int i = 0; i < filled.length; i++)
        {
            for(int j = 0; j < (filled[0].length/2 + filled[0].length%2); j++)
            {
                if(bitMaskThingy == 8)
                {
                    bitMaskThingy = 0;
                    indexOfRaw++;
                }
                filled[i][j] = (((1 << bitMaskThingy) & raw[indexOfRaw]) != 0);
                filled[i][filled[0].length - j - 1] = filled[i][j];
                bitMaskThingy++;
            }
        }
    }

    public void print() {
        for (boolean[] row : filled) {
            System.out.println();
            for (boolean b : row) {
                if (b) System.out.print("\u2588");
                else System.out.print(" ");
            }
        }
        System.out.println();
    }

    public boolean saveToFile(boolean verbose) {
        String[] content;
        int len = 0;
        for (boolean[] row : filled) {
            for (boolean b : row) {
                if (b) len++;
            }
        }
        content = new String[len];
        int k = 0;

        for(int i = 0; i < filled.length; i++)
        {
            for(int j = 0; j < filled[0].length; j++)
            {
                if(filled[i][j])
                {
                    int X = pixel*j + 32 + margins[1]*j;
                    int Y = pixel*i + 32 + margins[0]*i;
                    content[k] =    "<rect x=\"" + X + "\" y=\"" + Y + "\" width=\"" + pixel + "\" height=\"" + pixel +
                                    "\" style=\"fill:rgb(" + (color[0]) + "," + (color[1]) + "," +
                                    (color[2]) + ");stroke-width=0\" />\r\n";
                    k++;
                }
            }
        }

        try {
            File file = new File(System.getProperty("user.home") + "/Pictures/" + name + ".svg");
            if (!file.exists()) {
                if(!file.createNewFile())
                {
                    if(verbose) System.out.println("Could not create a new file.");
                    return false;
                }
            }
            BufferedWriter bw = new BufferedWriter(new FileWriter(file));
            for (String s : pre) if(s != null) bw.write(s);
            for (String s : content) if(s != null) bw.write(s);
            for (String s : post) if(s != null) bw.write(s);
            bw.close();
            if(verbose) System.out.println("Successfully saved to " + file.getAbsoluteFile());
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }
}
