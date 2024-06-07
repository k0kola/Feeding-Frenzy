import tester.*;
import javalib.worldimages.*;
import javalib.funworld.*;
import java.awt.Color;
import java.util.Random;

// Main world class representing the Pond
class PondWorld extends World {
  ILoFish fish;
  PlayerFish player;
  int tickCounter;

  PondWorld(ILoFish fish, PlayerFish player, int tickCounter) {
    this.fish = fish;
    this.player = player;
    this.tickCounter = tickCounter;
  }

  /*
   * Fields:
   * ... this.fish ...         -- ILoFish
   * ... this.player ...       -- PlayerFish
   * ... this.tickCounter ...  -- int
   * Methods:
   * ... this.makeScene() ...        -- WorldScene
   * ... this.onTick() ...           -- World
   * ... this.createRandomFish() ... -- bgFish
   * ... this.onKeyEvent(String) ... -- World
   * ... this.worldEnds() ...        -- WorldEnd
   * Methods for fields:
   * ... this.fish.draw(WorldScene) ...           -- WorldScene
   * ... this.fish.move() ...                     -- ILoFish
   * ... this.fish.filterEaten(PlayerFish) ...    -- ILoFish
   * ... this.fish.eats(PlayerFish) ...           -- boolean
   * ... this.fish.isEatenBy(PlayerFish) ...      -- boolean
   * ... this.fish.isPlayerLargest(PlayerFish) ...-- boolean
   * ... this.player.draw() ...                   -- WorldImage
   * ... this.player.move(String) ...             -- PlayerFish
   * ... this.player.move() ...                   -- PlayerFish
   * ... this.player.PlayerisEaten(ILoFish) ...   -- boolean
   * ... this.player.eatFish(ILoFish) ...         -- PlayerFish
   * ... this.player.eatSnack(ISnack) ...         -- PlayerFish
   */

  // Draw the world scene with all fish and the player
  // Draws the current world scene, including all fish and the player fish.
  public WorldScene makeScene() {
    WorldScene scene = this.fish.draw(new WorldScene(800, 600))
        .placeImageXY(this.player.draw(), this.player.x, this.player.y);

    scene = scene.placeImageXY(new TextImage("Score: " + this.player.score * 10,
        30, FontStyle.BOLD, Color.BLACK), 100, 50);
    scene = scene.placeImageXY(new TextImage("Lives: " + this.player.lives, 30,
        FontStyle.BOLD, Color.BLACK), 100, 100);

    return scene;
  }

  // Move all fish and the player on each tick
  // Moves all fish and the player fish on each game tick, 
  // and checks if the player fish eats other fish.
  public World onTick() {
    PlayerFish newPlayer = this.player.eatFish(this.fish);
    ILoFish newFish = this.fish.filterEaten(newPlayer);

    if (this.tickCounter % 100 == 0) { // create a random fish every 10 seconds
      newFish = new ConsLoFish(this.createRandomFish(), newFish);
    }

    return new PondWorld(newFish.move(), newPlayer.move(), this.tickCounter + 1);
  }

  //Create a new background fish with random properties
  public BgFish createRandomFish() {
    Random rand = new Random();
    int size = rand.nextInt(90) + 10; // size between 10 - 100
    Color color = new Color(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
    int x = 0;
    int y = rand.nextInt(600);
    int dx = rand.nextInt(3) + 1;
    int dy = rand.nextInt(3) - 1; // -1, 0, or 1 for random y-direction
    return new BgFish(size, color, x, y, dx, dy, rand.nextInt(100));
  }

  // Handle key events to move the player fish
  // Moves the player fish based on keyboard input.
  public World onKeyEvent(String key) {
    return new PondWorld(this.fish, this.player.move(key), this.tickCounter + 1);
  }

  // Check for end of game conditions
  // Checks for end of game conditions (whether the player fish is eaten). 
  // If the game ends, it displays "Game Over!".
  public WorldEnd worldEnds() {
    if (this.player.playerIsEaten(this.fish)) {
      if (this.player.lives == 1) {
        this.player.lives -= 1;
        return new WorldEnd(true, this.makeScene()
            .placeImageXY(new TextImage("Game Over! You Lost!", 50, FontStyle.BOLD, Color.RED), 
                400, 300));
      }
      else {
        this.player = new PlayerFish(this.player.size, this.player.color, 400, 300, this.player.dx, 
            this.player.dy, this.player.score, this.player.lives - 1, this.player.inertia, 
            this.player.speed);
        return new WorldEnd(false, this.makeScene());
      }
    }

    if (this.fish.isPlayerLargest(this.player)) {
      return new WorldEnd(true, this.makeScene()
          .placeImageXY(new AboveImage(
              new TextImage("Congratulations! You Won!", 50, FontStyle.BOLD, Color.GREEN),
              new TextImage("You are the largest fish!", 50, FontStyle.BOLD, Color.GREEN)), 
              400, 300));
    }
    return new WorldEnd(false, this.makeScene());
  }
}


// represent snacks
interface ISnack {
  // apply the snack effect on the player fish
  PlayerFish applyEffect(PlayerFish player);

