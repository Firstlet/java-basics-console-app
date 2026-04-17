import java.util.*;

public class Main {
    private static final Scanner SCANNER = new Scanner(System.in);

    public static void main(String[] args) {
        Game game = new Game();
        game.start();
    }

    static class Game {
        private final Random random = new Random();
        private final Map<ResourceType, Integer> resources = new EnumMap<>(ResourceType.class);
        private final List<BuildingType> buildings = new ArrayList<>();

        private Race playerRace;
        private ViewMode viewMode = ViewMode.FIRST_PERSON;

        private int day = 1;
        private int population = 20;
        private int armyPower = 12;
        private int morale = 55;
        private int enemyThreat = 40;
        private boolean running = true;

        Game() {
            for (ResourceType type : ResourceType.values()) {
                resources.put(type, 0);
            }

            resources.put(ResourceType.WOOD, 80);
            resources.put(ResourceType.STONE, 60);
            resources.put(ResourceType.FOOD, 100);
            resources.put(ResourceType.IRON, 20);
            resources.put(ResourceType.GOLD, 10);
        }

        void start() {
            printIntro();
            chooseRace();

            while (running) {
                printStatus();
                printMenu();
                int choice = readInt("Выберите действие: ");

                switch (choice) {
                    case 1 -> gatherResources();
                    case 2 -> buildStructure();
                    case 3 -> trainArmy();
                    case 4 -> scoutEnemies();
                    case 5 -> launchWar();
                    case 6 -> switchViewMode();
                    case 7 -> endDay();
                    case 8 -> showLoreAndHints();
                    case 9 -> {
                        System.out.println("Вы завершили кампанию. Ваш народ будет жить дальше в легендах!");
                        running = false;
                    }
                    default -> System.out.println("Неизвестная команда. Попробуйте еще раз.");
                }

                if (population <= 0 || morale <= 0) {
                    System.out.println("\nВаше королевство пало. Народ покинул земли из-за отчаяния.");
                    running = false;
                }

                if (enemyThreat <= 0) {
                    System.out.println("\nПобеда! Вражеские кланы разгромлены, эпоха войны окончена.");
                    running = false;
                }
            }
        }

        private void printIntro() {
            System.out.println("===============================================");
            System.out.println("         EPOCHS OF TRINITY - Console RPG       ");
            System.out.println("===============================================");
            System.out.println("Вы правитель новой фракции на континенте Трион.");
            System.out.println("Добывайте ресурсы, стройте поселения, развивайте армию и ведите войны.");
            System.out.println("Доступны режимы повествования: от 1-го и 3-го лица.");
            System.out.println();
        }

        private void chooseRace() {
            System.out.println("Выберите расу для кампании:");
            for (Race race : Race.values()) {
                System.out.printf("%d) %s - %s%n", race.id, race.displayName, race.description);
            }

            while (playerRace == null) {
                int raceChoice = readInt("Введите номер расы: ");
                for (Race race : Race.values()) {
                    if (race.id == raceChoice) {
                        playerRace = race;
                        applyRaceBonuses();
                        System.out.println("\nВы выбрали расу: " + race.displayName);
                        System.out.println("Стартовые бонусы применены. Удачи, полководец!\n");
                        return;
                    }
                }
                System.out.println("Неверный выбор. Введите 1, 2 или 3.");
            }
        }

        private void applyRaceBonuses() {
            switch (playerRace) {
                case HUMANS -> {
                    resources.merge(ResourceType.FOOD, 40, Integer::sum);
                    morale += 10;
                }
                case ORCS -> {
                    armyPower += 8;
                    enemyThreat += 10;
                    resources.merge(ResourceType.WOOD, 25, Integer::sum);
                }
                case ELVES -> {
                    resources.merge(ResourceType.GOLD, 20, Integer::sum);
                    resources.merge(ResourceType.WOOD, 35, Integer::sum);
                    morale += 5;
                }
            }
        }

        private void printStatus() {
            System.out.println("\n-------------------- День " + day + " --------------------");
            narrate("Мы стоим в центре столицы и оцениваем положение дел.",
                    "Ваш правитель стоит в центре столицы и оценивает положение дел.");

            System.out.println("Раса: " + playerRace.displayName + " | Вид: " + viewMode.label);
            System.out.printf("Население: %d | Армия: %d | Мораль: %d | Угроза врага: %d%n",
                    population, armyPower, morale, enemyThreat);

            System.out.println("Ресурсы:");
            for (ResourceType type : ResourceType.values()) {
                System.out.printf("- %-7s: %d%n", type.label, resources.get(type));
            }

            System.out.println("Постройки: " + (buildings.isEmpty() ? "нет" : formatBuildings()));
        }

