import java.net.*;
import java.awt.*;
import java.io.*;
import java.util.*;

class Server{
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Player p1 = null;
        Player p2 = null;
        try {
            serverSocket = new ServerSocket(87);
        } catch (IOException e) {}

        for(int i = 0; i < 2; i++){
            try{
                Socket s = serverSocket.accept();
                if(p1 == null){
                    p1 = new Player(s,i);
                    System.out.println("P1 OK");
                }else{
                    p2 = new Player(s,i);
                    System.out.println("P2 OK");
                }
            }catch(IOException e){}
        }
        new Game(p1,p2).start();
        System.out.println("Done");
        try{
            serverSocket.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}

class Player extends Thread{
    public Socket socket;
    public int id;
    public int input;
    public ObjectOutputStream os;
    public DataInputStream is;
    Player(Socket socket, int id){
        this.socket = socket;
        this.id = id;
        try{
            is = new DataInputStream(socket.getInputStream());
            os = new ObjectOutputStream(socket.getOutputStream());
        }catch(IOException e){}

    }

    public void run(){
        try{
            input = (int) is.readInt();
        }catch(Exception e){}
    }


}

class Game extends Thread{
    Player p1;
    Player p2;
    GameState currentState;

    Rectangle P1bar, P2bar;
    Rectangle Ball;
    float[] ballDirection = new float[2];

    Random rand = new Random();


    Game(Player p1, Player p2){
        this.p1 = p1;
        this.p2 = p2;
    }

    public void run(){
        InitGame();
        do{



            UpdateBall();
            UpdateList();
            try{
                sleep(60);
            }catch(Exception e){}
        }while(SendData());
    }

    void InitGame(){
        currentState = new GameState();
        Vector<GameElement> initList = new Vector<GameElement>();
        //initList.add(new GameElement(true, new Vector2(10,10), GameState.ObjectId.Block));
        //initList.add(new GameElement(true, new Vector2(100,300), GameState.ObjectId.Block));

        

        P1bar = new Rectangle(800/2 - 75, 10, 150, 50);
        P2bar = new Rectangle(800/2 - 75, 600 - 10 - 50, 150, 50);

        ballDirection[0] = (rand.nextFloat()*2f - 1f) * 2f;
        ballDirection[1] = (rand.nextFloat()*2f - 1f) * 2f;
        Ball = new Rectangle(800/2 - 20,600/2 - 20,20,20);

        currentState.elementList = initList;
    }

    boolean SendData(){
        try{
            p1.os.writeObject((GameState)currentState);
            p2.os.writeObject((GameState)currentState);
            p1.os.reset();
            p2.os.reset();
            return true;
        }catch(Exception e){
            System.out.println(e);
            return false;
        }
    }

    void UpdateList(){
        currentState.elementList.clear();
        //update bola, players
        System.out.println(Ball);
        currentState.elementList.add(new GameElement(true, new Vector2(Ball.x,Ball.y), GameState.ObjectId.Ball));
        currentState.elementList.add(new GameElement(true, new Vector2(P1bar.x,P1bar.y), GameState.ObjectId.Bar));
        currentState.elementList.add(new GameElement(true, new Vector2(P2bar.x,P2bar.y), GameState.ObjectId.Bar));
        
    }

    void UpdateBall(){
        Ball.x = (int)(Ball.x + ballDirection[0]);
        Ball.y = (int)(Ball.y + ballDirection[1]);
        
    }

}