  //Method to draw the snack
  WorldImage draw();

  // get X
  int getX();

  // get Y
  int getY();
}

// Abstract class representing a general snack
abstract class ASnack implements ISnack {

  /*
   * fields:
   *  ... this.x ... --int
   *  ... this.y ... --int
   * methods:
   *  ... this.appleEffect(PlayerFish) ... -- PlayerFish
   *  ... this.draw() ... --WorldImage
   *  ... this.getX() ... --int
   *  ... this.getY() ... --int
   * methods for fields: none
   */

  int x;
  int y;

  ASnack(int x, int y) {
    this.x = x;
    this.y = y;
  }

  // Abstract method to apply the snack effect on the player fish
  public abstract PlayerFish applyEffect(PlayerFish player);

  // Method to draw the snack
  public abstract WorldImage draw();

  // get X
  public int getX() {
    return this.x;
  }

  // get Y
  public int getY() {
    return this.y;
  }
}

//Class representing a size snack
class SizeSnack extends ASnack {

  /*
   * fields:
   *  ... this.x ... --int
   *  ... this.y ... --int
   *  ... this.growthAmount ... -- int
   * methods:
   *  ... this.appleEffect(PlayerFish) ... -- PlayerFish
   *  ... this.draw() ... --WorldImage
   *  ... this.getX() ... --int
   *  ... this.getY() ... --int
   *  ... this.draw() ... --WorldImage
   * methods for fields: none
   */

  int growthAmount;

  SizeSnack(int x, int y, int growthAmount) {
    super(x, y);
    this.growthAmount = growthAmount;
  }

  // Apply the size growth effect on the player fish
  public PlayerFish applyEffect(PlayerFish player) {
    /* 
     * fields:
     * ... this.x ... --int
     *  ... this.y ... --int
     *  ... this.growthAmount ... -- int
     * methods on fields: none
     * methods for parameters:
     *  ... this.draw() ... --WorldImage
     *  ... this.getX() ... --int
     *  ... this.getY() ... -- int
     *  ... this.getSize() ... int
     *  ... this.canEat(IFish) ... -- boolean
     *  ... this.move(String) ... --PlayerFish
     *  ... this.move() ... --PlayerFish
     *  ... this.PlayerIsEaten(ILoFish) ... --boolean
     *  ... this.eatFish(ILoFish) ... -- PlayerFish
     *  ... this.eatSnack(ISnack) ... -- PlayerFish
     */
    return new PlayerFish(player.size + this.growthAmount, player.color, player.x, 
        player.y, player.dx, player.dy, player.score, player.lives, player.inertia, player.speed);
  }

  // Draw the size snack
  public WorldImage draw() {
    return new CircleImage(10, OutlineMode.SOLID, Color.GREEN);
  }
}

// Class representing a speed snack
class SpeedSnack extends ASnack {
  /*
   * fields:
   *  ... this.x ... --int
   *  ... this.y ... --int
   *  ... this.speedBoost ... -- int
   *  ... this.boostDuration ... -- int
   * methods:
   *  ... this.appleEffect(PlayerFish) ... -- PlayerFish
   *  ... this.draw() ... --WorldImage
   *  ... this.getX() ... --int
   *  ... this.getY() ... --int
   *  ... this.draw() ... -- WorldImage
   * methods for fields: none
   */
  int speedBoost;
  int boostDuration;

  SpeedSnack(int x, int y, int speedBoost, int boostDuration) {
    super(x, y);
    this.speedBoost = speedBoost;
    this.boostDuration = boostDuration;
  }

  // Apply the speed boost effect on the player fish
  public PlayerFish applyEffect(PlayerFish player) {
    /* 
     * fields:
     * ... this.x ... --int
     *  ... this.y ... --int
     *  ... this.speedBoost ... -- int
     *  ... this.boostDuration ... --int
     * methods on fields: none
     * methods for parameters:
     *  ... this.draw() ... --WorldImage
     *  ... this.getX() ... --int
     *  ... this.getY() ... -- int
     *  ... this.getSize() ... int
     *  ... this.canEat(IFish) ... -- boolean
     *  ... this.move(String) ... --PlayerFish
     *  ... this.move() ... --PlayerFish
     *  ... this.PlayerIsEaten(ILoFish) ... --boolean
     *  ... this.eatFish(ILoFish) ... -- PlayerFish
     *  ... this.eatSnack(ISnack) ... -- PlayerFish
     */
    return new PlayerFish(player.size, player.color, player.x, player.y, player.dx + speedBoost,
        player.dy + speedBoost, player.score, player.lives, player.inertia, player.speed);
  }