        private String formatBuildings() {
            Map<BuildingType, Long> grouped = new LinkedHashMap<>();
            for (BuildingType building : buildings) {
                grouped.put(building, grouped.getOrDefault(building, 0L) + 1);
            }

            StringBuilder sb = new StringBuilder();
            for (Map.Entry<BuildingType, Long> entry : grouped.entrySet()) {
                if (!sb.isEmpty()) {
                    sb.append(", ");
                }
                sb.append(entry.getKey().nameRu).append(" x").append(entry.getValue());
            }
            return sb.toString();
        }

        private void printMenu() {
            System.out.println("\nДействия:");
            System.out.println("1) Сбор ресурсов");
            System.out.println("2) Строительство");
            System.out.println("3) Тренировать армию");
            System.out.println("4) Разведка врага");
            System.out.println("5) Начать войну");
            System.out.println("6) Переключить вид (1-е / 3-е лицо)");
            System.out.println("7) Завершить день");
            System.out.println("8) Лор и подсказки");
            System.out.println("9) Выйти из игры");
        }

        private void gatherResources() {
            System.out.println("\nВыберите тип экспедиции:");
            System.out.println("1) Лесозаготовка (+дерево, +еда)");
            System.out.println("2) Каменоломня (+камень, +железо)");
            System.out.println("3) Торговый караван (+золото, +еда)");

            int expedition = readInt("Ваш выбор: ");

            int bonus = playerRace == Race.ELVES ? 5 : 0;
            switch (expedition) {
                case 1 -> {
                    int woodGain = 18 + random.nextInt(8) + bonus;
                    int foodGain = 10 + random.nextInt(6);
                    addResource(ResourceType.WOOD, woodGain);
                    addResource(ResourceType.FOOD, foodGain);
                    narrate("Я отправляю отряды в древний лес.",
                            "Правитель отправляет отряды в древний лес.");
                    System.out.printf("Получено: дерево +%d, еда +%d%n", woodGain, foodGain);
                }
                case 2 -> {
                    int stoneGain = 16 + random.nextInt(9);
                    int ironGain = 7 + random.nextInt(5) + (playerRace == Race.ORCS ? 3 : 0);
                    addResource(ResourceType.STONE, stoneGain);
                    addResource(ResourceType.IRON, ironGain);
                    narrate("Я усиливаю добычу в скалистом ущелье.",
                            "Правитель усиливает добычу в скалистом ущелье.");
                    System.out.printf("Получено: камень +%d, железо +%d%n", stoneGain, ironGain);
                }
                case 3 -> {
                    int goldGain = 8 + random.nextInt(8) + (playerRace == Race.HUMANS ? 3 : 0);
                    int foodGain = 12 + random.nextInt(5);
                    addResource(ResourceType.GOLD, goldGain);
                    addResource(ResourceType.FOOD, foodGain);
                    narrate("Я открываю новый торговый путь через степи.",
                            "Правитель открывает новый торговый путь через степи.");
                    System.out.printf("Получено: золото +%d, еда +%d%n", goldGain, foodGain);
                }
                default -> {
                    System.out.println("Экспедиция сорвалась — неверная команда.");
                    morale -= 2;
                }
            }
        }

        private void buildStructure() {
            System.out.println("\nДоступные постройки:");
            for (BuildingType type : BuildingType.values()) {
                System.out.printf("%d) %s [%s]%n", type.id, type.nameRu, type.costDescription());
            }

            int pick = readInt("Что строим: ");
            BuildingType chosen = BuildingType.byId(pick);

            if (chosen == null) {
                System.out.println("Такой постройки не существует.");
                return;
            }

            if (!hasEnough(chosen.costs)) {
                System.out.println("Недостаточно ресурсов для строительства " + chosen.nameRu + ".");
                return;
            }

            for (Map.Entry<ResourceType, Integer> cost : chosen.costs.entrySet()) {
                addResource(cost.getKey(), -cost.getValue());
            }

            buildings.add(chosen);
            population += chosen.populationBonus;
            morale += chosen.moraleBonus;
            armyPower += chosen.armyBonus;

            narrate("Я закладываю фундамент новой постройки: " + chosen.nameRu + ".",
                    "Правитель закладывает фундамент новой постройки: " + chosen.nameRu + ".");
            System.out.println("Постройка завершена: " + chosen.nameRu);
        }

