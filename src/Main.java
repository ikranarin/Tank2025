import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import javafx.animation.FadeTransition;
import javafx.util.Duration;


/**
 * Tank2025 - A JavaFX based tank battle game.
 * <p>
 * This application features a player-controlled tank navigating and fighting enemy tanks
 * in a maze-like environment. The game includes features such as scoring, multiple lives,
 * explosions, a pause menu, and respawning enemies. The player uses arrow keys to move,
 * 'X' to shoot, 'R' to restart, 'P' to pause, and 'ESC' to exit.
 * </p>
 */
public class Main extends Application {
    /** Current player score */
    private int score = 0;

    /** Text display for current score */
    private javafx.scene.text.Text scoreText;

    /** Constant tile size used for grid layout */
    public static final int TILE_SIZE = 18;

    /** Game window width */
    public static final int WIDTH = TILE_SIZE * 55;

    /** Game window height */
    public static final int HEIGHT = TILE_SIZE * 45;

    /** Remaining lives of the player */
    private int lives = 3;

    private ArrayList<ImageView> heartIcons = new ArrayList<>();


    /** Flag indicating whether the game is over */
    private boolean isGameOver = false;

    /** Text element for displaying game over screen */
    private javafx.scene.text.Text gameOverText;

    /** Flag indicating whether the game is paused */
    private boolean isPaused = false;

    /** Text element for displaying pause screen */
    private Text pauseText;


    /** List of enemy tank objects */
    private ArrayList<ImageView> enemies = new ArrayList<>();

    /** HashMap storing enemy movement velocities */
    private HashMap<ImageView, Point2D> enemyVelocities = new HashMap<>();

    /** HashMap tracking which enemies are alive */
    private HashMap<ImageView, Boolean> enemyAlive = new HashMap<>();

    /** Player's tank object */
    private ImageView tank;
    private boolean playerIsMoving = false;

    /** List of player bullets */
    private ArrayList<ImageView> bullets = new ArrayList<>();

    /** List of enemy bullets */
    private ArrayList<ImageView> enemyBullets = new ArrayList<>();

    /** Player bullet movement speed */
    private final double BULLET_SPEED = 3.0;

    /** Enemy bullet movement speed */
    private final double ENEMY_BULLET_SPEED = 1.5;

    /** Map grid (0 = empty, 1 = wall) */
    private int[][] map;

    private boolean isGameStarted = false;

    private Text startText;

    private Pane startRoot;
    private Scene startScene;
    private Scene gameScene;

    private Stage primaryStage;




    /**
     * Launches the JavaFX application and sets up the main stage.
     * Initializes the game world including map, player tank, enemies, and UI elements.
     *
     * @param primaryStage the main stage for this application
     */

