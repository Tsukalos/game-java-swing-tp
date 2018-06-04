import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.Raster;

import javax.swing.*;
import javax.swing.Timer;


import java.io.*;
import javax.imageio.*;
import java.util.*;
import java.net.*;

class Client extends JFrame implements Runnable{
    DrawArea drawArea;
    volatile Vector<GameObject> objects;
    Socket socket;
    ObjectInputStream is;
    DataOutputStream os;
    BufferedImage ball,bar;
    Client(){
        super("Trabalho");
        try{
            socket = new Socket("127.0.0.1", 87);
            is = new ObjectInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            
        }catch(IOException e){
            System.out.println("Erro na conexão ao servidor \n"+e);
        }
        if(socket == null) return;
        try{
            ball = ImageIO.read(new File("sprites/ball.png"));
            bar = ImageIO.read(new File("sprites/bar.png"));
        }catch(IOException e){}
       

        objects = new Vector<GameObject>();
        drawArea = new DrawArea(objects);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(drawArea);
        pack();
        setVisible(true);
        new Thread(this).start();

    }
    class DrawArea extends JPanel implements ActionListener{
        Vector<GameObject> os;
        DrawArea(Vector<GameObject> os){
            this.os = os;
            Timer timer = new Timer(40, this);
            timer.setCoalesce(true);
            timer.start();
            setDoubleBuffered(true);
            setLayout(null);
        }

        public void paintComponent(Graphics g){
            super.paintComponent(g);
            GameObject[] a =  objects.toArray(new GameObject[objects.size()]);
            for(int i = 0; i < a.length; i++){
                a[i].Draw(g);
            }
        }


        public Dimension getPreferredSize() {
            return new Dimension(800, 600);
        }

        public void actionPerformed(ActionEvent e){
            repaint();
        }

    }

    public static void main(String[] args) {
        Client c = new Client();
    }

    public void run(){
        while(!socket.isClosed()){
            try {
                GetElements((GameState)is.readObject());
            } catch (Exception e) {
                System.out.println(e);
                try{
                    socket.close();
                    socket = new Socket("127.0.0.1", 87);
                    if(!socket.isConnected()) break;
                }catch(IOException er){
                    System.out.println(er);
                }
            }
        }
        System.out.println("Close");
    }

    void GetElements(GameState state){
        objects.clear();
        for(GameElement var : state.elementList) {
            BufferedImage i = null;
            if(var.id == GameState.ObjectId.Bar){
                i = bar;
            }
            if(var.id == GameState.ObjectId.Ball){
                i = ball;
            }
            objects.add(new GameObject(var.pos, i, this));
        }
    }


}

class GameObject{
    public Vector2 position;
    public BufferedImage sprite;
    ImageObserver observer;

    GameObject(Vector2 position, String image, ImageObserver observer){
        this.position = position;
        LoadImage(image);
        this.observer = observer;
    }

    GameObject(Vector2 position, BufferedImage image, ImageObserver observer){
        this.position = position;
        sprite = image;
        this.observer = observer;
    }

    void LoadImage(String path){
        try{
            sprite = ImageIO.read(new File(path));
        }catch(IOException e){
            	System.out.println(e.getMessage());
        }
    }

    void Draw(Graphics g){
        g.drawImage(sprite, position.x, position.y, observer);
    }
}

class Vector2 implements Serializable{
    public int x,y;

    Vector2(){
        x = 0;
        y = 0;
    }

    Vector2(int x, int y){
        this.x = x;
        this.y = y;
    }
}