  // Draw the speed snack
  public WorldImage draw() {
    return new CircleImage(10, OutlineMode.SOLID, Color.BLUE);
  }
}

interface IFish {

  // move the fish
  IFish move();

  // draw the fish
  WorldImage draw();

  // check if one fish can eat another
  boolean canEat(IFish other);

  // get the size of a fish
  int getSize();

  // get x coordinate of a fish
  int getX();

  // get y coordinate of a fish
  int getY();


}

// Class representing a generic fish
abstract class AFish implements IFish {
  int x;
  int y;
  int size;
  Color color;
  Random rand;

  /*
   * fields:
   *  ... this.x ... -- int
   *  ... this.y ... -- int
   *  ... this.size ... -- int
   *  ... this.color ... -- Color
   *  ... this.rand ... -- Random
   * methods:
   *  ... this.draw() ... --WorldImage
   *  ... this.move() ... -- IFish
   *  ... this.getX() ... --int
   *  ... this.getY() ... -- int
   *  ... this.getSize() ... int
   *  ... this.canEat(IFish) ... -- boolean
   * methods for fields:
   *  none
   */

  AFish(int size, Color color, int x, int y) {
    this.size = size;
    this.color = color;
    this.x = x;
    this.y = y;
    this.rand = new Random();
  }

  AFish(int size, Color color) {
    this(size, color, new Random().nextInt(800), new Random().nextInt(600));
  }

  //Draw the fish
  // Draws the fish as a circle with its size and color.
  public WorldImage draw() {
    return new CircleImage(this.size, OutlineMode.SOLID, this.color);
  }  

  public int getX() {
    return this.x;
  }

  public int getY() {
    return this.y;
  }

  public int getSize() {
    return this.size;
  }

  // Check if this fish eats another fish
  public boolean canEat(IFish other) {
    /*
     * fields:
     *  ... this.x ... -- int
     *  ... this.y ... -- int
     *  ... this.size ... -- int
     *  ... this.color ... -- Color
     *  ... this.rand ... -- Random
     * methods on fields: none
     * methods for parameters:
     *  ... this.draw() ... --WorldImage
     *  ... this.move() ... -- IFish
     *  ... this.getX() ... --int
     *  ... this.getY() ... -- int
     *  ... this.getSize() ... int
     *  ... this.canEat(IFish) ... -- boolean
     */
    return this.size > other.getSize()
        && Math.abs(this.x - other.getX()) < this.size 
        && Math.abs(this.y - other.getY()) < this.size;
  }

}

// represent background fish
class BgFish extends AFish {
  /* TEMPLATE
   * Fields:
   * ... this.size ...   -- int
   * ... this.color ...  -- Color
   * ... this.x ...      -- int
   * ... this.y ...      -- int
   * ... this.rand ...   -- Random
   * ... this.dy ...     -- int
   * ... this.dx ...     -- int
   * ... this.timeLeft ... -- int
   * Methods:
   *  ... this.move() ... --IFish
   *  ... this.draw() ... --WorldImage
   *  ... this.getX() ... --int
   *  ... this.getY() ... -- int
   *  ... this.getSize() ... int
   *  ... this.canEat(IFish) ... -- boolean
   * methods for fields:
   *  none
   */

  int dx;
  int dy;
  int timeLeft; // the bgfish will move in the same direction during the time left

  BgFish(int size, Color color, int x, int y, int dx, int dy, int timeLeft) {
    super(size, color, x, y);
    this.dx = dx;
    this.dy = dy;
    this.timeLeft = timeLeft;
  }

  BgFish(int size, Color color, int x, int y) {
    this(size, color, new Random().nextInt(800), new Random().nextInt(600), 1, 0, 
        new Random().nextInt(100));
  }

