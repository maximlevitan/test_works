package ru.mlevitan.app.lift;

import java.util.Scanner;

/**
 * Программа запускается из командной строки, в качестве параметров задается:
 *  - кол-во этажей в подъезде — N (от 5 до 20);
 *  - высота одного этажа;
 *  - скорость лифта при движении в метрах в секунду (ускорением пренебрегаем,
 *    считаем, что когда лифт едет — он сразу едет с определенной скоростью);
 *  - время между открытием и закрытием дверей.
 **/
public class Main {

    private static int floorCount = 20; // floors
    private static double floorHeight = 3.0; // meters
    private static double velocity = 3.0; // m/s
    private static double waitDoorsTimeout = 1.0; // seconds

    private static Lift lift = null;

    public static void main(String[] args) {
        try {
            prepareParameters(args);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: Не верный формат значений аргументов коммандной строки");

            return;
        } catch (RuntimeException e) {
            System.out.println(e.getMessage());

            return;
        }

        lift = new Lift(floorCount, floorHeight, velocity, waitDoorsTimeout);

        listenUserCommand();
    }

    private static void listenUserCommand() {
        Scanner in = new Scanner(System.in);
        String command = null;
        String[] tokens;
        int commandFloor = 0;

        while (true) {
            System.out.println(
                "Для вызова снаружи - комманда 'out' и номер этажа (например: out:10)\n, " +
                "изнутри - комманда 'in' и номер этажа (например: in:7):\n");

            command = in.next();

            if (command == null
                || command.isEmpty()
                || !command.startsWith("out:") && !command.startsWith("in:")
            ) {
                System.out.println("Ошибка: Введена недопустимая комманда");
                continue;
            }

            tokens = command.split(":", 2);
            if (tokens.length != 2 || tokens[1].isEmpty()) {
                System.out.println("Ошибка: Введена невалидная комманда");
                continue;
            }

            try {
                commandFloor = Integer.parseInt(tokens[1]);
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: Не валидный номер этажа");
                continue;
            }

            if (commandFloor < 1 || commandFloor > floorCount) {
                System.out.println("Ошибка: Недопустимый номер этажа");
                continue;
            }

            if (command.startsWith("out:")) {
                lift.callOutside(commandFloor);
            } else {
                lift.callInside(commandFloor);
            }
        }
    }

    private static void prepareParameters(String[] args) throws NumberFormatException {
        int argsCount = args.length;
        if (argsCount < 2) {
            return;
        }

        for (int idx = 0; idx < argsCount; idx++) {
            switch (idx) {
                case 0:
                    floorCount = Integer.parseInt(args[idx]);
                    break;
                case 1:
                    floorHeight = Double.parseDouble(args[idx]);
                    break;
                case 2:
                    velocity = Double.parseDouble(args[idx]);
                    break;
                case 3:
                    waitDoorsTimeout = Double.parseDouble(args[idx]);
                    break;
                default:
            }
        }

        if (floorCount < 5 || floorCount > 20) {
            throw new RuntimeException("Ошибка: Число этажей должно быть в диапазоне от 5 до 20");
        }

        if (floorHeight < 1.9 || floorHeight > 50.0) {
            throw new RuntimeException("Ошибка: Недопустимое значение высоты этажа");
        }

        if (velocity < 0.9 || velocity > 50.0) {
            throw new RuntimeException("Ошибка: Недопустимое значение скорости движения лифта");
        }

        if (waitDoorsTimeout < 0.3 || waitDoorsTimeout > 50.0) {
            throw new RuntimeException("Ошибка: Недопустимое значение задержки на открытие/закрытие дверей");
        }
    }

}
