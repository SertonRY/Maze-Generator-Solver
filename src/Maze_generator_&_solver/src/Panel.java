import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class Panel extends JPanel implements ActionListener{
    public enum nodeState {
        undiscovered,
        discovered,
        GO_UP,
        GO_DOWN,
        GO_LEFT,
        GO_RIGHT,
        GO_BACK,
        Wall,
        Reverse_Left,
        Reverse_Right,
        Reverse_Up,
        Reverse_Down,
        Reserved,
    }
    static int SCREEN_WIDTH = 800;
    static int SCREEN_HEIGHT = 600;
    static int rootX = 0;
    static int rootY = 0;
    static final int UNIT_SIZE = 25;
    static final int NODE_SIZE = 25;
    static final int NODE_UNITS_X = SCREEN_WIDTH / UNIT_SIZE;
    static final int NODE_UNITS_Y = SCREEN_HEIGHT / UNIT_SIZE;
    static final int TOTAL_UNITS_X = NODE_UNITS_X * 2 + 1;
    static final int TOTAL_UNITS_Y = NODE_UNITS_Y * 2 + 1;
    static final int resultRootX = 0;
    static final int resultRootY = 0;
    static final int endX = NODE_UNITS_X - 1;
    static final int endY = NODE_UNITS_Y - 1;
    static final int DELAY = 100;
    static ArrayList<Integer> selectedArrayX;
    static ArrayList<Integer> selectedArrayY;
    static ArrayList<Integer> selectedArrayValue;
    static int resultX = 0;
    static int resultY = 0;
    static int limit = 0;
    static ArrayList<Integer> routeX;
    static ArrayList<Integer> routeY;
    static ArrayList<Integer> userRouteX;
    static ArrayList<Integer> userRouteY;
    nodeState[][] node = new nodeState[NODE_UNITS_X][NODE_UNITS_Y];
    nodeState[][] total = new nodeState[TOTAL_UNITS_X][TOTAL_UNITS_Y];
    int[][] resultNode = new int[NODE_UNITS_X][NODE_UNITS_Y];
    nodeState[][] resultTotal = new nodeState[TOTAL_UNITS_X][TOTAL_UNITS_Y];

    char movingDirection = 'N';

    boolean pauseTrigger = false;
    boolean restartTrigger = false;
    boolean hintTrigger = false;
    boolean gameOverTrigger =false;

    Random random;
    Timer timer;

    static Color backGroundColor = new Color(240,230,140);

    Panel(){
        random = new Random();
        this.setPreferredSize(
                new Dimension(SCREEN_WIDTH, SCREEN_HEIGHT));
        this.setBackground(backGroundColor);
        this.setFocusable(true);
        this.addKeyListener(new GameKeyAdapter());
        newGame();
    }
    public void newGame(){
        mazeInitialize();
        mazeGenerator();
        mazeSolver();
        repaint();
        userRouteX = new ArrayList<>();
        userRouteY = new ArrayList<>();
        userRouteX.add(resultRootX);
        userRouteY.add(resultRootY);
        timer = new Timer(DELAY, this);
        timer.start();
    }
    public void paintComponent(Graphics g){
        //set graphics
        super.paintComponent(g);
        drawFrame(g);
    }
    public void mazeInitialize(){
        for(int x = 0; x < NODE_UNITS_X; x++){
            for(int y = 0; y < NODE_UNITS_Y; y++){
                node[x][y] = nodeState.undiscovered;
            }
        }
        for(int x = 0; x < TOTAL_UNITS_X; x++){
            total[x][0] = nodeState.Wall;
            total[x][TOTAL_UNITS_Y - 1] = nodeState.Wall;
        }
        for(int y = 0; y < TOTAL_UNITS_Y; y++){
            total[0][y] = nodeState.Wall;
            total[TOTAL_UNITS_X - 1][y] = nodeState.Wall;
        }
        for(int x = 1; x < TOTAL_UNITS_X - 1; x++){
            for(int y = 1; y < TOTAL_UNITS_Y - 1; y++){
                total[x][y] = nodeState.undiscovered;
            }
        }
    }
    public int node2Total(int s){
        s = s * 2 + 1;
        return s;
    }
    public void mazeGenerator(){
        rootX = random.nextInt(NODE_UNITS_X);
        rootY = random.nextInt(NODE_UNITS_Y);
        node[rootX][rootY] = nodeState.discovered;
        total[node2Total(rootX)][node2Total(rootY)] =
                nodeState.discovered;
        while(!nodeExploreComplete()){
            mazeNodeSelfExplore();
        }
        mazeWallAutoFill();
    }
    public void mazeNodeSelfExplore(){
        nodeState step = stepDecide(rootX, rootY);
        switch(step){
            case GO_RIGHT:
                node[rootX + 1][rootY] = nodeState.discovered;
                total[node2Total(rootX + 1)][node2Total(rootY)] =
                        nodeState.discovered;
                total[node2Total(rootX + 1) - 1][node2Total(rootY)] =
                        nodeState.Reverse_Left;
                rootX = rootX + 1;
                break;
            case GO_UP:
                node[rootX][rootY - 1] = nodeState.discovered;
                total[node2Total(rootX)][node2Total(rootY - 1)] =
                        nodeState.discovered;
                total[node2Total(rootX)][node2Total(rootY - 1) + 1] =
                        nodeState.Reverse_Down;
                rootY = rootY - 1;
                break;
            case GO_LEFT:
                node[rootX - 1][rootY] = nodeState.discovered;
                total[node2Total(rootX - 1)][node2Total(rootY)] =
                        nodeState.discovered;
                total[node2Total(rootX - 1) + 1][node2Total(rootY)] =
                        nodeState.Reverse_Right;
                rootX = rootX - 1;
                break;
            case GO_DOWN:
                node[rootX][rootY + 1] = nodeState.discovered;
                total[node2Total(rootX)][node2Total(rootY + 1)] =
                        nodeState.discovered;
                total[node2Total(rootX)][node2Total(rootY + 1) - 1] =
                        nodeState.Reverse_Up;
                rootY = rootY + 1;
                break;
            case GO_BACK:
                nodeState BACK_UP =
                        total[node2Total(rootX)][node2Total(rootY) - 1];
                nodeState BACK_DOWN =
                        total[node2Total(rootX)][node2Total(rootY) + 1];
                nodeState BACK_LEFT =
                        total[node2Total(rootX) - 1][node2Total(rootY)];
                nodeState BACK_RIGHT =
                        total[node2Total(rootX) + 1][node2Total(rootY)];
                if(BACK_UP == nodeState.Reverse_Up){
                    rootY = rootY - 1;
                }
                if(BACK_DOWN == nodeState.Reverse_Down){
                    rootY = rootY + 1;
                }
                if(BACK_LEFT == nodeState.Reverse_Left){
                    rootX = rootX - 1;
                }
                if(BACK_RIGHT == nodeState.Reverse_Right){
                    rootX = rootX + 1;
                }
                break;
        }
    }
    public nodeState stepDecide(int x, int y){
        x = node2Total(x);
        y = node2Total(y);
        switch(random.nextInt(24)){
            case 0:
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                break;
            case 1:
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                break;
            case 2:
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                break;
            case 3:
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                break;
            case 4:
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                break;
            case 5:
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                break;
            case 6:
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                break;
            case 7:
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                break;
            case 8:
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                break;
            case 9:
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                break;
            case 10:
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                break;
            case 11:
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                break;
            case 12:
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                break;
            case 13:
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                break;
            case 14:
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                break;
            case 15:
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                break;
            case 16:
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                break;
            case 17:
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                break;
            case 18:
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                break;
            case 19:
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                break;
            case 20:
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                break;
            case 21:
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                break;
            case 22:
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                break;
            case 23:
                if(stepDetectDown(x, y)){
                    return nodeState.GO_DOWN;
                }
                if(stepDetectUp(x, y)){
                    return nodeState.GO_UP;
                }
                if(stepDetectRight(x, y)){
                    return nodeState.GO_RIGHT;
                }
                if(stepDetectLeft(x, y)){
                    return nodeState.GO_LEFT;
                }
                break;
        }
        return nodeState.GO_BACK;
    }
    public boolean nodeExploreComplete(){
        for(int x = 0; x < NODE_UNITS_X; x++){
            for(int y = 0; y < NODE_UNITS_Y; y++){
                if(node[x][y] == nodeState.undiscovered){
                    return false;
                }
            }
        }
        return true;
    }
    public boolean stepDetectLeft(int totalX, int totalY){
        if(total[totalX - 1][totalY] != nodeState.Wall){
            return total[totalX - 2][totalY] ==
                    nodeState.undiscovered;
        }
        return false;
    }
    public boolean stepDetectRight(int totalX, int totalY){
        if(total[totalX + 1][totalY] != nodeState.Wall){
            return total[totalX + 2][totalY] ==
                    nodeState.undiscovered;
        }
        return false;
    }
    public boolean stepDetectUp(int totalX, int totalY){
        if(total[totalX][totalY - 1] != nodeState.Wall){
            return total[totalX][totalY - 2] ==
                    nodeState.undiscovered;
        }
        return false;
    }
    public boolean stepDetectDown(int totalX, int totalY){
        if(total[totalX][totalY + 1] != nodeState.Wall){
            return total[totalX][totalY + 2] ==
                    nodeState.undiscovered;
        }
        return false;
    }
    public void mazeWallAutoFill(){
        for(int x = 1; x < TOTAL_UNITS_X - 1; x++){
            for(int y = 1; y < TOTAL_UNITS_Y - 1; y++){
                if(total[x][y] == nodeState.undiscovered){
                    total[x][y] = nodeState.Wall;
                }
            }
        }
    }
    public void resultCost(int resultX, int resultY){
        resultNode[resultX][resultY] =
                Math.abs(resultX - resultRootX) +
                        Math.abs(resultY - resultRootY) +
                        Math.abs(resultX - endX) +
                        Math.abs(resultY - endY);
    }
    public void mazeSolver(){
        resultX = resultRootX;
        resultY = resultRootY;
        resultCost(resultX, resultY);
        resultTotal[node2Total(resultX)][node2Total(resultY)] =
                nodeState.Reserved;
        while((resultX != endX) || (resultY != endY)){
            aStar();
        }
        findRoute();
    }
    public void aStar(){
        if(total[node2Total(resultX) - 1][node2Total(resultY)] !=
                nodeState.Wall){
            if(resultTotal[node2Total(resultX - 1)][node2Total(resultY)] !=
                    nodeState.Reserved){
                resultCost(resultX - 1, resultY);
                resultTotal[node2Total(resultX - 1)][node2Total(resultY)] =
                        nodeState.discovered;
                resultTotal[node2Total(resultX) - 1][node2Total(resultY)] =
                        nodeState.Reverse_Right;
            }
        }
        if(total[node2Total(resultX) + 1][node2Total(resultY)] !=
                nodeState.Wall){
            if(resultTotal[node2Total(resultX + 1)][node2Total(resultY)] !=
                    nodeState.Reserved){
                resultCost(resultX + 1, resultY);
                resultTotal[node2Total(resultX + 1)][node2Total(resultY)] =
                        nodeState.discovered;
                resultTotal[node2Total(resultX) + 1][node2Total(resultY)] =
                        nodeState.Reverse_Left;
            }
        }
        if(total[node2Total(resultX)][node2Total(resultY) - 1] !=
                nodeState.Wall){
            if(resultTotal[node2Total(resultX)][node2Total(resultY - 1)] !=
                    nodeState.Reserved){
                resultCost(resultX, resultY - 1);
                resultTotal[node2Total(resultX)][node2Total(resultY - 1)] =
                        nodeState.discovered;
                resultTotal[node2Total(resultX)][node2Total(resultY) - 1] =
                        nodeState.Reverse_Down;
            }
        }
        if(total[node2Total(resultX)][node2Total(resultY) + 1] !=
                nodeState.Wall){
            if(resultTotal[node2Total(resultX)][node2Total(resultY + 1)] !=
                    nodeState.Reserved){
                resultCost(resultX, resultY + 1);
                resultTotal[node2Total(resultX)][node2Total(resultY + 1)] =
                        nodeState.discovered;
                resultTotal[node2Total(resultX)][node2Total(resultY) + 1] =
                        nodeState.Reverse_Up;
            }
        }
        getSelectedList();
        int nextIndex = getSelectedMinIndex();
        resultX = selectedArrayX.get(nextIndex);
        resultY = selectedArrayY.get(nextIndex);
        resultTotal[node2Total(resultX)][node2Total(resultY)] =
                nodeState.Reserved;
    }
    public void getSelectedList(){
        selectedArrayValue = new ArrayList<>();
        selectedArrayX = new ArrayList<>();
        selectedArrayY = new ArrayList<>();
        for(int x = 0; x < NODE_UNITS_X; x++){
            for(int y = 0; y <NODE_UNITS_Y; y++){
                if(resultTotal[node2Total(x)][node2Total(y)] == nodeState.discovered){
                    selectedArrayValue.add(resultNode[x][y]);
                    selectedArrayX.add(x);
                    selectedArrayY.add(y);
                }
            }
        }
    }
    public int getSelectedMinIndex(){
        int min = selectedArrayValue.get(0);
        int index = 0;
        for(int i = 0; i < selectedArrayValue.size(); i++){
            if(selectedArrayValue.get(i) < min){
                min = selectedArrayValue.get(i);
                index = i;
            }
        }
        return index;
    }
    public void findRoute(){
        routeX = new ArrayList<>();
        routeY = new ArrayList<>();
        resultX = endX;
        resultY = endY;
        routeX.add(resultX);
        routeY.add(resultY);
        while((resultX != resultRootX) || (resultY != resultRootY)){
            selectRoute();
        }
        routeX.add(resultRootX);
        routeY.add(resultRootY);
        Collections.reverse(routeX);
        Collections.reverse(routeY);
    }
    public void selectRoute(){
        if(resultTotal[node2Total(resultX) - 1][node2Total(resultY)] ==
                nodeState.Reverse_Left){
            resultX = resultX - 1;
        }
        else if(resultTotal[node2Total(resultX) + 1][node2Total(resultY)] ==
                nodeState.Reverse_Right){
            resultX = resultX + 1;
        }
        else if(resultTotal[node2Total(resultX)][node2Total(resultY) - 1] ==
                nodeState.Reverse_Up){
            resultY = resultY - 1;
        }
        else if(resultTotal[node2Total(resultX)][node2Total(resultY) + 1] ==
                nodeState.Reverse_Down){
            resultY = resultY + 1;
        }
        routeX.add(resultX);
        routeY.add(resultY);
    }
    public void drawFrame(Graphics g) {
        g.setColor(Color.BLACK);
        for(int x = 0; x < NODE_UNITS_X; x++){
            for(int y = 0; y < NODE_UNITS_Y; y++){
                if(total[node2Total(x) - 1][node2Total(y)] == nodeState.Wall){
                    g.drawLine(x * NODE_SIZE, y * NODE_SIZE,
                            x * NODE_SIZE, (y + 1) * NODE_SIZE);
                }
                if(total[node2Total(x) + 1][node2Total(y)] == nodeState.Wall){
                    g.drawLine((x + 1) * NODE_SIZE, y * NODE_SIZE,
                            (x + 1) * NODE_SIZE, (y + 1) * NODE_SIZE);
                }
                if(total[node2Total(x)][node2Total(y) - 1] == nodeState.Wall){
                    g.drawLine(x * NODE_SIZE, y * NODE_SIZE,
                            (x + 1) * NODE_SIZE, y * NODE_SIZE);
                }
                if(total[node2Total(x)][node2Total(y) + 1] == nodeState.Wall){
                    g.drawLine(x * NODE_SIZE, (y + 1) * NODE_SIZE,
                            (x + 1) * NODE_SIZE, (y + 1) * NODE_SIZE);
                }
            }
        }
        g.setColor(Color.GREEN);
        g.fillRect(endX * UNIT_SIZE + 4, endY * UNIT_SIZE + 4,
                UNIT_SIZE - 4, UNIT_SIZE - 4);
        g.setColor(Color.RED);
        for(int i = 0; i < limit; i++){
            g.fillRoundRect(routeX.get(i) * NODE_SIZE + 8,
                    routeY.get(i) * NODE_SIZE + 8,
                    UNIT_SIZE - 16, UNIT_SIZE - 16,
                    UNIT_SIZE - 16, UNIT_SIZE - 16);
            if(i != 0){
                g.drawLine(routeX.get(i) * NODE_SIZE + NODE_SIZE / 2,
                        routeY.get(i) * NODE_SIZE + NODE_SIZE / 2,
                        routeX.get(i - 1) * NODE_SIZE + NODE_SIZE / 2,
                        routeY.get(i - 1) * NODE_SIZE + NODE_SIZE / 2);
            }
        }
        for(int i = 0; i < userRouteX.size(); i++){
            if(i == userRouteX.size() - 1){
                g.drawImage(new ImageIcon("./icon.png").getImage(),
                        userRouteX.get(i) * NODE_SIZE, userRouteY.get(i) * NODE_SIZE,
                        UNIT_SIZE, UNIT_SIZE, this);
            }
            else {
                g.drawLine(userRouteX.get(i) * NODE_SIZE + NODE_SIZE / 2,
                        userRouteY.get(i) * NODE_SIZE + NODE_SIZE / 2,
                        userRouteX.get(i + 1) * NODE_SIZE + NODE_SIZE / 2,
                        userRouteY.get(i + 1) * NODE_SIZE + NODE_SIZE / 2);
                g.drawImage(new ImageIcon("./icon2.png").getImage(),
                        userRouteX.get(i) * NODE_SIZE, userRouteY.get(i) * NODE_SIZE,
                        UNIT_SIZE, UNIT_SIZE, this);
            }
        }
        if(gameOverTrigger){
            g.setColor(new Color(139,0,0));
            g.setFont(new Font("Consolas", Font.BOLD, 70));
            FontMetrics metricsUp = getFontMetrics(g.getFont());
            g.drawString("Game Over",
                    (SCREEN_WIDTH -
                            metricsUp.stringWidth("Game Over")) / 2,
                    SCREEN_HEIGHT / 2 - 50);
            //final scores
            g.setFont(new Font("Consolas", Font.BOLD, 30));
            FontMetrics metricsDown = getFontMetrics(g.getFont());
            g.drawString("Your Steps: " + userRouteX.size(),
                    (SCREEN_WIDTH -
                            metricsDown.stringWidth(
                                    "Your Steps: " + userRouteX.size())) / 2,
                    SCREEN_HEIGHT / 2 + 50);
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(restartTrigger){
            timer.stop();
            rootX = 0;
            rootY = 0;
            selectedArrayX.clear();
            selectedArrayY.clear();
            selectedArrayValue.clear();
            resultX = 0;
            resultY = 0;
            limit = 0;
            routeX.clear();
            routeY.clear();
            node = new nodeState[NODE_UNITS_X][NODE_UNITS_Y];
            total = new nodeState[TOTAL_UNITS_X][TOTAL_UNITS_Y];
            resultNode = new int[NODE_UNITS_X][NODE_UNITS_Y];
            resultTotal = new nodeState[TOTAL_UNITS_X][TOTAL_UNITS_Y];
            movingDirection = 'N';
            pauseTrigger = false;
            restartTrigger = false;
            hintTrigger = false;
            gameOverTrigger = false;
            userRouteX.clear();
            userRouteY.clear();
            newGame();
        }
        if(hintTrigger){
            if(limit < routeX.size()){
                limit++;
                userRouteX.add(routeX.get(limit - 1));
                userRouteY.add(routeY.get(limit - 1));
            }
            else{
                hintTrigger = false;
            }
        }
        if(movingDirection == 'L'){
            if(total[node2Total(userRouteX.get(userRouteX.size() - 1)) - 1]
                    [node2Total(userRouteY.get(userRouteY.size() - 1))] != nodeState.Wall){
                userRouteX.add(userRouteX.get(userRouteX.size() - 1) - 1);
                userRouteY.add(userRouteY.get(userRouteY.size() - 1));
            }
            movingDirection = 'N';
        }
        else if(movingDirection == 'R'){
            if(total[node2Total(userRouteX.get(userRouteX.size() - 1)) + 1]
                    [node2Total(userRouteY.get(userRouteY.size() - 1))] != nodeState.Wall){
                userRouteX.add(userRouteX.get(userRouteX.size() - 1) + 1);
                userRouteY.add(userRouteY.get(userRouteY.size() - 1));
            }
            movingDirection = 'N';
        }
        else if(movingDirection == 'U'){
            if(total[node2Total(userRouteX.get(userRouteX.size() - 1))]
                    [node2Total(userRouteY.get(userRouteY.size() - 1)) - 1] != nodeState.Wall){
                userRouteX.add(userRouteX.get(userRouteX.size() - 1));
                userRouteY.add(userRouteY.get(userRouteY.size() - 1) - 1);
            }
            movingDirection = 'N';
        }
        else if(movingDirection == 'D'){
            if(total[node2Total(userRouteX.get(userRouteX.size() - 1))]
                    [node2Total(userRouteY.get(userRouteY.size() - 1)) + 1] != nodeState.Wall){
                userRouteX.add(userRouteX.get(userRouteX.size() - 1));
                userRouteY.add(userRouteY.get(userRouteY.size() - 1) + 1);
            }
            movingDirection = 'N';
        }
        if((userRouteX.get(userRouteX.size() - 1) == endX) &&
                (userRouteY.get(userRouteY.size() - 1) == endY)){
            gameOverTrigger =true;
        }
        repaint();
    }
    public class GameKeyAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e){
            //get key pressed information
            switch(e.getKeyCode()){
                //4 directions
                case KeyEvent.VK_LEFT:
                    if(movingDirection == 'N'){
                        movingDirection = 'L';
                    }
                    break;
                case KeyEvent.VK_RIGHT:
                    if(movingDirection == 'N'){
                        movingDirection = 'R';
                    }
                    break;
                case KeyEvent.VK_UP:
                    if(movingDirection == 'N'){
                        movingDirection = 'U';
                    }
                    break;
                case KeyEvent.VK_DOWN:
                    if(movingDirection == 'N'){
                        movingDirection = 'D';
                    }
                    break;
                case KeyEvent.VK_R:
                    restartTrigger = true;
                    break;
                case KeyEvent.VK_SPACE:
                    hintTrigger = true;
                    break;
            }
        }
    }
}