        private void trainArmy() {
            Map<ResourceType, Integer> costs = Map.of(
                    ResourceType.FOOD, 20,
                    ResourceType.IRON, 12
            );

            if (!hasEnough(costs)) {
                System.out.println("Для тренировки армии не хватает еды или железа.");
                return;
            }

            for (Map.Entry<ResourceType, Integer> cost : costs.entrySet()) {
                addResource(cost.getKey(), -cost.getValue());
            }

            int gain = 8 + random.nextInt(8) + (playerRace == Race.ORCS ? 4 : 0);
            armyPower += gain;
            morale += 3;

            narrate("Я лично наблюдаю за учениями на плацу.",
                    "Правитель лично наблюдает за учениями на плацу.");
            System.out.println("Армия стала сильнее на " + gain + " очков силы.");
        }

        private void scoutEnemies() {
            int info = 8 + random.nextInt(9);
            enemyThreat = Math.max(0, enemyThreat - info / 2);
            morale += 2;

            narrate("Я отправляю разведчиков в земли противника.",
                    "Правитель отправляет разведчиков в земли противника.");
            System.out.println("Разведка успешна. Осведомленность повышена (" + info + "), угроза немного снижена.");
        }

        private void launchWar() {
            int warCostFood = 30;
            int warCostGold = 10;

            if (resources.get(ResourceType.FOOD) < warCostFood || resources.get(ResourceType.GOLD) < warCostGold) {
                System.out.println("Недостаточно припасов для крупного военного похода.");
                return;
            }

            addResource(ResourceType.FOOD, -warCostFood);
            addResource(ResourceType.GOLD, -warCostGold);

            int enemyPower = enemyThreat + random.nextInt(25);
            int playerPower = armyPower + random.nextInt(20) + buildingsArmyBonus();

            narrate("Я веду войска на решающий штурм.",
                    "Правитель ведет войска на решающий штурм.");
            System.out.printf("Сила ваших войск: %d | Сила врага: %d%n", playerPower, enemyPower);

            if (playerPower >= enemyPower) {
                int threatDrop = 14 + random.nextInt(12);
                enemyThreat = Math.max(0, enemyThreat - threatDrop);
                morale += 10;
                population = Math.max(1, population - random.nextInt(4));
                System.out.println("Победа в битве! Угроза врага снижена на " + threatDrop + ".");
            } else {
                int losses = 3 + random.nextInt(6);
                int moraleLoss = 6 + random.nextInt(8);
                population = Math.max(0, population - losses);
                morale = Math.max(0, morale - moraleLoss);
                enemyThreat += 5;
                System.out.println("Поражение... Потери населения: " + losses + ", мораль упала на " + moraleLoss + ".");
            }
        }

        private int buildingsArmyBonus() {
            int bonus = 0;
            for (BuildingType building : buildings) {
                bonus += building.armyBonus;
            }
            return bonus;
        }

        private void switchViewMode() {
            viewMode = (viewMode == ViewMode.FIRST_PERSON)
                    ? ViewMode.THIRD_PERSON
                    : ViewMode.FIRST_PERSON;
            System.out.println("Режим повествования переключен: " + viewMode.label);
        }

        private void showLoreAndHints() {
            System.out.println("\n--- Лор мира Трион ---");
            System.out.println("После Пепельного Раскола три великие расы начали борьбу за артефакты света.");
            System.out.println("Люди удерживают торговые города, орки контролируют горные перевалы,");
            System.out.println("а эльфы защищают лесные святилища и древние знания.");
            System.out.println("Подсказки:");
            System.out.println("- Не забывайте балансировать строительство и армию.");
            System.out.println("- Разведка перед войной снижает риск тяжелых потерь.");
            System.out.println("- Еда критична: без нее сложно тренировать армию и вести кампанию.");
        }

        private void endDay() {
            day++;

            int foodConsumption = Math.max(10, population / 2);
            addResource(ResourceType.FOOD, -foodConsumption);

            if (resources.get(ResourceType.FOOD) < 0) {
                int deficit = -resources.get(ResourceType.FOOD);
                resources.put(ResourceType.FOOD, 0);
                int popLoss = Math.max(1, deficit / 5);
                population = Math.max(0, population - popLoss);
                morale = Math.max(0, morale - 8);
                System.out.println("Голод! Нехватка еды привела к потере населения: " + popLoss);
            }

            enemyThreat += 2 + random.nextInt(4);

            if (hasBuilding(BuildingType.FARM)) {
                addResource(ResourceType.FOOD, 15);
            }
            if (hasBuilding(BuildingType.MINE)) {
                addResource(ResourceType.IRON, 6);
                addResource(ResourceType.STONE, 8);
            }
            if (hasBuilding(BuildingType.BARRACKS)) {
                armyPower += 2;
            }

            morale = Math.min(100, Math.max(0, morale));
            enemyThreat = Math.max(0, enemyThreat);
            System.out.println("День завершен. Мир меняется вместе с вашими решениями.");
        }

