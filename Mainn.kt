import java.io.File

data class BusRoute(
    val id: Int,
    var routeNumber: String,
    var departure: String,
    var destination: String,
    var departureTime: String,
    var price: Double,
    var seatsTotal: Int,
    var seatsAvailable: Int,
    var status: String
)

val routes = mutableListOf<BusRoute>()
val allowedStatuses = listOf("Активен", "Отменён", "Задержан", "Завершён")
var nextId = 1
val csvFile = "routes.csv"

// ===== ВСПОМОГАТЕЛЬНЫЕ ФУНКЦИИ =====
fun readInt(prompt: String): Int? {
    print(prompt)
    return readlnOrNull()?.trim()?.toIntOrNull()
}

fun readDouble(prompt: String): Double? {
    print(prompt)
    return readlnOrNull()?.trim()?.toDoubleOrNull()
}

fun readString(prompt: String): String {
    print(prompt)
    return readlnOrNull()?.trim() ?: ""
}

fun printSeparator() = println("-".repeat(110)) //вывод рейсов

fun printRoutes(list: List<BusRoute>) {
    if (list.isEmpty()) { println("Список рейсов пуст."); return }
    printSeparator()
    println("%-4s | %-8s | %-16s | %-16s | %-8s | %-8s | %-6s | %-7s | %-10s".format(
        "ID", "Маршрут", "Откуда", "Куда", "Время", "Цена", "Мест", "Своб.", "Статус"))
    printSeparator()
    for (r in list) {
        println("%-4d | %-8s | %-16s | %-16s | %-8s | %-8.2f | %-6d | %-7d | %-10s".format(
            r.id, r.routeNumber, r.departure, r.destination,
            r.departureTime, r.price, r.seatsTotal, r.seatsAvailable, r.status))
    }
    printSeparator()
}

// ===== ЗАГРУЗКА ИЗ CSV =====
fun loadFromCsv(showSuccessMessage: Boolean = true, missingFileIsError: Boolean = true) {
    val file = File(csvFile)
    if (!file.exists()) {
        if (missingFileIsError) println("Файл '$csvFile' не найден. Начат пустой список.")
        return
    }
    routes.clear()
    var errors = 0
    val usedIds = mutableSetOf<Int>()
    file.readLines().drop(1).forEachIndexed { lineIndex, line ->
        val parts = line.split(";")
        if (parts.size < 9) { errors++; return@forEachIndexed }
        try {
            val id = parts[0].trim().toInt()
            if (id in usedIds) { errors++; return@forEachIndexed }
            val price = parts[5].trim().toDouble()
            if (price < 0) { errors++; return@forEachIndexed }
            val seatsTotal = parts[6].trim().toInt()
            val seatsAvailable = parts[7].trim().toInt()
            val status = parts[8].trim()
            if (status !in allowedStatuses) { errors++; return@forEachIndexed }
            usedIds.add(id)
            routes.add(BusRoute(id, parts[1].trim(), parts[2].trim(), parts[3].trim(),
                parts[4].trim(), price, seatsTotal, seatsAvailable, status))
        } catch (e: NumberFormatException) { errors++ }
    }
    nextId = (routes.maxOfOrNull { it.id } ?: 0) + 1
    if (showSuccessMessage) {
        println("Загружено ${routes.size} рейс(ов) из файла '$csvFile'.")
        if (errors > 0) println("Пропущено строк с ошибками: $errors.")
    }
}

// ===== СОХРАНЕНИЕ В CSV =====
fun saveToCsv() {
    val file = File(csvFile)
    val header = "id;routeNumber;departure;destination;departureTime;price;seatsTotal;seatsAvailable;status"
    val lines = mutableListOf(header)
    for (r in routes) {
        lines.add("${r.id};${r.routeNumber};${r.departure};${r.destination};${r.departureTime};${r.price};${r.seatsTotal};${r.seatsAvailable};${r.status}")
    }
    file.writeText(lines.joinToString("\n"))
    println("Данные успешно сохранены в файл '$csvFile'. Записей: ${routes.size}.")
}

// ===== ПОКАЗАТЬ ВСЕ РЕЙСЫ =====
fun showRoutes() {
    println("\n=== Все рейсы ===")
    printRoutes(routes)
}