    @Override
    public void start(Stage primaryStage) {

        this.primaryStage = primaryStage;

        Font pressStartFont = Font.loadFont(getClass().getResourceAsStream("PressStart2P.ttf"), 36);


        // Start Screen Root
        startRoot = new Pane();
        startRoot.setPrefSize(WIDTH, HEIGHT);

        // Arka plan resmi
        Image backgroundImage = new Image("arkaplan1.png");
        ImageView backgroundView = new ImageView(backgroundImage);
        backgroundView.setFitWidth(WIDTH);
        backgroundView.setFitHeight(HEIGHT);

        startText = new Text("Press X to Start");
        startText.setFont(Font.font("Arial", FontWeight.EXTRA_BOLD, 36));
        startText.setFill(Color.GOLD);
        startText.setStroke(Color.BLACK);
        startText.setStrokeWidth(2);
        startText.setTextAlignment(TextAlignment.CENTER);
        startText.setWrappingWidth(WIDTH);
        startText.setY(HEIGHT / 2.0);
        startText.setX(0);


        FadeTransition fade = new FadeTransition(Duration.seconds(1), startText);
        fade.setFromValue(1.0);
        fade.setToValue(0.3);
        fade.setCycleCount(FadeTransition.INDEFINITE);
        fade.setAutoReverse(true);
        fade.play();

        // Önce arka planı ekle
        startRoot.getChildren().add(backgroundView);

        // Oyun başlığı
        Text titleText = new Text("TANK GAME");
        titleText.setFont(pressStartFont);
        titleText.setFill(Color.DARKRED);
        titleText.setStroke(Color.WHITE);
        titleText.setStrokeWidth(3);
        titleText.setTextAlignment(TextAlignment.CENTER);
        titleText.setWrappingWidth(WIDTH);
        titleText.setY(HEIGHT / 2.0 - 100); // startText'in üstünde olsun
        titleText.setX(0);

        Text creditText = new Text("Made by Ikra Narin Soran");
        creditText.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        creditText.setFill(Color.WHITE);
        creditText.setOpacity(0.6); // Şeffaflık veriyoruz
        creditText.setX(WIDTH - 180); // Sağdan boşluk
        creditText.setY(HEIGHT - 15); // Alttan boşluk
        startRoot.getChildren().add(creditText);


        // Başlangıç ekranına ekle
        startRoot.getChildren().add(titleText);

        startRoot.getChildren().add(startText);

        // Start Scene
        startScene = new Scene(startRoot);

        // Start Scene Key Controls
        startScene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case X:
                    if (!isGameStarted) {
                        isGameStarted = true;
                        primaryStage.setScene(gameScene);
                    }
                    break;
                case ESCAPE:
                    System.exit(0);
                    break;
                default:
                    break;
            }
        });

        // Tank images for animation
        Image yellowTank1 = new Image("yellowTank1.png");

        Pane gameRoot = new Pane();
        gameRoot.setPrefSize(WIDTH, HEIGHT);
        gameRoot.setStyle("-fx-background-color: #040444;");

        Image wallImage = new Image("wall.png");
        Image tankImage = new Image("yellowTank1.png");
        Image enemyImage = new Image("whiteTank1.png");

        int COLS = WIDTH / TILE_SIZE;
        int ROWS = HEIGHT / TILE_SIZE;
        map = new int[ROWS][COLS];

        // Place map borders and interior walls
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                map[row][col] = (row == 0 || row == ROWS - 1 || col == 0 || col == COLS - 1) ? 1 : 0;
            }
        }

        int centerRow = ROWS / 2 - 3;
        for (int i = COLS / 2 - 4; i <= COLS / 2 + 4; i++) map[centerRow][i] = 1;
        for (int i = centerRow + 5; i <= centerRow + 16; i++) {
            map[i][COLS / 4 - 2] = 1; map[i][COLS / 4 + 2] = 1;
            map[i][COLS * 3 / 4 - 3] = 1; map[i][COLS * 3 / 4 + 1] = 1;
        }

        // Place walls on stage
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                if (map[row][col] == 1) {
                    ImageView wall = new ImageView(wallImage);
                    wall.setX(col * TILE_SIZE);
                    wall.setY(row * TILE_SIZE);
                    wall.setFitWidth(TILE_SIZE);
                    wall.setFitHeight(TILE_SIZE);
                    gameRoot.getChildren().add(wall);
                }
            }
        }

        // Player's tank
        tank = new ImageView(yellowTank1);
        tank.setFitWidth(TILE_SIZE * 1.7);
        tank.setFitHeight(TILE_SIZE * 1.7);
        tank.setX(WIDTH / 2);
        tank.setY(HEIGHT / 2);
        gameRoot.getChildren().add(tank);

        // Create 6 initial enemies
        for (int i = 0; i < 6; i++) {
            respawnEnemy(gameRoot);
        }

        // Oyunun sahnesini tanımlıyoruz
        gameScene = new Scene(gameRoot);


        // Oyun kontrollerini bu sahneye bağlıyoruz
        setupControls(gameScene, gameRoot);
        setupEnemy(gameRoot);


        // Artık başlangıç sahnesini açıyoruz
        primaryStage.setScene(startScene);


        // Text showing the player's score is created and placed in the top left corner
        scoreText = new Text(40, 50, "Score: 0");
        scoreText.setStyle("-fx-font-size: 20px; -fx-fill: white; -fx-font-weight: bold;");
        gameRoot.getChildren().add(scoreText);

        // Text showing the player's life count in the top left
        Image heartImage = new Image("heart.png");
        for (int i = 0; i < lives; i++) {
            ImageView heart = new ImageView(heartImage);
            heart.setFitWidth(25);
            heart.setFitHeight(25);
            heart.setX(40 + i * 30); // 30 px aralıkla
            heart.setY(70);
            gameRoot.getChildren().add(heart);
            heartIcons.add(heart);
        }


        // The \"Game Over\" text that will be displayed when the game is over is created and placed centered on the stage
        gameOverText = new Text();
        gameOverText.setStyle("-fx-font-size: 32px; -fx-fill: red; -fx-font-weight: bold;");
        gameOverText.setTextAlignment(TextAlignment.CENTER);
        gameOverText.setWrappingWidth(WIDTH); // Cover the entire scene
        gameOverText.setY(HEIGHT / 2.0); // Center vertically
        gameOverText.setX(0); // start from left but will be displayed centered
        gameRoot.getChildren().add(gameOverText);

        // Text for the pause menu is created and added centered on the stage
        pauseText = new Text();
        pauseText.setStyle("-fx-font-size: 32px; -fx-fill: pink; -fx-font-weight: bold;");
        pauseText.setTextAlignment(TextAlignment.CENTER);
        pauseText.setWrappingWidth(WIDTH);
        pauseText.setY(HEIGHT / 2.0 + 50);
        pauseText.setX(0);
        gameRoot.getChildren().add(pauseText);

        startEnemyAnimation();

        primaryStage.setScene(startScene);
        primaryStage.setTitle("Tank 2025");
        primaryStage.show();

        setupPlayerTankAnimation();
    }

    /**
     * Sets up player controls and game interaction via keyboard input.
     * Handles movement, shooting, pausing, restarting, and collision detection.
     *
     * @param scene the main game scene
     * @param gameRoot the root pane of the game
     */

    private void setupControls(Scene scene, Pane gameRoot) {
        scene.setOnKeyPressed(event -> {
            double currentX = tank.getX();
            double currentY = tank.getY();
            double newX = currentX;
            double newY = currentY;
            double angle = tank.getRotate();

            switch (event.getCode()) {
                case UP:
                    newY -= TILE_SIZE;
                    angle = -90;
                    break;
                case DOWN:
                    newY += TILE_SIZE;
                    angle = 90;
                    break;
                case LEFT:
                    newX -= TILE_SIZE;
                    angle = 180;
                    break;
                case RIGHT:
                    newX += TILE_SIZE;
                    angle = 0;
                    break;
                case R:
                    if (!isGameStarted || isGameOver || isPaused) {
                        restartGame(gameRoot);
                    }
                    return;
                case P:
                    if (!isGameOver) {
                        isPaused = !isPaused;
                        pauseText.setText(isPaused ? "PAUSED\nPress R to Restart or ESC to Exit" : "");
                    }
                    return;
                case ESCAPE:
                    System.exit(0);
                    return;
                case X:
                    if (!isGameStarted) {
                        isGameStarted = true;
                        primaryStage.setScene(gameScene); // <-- Start ekranını kapat, oyun ekranına geç
                    } else {
                        shootBullet(gameRoot);
                    }
                    return;

                default:
                    return;
            }

            boolean isValidMove = true;

            double left = newX;
            double right = newX + tank.getFitWidth();
            double top = newY;
            double bottom = newY + tank.getFitHeight();

            int leftCol = (int) (left / TILE_SIZE);
            int rightCol = (int) ((right - 1) / TILE_SIZE);
            int topRow = (int) (top / TILE_SIZE);
            int bottomRow = (int) ((bottom - 1) / TILE_SIZE);

            for (int row = topRow; row <= bottomRow; row++) {
                for (int col = leftCol; col <= rightCol; col++) {
                    if (row < 0 || row >= map.length || col < 0 || col >= map[0].length || map[row][col] != 0) {
                        isValidMove = false;
                        break;
                    }
                }
                if (!isValidMove) break;
            }



            if (isValidMove && (newX != currentX || newY != currentY)) {
                tank.setX(newX);
                tank.setY(newY);
                tank.setRotate(angle);
                playerIsMoving = true;

                // Collision control with enemies
                for (ImageView enemy : enemies) {
                    if (enemyAlive.get(enemy) && tank.getBoundsInParent().intersects(enemy.getBoundsInParent())) {
                        // Player dies
                        showTankExplosion(gameRoot, tank.getX(), tank.getY());
                        handlePlayerHit(gameRoot);

                        // Enemy dies
                        gameRoot.getChildren().remove(enemy);
                        enemyAlive.put(enemy, false);
                        score++;
                        scoreText.setText("Score: " + score);
                        showTankExplosion(gameRoot, enemy.getX(), enemy.getY());

                        playerIsMoving = false;

                        return;
                    }
                }

            } else {
                playerIsMoving = false;
            }

        });

        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case UP:
                case DOWN:
                case LEFT:
                case RIGHT:
                    playerIsMoving = false;
                    break;
                default:
                    break;
            }
        });

    }


    /**
     * Fires a bullet from the player's tank in the current direction.
     * Creates a bullet with proper velocity based on tank rotation and handles
     * collision detection with walls and enemies.
     *
     * @param gameRoot the root pane of the game
     */
    private void shootBullet(Pane gameRoot) {
        double bulletX = tank.getX() + tank.getFitWidth() / 2;
        double bulletY = tank.getY() + tank.getFitHeight() / 2;

        Image bulletImage = new Image("bullet.png");
        ImageView bullet = new ImageView(bulletImage);
        bullet.setFitWidth(8);
        bullet.setFitHeight(8);
        bullet.setRotate(tank.getRotate());
        bullet.setTranslateX(bulletX - 4);
        bullet.setTranslateY(bulletY - 4);
        gameRoot.getChildren().add(bullet);
        bullets.add(bullet);

        final Point2D velocity;
        double angle = tank.getRotate();

        switch ((int) angle) {
            case 0:
                velocity = new Point2D(BULLET_SPEED, 0);
                break;
            case 90:
            case -270:
                velocity = new Point2D(0, BULLET_SPEED);
                break;
            case 180:
            case -180:
                velocity = new Point2D(-BULLET_SPEED, 0);
                break;
            case -90:
            case 270:
                velocity = new Point2D(0, -BULLET_SPEED);
                break;
            default:
                velocity = new Point2D(0, 0);
                break;
        }

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isGameStarted || isPaused || isGameOver) return;

                bullet.setTranslateX(bullet.getTranslateX() + velocity.getX());
                bullet.setTranslateY(bullet.getTranslateY() + velocity.getY());

                int col = (int) (bullet.getTranslateX() / TILE_SIZE);
                int row = (int) (bullet.getTranslateY() / TILE_SIZE);

                if (col < 0 || row < 0 || row >= map.length || col >= map[0].length || map[row][col] == 1) {
                    gameRoot.getChildren().remove(bullet);
                    bullets.remove(bullet);
                    stop();
                    showExplosion(gameRoot, bullet.getTranslateX(), bullet.getTranslateY());
                    return;
                }

                for (ImageView enemy : enemies) {
                    if (enemyAlive.get(enemy) && enemy.getBoundsInParent().intersects(bullet.getBoundsInParent())) {
                        gameRoot.getChildren().remove(bullet);
                        bullets.remove(bullet);
                        gameRoot.getChildren().remove(enemy);
                        enemyAlive.put(enemy, false);
                        score++;
                        scoreText.setText("Score: " + score);

                        // Create new enemy after 3 seconds
                        new AnimationTimer() {
                            private long startTime = -1;
                            @Override
                            public void handle(long now) {
                                if (startTime == -1) startTime = now;
                                if (now - startTime > 3_000_000_000L) {
                                    respawnEnemy(gameRoot);
                                    stop();
                                }
                            }
                        }.start();

                        stop();
                        showTankExplosion(gameRoot, enemy.getX(), enemy.getY());

                        return;
                    }
                }
            }
        }.start();
    }

    /**
     * Displays a temporary explosion effect at given coordinates.
     * Used for bullet impacts on walls.
     * @param gameRoot the root pane of the game
     * @param x x-coordinate of the explosion
     * @param y y-coordinate of the explosion
     */
    private void showExplosion(Pane gameRoot, double x, double y) {
        Image explosionImage = new Image("smallExplosion.png");
        ImageView explosion = new ImageView(explosionImage);
        explosion.setFitWidth(30);
        explosion.setFitHeight(30);
        explosion.setX(x - 15);
        explosion.setY(y - 15);
        gameRoot.getChildren().add(explosion);

        new AnimationTimer() {
            private long start = -1;
            @Override
            public void handle(long now) {
                if (!isGameStarted || isPaused || isGameOver) return;
                if (start < 0) start = now;
                if (now - start > 300_000_000) {
                    gameRoot.getChildren().remove(explosion);
                    stop();
                }
            }
        }.start();
    }

    /**
     * Displays a special explosion effect for tank destruction.
     * Used when tanks are destroyed by bullets or collisions.
     * @param gameRoot the root pane of the game
     * @param x x-coordinate of the explosion
     * @param y y-coordinate of the explosion
     */
    private void showTankExplosion(Pane gameRoot, double x, double y) {
        Image explosionImage = new Image("explosion.png");
        ImageView explosion = new ImageView(explosionImage);
        explosion.setFitWidth(50);
        explosion.setFitHeight(50);
        explosion.setX(x - 25);
        explosion.setY(y - 25);
        gameRoot.getChildren().add(explosion);

        new AnimationTimer() {
            private long start = -1;
            @Override
            public void handle(long now) {
                if (!isGameStarted || isPaused || isGameOver) return;
                if (start < 0) start = now;
                if (now - start > 300_000_000) {
                    gameRoot.getChildren().remove(explosion);
                    stop();
                }
            }
        }.start();
    }


    /**
     * Sets up enemy tank behavior, including random movement, firing, and collision response.
     * Manages three separate animation timers for direction changes, shooting, and movement.
     * @param gameRoot the root pane of the game
     */
    private void setupEnemy(Pane gameRoot) {
        // Randomly choose a new direction from 4 options every 2 seconds
        new AnimationTimer() {
            private long lastTurn = 0;

            @Override
            public void handle(long now) {
                if (!isGameStarted || isPaused || isGameOver) return;
                if (now - lastTurn > 2_000_000_000L) {
                    lastTurn = now;
                    for (ImageView enemy : enemies) {
                        if (!enemyAlive.get(enemy)) continue;

                        Point2D[] directions = {
                                new Point2D(1, 0),   // right
                                new Point2D(-1, 0),  // left
                                new Point2D(0, 1),   // down
                                new Point2D(0, -1)   // up
                        };
                        Point2D newDir = directions[(int)(Math.random() * 4)].multiply(0.5);
                        enemyVelocities.put(enemy, newDir);
                    }
                }
            }
        }.start();

        //Shoot bullets for each enemy every 2 seconds
        new AnimationTimer() {
            private long lastShot = 0;

            @Override
            public void handle(long now) {
                if (!isGameStarted || isPaused || isGameOver) return;
                if (now - lastShot > 2_000_000_000L) {
                    lastShot = now;
                    for (ImageView enemy : enemies) {
                        if (enemyAlive.get(enemy)) {
                            shootEnemyBullet(gameRoot, enemy);
                        }
                    }
                }
            }
        }.start();

        //Handle enemy movement and collision detection
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isGameStarted || isPaused || isGameOver) return;
                for (ImageView enemy : enemies) {
                    if (!enemyAlive.get(enemy)) continue;

                    Point2D velocity = enemyVelocities.get(enemy);

                    double nextX = enemy.getX() + velocity.getX();
                    double nextY = enemy.getY() + velocity.getY();

                    // Test X movement
                    enemy.setX(nextX);
                    boolean hitsWallX = checkWallCollision(enemy.getX(), enemy.getY(), enemy);
                    boolean hitsPlayerX = enemy.getBoundsInParent().intersects(tank.getBoundsInParent());
                    boolean hitsEnemyX = checkEnemyCollision(enemy);
                    enemy.setX(enemy.getX() - velocity.getX());

                    // Test Y movement
                    enemy.setY(nextY);
                    boolean hitsWallY = checkWallCollision(enemy.getX(), enemy.getY(), enemy);
                    boolean hitsPlayerY = enemy.getBoundsInParent().intersects(tank.getBoundsInParent());
                    boolean hitsEnemyY = checkEnemyCollision(enemy);
                    enemy.setY(enemy.getY() - velocity.getY());

                    // Change direction if necessary
                    if (hitsWallX || hitsEnemyX)
                        velocity = new Point2D(-velocity.getX(), velocity.getY());
                    if (hitsWallY || hitsEnemyY)
                        velocity = new Point2D(velocity.getX(), -velocity.getY());

                    if (hitsPlayerX || hitsPlayerY) {
                        // Enemy death
                        showTankExplosion(gameRoot, enemy.getX(), enemy.getY());
                        gameRoot.getChildren().remove(enemy);
                        enemyAlive.put(enemy, false);
                        score++;
                        scoreText.setText("Score: " + score);

                        // Player death
                        showTankExplosion(gameRoot, tank.getX(), tank.getY());
                        handlePlayerHit(gameRoot);

                        continue; // Move to next enemy
                    }


                    // Set enemy rotation based on movement direction
                    if (Math.abs(velocity.getX()) > Math.abs(velocity.getY())) {
                        enemy.setRotate(velocity.getX() > 0 ? 0 : 180);
                    } else {
                        enemy.setRotate(velocity.getY() > 0 ? 90 : -90);
                    }

                    // Update and move enemy
                    enemyVelocities.put(enemy, velocity);
                    enemy.setX(enemy.getX() + velocity.getX());
                    enemy.setY(enemy.getY() + velocity.getY());
                }
            }
        }.start();
    }

    /**
     * Respawns a new enemy tank at a random location.
     * Ensures the new enemy doesn't spawn inside walls or colliding with other enemies.
     * Uses up to 100 attempts to find a valid spawn location.
     *
     * @param gameRoot the root pane of the game
     */
    private void respawnEnemy(Pane gameRoot) {
        Image enemyImage = new Image("whiteTank1.png");

        ImageView newEnemy = new ImageView(enemyImage);
        newEnemy.setFitWidth(TILE_SIZE * 1.7);
        newEnemy.setFitHeight(TILE_SIZE * 1.7);

        double x, y;
        int attempts = 0;
        boolean collision;
        boolean wallCollision;

        do {
            x = 2 * TILE_SIZE + Math.random() * (WIDTH - 4 * TILE_SIZE);
            y = 2 * TILE_SIZE + Math.random() * (HEIGHT - 4 * TILE_SIZE);

            newEnemy.setX(x);
            newEnemy.setY(y);

            // Check wall collision
            double left = x;
            double right = x + newEnemy.getFitWidth();
            double top = y;
            double bottom = y + newEnemy.getFitHeight();

            int leftCol = (int) (left / TILE_SIZE);
            int rightCol = (int) ((right - 1) / TILE_SIZE);
            int topRow = (int) (top / TILE_SIZE);
            int bottomRow = (int) ((bottom - 1) / TILE_SIZE);

            wallCollision = false;
            for (int row = topRow; row <= bottomRow; row++) {
                for (int col = leftCol; col <= rightCol; col++) {
                    if (row < 0 || row >= map.length || col < 0 || col >= map[0].length || map[row][col] != 0) {
                        wallCollision = true;
                        break;
                    }
                }
                if (wallCollision) break;
            }

            // Check enemy collision
            collision = false;
            for (ImageView otherEnemy : enemies) {
                if (otherEnemy.getBoundsInParent().intersects(newEnemy.getBoundsInParent())) {
                    collision = true;
                    break;
                }
            }

            attempts++;
        } while ((wallCollision || collision) && attempts < 100);

        gameRoot.getChildren().add(newEnemy);

        enemies.add(newEnemy);

        // Set random initial direction
        Point2D[] directions = {
                new Point2D(1, 0),
                new Point2D(-1, 0),
                new Point2D(0, 1),
                new Point2D(0, -1)
        };
        Point2D randomDirection = directions[(int) (Math.random() * directions.length)];
        enemyVelocities.put(newEnemy, randomDirection.multiply(0.5));

        enemyAlive.put(newEnemy, true);
    }


    /**
     * Handles what happens when the player is hit by an enemy bullet or collides with an enemy.
     * Decreases player lives, removes tank from screen, and either respawns or shows game over.
     *
     * @param gameRoot the root pane of the game
     */
    private void handlePlayerHit(Pane gameRoot) {
        if (lives > 0) {
            lives--;
            heartIcons.get(lives).setVisible(false); // Son kalbi gizle
        }

        gameRoot.getChildren().remove(tank);

        if (lives > 0) {
            // Respawn after 2 seconds
            new AnimationTimer() {
                private long start = -1;
                @Override
                public void handle(long now) {
                    if (!isGameStarted || isPaused || isGameOver) return;
                    if (start < 0) start = now;
                    if (now - start > 2_000_000_000L) {
                        tank.setX(WIDTH / 2);
                        tank.setY(HEIGHT / 2);
                        if (tank.getParent() != gameRoot) {
                            gameRoot.getChildren().add(tank);
                        }
                        stop();
                    }
                }
            }.start();
        } else {
            isGameOver = true;
            gameOverText.setText("GAME OVER\nScore: " + score + "\nPress R to restart or Esc to quit");
        }
    }

    /**
     * Restarts the game by resetting the score, lives, enemies, and player tank.
     * Clears all existing enemies and creates 8 new ones.
     *
     * @param gameRoot the root pane of the game
     */
    private void restartGame(Pane gameRoot) {
        score = 0;
        lives = 3;
        isGameOver = false;
        scoreText.setText("Score: 0");
        lives = 3;
        for (ImageView heart : heartIcons) {
            heart.setVisible(true);
        }
        gameOverText.setText("");
        pauseText.setText("");
        isPaused = false;

        gameRoot.getChildren().remove(tank);
        tank.setX(WIDTH / 2);
        tank.setY(HEIGHT / 2);
        gameRoot.getChildren().add(tank);

        // Clear existing enemies
        for (ImageView enemy : enemies) {
            gameRoot.getChildren().remove(enemy);
        }
        enemies.clear();
        enemyVelocities.clear();
        enemyAlive.clear();

        // Create 6 enemies
        for (int i = 0; i < 6; i++) {
            respawnEnemy(gameRoot);
        }

    }

    /**
     * Fires a bullet from an enemy tank directed toward the player.
     * Creates an enemy bullet that moves in the direction of the enemy's current movement.
     *
     * @param gameRoot the root pane of the game
     * @param enemy the enemy tank shooting the bullet
     */
    private void shootEnemyBullet(Pane gameRoot, ImageView enemy) {
        double bulletX = enemy.getX() + enemy.getFitWidth() / 2;
        double bulletY = enemy.getY() + enemy.getFitHeight() / 2;

        Image bulletImage = new Image("bullet.png");
        ImageView bullet = new ImageView(bulletImage);
        bullet.setFitWidth(8);
        bullet.setFitHeight(8);
        bullet.setTranslateX(bulletX - 4);
        bullet.setTranslateY(bulletY - 4);
        gameRoot.getChildren().add(bullet);
        enemyBullets.add(bullet);

        // Shoot in the direction of current movement
        Point2D velocity = enemyVelocities.get(enemy);
        Point2D direction = velocity.normalize().multiply(ENEMY_BULLET_SPEED);


        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (!isGameStarted || isPaused || isGameOver) return;
                bullet.setTranslateX(bullet.getTranslateX() + direction.getX());
                bullet.setTranslateY(bullet.getTranslateY() + direction.getY());

                int col = (int) (bullet.getTranslateX() / TILE_SIZE);
                int row = (int) (bullet.getTranslateY() / TILE_SIZE);

                if (col < 0 || row < 0 || row >= map.length || col >= map[0].length || map[row][col] == 1) {
                    gameRoot.getChildren().remove(bullet);
                    showExplosion(gameRoot, bullet.getTranslateX(), bullet.getTranslateY());
                    stop();
                    return;
                }

                if (tank.getBoundsInParent().intersects(bullet.getBoundsInParent()) && !isGameOver) {
                    gameRoot.getChildren().remove(bullet);
                    stop();
                    showTankExplosion(gameRoot, tank.getX(), tank.getY());
                    handlePlayerHit(gameRoot);
                }

            }
        }.start();
    }

    /**
     * Checks if a given position collides with a wall.
     * @param x x-position to check
     * @param y y-position to check
     * @return true if the position collides with a wall, false otherwise
     */
    private boolean checkWallCollision(double x, double y, ImageView tank) {
        double left = x;
        double right = x + tank.getFitWidth();
        double top = y;
        double bottom = y + tank.getFitHeight();

        int leftCol = (int) (left / TILE_SIZE);
        int rightCol = (int) ((right - 1) / TILE_SIZE);
        int topRow = (int) (top / TILE_SIZE);
        int bottomRow = (int) ((bottom - 1) / TILE_SIZE);

        for (int row = topRow; row <= bottomRow; row++) {
            for (int col = leftCol; col <= rightCol; col++) {
                if (row < 0 || row >= map.length || col < 0 || col >= map[0].length || map[row][col] != 0) {
                    return true; // hits the wall
                }
            }
        }
        return false;
    }



    private boolean togglePlayerTank = false;

    private void startEnemyAnimation() {
        new AnimationTimer() {
            private long lastSwitch = 0;
            private boolean toggle = false;
            @Override
            public void handle(long now) {
                if (!isGameStarted || isPaused || isGameOver) return;
                if (now - lastSwitch > 200_000_000) {
                    toggle = !toggle;
                    for (ImageView enemy : enemies) {
                        if (enemyAlive.get(enemy)) {
                            enemy.setImage(toggle ?
                                    new Image("whiteTank1.png") :
                                    new Image("whiteTank2.png")
                            );
                        }
                    }
                    lastSwitch = now;
                }
            }
        }.start();
    }

    private AnimationTimer playerTankAnimation;

    private void setupPlayerTankAnimation() {
        playerTankAnimation = new AnimationTimer() {
            private long lastSwitch = 0;
            @Override
            public void handle(long now) {
                if (!isGameStarted || isPaused || isGameOver || !playerIsMoving) return;

                if (now - lastSwitch > 200_000_000) {
                    togglePlayerTank = !togglePlayerTank;
                    tank.setImage(togglePlayerTank
                            ? new Image("yellowTank1.png")
                            : new Image("yellowTank2.png"));
                    lastSwitch = now;
                }
            }
        };
        playerTankAnimation.start();
    }

    /**
     * Checks if an enemy tank collides with another enemy tank.
     * @param movingEnemy the enemy tank being moved
     * @return true if there is a collision, false otherwise
     */
    private boolean checkEnemyCollision(ImageView movingEnemy) {
        for (ImageView otherEnemy : enemies) {
            if (otherEnemy == movingEnemy) continue;
            if (!enemyAlive.get(otherEnemy)) continue;
            if (movingEnemy.getBoundsInParent().intersects(otherEnemy.getBoundsInParent())) {
                return true;
            }
        }
        return false;
    }

    /**
     * The main method to launch the JavaFX application.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