  //Move the fish in the current direction for the remaining time
  // Change direction after TIME_INTERVAL ticks
  public IFish move() {
    if (this.timeLeft <= 0) {
      int newDx = (rand.nextInt(2) == 0 ? -1 : 1); // -1 or 1
      int newDy = (rand.nextInt(2) == 0 ? -1 : 1); // -1, or 1
      return new BgFish(this.size, this.color, this.x, this.y, newDx, newDy, 
          new Random().nextInt(100));
    } 
    else {
      return new BgFish(this.size, this.color, (this.x + this.dx + 800) % 800, 
          (this.y + this.dy + 600) % 600, this.dx, this.dy, this.timeLeft - 1);
    }
  }

}

// Class representing the player-controlled fish
class PlayerFish extends AFish {
  /*
   * fields:
   *  ... this.x ... -- int
   *  ... this.y ... -- int
   *  ... this.size ... -- int
   *  ... this.color ... -- Color
   *  ... this.rand ... -- Random
   *  ... this.inertia ... -- double
   *  ... this.speed ... -- int
   *  ... this.score ... -- int
   *  ... this.dx ... -- int
   *  ... this.dy ... -- int
   *  ... this.lives ... -- int
   * methods:
   *  ... this.draw() ... --WorldImage
   *  ... this.getX() ... --int
   *  ... this.getY() ... -- int
   *  ... this.getSize() ... int
   *  ... this.canEat(IFish) ... -- boolean
   *  ... this.move(String) ... --PlayerFish
   *  ... this.move() ... --PlayerFish
   *  ... this.PlayerIsEaten(ILoFish) ... --boolean
   *  ... this.eatFish(ILoFish) ... -- PlayerFish
   *  ... this.eatSnack(ISnack) ... -- PlayerFish
   * methods for fields:
   *  none
   */
  double inertia;
  int speed;
  int score;
  int dx; // velocity in x direction
  int dy; // velocity in y direction
  int lives; // multiple lives for player

  PlayerFish(int size, Color color, int x, int y, int dx, int dy, 
      int score, int lives, double inertia, int speed) {
    super(size, color, x, y);
    this.dx = dx;
    this.dy = dy;
    this.score = score;
    this.lives = lives;
    this.inertia = inertia;
    this.speed = speed;
  }

  PlayerFish(int size, Color color, int dx, int dy, int score, int lives) {
    super(size, color);
    this.dx = dx;
    this.dy = dy;
    this.score = score;
    this.lives = lives;
    this.inertia = 0.85;
    this.speed = 10;
  }

  PlayerFish(int size, Color color) {
    this(size, color, 0, 0, 0, 3);
  }


  // Move the player fish based on key input
  // Moves the player fish based on key input (left, right, up, down).
  public PlayerFish move(String key) {
    if (key.equals("left")) {
      this.dx = -this.speed;
    }
    if (key.equals("right")) {
      this.dx = this.speed;
    }
    if (key.equals("up")) {
      this.dy = -this.speed;
    }
    if (key.equals("down")) {
      this.dy = this.speed;
    }

    int newX = (this.x + this.dx + 800) % 800;
    int newY = (this.y + this.dy + 600) % 600;

    return new PlayerFish(this.size, this.color, newX, newY, 
        this.dx, this.dy, this.score, this.lives, this.inertia, this.speed);
  }

  //Move the player fish based on its velocity
  public PlayerFish move() {
    if (this.inertia <= 0) {
      this.inertia = 0;
    }

    int newDx = (int) (this.dx * this.inertia);
    int newDy = (int) (this.dy * this.inertia);

    // Ensure fish stays within bounds
    int newX = (this.x + newDx + 800) % 800;
    int newY = (this.y + newDy + 600) % 600;

    return new PlayerFish(this.size, this.color, newX, newY, 
        newDx, newDy, this.score, this.lives, this.inertia, this.speed);
  }

  // Checks if the player fish is eaten by any background fish
  public boolean playerIsEaten(ILoFish fish) {
    /*
     * fields:
     *  ... this.x ... -- int
     *  ... this.y ... -- int
     *  ... this.size ... -- int
     *  ... this.color ... -- Color
     *  ... this.rand ... -- Random
     *  ... this.inertia ... -- double
     *  ... this.speed ... -- int
     *  ... this.score ... -- int
     *  ... this.dx ... -- int
     *  ... this.dy ... -- int
     *  ... this.lives ... -- int
     * methods on fields: none
     * methods for parameters:
     * ... this.draw(WorldScene) ...           -- WorldScene
     * ... this.move() ...                     -- ILoFish
     * ... this.isEatenBy(PlayerFish) ...      -- boolean
     * ... this.eats(PlayerFish) ...           -- boolean
     * ... this.filterEaten(PlayerFish) ...    -- ILoFish
     * ... this.isPlayerLargest(PlayerFish) ...-- boolean
     */
    return fish.eats(this);
  }

