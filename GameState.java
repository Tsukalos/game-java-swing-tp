import java.awt.*;
import java.io.Serializable;

import javax.swing.*;
import java.util.*;

class GameState implements Serializable{
    public Vector<GameElement> elementList;
    public enum ObjectId{
            Bar, Block, Ball
    }
    GameState(){
        elementList = new Vector<GameElement>();
    }
}
class GameElement implements Serializable{
    boolean active = true;
    Vector2 pos;
    GameState.ObjectId id;

    GameElement(boolean active, Vector2 pos, GameState.ObjectId id){
        this.active = active;
        this.pos = pos;
        this.id = id;
    }
}