// ===== ДОБАВИТЬ РЕЙС =====
fun addRoute() {
    println("\n=== Добавление нового рейса ===")
    val routeNumber = readString("Номер маршрута (например, 101): ")
    if (routeNumber.isBlank()) { println("Ошибка: номер маршрута не может быть пустым."); return }
    val departure = readString("Пункт отправления: ")
    if (departure.isBlank()) { println("Ошибка: пункт отправления не может быть пустым."); return }
    val destination = readString("Пункт назначения: ")
    if (destination.isBlank()) { println("Ошибка: пункт назначения не может быть пустым."); return }
    val departureTime = readString("Время отправления (чч:мм): ")
    if (departureTime.isBlank()) { println("Ошибка: время отправления не может быть пустым."); return }
    val price = readDouble("Цена билета (руб.): ")
    if (price == null || price < 0) { println("Ошибка: введите корректную неотрицательную цену."); return }
    val seatsTotal = readInt("Общее количество мест: ")
    if (seatsTotal == null || seatsTotal <= 0) { println("Ошибка: количество мест должно быть положительным."); return }
    val seatsAvailable = readInt("Количество свободных мест: ")
    if (seatsAvailable == null || seatsAvailable < 0 || seatsAvailable > seatsTotal) {
        println("Ошибка: количество свободных мест должно быть от 0 до $seatsTotal."); return
    }
    println("Доступные статусы: ${allowedStatuses.joinToString(", ")}")
    val status = readString("Статус рейса: ")
    if (status !in allowedStatuses) { println("Ошибка: недопустимый статус."); return }
    routes.add(BusRoute(nextId++, routeNumber, departure, destination,
        departureTime, price, seatsTotal, seatsAvailable, status))
    println("Рейс успешно добавлен с ID = ${nextId - 1}.")
}

// ===== РЕДАКТИРОВАТЬ РЕЙС =====
fun editRoute() {
    println("\n=== Редактирование рейса ===")
    val id = readInt("Введите ID рейса: ") ?: run { println("Ошибка: введите целое число."); return }
    val route = routes.find { it.id == id } ?: run { println("Рейс с ID=$id не найден."); return }
    println("Текущие данные:"); printRoutes(listOf(route))
    println("Оставьте поле пустым для сохранения текущего значения.")
    val routeNumber = readString("Номер маршрута [${route.routeNumber}]: ")
    if (routeNumber.isNotBlank()) route.routeNumber = routeNumber
    val departure = readString("Пункт отправления [${route.departure}]: ")
    if (departure.isNotBlank()) route.departure = departure
    val destination = readString("Пункт назначения [${route.destination}]: ")
    if (destination.isNotBlank()) route.destination = destination
    val departureTime = readString("Время отправления [${route.departureTime}]: ")
    if (departureTime.isNotBlank()) route.departureTime = departureTime
    val priceStr = readString("Цена билета [${route.price}]: ")
    if (priceStr.isNotBlank()) {
        val price = priceStr.toDoubleOrNull()
        if (price == null || price < 0) println("Ошибка: цена не изменена.")
        else route.price = price
    }
    val seatsTotalStr = readString("Общее количество мест [${route.seatsTotal}]: ")
    if (seatsTotalStr.isNotBlank()) {
        val seats = seatsTotalStr.toIntOrNull()
        if (seats == null || seats <= 0) println("Ошибка: поле не изменено.")
        else route.seatsTotal = seats
    }
    val seatsAvailableStr = readString("Свободных мест [${route.seatsAvailable}]: ")
    if (seatsAvailableStr.isNotBlank()) {
        val seats = seatsAvailableStr.toIntOrNull()
        if (seats == null || seats < 0 || seats > route.seatsTotal) println("Ошибка: поле не изменено.")
        else route.seatsAvailable = seats
    }
    println("Доступные статусы: ${allowedStatuses.joinToString(", ")}")
    val status = readString("Статус [${route.status}]: ")
    if (status.isNotBlank()) {
        if (status !in allowedStatuses) println("Ошибка: статус не изменён.")
        else route.status = status
    }
    println("Рейс с ID=$id успешно обновлён.")
}

// ===== УДАЛИТЬ РЕЙС =====
fun deleteRoute() {
    println("\n=== Удаление рейса ===")
    val id = readInt("Введите ID рейса: ") ?: run { println("Ошибка: введите целое число."); return }
    val route = routes.find { it.id == id } ?: run { println("Рейс с ID=$id не найден."); return }
    routes.remove(route)
    println("Рейс с ID=$id успешно удалён.")
}