  // If the player fish eats a smaller fish, it grows in size by the eaten fish
  public PlayerFish eatFish(ILoFish fish) {
    if (fish.isEatenBy(this)) {
      return new PlayerFish(this.size, this.color, this.x, this.y,
          this.dx, this.dy, this.score, this.lives, this.inertia, this.speed);
    }
    return this;
  }

  //Check for collision with a snack and apply its effect
  public PlayerFish eatSnack(ISnack snack) {
    /*
     * fields:
     *  ... this.x ... -- int
     *  ... this.y ... -- int
     *  ... this.size ... -- int
     *  ... this.color ... -- Color
     *  ... this.rand ... -- Random
     *  ... this.inertia ... -- double
     *  ... this.speed ... -- int
     *  ... this.score ... -- int
     *  ... this.dx ... -- int
     *  ... this.dy ... -- int
     *  ... this.lives ... -- int
     * methods on fields: none
     * methods for parameters:
     * ... this.appleEffect(PlayerFish) ... -- PlayerFish
     *  ... this.draw() ... --WorldImage
     *  ... this.getX() ... --int
     *  ... this.getY() ... --int
     */
    if (Math.abs(this.x - snack.getX()) < this.size / 2 
        && Math.abs(this.y - snack.getY()) < this.size / 2) {
      return snack.applyEffect(this);
    }
    return this;
  }

  // Draws the player fish with the text "Player Fish" above it.
  public WorldImage draw() {
    WorldImage fishImage = super.draw();
    WorldImage textImage = new TextImage("You", this.size / 6 + 10, FontStyle.BOLD, Color.BLACK);
    return new OverlayImage(textImage, fishImage);
  }
}

// Interface for a list of fish
interface ILoFish {

  // Draws the world scene
  WorldScene draw(WorldScene acc);

  // move the fish in the list
  ILoFish move();

  // check if the fish is eaten by the player
  boolean isEatenBy(PlayerFish player);

  // check if the fish can eat the given fish
  boolean eats(IFish fish);

  // eliminate the fish that are eaten by the player
  ILoFish filterEaten(PlayerFish player);

  // check if the player is the largest fish
  boolean isPlayerLargest(PlayerFish player);
}

// Class representing an empty list of fish
class MtLoFish implements ILoFish {
  /* fields: none
   * Methods:
   * ... this.draw(WorldScene) ...           -- WorldScene
   * ... this.move() ...                     -- ILoFish
   * ... this.isEatenBy(PlayerFish) ...      -- boolean
   * ... this.eats(PlayerFish) ...           -- boolean
   * ... this.filterEaten(PlayerFish) ...    -- ILoFish
   * ... this.isPlayerLargest(PlayerFish) ...-- boolean
   */

  // Draws nothing and returns the unchanged world scene.
  public WorldScene draw(WorldScene acc) {
    return acc;
  }

  // Returns an empty list (no fish to move).
  public ILoFish move() {
    return this;
  }


  // Returns false (no fish to be eaten by the player).
  public boolean isEatenBy(PlayerFish player) {
    return false;
  }

  // Returns false (no fish to eat the player).
  public boolean eats(IFish fish) {
    return false;
  }

  // Returns an empty list (no fish to filter).
  public ILoFish filterEaten(PlayerFish player) {
    return this;
  }

  //Returns true as there are no fish larger than the player.
  public boolean isPlayerLargest(PlayerFish player) {
    return true;
  }
}

// Class representing a non-empty list of fish
class ConsLoFish implements ILoFish {
  IFish first;
  ILoFish rest;

  ConsLoFish(IFish first, ILoFish rest) {
    this.first = first;
    this.rest = rest;
  }
  /* 
   * Fields:
   * ... this.first ... -- IFish
   * ... this.rest ...  -- ILoFish
   * Methods:
   * ... this.draw(WorldScene) ...           -- WorldScene
   * ... this.move() ...                     -- ILoFish
   * ... this.isEatenBy(PlayerFish) ...      -- boolean
   * ... this.eats(IFish) ...                -- boolean
   * ... this.filterEaten(PlayerFish) ...    -- ILoFish
   * ... isPlayerLargerst(PlayerFish) ...    -- boolean
   * Methods for fields:
   * ... this.first.draw() ... --WorldImage
   * ... this.first.move() ... -- IFish
   * ... this.first.getX() ... --int
   * ... this.first.getY() ... -- int
   * ... this.first.getSize() ... int
   * ... this.first.canEat(IFish) ... -- boolean
   * ... this.rest.draw(WorldScene) ... -- WorldScene
   * ... this.rest.move() ...  -- ILoFish
   * ... this.rest.isEatenBy(PlayerFish) ... -- boolean
   * ... this.rest.eats(PlayerFish) ... -- boolean
   * ... this.rest.filterEaten(PlayerFish) ... -- ILoFish
   * ... this.rest.isPlayerLargest(PlayerFish) ...-- boolean
   */

