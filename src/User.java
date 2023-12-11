import java.io.IOException;
import java.io.Serializable;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import java.util.Scanner;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;


public class User implements Serializable{
    private String username;
    private String password;

    public User(String username) {
        this.username = username;
        this.password = null;
    }

    public User(String username, String password) {
        this.username = username;
        this.password = hashPassword(password);
    }

    public String getUsername() {return username;}
    public String getPassword() {return password;}

    public void setUsername(String username) {
        this.username = username;
    }

    public void updatePassword(String password) {
        if (this.password != null) {
            Scanner sc = new Scanner(System.in);
            System.out.println("confirm current password :");
            if (checkPassword(sc.nextLine())) {
                System.out.println("enter new password :");
                this.password = hashPassword(sc.nextLine());
                System.out.println("password updated");
            } else {
                System.out.println("wrong password");
            }
            sc.close();
        } else {
            this.password = hashPassword(password);
        }
    }

    public boolean checkPassword(String password) {
        return this.password.equals(hashPassword(password));
    }

    private String hashPassword(String password) {
        return Integer.toString(password.hashCode());
    }

    public String toString() {
        return "["+ username + "] " + password;
    }

    public boolean save(String path) {
        boolean result = false;
        try {
            FileOutputStream fos = new FileOutputStream(path);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            oos.writeObject(this);

            oos.close();
            fos.close();
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static boolean delete(String path) {
        boolean result = false;
        Path p = Paths.get(path);
        try {
            Files.deleteIfExists(p);
            result = true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static User load(String path) {
        User user = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            ObjectInputStream ois = new ObjectInputStream(fis);

            user = (User) ois.readObject();

            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return user;
    }
}