        private boolean hasBuilding(BuildingType type) {
            return buildings.contains(type);
        }

        private boolean hasEnough(Map<ResourceType, Integer> costs) {
            for (Map.Entry<ResourceType, Integer> entry : costs.entrySet()) {
                if (resources.get(entry.getKey()) < entry.getValue()) {
                    return false;
                }
            }
            return true;
        }

        private void addResource(ResourceType type, int delta) {
            resources.put(type, resources.get(type) + delta);
        }

        private void narrate(String firstPerson, String thirdPerson) {
            if (viewMode == ViewMode.FIRST_PERSON) {
                System.out.println("[1-е лицо] " + firstPerson);
            } else {
                System.out.println("[3-е лицо] " + thirdPerson);
            }
        }

        private int readInt(String prompt) {
            while (true) {
                System.out.print(prompt);
                String input = SCANNER.nextLine();
                try {
                    return Integer.parseInt(input.trim());
                } catch (NumberFormatException e) {
                    System.out.println("Введите целое число.");
                }
            }
        }
    }

    enum ViewMode {
        FIRST_PERSON("1-е лицо"),
        THIRD_PERSON("3-е лицо");

        final String label;

        ViewMode(String label) {
            this.label = label;
        }
    }

    enum ResourceType {
        WOOD("Дерево"),
        STONE("Камень"),
        FOOD("Еда"),
        IRON("Железо"),
        GOLD("Золото");

        final String label;

        ResourceType(String label) {
            this.label = label;
        }
    }

    enum Race {
        HUMANS(1, "Люди", "Универсальная раса: бонус к торговле и морали."),
        ORCS(2, "Орки", "Воинственная раса: сильнее армия и добыча металла."),
        ELVES(3, "Эльфы", "Мастера природы: больше дерева и золота, стабильная экономика.");

        final int id;
        final String displayName;
        final String description;

        Race(int id, String displayName, String description) {
            this.id = id;
            this.displayName = displayName;
            this.description = description;
        }
    }

    enum BuildingType {
        FARM(1, "Ферма", 6, 2, 0, Map.of(
                ResourceType.WOOD, 30,
                ResourceType.STONE, 12
        )),
        BARRACKS(2, "Казармы", 0, 3, 6, Map.of(
                ResourceType.WOOD, 20,
                ResourceType.STONE, 25,
                ResourceType.IRON, 15
        )),
        MINE(3, "Шахта", 0, 1, 2, Map.of(
                ResourceType.WOOD, 10,
                ResourceType.STONE, 30,
                ResourceType.IRON, 10
        )),
        WALL(4, "Крепостная стена", 0, 5, 4, Map.of(
                ResourceType.STONE, 40,
                ResourceType.WOOD, 10,
                ResourceType.GOLD, 8
        )),
        MARKET(5, "Рынок", 2, 4, 0, Map.of(
                ResourceType.WOOD, 15,
                ResourceType.STONE, 10,
                ResourceType.GOLD, 20
        ));

        final int id;
        final String nameRu;
        final int populationBonus;
        final int moraleBonus;
        final int armyBonus;
        final Map<ResourceType, Integer> costs;

        BuildingType(int id,
                     String nameRu,
                     int populationBonus,
                     int moraleBonus,
                     int armyBonus,
                     Map<ResourceType, Integer> costs) {
            this.id = id;
            this.nameRu = nameRu;
            this.populationBonus = populationBonus;
            this.moraleBonus = moraleBonus;
            this.armyBonus = armyBonus;
            this.costs = costs;
        }

        static BuildingType byId(int id) {
            for (BuildingType type : values()) {
                if (type.id == id) {
                    return type;
                }
            }
            return null;
        }

        String costDescription() {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<ResourceType, Integer> entry : costs.entrySet()) {
                if (!sb.isEmpty()) {
                    sb.append(", ");
                }
                sb.append(entry.getKey().label).append(": ").append(entry.getValue());
            }
            return sb.toString();
        }
    }
}