  // Draw the current fish and then recursively draw the rest of the fish in the list.
  public WorldScene draw(WorldScene acc) {
    return this.rest.draw(acc).placeImageXY(this.first.draw(), 
        this.first.getX(), this.first.getY());
  }

  // Move the current fish and then recursively move the rest of the fish in the list.
  public ILoFish move() {
    return new ConsLoFish(this.first.move(), this.rest.move());
  }

  // Check if the current fish is eaten by the player fish or 
  // if any fish in the rest of the list is eaten by the player.
  public boolean isEatenBy(PlayerFish player) {
    /*
     * methods for parameters:
     * ... this.draw() ... --WorldImage
     *  ... this.getX() ... --int
     *  ... this.getY() ... -- int
     *  ... this.getSize() ... int
     *  ... this.canEat(IFish) ... -- boolean
     *  ... this.move(String) ... --PlayerFish
     *  ... this.move() ... --PlayerFish
     *  ... this.PlayerIsEaten(ILoFish) ... --boolean
     *  ... this.eatFish(ILoFish) ... -- PlayerFish
     *  ... this.eatSnack(ISnack) ... -- PlayerFish
     */
    return player.canEat(this.first) || this.rest.isEatenBy(player);
  }

  // Check if the current fish eats the player fish or if any fish in 
  // the rest of the list eats the player.
  public boolean eats(IFish fish) {
    /*
     * methods for parameters:
     *  ... this.draw() ... --WorldImage
     *  ... this.move() ... -- IFish
     *  ... this.getX() ... --int
     *  ... this.getY() ... -- int
     *  ... this.getSize() ... int
     *  ... this.canEat(IFish) ... -- boolean
     */
    return this.first.canEat(fish) || this.rest.eats(fish);
  }

  // Filter out the current fish if it is eaten by the player fish and 
  // recursively filter the rest of the fish in the list.
  public ILoFish filterEaten(PlayerFish player) {

    if (player.canEat(this.first)) {
      player.size += this.first.getSize() / 5; // grow the player fish
      player.score += this.first.getSize() / 5; // add score to the player fish

      // The bigger the player gets, the harder it should become to accelerate 
      // the fish and also to stop
      player.inertia -= player.size / 100; 
      return this.rest.filterEaten(player);
    } 
    else {
      return new ConsLoFish(this.first, this.rest.filterEaten(player));
    }
  }

  //Checks if the player fish is the largest fish in the list.
  public boolean isPlayerLargest(PlayerFish player) {
    if (player.size >= this.first.getSize()) {
      return this.rest.isPlayerLargest(player);
    } 
    else {
      return false;
    }
  }
}

// Example class to test the game
class ExamplesPondWorld {
  public int playersize = 12;
  IFish f1 = new BgFish(10, Color.BLUE, 100, 100);
  IFish f2 = new BgFish(15, Color.RED, 200, 200);
  IFish f3 = new BgFish(5, Color.GREEN, 300, 300);
  IFish f4 = new BgFish(20, Color.ORANGE, 400, 200);
  IFish f5 = new BgFish(8, Color.MAGENTA, 500, 500);
  IFish f6 = new BgFish(12, Color.CYAN, 600, 100);
  IFish f7 = new BgFish(18, Color.PINK, 100, 500);
  IFish f8 = new BgFish(25, Color.GRAY, 200, 300);
  IFish f9 = new BgFish(30, Color.DARK_GRAY, 300, 400);
  IFish f10 = new BgFish(7, Color.LIGHT_GRAY, 400, 500);
  IFish f11 = new BgFish(9, Color.BLACK, 500, 100);
  IFish f12 = new BgFish(14, Color.BLUE, 600, 200);
  IFish f13 = new BgFish(11, Color.RED, 100, 300);
  IFish f14 = new BgFish(17, Color.GREEN, 200, 400);
  IFish f15 = new BgFish(22, Color.ORANGE, 300, 500);
  IFish f16 = new BgFish(6, Color.MAGENTA, 400, 100);
  IFish f17 = new BgFish(13, Color.CYAN, 500, 200);
  IFish f18 = new BgFish(21, Color.PINK, 600, 300);
  IFish f19 = new BgFish(23, Color.GRAY, 100, 400);
  IFish f20 = new BgFish(24, Color.DARK_GRAY, 200, 500);
  ILoFish mt = new MtLoFish();
  ILoFish lof1 = new ConsLoFish(f1, mt);
  ILoFish lof2 = new ConsLoFish(f2, lof1);
  ILoFish lof3 = new ConsLoFish(f3, lof2);
  ILoFish lof4 = new ConsLoFish(f4, lof3);
  ILoFish lof5 = new ConsLoFish(f5, lof4);
  ILoFish lof6 = new ConsLoFish(f6, lof5);
  ILoFish lof7 = new ConsLoFish(f7, lof6);
  ILoFish lof8 = new ConsLoFish(f8, lof7);
  ILoFish lof9 = new ConsLoFish(f9, lof8);
  ILoFish lof10 = new ConsLoFish(f10, lof9);
  ILoFish lof11 = new ConsLoFish(f11, lof10);
  ILoFish lof12 = new ConsLoFish(f12, lof11);
  ILoFish lof13 = new ConsLoFish(f13, lof12);
  ILoFish lof14 = new ConsLoFish(f14, lof13);
  ILoFish lof15 = new ConsLoFish(f15, lof14);
  ILoFish lof16 = new ConsLoFish(f16, lof15);
  ILoFish lof17 = new ConsLoFish(f17, lof16);
  ILoFish lof18 = new ConsLoFish(f18, lof17);
  ILoFish lof19 = new ConsLoFish(f19, lof18);
  ILoFish fishList = new ConsLoFish(f20, lof19);


