package ru.mlevitan.app.lift;

import java.util.concurrent.ConcurrentSkipListSet;

public class Lift extends Thread {

    private State status = State.WAIT;

    private int currentFloor = 1;
    private int movingFloor = 0;

    private final int floorCount;
    private final double floorHeight;
    private final double velocity;
    private final double waitDoorsTimeout;

    private enum State {
        WAIT, OPEN_DOORS, CLOSE_DOORS, MOVING
    };

    private ConcurrentSkipListSet<Integer> commands = new ConcurrentSkipListSet<>();

    public Lift(int floorCount, double floorHeight, double velocity, double waitDoorsTimeout) {
        super("thread-of-lift");

        this.floorCount = floorCount;
        this.floorHeight = floorHeight;
        this.velocity = velocity;
        this.waitDoorsTimeout = waitDoorsTimeout;

        this.setDaemon(true);
        start();
    }

    @Override
    public void run() {
        int commandsCont;
        boolean needDispalyWaitStatus = true;

        try {
            while (true) {
                commandsCont = commands.size();

                if (commandsCont > 0) {
                    needDispalyWaitStatus = true;

                    // Движение лифта на очередной этаж
                    moving();
                } else {
                    status = State.WAIT;
                    if (needDispalyWaitStatus) {
                        needDispalyWaitStatus = false;

                        displayStatus();
                    }
                }

                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Вызвать лифт снаружи на свой этаж
     */
    public void callOutside(int floor) {
        if (floor > 0 && floor <= floorCount) {
            commands.add(floor);
        } else {
            System.out.printf("Указанный этаж %d не существует.\n", floor);
        }
    }

    /**
     * Направить лифт изнутри на определенный этаж
     */
    public void callInside(int floor) {
        if (floor > 0 && floor <= floorCount) {
            commands.add(floor);
        } else {
            System.out.printf("Указанный этаж %d не существует.\n", floor);
        }
    }

    /**
     * Движение лифта по этажам
     */
    private void moving() throws InterruptedException {
        int nextFloor = getNextFloor();

        if (nextFloor > 0 && nextFloor <= floorCount && nextFloor != currentFloor) {
            boolean isMovedOnFloor = false;
            if (nextFloor < currentFloor) {
                status = State.MOVING;

                isMovedOnFloor = moveDown(nextFloor);
            } else if (nextFloor > currentFloor) {
                status = State.MOVING;

                isMovedOnFloor = moveUp(nextFloor);
            }

            if (isMovedOnFloor) {
                // Высадка или посадка пассажира(ов), удаляем выполненную задачу, т.к. лифт уже остановился на этаже
                if (status != State.OPEN_DOORS) {
                    openDoors();
                }

                commands.remove(nextFloor);

                if (status != State.CLOSE_DOORS) {
                    closeDoors();
                }
            }
        } else if (nextFloor == currentFloor) {
            // Открываем лифт на том этаже
            if (status != State.OPEN_DOORS) {
                openDoors();
            }

            commands.remove(nextFloor);

            if (status != State.CLOSE_DOORS) {
                closeDoors();
            }
        } else {
            commands.remove(nextFloor);
        }
    }

    /**
     * Едит вниз
     *
     * @param floor
     * @return boolean
     * @throws InterruptedException
     */
    private boolean moveDown(int floor) throws InterruptedException {
        for (movingFloor = currentFloor; movingFloor >= floor; movingFloor--) {
            Thread.sleep((long) (floorHeight / velocity) * 1000);
            currentFloor = movingFloor;
            displayStatus();
        }

        return true;
    }

    /**
     * Едит вверх
     *
     * @param floor
     * @return boolean
     * @throws InterruptedException
     */
    private boolean moveUp(int floor) throws InterruptedException {
        for (movingFloor = currentFloor; movingFloor <= floor; movingFloor++) {
            Thread.sleep((long) (floorHeight / velocity) * 1000);
            currentFloor = movingFloor;
            displayStatus();
        }

        return true;
    }

    /**
     * Получить очередной этаж
     *
     * @return int
     */
    private int getNextFloor() {
        int nextFloor = currentFloor;
        int minDistance = floorCount;

        int commandsCont = commands.size();
        if (commandsCont > 0) {
            // Ищем первоочередной блажайший этаж
            for (int floor : commands) {
                int distance = Math.abs(floor - currentFloor);

                if (distance < minDistance) {
                    minDistance = distance;
                    nextFloor = floor;
                }
            }
        }

        return nextFloor;
    }

    /**
     * Закрыть двери
     *
     * @throws InterruptedException
     */
    private void closeDoors() throws InterruptedException  {
        status = State.CLOSE_DOORS;

        Thread.sleep((long) (waitDoorsTimeout * 1000));
        displayStatus();
    }

    /**
     * Открыть двери
     *
     * @throws InterruptedException
     */
    private void openDoors() throws InterruptedException  {
        status = State.OPEN_DOORS;

        displayStatus();
        Thread.sleep((long) (waitDoorsTimeout * 1000));
    }

    /**
     * Вывести состояние
     */
    public void displayStatus() {
        switch (status) {
            case MOVING:
                System.out.printf("Лифт на %d этаже.\n", movingFloor);
                break;

            case OPEN_DOORS:
                System.out.println("Лифт открыл двери.");
                break;

            case CLOSE_DOORS:
                System.out.println("Лифт закрыл двери.");
                break;

            case WAIT:
                System.out.println("Лифт свободен и ожидает вызова.");
                break;
        }
    }

}
