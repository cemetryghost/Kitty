package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Heart {
    Rectangle rectangle;
    Texture texture;
    public Heart(Rectangle rectangle, Texture texture){
        this.rectangle = rectangle;
        this.texture = texture;
    }
}
