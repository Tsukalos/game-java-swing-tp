import java.awt.*;
import java.awt.event.*;
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
    BufferedReader in;
    BufferedWriter out;
    int playerInput = 0;
    BufferedImage ball,bar,block;
    JLabel scoreP1, scoreP2;
    Client(){
        super("Trabalho");
        try{
            socket = new Socket("127.0.0.1", 87);

            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            
        }catch(IOException e){
            System.out.println("Erro na conexão ao servidor \n"+e);
        }
        if(socket == null) return;
        try{
            ball = ImageIO.read(new File("sprites/ball.png"));
            bar = ImageIO.read(new File("sprites/bar.png"));
            block = ImageIO.read(new File("sprites/block.png"));
        }catch(IOException e){}
       

        objects = new Vector<GameObject>();
        drawArea = new DrawArea(objects);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        add(drawArea);
        pack();
        addKeyListener(new KeyAdapter(){
            public void keyPressed(KeyEvent e) {
                switch(e.getKeyCode()){
                    case KeyEvent.VK_RIGHT:
                        playerInput = 1;
                    break;
                    case KeyEvent.VK_LEFT:
                        playerInput = -1;
                    break;
                }
            }
        });
        Timer t1 = new Timer(200, new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                try{
                    out.write(String.valueOf(playerInput)+"\n");
                    out.flush();
                }catch(IOException er){}
            }
        });
        t1.start();
        setVisible(true);
        new Thread(this).start();

    }
    class DrawArea extends JPanel implements ActionListener{
        Vector<GameObject> os;
        DrawArea(Vector<GameObject> os){
            this.os = os;
            Timer timer = new Timer(40, this);
            scoreP1 = new JLabel("0");
            scoreP1.setBounds(400-25, 600/2 - 200, 100, 100);
            scoreP1.setFont(new Font("", Font.BOLD, 100));
            scoreP2 = new JLabel("0"); 
            scoreP2.setBounds(400-25, 600/2 + 100, 100, 100);
            scoreP2.setFont(new Font("", Font.BOLD, 100));
            this.add(scoreP1);
            this.add(scoreP2);
            timer.setCoalesce(true);
            timer.start();
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
                if(in.ready()){
                    GetElements(in);
                }
            } catch (Exception e) {
                System.out.println(e);
                try{
                    socket.close();
                    socket = new Socket("127.0.0.1", 87);
                    if(!socket.isConnected()){
                        socket.close();
                        break;
                    } 
                }catch(IOException er){
                    System.out.println(er);
                }
            }
        }
        System.exit(1);
        System.out.println("Close");
    }

    void GetElements(BufferedReader in) throws Exception{
        String a;
        StringTokenizer st = new StringTokenizer(a = in.readLine()," ");
        if(a.contentEquals("Point 1")||a.contentEquals("Point 2")){
            System.out.println("a");
            UpdateScore(Integer.parseInt(String.valueOf(a.charAt(a.length()-1))));
        }else{
            String type, posx, posy, active = null;
            BufferedImage i = null;
            objects.clear();
            while(st.hasMoreTokens()){
                type = st.nextToken();
                posx = st.nextToken();
                posy = st.nextToken();
                active = st.nextToken();

                switch(type){
                    case "Ball":
                        i = ball;
                    break;
                    case "Bar":
                        i = bar;
                    break;
                    case "Block":
                        i = block;
                    break;
                }
                objects.add(new GameObject(new Vector2(Integer.parseInt(posx), Integer.parseInt(posy)), i, this));
            }
        }
    }

    void UpdateScore(int player){
        if(player == 1){
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    int a = Integer.parseInt(scoreP1.getText());
                    a++;
                    scoreP1.setText(String.valueOf(a));
                    playerInput = 0;
                    repaint();
                }
            });
        }else{
            SwingUtilities.invokeLater(new Runnable(){
                public void run() {
                    int a = Integer.parseInt(scoreP2.getText());
                    a++;
                    scoreP2.setText(String.valueOf(a));
                    playerInput = 0;
                    repaint();
                }
            });
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