  PlayerFish pf = new PlayerFish(12, Color.YELLOW, 400, 400, 0, 0, 0, 3, 0.85, 10);
  PondWorld world = new PondWorld(this.fishList, pf, 0);

  // Test the game using bigBang
  boolean testBigBang(Tester t) {
    // Runs the game using the bigBang method to initiate the game loop with a 
    // window size of 800x600 and a tick rate of 0.1 seconds.
    return this.world.bigBang(800, 600, 0.1);
  }

  // test the constructor
  boolean testConstructor(Tester t) {
    return t.checkExpect(this.world.fish, this.fishList) 
        && t.checkExpect(this.world.player, this.pf)
        && t.checkExpect(this.world.tickCounter, 0);
  }

  // test the methods makeScene
  boolean testMakeScene(Tester t) {
    WorldScene scene = new WorldScene(800, 600);
    scene = this.fishList.draw(scene);
    scene = scene.placeImageXY(this.pf.draw(), this.pf.x, this.pf.y);
    scene = scene.placeImageXY(
        new TextImage("Score: " + this.pf.score * 10, 30, FontStyle.BOLD, Color.BLACK), 100, 50);
    scene = scene.placeImageXY(new TextImage("Lives: " + this.pf.lives, 30,
        FontStyle.BOLD, Color.BLACK), 100, 100);
    return t.checkExpect(this.world.makeScene(), scene);
  }

  // test the method onTick
  boolean testOnTick(Tester t) {
    PondWorld newWorld = (PondWorld) this.world.onTick();
    PlayerFish movedPlayer = this.pf.move();
    return t.checkExpect(newWorld.player, movedPlayer)
        && t.checkExpect(newWorld.tickCounter, 1);
  }

  // test the method createRandomFish
  boolean testCreateRandomFish(Tester t) {
    BgFish randomFish = this.world.createRandomFish();
    return t.checkExpect(randomFish.size >= 10 && randomFish.size <= 100, true)
        && t.checkExpect(randomFish.x, 0)
        && t.checkExpect(randomFish.y >= 0 && randomFish.y <= 600, true);
  }

  // test the method onKeyEvent
  boolean testOnKeyEvent(Tester t) {
    PondWorld leftWorld = (PondWorld) this.world.onKeyEvent("left");
    PlayerFish movedPlayer = this.pf.move("left");
    return t.checkExpect(leftWorld.player, movedPlayer) && t.checkExpect(leftWorld.tickCounter, 1);
  }

  // test the method worldEnds 
  boolean testWorldEnds(Tester t) {
    PlayerFish eatenPlayer = new PlayerFish(12, Color.YELLOW, 400, 400, 0, 0, 0, 0, 0.85, 10);
    PondWorld endWorld = new PondWorld(this.fishList, eatenPlayer, 0);
    WorldEnd result = endWorld.worldEnds();
    return t.checkExpect(result.worldEnds, false);
  }

  // test the method move in class playerfish
  boolean testPlayerFishMove(Tester t) {
    PlayerFish movedLeft = this.pf.move("left");
    return t.checkExpect(movedLeft.x, 390) && t.checkExpect(movedLeft.dx, -10);
  }