// ===== ПОИСК РЕЙСОВ =====
fun findRoutes() {
    println("\n=== Поиск рейсов ===")
    println("1. По пункту отправления")
    println("2. По пункту назначения")
    println("3. По номеру маршрута")
    println("4. По статусу")
    when (readInt("Выберите критерий: ")) {
        1 -> { val q = readString("Пункт отправления: ")
               val r = routes.filter { it.departure.contains(q, ignoreCase = true) }
               if (r.isEmpty()) println("Рейсов не найдено.") else printRoutes(r) }
        2 -> { val q = readString("Пункт назначения: ")
               val r = routes.filter { it.destination.contains(q, ignoreCase = true) }
               if (r.isEmpty()) println("Рейсов не найдено.") else printRoutes(r) }
        3 -> { val q = readString("Номер маршрута: ")
               val r = routes.filter { it.routeNumber.contains(q, ignoreCase = true) }
               if (r.isEmpty()) println("Маршрут не найден.") else printRoutes(r) }
        4 -> { println("Статусы: ${allowedStatuses.joinToString(", ")}")
               val st = readString("Статус: ")
               val r = routes.filter { it.status.equals(st, ignoreCase = true) }
               if (r.isEmpty()) println("Рейсов не найдено.") else printRoutes(r) }
        else -> println("Ошибка: выберите пункт от 1 до 4.")
    }
}

// ===== СОРТИРОВКА =====
fun sortRoutes() {
    println("\n=== Сортировка рейсов ===")
    println("1. По ID")
    println("2. По номеру маршрута")
    println("3. По цене (возрастание)")
    println("4. По цене (убывание)")
    println("5. По количеству свободных мест")
    println("6. По статусу")
    val sorted = when (readInt("Выберите поле: ")) {
        1 -> routes.sortedBy { it.id }
        2 -> routes.sortedBy { it.routeNumber }
        3 -> routes.sortedBy { it.price }
        4 -> routes.sortedByDescending { it.price }
        5 -> routes.sortedByDescending { it.seatsAvailable }
        6 -> routes.sortedBy { it.status }
        else -> { println("Ошибка: выберите пункт от 1 до 6."); return }
    }
    printRoutes(sorted)
}

// ===== АГРЕГИРОВАННЫЕ ПОКАЗАТЕЛИ =====
fun showStats() {
    println("\n=== Агрегированные показатели ===")
    if (routes.isEmpty()) { println("Нет данных для анализа."); return }
    println("Всего рейсов: ${routes.size}")
    println("Средняя цена билета: %.2f руб.".format(routes.map { it.price }.average()))
    println("Минимальная цена: %.2f руб.".format(routes.minOf { it.price }))
    println("Максимальная цена: %.2f руб.".format(routes.maxOf { it.price }))
    println("Сумма всех цен: %.2f руб.".format(routes.sumOf { it.price }))
    println("Всего мест: ${routes.sumOf { it.seatsTotal }}")
    println("Свободных мест: ${routes.sumOf { it.seatsAvailable }}")
    println("\nКоличество рейсов по статусам:")
    val byStatus: Map<String, Int> = routes.groupingBy { it.status }.eachCount()
    for ((status, count) in byStatus) println("  $status: $count")
}

// ===== МЕНЮ ВВОДА/ВЫВОДА =====
fun inputOutputMenu() {
    println("\n=== Ввод / Вывод данных ===")
    println("1. Загрузить данные из CSV-файла")
    println("2. Сохранить данные в CSV-файл")
    println("3. Добавить новый рейс")
    when (readInt("Выберите действие: ")) {
        1 -> loadFromCsv()
        2 -> saveToCsv()
        3 -> addRoute()
        else -> println("Ошибка: выберите пункт от 1 до 3.")
    }
}

// ===== ГЛАВНАЯ ФУНКЦИЯ =====
fun main() {
    loadFromCsv(showSuccessMessage = false, missingFileIsError = false)
    var choice: Int?
    do {
        println()
        println("=== СИСТЕМА УЧЁТА АВТОБУСНЫХ РЕЙСОВ ===")
        println("Главное меню:")
        println("1. Ввод/вывод данных")
        println("2. Отобразить все рейсы")
        println("3. Найти рейс")
        println("4. Редактировать рейс")
        println("5. Удалить рейс")
        println("6. Агрегированные показатели")
        println("7. Сортировка рейсов")
        println("8. Выход")
        choice = readInt("Введите выбор: ")
        when (choice) {
            1 -> inputOutputMenu()
            2 -> showRoutes()
            3 -> findRoutes()
            4 -> editRoute()
            5 -> deleteRoute()
            6 -> showStats()
            7 -> sortRoutes()
            8 -> println("Выход из программы...")
            else -> println("Ошибка: введите число от 1 до 8.")
        }
    } while (choice != 8)
}
