import java.net.Socket;

public class ClientInfo {
    private Socket socket;
    //private User user;

    public ClientInfo(Socket socket) {
        this.socket = socket;
        //this.user = null;
    }
    
    public ClientInfo(Socket socket, String username) {
        this.socket = socket;
        //this.user = new User(username);
    }

    public ClientInfo(Socket socket, String username, String password) {
        this.socket = socket;
        //this.user = new User(username, password);
    }

    public Socket getSocket() {return socket;}
    //public User getUser() {return user;}

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
    // public void setUser(String username, String password) {
    //     this.user = new User(username, password);
    // }

    public String toString() {
        return "["+ socket.getInetAddress() + ":" + socket.getPort() + "] ";// + user;
    }
}