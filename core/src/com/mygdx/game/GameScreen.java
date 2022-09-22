package com.mygdx.game;

import java.util.Iterator;


import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

public class GameScreen implements Screen {
    final Drop game;

    Texture kuromi;
    Texture myMelody;
    Texture helloKitty;
    Texture blackHeart;
    Texture pinkHeart;
    Texture redHeart;
    Texture bucketImage;

    TextureRegion background;

    Sound dropSound;
    Music gameMusic;
    OrthographicCamera camera;
    Rectangle bucket;
    Array<Heart> raindrops;
    long lastDropTime;
    int dropsGathered;

    public GameScreen(final Drop game) {
        this.game = game;
        background = new TextureRegion(new Texture("background.jpg"), 0, 0, 1920, 1080);
        // загрузка изображений для капли и ведра, 64x64 пикселей каждый
        redHeart = new Texture(Gdx.files.internal("red_heart.png"));
        blackHeart = new Texture(Gdx.files.internal("black_Heart.png"));
        pinkHeart = new Texture(Gdx.files.internal("pink_heart.png"));
        helloKitty = new Texture(Gdx.files.internal("hello_kitty.png"));
        kuromi = new Texture(Gdx.files.internal("kuromi.png"));
        myMelody = new Texture(Gdx.files.internal("mymelody.png"));
        bucketImage = helloKitty;

        // загрузка звукового эффекта падающей капли и фоновой "музыки" дождя
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drops.wav"));
        gameMusic = Gdx.audio.newMusic(Gdx.files.internal("soundtrack.mp3"));
        gameMusic.setLooping(true);

        // создает камеру
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 1920, 1080);

        // создается Rectangle для представления ведра
        bucket = new Rectangle();
        // центрируем ведро по горизонтали
        bucket.x = 1920 / 2 - 64 / 2;
        // размещаем на 20 пикселей выше нижней границы экрана.
        bucket.y = 20;

        bucket.width = 64;
        bucket.height = 64;

        // создает массив капель и возрождает первую
        raindrops = new Array<Heart>();
        spawnRaindrop();

    }

    private void spawnRaindrop() {
        Texture hearts;
        Rectangle raindrop = new Rectangle();
        raindrop.x = MathUtils.random(0, 1920 - 64);
        raindrop.y = 1080;
        raindrop.width = 64;
        raindrop.height = 64;

        if(dropsGathered <= 15){
            hearts = redHeart;
        }
        else if (dropsGathered > 15 && dropsGathered <= 30){
            hearts = blackHeart;
        }
        else{
            hearts = pinkHeart;
        }

        raindrops.add(new Heart(raindrop, hearts));
        lastDropTime = TimeUtils.nanoTime();
    }

    @Override
    public void render(float delta) {
        // очищаем экран темно-синим цветом.
        // Аргументы для glClearColor красный, зеленый
        // синий и альфа компонент в диапазоне [0,1]
        // цвета используемого для очистки экрана.


        // сообщает камере, что нужно обновить матрицы.
        camera.update();

        // сообщаем SpriteBatch о системе координат
        // визуализации указанных для камеры.
        game.batch.setProjectionMatrix(camera.combined);

        // начитаем новую серию, рисуем ведро и
        // все капли
        game.batch.begin();
        game.batch.draw(background, 0, 0);
        game.font.draw(game.batch, "Drops Collected: " + dropsGathered, 0, 1080);
        game.batch.draw(bucketImage, bucket.x, bucket.y);
        for (Heart heart : raindrops) {
            game.batch.draw(heart.texture, heart.rectangle.x, heart.rectangle.y);
        }
        game.batch.end();

        // обработка пользовательского ввода
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            bucket.x = touchPos.x - 64 / 2;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT))
            bucket.x -= 200 * Gdx.graphics.getDeltaTime();
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT))
            bucket.x += 200 * Gdx.graphics.getDeltaTime();

        // убедитесь, что ведро остается в пределах экрана
        if (bucket.x < 0)
            bucket.x = 0;
        if (bucket.x > 1920 - 64)
            bucket.x = 1920 - 64;

        // проверка, нужно ли создавать новую каплю
        if (TimeUtils.nanoTime() - lastDropTime > 1000000000)
            spawnRaindrop();


        // движение капли, удаляем все капли выходящие за границы экрана
        // или те, что попали в ведро. Воспроизведение звукового эффекта
        // при попадании.
        Iterator<Heart> iter = raindrops.iterator();
        while (iter.hasNext()) {
            Heart raindrop = iter.next();
            raindrop.rectangle.y -= 200 * Gdx.graphics.getDeltaTime();
            if (raindrop.rectangle.y + 64 < 0)
                iter.remove();
            if (raindrop.rectangle.overlaps(bucket)) {
                dropsGathered++;
                dropSound.play();
                iter.remove();
            }
            if (dropsGathered > 15 && dropsGathered <= 30){
                bucketImage = kuromi;
            }
            else if(dropsGathered > 30){
                bucketImage = myMelody;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // воспроизведение фоновой музыки
        // когда отображается экрана
        gameMusic.play();
    }

    @Override
    public void hide() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        kuromi.dispose();
        myMelody.dispose();
        helloKitty.dispose();
        blackHeart.dispose();
        pinkHeart.dispose();
        redHeart.dispose();
        bucketImage.dispose();
        dropSound.dispose();
        gameMusic.dispose();
    }

}