  // test the method eatFish in class playerfish
  boolean testPlayerFishEatFish(Tester t) {
    PlayerFish newPlayer = this.pf.eatFish(this.fishList);
    return t.checkExpect(newPlayer.size, this.pf.size) 
        && t.checkExpect(newPlayer.score, this.pf.score);
  }

  // test the method eatSnack in class playerfish
  boolean testPlayerFishEatSnack(Tester t) {
    ISnack snack = new SizeSnack(400, 400, 2);
    PlayerFish newPlayer = this.pf.eatSnack(snack);
    return t.checkExpect(newPlayer.size, this.pf.size + 2);
  }

  // test the method playerIsEaten in class playerfish
  boolean testPlayerFishIsEaten(Tester t) {
    PlayerFish newPlayer = new PlayerFish(1, Color.YELLOW, 400, 400, 0, 0, 0, 3, 0.85, 10);
    return t.checkExpect(newPlayer.playerIsEaten(this.fishList), false);
  }

  // test the method move in ILoFish
  boolean testILoFishMove(Tester t) {
    ILoFish movedFishList = this.fishList.move();
    return t.checkExpect(movedFishList instanceof ConsLoFish, true);
  }

  // test the method isEatenBy in ILoFish
  boolean testILoFishIsEatenBy(Tester t) {
    return t.checkExpect(this.fishList.isEatenBy(this.pf), false);
  }

  // test the method eats in ILoFish
  boolean testFishEats(Tester t) {
    return t.checkExpect(this.fishList.eats(this.pf), false);
  }

  // test the method draw in class MtLoFish
  boolean testMtLoFishDraw(Tester t) {
    WorldScene scene = new WorldScene(800, 600);
    return t.checkExpect(this.mt.draw(scene), scene);
  }

  // test the method draw in class ConsLoFish  
  boolean testConsLoFishDraw(Tester t) {
    WorldScene scene = new WorldScene(800, 600);
    scene = this.fishList.draw(scene);
    return t.checkExpect(scene, scene);
  }

  // test the method move in ILoFish
  boolean testMove(Tester t) {
    return t.checkExpect(this.mt.move(), this.mt);
  }

  // test the method isEatenBy in ILoFish
  boolean testIsEatenBy(Tester t) {
    return t.checkExpect(this.mt.isEatenBy(this.pf), false)
        && t.checkExpect(this.fishList.isEatenBy(this.pf), false);
  }

  // test the method eats in ILoFish
  boolean testEats(Tester t) {
    return t.checkExpect(this.mt.eats(this.pf), false)
        && t.checkExpect(this.fishList.eats(this.pf), false);
  }

  // test the method filterEaten in ILoFish
  boolean testFilterEaten(Tester t) {
    ILoFish filteredFishList = this.fishList.filterEaten(this.pf);
    return t.checkExpect(this.mt.filterEaten(this.pf), this.mt)
        && t.checkExpect(filteredFishList instanceof ConsLoFish, true);
  }

  // test the method isPlayerLargest in ILoFish
  boolean testIsPlayerLargest(Tester t) {
    return t.checkExpect(this.mt.isPlayerLargest(this.pf), true)
        && t.checkExpect(this.fishList.isPlayerLargest(this.pf), false);
  }

  // test the method applyEffect in class SizeSnack
  boolean testSizeSnackApplyEffect(Tester t) {
    ISnack snack = new SizeSnack(400, 400, 2);
    PlayerFish newPlayer = snack.applyEffect(this.pf);
    return t.checkExpect(newPlayer.size, this.pf.size + 2);
  }

  // test the method draw in ISnack
  boolean testSizeSnackDraw(Tester t) {
    ISnack snack = new SizeSnack(400, 400, 2);
    WorldImage image = snack.draw();
    return t.checkExpect(image, new CircleImage(10, OutlineMode.SOLID, Color.GREEN));
  }

  // test the method applyEffect in class SpeedSnack
  boolean testSpeedSnackApplyEffect(Tester t) {
    ISnack snack = new SpeedSnack(400, 400, 2, 5);
    PlayerFish newPlayer = snack.applyEffect(this.pf);
    return t.checkExpect(newPlayer.dx, this.pf.dx + 2) 
        && t.checkExpect(newPlayer.dy, this.pf.dy + 2);
  }

  // test the method draw in class SpeedSnack
  boolean testSpeedSnackDraw(Tester t) {
    ISnack snack = new SpeedSnack(400, 400, 2, 5);
    WorldImage image = snack.draw();
    return t.checkExpect(image, new CircleImage(10, OutlineMode.SOLID, Color.BLUE));
  }
}


