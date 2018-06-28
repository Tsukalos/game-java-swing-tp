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

    public BufferedReader in;
    public BufferedWriter out;
    Player(Socket socket, int id){
        this.socket = socket;
        this.id = id;
        try{
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }catch(IOException e){}

    }

    public void run(){
        do{
            try{
                if(in.ready()){
                    input = Integer.parseInt(in.readLine());
                    sleep(67);
                }
            }catch(Exception e){}
        }while(!socket.isClosed());
        System.exit(1);
            
    }


}

class Game extends Thread{
    Player p1;
    Player p2;
    StringBuffer sb;

    Rectangle P1bar, P2bar;
    Rectangle Ball;
    final int blockNum = 4;
    Block[] blocks = new Block[blockNum];
    float[] ballDirection = new float[2];
    float ballSpeed = 10F;
    int barSpeed = 5;

    Random rand = new Random();


    Game(Player p1, Player p2){
        sb = new StringBuffer();
        this.p1 = p1;
        this.p2 = p2;
    }

    public void run(){
        InitGame();
        do{
            sb.delete(0, sb.length());
            UpdateBars();
            UpdateBall();
            UpdateList();
            try{
                // Server Thread
                sleep(36);
            }catch(Exception e){}
        }while(SendData());
    }

    void InitGame(){

        

        P1bar = new Rectangle(800/2 - 75, 10, 150, 32);
        P2bar = new Rectangle(800/2 - 75, 600 - 10 - 32, 150, 32);
        for(int i = 0; i < blockNum;i++){
            blocks[i] = new Block((800/(2*blockNum)+(i*(800/blockNum))) - 25, 600/2 - 32);
        }

        ballDirection[0] = (rand.nextFloat()*2f - 1f);
        ballDirection[1] = (rand.nextFloat()*2f - 1f);
        Ball = new Rectangle((int)800/2 - 20,(int)600/2 - 20,20,20);
        new Thread(p1).start();
        new Thread(p2).start();
        //currentState.elementList = initList;
        try {
            for(int i = 5; i > 0; i--){
                System.out.println("Start in "+i+" s...");
                sleep(1000);
            }
        } catch (Exception e) {}
    }

    boolean SendData(){
        try{
            //String b = PrepareStreams(currentState);
            p1.out.write(sb.toString());
            p2.out.write(sb.toString());
            p1.out.flush();
            p2.out.flush();
            return true;
        }catch(Exception e){
            try{
                p1.socket.close();
                p2.socket.close();
            }catch(IOException err){}
            
            System.out.println(e);
            return false;
        }
    }



    void UpdateList(){
        //update bola, players
        sb.append("Ball"+" "+Ball.x+" "+Ball.y+" "+1+" ");
        sb.append("Bar"+" "+P1bar.x+" "+P1bar.y+" "+1+" ");
        sb.append("Bar"+" "+P2bar.x+" "+P2bar.y+" "+1+" ");

        for(int i = 0; i < blockNum; i++){
            if(blocks[i].active)
                sb.append("Block"+" "+blocks[i].rect.x+" "+blocks[i].rect.y+" "+1+" ");
        }
        sb.append("\n");
        
    }

    void UpdateBall(){
        Ball.x = (int)(Ball.x + ballDirection[0]*ballSpeed);
        Ball.y = (int)(Ball.y + ballDirection[1]*ballSpeed);
        
        if(Ball.x <= 0 || Ball.x+Ball.width >= 800)
            ballDirection[0] = -ballDirection[0];
        if(Ball.y <= 0 || Ball.y+Ball.height >= 600)
            ballDirection[1] = -ballDirection[1];
        if(BallCol(P1bar)){
            ballDirection[0] = (Math.signum(ballDirection[0])!=Math.signum((float)p1.input)) ? -ballDirection[0] : ballDirection[0];
        }
        if(BallCol(P2bar)){
            ballDirection[0] = (Math.signum(ballDirection[0])!=Math.signum((float)p2.input)) ? -ballDirection[0] : ballDirection[0];
        }

        for(int i = 0; i < blockNum; i++){
            if(blocks[i].active){
                if(BallCol(blocks[i].rect)){
                    blocks[i].active = false;
                }
            }
        }
    }

    boolean BallCol(Rectangle b){
        switch(ColRect(Ball, b)){
            case Bottom: ballDirection[1] = -ballDirection[1];
            return true;
            case Top: ballDirection[1] = -ballDirection[1];
            return true;
            case Right: ballDirection[0] = -ballDirection[0];
            return true;
            case Left: ballDirection[0] = -ballDirection[0];
            return true;
            case None: return false;
        }
        return false;
    }

    Collision ColRect(Rectangle A, Rectangle B){
        float w = 0.5F * (A.width + B.width);
        float h = 0.5F * (A.height + B.height);
        float dx =(float)(A.getCenterX() - B.getCenterX());
        float dy =(float)(A.getCenterY() - B.getCenterY());
        if(Math.abs(dx) <= w && Math.abs(dy) <= h){
            float wy = w * dy;
            float hx = h * dx;
            if (wy > hx){
                if(wy > -hx){
                    return Collision.Top;    
                }else{
                    return Collision.Left;
                }
            }else{
                if(wy > -hx){
                    return Collision.Right;
                }else{
                    return Collision.Bottom;
                }
            }
        }
        return Collision.None;
    }

    public enum Collision {
        Right, Top, Bottom, None, Left
    };


    void UpdateBars(){
        P1bar.x += (int)p1.input*barSpeed;
        if(P1bar.x < 0) 
            P1bar.x = 0;
        if(P1bar.y < 0)
            P1bar.y = 0;
        if(P1bar.x + P1bar.width > 800)
            P1bar.x = 800 - P1bar.width;
        if(P1bar.y + P1bar.height > 600)
            P1bar.y = 600 - P1bar.height;
        P2bar.x += (int)p2.input*barSpeed;
        if(P2bar.x < 0) 
            P2bar.x = 0;
        if(P2bar.y < 0)
            P2bar.y = 0;
        if(P2bar.x + P2bar.width > 800)
            P2bar.x = 800 - P2bar.width;
        if(P2bar.y + P2bar.height > 600)
            P2bar.y = 600 - P2bar.height;
    }

    class Block{
        Rectangle rect;
        boolean active = true;
            Block(int x, int y){
                rect = new Rectangle(x, y, 50, 32);
            }
    }


}

